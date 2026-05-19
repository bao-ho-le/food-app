import { apiClient } from "@/lib/api-client"

export type RecommendationAction =
  | "DETAILS"
  | "ADD_TO_CART"
  | "ORDER"
  | "REMOVE_FROM_CART"
  | "CANCEL_ORDER"

export type RecommendationPayload = {
  dishId: number | string
  action: RecommendationAction
}

export type RecommendationResponse = Record<string, number>

function getAuthToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem("token")
}

export async function fetchRecommendations(): Promise<RecommendationResponse> {
  const token = getAuthToken()
  if (!token) {
    throw new Error("Missing auth token")
  }
  return apiClient.get<RecommendationResponse>("/recommendations", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
}

export async function trackUserAction(payload: RecommendationPayload): Promise<void> {
  const token = getAuthToken()
  if (!token) return
  await apiClient.post("/recommendations", payload, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
}
