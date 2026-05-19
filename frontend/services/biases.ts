import { apiClient } from "@/lib/api-client"
import type { TagResponse } from "./tags"

export type UserBiasResponse = {
  id?: number | string
  score?: number
  tag?: TagResponse
  user?: {
    id?: number | string
  }
}

export async function fetchUserBiases(token: string) {
  return apiClient.get<UserBiasResponse[]>("/bias", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
}

export type UpdateUserBiasPayload = {
  tagId: number | string
  score: number
}

export async function updateUserBias(token: string, payload: UpdateUserBiasPayload) {
  return apiClient.put<UserBiasResponse>("/bias", payload, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
}
