import {authFetch, readApiBody, readCurrentUserId} from '/account/js/auth-client.js';

/**
 * 주문 상세 화면에서 소유자 전용 액션과 주문 취소를 처리한다.
 */
const ownerActions = document.getElementById('orderOwnerActions');
const cancelButton = document.getElementById('cancelOrderButton');

const currentUserId = readCurrentUserId();

if (ownerActions) {
    const buyerId = Number(ownerActions.dataset.buyerId);

    if (currentUserId !== null && currentUserId === buyerId) {
        ownerActions.hidden = false;
    }
}

if (cancelButton) {
    cancelButton.addEventListener('click', async () => {
        const requestId = cancelButton.dataset.requestId;

        if (!confirm('이 주문을 취소하시겠습니까?')) {
            return;
        }

        const response = await authFetch(
            `/api/v1/requests/${requestId}/cancel`,
            {
                method: 'PATCH'
            }
        );

        if (response.status === 401) {
            const redirect = encodeURIComponent(window.location.pathname);
            window.location.href = `/login?redirect=${redirect}`;
            return;
        }

        if (response.status === 403) {
            alert('본인이 등록한 주문만 취소할 수 있습니다.');
            window.location.reload();
            return;
        }

        if (!response.ok) {
            const body = await readApiBody(response);

            alert(
                body?.message ??
                '주문 취소 중 문제가 발생했습니다.'
            );

            return;
        }

        window.location.reload();
    });
}