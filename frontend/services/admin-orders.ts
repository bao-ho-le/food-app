import { apiClient } from "@/lib/api-client"
import type { AdminOrder, AdminOrderItem } from "@/types"

type OrderApiResponse = {
  id: number | string
  status: string
  totalPrice: number
  deliveryAddress: string
  createdAt: string
  customerName?: string
  customerEmail?: string
  customerPhone?: string
}

type OrderItemApiResponse = {
  id: number | string
  dishName: string
  quantity: number
  price: number
  imageUrl?: string
}

function getAuthHeaders(): Record<string, string> {
  if (typeof window === "undefined") return {}
  const token = localStorage.getItem("token")
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function mapAdminOrder(order: OrderApiResponse): AdminOrder {
  return {
    id: String(order.id),
    customerName: order.customerName ?? "Không rõ",
    customerEmail: order.customerEmail,
    customerPhone: order.customerPhone,
    deliveryAddress: order.deliveryAddress,
    totalPrice: order.totalPrice,
    status: order.status as AdminOrder["status"],
    createdAt: order.createdAt,
  }
}

export async function fetchAdminOrders(): Promise<AdminOrder[]> {
  try {
    const data = await apiClient.get<OrderApiResponse[]>("/admin/orders", { headers: getAuthHeaders() })
    return data.map(mapAdminOrder)
  } catch (e) {
    // BE trả 404 khi danh sách rỗng (hành vi hiện có của OrderService.getAll) — coi là danh sách rỗng, không phải lỗi
    if (e instanceof Error && e.message.includes("404")) return []
    throw e
  }
}

export async function fetchAdminOrderItems(orderId: number | string): Promise<AdminOrderItem[]> {
  const data = await apiClient.get<OrderItemApiResponse[]>(`/admin/orders/${orderId}/items`, { headers: getAuthHeaders() })
  return data.map((item) => ({
    dishName: item.dishName,
    imageUrl: item.imageUrl,
    quantity: item.quantity,
    price: item.price,
  }))
}

export async function updateOrderStatus(orderId: number | string, status: AdminOrder["status"]): Promise<AdminOrder> {
  const data = await apiClient.patch<OrderApiResponse>(`/admin/orders/${orderId}/status`, { status }, { headers: getAuthHeaders() })
  return mapAdminOrder(data)
}
