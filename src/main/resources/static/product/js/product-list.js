import { authFetch, readApiBody, readCurrentUserId, readCurrentUserRole } from '/account/js/auth-client.js';

/**
 * 판매 조건 목록을 검색·조회하고, 게시판형 카드로 렌더링한다.
 * "새 판매 조건 등록" 버튼은 승인된 판매자에게만, "숨김 처리" 버튼은 본인 소유(또는 관리자)에게만 보인다.
 */
const productList = document.getElementById('productList');
const searchForm = document.getElementById('searchForm');
const resetBtn = document.getElementById('resetBtn');
const quantityInput = document.getElementById('quantity');
const categoryInput = document.getElementById('category');
const servingPriceInput = document.getElementById('servingPrice');
const newProductButton = document.getElementById('newProductButton');

const currentUserId = readCurrentUserId();
const isAdmin = readCurrentUserRole() === 'ADMIN';
let ownedProductIds = new Set();

const dayOfWeekMap = {
    MONDAY: '월요일',
    TUESDAY: '화요일',
    WEDNESDAY: '수요일',
    THURSDAY: '목요일',
    FRIDAY: '금요일',
    SATURDAY: '토요일',
    SUNDAY: '일요일'
};

async function showRegisterButtonIfApprovedSeller() {
    if (currentUserId === null) {
        return;
    }

    try {
        const response = await authFetch('/api/v1/account/me');
        if (!response.ok) {
            return;
        }

        const account = await readApiBody(response);
        if (account?.sellerStatus === 'APPROVED') {
            newProductButton.style.display = '';
        }
    } catch {
        // 계정 정보를 못 가져와도 목록 조회 자체는 계속 진행한다.
    }
}

async function loadOwnedProductIds() {
    if (currentUserId === null || isAdmin) {
        return;
    }

    try {
        const response = await authFetch('/api/v1/products/mine');
        if (!response.ok) {
            return;
        }

        const products = await readApiBody(response);
        ownedProductIds = new Set(
            (Array.isArray(products) ? products : []).map(product => Number(product.id))
        );
    } catch {
        // 소유 상품 조회 실패는 공개 목록 조회를 막지 않는다.
    }
}

function formatMoney(value) {
    return value != null ? `${Number(value).toLocaleString()}원` : '-';
}

function formatUnavailableDates(unavailableDates) {
    if (!Array.isArray(unavailableDates) || unavailableDates.length === 0) {
        return '-';
    }

    return unavailableDates.join(', ');
}

function buildSearchParams() {
    const params = new URLSearchParams();
    const quantity = quantityInput.value.trim();
    const category = categoryInput.value.trim();
    const servingPrice = servingPriceInput.value.trim();

    if (quantity) params.set('quantity', quantity);
    if (category) params.set('category', category);
    if (servingPrice) params.set('servingPrice', servingPrice);

    return params;
}

function renderEmpty(message) {
    productList.innerHTML = `<p>${message}</p>`;
}

function renderItems(items) {
    if (!items.length) {
        renderEmpty('조건에 맞는 판매 조건이 없습니다.');
        return;
    }

    productList.innerHTML = items.map(product => `
        <article class="product-item">
            ${product.imageUrl
                ? `<div class="product-item-thumb"><img src="${encodeURI(product.imageUrl)}" alt="${product.productName ?? '상품 이미지'}"></div>`
                : ''
            }
            <div class="product-item-main">
                <div class="product-item-title">
                    <span class="product-rating">⭐ ${product.ratingAvg != null ? product.ratingAvg.toFixed(1) : '평점 없음'}</span>
                    <h2>
                        <a href="/product/detail?id=${product.id}">
                            ${product.productName ?? '(상품명 없음)'}
                        </a>
                    </h2>
                </div>

                <div class="product-item-summary">
                    <div class="product-meta-item">
                        <span class="product-meta-label">카테고리</span>
                        <span class="product-meta-value">${product.category ?? '-'}</span>
                    </div>
                    <div class="product-meta-item">
                        <span class="product-meta-label">수주 가능 수량</span>
                        <span class="product-meta-value">${product.minHeadcount}~${product.maxHeadcount}인분</span>
                    </div>
                    <div class="product-meta-item">
                        <span class="product-meta-label">1인분 가격</span>
                        <span class="product-meta-value">${formatMoney(product.servingPrice)}</span>
                    </div>
                    <div class="product-meta-item">
                        <span class="product-meta-label">배송 반경</span>
                        <span class="product-meta-value">${product.deliveryRadiusKm}km</span>
                    </div>
                    <div class="product-meta-item">
                        <span class="product-meta-label">정기 휴무</span>
                        <span class="product-meta-value">${dayOfWeekMap[product.dayOfWeek] ?? '없음'}</span>
                    </div>

                    <div class="product-meta-item product-meta-full">
                        <span class="product-meta-label">가게 주소</span>
                        <span class="product-meta-value">${product.storeAddress ?? '-'}</span>
                    </div>

                    <div class="product-meta-item product-meta-full">
                        <span class="product-meta-label">설명</span>
                        <span class="product-meta-value">${product.description ?? '-'}</span>
                    </div>

                    <div class="product-meta-item product-meta-full">
                        <span class="product-meta-label">특정 불가 날짜</span>
                        <span class="product-meta-value">${formatUnavailableDates(product.unavailableDates)}</span>
                    </div>
                </div>
            </div>

            <div class="product-item-actions">
                <a href="/product/detail?id=${product.id}">상세 보기</a>
                ${(isAdmin || ownedProductIds.has(Number(product.id)))
                    ? `<button type="button" class="product-item-danger" data-product-id="${product.id}">숨김 처리</button>`
                    : ''
                }
            </div>
        </article>
    `).join('');
}

async function fetchProducts() {
    try {
        renderEmpty('데이터를 불러오는 중입니다...');

        const params = buildSearchParams();
        const query = params.toString();
        const response = await authFetch(`/api/v1/products/search${query ? `?${query}` : ''}`);

        if (!response.ok) {
            throw new Error('판매 조건 목록을 불러오지 못했습니다.');
        }

        const data = await readApiBody(response);
        renderItems(data ?? []);
    } catch (error) {
        renderEmpty(error.message);
    }
}

searchForm.addEventListener('submit', (event) => {
    event.preventDefault();
    fetchProducts();
});

productList.addEventListener('click', async (event) => {
    const deleteButton = event.target.closest('[data-product-id]');
    if (!deleteButton) {
        return;
    }

    const productId = deleteButton.dataset.productId;
    const confirmed = window.confirm(`상품 ID ${productId}를 숨김 처리하시겠습니까?`);
    if (!confirmed) {
        return;
    }

    deleteButton.disabled = true;

    try {
        const response = await authFetch(`/api/v1/products/${productId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const body = await readApiBody(response).catch(() => null);
            throw new Error(body?.message ?? '판매 조건을 숨김 처리하지 못했습니다.');
        }

        await fetchProducts();
    } catch (error) {
        alert(error.message);
        deleteButton.disabled = false;
    }
});

resetBtn.addEventListener('click', () => {
    searchForm.reset();
    fetchProducts();
});

async function initialize() {
    await Promise.all([
        showRegisterButtonIfApprovedSeller(),
        loadOwnedProductIds()
    ]);
    await fetchProducts();
}

initialize();
