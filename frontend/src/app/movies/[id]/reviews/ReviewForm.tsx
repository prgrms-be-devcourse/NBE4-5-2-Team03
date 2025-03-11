"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export default function ReviewForm({
  movieId,
  onReviewAdded,
}: {
  movieId: number;
  onReviewAdded: (review: any) => void;
}) {
  const router = useRouter();
  const [content, setContent] = useState("");
  // 평점 상태를 null 로 초기화, 선택되지 않았을 때 null 값 가짐
  const [rating, setRating] = useState<number | null>(null);
  // 호버된 별점 상태
  const [hoverRating, setHoverRating] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!content.trim()) return alert("리뷰를 입력해주세요.");
    if (rating === null) return alert("평점을 선택해주세요.");

    setLoading(true);

    /* 나중에 로그인 구현되면, 이 곳에 userAccountId를 받아 로그인 하는 기능 구현할 것 */

    const reviewData = {
      userAccountId: 1, // 임시로 1로 설정
      movieId: Number(movieId),
      content,
      rating: rating,
    };

    // 디버깅용 콘솔 로그
    console.log("전송할 리뷰 데이터:", reviewData);

    try {
      const response = await fetch("http://localhost:8080/api/reviews", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(reviewData),
      });

      // 응답 확인
      console.log("서버 응답 상태:", response.status);

      if (!response.ok) {
        alert("리뷰 작성에 실패했습니다.");
        setLoading(false);

        // 로그
        const errorData = await response.json();
        console.error("리뷰 작성 실패:", errorData);
        alert(`리뷰 작성 실패: ${errorData.message || response.statusText}`);
        return;
      }

      const newReview = await response.json();

      console.log("작성된 리뷰:", newReview);

      // 최신 리뷰 추가
      onReviewAdded(newReview);
      setContent("");
      setRating(null);
      setLoading(false);
    } catch (error) {
      console.error("네트워크 오류:", error);
      alert("리뷰 작성 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mb-6">
      {/* 별점 선택 UI*/}
      <div className="flex items-center mb-2">
        <p className="mr-2 font-semibold">평점:</p>
        {[1, 2, 3, 4, 5].map((starRating) => (
          <span
            key={starRating}
            className="text-2xl cursor-pointer"
            style={{
              color:
                starRating <= (hoverRating || rating || 0)
                  ? "gold"
                  : "lightgray",
            }} // 조건부 색상 변경: 선택/호버 시 "까만색", 아니면 "lightgray"
            onClick={() => setRating(starRating)}
            onMouseEnter={() => setHoverRating(starRating)}
            onMouseLeave={() => setHoverRating(null)}
          >
            ★
          </span>
        ))}
      </div>

      {/* --- [리뷰 입력 칸 & 버튼 UI] --- */}
      <div className="flex">
        <Input
          type="text"
          placeholder="리뷰를 입력하세요"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          className="mr-2 h-10"
          onKeyDown={(e) => {
            // 엔터 키 감지 및 로딩 상태 확인
            if (e.key === "Enter" && !loading) {
              // handleSubmit 함수 호출
              handleSubmit();
              // 기본 엔터 키 동작 (새 줄 추가) 방지
              e.preventDefault;
            }
          }}
        />
        <Button onClick={handleSubmit} disabled={loading} className="h-10">
          {loading ? "등록 중..." : "리뷰 작성"}
        </Button>
      </div>
    </div>
  );
}
