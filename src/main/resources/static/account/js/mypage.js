import {
  authFetch,
  clearAccessToken,
  clearFieldErrors,
  readApiBody,
  readAccessToken,
  showFieldErrors,
  validateRequiredFields,
} from './auth-client.js';
import { adaptMypagePayload, extractPage, mypageViews } from './mypage-api-adapters.js';

const currentView = location.pathname.split('/').filter(Boolean).at(-1);
const viewKey = currentView === 'mypage' ? 'profile' : currentView;
const view = mypageViews[viewKey] || mypageViews.profile;

if (!readAccessToken()) redirectToLogin();

document.querySelector('[data-view-title]').textContent = view.title;
document.querySelector('[data-view-kicker]').textContent = view.kicker;
document.querySelector(`[data-view="${viewKey}"]`)?.setAttribute('aria-current', 'page');

const profileResponse = await authFetch('/api/v1/account/me');
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
} else {
  const authorizedSources = view.sources.filter((source) => !source.sellerOnly || profile.role === 'SELLER');
  await loadRecords(authorizedSources);
}

async function loadRecords(sources) {
  const responses = await Promise.all(sources.map(({ endpoint }) => authFetch(endpoint)));
  if (responses.some((response) => response.status === 401)) redirectToLogin();
  if (responses.some((response) => [404, 405].includes(response.status))) {
    showState('pending');
    return;
  }
  if (responses.some((response) => !response.ok)) {
    showFailure('목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    return;
  }

  const payloads = await Promise.all(responses.map(readApiBody));
  const records = payloads.flatMap((payload, index) =>
    adaptMypagePayload(sources[index].key, payload));
  const pages = payloads.map(extractPage);
  if (records.length === 0) {
    showState('empty');
    return;
  }
  renderRecords(records, pages);
}

function renderProfileSummary(profile) {
  document.querySelector('[data-profile-name]').textContent = `${profile.name}님의 거래`;
  document.querySelector('[data-profile-email]').textContent = profile.email;
  document.querySelector('[data-account-status]').textContent = profile.role === 'ADMIN' ? '관리자' : '활성 계정';
  document.querySelectorAll('[data-profile-field]').forEach((element) => {
    const value = profile[element.dataset.profileField];
    element.textContent = profileLabel(element.dataset.profileField, value);
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
    role: { USER: '일반 회원', ADMIN: '관리자' },
    sellerStatus: { PENDING: '심사 대기', APPROVED: '승인', REJECTED: '거부' },
  };
  return labels[field]?.[value] || value || '신청 전';
}

function renderRecords(records, pages) {
  const list = document.querySelector('[data-record-list]');
  document.querySelector('[data-loading-state]').hidden = true;
  list.replaceChildren();
  records.forEach((record) => {
    const article = document.createElement('article');
    article.className = 'record-card';
    article.dataset.recordKey = record.key;
    article.innerHTML = `<div><h2></h2><p></p><small></small></div><span class="status-chip"></span>`;
    article.querySelector('h2').textContent = record.title;
    article.querySelector('p').textContent = record.detail;
    article.querySelector('small').textContent = record.meta;
    article.querySelector('.status-chip').textContent = record.status;
    list.append(article);
  });

  // Paging controls can consume this metadata once each domain fixes its paging contract.
  list.dataset.pages = JSON.stringify(pages);
}

function showState(state) {
  document.querySelector('[data-loading-state]').hidden = true;
  document.querySelector(`[data-${state}-state]`).hidden = false;
}

function showFailure(message) {
  document.querySelector('[data-loading-state] p').textContent = message;
}

function redirectToLogin() {
  const redirect = encodeURIComponent(location.pathname + location.search);
  window.location.replace(`/login?redirect=${redirect}`);
  throw new Error('Redirecting to login');
}
