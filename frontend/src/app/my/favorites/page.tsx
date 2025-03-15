import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";
import { fetchUserProfileServer } from "@/lib/api/user";
import { cookies } from "next/headers"; // 서버에서 쿠키 가져오기

export default async function Page({
  searchParams,
}: {
  searchParams: {
    page?: number;
    pageSize?: number;
    sortBy?: string;
  };
}) {
  const params = await searchParams;
  const { page = 1, pageSize = 10, sortBy = "id" } = params;

  // 서버에서 쿠키를 가져와 인증 포함 요청
  const cookieHeader = cookies().toString();
  const user = await fetchUserProfileServer(cookieHeader);

  if (!user) {
    return <div>로그인이 필요합니다.</div>;
  }

  try {
    const response = await client.GET("/api/favorites/{userId}", {
      params: {
        path: { userId: user.id },
        query: {
          pageSize,
          page,
          sortBy,
          direction: "desc",
        },
      },
    });

    // API 응답 데이터 검증
    if (!response || !response.data) {
      return <div>데이터를 불러오는 데 실패했습니다.</div>;
    }

    return (
      <ClientPage
        data={response.data.data}
        userId={String(user.id)}
        pageSize={pageSize}
        page={page}
        sortBy={sortBy}
      />
    );
  } catch (error) {
    console.error("에러 발생:", error);
    return <div>에러 발생: {String(error)}</div>;
  }
}
