const API_BASE_URL = "http://localhost:8080"; // API 기본 URL

// 🔹 서버 측 요청을 위한 fetch 함수 (SSR)
export const fetchUserProfileServer = async (cookieHeader: string) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/users`, {
      method: "GET",
      headers: { Cookie: cookieHeader }, // 서버에서 쿠키를 포함해서 요청
    });

    if (response.ok) {
      const data = await response.json();
      return data.data; // 유저 정보 객체 반환
    } else {
      console.error("서버: 사용자 정보를 가져오지 못했습니다.");
      return null;
    }
  } catch (error) {
    console.error("서버: 유저 정보 가져오는 중 에러 발생:", error);
    return null;
  }
};

// 🔹 클라이언트 측 요청을 위한 fetch 함수 (CSR)
export const fetchUserProfileClient = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/users`, {
      method: "GET",
      credentials: "include", // 클라이언트에서는 쿠키 자동 포함
    });

    if (response.ok) {
      const data = await response.json();
      return data.data;
    } else {
      console.error("클라이언트: 사용자 정보를 가져오지 못했습니다.");
      return null;
    }
  } catch (error) {
    console.error("클라이언트: 유저 정보 가져오는 중 에러 발생:", error);
    return null;
  }
};
