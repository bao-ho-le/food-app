import type React from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Logo } from "@/components/logo"
import { UtensilsCrossed, ShoppingBag, Star, Clock } from "lucide-react"

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <Logo />
          <nav className="flex items-center gap-4">
            <Button variant="ghost" asChild>
              <Link href="/login">Đăng nhập</Link>
            </Button>
            <Button asChild>
              <Link href="/register">Đăng ký</Link>
            </Button>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-b from-secondary/30 to-background py-20">
        <div className="container mx-auto px-4">
          <div className="mx-auto max-w-3xl text-center">
            <h1 className="text-balance text-5xl font-bold leading-tight text-foreground md:text-6xl">
              Đặt đồ ăn ngon, giao tận nơi
            </h1>
            <p className="mt-6 text-pretty text-lg text-muted-foreground">
              Khám phá hàng ngàn món ăn từ các nhà hàng yêu thích. Giao hàng nhanh chóng, thanh toán tiện lợi.
            </p>
            <div className="mt-8 flex flex-col gap-4 sm:flex-row sm:justify-center">
              <Button size="lg" asChild>
                <Link href="/register">Bắt đầu đặt món</Link>
              </Button>
              <Button size="lg" variant="outline" asChild>
                <Link href="/user/food">Xem thực đơn</Link>
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-20">
        <div className="container mx-auto px-4">
          <h2 className="text-center text-3xl font-bold text-foreground">Tại sao chọn chúng tôi?</h2>
          <div className="mt-12 grid gap-8 md:grid-cols-2 lg:grid-cols-4">
            <FeatureCard
              icon={<UtensilsCrossed className="h-8 w-8" />}
              title="Đa dạng món ăn"
              description="Hàng ngàn món ăn từ các nhà hàng uy tín"
            />
            <FeatureCard
              icon={<Clock className="h-8 w-8" />}
              title="Giao hàng nhanh"
              description="Giao hàng trong 30-45 phút"
            />
            <FeatureCard
              icon={<Star className="h-8 w-8" />}
              title="Chất lượng đảm bảo"
              description="Đánh giá từ khách hàng thực tế"
            />
            <FeatureCard
              icon={<ShoppingBag className="h-8 w-8" />}
              title="Đặt hàng dễ dàng"
              description="Giao diện đơn giản, thanh toán tiện lợi"
            />
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-primary py-20 text-primary-foreground">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold">Sẵn sàng đặt món?</h2>
          <p className="mt-4 text-lg opacity-90">Đăng ký ngay để nhận ưu đãi cho đơn hàng đầu tiên</p>
          <Button size="lg" variant="secondary" className="mt-8" asChild>
            <Link href="/register">Đăng ký miễn phí</Link>
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border bg-card py-8">
        <div className="container mx-auto px-4 text-center text-sm text-muted-foreground">
          <p>&copy; 2025 FoodOrder. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}

function FeatureCard({ icon, title, description }: { icon: React.ReactNode; title: string; description: string }) {
  return (
    <div className="flex flex-col items-center rounded-lg border border-border bg-card p-6 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-primary/10 text-primary">{icon}</div>
      <h3 className="mt-4 text-xl font-semibold text-card-foreground">{title}</h3>
      <p className="mt-2 text-muted-foreground">{description}</p>
    </div>
  )
}
