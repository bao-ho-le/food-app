"use client";

import type React from "react";
import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { Logo } from "@/components/logo";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
// import { authService } from "@/lib/auth";
import useLogin from "@/hooks/authService/use-login";
import { fetchUserProfile } from "@/services/users";

import { useCart } from "@/hooks/use-cart";
import {
  UtensilsCrossed,
  ShoppingCart,
  User,
  Package,
  LogOut,
  Menu,
  X,
  ShieldCheck,
} from "lucide-react";

export default function UserLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const { getTotalItems } = useCart();

  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { logout } = useLogin();
  const [displayName, setDisplayName] = useState("Người dùng");
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const storedUser =
      localStorage.getItem("food_ordering_user") ?? localStorage.getItem("user");
    if (storedUser) {
      try {
        const parsed = JSON.parse(storedUser);
        setDisplayName(parsed?.name ?? parsed?.email ?? "Người dùng");
      } catch (error) {
        console.error("Không thể đọc thông tin người dùng từ localStorage", error);
      }
    }

    const token = localStorage.getItem("token") ?? undefined;
    fetchUserProfile({ token })
      .then((profile) => {
        setDisplayName(profile.fullName || profile.email || "Người dùng");
        setIsAdmin(profile.roleName === "ADMIN");
      })
      .catch((error) => {
        console.error("Không thể tải thông tin người dùng", error);
      });
  }, []);

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      if (typeof window !== "undefined") {
        localStorage.removeItem("food_ordering_user");
        localStorage.removeItem("user");
      }
      router.replace("/login");
    }
  };

  const cartItemCount = getTotalItems();

  const navItems = [
    { href: "/user/food", label: "Món ăn", icon: UtensilsCrossed },
    { href: "/user/orders", label: "Đơn hàng", icon: Package },
  ];

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 border-b border-border bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-8">
            <Logo />
            <nav className="hidden items-center gap-6 md:flex">
              {navItems.map((item) => {
                const Icon = item.icon;
                const isActive = pathname.startsWith(item.href);
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`flex items-center gap-2 text-sm font-medium transition-colors hover:text-primary ${
                      isActive ? "text-primary" : "text-muted-foreground"
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    {item.label}
                  </Link>
                );
              })}
            </nav>
          </div>

          <div className="flex items-center gap-2">
            <Button variant="ghost" size="icon" className="relative" asChild>
              <Link href="/user/cart">
                <ShoppingCart className="h-5 w-5" />
                {/* {cartItemCount > 0 && (
                  <Badge className="absolute -right-1 -top-1 h-5 w-5 rounded-full p-0 text-xs" variant="destructive">
                    {cartItemCount}
                  </Badge>
                )} */}
              </Link>
            </Button>

            <div className="hidden md:flex">
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="gap-2">
                    <User className="h-4 w-4" />
                    <span className="max-w-[160px] truncate text-sm font-medium">
                      {displayName}
                    </span>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-48">
                  <DropdownMenuItem asChild>
                    <Link href="/user/profile">
                      <User className="h-4 w-4" />
                      Hồ sơ
                    </Link>
                  </DropdownMenuItem>
                  {isAdmin && (
                    <DropdownMenuItem asChild>
                      <Link href="/admin/dashboard">
                        <ShieldCheck className="h-4 w-4" />
                        Trang quản trị
                      </Link>
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="h-4 w-4" />
                    Đăng xuất
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            <Button
              variant="ghost"
              size="icon"
              className="md:hidden"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              {mobileMenuOpen ? (
                <X className="h-5 w-5" />
              ) : (
                <Menu className="h-5 w-5" />
              )}
            </Button>
          </div>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="border-t border-border bg-card md:hidden">
            <nav className="container mx-auto flex flex-col px-4 py-4">
              {navItems.map((item) => {
                const Icon = item.icon;
                const isActive = pathname.startsWith(item.href);
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    onClick={() => setMobileMenuOpen(false)}
                    className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors hover:bg-accent ${
                      isActive
                        ? "bg-accent text-primary"
                        : "text-muted-foreground"
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    {item.label}
                  </Link>
                );
              })}
              <div className="mt-4 flex flex-col gap-1 border-t border-border pt-4">
                <span className="px-3 pb-1 text-sm font-medium truncate">
                  {displayName}
                </span>
                <Link
                  href="/user/profile"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent"
                >
                  <User className="h-4 w-4" />
                  Hồ sơ
                </Link>
                {isAdmin && (
                  <Link
                    href="/admin/dashboard"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent"
                  >
                    <ShieldCheck className="h-4 w-4" />
                    Trang quản trị
                  </Link>
                )}
                <Button variant="ghost" size="sm" className="justify-start" onClick={handleLogout}>
                  <LogOut className="mr-2 h-4 w-4" />
                  Đăng xuất
                </Button>
              </div>
            </nav>
          </div>
        )}
      </header>

      {/* Main Content */}
      <main>{children}</main>
    </div>
  );
}
