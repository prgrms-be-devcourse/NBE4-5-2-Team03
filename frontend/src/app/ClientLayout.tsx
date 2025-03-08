"use client";

import Link from "next/link";

export default function ClientLayout({
  children,
  fontVariable,
  fontClassName,
}: Readonly<{
  children: React.ReactNode;
  fontVariable: string;
  fontClassName: string;
}>) {
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
