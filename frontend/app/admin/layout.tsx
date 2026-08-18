"use client";

import type React from "react";
import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { Logo } from "@/components/logo";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import {
  LayoutDashboard,
  Users,
  UtensilsCrossed,
  Package,
  LogOut,
  Home,
} from "lucide-react";
import { User } from "@/types";
import { fetchUserProfile } from "@/services/users";

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
    address: Array.isArray(data?.address) ? data.address : [],
  };
};

const navItems = [
  { href: "/admin/dashboard", label: "Trang chủ", icon: LayoutDashboard },
  { href: "/admin/users", label: "Người dùng", icon: Users },
  { href: "/admin/foods", label: "Món ăn", icon: UtensilsCrossed },
  { href: "/admin/orders", label: "Đơn hàng", icon: Package },
  { href: "/admin/restaurants", label: "Quán ăn", icon: Home },
];

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    if (typeof window === "undefined") return;

    const token = localStorage.getItem("token");
    const storedUserString =
      localStorage.getItem("food_ordering_user") ?? localStorage.getItem("user");

    if (!token) {
      router.replace("/login");
      return;
    }

    // Hiển thị tạm dữ liệu từ localStorage trong lúc chờ xác thực thật với backend
    if (storedUserString) {
      try {
        setUser(createAdminUserFromStorage(JSON.parse(storedUserString)));
      } catch {
        // bỏ qua, chờ kết quả từ /users/profiles
      }
    }

    fetchUserProfile({ token })
      .then((profile) => {
        if (profile.roleName !== "ADMIN") {
          router.replace("/login");
          return;
        }
        setUser(
          createAdminUserFromStorage({
            name: profile.fullName,
            email: profile.email,
            phone: profile.phoneNumber,
            gender: profile.gender?.toLowerCase(),
            birthdate: profile.birthday ?? "",
          }),
        );
      })
      .catch((error) => {
        console.error("Failed to verify admin role", error);
        router.replace("/login");
      });
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

  const currentNavItem = navItems.find((item) => item.href === pathname);

  if (!user) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <SidebarProvider>
      <Sidebar collapsible="icon">
        <SidebarHeader>
          <Logo className="px-2 py-1 group-data-[collapsible=icon]:justify-center" />
        </SidebarHeader>
        <SidebarContent>
          <SidebarGroup>
            <SidebarMenu>
              {navItems.map((item) => (
                <SidebarMenuItem key={item.href}>
                  <SidebarMenuButton
                    asChild
                    isActive={pathname === item.href}
                    tooltip={item.label}
                  >
                    <Link href={item.href}>
                      <item.icon />
                      <span>{item.label}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroup>
        </SidebarContent>
        <SidebarFooter>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton onClick={handleLogout} tooltip="Log out">
                <LogOut />
                <span>Log out</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarFooter>
      </Sidebar>

      <SidebarInset>
        <header className="sticky top-0 z-30 flex h-16 items-center gap-2 border-b border-border bg-background/80 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <SidebarTrigger />
          <Separator orientation="vertical" className="mr-2 h-4" />
          <Breadcrumb>
            <BreadcrumbList>
              <BreadcrumbItem>
                <BreadcrumbLink asChild>
                  <Link href="/admin/dashboard">Admin</Link>
                </BreadcrumbLink>
              </BreadcrumbItem>
              {currentNavItem && (
                <>
                  <BreadcrumbSeparator />
                  <BreadcrumbItem>
                    <BreadcrumbPage>{currentNavItem.label}</BreadcrumbPage>
                  </BreadcrumbItem>
                </>
              )}
            </BreadcrumbList>
          </Breadcrumb>
          <div className="ml-auto">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <button className="flex items-center gap-3 rounded-lg px-2 py-1.5 hover:bg-accent">
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
                      {user.email}
                    </p>
                  </div>
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel>My Account</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout}>
                  Log out
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto">{children}</main>
      </SidebarInset>
    </SidebarProvider>
  );
}
