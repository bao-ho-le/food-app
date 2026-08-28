"use client";

import type React from "react";
import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { Logo } from "@/components/logo";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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
} from "@/components/ui/sidebar";
import {
  LayoutDashboard,
  Users,
  UtensilsCrossed,
  Package,
  LogOut,
  Home,
  Store,
  Settings,
} from "lucide-react";
import { User } from "@/types";
import { fetchUserProfile } from "@/services/users";
import useLogin from "@/hooks/authService/use-login";

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
  const { logout } = useLogin();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    if (typeof window === "undefined") return;

    const storedUserString =
      localStorage.getItem("food_ordering_user") ?? localStorage.getItem("user");

    // Không gate cứng theo localStorage nữa — access token giờ sống trong
    // memory nên sau F5 sẽ luôn rỗng. Cứ gọi thẳng /users/profiles: nếu
    // request 401, apiClient tự refresh bằng cookie rồi retry; chỉ khi
    // refresh cũng thất bại (không còn phiên hợp lệ) mới rơi vào .catch bên
    // dưới và redirect login.

    // Hiển thị tạm dữ liệu từ localStorage trong lúc chờ xác thực thật với backend
    if (storedUserString) {
      try {
        setUser(createAdminUserFromStorage(JSON.parse(storedUserString)));
      } catch {
        // bỏ qua, chờ kết quả từ /users/profiles
      }
    }

    fetchUserProfile()
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

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      if (typeof window !== "undefined") {
        localStorage.removeItem("food_ordering_user");
        localStorage.removeItem("user");
      }
      router.push("/login");
    }
  };

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
        <SidebarFooter className="pb-6">
          <SidebarMenu>
            <SidebarMenuItem>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <SidebarMenuButton size="lg" tooltip={user.name}>
                    <Settings />
                    <span>{user.name}</span>
                  </SidebarMenuButton>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuItem asChild>
                    <Link href="/user/food">
                      <Store className="h-4 w-4" />
                      Quay về trang chủ
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="h-4 w-4" />
                    Đăng xuất
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarFooter>
      </Sidebar>

      <SidebarInset>
        <main className="flex flex-1 min-h-0 flex-col overflow-y-auto">{children}</main>
      </SidebarInset>
    </SidebarProvider>
  );
}
