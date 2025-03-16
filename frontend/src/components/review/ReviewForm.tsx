"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";
import { Card, CardContent } from "@/components/ui/card";
import { fetchUserProfileClient } from "@/lib/api/user";

interface ReviewFormProps {
  contentType: string;
  contentId: string;
  onReviewAdded: (review: any) => void;
  allReviews: components["schemas"]["ReviewDto"][];
}

export default function ReviewForm({
  contentType,
  contentId,
  onReviewAdded,
  allReviews,
}: ReviewFormProps) {
  const router = useRouter();
  const [content, setContent] = useState("");
  // 평점 상태를 null 로 초기화, 선택되지 않았을 때 null 값 가짐
  const [rating, setRating] = useState<number | null>(null);
  // 호버된 별점 상태
  const [hoverRating, setHoverRating] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [existingReview, setExistingReview] = useState<any | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [userAccountId, setUserAccountId] = useState<number | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      const userData = await fetchUserProfileClient();
      if (userData && userData.id) {
        setUserAccountId(userData.id);
        setIsLoggedIn(true);
      } else {
        setUserAccountId(null);
        setIsLoggedIn(false);
      }
    };

    fetchUser();
  });

  useEffect(() => {
    if (allReviews && allReviews.length > 0 && userAccountId) {
      const userReview = allReviews.find(
        (review: components["schemas"]["ReviewDto"]) =>
          review.userAccountId === userAccountId,
      );
      if (userReview) {
        setExistingReview(userReview);
        setContent(userReview.content ?? "");
        setRating(userReview.rating ?? null);
      } else {
        setExistingReview(null);
        setContent("");
        setRating(null);
      }
    } else if (allReviews && allReviews.length > 0 && !userAccountId) {
      setExistingReview(null);
      setContent("");
      setRating(null);
    } else {
      setExistingReview(null);
      setContent("");
      setRating(null);
    }
  }, [allReviews, userAccountId]);

  const handleSubmit = async () => {
    if (!userAccountId) {
      alert("로그인 후 리뷰를 작성할 수 있습니다.");
      return;
    }
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

    const reviewData = {
      userAccountId: userAccountId,
      movieId: movieId,
      seriesId: seriesId,
      contentType: contentType,
      content,
      rating: rating,
    };

    // 디버깅용 콘솔 로그
    console.log("전송할 리뷰 데이터:", reviewData);

    try {
      const apiUrl = "http://localhost:8080/api/reviews";
      // 기존 리뷰가 있으면 수정 (PUT), 없으면 작성 (POST)
      let method = "POST";
      // 수정 시에는 리뷰 ID 포함
      let urlForSubmit = apiUrl;

      if (existingReview) {
        method = "PUT";
        urlForSubmit = `${apiUrl}/${existingReview.id}`;
      }

      // 요청 URL 로그
      console.log("리뷰 작성 요청 URL:", urlForSubmit);

      const response = await fetch(urlForSubmit, {
        method: method,
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(reviewData),
      });

      // 응답 확인
      console.log("서버 응답 상태:", response.status);

      if (!response.ok) {
        const errorData = await response.json();
        console.error("리뷰 작성/수정 실패:", errorData);
        let alertMessage = "서버 오류로 인해 리뷰 작성/수정을 실패했습니다.";
        if (errorData.message) {
          if (
            errorData.message.includes(
              "이미 해당 영화에 대한 리뷰를 작성하셨습니다.",
            ) ||
            errorData.message.includes(
              "이미 해당 드라마에 대한 리뷰를 작성하셨습니다.",
            )
          ) {
            alertMessage = errorData.message;
          } else {
            alertMessage = `리뷰 작성/수정 실패: ${errorData.message}`;
          }
        }
        alert(alertMessage);
        setLoading(false);
        return;
      }

      const newReview = await response.json();

      console.log("작성된 리뷰:", newReview);
      // 최신 리뷰 추가
      onReviewAdded(newReview);
      setExistingReview(newReview);
      setContent("");
      setRating(null);
      setLoading(false);
      setIsEditing(false);

      router.push(`/${contentType}/${contentId}`);
    } catch (error) {
      console.error("네트워크 오류:", error);
      alert("리뷰 작성/수정 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteReview = async () => {
    if (!existingReview?.id) {
      alert("삭제할 리뷰 정보가 없습니다.");
      return;
    }

    if (window.confirm("정말로 리뷰를 삭제하시겠습니까?")) {
      setLoading(true);
      try {
        const apiUrl = `http://localhost:8080/api/reviews/${existingReview.id}`;
        const response = await fetch(apiUrl, {
          method: "DELETE",
        });

        console.log("서버 응답 상태 (삭제):", response.status);

        if (response.ok) {
          alert("리뷰가 삭제되었습니다.");
          setExistingReview(null);
          setContent("");
          setRating(null);
          // 부모 컴포넌트에게 리뷰가 삭제되었음을 알립니다.
          onReviewAdded(null);
          router.push(`/${contentType}/${contentId}`);
        } else {
          const errorData = await response.json();
          console.error("리뷰 삭제 실패:", errorData);
          alert(
            `리뷰 삭제 실패: ${errorData.message || "서버 오류가 발생했습니다."}`,
          );
        }
      } catch (error) {
        console.error("네트워크 오류 (삭제):", error);
        alert("리뷰 삭제 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
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

  return (
    <div className="mb-6">
      {existingReview && !isEditing ? (
        <Card
          key={existingReview?.id}
          className="border border-gray-200 rounded-md mb-2"
        >
          <CardContent className="p-4">
            <div className="flex items-center justify-between mb-1">
              <p className="font-semibold">나의 리뷰</p>
              <div className="flex">
                {Array.from({ length: existingReview?.rating || 0 }, (_, i) => (
                  <span key={i} className="text-xl text-gold-500">
                    ⭐
                  </span>
                ))}
              </div>
            </div>
            <div className="flex justify-between items-center">
              <p className="mb-2">{existingReview?.content}</p>
              <div className="flex justify-end">
                {" "}
                {/* 여기에 justify-end 추가 */}
                <Button
                  variant="outline"
                  onClick={() => setIsEditing(true)}
                  className="mr-2"
                >
                  리뷰 수정
                </Button>
                <Button variant="destructive" onClick={handleDeleteReview}>
                  삭제
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      ) : !isLoggedIn ? (
        <div className="border p-4 rounded-md">
          <p className="mb-2">로그인한 유저만 리뷰를 남길 수 있습니다.</p>
          <div className="flex gap-2">
            <Button onClick={() => router.push("/login")}>로그인</Button>
            <Button variant="outline" onClick={() => router.push("/signup")}>
              회원가입
            </Button>
          </div>
        </div>
      ) : (
        <>
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
                }}
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
                handleResizeTextarea();
              }}
              className="mr-2 border p-2 rounded-md resize-none overflow-hidden focus:ring-blue-500 focus:border-blue-500 block w-full text-gray-900 placeholder-gray-500 flex-1 min-w-0"
              onKeyDown={(e) => {
                if (e.key === "Enter" && !loading) {
                  handleSubmit();
                  e.preventDefault;
                }
              }}
            />
            <Button onClick={handleSubmit} disabled={loading} className="h-10">
              {loading
                ? "등록 중..."
                : existingReview && isEditing
                  ? "리뷰 수정"
                  : "리뷰 작성"}
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
