import { apiClient } from "@/lib/api-client"

export type UserAddressResponse = {
  id: number | string
  address: string
  isDefault: boolean
  user?: {
    id?: number | string
  }
}

export type UserAddressPayload = {
  address: string
  isDefault: boolean
}

function authHeaders(token: string): HeadersInit {
  return {
    Authorization: `Bearer ${token}`,
  }
}

export async function fetchUserAddresses(token: string) {
  return apiClient.get<UserAddressResponse[]>("/address/user", { headers: authHeaders(token) })
}

export async function createUserAddress(token: string, payload: UserAddressPayload) {
  return apiClient.post<UserAddressResponse>("/address/user", payload, { headers: authHeaders(token) })
}

export async function updateUserAddress(token: string, addressId: string | number, payload: UserAddressPayload) {
  return apiClient.put<UserAddressResponse>(`/address/user/${addressId}`, payload, { headers: authHeaders(token) })
}

export async function deleteUserAddress(token: string, addressId: string | number) {
  return apiClient.delete<string>(`/address/user/${addressId}`, { headers: authHeaders(token) })
}
