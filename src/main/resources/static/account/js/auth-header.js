import {clearAccessToken, readAccessTokenPayload, readCurrentUserRole} from '/account/js/auth-client.js';

/**
 * JWT 로그인 상태에 따라 공통 헤더 메뉴를 변경한다.
 */
const header = document.querySelector('[data-auth-header]');

if (header) {
    const payload = readAccessTokenPayload();

    const guestLinks = header.querySelectorAll('[data-guest-link]');
    const memberLinks = header.querySelectorAll('[data-member-link]');
    const logoutButton = header.querySelector('[data-logout-button]');
    const adminLink = header.querySelector('[data-admin-link]');

    // 만료된 JWT도 로그인 상태로 표시하지 않는다.
    const expired =
        payload?.exp &&
        payload.exp * 1000 <= Date.now();

    const loggedIn = Boolean(payload) && !expired;

    if (expired) {
        clearAccessToken();
    }

    guestLinks.forEach(link => {
        link.hidden = loggedIn;
    });

    memberLinks.forEach(link => {
        link.hidden = !loggedIn;
    });

    if (logoutButton) {
        logoutButton.hidden = !loggedIn;

        logoutButton.addEventListener('click', () => {
            clearAccessToken();
            window.location.href = '/';
        });
    }

    if (adminLink) {
        adminLink.hidden =
            !loggedIn ||
            readCurrentUserRole() !== 'ADMIN';
    }
}