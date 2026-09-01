const ACCESS_TOKEN_KEY = 'matcheat.accessToken';

export function saveAccessToken(token) {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function readAccessToken() {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
}

export function clearAccessToken() {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
}

/**
 * 저장된 JWT의 payload를 읽는다.
 * 화면 표시용 정보만 사용하며 실제 권한 검증은 서버에서 수행한다.
 */
export function readAccessTokenPayload() {
    const token = readAccessToken();

    if (!token) {
        return null;
    }

    try {
        const [, payload] = token.split('.');

        if (!payload) {
            return null;
        }

        const normalized = payload
            .replace(/-/g, '+')
            .replace(/_/g, '/');

        const padded = normalized.padEnd(
            Math.ceil(normalized.length / 4) * 4,
            '='
        );

        return JSON.parse(atob(padded));
    } catch {
        clearAccessToken();
        return null;
    }
}

/**
 * 현재 로그인한 사용자의 userId를 JWT sub에서 읽는다.
 */
export function readCurrentUserId() {
    const payload = readAccessTokenPayload();

    if (!payload?.sub) {
        return null;
    }

    const userId = Number(payload.sub);

    return Number.isFinite(userId) ? userId : null;
}

/**
 * 현재 로그인한 사용자의 역할을 JWT에서 읽는다.
 */
export function readCurrentUserRole() {
    return readAccessTokenPayload()?.role ?? null;
}

export async function authFetch(url, options = {}) {
    const headers = new Headers(options.headers || {});
    const token = readAccessToken();
    if (token) headers.set('Authorization', `Bearer ${token}`);

    const response = await fetch(url, {...options, headers});
    if (response.status === 401) {
        clearAccessToken();
    }
    return response;
}

export async function readApiBody(response) {
    const type = response.headers.get('content-type') || '';
    return type.includes('application/json') ? response.json() : null;
}

export function clearFieldErrors(form) {
    for (const element of form.elements) {
        if (element instanceof HTMLElement && element.matches('input, select, textarea')) {
            element.removeAttribute('aria-invalid');
        }
    }
    form.querySelectorAll('[data-field-error]').forEach((element) => {
        element.classList.remove('is-success');
        element.textContent = '';
    });
}

export function showFieldErrors(form, fieldErrors = {}) {
    for (const [field, message] of Object.entries(fieldErrors)) {
        const input = form.elements.namedItem(field);
        if (input instanceof HTMLElement) input.setAttribute('aria-invalid', 'true');
        const output = [...form.querySelectorAll('[data-field-error]')]
            .find((element) => element.dataset.fieldError === field);
        if (output) {
            output.classList.remove('is-success');
            output.textContent = message;
        }
    }
}

export function validateRequiredFields(form) {
    clearFieldErrors(form);
    if (form.checkValidity()) return true;

    for (const element of form.elements) {
        if (!(element instanceof HTMLInputElement) || element.validity.valid) continue;
        element.setAttribute('aria-invalid', 'true');
        const output = [...form.querySelectorAll('[data-field-error]')]
            .find((candidate) => candidate.dataset.fieldError === element.name);
        if (output) output.textContent = element.validationMessage;
    }
    form.reportValidity();
    return false;
}

export function safeInternalRedirect(search, fallback = '/') {
    const target = new URLSearchParams(search).get('redirect');
    if (!target || !target.startsWith('/') || target.startsWith('//') || target.includes('\\')) {
        return fallback;
    }
    const resolved = new URL(target, window.location.origin);
    return resolved.origin === window.location.origin
        ? `${resolved.pathname}${resolved.search}${resolved.hash}`
        : fallback;
}
