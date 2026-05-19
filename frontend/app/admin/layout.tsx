"use client";

import type React from "react";
import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { Logo } from "@/components/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
// import { authService } from "@/lib/auth";
import {
  LayoutDashboard,
  Users,
  UtensilsCrossed,
  Package,
  BarChart3,
  LogOut,
  Menu,
  Settings,
  Bell,
  Search,
  Home,
} from "lucide-react";
import { User } from "@/types";

const createAdminUserFromStorage = (data: any): User => {
  return {
    id: data?.id ? String(data.id) : `admin_${Date.now()}`,
    name: data?.name ?? "Admin",
    email: data?.email ?? "",
    phone: data?.phone ?? "",
    gender: (data?.gender as User["gender"]) ?? "other",
    role: "admin",
    birthdate: data?.birthdate ?? "",
    avatarUrl: data?.avatarUrl,
    roleName: "ADMIN",
    createdAt: data?.createdAt ?? new Date().toISOString(),
    isActive: data?.isActive ?? true,
    bias: Array.isArray(data?.bias) ? data.bias : [],
    address: Array.isArray(data?.address) ? data.address : [],
  };
};

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [user, setUser] = useState<User | null>(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") return;

    const storedUserString =
      localStorage.getItem("food_ordering_user") ?? localStorage.getItem("user");
    const storedRole = localStorage.getItem("roleName");

    if (!storedUserString) {
      router.replace("/login");
      return;
    }

    try {
      const parsed = JSON.parse(storedUserString) as User | { role?: string };
      const roleFromUser =
        (parsed as any)?.roleName ?? (parsed as any)?.role ?? storedRole ?? "";
      if (String(roleFromUser).toUpperCase() !== "ADMIN") {
        router.replace("/login");
        return;
      }
      setUser(createAdminUserFromStorage(parsed));
    } catch (error) {
      console.error("Failed to parse stored admin user", error);
      router.replace("/login");
    }
  }, [router]);

  const handleLogout = () => {
    if (typeof window !== "undefined") {
      localStorage.removeItem("token");
      localStorage.removeItem("roleName");
      localStorage.removeItem("food_ordering_user");
      localStorage.removeItem("user");
    }
    router.push("/login");
  };

  const navItems = [
    { href: "/admin/dashboard", label: "Trang chủ", icon: LayoutDashboard },
    { href: "/admin/users", label: "Người dùng", icon: Users },
    { href: "/admin/foods", label: "Món ăn", icon: UtensilsCrossed },
    { href: "/admin/restaurants", label: "Quán ăn", icon: Home },
  ];

  const bottomNavItems = [
    { href: "#", label: "Setting", icon: Settings },
    { href: "#", label: "Log out", icon: LogOut, onClick: handleLogout },
  ];

  if (!user) {
    return (
      <div className="flex h-screen items-center justify-center">
        {/* You can add a loading spinner here */}
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen w-full bg-gray-50 dark:bg-gray-900">
      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 transform flex-col border-r border-border bg-background transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-20 items-center gap-2 border-b border-border px-6">
          <Logo />
        </div>
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            {navItems.map((item) => (
              <li key={item.href}>
                <Link
                  href={item.href}
                  onClick={() => setSidebarOpen(false)}
                  className={`flex items-center gap-3 rounded-xl px-4 py-2.5 text-base font-medium transition-colors ${
                    pathname === item.href
                      ? "bg-primary/10 text-primary ring-1 ring-primary/20"
                      : "text-muted-foreground hover:bg-gray-100 hover:text-foreground dark:hover:bg-gray-800"
                  }`}
                >
                  <item.icon
                    className={`h-5 w-5 ${
                      pathname === item.href ? "text-primary" : ""
                    }`}
                  />
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        <div className="mt-auto p-4">
          <ul className="space-y-2">
            {bottomNavItems.map((item) => (
              <li key={item.label}>
                <button
                  onClick={item.onClick}
                  className="flex w-full items-center gap-3 rounded-lg px-4 py-2.5 text-base font-medium text-muted-foreground transition-colors hover:bg-gray-100 hover:text-foreground dark:hover:bg-gray-800"
                >
                  <item.icon className="h-5 w-5" />
                  {item.label}
                </button>
              </li>
            ))}
          </ul>
        </div>
      </aside>

      {/* Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Main Content */}
      <div className="flex flex-1 flex-col">
        <header className="sticky top-0 z-30 flex h-20 items-center justify-between border-b border-border bg-background/80 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/60 md:px-8">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="icon"
              className="lg:hidden"
              onClick={() => setSidebarOpen(!sidebarOpen)}
            >
              <Menu className="h-6 w-6" />
            </Button>
            <div className="relative hidden md:block">
              <Search className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Tìm kiếm..."
                className="w-full rounded-2xl bg-gray-100 pl-10 dark:bg-gray-800"
              />
            </div>
          </div>
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" className="rounded-full">
              <Bell className="h-6 w-6" />
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  className="flex items-center gap-3 rounded-lg px-2 py-1.5"
                >
                  <Avatar className="h-9 w-9">
                    <AvatarImage
                      src={user.avatarUrl || "/placeholder-user.jpg"}
                      alt={user.name}
                    />
                    <AvatarFallback>{user.name.charAt(0)}</AvatarFallback>
                  </Avatar>
                  <div className="hidden text-left lg:block">
                    <p className="text-sm font-semibold">{user.name}</p>
                    <p className="text-xs text-muted-foreground">
                      Visual Designer
                    </p>
                  </div>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel>My Account</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem>Profile</DropdownMenuItem>
                <DropdownMenuItem>Settings</DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout}>
                  Log out
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto">{children}</main>
      </div>
    </div>
  );
}
