import { apiClient } from "@/lib/api-client"
import type { AdminDish, Dish } from "@/types"

export type DishApiResponse = {
  id: number | string
  name: string
  description?: string
  price: number
  url?: string
  rating?: number
  totalReviews?: number
  available?: boolean
  tags?: {
    id?: number | string
    name: string
    category?: {
      id?: number | string
      name?: string
    }
  }[]
  restaurant?: {
    id?: number | string
    name?: string
    address?: string
    phoneNumber?: string
    available?: boolean
  }
}

function getAuthHeaders(): HeadersInit {
  if (typeof window === "undefined") return {}
  const token = localStorage.getItem("token")
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function mapAdminDish(dish: DishApiResponse): AdminDish {
  return {
    id: String(dish.id),
    name: dish.name,
    price: Number(dish.price ?? 0),
    rating: typeof dish.rating === "number" ? dish.rating : 0,
    image: dish.url || "",
    isAvailable: Boolean(dish.available),
    restaurantName: dish.restaurant?.name ?? "Không rõ",
  }
}

export type CreateDishInput = {
  name: string
  price: number
  restaurantId: number | string
  tags: Array<number | string>
  imageUrl: string
}

export async function fetchDishesRaw(): Promise<DishApiResponse[]> {
  const headers = getAuthHeaders()
  return apiClient.get<DishApiResponse[]>("/dishes", { headers })
}

export async function fetchAdminDishes(): Promise<AdminDish[]> {
  const data = await fetchDishesRaw()
  return data.map(mapAdminDish)
}

export async function createDish(input: CreateDishInput): Promise<AdminDish> {
  const headers = getAuthHeaders()
  const payload = {
    name: input.name,
    price: input.price,
    restaurantId: input.restaurantId,
    tags: input.tags,
    imageUrl: input.imageUrl,
  }
  const data = await apiClient.post<DishApiResponse>("/dishes", payload, { headers })
  return mapAdminDish(data)
}

export async function setDishBlocking(id: number | string, type: 0 | 1): Promise<string> {
  const headers = getAuthHeaders()
  return apiClient.post<string>(`/dishes/blocking/${id}/${type}`, undefined, { headers })
}
