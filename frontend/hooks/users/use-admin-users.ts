"use client"

import { useCallback, useEffect, useState } from "react"
import type { User } from "@/types"
import { fetchAllUsers } from "@/services/users"

export function useAdminUsers(initialUsers: User[] = []) {
  const [data, setData] = useState<User[]>(initialUsers)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const users = await fetchAllUsers()
      setData(users)
    } catch (err) {
      const message = err instanceof Error ? err.message : "Không thể tải danh sách người dùng."
      setError(message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  return {
    data,
    loading,
    error,
    refresh: load,
    setData,
  }
}
