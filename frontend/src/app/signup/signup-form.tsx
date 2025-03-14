"use client";

import client from "@/lib/backend/client";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export function SignupForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  async function register(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = e.target as HTMLFormElement;
    const data = {
      username: form.username.value,
      password: form.password.value,
      email: form.email.value,
      nickname: form.nickname.value,
      role: "USER",
    };
    const response = await client.POST("/api/users/register", {
      body: data,
      credentials: "include",
    });
    if (response.error) {
      alert(response["error"]["msg"]);
      return;
    }
    // show success message and redirect to login page
    alert(response.data);
    window.location.href = "/login";
  }

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card>
        <CardHeader>
          <CardTitle>회원가입</CardTitle>
          <CardDescription>
            회원가입을 위해 가입 정보를 입력해주세요.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={register}>
            <div className="flex flex-col gap-6">
              <div className="grid gap-3">
                <Label htmlFor="username">유저 ID</Label>
                <Input
                  id="username"
                  type="username"
                  placeholder="유저 ID"
                  required
                />
              </div>
              <div className="grid gap-3">
                <div className="flex items-center">
                  <Label htmlFor="password">비밀번호</Label>
                </div>
                <Input
                  id="password"
                  type="password"
                  placeholder="비밀번호"
                  required
                />
              </div>
              <div className="grid gap-3">
                <Label htmlFor="email">이메일</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="email@flicktionary.com"
                  required
                />
              </div>
              <div className="grid gap-3">
                <Label htmlFor="nickname">닉네임</Label>
                <Input
                  id="nickname"
                  type="nickname"
                  placeholder="닉네임"
                  required
                />
              </div>
              <div className="flex flex-col gap-3">
                <Button type="submit" className="w-full">
                  가입하기
                </Button>
              </div>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
