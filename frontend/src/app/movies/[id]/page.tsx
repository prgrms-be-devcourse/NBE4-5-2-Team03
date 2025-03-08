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

  const response = await client.GET("/api/movies/{id}", {
    params: {
      path: {
        id,
      },
    },
  });

  if (response.error) {
    return <div>{response}</div>;
  }

  const data = response.data;

  return <ClientPage data={data} />;
}
