import { readApiBody } from './auth-client.js';

const form = document.querySelector('#account-signup-form');
const emailInput = document.querySelector('#email');
const emailCheck = document.querySelector('#account-email-check');
const emailMessage = document.querySelector('#account-email-message');
const formMessage = document.querySelector('#account-form-message');
const submit = form.querySelector('button[type="submit"]');
let checkedEmail = null;

emailInput.addEventListener('input', () => {
  checkedEmail = null;
  emailMessage.classList.remove('is-success');
  emailMessage.textContent = '';
});

emailCheck.addEventListener('click', async () => {
  const email = emailInput.value.trim();
  if (!email) {
    emailMessage.textContent = '이메일을 먼저 입력해 주세요.';
    return;
  }

  emailCheck.disabled = true;
  try {
    const response = await fetch(`/api/v1/auth/email-availability?email=${encodeURIComponent(email)}`);
    const body = await readApiBody(response);
    if (!response.ok) throw new Error(body?.message || '이메일을 확인하지 못했습니다.');

    if (body.available) {
      checkedEmail = body.email;
      emailInput.value = body.email;
      emailMessage.classList.add('is-success');
      emailMessage.textContent = '사용할 수 있는 이메일입니다.';
    } else {
      checkedEmail = null;
      emailMessage.classList.remove('is-success');
      emailMessage.textContent = '이미 사용 중인 이메일입니다.';
    }
  } catch (error) {
    checkedEmail = null;
    emailMessage.classList.remove('is-success');
    emailMessage.textContent = error.message || '네트워크 연결을 확인해 주세요.';
  } finally {
    emailCheck.disabled = false;
  }
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  formMessage.textContent = '';
  const formData = Object.fromEntries(new FormData(form));

  if (formData.password !== formData.passwordConfirm) {
    formMessage.textContent = '비밀번호 확인이 일치하지 않습니다.';
    return;
  }
  if (checkedEmail !== formData.email.trim().toLowerCase()) {
    formMessage.textContent = '이메일 중복 확인을 완료해 주세요.';
    return;
  }

  submit.disabled = true;
  submit.textContent = '가입 중...';
  try {
    const response = await fetch('/api/v1/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData),
    });
    const body = await readApiBody(response);
    if (!response.ok) throw new Error(body?.message || '회원가입에 실패했습니다.');

    formMessage.classList.add('is-success');
    formMessage.textContent = '회원가입되었습니다. 로그인 화면으로 이동합니다.';
    window.setTimeout(() => window.location.assign('/login'), 600);
  } catch (error) {
    formMessage.classList.remove('is-success');
    formMessage.textContent = error.message || '네트워크 연결을 확인해 주세요.';
  } finally {
    submit.disabled = false;
    submit.textContent = '회원가입';
  }
});
