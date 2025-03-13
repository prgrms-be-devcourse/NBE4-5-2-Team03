import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";

export default async function Page({
  params,
}: {
  params: {
    id: number;
  };
}) {
  const { id } = await params;

  const response = await client.GET("/api/series/{id}", {
    params: {
      path: {
        id,
      },
    },
  });

  if (response.data?.data == null) {
    return <div>{response.data?.message}</div>;
  }

  const data = response.data.data;

  return <ClientPage data={data} />;
}
