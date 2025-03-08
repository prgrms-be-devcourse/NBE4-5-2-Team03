"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { components } from "@/lib/backend/apiV1/schema";

export default function ClientPage({
  data,
  keyword,
  page,
  pageSize,
  sortBy,
}: {
  data: components["schemas"]["PageDtoMovieResponse"];
  keyword: string;
  page: number;
  pageSize: number;
  sortBy: string;
}) {
  const router = useRouter();
  const movies = data.items;
  const totalPages = data.totalPages || 1;

  // 페이지 그룹 계산 (5개씩 이동)
  const groupSize = 5;
  const currentGroup = Math.floor((page - 1) / groupSize);
  const startPage = currentGroup * groupSize + 1;
  const endPage = Math.min(startPage + groupSize - 1, totalPages);

  // 이전/다음 그룹 이동 페이지 계산
  const prevPage = startPage - 1;
  const nextPage = endPage + 1;

  // 첫 번째 & 마지막 그룹 여부 확인
  const isFirstGroup = startPage === 1;
  const isLastGroup = endPage >= totalPages;

  // 상태 관리 (검색어, 정렬 기준, 페이지 크기)
  const [searchKeyword, setSearchKeyword] = useState(keyword);

  // 검색 실행 함수
  const handleSearch = () => {
    router.push(
      `/movies?keyword=${searchKeyword}&page=1&pageSize=${pageSize}&sortBy=${sortBy}`
    );
  };

  // Enter 키 입력 시 검색 실행
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  // 정렬 옵션 변경 시 즉시 반영
  const handleSortChange = (value: string) => {
    router.push(
      `/movies?keyword=${keyword}&page=${page}&pageSize=${pageSize}&sortBy=${value}`
    );
  };

  // 페이지 크기 변경 시 첫 페이지로 이동하며 즉시 반영
  const handlePageSizeChange = (value: string) => {
    router.push(
      `/movies?keyword=${keyword}&page=1&pageSize=${value}&sortBy=${sortBy}`
    );
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6 text-center">🎬 영화 목록</h1>

      {/* 검색 바 + 정렬 옵션 + 페이지 크기 선택 */}
      <div className="flex flex-col md:flex-row gap-4 justify-between items-center mb-6">
        <div className="flex gap-2">
          <Input
            type="text"
            placeholder="검색어를 입력하세요"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onKeyDown={handleKeyDown}
            className="w-64"
          />
          <Button
            variant="default"
            onClick={handleSearch}
            className="cursor-pointer"
          >
            검색
          </Button>
        </div>

        <div className="flex gap-4 items-center">
          {/* 정렬 옵션 */}
          <Select onValueChange={handleSortChange} defaultValue={sortBy}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="정렬 기준" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="id">아이디 순</SelectItem>
              <SelectItem value="rating">평점 순</SelectItem>
              <SelectItem value="ratingCount">리뷰 많은 순</SelectItem>
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

      {/* 영화 리스트 */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
        {movies.map((movie) => (
          <Link key={movie.id} href={`/movies/${movie.id}`}>
            <Card className="overflow-hidden shadow-lg hover:shadow-xl transition-transform transform hover:scale-105 cursor-pointer">
              <CardHeader className="p-0">
                <img
                  src={movie.posterPath || "/no-image.png"}
                  alt={movie.title}
                  className="w-full h-60 object-contain"
                />
              </CardHeader>
              <CardContent className="p-3 bg-white">
                <h2
                  className="text-lg font-semibold truncate"
                  title={movie.title}
                >
                  {movie.title}
                </h2>
                <div className="flex items-center gap-2 mt-2">
                  <span className="text-yellow-500 font-bold text-lg">
                    ⭐ {movie.averageRating?.toFixed(2)}
                  </span>
                  <span className="text-sm text-gray-500">
                    ({movie.ratingCount} 리뷰)
                  </span>
                </div>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>

      {/* 페이지네이션 */}
      <div className="flex justify-center mt-8 space-x-2">
        {!isFirstGroup && (
          <Link
            href={`/movies?keyword=${keyword}&page=${prevPage}&pageSize=${pageSize}&sortBy=${sortBy}`}
          >
            <Button variant="outline" className="cursor-pointer">
              이전
            </Button>
          </Link>
        )}

        {Array.from(
          { length: endPage - startPage + 1 },
          (_, i) => startPage + i
        ).map((pageNo) => (
          <Link
            key={pageNo}
            href={`/movies?keyword=${keyword}&page=${pageNo}&pageSize=${pageSize}&sortBy=${sortBy}`}
          >
            <Button
              variant={pageNo == page ? "default" : "outline"}
              className={`px-4 py-2 cursor-pointer ${
                pageNo == page
                  ? "font-bold bg-black text-white"
                  : "hover:bg-gray-200"
              }`}
            >
              {pageNo}
            </Button>
          </Link>
        ))}

        {!isLastGroup && (
          <Link
            href={`/movies?keyword=${keyword}&page=${nextPage}&pageSize=${pageSize}&sortBy=${sortBy}`}
          >
            <Button variant="outline" className="cursor-pointer">
              다음
            </Button>
          </Link>
        )}
      </div>
    </div>
  );
}
