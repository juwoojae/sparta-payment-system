
//
export function createAuthHeaders() {
    const accessToken = localStorage.getItem("accessToken");  //localStorage 에서
    const refreshToken = localStorage.getItem("refreshToken");

    return {
        "Content-Type": "application/json",
        "Authorization": accessToken ? `Bearer ${accessToken}` : "",
        "X-Refresh-Token": refreshToken ? refreshToken : ""
    };
}

// localStorage setter
export function setAuthToken(data) {

    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
}

// localStorage 내용 비우기
export function removeAuthToken() {

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    console.log("localStorage clear")
}

// localStorage 에서 refresh 토큰 가지고 오는 메서드
export function getRefreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    console.log("refreshToken:", refreshToken);
    return refreshToken;
}

