"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";

interface ReviewFormProps {
  contentType: string;
  contentId: string;
  onReviewAdded: (review: any) => void;
}

export default function ReviewForm({
  contentType,
  contentId,
  onReviewAdded,
}: ReviewFormProps) {
  const router = useRouter();
  const [content, setContent] = useState("");
  // 평점 상태를 null 로 초기화, 선택되지 않았을 때 null 값 가짐
  const [rating, setRating] = useState<number | null>(null);
  // 호버된 별점 상태
  const [hoverRating, setHoverRating] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    setIsLoggedIn(
      !!(typeof window !== "undefined" && localStorage.getItem("token"))
    );
  });

  const handleSubmit = async () => {
    if (!content.trim()) return alert("리뷰를 입력해주세요.");
    if (rating === null) return alert("평점을 선택해주세요.");

    setLoading(true);

    let movieId = null;
    let seriesId = null;

    // contentType에 따라 movieId 또는 seriesId 설정
    if (contentType === "movies") {
      movieId = Number(contentId);
    } else if (contentType === "series") {
      seriesId = Number(contentId);
    }

    // 로컬 스토리지에서 토큰 가져오기
    const token = localStorage.getItem("token");
    // 로컬 스토리지에서 사용자 ID 가져오기
    const userId = localStorage.getItem("userId");

    const reviewData = {
      userAccountId: Number(userId),
      movieId: movieId,
      seriesId: seriesId,
      contentType: contentType,
      content,
      rating: rating,
    };

    // 디버깅용 콘솔 로그
    console.log("전송할 리뷰 데이터:", reviewData);

    try {
      const reviewApiUrl = "http://localhost:8080/api/reviews";

      // 요청 URL 로그
      console.log("리뷰 작성 요청 URL:", reviewApiUrl);

      const response = await fetch(reviewApiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(reviewData),
      });

      // 응답 확인
      console.log("서버 응답 상태:", response.status);

      if (!response.ok) {
        let errorMessage = "리뷰 작성에 실패했습니다.";

        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch (e) {
          console.error("에러 응답 파싱 실패함:", e);
        }

        // 로그
        alert(errorMessage);
        setLoading(false);
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

  const handleResizeTextarea = () => {
    // textarea 높이 자동 조절 함수
    if (textareaRef.current) {
      // textareaRef.current 가 null 이 아닌지 확인
      // 높이를 auto 로 설정하여 내용에 맞게 자동 조절
      textareaRef.current.style.height = "auto";
      // scrollHeight 를 이용하여 실제 필요한 높이로 설정
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }
  };

  // 로그인되지 않은 경우 로그인 안내 메시지 표시
  if (!isLoggedIn) {
    return (
      <div className="mb-6">
        <p>리뷰를 작성하려면 로그인이 필요합니다.</p>
        <Button onClick={() => router.push("/login")}>로그인</Button>
      </div>
    );
  }

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
        <textarea
          ref={textareaRef}
          placeholder="리뷰를 입력하세요"
          value={content}
          onChange={(e) => {
            setContent(e.target.value);
            // 내용 변경 시 textarea 높이 자동 조절
            handleResizeTextarea();
          }}
          className="mr-2 border p-2 rounded-md resize-none overflow-hidden focus:ring-blue-500 focus:border-blue-500 block w-full text-gray-900 placeholder-gray-500 flex-1 min-w-0"
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
