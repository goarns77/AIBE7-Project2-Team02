import { readApiBody, saveAccessToken } from './auth-client.js';

const form = document.querySelector('#account-login-form');
const message = document.querySelector('#account-form-message');
const submit = form.querySelector('button[type="submit"]');

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  message.textContent = '';
  submit.disabled = true;
  submit.textContent = '확인 중...';

  const formData = new FormData(form);
  try {
    const response = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(Object.fromEntries(formData)),
    });
    const body = await readApiBody(response);
    if (!response.ok) throw new Error(body?.message || '로그인에 실패했습니다.');

    saveAccessToken(body.accessToken);
    message.classList.add('is-success');
    message.textContent = '로그인되었습니다. 이동합니다.';
    window.location.assign(safeRedirectTarget());
  } catch (error) {
    message.classList.remove('is-success');
    message.textContent = error.message || '네트워크 연결을 확인해 주세요.';
  } finally {
    submit.disabled = false;
    submit.textContent = '로그인';
  }
});

function safeRedirectTarget() {
  const target = new URLSearchParams(window.location.search).get('redirect');
  return target?.startsWith('/') && !target.startsWith('//') ? target : '/';
}
