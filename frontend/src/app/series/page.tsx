import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: {
    keyword: string;
    page: number;
    pageSize: number;
    sortBy: string;
  };
}) {
  const {
    keyword = "",
    page = 1,
    pageSize = 10,
    sortBy = "id",
  } = await searchParams;

  const response = await client.GET("/api/series", {
    params: {
      query: {
        keyword,
        pageSize,
        page,
        sortBy,
      },
    },
  });

  if (response.data?.data == null) {
    return <div>{response.data?.message}</div>;
  }

  const data = response.data.data;

  return (
    <ClientPage
      data={data}
      keyword={keyword}
      pageSize={pageSize}
      page={page}
      sortBy={sortBy}
    />
  );
}
