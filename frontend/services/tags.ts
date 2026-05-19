import { apiClient } from "@/lib/api-client"

export type TagResponse = {
  id: number | string
  name: string
  category?: {
    id: number | string
    name: string
  }
}

export async function fetchAllTags() {
  return apiClient.get<TagResponse[]>("/tags")
}
