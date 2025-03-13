"use client";

import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";
import Link from "next/link";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { ChevronLeft, ChevronRight } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export default function MainPage() {
  const router = useRouter();
  const sortBy = "rating";
  const [searchQuery, setSearchQuery] = useState("");
  const [searchType, setSearchType] = useState("movies");
  const [tab, setTab] = useState("movies");
  const [movies, setMovies] =
    useState<components["schemas"]["PageDtoMovieResponse"]>();
  const [series, setSeries] =
    useState<components["schemas"]["PageDtoSeriesSummaryResponse"]>();
  const [loading, setLoading] = useState(true);
  const scrollRef = useRef<HTMLDivElement>(null);
  const itemWidth = useRef(0); // 아이템의 너비를 저장

  useEffect(() => {
    const fetchData = async () => {
      try {
        const movieResponse = await client.GET("/api/movies", {
          params: {
            query: {
              sortBy,
            },
          },
        });
        setMovies(movieResponse.data?.data);
        const seriesResponse = await client.GET("/api/series", {
          params: {
            query: {
              sortBy,
            },
          },
        });
        setSeries(seriesResponse.data?.data);
      } catch (error) {
        console.error("데이터를 불러오는 중 오류 발생:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    // 스크롤에 대한 아이템 크기 계산
    if (scrollRef.current && scrollRef.current.firstChild) {
      const firstItem = scrollRef.current.firstChild as HTMLElement;
      itemWidth.current = firstItem.offsetWidth + 16; // 간격 추가
    }
  }, [movies, series]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    router.push(
      `/${searchType === "movies" ? "movies" : "series"}?keyword=${encodeURIComponent(searchQuery)}`
    );
  };

  const scrollLeft = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({
        left: -itemWidth.current,
        behavior: "smooth",
      });
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({
        left: itemWidth.current,
        behavior: "smooth",
      });
    }
  };

  return (
    <div className="w-full min-h-[50vh] flex flex-col items-center px-4 mt-24">
      <h2 className="text-2xl font-semibold mb-6 text-center">
        영화/시리즈 정보를 검색하고 리뷰를 남겨보세요
      </h2>
      <form
        onSubmit={handleSearch}
        className="flex gap-3 bg-white p-5 shadow-md rounded-lg max-w-2xl w-full"
      >
        <Select value={searchType} onValueChange={setSearchType}>
          <SelectTrigger className="w-32">
            <SelectValue placeholder="검색 타입" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="movies">영화</SelectItem>
            <SelectItem value="series">시리즈</SelectItem>
          </SelectContent>
        </Select>
        <Input
          type="text"
          placeholder="검색어를 입력하세요"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full border-gray-300 shadow-sm"
        />
        <Button type="submit" className="bg-black text-white">
          검색
        </Button>
      </form>
      <div className="flex gap-4 mt-6">
        <Button
          variant={tab === "movies" ? "default" : "outline"}
          onClick={() => setTab("movies")}
        >
          영화
        </Button>
        <Button
          variant={tab === "series" ? "default" : "outline"}
          onClick={() => setTab("series")}
        >
          시리즈
        </Button>
      </div>
      {loading ? (
        <p className="mt-6">데이터를 불러오는 중...</p>
      ) : (
        <div className="relative w-full max-w-6xl mt-6">
          <h3 className="text-xl font-bold mb-4">🎬 평점 TOP 10</h3>

          {/* 왼쪽 스크롤 버튼 */}
          <button
            onClick={scrollLeft}
            className="absolute left-0 top-1/2 transform -translate-y-1/2 
               z-10 bg-white shadow-md p-2 rounded-full opacity-80 
               hover:opacity-100 transition-opacity"
            style={{ visibility: "visible" }}
          >
            <ChevronLeft size={24} />
          </button>

          {/* 영화/시리즈 목록 */}
          <div
            ref={scrollRef}
            className="overflow-hidden whitespace-nowrap flex gap-4 px-10 scrollbar-hide"
          >
            {(tab === "movies" ? movies?.items : series?.items)?.map((item) => (
              <Link
                key={item.id}
                href={`/${tab}/${item.id}`}
                className="flex-none w-40"
              >
                <Card className="shadow-lg hover:shadow-xl transition-transform transform hover:scale-105">
                  <CardHeader className="p-0">
                    <img
                      src={item.posterPath || "/no-image.png"}
                      alt={item.title}
                      className="w-full h-60 object-cover"
                    />
                  </CardHeader>
                  <CardContent className="p-3 bg-white">
                    <h2
                      className="text-sm font-semibold truncate"
                      title={item.title}
                    >
                      {item.title}
                    </h2>
                    <div className="flex items-center gap-2 mt-2">
                      <span className="text-yellow-500 font-bold">
                        ⭐ {item.averageRating?.toFixed(2)}
                      </span>
                      <span className="text-xs text-gray-500">
                        ({item.ratingCount} 리뷰)
                      </span>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>

          {/* 오른쪽 스크롤 버튼 */}
          <button
            onClick={scrollRight}
            className="absolute right-0 top-1/2 transform -translate-y-1/2 
               z-10 bg-white shadow-md p-2 rounded-full opacity-80 
               hover:opacity-100 transition-opacity"
            style={{ visibility: "visible" }}
          >
            <ChevronRight size={24} />
          </button>
        </div>
      )}
    </div>
  );
}
