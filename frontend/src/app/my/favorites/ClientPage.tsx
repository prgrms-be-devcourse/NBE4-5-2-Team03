"use client";

import client from "@/lib/backend/client";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import React from "react";

type FavoriteContentDto = {
  title: string;
  imageUrl: string;
  averageRating: number;
  ratingCount: number;
};

export default function ClientPage({
  data,
  userId,
  page,
  pageSize,
  sortBy,
}: {
  data: components["schemas"]["PageDtoFavoriteDto"];
  userId: string;
  page: number;
  pageSize: number;
  sortBy: string;
}) {
  const router = useRouter();
  const favorites = data.items || [];
  const totalPages = data.totalPages ?? 1;

  const handleDelete = async (favoriteId: number) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) {
      return; // 사용자가 취소하면 삭제 중단
    }

    try {
      // console.log("삭제 요청 favoriteId:", favoriteId); // ✅ 삭제 요청 확인용 로그

      const res = await client.DELETE("/api/favorites/{id}", {
        params: { path: { id: favoriteId } },
      });

      if (!res) {
        new Error("삭제 요청 실패");
      }

      router.refresh();
    } catch (error) {
      console.error("삭제 요청 실패:", error);
      alert("삭제에 실패했습니다.");
    }
  };

  // 정렬 옵션 변경 시 즉시 반영
  const handleSortChange = (value: string) => {
    router.push(
      `/my/favorites?page=${page}&pageSize=${pageSize}&sortBy=${value}`,
    );
  };

  // 페이지 크기 변경 시 첫 페이지로 이동하며 즉시 반영
  const handlePageSizeChange = (value: string) => {
    router.push(`/my/favorites?page=1&pageSize=${value}&sortBy=${sortBy}`);
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6 text-center">
        ⭐ 즐겨찾기 목록 ({data.totalItems ?? 0})
      </h1>

      {/* 정렬 옵션 + 페이지 크기 선택 */}
      <div className="flex flex-col md:flex-row gap-4 justify-between items-center mb-6">
        <div className="flex gap-4 items-center">
          {/* 정렬 옵션 */}
          <Select onValueChange={handleSortChange} defaultValue={sortBy}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="정렬 기준" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="id">아이디 순</SelectItem>
              <SelectItem value="rating">평점 순</SelectItem>
              <SelectItem value="reviews">리뷰 많은 순</SelectItem>
            </SelectContent>
          </Select>

          {/* 페이지 크기 선택 */}
          <Select
            onValueChange={handlePageSizeChange}
            defaultValue={String(pageSize)}
          >
            <SelectTrigger className="w-24">
              <SelectValue placeholder="페이지 크기" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="10">10개</SelectItem>
              <SelectItem value="15">15개</SelectItem>
              <SelectItem value="20">20개</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex flex-col gap-4">
        {favorites.map((favorite) => {
          // `data` 필드에서 FavoriteContentDto 타입으로 변환
          const content = favorite.data as unknown as FavoriteContentDto | null;
          const title = content?.title || "제목 없음";
          const rating =
            content?.averageRating !== null &&
            content?.averageRating !== undefined
              ? content.averageRating.toFixed(1)
              : "N/A";

          const reviewCount = content?.ratingCount ?? 0;
          const poster = content?.imageUrl || "/no-image.png";

          // 종류에 따른 디테일 페이지 URL 설정
          let detailUrl = "";
          if (favorite.contentType === "MOVIE") {
            detailUrl = `/movies/${favorite.contentId}`;
          } else if (favorite.contentType === "SERIES") {
            detailUrl = `/series/${favorite.contentId}`;
          }

          return (
            <div
              key={`${favorite.contentType}-${favorite.contentId}`} // ✅ contentType 추가하여 고유한 key 보장
              className="flex items-center justify-between bg-white shadow-md p-4 rounded-lg cursor-pointer hover:bg-gray-50 transition"
              onClick={() => router.push(detailUrl)}
            >
              {/* 종류 */}
              <div className="text-sm font-semibold text-gray-600 w-16 flex-shrink-0">
                {favorite.contentType}
              </div>

              {/* 제목 (적당한 여백 추가) */}
              <div className="flex-1 text-lg font-semibold text-gray-800 px-6 truncate">
                {title}
              </div>

              {/* 별점 */}
              <div className="text-sm text-gray-700 flex items-center w-24 text-center mr-4">
                ⭐ {rating} ({reviewCount})
              </div>

              {/* 이미지 */}
              <div className="w-24 h-32 flex-shrink-0 mr-4">
                <img
                  src={poster}
                  alt={title}
                  className="w-full h-full object-cover rounded"
                />
              </div>

              {/* 삭제 버튼 */}
              <Button
                className="ml-auto" // 자동 우측 정렬
                onClick={async (e) => {
                  e.stopPropagation();
                  await handleDelete(favorite.id);
                }}
                variant="destructive"
              >
                삭제
              </Button>
            </div>
          );
        })}
      </div>

      {/* 페이지네이션 */}
      <div className="flex justify-center mt-8 space-x-2 min-w-[250px]">
        {/* 이전 버튼 (첫 페이지에서는 숨김) */}
        <Button
          variant="outline"
          className={Number(page) === 1 ? "invisible" : ""}
          onClick={() =>
            Number(page) > 1 &&
            router.push(
              `/my/favorites?page=${Number(page) - 1}&pageSize=${pageSize}&sortBy=${sortBy}`,
            )
          }
        >
          이전
        </Button>

        {/* 페이지 번호 버튼 */}
        {Array.from({ length: totalPages }, (_, i) => i + 1)
          .filter(
            (pageNo) =>
              pageNo === 1 ||
              pageNo === totalPages ||
              (pageNo >= Number(page) - 2 && pageNo <= Number(page) + 2),
          )
          .map((pageNo, index, arr) => (
            <React.Fragment key={pageNo}>
              {/* "..." 표시 추가 (범위가 연속되지 않는 경우) */}
              {index > 0 && pageNo !== arr[index - 1] + 1 && (
                <span key={`dots-${pageNo}`} className="px-2">
                  ...
                </span>
              )}

              <Button
                key={pageNo}
                variant={pageNo === Number(page) ? "default" : "outline"}
                onClick={() =>
                  router.push(
                    `/my/favorites?page=${pageNo}&pageSize=${pageSize}&sortBy=${sortBy}`,
                  )
                }
              >
                {pageNo}
              </Button>
            </React.Fragment>
          ))}

        {/* 다음 버튼 (마지막 페이지에서는 숨김) */}
        <Button
          variant="outline"
          className={Number(page) === totalPages ? "invisible" : ""}
          onClick={() =>
            Number(page) < totalPages &&
            router.push(
              `/my/favorites?page=${Number(page) + 1}&pageSize=${pageSize}&sortBy=${sortBy}`,
            )
          }
        >
          다음
        </Button>
      </div>
    </div>
  );
}
