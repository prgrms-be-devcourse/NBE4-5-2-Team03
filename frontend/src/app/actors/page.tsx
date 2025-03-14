import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: {
    keyword: string;
    page: number;
    pageSize: number;
  };
}) {
  const { keyword = "", page = 1, pageSize = 10 } = await searchParams;

  const response = await client.GET("/api/actors", {
    params: {
      query: {
        keyword,
        pageSize,
        page,
      },
    },
  });

  if (response.data == null) {
    return <div>배우 목록을 불러오는데 실패했습니다.</div>;
  }

  const data = response.data;
  return (
    <ClientPage data={data} keyword={keyword} pageSize={pageSize} page={page} />
  );
}
