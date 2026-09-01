import Link from "next/link"
import { Button } from "@/components/ui/button"
import type { LucideIcon } from "lucide-react"

export function AuthRequiredGate({
  icon: Icon,
  title,
  description,
}: {
  icon: LucideIcon
  title: string
  description: string
}) {
  return (
    <div className="container mx-auto px-4 py-16">
      <div className="mx-auto max-w-md text-center">
        <Icon className="mx-auto h-16 w-16 text-muted-foreground" />
        <h2 className="mt-4 text-2xl font-bold text-foreground">{title}</h2>
        <p className="mt-2 text-muted-foreground">{description}</p>
        <div className="mt-6 flex justify-center gap-3">
          <Button asChild>
            <Link href="/login">Đăng nhập</Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href="/register">Đăng ký</Link>
          </Button>
        </div>
      </div>
    </div>
  )
}
