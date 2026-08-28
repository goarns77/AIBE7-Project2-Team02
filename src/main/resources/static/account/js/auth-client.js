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

export async function authFetch(url, options = {}) {
  const headers = new Headers(options.headers || {});
  const token = readAccessToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(url, { ...options, headers });
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
