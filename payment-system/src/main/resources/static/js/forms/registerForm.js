
const API_BASE_URL = "http://localhost:8080";  //api 공통 시작부


const emailInput = document.getElementById('emailInput');
const passwordInput = document.getElementById('passwordInput');
const nameInput = document.getElementById('nameInput');
const registerButton = document.getElementById('registerButton');
// 회원가입 처리
registerButton.addEventListener('click',async function () {
    const email = emailInput.value;
    const password = passwordInput.value;
    const userName = nameInput.value;

    if (!email || !password || !userName) {
        alert("error', '모든 필드를 입력해주세요.")
        return;
    }

    // 이메일 검증 추가
    if (!validateEmail(email)) {
        alert("올바른 이메일 형식이 아닙니다.");
        return;
    }

    if (password.length < 6) {
        alert("비밀번호는 6 자 이상이어야 합니다")
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password, userName })
        });

        const data = await response.json();

        if (!response.ok) {
            console.log("회원가입에 실패했습니다")
        }

        console.log(email, password, userName)
        alert("회원가입에 성공 했습니다")
    } catch (error) {
        console.error('회원가입 오류:', error);
        showResult('error', `오류: ${error.message}`);
        registerButton.disabled = false;
        registerButton.textContent = '회원가입';
    }
});

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}