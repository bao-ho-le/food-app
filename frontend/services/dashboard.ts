import { apiClient } from "@/lib/api-client"

export type DashboardStatsResponse = {
  totalUsers: number
  totalUsersChangePercent: number
  totalStores: number
  totalStoresChangePercent: number
  totalOrders: number
  totalOrdersChangePercent: number
  totalRevenue: number
  totalRevenueChangePercent: number
}

export type DashboardTrendPoint = {
  date: string
  revenue: number
  orderCount: number
  newCustomerCount: number
}

export type TopProduct = {
  dishId: number | string
  dishName: string
  imageUrl?: string
  price: number
  quantitySold: number
}

function getAuthHeaders(): Record<string, string> {
  if (typeof window === "undefined") return {}
  const token = localStorage.getItem("token")
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function fetchDashboardStats(): Promise<DashboardStatsResponse> {
  return apiClient.get<DashboardStatsResponse>("/admin/dashboard/stats", { headers: getAuthHeaders() })
}

export async function fetchDashboardTrend(days: 7 | 14 | 30): Promise<DashboardTrendPoint[]> {
  return apiClient.get<DashboardTrendPoint[]>(`/admin/dashboard/trend?days=${days}`, { headers: getAuthHeaders() })
}

export async function fetchTopProducts(): Promise<TopProduct[]> {
  return apiClient.get<TopProduct[]>("/admin/dashboard/top-products", { headers: getAuthHeaders() })
}
