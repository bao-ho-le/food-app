import { useState, useCallback } from "react";
import { API_BASE_URL, getAccessToken, setAccessToken } from "@/lib/api-client";

type LoginCredentials = {
  email: string;
  password: string;
};

type LoginResponse = {
  accessToken?: string;
  roleName?: string;
  email?: string;
};

const LOGIN_URL = `${API_BASE_URL}/users/login`;
const LOGOUT_URL = `${API_BASE_URL}/users/logout`;

/**
 * Hook để gọi API login/logout (Spring Boot).
 * Access token giữ trong memory (lib/api-client.ts), refresh token do backend
 * set qua HttpOnly cookie — hook này không tự tay đọc/ghi refresh token.
 * Trả về: { login, loading, error, token, logout }
 */
export default function useLogin() {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(() => getAccessToken());

  const login = useCallback(async (credentials: LoginCredentials) => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(LOGIN_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
        credentials: "include", // bắt buộc để browser lưu refresh-token cookie
      });

      if (!res.ok) {
        let message = res.statusText;
        let errBody: any = null;
        try {
          errBody = await res.json();
          message = errBody?.message ?? JSON.stringify(errBody) ?? message;
        } catch {}
        if (res.status === 401) {
          message = errBody?.message ?? "Email hoặc mật khẩu không chính xác";
        }
        throw new Error(message || `HTTP ${res.status}`);
      }

      let data: LoginResponse;
      try {
        data = await res.json();
      } catch (parseErr) {
        const textBody = await res.text().catch(() => String(parseErr));
        throw new Error(
          `Failed to parse JSON response: ${textBody || (parseErr as Error).message || String(parseErr)}`
        );
      }

      let roleName: string | undefined;
      let email: string | undefined;
      if (data.roleName) {
        roleName = data.roleName;
        localStorage.setItem("roleName", data.roleName);
      }
      if (data.email) {
        email = data.email;
      }

      const receivedToken = data.accessToken ?? null;
      if (!receivedToken) {
        throw new Error("accessToken not found in login response");
      }

      setAccessToken(receivedToken);
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

  const logout = useCallback(async () => {
    try {
      // Chỉ backend mới xoá được HttpOnly cookie — JS không đọc/xoá được nó.
      await fetch(LOGOUT_URL, { method: "POST", credentials: "include" });
    } catch {
      // Logout phải luôn coi là thành công phía client dù request lỗi mạng.
    } finally {
      setAccessToken(null);
      setToken(null);
      localStorage.removeItem("roleName");
    }
  }, []);

  return { login, loading, error, token, logout };
}
