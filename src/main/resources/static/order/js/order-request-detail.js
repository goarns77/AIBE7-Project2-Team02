import {authFetch, readApiBody, readCurrentUserId} from '/account/js/auth-client.js';

const page = document.getElementById('orderDetailPage');
const requestId = page?.dataset.requestId;
const currentUserId = readCurrentUserId();

if (currentUserId === null) redirectToLogin();

const response = await authFetch(`/api/v1/requests/${requestId}`);
if (response.status === 401) redirectToLogin();
if (!response.ok) {
    const body = await readApiBody(response);
    showError(body?.message || '주문 정보를 불러올 수 없습니다.');
} else {
    const order = await response.json();
    renderOrder(order);
    await configureActions(order);
}

function renderOrder(order) {
    document.getElementById('orderLoading').hidden = true;
    document.getElementById('orderContent').hidden = false;
    document.getElementById('orderStatus').textContent = statusLabel(order.status);
    setField('title', order.title || '제목 없음');
    setField('eventDateTime', formatDateTime(order.eventDateTime));
    setField('quantity', order.quantity ?? '-');
    setField('budget', `${order.budgetType === 'PER_PERSON' ? '1인당' : '총'} ${formatNumber(order.budget)}원`);
    setField('category', order.category || '-');
    setField('deliveryAddress', order.deliveryAddress || '-');
    setField('description', order.description || '등록된 상세 요청사항이 없습니다.');
}

async function configureActions(order) {
    if (order.status !== 'MATCHING') return;
    if (Number(order.buyerId) === currentUserId) {
        document.getElementById('orderOwnerActions').hidden = false;
        document.getElementById('matchingLink').href = `/requests/${requestId}/matches`;
        document.getElementById('editLink').href = `/requests/${requestId}/edit`;
        const cancelButton = document.getElementById('cancelOrderButton');
        cancelButton.addEventListener('click', cancelOrder);
        return;
    }
    const eligibility = await authFetch('/api/v1/proposals/eligibility');
    if (eligibility.ok) {
        document.getElementById('orderSellerActions').hidden = false;
        document.getElementById('proposalLink').href = `/requests/${requestId}/proposals/new`;
    }
}

async function cancelOrder() {
    if (!confirm('이 주문을 취소하시겠습니까?')) return;
    const response = await authFetch(`/api/v1/requests/${requestId}/cancel`, {method: 'PATCH'});
    if (response.status === 401) redirectToLogin();
    if (!response.ok) {
        const body = await readApiBody(response);
        alert(body?.message || '주문 취소 중 문제가 발생했습니다.');
        return;
    }
    window.location.reload();
}

function setField(field, value) {
    document.querySelector(`[data-order-field="${field}"]`).textContent = value;
}

function showError(message) {
    document.getElementById('orderLoading').hidden = true;
    const error = document.getElementById('orderError');
    error.hidden = false;
    error.querySelector('p').textContent = message;
}

function redirectToLogin() {
    const redirect = encodeURIComponent(location.pathname + location.search);
    location.replace(`/login?redirect=${redirect}`);
    throw new Error('Redirecting to login');
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString('ko-KR');
}

function formatDateTime(value) {
    if (!value) return '-';
    return new Intl.DateTimeFormat('ko-KR', {dateStyle: 'medium', timeStyle: 'short'}).format(new Date(value));
}

function statusLabel(status) {
    return {MATCHING: '매칭 중', IN_TALK: '협의 중', CONFIRMED: '확정', CANCELLED: '취소', CLOSED: '종료'}[status] || status;
}
