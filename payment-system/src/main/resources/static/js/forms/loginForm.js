

const API_BASE_URL = "http://localhost:8080";  //api 공통 시작부

//dom Element 객체 반환
const emailInput = document.getElementById('emailInput');
const passwordInput = document.getElementById('passwordInput');
const loginBtn = document.getElementById('loginBtn');
const registerBtn = document.getElementById('registerBtn');

// localStorage setter
function setAuthToken(data) {

    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('userId', data.userId);
}

// localStorage 내용 비우기
function removeAuthToken() {

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
    console.log("localStorage clear")
}

// 로그인 처리
loginBtn.addEventListener('click', async function () {

    const email = emailInput.value;
    const password = passwordInput.value;
        //검증 로직 1.
        if (!email || !password) {
            alert("email 과 password 를 입력해주세요. ");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            // 401 인증 실패 (아이디 틀림)
            if (response.status === 404) {
                const msg = data.message
                alert(msg);
                return;
            }

            // 400 비밀번호 오류
            if (response.status === 400) {
                const msg = data.message
                alert(msg);
                return;
            }

            // 그 외 response.ok가 false인 경우 공통 실패 처리
            if (!response.ok) {
                console.log("로그인 실패");
                return;
            }

            // ---- 여기 도달하면 로그인 성공 ----
            removeAuthToken()  //이전에 있던 localStorage 의 토큰 지우기
            setAuthToken(data);
            console.log(data.userId, data.email, data.name);
            alert("로그인 성공");
            await LoginSuccess()

        } catch (error) {
            console.error('로그인 오류:', error);
            alert("로그인에 실패 했습니다.");
        }
    }
)

//로그인 성공시 호출하는 메서드
async function LoginSuccess()  {

    try {
        window.location.href = `${API_BASE_URL}/api/payment`;
        alert("결제 화면을 불러옵니다");

    } catch (e) {
        console.error(e);
        alert("서버 요청 중 오류 발생");
    }
}
//로그인 실패시 호출하는 메서드
// 미 구현