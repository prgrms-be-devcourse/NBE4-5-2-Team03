"use client";

import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { components } from "@/lib/backend/apiV1/schema";
import { Button } from "@/components/ui/button";
import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react";
import ReviewForm from "./ReviewForm";

interface ClientPageProps {
  contentType: string;
  contentId: string;
}

export default function ClientPage({
  contentType,
  contentId,
}: ClientPageProps) {
  const [reviews, setReviews] = useState<components["schemas"]["ReviewDto"][]>(
    []
  );

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [pageGroupStart, setPageGroupStart] = useState(0);
  const [averageRating, setAverageRating] = useState<number>(0);
  const [totalReviewsCount, setTotalReviewsCount] = useState<number>(0);

  useEffect(() => {
    fetchReviews(currentPage);
    fetchAverageRating();
    fetchTotalReviewsCount();
  }, [currentPage, contentType, contentId]);

  // 리뷰 목록을 불러오는 함수
  const fetchReviews = async (page: number) => {
    try {
      const baseUrl = "http://localhost:8080/api/reviews";
      let apiUrl = "";

      if (contentType === "movies") {
        // "/movies" 로 URL 시작
        apiUrl = `${baseUrl}/movies/${contentId}?page=${page}`;
      } else if (contentType === "series") {
        // "/series" 로 URL 시작
        apiUrl = `${baseUrl}/series/${contentId}?page=${page}`;
      } else {
        console.error("잘못된 contentType:", contentType);
        return;
      }

      // 요청 URL 로그
      console.log("fetchReviews 요청 URL:", apiUrl);

      const response = await fetch(apiUrl);

      if (!response.ok) {
        throw new Error("리뷰 목록을 불러오는 데 실패했습니다.");
      }

      const newData: components["schemas"]["PageDtoReviewDto"] =
        await response.json();

      setReviews(newData.items);

      setTotalPages(newData.totalPages);
    } catch (error) {
      console.error("리뷰 목록 불러오기 오류:", error);
    }
  };

  // 평균 평점을 가져오는 함수
  const fetchAverageRating = async () => {
    try {
      const baseUrl = "http://localhost:8080/api/reviews";
      let apiUrl = "";

      if (contentType === "movies") {
        // "/movies" 로 URL 시작
        apiUrl = `${baseUrl}/movies/${contentId}/average-rating`;
      } else if (contentType === "series") {
        // "/series" 로 URL 시작
        apiUrl = `${baseUrl}/series/${contentId}/average-rating`;
      } else {
        console.error("잘못된 contentType:", contentType);
        return;
      }

      // 요청 URL 로그
      console.log("fetchAverageRating 요청 URL:", apiUrl);

      const response = await fetch(apiUrl);
      if (!response.ok) {
        throw new Error("평균 평점을 불러오는 데 실패했습니다.");
      }

      // 응답 데이터를 숫자로 변환
      const averageRatingData: number = await response.json();

      // 평균 평점 상태 업데이트
      setAverageRating(averageRatingData);
    } catch (error) {
      console.error("평균 평점 불러오기 오류:", error);
      setAverageRating(0); // 에러 발생 시 평균 평점 0으로 처리 (UI 깨짐 방지)
    }
  };

  // 총 리뷰 개수를 불러오는 함수
  const fetchTotalReviewsCount = async () => {
    try {
      const baseUrl = "http://localhost:8080/api/reviews";
      let apiUrl = "";

      if (contentType === "movies") {
        // "/movies" 로 URL 시작
        apiUrl = `${baseUrl}/movies/${contentId}/count`;
      } else if (contentType === "series") {
        // "/series" 로 URL 시작
        apiUrl = `${baseUrl}/series/${contentId}/count`;
      } else {
        console.error("잘못된 contentType:", contentType);
        return;
      }

      // 요청 URL 로그
      console.log("fetchTotalReviewsCount 요청 URL:", apiUrl);

      const response = await fetch(apiUrl);

      if (!response.ok) {
        throw new Error("총 리뷰 개수를 불러오는 데 실패했습니다.");
      }

      const totalCount: number = await response.json();

      // 총 리뷰 개수 상태 업데이트
      setTotalReviewsCount(totalCount);
    } catch (error) {
      console.error("총 리뷰 개수 불러오기 오류:", error);
      setTotalReviewsCount(0); // 에러 발생 시 0으로 처리 (UI 깨짐 방지)
    }
  };

  const handleReviewAdded = (newReview: components["schemas"]["ReviewDto"]) => {
    // 리뷰 추가 후 현재 페이지 리뷰 목록 갱신 (페이지 번호 유지)
    fetchReviews(currentPage);
    fetchAverageRating();
  };

  const handlePageChange = (page: number) => {
    // 페이지 번호 0부터 시작이므로 그대로 유지
    setCurrentPage(page);
  };

  const handlePrevGroup = () => {
    setPageGroupStart((prev) => Math.max(0, prev - 10));
    setCurrentPage(Math.max(0, pageGroupStart - 10));
  };

  const handleNextGroup = () => {
    setPageGroupStart((prev) => prev + 10);
    setCurrentPage(pageGroupStart + 10);
  };

  const pageNumbers = [];

  for (let i = pageGroupStart; i < pageGroupStart + 10; i++) {
    if (i >= totalPages) break;
    pageNumbers.push(i);
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">리뷰</h1>
      <ReviewForm
        contentType={contentType}
        contentId={contentId}
        onReviewAdded={handleReviewAdded}
      />

      {/* --- [평균 평점 표시 UI] --- */}
      <div className="flex items-center mb-4">
        <p className="mr-2 font-semibold">평균 평점:</p>
        <span className="text-xl font-bold mr-1">
          {averageRating.toFixed(1)}
        </span>{" "}
        {/* 평균 평점 값 표시, 소수점 1자리까지 */}
        {Array.from(
          { length: Math.round(averageRating) },
          (
            _,
            i // 평균 평점 기준으로 별 아이콘 표시
          ) => (
            <span key={i} className="text-xl text-gold-500">
              ⭐
            </span>
          )
        )}
        {/* 리뷰 개수 표시 */}
        <span className="text-gray-500 ml-1">
          ({totalReviewsCount}개의 리뷰)
        </span>{" "}
      </div>
      <Card className="border border-gray-200 rounded-md">
        <CardContent className="p-6">
          <div className="space-y-4">
            {reviews.map((review) => (
              <Card key={review?.id} className="border border-gray-200">
                <CardContent>
                  <div className="flex items-center justify-between">
                    <p className="font-semibold">{review?.nickname}</p>

                    <div className="flex">
                      {Array.from({ length: review?.rating || 0 }, (_, i) => (
                        <span key={i}>⭐</span>
                      ))}
                    </div>
                  </div>

                  <p className="mt-2">{review?.content}</p>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="flex justify-center mt-4">
            <Button
              onClick={handlePrevGroup}
              disabled={pageGroupStart === 0}
              variant="outline"
            >
              <ChevronLeftIcon className="h-5 w-5" />
            </Button>

            {pageNumbers.map((page) => (
              <Button
                key={page}
                variant={currentPage === page ? "default" : "outline"}
                onClick={() => handlePageChange(page)}
                disabled={currentPage === page}
              >
                {page + 1}
              </Button>
            ))}

            <Button
              onClick={handleNextGroup}
              disabled={pageGroupStart + 9 >= totalPages}
              variant="outline"
            >
              <ChevronRightIcon className="h-5 w-5" />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
