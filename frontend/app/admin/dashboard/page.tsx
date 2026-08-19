"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { ArrowDownRight, ArrowUpRight, DollarSign, LayoutDashboard, Package, Store, Users } from "lucide-react"
import { AreaChart, Area, CartesianGrid, XAxis, YAxis } from "recharts"
import type { AdminOrder, AdminOrderStatus } from "@/types"
import { fetchAdminOrders } from "@/services/admin-orders"
import {
  fetchDashboardStats,
  fetchDashboardTrend,
  fetchTopProducts,
  type DashboardStatsResponse,
  type DashboardTrendPoint,
  type TopProduct,
} from "@/services/dashboard"

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

function formatCurrency(value: number) {
  return `${value.toLocaleString("vi-VN")}₫`
}

function formatShortDate(isoDate: string) {
  const d = new Date(isoDate)
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`
}

function chartConfigFor(metric: "revenue" | "orderCount" | "newCustomerCount"): ChartConfig {
  if (metric === "revenue") {
    return {
      value: {
        label: "Doanh thu",
        color: "var(--chart-1)",
      },
    }
  }
  if (metric === "orderCount") {
    return {
      value: {
        label: "Số đơn hàng",
        color: "var(--chart-2)",
      },
    }
  }
  return {
    value: {
      label: "Khách hàng mới",
      color: "var(--chart-3)",
    },
  }
}

const RECENT_ORDERS_LIMIT = 6

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStatsResponse | null>(null)
  const [trendData, setTrendData] = useState<DashboardTrendPoint[]>([])
  const [topProducts, setTopProducts] = useState<TopProduct[]>([])
  const [recentOrders, setRecentOrders] = useState<AdminOrder[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [trendPeriodDays, setTrendPeriodDays] = useState<7 | 14 | 30>(7)
  const [activeMetric, setActiveMetric] = useState<"revenue" | "orderCount" | "newCustomerCount">("revenue")

  /* Initial load — fetch everything */
  useEffect(() => {
    let isMounted = true
    setIsLoading(true)
    setLoadError(null)

    Promise.all([
      fetchDashboardStats(),
      fetchDashboardTrend(7),
      fetchTopProducts(),
      fetchAdminOrders(),
    ])
      .then(([statsData, trendData, topProductsData, ordersData]) => {
        if (!isMounted) return
        setStats(statsData)
        setTrendData(trendData)
        setTopProducts(topProductsData)
        setRecentOrders(
          [...ordersData]
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
            .slice(0, RECENT_ORDERS_LIMIT),
        )
      })
      .catch((error) => {
        if (isMounted) setLoadError(error instanceof Error ? error.message : "Không thể tải dữ liệu dashboard.")
      })
      .finally(() => {
        if (isMounted) setIsLoading(false)
      })

    return () => { isMounted = false }
  }, [])

  /* Re-fetch trend when period changes (independent of full page load) */
  useEffect(() => {
    if (isLoading) return
    let isMounted = true
    fetchDashboardTrend(trendPeriodDays)
      .then((data) => { if (isMounted) setTrendData(data) })
      .catch(() => { /* keep existing trend data on error */ })
    return () => { isMounted = false }
  }, [trendPeriodDays, isLoading])

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

  if (!stats) {
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
    <div className="flex-1 space-y-8 bg-background p-4 md:px-18 pt-4 md:pt-6 md:pb-10">
      {/* B3 — 4 KPI cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          label="Tổng số tài khoản"
          value={stats.totalUsers.toLocaleString("vi-VN")}
          changePercent={stats.totalUsersChangePercent}
          icon={<Users className="h-5 w-5 text-white" />}
          iconBgColor="bg-yellow-500"
        />
        <KpiCard
          label="Số cửa hàng đang hoạt động"
          value={stats.totalStores.toLocaleString("vi-VN")}
          changePercent={stats.totalStoresChangePercent}
          icon={<Store className="h-5 w-5 text-white" />}
          iconBgColor="bg-cyan-500"
        />
        <KpiCard
          label="Tổng đơn hàng"
          value={stats.totalOrders.toLocaleString("vi-VN")}
          changePercent={stats.totalOrdersChangePercent}
          icon={<Package className="h-5 w-5 text-white" />}
          iconBgColor="bg-blue-500"
        />
        <KpiCard
          label="Tổng doanh thu"
          value={formatCurrency(stats.totalRevenue)}
          changePercent={stats.totalRevenueChangePercent}
          icon={<DollarSign className="h-5 w-5 text-white" />}
          iconBgColor="bg-indigo-500"
        />
      </div>

      {/* B4 — Full-width chart */}
      <Card>
        <CardHeader className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <CardTitle>Tổng quan doanh thu</CardTitle>
          <div className="flex items-center gap-3">
            <ToggleGroup
              type="single"
              value={activeMetric}
              onValueChange={(v) => v && setActiveMetric(v as typeof activeMetric)}
              className="gap-1 rounded-lg bg-muted/60 p-1"
            >
              <ToggleGroupItem value="revenue" className="rounded-md data-[state=on]:bg-emerald-100 data-[state=on]:text-emerald-700 data-[state=on]:shadow-sm">
                Doanh thu
              </ToggleGroupItem>
              <ToggleGroupItem value="orderCount" className="rounded-md data-[state=on]:bg-emerald-100 data-[state=on]:text-emerald-700 data-[state=on]:shadow-sm">
                Đơn hàng
              </ToggleGroupItem>
              <ToggleGroupItem value="newCustomerCount" className="rounded-md data-[state=on]:bg-emerald-100 data-[state=on]:text-emerald-700 data-[state=on]:shadow-sm">
                Khách hàng
              </ToggleGroupItem>
            </ToggleGroup>
            <Select value={String(trendPeriodDays)} onValueChange={(v) => setTrendPeriodDays(Number(v) as 7 | 14 | 30)}>
              <SelectTrigger className="w-[120px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="7">7 ngày</SelectItem>
                <SelectItem value="14">14 ngày</SelectItem>
                <SelectItem value="30">30 ngày</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          {trendData.length === 0 ? (
            <p className="py-16 text-center text-sm text-muted-foreground">Chưa có dữ liệu xu hướng.</p>
          ) : (
            <ChartContainer config={chartConfigFor(activeMetric)} className="h-[320px] w-full">
              <AreaChart data={trendData.map((p) => ({ ...p, value: p[activeMetric] }))}>
                <CartesianGrid vertical={false} />
                <XAxis dataKey="date" tickFormatter={(d) => formatShortDate(d)} tickLine={false} axisLine={false} />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  width={activeMetric === "revenue" ? 70 : 40}
                  tickFormatter={(v) => activeMetric === "revenue" ? formatCurrency(v as number) : (v as number).toLocaleString("vi-VN")}
                />
                <ChartTooltip content={<ChartTooltipContent formatter={(value) => activeMetric === "revenue" ? formatCurrency(value as number) : (value as number).toLocaleString("vi-VN")} />} />
                <Area dataKey="value" type="monotone" fill="var(--color-value)" stroke="var(--color-value)" />
              </AreaChart>
            </ChartContainer>
          )}
        </CardContent>
      </Card>

      {/* B5 + B6 — Recent Orders & Top Products side by side */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* B5 — Recent Orders */}
        <Card>
          <CardHeader>
            <CardTitle>Đơn hàng gần đây</CardTitle>
          </CardHeader>
          <CardContent>
            {recentOrders.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">Chưa có đơn hàng nào.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã đơn</TableHead>
                    <TableHead>Khách hàng</TableHead>
                    <TableHead>Trạng thái</TableHead>
                    <TableHead className="text-right">Tổng tiền</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {recentOrders.map((order) => (
                    <TableRow key={order.id} className="py-2">
                      <TableCell className="font-medium">#{order.id}</TableCell>
                      <TableCell>{order.customerName}</TableCell>
                      <TableCell>
                        <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset ${STATUS_BADGE_CLASS[order.status]}`}>
                          {STATUS_LABEL[order.status]}
                        </span>
                      </TableCell>
                      <TableCell className="text-right font-semibold">{formatCurrency(order.totalPrice)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>

        {/* B6 — Top Products */}
        <Card>
          <CardHeader>
            <CardTitle>Sản phẩm bán chạy</CardTitle>
          </CardHeader>
          <CardContent>
            {topProducts.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">Chưa có dữ liệu.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tên món</TableHead>
                    <TableHead className="text-right">Đã bán</TableHead>
                    <TableHead className="text-right">Giá</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {topProducts.map((product) => (
                    <TableRow key={product.dishId} className="py-2">
                      <TableCell>{product.dishName}</TableCell>
                      <TableCell className="text-right">{product.quantitySold.toLocaleString("vi-VN")}</TableCell>
                      <TableCell className="text-right">{formatCurrency(product.price)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

/* B3 — KpiCard component */
interface KpiCardProps {
  label: string
  value: string
  changePercent: number
  icon: React.ReactNode
  iconBgColor: string
}

function KpiCard({ label, value, changePercent, icon, iconBgColor }: KpiCardProps) {
  const isPositiveChange = changePercent >= 0
  return (
    <Card className="rounded-2xl shadow-sm">
      <CardContent className="space-y-3 px-5 py-4">
        <div className={`flex h-9 w-9 items-center justify-center rounded-full ${iconBgColor}`}>
          {icon}
        </div>
        <p className="text-sm font-medium">
          {label}: {value}
        </p>
        <p className={`flex items-center gap-1 whitespace-nowrap text-xs font-medium ${isPositiveChange ? "text-emerald-600" : "text-rose-600"}`}>
          {isPositiveChange ? <ArrowUpRight className="h-3 w-3 shrink-0" /> : <ArrowDownRight className="h-3 w-3 shrink-0" />}
          {isPositiveChange ? "+" : ""}{changePercent.toFixed(1)}% so với tháng trước
        </p>
      </CardContent>
    </Card>
  )
}
