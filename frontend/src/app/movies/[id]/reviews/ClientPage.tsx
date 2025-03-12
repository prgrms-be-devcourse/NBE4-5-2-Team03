"use client";

import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { components } from "@/lib/backend/apiV1/schema";
import ReviewForm from "./ReviewForm";
import { Button } from "@/components/ui/button";
import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react";

export default function ClientPage({ movieId }: { movieId: number }) {
  const [reviews, setReviews] = useState<components["schemas"]["ReviewDto"][]>(
    []
  );
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [pageGroupStart, setPageGroupStart] = useState(0);

  useEffect(() => {
    fetchReviews(currentPage);
  }, [currentPage, movieId]);

  const fetchReviews = async (page: number) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/reviews/movie/${movieId}?page=${page}`
      );
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

  const handleReviewAdded = (newReview: components["schemas"]["ReviewDto"]) => {
    // 리뷰 추가 후 현재 페이지 리뷰 목록 갱신 (페이지 번호 유지)
    fetchReviews(currentPage);
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
      <h1 className="text-2xl font-bold mb-4">영화 리뷰</h1>

      <ReviewForm movieId={movieId} onReviewAdded={handleReviewAdded} />

      <Card className="border border-gray-200 rounded-md">
        <CardContent className="p-6">
          <div className="space-y-4">
            {reviews.map((review) => (
              <Card key={review?.id} className="border border-gray-200">
                <CardContent>
                  <div className="flex items-center justify-between">
                    <p className="font-semibold">
                      {review?.nickname || "익명"}
                    </p>
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
