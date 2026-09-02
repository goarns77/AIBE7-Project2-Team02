import {authFetch, readApiBody, readCurrentUserId, readCurrentUserRole} from '/account/js/auth-client.js';

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
const productCount =
    document.getElementById('productCount');
const productPagination =
    document.getElementById('productPagination');

const PAGE_SIZE = 9;

let currentPage = 1;
let currentProducts = [];

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
    currentProducts = items;

    const totalPages =
        Math.ceil(items.length / PAGE_SIZE);

    if (currentPage > totalPages) {
        currentPage = totalPages || 1;
    }

    productCount.textContent =
        `총 ${items.length}개의 상품`;

    if (!items.length) {
        renderEmpty(
            '조건에 맞는 상품이 없습니다.'
        );

        productPagination.innerHTML = '';
        return;
    }

    const start =
        (currentPage - 1) * PAGE_SIZE;

    const pageItems =
        items.slice(start, start + PAGE_SIZE);

    productList.innerHTML =
        pageItems.map(product => `
            <article class="product-item">
                <a
                    class="product-item-image"
                    href="/product/detail?id=${product.id}"
                >
                    ${product.imageUrl
            ? `
                            <img
                                src="${encodeURI(product.imageUrl)}"
                                alt="${product.productName ?? '상품 이미지'}"
                            >
                        `
            : `
                            <div class="product-item-image-placeholder">
                                <span>이미지 없음</span>
                            </div>
                        `
        }
                </a>

                <div class="product-item-body">
                    <div class="product-item-category">
                        ${product.category ?? '카테고리 없음'}
                    </div>

                    <div class="product-item-title-row">
                        <h2>
                            <a href="/product/detail?id=${product.id}">
                                ${product.productName ?? '(상품명 없음)'}
                            </a>
                        </h2>

                        ${
            product.ratingAvg != null
                ? `
                                    <span class="product-rating">
                                        ★ ${product.ratingAvg.toFixed(1)}
                                    </span>
                                `
                : ''
        }
                    </div>

                    <div class="product-item-bottom">
                        <div class="product-item-price">
                            <strong>
                                ${formatMoney(product.servingPrice)}
                            </strong>
                            <span>/인분</span>
                        </div>

                        <span class="product-min-order">
                            최소 ${product.minHeadcount ?? '-'}인분
                        </span>
                    </div>

                    ${(isAdmin
            || ownedProductIds.has(
                Number(product.id)
            ))
            ? `
                            <button
                                type="button"
                                class="product-item-danger"
                                data-product-id="${product.id}"
                            >
                                숨김 처리
                            </button>
                        `
            : ''
        }
                </div>
            </article>
        `).join('');

    renderPagination(totalPages);
}

/**
 * 상품 목록의 페이지 이동 버튼을 렌더링한다.
 */
function renderPagination(totalPages) {
    if (totalPages <= 1) {
        productPagination.innerHTML = '';
        return;
    }

    let html = `
        <button
            type="button"
            data-page="${currentPage - 1}"
            ${currentPage === 1 ? 'disabled' : ''}
        >
            ‹
        </button>
    `;

    for (
        let page = 1;
        page <= totalPages;
        page++
    ) {
        html += `
            <button
                type="button"
                class="${page === currentPage ? 'is-active' : ''}"
                data-page="${page}"
            >
                ${page}
            </button>
        `;
    }

    html += `
        <button
            type="button"
            data-page="${currentPage + 1}"
            ${currentPage === totalPages ? 'disabled' : ''}
        >
            ›
        </button>
    `;

    productPagination.innerHTML = html;
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

        currentPage = 1;
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

productPagination.addEventListener(
    'click',
    (event) => {
        const button =
            event.target.closest('[data-page]');

        if (!button || button.disabled) {
            return;
        }

        currentPage =
            Number(button.dataset.page);

        renderItems(currentProducts);

        document.querySelector(
            '.product-list-section'
        )?.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
);

async function initialize() {
    await Promise.all([
        showRegisterButtonIfApprovedSeller(),
        loadOwnedProductIds()
    ]);
    await fetchProducts();
}

initialize();
