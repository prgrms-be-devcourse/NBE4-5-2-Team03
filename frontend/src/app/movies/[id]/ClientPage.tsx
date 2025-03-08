"use client";

import { Card } from "@/components/ui/card";
import { components } from "@/lib/backend/apiV1/schema";

export default function ClientPage({
  data,
}: {
  data: components["schemas"]["MovieResponseWithDetail"];
}) {
  return (
    <div className="max-w-6xl mx-auto px-4 py-10 space-y-10">
      {/* 상단: 포스터 + 영화 정보 */}
      <div className="flex flex-col md:flex-row gap-12">
        {/* 왼쪽: 영화 포스터 */}
        <div className="w-full md:w-1/3">
          <img
            src={data.posterPath || "/no-image.png"}
            alt={data.title}
            className="w-full h-auto object-cover rounded-lg shadow-lg"
          />
        </div>

        {/* 오른쪽: 영화 정보 */}
        <div className="w-full md:w-2/3 space-y-4">
          <h1 className="text-4xl font-bold">{data.title}</h1>
          <div className="text-gray-500">
            <ul className="space-y-4">
              <li>
                <span className="font-semibold">장르:</span>{" "}
                {data.genres?.map((genre) => genre.name).join(", ") ||
                  "정보 없음"}
              </li>
              <li>
                <span className="font-semibold">개봉일:</span>{" "}
                {data.releaseDate || "미공개"}
              </li>
              <li>
                <span className="font-semibold">영화 상태:</span>{" "}
                {data.status || "미공개"}
              </li>
              <li>
                <span className="font-semibold">상영 시간:</span>{" "}
                {data.runtime ? `${data.runtime} 분` : "미공개"}
              </li>
              <li>
                <span className="font-semibold">제작사:</span>{" "}
                {data.productionCompany || "미공개"}
              </li>
              <li>
                <span className="font-semibold">제작 국가:</span>{" "}
                {data.productionCountry || "미공개"}
              </li>
            </ul>
          </div>

          {/* 영화 개요 */}
          <h2 className="text-2xl font-bold mb-4">줄거리</h2>
          <p className="text-gray-700">
            {data.overview || "줄거리 정보가 없습니다."}
          </p>
        </div>
      </div>

      {/* 감독 정보 */}
      <div>
        <h2 className="text-2xl font-bold mb-4">🎬 감독</h2>
        <div className="flex">
          <Card className="w-40 flex flex-col items-center p-4 shadow-md">
            <img
              src={"test.png"} // 프로필 이미지 (임시)
              alt={data.director?.name}
              className="w-20 h-20 object-cover rounded-full border mb-3"
            />
            <h3 className="text-lg font-semibold text-center">
              {data.director?.name || "미정"}
            </h3>
          </Card>
        </div>
      </div>

      {/* 배우 정보 */}
      <div>
        <h2 className="text-2xl font-bold mb-4">🎭 배우</h2>
        <div className="grid grid-cols-5 gap-6">
          {data.actors?.map((actor) => (
            <Card
              key={actor.id}
              className="flex flex-col items-center p-4 shadow-md w-40"
            >
              <img
                src={"test.png"} // 프로필 이미지 (임시)
                alt={actor.name}
                className="w-20 h-20 object-cover rounded-full border mb-3"
              />
              <h3 className="text-lg font-semibold text-center">
                {actor.name}
              </h3>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}
