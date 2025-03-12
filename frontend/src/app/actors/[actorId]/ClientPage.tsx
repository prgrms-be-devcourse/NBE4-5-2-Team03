"use client";

import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";

interface Movie {
  id: number;
  title: string;
  posterPath: string;
  releaseDate: string;
}

interface Actor {
  id: number;
  name: string;
  profilePath: string;
  movies: Movie[];
}

export default function ActorDetailPage({ actor }: { actor: Actor }) {
  const router = useRouter();

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6 text-center">🎭 배우 정보</h1>

      {/* 배우 정보 */}
      <div className="flex flex-col md:flex-row items-center space-x-6 bg-white shadow-md p-6 rounded-lg">
        <img
          src={actor.profilePath || "/default-profile.png"}
          alt={actor.name}
          width={150}
          height={150}
          className="rounded-lg"
        />
        <h1 className="text-2xl font-bold">{actor.name}</h1>
      </div>

      {/* 출연 영화 리스트 */}
      <div className="mt-8">
        <h2 className="text-xl font-semibold mb-4">🎬 출연한 영화</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {actor.movies.length > 0 ? (
            actor.movies.map((movie) => (
              <div
                key={movie.id}
                className="bg-white shadow-md p-4 rounded-lg cursor-pointer hover:bg-gray-50 transition"
                onClick={() => router.push(`/movies/${movie.id}`)}
              >
                <img
                  src={movie.posterPath || "/default-movie.png"}
                  alt={movie.title}
                  width={200}
                  height={300}
                  className="rounded-md"
                />
                <h3 className="text-lg font-medium mt-2">{movie.title}</h3>
                <p className="text-gray-500">{movie.releaseDate}</p>
              </div>
            ))
          ) : (
            <p className="text-gray-500">출연한 영화가 없습니다.</p>
          )}
        </div>
      </div>

      {/* 돌아가기 버튼 */}
      <div className="flex justify-center mt-8">
        <Button variant="outline" onClick={() => router.back()}>
          🔙 돌아가기
        </Button>
      </div>
    </div>
  );
}
