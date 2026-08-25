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
