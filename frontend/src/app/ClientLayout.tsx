"use client";

import Link from "next/link";
import { useState } from "react";

export default function ClientLayout({
  children,
  fontVariable,
  fontClassName,
}: Readonly<{
  children: React.ReactNode;
  fontVariable: string;
  fontClassName: string;
}>) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  // 배우나 감독을 눌러서 이동하면 드롭다운이 닫힌다.
  const handleLinkClick = () => {
    setIsDropdownOpen(false);
  };

  return (
    <html lang="ko" className={`${fontVariable}`}>
      <body className={`min-h-[100dvh] flex flex-col ${fontClassName}`}>
        {/* 헤더 */}
        <header className="flex justify-between items-center px-6 py-4 bg-gray-800 text-white">
          <div className="flex items-center gap-20">
            <h1 className="text-2xl font-bold">
              <Link href="/">🎬 Flicktionary</Link>
            </h1>
            <nav className="flex gap-8">
              <Link href="/movies" className="hover:text-gray-300">
                영화
              </Link>
              <Link href="/series" className="hover:text-gray-300">
                시리즈
              </Link>
              {/* Dropdown for 인물 */}
              <div className="relative">
                <button
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                  className="hover:text-gray-300"
                >
                  인물
                </button>
                {isDropdownOpen && (
                  <div className="absolute bg-gray-800 text-white w-32 mt-2 rounded shadow-lg">
                    <Link
                      href="/actors"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleLinkClick}
                    >
                      배우
                    </Link>
                    <Link
                      href="/directors"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleLinkClick}
                    >
                      감독
                    </Link>
                  </div>
                )}
              </div>
            </nav>
          </div>

          <nav className="flex gap-4">
            <Link href="/login" className="hover:text-gray-300">
              로그인
            </Link>
            <Link href="/signup" className="hover:text-gray-300">
              회원 가입
            </Link>
          </nav>
        </header>

        {/* 메인 콘텐츠 */}
        <div className="flex-grow">{children}</div>

        {/* 푸터 */}
        <footer className="py-4 text-center bg-gray-800 text-white">
          © {new Date().getFullYear()} Flicktionary. All rights reserved.
        </footer>
      </body>
    </html>
  );
}
