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
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1); // 총 페이지 수 상태
  const [pageSize, setPageSize] = useState(10);

  // 리뷰 목록 가져오기
  const fetchAllReviews = async () => {
    const response = await client.GET("/api/reviews", {
      params: {
        query: {
          page: page - 1, // 백엔드는 페이지를 0부터 시작하므로 -1 처리
          size: pageSize,
        },
      },
    });

    console.log("fetchAllReviews - API 응답 데이터:", response);

    if (!response.error && response.data?.data) {
      setReviews(response.data.data.items || []);
      setTotalPages(response.data.data.totalPages || 1);
    } else {
      console.warn("fetchAllReviews - 응답 데이터가 없음 (API 확인 필요)");
      setReviews([]);
    }
    setLoading(false);
  };

  // 검색어로 리뷰 조회
  const fetchSearchReviews = async () => {
    if (!searchKeyword.trim()) {
      return;
    }
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
    if (!response.error && response.data?.data?.items) {
      setReviews(response.data.data.items || []);
      // API 응답에서 총 페이지 수를 추출하여 상태 업데이트
      setTotalPages(response.data.data.totalPages || 1);
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
      fetchAllReviews(); // 삭제 후 목록 갱신
    } else {
      alert("삭제 실패: " + response.error.message);
    }
  };

  useEffect(() => {
    fetchAllReviews();
  }, [page, pageSize]);

  // 검색 버튼 클릭 핸들러
  const handleSearchClick = () => {
    setPage(1); // 검색 시 페이지 1로 초기화
    fetchSearchReviews();

    // 검색 버튼이 비어있을 때 검색하면 목록 전체 출력
    if (searchKeyword.trim() === "") {
      fetchAllReviews();
    } else {
      fetchSearchReviews();
    }
  };

  const handleNextPage = () => {
    if (page < totalPages) {
      setPage(page + 1);
    }
  };

  const handlePreviousPage = () => {
    if (page > 1) {
      setPage(page - 1);
    }
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
        <>
          <Table>
            <TableHeader style={{ backgroundColor: "lightgray" }}>
              <TableRow>
                <TableHead
                  style={{
                    borderRight: "1px solid #ccc",
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                    width: "10%",
                  }}
                >
                  닉네임
                </TableHead>
                {/* 너비 조정 */}
                <TableHead
                  style={{
                    borderRight: "1px solid #ccc",
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                    width: "5%",
                  }}
                >
                  평점
                </TableHead>
                {/* 너비 조정 */}
                <TableHead
                  style={{
                    borderRight: "1px solid #ccc",
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                  }}
                >
                  내용
                </TableHead>
                {/* 너비 자동 조정 */}
                <TableHead
                  style={{
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                    width: "8%",
                  }}
                >
                  삭제
                </TableHead>
                {/* 너비 조정 */}
              </TableRow>
            </TableHeader>
            <TableBody>
              {reviews.length > 0 ? (
                reviews.map((review) => (
                  <TableRow key={review.id}>
                    <TableCell
                      style={{
                        borderRight: "1px solid #ccc",
                        textAlign: "center",
                        width: "10%",
                      }}
                    >
                      {review.nickname}
                    </TableCell>
                    {/* 너비 조정 */}
                    <TableCell
                      style={{
                        borderRight: "1px solid #ccc",
                        textAlign: "center",
                        width: "5%",
                      }}
                    >
                      {review.rating} ⭐
                    </TableCell>
                    {/* 너비 조정 */}
                    <TableCell
                      style={{ borderRight: "1px solid #ccc" }}
                      className="truncate w-60"
                    >
                      {/* 너비 자동 조정 */}
                      {review.content}
                    </TableCell>
                    <TableCell style={{ textAlign: "center", width: "8%" }}>
                      {/* 너비 조정 */}
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

          {/* 페이지네이션 UI */}
          <div className="flex justify-center mt-4 gap-2">
            <Button onClick={handlePreviousPage} disabled={page === 1}>
              이전
            </Button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(
              (pageNum) => (
                <Button
                  key={pageNum}
                  onClick={() => setPage(pageNum)}
                  variant={pageNum === page ? "default" : "outline"} // 현재 페이지 강조
                >
                  {pageNum}
                </Button>
              )
            )}
            <Button onClick={handleNextPage} disabled={page === totalPages}>
              다음
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
