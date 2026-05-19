import { apiClient } from "@/lib/api-client"
import type { User } from "@/types"

export type UserProfileResponse = {
  fullName?: string
  birthday?: string | null
  gender?: "MALE" | "FEMALE" | "OTHER"
  phoneNumber?: string
  email?: string
}

type FetchUserProfileOptions = {
  token?: string
}

export async function fetchUserProfile(options?: FetchUserProfileOptions) {
  const headers: HeadersInit = {}

  if (options?.token) {
    headers.Authorization = `Bearer ${options.token}`
  }

  return apiClient.get<UserProfileResponse>("/users/profiles", { headers })
}

export type UpdateUserProfilePayload = {
  fullName: string
  birthday?: string | null
  gender: "MALE" | "FEMALE" | "OTHER"
  phoneNumber: string
  email?: string
}

export async function updateUserProfile(
  payload: UpdateUserProfilePayload,
  options?: FetchUserProfileOptions,
) {
  const headers: HeadersInit = {}

  if (options?.token) {
    headers.Authorization = `Bearer ${options.token}`
  }

  return apiClient.put<UserProfileResponse>("/users/profiles", payload, { headers })
}

type RoleResponse = {
  id?: string | number
  roleName?: string
}

type AdminUserResponse = {
  id?: string | number
  fullName?: string
  birthday?: string
  gender?: string
  phoneNumber?: string
  email?: string
  createdAt?: string
  active?: boolean
  role?: RoleResponse
}

function normalizeGender(gender?: string): User["gender"] {
  const normalized = gender?.toLowerCase()
  if (normalized === "male") return "male"
  if (normalized === "female") return "female"
  return "other"
}

function mapRole(role?: RoleResponse) {
  const upper = role?.roleName?.toUpperCase() === "ADMIN" ? "ADMIN" : "USER"
  return {
    roleName: upper as User["roleName"],
    role: upper === "ADMIN" ? "admin" : "user",
  }
}

function mapAdminUser(user: AdminUserResponse): User {
  const { roleName, role } = mapRole(user.role)
  return {
    id: user.id !== undefined && user.id !== null ? String(user.id) : `user-${Date.now()}`,
    email: user.email ?? "",
    name: user.fullName ?? "",
    phone: user.phoneNumber ?? "",
    gender: normalizeGender(user.gender),
    birthdate: user.birthday ?? "",
    avatarUrl: undefined,
    roleName,
    role,
    createdAt: user.createdAt ?? new Date().toISOString(),
    isActive: user.active === undefined ? true : Boolean(user.active),
    bias: [],
    address: [],
  }
}

function getStoredAuthHeaders(): HeadersInit {
  if (typeof window === "undefined") return {}
  const token = localStorage.getItem("token")
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function fetchAllUsers(): Promise<User[]> {
  const headers = getStoredAuthHeaders()
  const data = await apiClient.get<AdminUserResponse[]>("/users/getAll", { headers })
  return data.map(mapAdminUser)
}

export async function setUserBlocking(id: number | string, type: 0 | 1): Promise<string> {
  const headers = getStoredAuthHeaders()
  return apiClient.post<string>(`/users/blocking/${id}/${type}`, undefined, { headers })
}
