import {authFetch, readApiBody, readCurrentUserId} from '/account/js/auth-client.js';

/**
 * 주문 등록/수정 폼을 JWT 인증 API와 연결한다.
 */
const form = document.getElementById('orderRequestForm');

if (form) {
    const currentUserId = readCurrentUserId();
    const mode = form.dataset.mode;
    const requestId = form.dataset.requestId;

    // 주문 등록과 수정은 로그인 사용자만 이용한다.
    if (currentUserId === null) {
        const redirect = encodeURIComponent(window.location.pathname);
        window.location.href = `/login?redirect=${redirect}`;
    } else if (mode === 'edit') {
        const buyerId = Number(form.dataset.buyerId);

        if (currentUserId !== buyerId) {
            alert('본인이 등록한 주문만 수정할 수 있습니다.');
            window.location.href = `/requests/${requestId}`;
        } else {
            form.hidden = false;
        }
    } else {
        form.hidden = false;
    }

    const categorySelect = document.getElementById('category');
    const customCategoryArea = document.getElementById('customCategoryArea');
    const customCategoryInput = document.getElementById('customCategory');

    const standardCategories = [
        '한식',
        '중식',
        '일식',
        '양식',
        '도시락/간편식',
        '디저트/다과',
        '카페/음료',
        '비건'
    ];

    // 수정 화면에서 기존 값이 직접 입력 카테고리라면 "기타" 입력란을 연다.
    const originalCategory = categorySelect.dataset.originalCategory;

    if (originalCategory && !standardCategories.includes(originalCategory)) {
        categorySelect.value = '기타';
        customCategoryInput.value = originalCategory;
        customCategoryInput.required = true;
        customCategoryArea.hidden = false;
    }

    categorySelect.addEventListener('change', () => {
        const isOther = categorySelect.value === '기타';

        customCategoryArea.hidden = !isOther;
        customCategoryInput.required = isOther;

        if (!isOther) {
            customCategoryInput.value = '';
        }
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        if (!form.reportValidity()) {
            return;
        }

        const category = categorySelect.value === '기타'
            ? customCategoryInput.value.trim()
            : categorySelect.value;

        if (!category) {
            alert('음식 카테고리를 입력해주세요.');
            return;
        }

        const payload = {
            title: form.elements.namedItem('title').value.trim(),
            description: form.elements.namedItem('description').value.trim(),
            eventDateTime: form.elements.namedItem('eventDateTime').value,
            quantity: Number(form.elements.namedItem('quantity').value),
            budgetType: form.elements.namedItem('budgetType').value,
            budget: Number(form.elements.namedItem('budget').value),
            category,
            deliveryAddress: form.elements.namedItem('deliveryAddress').value.trim(),
            latitude: Number(form.elements.namedItem('latitude').value),
            longitude: Number(form.elements.namedItem('longitude').value)
        };

        const url = mode === 'edit'
            ? `/api/v1/requests/${requestId}`
            : '/api/v1/requests';

        const method = mode === 'edit' ? 'PATCH' : 'POST';

        const response = await authFetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.status === 401) {
            const redirect = encodeURIComponent(window.location.pathname);
            window.location.href = `/login?redirect=${redirect}`;
            return;
        }

        if (response.status === 403) {
            alert('본인이 등록한 주문만 수정할 수 있습니다.');
            return;
        }

        if (!response.ok) {
            const body = await readApiBody(response);
            alert(body?.message ?? '주문 처리 중 문제가 발생했습니다.');
            return;
        }

        const body = await readApiBody(response);

        if (mode === 'edit') {
            window.location.href = `/requests/${requestId}`;
        } else {
            window.location.href = `/requests/${body.id}`;
        }
    });
}