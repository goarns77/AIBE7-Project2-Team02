import {authFetch, readApiBody, readCurrentUserId} from '/account/js/auth-client.js';

/**
 * 판매 조건 등록 폼을 JWT 인증 API와 연결한다.
 */
const form = document.getElementById('productForm');
const messageBox = document.getElementById('messageBox');
const resultBox = document.getElementById('result');
const imageFileInput = document.getElementById('imageFile');
const imagePreview = document.getElementById('imagePreview');
const storeAddressInput =
    document.getElementById('storeAddress');

const storeAddressSearchButton =
    document.getElementById('storeAddressSearchButton');

if (readCurrentUserId() === null) {
    const redirect = encodeURIComponent(window.location.pathname);
    window.location.href = `/login?redirect=${redirect}`;
}

function showMessage(text, isSuccess) {
    messageBox.textContent = text;
    messageBox.className = 'product-form-message ' + (isSuccess ? 'is-success' : 'is-error');
}

function parseUnavailableDates(value) {
    return value
        .split(/[\n,]/)
        .map(date => date.trim())
        .filter(Boolean);
}

function updatePreview(file) {
    if (!file) {
        imagePreview.style.display = 'none';
        imagePreview.src = '';
        return;
    }

    imagePreview.src = URL.createObjectURL(file);
    imagePreview.style.display = 'block';
}

/**
 * Kakao 우편번호 검색창을 열고 선택한 주소를 가게 주소에 입력한다.
 */
function openStoreAddressSearch() {
    if (
        typeof kakao === 'undefined'
        || !kakao.Postcode
    ) {
        alert(
            '주소 검색 서비스를 불러오지 못했습니다.'
        );
        return;
    }

    new kakao.Postcode({
        oncomplete(data) {
            const selectedAddress =
                data.roadAddress
                || data.jibunAddress
                || data.address;

            storeAddressInput.value =
                selectedAddress;

            storeAddressInput.focus();
        }
    }).open();
}

storeAddressSearchButton.addEventListener(
    'click',
    openStoreAddressSearch
);

imageFileInput.addEventListener('change', () => {
    updatePreview(imageFileInput.files?.[0]);
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const requestData = {
        productName: document.getElementById('productName').value,
        minHeadcount: Number(document.getElementById('minHeadcount').value),
        maxHeadcount: Number(document.getElementById('maxHeadcount').value),
        servingPrice: Number(document.getElementById('servingPrice').value),
        deliveryRadiusKm: Number(document.getElementById('deliveryRadiusKm').value),
        storeAddress: document.getElementById('storeAddress').value,
        category: document.getElementById('category').value,
        description: document.getElementById('description').value,
        dayOfWeek: document.getElementById('dayOfWeek').value || null,
        unavailableDates: parseUnavailableDates(document.getElementById('unavailableDates').value)
    };

    const formData = new FormData();
    formData.append('product', new Blob([JSON.stringify(requestData)], {type: 'application/json'}));

    const imageFile = imageFileInput.files?.[0];
    if (imageFile) {
        formData.append('imageFile', imageFile);
    }

    try {
        resultBox.textContent = '전송 중...';

        const response = await authFetch('/api/v1/products', {
            method: 'POST',
            body: formData
        });

        if (response.status === 401) {
            const redirect = encodeURIComponent(window.location.pathname);
            window.location.href = `/login?redirect=${redirect}`;
            return;
        }

        const data = await readApiBody(response);

        if (!response.ok) {
            throw new Error(data?.message ?? '판매 조건 등록에 실패했습니다.');
        }

        resultBox.textContent = JSON.stringify(data, null, 2);
        showMessage('판매 조건이 성공적으로 등록되었습니다.', true);
        form.reset();
        updatePreview(null);
    } catch (error) {
        resultBox.textContent = error.message;
        showMessage(error.message, false);
    }
});
