"use client";

import { useEffect, useState } from "react";
import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableHead,
  TableHeader,
  TableRow,
  TableCell,
} from "@/components/ui/table";

export default function AdminReviewPage() {
  const router = useRouter();
  const [reviews, setReviews] = useState<components["schemas"]["ReviewDto"][]>(
    []
  );
  const [loading, setLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortBy, setSortBy] = useState("latest"); // 기본 정렬: 최신 순

  // 리뷰 목록 가져오기
  const fetchReviews = async () => {
    setLoading(true);
    const response = await client.GET("/api/reviews/search", {
      params: {
        query: {
          keyword: searchKeyword,
          page: page - 1,
          size: pageSize,
        },
      },
    });

    if (!response.error) {
      // response.data  객체 타입 체크 및 any 타입 단언 후 items 접근
      setReviews(
        response.data &&
          typeof response.data === "object" &&
          (response.data as any).items
          ? (response.data as any).items
          : []
      );
    }
    setLoading(false);
  };

  // 삭제 기능
  const handleDelete = async (reviewId: number) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    const response = await client.DELETE("/api/reviews/{id}" as any, {
      params: { path: { id: reviewId } },
    });

    if (!response.error) {
      alert("삭제되었습니다.");
      fetchReviews(); // 삭제 후 목록 갱신
    } else {
      alert("삭제 실패: " + response.error.message);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [page, pageSize, searchKeyword]);

  // 검색 버튼 클릭 핸들러
  const handleSearchClick = () => {
    setPage(1); // 검색 시 페이지 1로 초기화
    fetchReviews(); // 검색어로 리뷰 목록 조회
  };

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">리뷰 관리</h1>

      {/* 검색 & 정렬 */}
      <div className="flex gap-3 mb-4">
        <Input
          placeholder="닉네임 또는 내용 검색"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <Button onClick={handleSearchClick}>검색</Button>
      </div>

      {/* 리뷰 목록 */}
      {loading ? (
        <p>로딩 중...</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>닉네임</TableHead>
              <TableHead>평점</TableHead>
              <TableHead>내용</TableHead>
              <TableHead>삭제</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {reviews.length > 0 ? (
              reviews.map((review) => (
                <TableRow key={review.id}>
                  <TableCell>{review.nickname}</TableCell>
                  <TableCell>{review.rating} ⭐</TableCell>
                  <TableCell className="truncate w-60">
                    {review.content}
                  </TableCell>
                  <TableCell>
                    <Button
                      variant="destructive"
                      onClick={() => {
                        if (review.id !== undefined) {
                          handleDelete(review.id);
                        } else {
                          console.error("리뷰 id가 존재하지 않습니다.");
                        }
                      }}
                    >
                      삭제
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={4} className="text-center">
                  리뷰가 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
