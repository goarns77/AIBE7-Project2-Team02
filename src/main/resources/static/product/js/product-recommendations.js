import { authFetch, readApiBody } from '/account/js/auth-client.js';

/**
 * 내 판매 조건(productId)에 대한 추천 주문 요청을 조회해 카드로 렌더링한다.
 */
const productIdInput = document.getElementById('productId');
const fetchBtn = document.getElementById('fetchBtn');
const result = document.getElementById('result');
const errorBox = document.getElementById('errorBox');
const rawJson = document.getElementById('rawJson');
const toggleRaw = document.getElementById('toggleRaw');

const sample = [
    {
        orderRequest: {
            id: 101,
            title: '회식용 도시락 30인분',
            description: '금요일 저녁 배달 요청',
            eventDateTime: '2026-08-30T18:30:00',
            quantity: 30,
            budgetType: 'TOTAL',
            budget: 250000,
            category: '도시락',
            deliveryAddress: '서울시 강남구 ...'
        },
        totalScore: 87.4,
        scoreItems: [
            { reasonTag: 'DISTANCE', label: '거리 인접도', score: 91.0, weight: 35.0, contribution: 31.9, reason: '배송 반경 대비 근접도 91%' },
            { reasonTag: 'BUDGET', label: '예산 적합도', score: 92.0, weight: 25.0, contribution: 23.0, reason: '예산 적합도 92%' },
            { reasonTag: 'CATEGORY', label: '카테고리 일치도', score: 100.0, weight: 20.0, contribution: 20.0, reason: '카테고리 완전 일치' },
            { reasonTag: 'RATING', label: '판매자 평점', score: 79.0, weight: 10.0, contribution: 7.9, reason: '평점 4.0/5.0' },
            { reasonTag: 'TEXT_SIMILARITY', label: '텍스트 유사도', score: 46.0, weight: 10.0, contribution: 4.6, reason: '설명 내용 유사도 46%' }
        ]
    }
];

function formatMoney(value) {
    return value != null ? `${Number(value).toLocaleString()}원` : '-';
}

function formatDateTime(value) {
    return value ? new Date(value).toLocaleString() : '-';
}

function renderCard(item) {
    const order = item.orderRequest ?? {};
    const scoreItems = item.scoreItems ?? [];

    const rows = scoreItems.map(scoreItem => `
        <div class="product-rec-score-row">
            <div>
                <div class="product-rec-score-label">${scoreItem.label}</div>
                <div class="product-rec-score-reason">${scoreItem.reason ?? ''}</div>
            </div>
            <div class="product-rec-score-bar">
                <div class="product-rec-score-bar-fill" style="width:${Math.max(0, Math.min(100, scoreItem.score))}%"></div>
            </div>
            <div class="product-rec-score-value">${scoreItem.score.toFixed(0)}%</div>
        </div>
    `).join('');

    return `
        <article class="product-rec-card">
            <div class="product-rec-card-head">
                <div>
                    <p class="product-rec-title">${order.title ?? '(제목 없음)'}</p>
                    <p class="product-rec-sub">
                        ${order.category ?? '-'} · ${order.quantity ?? '-'}인분 · ${formatMoney(order.budget)}(${order.budgetType ?? '-'})<br>
                        📍 ${order.deliveryAddress ?? '-'}<br>
                        🗓 ${formatDateTime(order.eventDateTime)}
                    </p>
                </div>
                <div class="product-rec-score">
                    <div class="num">${item.totalScore.toFixed(1)}</div>
                    <div class="label">종합 매칭 점수</div>
                </div>
            </div>
            <div class="product-rec-score-list">${rows}</div>
        </article>
    `;
}

function render(data) {
    rawJson.querySelector('code').textContent = JSON.stringify(data, null, 2);

    const items = Array.isArray(data) ? data : [data];
    if (!items.length) {
        result.innerHTML = `<div class="product-rec-empty">매칭되는 주문 요청이 없습니다.</div>`;
        return;
    }

    result.innerHTML = items.map(renderCard).join('');
}

async function fetchRecommendations() {
    const productId = productIdInput.value.trim();
    if (!productId) {
        errorBox.textContent = 'productId를 입력하세요.';
        errorBox.style.display = 'block';
        return;
    }

    errorBox.style.display = 'none';
    result.innerHTML = `<div class="product-rec-empty">불러오는 중...</div>`;

    try {
        const response = await authFetch(`/api/products/${productId}/order-requests/recommendations`);
        if (!response.ok) {
            throw new Error('추천 데이터를 불러오지 못했습니다.');
        }

        const data = await readApiBody(response);
        render(data);
    } catch (error) {
        errorBox.textContent = error.message;
        errorBox.style.display = 'block';
        render(sample);
    }
}

toggleRaw.addEventListener('click', () => {
    const showing = rawJson.style.display !== 'none';
    rawJson.style.display = showing ? 'none' : 'block';
    toggleRaw.textContent = showing ? '원본 JSON 보기' : '원본 JSON 숨기기';
});

fetchBtn.addEventListener('click', fetchRecommendations);
render(sample);
