// import type { User } from "@/types"

// const AUTH_KEY = "food_ordering_auth"
// const USER_KEY = "food_ordering_user"

// export const authService = {
//   login: (email: string, password: string): User | null => {
//     // Mock authentication
//     const user = users.find((u) => u.email === email)

//     if (user && password === "password123") {
//       localStorage.setItem(AUTH_KEY, "true")
//       localStorage.setItem(USER_KEY, JSON.stringify(user))
//       return user
//     }
//     return null
//   },

//   register: (data: { email: string; name: string; phone: string; password: string, birthday: string, gender: string }): User => {
//     const newUser: User = {
//       id: `user_${Date.now()}`,
//       email: data.email,
//       name: data.name,
//       phone: data.phone,
//       roleName: "USER",
//       createdAt: new Date().toISOString(),
//       isActive: true,
//     }

//     localStorage.setItem(AUTH_KEY, "true")
//     localStorage.setItem(USER_KEY, JSON.stringify(newUser))
//     return newUser
//   },

//   logout: () => {
//     localStorage.removeItem(AUTH_KEY)
//     localStorage.removeItem(USER_KEY)
//   },

//   getCurrentUser: (): User | null => {
//     if (typeof window === "undefined") return null
//     const userStr = localStorage.getItem(USER_KEY)
//     return userStr ? JSON.parse(userStr) : null
//   },

//   isAuthenticated: (): boolean => {
//     if (typeof window === "undefined") return false
//     return localStorage.getItem(AUTH_KEY) === "true"
//   },

//   isAdmin: (): boolean => {
//     const user = authService.getCurrentUser()
//     return user?.roleName === "ADMIN"
//   },
// }

