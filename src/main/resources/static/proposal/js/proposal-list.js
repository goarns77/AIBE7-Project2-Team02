import {authFetch, readApiBody, readCurrentUserId} from '/account/js/auth-client.js';

/**
 * 받은 제안과 보낸 제안 목록을 탭 형태로 조회하고 표시한다.
 */

const proposalList =
    document.getElementById('proposalList');

const tabButtons =
    document.querySelectorAll('[data-proposal-tab]');

const currentUserId =
    readCurrentUserId();

let currentTab = 'received';

/**
 * Proposal 상태를 사용자에게 보여줄 한글 문자열로 변환한다.
 */
function proposalStatusLabel(status) {
    const labels = {
        SENT: '제안 전송',
        IN_TALK: '협의 중',
        ACCEPTED: '수락',
        REJECTED: '거절',
        WITHDRAWN: '철회'
    };

    return labels[status] ?? status;
}

/**
 * 금액을 천 단위 구분 형태로 표시한다.
 */
function formatMoney(value) {
    if (value == null) {
        return '-';
    }

    return Number(value).toLocaleString() + '원';
}

/**
 * 제안 생성 시간을 화면용 문자열로 변환한다.
 */
function formatDateTime(value) {
    if (!value) {
        return '-';
    }

    return new Date(value).toLocaleString('ko-KR');
}

/**
 * 목록 데이터를 카드 형태로 렌더링한다.
 */
function renderProposals(proposals) {
    if (!Array.isArray(proposals) || proposals.length === 0) {
        proposalList.innerHTML = `
            <div class="proposal-list-empty">
                ${
            currentTab === 'received'
                ? '받은 제안이 없습니다.'
                : '보낸 제안이 없습니다.'
        }
            </div>
        `;

        return;
    }

    proposalList.innerHTML = '';

    proposals.forEach(proposal => {
        const card =
            document.createElement('article');

        card.className =
            'proposal-list-item';

        card.innerHTML = `
            <div class="proposal-list-item-main">

                <div class="proposal-list-item-header">

                    <div>
                        <span class="proposal-request-number">
                            주문 #${proposal.requestId}
                        </span>

                        <h2>
                            ${escapeHtml(proposal.itemName)}
                        </h2>
                    </div>

                    <span class="proposal-status-badge">
                        ${proposalStatusLabel(proposal.status)}
                    </span>

                </div>

                <div class="proposal-list-meta">

                    <div>
                        <span class="proposal-list-meta-label">
                            수량
                        </span>

                        <strong>
                            ${proposal.quantity}명
                        </strong>
                    </div>

                    <div>
                        <span class="proposal-list-meta-label">
                            1인 단가
                        </span>

                        <strong>
                            ${formatMoney(proposal.unitPrice)}
                        </strong>
                    </div>

                    <div>
                        <span class="proposal-list-meta-label">
                            최종 제안 금액
                        </span>

                        <strong>
                            ${formatMoney(proposal.totalAmount)}
                        </strong>
                    </div>

                    <div>
                        <span class="proposal-list-meta-label">
                            준비 기간
                        </span>

                        <strong>
                            ${proposal.preparationDays}일
                        </strong>
                    </div>

                </div>

                ${
            proposal.description
                ? `
                            <p class="proposal-list-description">
                                ${escapeHtml(proposal.description)}
                            </p>
                        `
                : ''
        }

                <span class="proposal-list-date">
                    ${formatDateTime(proposal.createdAt)}
                </span>

            </div>

            <div class="proposal-list-item-actions">

                <a href="/requests/${proposal.requestId}"
                   class="proposal-list-order-link">
                    주문 보기
                </a>

            </div>

        `;

        proposalList.appendChild(card);
    });
}

/**
 * HTML 문자열 삽입 시 사용자 입력값을 안전하게 표시한다.
 */
function escapeHtml(value) {
    const element =
        document.createElement('div');

    element.textContent =
        value ?? '';

    return element.innerHTML;
}

/**
 * 선택한 탭의 Proposal 목록을 서버에서 조회한다.
 */
async function loadProposals(tab) {
    currentTab = tab;

    proposalList.innerHTML = `
        <div class="proposal-list-empty">
            제안 목록을 불러오는 중입니다.
        </div>
    `;

    const url =
        tab === 'received'
            ? '/api/v1/proposals/received'
            : '/api/v1/proposals/sent';

    const response =
        await authFetch(url);

    if (response.status === 401) {
        const redirect =
            encodeURIComponent(window.location.pathname);

        window.location.href =
            `/login?redirect=${redirect}`;

        return;
    }

    const body =
        await readApiBody(response);

    if (!response.ok) {
        proposalList.innerHTML = `
            <div class="proposal-list-empty">
                ${escapeHtml(
            body?.message ??
            '제안 목록을 불러오지 못했습니다.'
        )}
            </div>
        `;

        return;
    }

    renderProposals(body);
}

/**
 * 탭 활성 상태를 변경한다.
 */
function selectTab(tab) {
    tabButtons.forEach(button => {
        button.classList.toggle(
            'is-active',
            button.dataset.proposalTab === tab
        );
    });

    loadProposals(tab);
}

tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        selectTab(
            button.dataset.proposalTab
        );
    });
});

/**
 * 로그인 여부를 확인한 뒤 기본 탭을 조회한다.
 */
function initialize() {
    if (currentUserId === null) {
        const redirect =
            encodeURIComponent(window.location.pathname);

        window.location.href =
            `/login?redirect=${redirect}`;

        return;
    }

    loadProposals('received');
}

initialize();