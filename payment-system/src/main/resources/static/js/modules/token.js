
export function createAuthHeaders() {
    const accessToken = localStorage.getItem("accessToken");  //localStorage 에서
    const refreshToken = localStorage.getItem("refreshToken");

    return {
        "Content-Type": "application/json",
        "Authorization": accessToken ? `Bearer ${accessToken}` : "",
        "X-Refresh-Token": refreshToken ? refreshToken : ""
    };
}
