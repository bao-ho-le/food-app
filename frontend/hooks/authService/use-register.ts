import { useCallback, useState } from "react";
import { API_BASE_URL, getAccessToken, setAccessToken } from "@/lib/api-client";

type RegisterCredentials = {
  fullName?: string;
  birthday?: string;
  email: string;
  gender?: string;
  password: string;
  phoneNumber?: string;
  [key: string]: any;
};

type RegisterResponse = {
  accessToken?: string;
  roleName?: string;
  email?: string;
    fullName?: string;
};

const REGISTER_URL = `${API_BASE_URL}/users/register`;


export default function useRegister() {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(() => getAccessToken());

  const register = useCallback(async (credentials: RegisterCredentials) => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(REGISTER_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
        credentials: "include", // bắt buộc để browser lưu refresh-token cookie
      });

      if (!res.ok) {
        // cố gắng parse body lỗi để lấy message rõ ràng
        let message = res.statusText;
        let errBody: any = null;
        try {
          errBody = await res.json();
          message = errBody?.message ?? JSON.stringify(errBody) ?? message;
        } catch {
          // ignore parse error
        }
        if (res.status === 409) {
          message = errBody?.message ?? "Email đã tồn tại";
        }
        throw new Error(message || `HTTP ${res.status}`);
      }

      // parse response an toàn
      let data: RegisterResponse;
      try {
        data = await res.json();
      } catch (parseErr) {
        const textBody = await res.text().catch(() => String(parseErr));
        throw new Error(
          `Failed to parse JSON response: ${textBody || (parseErr as Error).message || String(parseErr)}`
        );
      }

      const receivedToken = data.accessToken ?? null;

      if (!receivedToken) {
        throw new Error("Không nhận được token từ server");
      }

      // Access token giữ trong memory (lib/api-client.ts) — không localStorage.
      // Refresh token do backend set qua HttpOnly cookie, JS không đọc/ghi.
      setAccessToken(receivedToken);

      if (typeof window !== "undefined") {
        if (data?.roleName) {
          localStorage.setItem("roleName", data.roleName);
        }
        // lưu thông tin user tối giản (nếu muốn)
        const userObj = { email: data?.email ?? credentials.email, name: credentials.name ?? null };
        localStorage.setItem("user", JSON.stringify(userObj));
      }

      setToken(receivedToken);
      return receivedToken;
    } catch (err: any) {
      setError(err?.message ?? "Đăng ký thất bại");
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return { register, loading, error, token };
}