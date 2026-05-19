import { apiClient } from "@/lib/api-client"

export type DishReviewResponse = {
  userName?: string
  comment?: string
  rating?: number
  createdAt?: string
}

function getAuthHeaders(): HeadersInit {
  if (typeof window === "undefined") return {}
  const token = localStorage.getItem("token")
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function fetchDishReviews(dishId: string | number) {
  const headers = getAuthHeaders()
  const data = await apiClient.get<DishReviewResponse[]>(`/reviews/dish/${dishId}`, { headers })
  return data.sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    )
}
