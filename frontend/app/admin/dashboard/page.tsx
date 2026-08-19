"use client"

import { useEffect, useMemo, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart"
import { DollarSign, Package, Users, UtensilsCrossed, LayoutDashboard } from "lucide-react"
import { AreaChart, Area, CartesianGrid, XAxis } from "recharts"
import type { AdminDish, AdminOrder, AdminOrderStatus, User } from "@/types"
import { fetchAdminOrders } from "@/services/admin-orders"
import { fetchAllUsers } from "@/services/users"
import { fetchAdminDishes } from "@/services/dishes"

const STATUS_LABEL: Record<AdminOrderStatus, string> = {
  PENDING: "Chờ xử lý",
  PREPARING: "Đang chuẩn bị",
  DELIVERING: "Đang giao hàng",
  DELIVERED: "Đã giao",
  CANCELLED: "Đã huỷ",
}

const STATUS_BADGE_CLASS: Record<AdminOrderStatus, string> = {
  PENDING: "bg-amber-100 text-amber-700 ring-amber-200",
  PREPARING: "bg-blue-100 text-blue-700 ring-blue-200",
  DELIVERING: "bg-indigo-100 text-indigo-700 ring-indigo-200",
  DELIVERED: "bg-emerald-100 text-emerald-700 ring-emerald-200",
  CANCELLED: "bg-rose-100 text-rose-700 ring-rose-200",
}

const CHART_DAYS = 14

const chartConfig = {
  revenue: {
    label: "Doanh thu",
    color: "var(--chart-1)",
  },
} satisfies ChartConfig

function formatCurrency(value: number) {
  return `${value.toLocaleString("vi-VN")}₫`
}

export default function DashboardPage() {
  const [orders, setOrders] = useState<AdminOrder[]>([])
  const [users, setUsers] = useState<User[]>([])
  const [dishes, setDishes] = useState<AdminDish[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  useEffect(() => {
    let isMounted = true
    setIsLoading(true)
    setLoadError(null)
    Promise.all([fetchAdminOrders(), fetchAllUsers(), fetchAdminDishes()])
      .then(([ordersData, usersData, dishesData]) => {
        if (!isMounted) return
        setOrders(ordersData)
        setUsers(usersData)
        setDishes(dishesData)
      })
      .catch((error) => {
        if (isMounted) setLoadError(error instanceof Error ? error.message : "Không thể tải dữ liệu dashboard.")
      })
      .finally(() => {
        if (isMounted) setIsLoading(false)
      })
    return () => {
      isMounted = false
    }
  }, [])

  const totalRevenue = useMemo(
    () => orders.filter((o) => o.status === "DELIVERED").reduce((sum, o) => sum + o.totalPrice, 0),
    [orders],
  )
  const totalDishesAvailable = useMemo(() => dishes.filter((d) => d.isAvailable).length, [dishes])

  const chartData = useMemo(() => {
    const byDay = new Map<string, number>()
    orders
      .filter((o) => o.status === "DELIVERED")
      .forEach((o) => {
        const day = new Date(o.createdAt).toISOString().slice(0, 10)
        byDay.set(day, (byDay.get(day) ?? 0) + o.totalPrice)
      })
    return Array.from(byDay.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .slice(-CHART_DAYS)
      .map(([day, revenue]) => ({
        day: new Date(day).toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" }),
        revenue,
      }))
  }, [orders])

  const recentOrders = useMemo(
    () =>
      [...orders]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 8),
    [orders],
  )

  if (isLoading) {
    return null
  }

  if (loadError) {
    return (
      <div className="flex-1 p-4 md:px-18 md:py-10">
        <Empty>
          <EmptyHeader>
            <EmptyMedia variant="icon" className="bg-destructive/10 text-destructive">
              <LayoutDashboard />
            </EmptyMedia>
            <EmptyTitle>Không thể tải dashboard</EmptyTitle>
            <EmptyDescription>{loadError}</EmptyDescription>
          </EmptyHeader>
        </Empty>
      </div>
    )
  }

  if (orders.length === 0) {
    return (
      <div className="flex-1 p-4 md:px-18 md:py-10">
        <Empty>
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <LayoutDashboard />
            </EmptyMedia>
            <EmptyTitle>Chưa có dữ liệu để hiển thị</EmptyTitle>
            <EmptyDescription>Hệ thống chưa có đơn hàng nào. Số liệu sẽ xuất hiện khi có đơn hàng đầu tiên.</EmptyDescription>
          </EmptyHeader>
        </Empty>
      </div>
    )
  }

  return (
    <div className="flex-1 space-y-10 bg-background p-4 md:px-18 pt-4 md:pt-6 md:pb-10">
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Tổng doanh thu" value={formatCurrency(totalRevenue)} icon={<DollarSign className="h-6 w-6 text-white" />} iconBgColor="bg-indigo-500" />
        <StatCard title="Tổng đơn hàng" value={orders.length.toLocaleString("vi-VN")} icon={<Package className="h-6 w-6 text-white" />} iconBgColor="bg-blue-500" />
        <StatCard title="Tổng người dùng" value={users.length.toLocaleString("vi-VN")} icon={<Users className="h-6 w-6 text-white" />} iconBgColor="bg-yellow-500" />
        <StatCard title="Món ăn đang bán" value={totalDishesAvailable.toLocaleString("vi-VN")} icon={<UtensilsCrossed className="h-6 w-6 text-white" />} iconBgColor="bg-cyan-500" />
      </div>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Doanh thu theo ngày</CardTitle>
          </CardHeader>
          <CardContent>
            {chartData.length === 0 ? (
              <p className="py-16 text-center text-sm text-muted-foreground">Chưa có đơn hàng đã giao để tính doanh thu.</p>
            ) : (
              <ChartContainer config={chartConfig} className="h-[320px] w-full">
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="var(--color-revenue)" stopOpacity={0.8} />
                      <stop offset="95%" stopColor="var(--color-revenue)" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="day" tickLine={false} axisLine={false} />
                  <ChartTooltip content={<ChartTooltipContent indicator="dot" />} />
                  <Area type="monotone" dataKey="revenue" stroke="var(--color-revenue)" fillOpacity={1} fill="url(#colorRevenue)" />
                </AreaChart>
              </ChartContainer>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Đơn hàng gần đây</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {recentOrders.map((order) => (
              <div key={order.id} className="flex items-center justify-between gap-3 border-b pb-3 last:border-b-0 last:pb-0">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">#{order.id} · {order.customerName}</p>
                  <p className="text-xs text-muted-foreground">{new Date(order.createdAt).toLocaleDateString("vi-VN")}</p>
                </div>
                <div className="flex shrink-0 flex-col items-end gap-1">
                  <span className="text-sm font-semibold">{formatCurrency(order.totalPrice)}</span>
                  <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset ${STATUS_BADGE_CLASS[order.status]}`}>
                    {STATUS_LABEL[order.status]}
                  </span>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

interface StatCardProps {
  title: string
  value: string
  icon: React.ReactNode
  iconBgColor: string
}

function StatCard({ title, value, icon, iconBgColor }: StatCardProps) {
  return (
    <Card className="rounded-3xl shadow-sm">
      <CardContent className="px-8">
        <div className={`flex h-12 w-12 items-center justify-center rounded-full ${iconBgColor}`}>
          {icon}
        </div>
        <div className="mt-3">
          <p className="text-3xl font-bold">{value}</p>
          <p className="mt-2 text-sm text-muted-foreground">{title}</p>
        </div>
      </CardContent>
    </Card>
  )
}
