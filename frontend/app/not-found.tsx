import Link from "next/link"
import { Button } from "@/components/ui/button"

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-background px-4">
      <div className="text-center">
        <h1 className="text-9xl font-bold text-primary">404</h1>
        <h2 className="mt-4 text-3xl font-semibold text-foreground">Không tìm thấy trang</h2>
        <p className="mt-2 text-muted-foreground">Trang bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <div className="mt-8">
          <Button asChild>
            <Link href="/">Về trang chủ</Link>
          </Button>
        </div>
      </div>
    </div>
  )
}
