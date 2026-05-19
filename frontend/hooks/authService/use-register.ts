import { useCallback, useState } from "react";
import { API_BASE_URL } from "@/lib/api-client";

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
  token?: string;
  roleName?: string;
  email?: string;
    fullName?: string;
};

const REGISTER_URL = `${API_BASE_URL}/users/register`;


export default function useRegister() {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(() =>
    typeof window !== "undefined" ? localStorage.getItem("token") : null
  );

  const register = useCallback(async (credentials: RegisterCredentials) => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(REGISTER_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
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

      // nhận token từ nhiều dạng response
      let receivedToken: string | null = null;
     
        receivedToken = data.token ?? null;
      

      if (!receivedToken) {
        throw new Error("Không nhận được token từ server");
      }

      // lưu token / thông tin cần thiết trên client
      if (typeof window !== "undefined") {
        localStorage.setItem("token", receivedToken);
        if (typeof data === "object" && data?.roleName) {
          localStorage.setItem("roleName", data.roleName);
        }
        // lưu thông tin user tối giản (nếu muốn)
        const userObj = { email: (data && (data as any).email) ?? credentials.email, name: credentials.name ?? null };
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