import { useState, useCallback } from "react";
import { API_BASE_URL } from "@/lib/api-client";

// /home/thiennk/Documents/food-ordering-app/hooks/authService/use-login.ts

type LoginCredentials = {
  email: string;
  password: string;
};

type LoginResponse = {
  token?: string;
  roleName?: string;
  email?: string;
};

const LOGIN_URL = `${API_BASE_URL}/users/login`;

/**
 * Hook để gọi API login (Spring Boot) và lưu token vào localStorage.
 * Trả về: { login, loading, error, token, logout }
 */
export default function useLogin() {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(() =>
    typeof window !== "undefined" ? localStorage.getItem("token") : null
  );

  const login = useCallback(async (credentials: LoginCredentials) => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(LOGIN_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
      });

      if (!res.ok) {
        // try to parse error body, fallback to status text
        let message = res.statusText;
        let errBody: any = null;
        try {
          const errBody = await res.json();
          message = errBody?.message ?? JSON.stringify(errBody) ?? message;
        } catch {}
        if (res.status === 401) {
          message = errBody?.message ?? "Email hoặc mật khẩu không chính xác";
        }
        throw new Error(message || `HTTP ${res.status}`);
      }

      // const data: LoginResponse | string = await res
      //   .json()
      //   .catch(() => "" as any);

      let data: LoginResponse | string;
      try {
        data = await res.json();
      } catch (parseErr) {
        // Cố gắng đọc body dưới dạng text để trả về message rõ ràng
        const textBody = await res.text().catch(() => String(parseErr));
        throw new Error(
          `Failed to parse JSON response: ${textBody || (parseErr as Error).message || String(parseErr)}`
        );
      }



      // Kiểm tra nếu data là object thì mới đọc roleName/email
      let roleName: string | undefined;
      let email: string | undefined;
      if (typeof data === "object" && data !== null) {
        if (data.roleName) {
          roleName = data.roleName;
          localStorage.setItem("roleName", data.roleName);
        }
        if (data.email) {
          email = data.email;
        }
      }

      // Support several shapes: { token }, { accessToken }, or plain string
      let receivedToken: string | null = null;

      if (typeof data === "string") {
        // Trường hợp backend trả token dưới dạng chuỗi thuần (ví dụ: "eyJhbGciOi...")
        receivedToken = data;
      } else {
        // Trường hợp backend trả JSON object, ví dụ: { token: "...", roleName: "USER", email: "..." }
        receivedToken = data.token ?? null;
      }

      if (!receivedToken) {
        throw new Error("Token not found in login response");
      }

      localStorage.setItem("token", receivedToken);
      setToken(receivedToken);

      // Demo compatibility: also set mock auth keys so layouts using lib/auth.ts work.
      try {
        const lowerRole = roleName?.toLowerCase() || "user";
        const nameFromEmail = email ? email.split("@")[0] : "User";
        const demoUser = {
          id: `user_${Date.now()}`,
          email: email ?? "user@example.com",
          name: nameFromEmail,
          phone: "",
          role: lowerRole,
          createdAt: new Date().toISOString(),
          isActive: true,
        };
        localStorage.setItem("food_ordering_auth", "true");
        localStorage.setItem("food_ordering_user", JSON.stringify(demoUser));
      } catch {}
      return receivedToken;
    } catch (err: any) {
      setError(err?.message ?? "Login failed");
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("roleName");
    setToken(null);
  }, []);

  return { login, loading, error, token, logout };
}
