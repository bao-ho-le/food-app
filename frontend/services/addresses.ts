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

export async function fetchUserAddresses() {
  return apiClient.get<UserAddressResponse[]>("/address/user")
}

export async function createUserAddress(payload: UserAddressPayload) {
  return apiClient.post<UserAddressResponse>("/address/user", payload)
}

export async function updateUserAddress(addressId: string | number, payload: UserAddressPayload) {
  return apiClient.put<UserAddressResponse>(`/address/user/${addressId}`, payload)
}

export async function deleteUserAddress(addressId: string | number) {
  return apiClient.delete<string>(`/address/user/${addressId}`)
}
