import {
  authFetch,
  clearAccessToken,
  clearFieldErrors,
  readApiBody,
  readAccessToken,
  showFieldErrors,
  validateRequiredFields,
} from './auth-client.js';
import { adaptMypagePayload, filterMypageRecords, mypageViews } from './mypage-api-adapters.js';

const RECORD_PAGE_SIZE = 6;
const recordsBySource = new Map();
const failuresBySource = new Map();
let authorizedSources = [];
let visibleRecordCount = RECORD_PAGE_SIZE;

const currentView = location.pathname.split('/').filter(Boolean).at(-1);
const viewKey = currentView === 'mypage' ? 'profile' : currentView;
const view = mypageViews[viewKey] || mypageViews.profile;

if (!readAccessToken()) redirectToLogin();

document.querySelector('[data-view-title]').textContent = view.title;
document.querySelector('[data-view-kicker]').textContent = view.kicker;
document.querySelector(`[data-view="${viewKey}"]`)?.setAttribute('aria-current', 'page');
configureViewStates();
configureRecordControls();

let profileResponse;
try {
  profileResponse = await authFetch('/api/v1/account/me');
} catch (error) {
  showFailure('계정 정보를 불러오지 못했습니다. 네트워크 연결을 확인해 주세요.');
  throw error;
}
if (profileResponse.status === 401) redirectToLogin();
if (!profileResponse.ok) {
  showFailure('계정 정보를 불러오지 못했습니다.');
  throw new Error('Unable to load account profile');
}

const profile = await profileResponse.json();
renderProfileSummary(profile);

if (viewKey === 'profile') {
  document.querySelector('[data-profile-panel]').hidden = false;
  document.querySelector('[data-account-actions]').hidden = false;
  document.querySelector('[data-loading-state]').hidden = true;
  configureAccountActions(profile);
} else if (viewKey === 'reports') {
  await configureReportView();
} else {
  authorizedSources = view.sources.filter((source) => !source.sellerOnly || profile.role === 'SELLER');
  await loadRecords(authorizedSources);
}

async function configureReportView() {
  document.querySelector('[data-loading-state]').hidden = true;
  document.querySelector('[data-report-panel]').hidden = false;
  const form = document.querySelector('[data-report-form]');
  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const created = await submitJsonForm(form, '/api/v1/reports', 'POST', {
      title: form.elements.title.value,
      message: form.elements.message.value,
    });
    if (!created) return;
    form.reset();
    showFormMessage(form, '관리자에게 신고 내용을 전달했습니다.', true);
    await loadReports(0);
  });
  document.querySelector('[data-report-refresh]').addEventListener('click', () => loadReports(0));
  await loadReports(0);
}

async function loadReports(page) {
  const response = await authFetch(`/api/v1/reports/mine?page=${page}&size=10`);
  if (response.status === 401) redirectToLogin();
  const body = await readApiBody(response);
  if (!response.ok) {
    document.querySelector('[data-report-list]').replaceChildren();
    document.querySelector('[data-report-empty]').hidden = false;
    return;
  }
  const list = document.querySelector('[data-report-list]');
  list.replaceChildren(...body.content.map(reportCard));
  document.querySelector('[data-report-empty]').hidden = body.content.length !== 0;
  renderReportPagination(body);
}

function reportCard(report) {
  const card = document.createElement('article');
  card.className = 'report-card';
  const heading = document.createElement('div');
  const title = document.createElement('h3');
  title.textContent = report.title;
  const status = document.createElement('span');
  status.className = 'status-chip';
  status.dataset.status = report.status;
  status.textContent = reportStatusLabel(report.status);
  heading.append(title, status);
  const message = document.createElement('p');
  message.textContent = report.message;
  const date = document.createElement('small');
  date.textContent = new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' })
    .format(new Date(report.createdAt));
  card.append(heading, message, date);
  if (report.adminResponse) {
    const response = document.createElement('blockquote');
    response.textContent = `관리자 답변: ${report.adminResponse}`;
    card.append(response);
  }
  return card;
}

function renderReportPagination(page) {
  const container = document.querySelector('[data-report-pagination]');
  container.replaceChildren();
  for (let index = 0; index < page.totalPages; index += 1) {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = index + 1;
    if (index === page.page) button.setAttribute('aria-current', 'page');
    button.addEventListener('click', () => loadReports(index));
    container.append(button);
  }
}

function reportStatusLabel(status) {
  return { PENDING: '접수', IN_REVIEW: '검토 중', RESOLVED: '처리 완료', REJECTED: '반려' }[status] || status;
}

async function loadRecords(sources) {
  if (sources.length === 0) {
    showState('pending');
    return;
  }

  setRetryButtonsDisabled(true);
  if (recordsBySource.size === 0) showState('loading');
  try {
    const results = await Promise.all(sources.map(fetchSource));
    if (results.some(({ response }) => response?.status === 401)) redirectToLogin();

    for (const result of results) {
      if (!result.response?.ok) {
        failuresBySource.set(result.source.key, result);
        continue;
      }
      try {
        const payload = await readApiBody(result.response);
        recordsBySource.set(result.source.key, adaptMypagePayload(result.source.key, payload));
        failuresBySource.delete(result.source.key);
      } catch (error) {
        failuresBySource.set(result.source.key, {...result, error});
      }
    }
    renderRecordView();
  } finally {
    setRetryButtonsDisabled(false);
  }
}

async function fetchSource(source) {
  try {
    return {source, response: await authFetch(source.endpoint)};
  } catch (error) {
    return {source, response: null, error};
  }
}

function renderRecordView() {
  hideDataStates();
  const records = [...recordsBySource.values()]
    .flat()
    .sort((left, right) => right.sortAt - left.sortAt);
  const hasSuccessfulSource = authorizedSources.some(({key}) => recordsBySource.has(key));

  if (records.length === 0) {
    document.querySelector('[data-record-tools]').hidden = true;
    renderRecords([]);
    if (hasSuccessfulSource) {
      showState('empty');
      showPartialState();
    } else if ([...failuresBySource.values()].every(({response}) => [404, 405].includes(response?.status))) {
      showState('pending');
    } else {
      showFailure(recordFailureMessage());
    }
    return;
  }

  document.querySelector('[data-record-tools]').hidden = false;
  syncStatusOptions(records);
  showPartialState();
  renderFilteredRecords(records);
}

function renderFilteredRecords(records) {
  const query = document.querySelector('[data-record-search]').value;
  const statusCode = document.querySelector('[data-record-status]').value;
  const filtered = filterMypageRecords(records, query, statusCode);
  const visible = filtered.slice(0, visibleRecordCount);
  renderRecords(visible);

  const summary = document.querySelector('[data-record-summary]');
  summary.textContent = filtered.length === records.length
    ? `전체 ${records.length}건`
    : `전체 ${records.length}건 중 ${filtered.length}건`;
  document.querySelector('[data-filter-empty]').hidden = filtered.length !== 0;
  const loadMore = document.querySelector('[data-load-more]');
  loadMore.hidden = visible.length >= filtered.length;
  loadMore.textContent = `${Math.min(RECORD_PAGE_SIZE, filtered.length - visible.length)}건 더 보기`;
}

function renderProfileSummary(profile) {
  document.querySelector('[data-profile-name]').textContent = `${profile.name}님의 거래`;
  document.querySelector('[data-profile-email]').textContent = profile.email;
  document.querySelector('[data-account-status]').textContent = {
    ADMIN: '관리자',
    SELLER: '판매자 계정',
    USER: '활성 계정',
  }[profile.role] || '계정 확인';
  document.querySelectorAll('[data-profile-field]').forEach((element) => {
    const value = profile[element.dataset.profileField];
    element.textContent = profileLabel(element.dataset.profileField, value);
  });
  document.querySelectorAll('[data-seller-navigation]').forEach((element) => {
    element.hidden = profile.role !== 'SELLER';
  });
}

function configureAccountActions(profile) {
  const nameForm = document.querySelector('[data-name-form]');
  nameForm.elements.name.value = profile.name;
  nameForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const updated = await submitJsonForm(nameForm, '/api/v1/account/me', 'PATCH', {
      name: nameForm.elements.name.value,
    });
    if (!updated) return;
    renderProfileSummary(updated);
    showFormMessage(nameForm, '이름을 변경했습니다.', true);
  });

  configureSellerAction(profile);
  const withdrawForm = document.querySelector('[data-withdraw-form]');
  withdrawForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!validateRequiredFields(withdrawForm)) return;
    if (!window.confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;

    const response = await submitJsonForm(withdrawForm, '/api/v1/account/me', 'DELETE', {
      currentPassword: withdrawForm.elements.currentPassword.value,
    });
    if (response === null) return;
    clearAccessToken();
    window.location.replace('/login?withdrawn=success');
  });
}

function configureSellerAction(profile) {
  const section = document.querySelector('[data-seller-action]');
  const form = section.querySelector('[data-seller-form]');
  const state = section.querySelector('[data-seller-state]');
  if (profile.role === 'ADMIN') {
    section.hidden = true;
    return;
  }
  if (profile.sellerStatus) {
    showSellerState(profile.sellerStatus);
    return;
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(form));
    if (Boolean(values.latitude) !== Boolean(values.longitude)) {
      showFieldErrors(form, {
        latitude: '위도와 경도를 함께 입력해 주세요.',
        longitude: '위도와 경도를 함께 입력해 주세요.',
      });
      return;
    }
    const result = await submitJsonForm(form, '/api/v1/account/seller-applications', 'POST', {
      businessName: values.businessName,
      businessNumber: values.businessNumber,
      latitude: optionalNumber(values.latitude),
      longitude: optionalNumber(values.longitude),
      deliveryRadiusKm: optionalNumber(values.deliveryRadiusKm),
    });
    if (!result) return;
    profile.sellerStatus = result.status;
    renderProfileSummary(profile);
    showSellerState(result.status);
  });

  function showSellerState(status) {
    const labels = {
      PENDING: ['심사 대기 중', '관리자가 사업자 정보를 확인하고 있습니다.'],
      APPROVED: ['판매자 승인 완료', '판매 기능을 사용할 수 있습니다.'],
      REJECTED: ['판매자 신청 거부', '현재 정책상 재신청은 관리자 문의가 필요합니다.'],
    };
    const [title, description] = labels[status] || ['심사 상태 확인 중', '판매자 심사 상태를 확인하고 있습니다.'];
    form.hidden = true;
    state.hidden = false;
    state.querySelector('[data-seller-state-title]').textContent = title;
    state.querySelector('[data-seller-state-description]').textContent = description;
    state.querySelector('[data-seller-products]').hidden = status !== 'APPROVED';
  }
}

async function submitJsonForm(form, url, method, body) {
  if (!validateRequiredFields(form)) return null;
  clearFieldErrors(form);
  const button = form.querySelector('button[type="submit"]');
  button.disabled = true;
  showFormMessage(form, '');
  try {
    const response = await authFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (response.status === 401) redirectToLogin();
    const payload = await readApiBody(response);
    if (!response.ok) {
      showFieldErrors(form, payload?.fieldErrors);
      showFormMessage(form, payload?.message || '요청을 처리하지 못했습니다.');
      return null;
    }
    return payload ?? true;
  } catch (error) {
    showFormMessage(form, error.message || '네트워크 연결을 확인해 주세요.');
    return null;
  } finally {
    button.disabled = false;
  }
}

function showFormMessage(form, text, success = false) {
  const message = form.querySelector('[data-form-message]');
  message.classList.toggle('is-success', success);
  message.textContent = text;
}

function optionalNumber(value) {
  return value === '' ? null : Number(value);
}

function profileLabel(field, value) {
  const labels = {
    role: { USER: '일반 회원', SELLER: '판매자', ADMIN: '관리자' },
    sellerStatus: { PENDING: '심사 대기', APPROVED: '승인', REJECTED: '거부' },
  };
  return labels[field]?.[value] || value || '신청 전';
}

function configureViewStates() {
  if (view.empty) {
    const empty = document.querySelector('[data-empty-state]');
    empty.querySelector('strong').textContent = view.empty[0];
    empty.querySelector('p').textContent = view.empty[1];
  }
  if (view.pending) {
    const pending = document.querySelector('[data-pending-state]');
    pending.querySelector('strong').textContent = view.pending[0];
    pending.querySelector('p').textContent = view.pending[1];
  }
}

function configureRecordControls() {
  const search = document.querySelector('[data-record-search]');
  const status = document.querySelector('[data-record-status]');
  const refresh = () => {
    visibleRecordCount = RECORD_PAGE_SIZE;
    renderRecordView();
  };
  search.addEventListener('input', refresh);
  status.addEventListener('change', refresh);
  document.querySelector('[data-reset-filters]').addEventListener('click', () => {
    search.value = '';
    status.value = '';
    refresh();
    search.focus();
  });
  document.querySelector('[data-load-more]').addEventListener('click', () => {
    visibleRecordCount += RECORD_PAGE_SIZE;
    renderRecordView();
  });
  document.querySelector('[data-retry-all]').addEventListener('click', () => {
    if (authorizedSources.length === 0) {
      window.location.reload();
      return;
    }
    loadRecords(authorizedSources);
  });
  document.querySelector('[data-retry-partial]').addEventListener('click', () => {
    loadRecords([...failuresBySource.values()].map(({source}) => source));
  });
}

function syncStatusOptions(records) {
  const select = document.querySelector('[data-record-status]');
  const selected = select.value;
  const statuses = new Map(records.map(({statusCode, status}) => [statusCode, status]));
  select.replaceChildren(new Option('전체 상태', ''));
  [...statuses.entries()]
    .sort((left, right) => left[1].localeCompare(right[1], 'ko-KR'))
    .forEach(([code, label]) => select.add(new Option(label, code)));
  if (statuses.has(selected)) select.value = selected;
}

function showPartialState() {
  const partial = document.querySelector('[data-partial-state]');
  if (failuresBySource.size === 0) {
    partial.hidden = true;
    return;
  }
  const labels = [...failuresBySource.values()].map(({source}) => source.label).filter(Boolean);
  partial.querySelector('[data-partial-message]').textContent = labels.length > 0
    ? `${labels.join(', ')} 내역을 불러오지 못해 나머지 항목만 표시합니다.`
    : '일부 내역을 불러오지 못해 조회 가능한 항목만 표시합니다.';
  partial.hidden = false;
}

function setRetryButtonsDisabled(disabled) {
  document.querySelectorAll('[data-retry-all], [data-retry-partial]').forEach((button) => {
    button.disabled = disabled;
  });
}

function recordFailureMessage() {
  const failures = [...failuresBySource.values()];
  if (failures.some(({response}) => response === null)) {
    return '네트워크 연결을 확인한 뒤 다시 시도해 주세요.';
  }
  if (failures.every(({response}) => response?.status === 403)) {
    return '이 내역을 조회할 권한이 없습니다. 계정 역할을 다시 확인해 주세요.';
  }
  return '내역 조회 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.';
}

function renderRecords(records) {
  const list = document.querySelector('[data-record-list]');
  list.replaceChildren();
  records.forEach((record) => {
    const article = document.createElement('article');
    article.className = 'record-card';
    article.dataset.recordKey = record.key;
    article.innerHTML = `
      <div class="record-card-content">
        <span class="record-source"></span>
        <h2></h2><p></p><small></small>
      </div>
      <div class="record-card-actions">
        <span class="status-chip"></span>
        <a class="record-link" hidden></a>
      </div>`;
    article.querySelector('.record-source').textContent = record.sourceLabel;
    article.querySelector('h2').textContent = record.title;
    const detail = article.querySelector('p');
    detail.textContent = record.detail;
    detail.hidden = !record.detail;
    const meta = article.querySelector('small');
    meta.textContent = record.meta;
    meta.hidden = !record.meta;
    const status = article.querySelector('.status-chip');
    status.textContent = record.status;
    status.dataset.status = record.statusCode;
    if (record.href) {
      article.classList.add('is-clickable');
      article.tabIndex = 0;
      article.setAttribute('role', 'link');
      const link = article.querySelector('.record-link');
      link.href = record.href;
      link.textContent = record.actionLabel || '상세 보기';
      link.hidden = false;
      article.addEventListener('click', (event) => {
        if (event.target.closest('a, button')) return;
        window.location.assign(record.href);
      });
      article.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') window.location.assign(record.href);
      });
    }
    list.append(article);
  });
}

function showState(state) {
  hideDataStates();
  document.querySelector('[data-partial-state]').hidden = true;
  document.querySelector('[data-filter-empty]').hidden = true;
  document.querySelector('[data-load-more]').hidden = true;
  document.querySelector(`[data-${state}-state]`).hidden = false;
}

function showFailure(message) {
  showState('error');
  document.querySelector('[data-error-state] p').textContent = message;
}

function hideDataStates() {
  document.querySelectorAll('[data-loading-state], [data-empty-state], [data-pending-state], [data-error-state]')
    .forEach((element) => { element.hidden = true; });
}

function redirectToLogin() {
  const redirect = encodeURIComponent(location.pathname + location.search);
  window.location.replace(`/login?redirect=${redirect}`);
  throw new Error('Redirecting to login');
}
