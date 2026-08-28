export const API_BASE_URL = process.env.NEXT_PUBLIC_SPRING_URL;

// Access token sống trong memory (không localStorage) — mất khi reload trang,
// nhưng request đầu tiên sau reload sẽ tự 401 -> interceptor bên dưới tự
// refresh lại bằng refresh-token cookie (HttpOnly) trước khi retry.
let accessToken: string | null = null;

export function getAccessToken(): string | null {
  return accessToken;
}

export function setAccessToken(token: string | null) {
  accessToken = token;
}

export class ApiError extends Error {
  status: number;
  errorCode?: string;

  constructor(status: number, errorCode: string | undefined, message: string) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }
}

async function handle<T>(res: Response): Promise<T> {
  const contentType = res.headers.get("content-type") || "";
  if (!res.ok) {
    const raw = await res.text().catch(() => "");
    let message = `Đã có lỗi xảy ra (mã ${res.status}). Vui lòng thử lại sau.`;
    let errorCode: string | undefined;
    if (raw) {
      try {
        const parsed = JSON.parse(raw);
        if (typeof parsed?.message === "string" && parsed.message.trim()) {
          message = parsed.message;
        }
        if (typeof parsed?.error === "string") {
          errorCode = parsed.error;
        }
      } catch {
        // body không phải JSON hợp lệ, giữ message mặc định ở trên
      }
    }
    throw new ApiError(res.status, errorCode, message);
  }
  if (contentType.includes("application/json")) {
    return (await res.json()) as T;
  }
  const text = await res.text().catch(() => "");
  return text as unknown as T;
}

function clearSessionAndRedirect() {
  setAccessToken(null);
  if (typeof window !== "undefined") {
    window.location.href = "/login";
  }
}

// Single-flight refresh: nhiều request 401 cùng lúc chỉ trigger đúng 1 lần
// gọi /users/refresh, tất cả cùng chờ chung 1 promise — tránh kích hoạt
// reuse-detection ở backend (sẽ revoke toàn bộ token nếu 2 refresh cùng jti
// chạy song song).
let refreshPromise: Promise<string> | null = null;

async function doActualRefreshCall(): Promise<string> {
  // fetch trần, KHÔNG qua apiClient/request() — nếu chính call này cũng
  // 401 thì không được đệ quy vào lại logic retry bên dưới.
  const res = await fetch(`${API_BASE_URL}/users/refresh`, {
    method: "POST",
    credentials: "include", // bắt buộc để gửi kèm refresh-token cookie
  });
  if (!res.ok) {
    throw new Error(`Refresh thất bại với status ${res.status}`);
  }
  const data = await res.json();
  const newToken = data?.accessToken as string | undefined;
  if (!newToken) {
    throw new Error("Response /users/refresh thiếu accessToken");
  }
  setAccessToken(newToken);
  return newToken;
}

// Không có side-effect khi thất bại (không tự redirect) — caller tự quyết
// định phải làm gì tiếp. Single-flight: nhiều nơi gọi cùng lúc chỉ tạo đúng
// 1 request /users/refresh thật sự (quan trọng — 2 lệnh refresh song song
// cùng 1 refresh token sẽ kích hoạt reuse-detection ở backend).
function getNewAccessToken(): Promise<string> {
  if (!refreshPromise) {
    refreshPromise = doActualRefreshCall().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

// Dùng cho những nơi CHỈ MUỐN BIẾT "đang có phiên đăng nhập hợp lệ không"
// (vd. trang chủ công khai quyết định có tự chuyển vào /user/food hay không)
// mà KHÔNG được ép redirect /login nếu không — khách vãng lai chưa đăng nhập
// ghé trang chủ là chuyện bình thường.
export async function trySilentRefresh(): Promise<boolean> {
  if (accessToken) return true;
  try {
    await getNewAccessToken();
    return true;
  } catch {
    return false;
  }
}

async function request<T>(
  path: string,
  method: string,
  body: unknown,
  init?: RequestInit,
  isRetry = false,
): Promise<T> {
  const headers = new Headers(init?.headers);
  if (body !== undefined && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (!headers.has("Authorization") && accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    credentials: "include",
  });

  if (res.status === 401 && !isRetry) {
    try {
      await getNewAccessToken();
    } catch (err) {
      clearSessionAndRedirect();
      throw err;
    }
    return request<T>(path, method, body, init, true);
  }

  return handle<T>(res);
}

export const apiClient = {
  get: <T>(path: string, init?: RequestInit) => request<T>(path, "GET", undefined, init),
  post: <T>(path: string, body?: unknown, init?: RequestInit) => request<T>(path, "POST", body, init),
  put: <T>(path: string, body?: unknown, init?: RequestInit) => request<T>(path, "PUT", body, init),
  patch: <T>(path: string, body?: unknown, init?: RequestInit) => request<T>(path, "PATCH", body, init),
  delete: <T>(path: string, init?: RequestInit) => request<T>(path, "DELETE", undefined, init),
};
