import { apiClient } from "@/lib/api-client";
import type { Restaurant } from "@/types";

// Map backend fields -> frontend Restaurant type
function mapRestaurant(r: any): Restaurant {
  return {
    id: String(r.id),
    name: r.name,
    address: r.address,
    phone: r.phoneNumber,
    isActive: Boolean(r.available),
  };
}

function getAuthHeaders(): HeadersInit {
  if (typeof window === "undefined") return {};
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function fetchRestaurants(): Promise<Restaurant[]> {
  const data = await apiClient.get<any[]>("/restaurants", { headers: getAuthHeaders() });
  return data.map(mapRestaurant);
}

export type CreateRestaurantInput = {
  name: string;
  address: string;
  phone: string;
};

export type UpdateRestaurantInput = CreateRestaurantInput;

export async function createRestaurant(input: CreateRestaurantInput): Promise<Restaurant> {
  const payload = {
    name: input.name,
    address: input.address,
    phoneNumber: input.phone,
  };
  const data = await apiClient.post<any>("/restaurants", payload, { headers: getAuthHeaders() });
  return mapRestaurant(data);
}

export async function updateRestaurant(id: number | string, input: UpdateRestaurantInput): Promise<Restaurant> {
  const payload = {
    name: input.name,
    address: input.address,
    phoneNumber: input.phone,
  };
  const data = await apiClient.put<any>(`/restaurants/${id}`, payload, { headers: getAuthHeaders() });
  return mapRestaurant(data);
}

export async function setRestaurantBlocking(id: number | string, type: 0 | 1): Promise<string> {
  const headers = getAuthHeaders();
  return apiClient.post<string>(`/restaurants/blocking/${id}/${type}`, undefined, { headers });
}
