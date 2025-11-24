import { createAuthHeaders } from "../modules/token.js";

const API_BASE_URL = "http://localhost:8080";  //api 공통 시작부

//dom Element 객체 반환
const totalAmountInput = document.getElementById('totalAmount');
const pointsInput = document.getElementById('points');
const finalAmountDisplay = document.getElementById('finalAmount');
const formulaDisplay = document.getElementById('formula');
const getOrdersBtn = document.getElementById('getOrdersBtn');
const getPaymentsBtn = document.getElementById('getPaymentsBtn');
const paymentBtn = document.getElementById('paymentBtn');
const applyOrderBtn = document.getElementById('applyOrderBtn');

// 주문 적용 버튼 이벤트 처리 메서드
applyOrderBtn.addEventListener('click', async function () {
    const orderId = document.getElementById('orderId').value;

    if (!orderId) {
        alert("주문 ID를 입력하세요.");
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/api/orders/${orderId}`, {
            method: "GET",
            headers: createAuthHeaders(),
        });
        if (!res.ok) {
            alert("orderId 를 불러올 수 없습니다.");
            return;
        }

        const data = await res.json();

        //  성공 시 총금액 input 자동 세팅
        totalAmountInput.value = data.totalAmount || 0;

        //  최종 결제 금액 계산
        calculateFinalAmount();

        alert("주문 정보가 적용되었습니다.");

    } catch (e) {
        console.error(e);
        alert("서버 요청 중 오류 발생");
    }
});

//주문 조회 버튼 이밴트 처리 메서드
getOrdersBtn.addEventListener('click', async function () {

    try {
        const res = await fetch(`${API_BASE_URL}/api/orders`, {
            method: "GET",
            headers: createAuthHeaders(),
        });
        if (!res.ok) {
            alert("주문 내역 리스트 를 불러올 수 없습니다.");
            return;
        }

        alert("주문 리스트가 조회됨");

    } catch (e) {
        console.error(e);
        alert("서버 요청 중 오류 발생");
    }
});

//결제 조회 버튼 이밴트 처리 메서드
getPaymentsBtn.addEventListener('click', async function () {

    try {
        const res = await fetch(`${API_BASE_URL}/api/payments/paid`, {
            method: "GET",
            headers: createAuthHeaders(),
        });
        if (!res.ok) {
            alert("결제 내역 리스트 를 불러올 수 없습니다.");
            return;
        }

        alert("결제 리스트가  조회됨");

    } catch (e) {
        console.error(e);
        alert("서버 요청 중 오류 발생");
    }
});

// 최종 결제 금액 계산 함수
function calculateFinalAmount() {
    // 주문 총 금액을 숫자로 변환
    const total = parseFloat(totalAmountInput.value) || 0;
    // 사용 포인트 숫자로 변환 (입력이 없으면 0으로 처리)
    const points = parseFloat(pointsInput.value) || 0;
    // 최종 결제 금액 (총 금액 - 포인트)
    const final = total - points;

    // 입력값이 모두 비어있을 때 결과 표시 초기화
    if (totalAmountInput.value === '' && pointsInput.value === '') {
        formulaDisplay.textContent = ' ';
        finalAmountDisplay.textContent = ' ';
    } else {
        formulaDisplay.textContent = total.toLocaleString() + '원 - ' + points.toLocaleString() + '원';
        finalAmountDisplay.textContent = '= ' + final.toLocaleString() + '원';
    }
}

// input 이벤트가 발생 했을때 어떤 함수를 실행할지
totalAmountInput.addEventListener('input', calculateFinalAmount);
pointsInput.addEventListener('input', calculateFinalAmount);

// 결제 버튼 클릭 이벤트 처리
// async function 은 비동기 함수
paymentBtn.addEventListener('click', async function (e) {
    e.preventDefault();
    const orderId = document.getElementById('orderId').value;
    const total = parseFloat(totalAmountInput.value) || 0;
    const points = parseFloat(pointsInput.value) || 0;
    const final = total - points;
    console.log(orderId, total, points, final);

    // 검증 1. 프론트 단에서의 검증 코드: orderId와 total이 기입되지 않은 경우
    if (!orderId || total <= 0) {
        alert("주문 ID와 총 금액을 입력해주세요.");
        return;
    }

    // 검증 2. 총 결제 금액보다 주문금액이 많은 경우
    if (final < 0) {
        alert('포인트가 주문 금액보다 많습니다.');
        return;
    }

    alert('결제 금액: ' + final.toLocaleString() + '원\n결제를 진행합니다.');
    // alert 확인 버튼을 누른 후에 바로 실행되는 코드

    //
    // console.log(STORE_ID, CHANNEL_KEY, USER_ID, PAYMENT_ID);
    const redirectUrl = window.location.origin + window.location.pathname;

    // 여기서부터가 진짜 PortOne 결제 로직
    const response = await PortOne.requestPayment({
        storeId: STORE_ID,
        channelKey: CHANNEL_KEY,
        paymentId: PAYMENT_ID,
        orderName: `주문번호 ${orderId}`,
        totalAmount: final,  // 결제 금액
        currency: "CURRENCY_KRW",  // 원
        payMethod: 'CARD',
        pg: "inicis_v2",  // 이니시스 PG
        customer: {
            customerId: USER_ID.toString(),
            fullName: "테스트 사용자",
            phoneNumber: "010-1234-5678",
            email: "test@example.com"
        },
        redirectUrl: redirectUrl, // 결제 완료 후 리다이렉트 URL
        testMode: true,
        customData: {
            orderId: orderId
        }
    });

    // 결제가 실패하는 경우
    if (response.code != null) {
        console.error("결제 실패", response);
        console.log("결제 상태, failed")
        return;
    }

    //리다이렉트 후에도 결제 정보를 처리할 수 있도록 paymentId를 sessionStorage에 저장
    //결제 검증 후에 만료 시켜야 한다.
    if(response.paymentId){
        sessionStorage.setItem('pendingPaymentId', response.paymentId);
        sessionStorage.setItem('pendingOrderId', orderId);
        console.log('결제 정보를 sessionStorage에 저장:', {
            paymentId: paymentResponse.paymentId,
            orderId: orderId
        });
    }

    //결제 검증 로직
    try{
        await verifyPayment(response.paymentId); // 백엔드로 결제 검증 요청 메서드 호출
        // 검증 성공 시 sessionStorage에서 제거
        sessionStorage.removeItem('pendingPaymentId');
        sessionStorage.removeItem('pendingOrderId');
        console.log(`paymentId`, response.paymentId, '결제 검증이 성공했습니다.')
        alert('결제 검증이 성공했습니다.')

    } catch (error) {
        console.warn('결제 검증 중 오류 (리다이렉트 후 재시도 가능):', error);
        alert("결제 검증에 실패 했습니다. 다시 시도해 주세요")
        // 오류가 발생해도 리다이렉트 후 재시도할 수 있도록 sessionStorage는 유지
    }

    // 결제 검증
    async function verifyPayment(orderId, paymentId, final) {
        // 결제 검증 엔드포인트 호출
        const response = await fetch(`${API_BASE_URL}/api/payments/complete`, {
            method: 'POST',
            headers: createAuthHeaders(),
            body: JSON.stringify({
                orderId: orderId,
                impUid: paymentId,
                amount: final
            }) //requestBody 형식 - VerifyPaymentRequest  dto 형식 orderId, impUid, amount 과 매칭
        });
        const resultText = await response.text();

        if (!response.ok) {
            alert("결제 검증 중 예상치 못한 오류 발생 ( error message : ${resultText})");
        }
    }
});

// 페이지 로드 시 초기 계산
calculateFinalAmount();