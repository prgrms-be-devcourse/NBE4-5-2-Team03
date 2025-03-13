import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: {
    page?: number;
    pageSize?: number;
    sortBy?: string;
    userId?: string;
  };
}) {
  const params = await searchParams;

  const { userId = "1", page = 1, pageSize = 10, sortBy = "id" } = params;

  try {
    const response = await client.GET("/api/favorites/{userId}", {
      params: {
        path: { userId },
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
        userId={userId}
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
