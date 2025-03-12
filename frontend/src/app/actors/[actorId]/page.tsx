import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";

export default async function Page({
  params,
}: {
  params: { actorId: string };
}) {
  const { actorId } = params;

  try {
    const response = await client.GET("/api/actors/{actorId}", {
      params: {
        path: { actorId },
      },
    });

    // API 응답 데이터 검증
    if (!response || !response.data) {
      return <div>배우 정보를 불러오는 데 실패했습니다.</div>;
    }

    return <ClientPage actor={response.data} />;
  } catch (error) {
    console.error("에러 발생:", error);
    return <div>에러 발생: {String(error)}</div>;
  }
}
