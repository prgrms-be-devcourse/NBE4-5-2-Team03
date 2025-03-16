"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { fetchUserProfileClient } from "@/lib/api/user"; // 로그인된 유저 정보 가져오기
import client from "@/lib/backend/client"; // API 클라이언트
import { Button } from "@/components/ui/button";

type FavoriteButtonProps = {
  contentId: number;
  contentType: "MOVIE" | "SERIES"; // 콘텐츠 타입
};

export default function FavoriteButton({
  contentId,
  contentType,
}: FavoriteButtonProps) {
  const [user, setUser] = useState<{ id: number } | null>(null);
  const [isFavorite, setIsFavorite] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const loadUser = async () => {
      const userData = await fetchUserProfileClient();
      if (userData) {
        setUser(userData);
        checkFavorite(userData.id);
      }
    };

    loadUser();
  }, [contentId]);

  // 즐겨찾기 여부 확인 API 호출
  const checkFavorite = async (userId: number) => {
    try {
      const response = await client.GET("/api/favorites/{userId}", {
        params: { path: { userId } },
      });

      if (response.data) {
        // 즐겨찾기 목록에 현재 콘텐츠 ID가 있는지 확인
        const isFav = response.data.data.items.some(
          (item: any) =>
            item.contentId === contentId && item.contentType === contentType,
        );
        setIsFavorite(isFav);
      }
    } catch (error) {
      // console.error("즐겨찾기 목록 확인 실패:", error);
    }
  };

  // 즐겨찾기 추가 API 호출
  const handleFavorite = async () => {
    if (!user) {
      if (confirm("로그인이 필요합니다. 로그인하시겠습니까?")) {
        router.push("/login");
      }
      return;
    }

    try {
      const response = await client.POST("/api/favorites", {
        body: {
          userId: user.id,
          contentId,
          contentType,
        },
      });

      if (response && response.data) {
        setIsFavorite(true);
        alert("즐겨찾기에 추가되었습니다.");
      }
    } catch (error) {
      console.error("즐겨찾기 추가 실패:", error);
      alert("즐겨찾기 추가 중 오류가 발생했습니다.");
    }
  };

  return (
    <Button
      onClick={handleFavorite}
      variant={isFavorite ? "secondary" : "default"}
    >
      {isFavorite ? "⭐ 즐겨찾기 추가됨" : "☆ 즐겨찾기 추가"}
    </Button>
  );
}
