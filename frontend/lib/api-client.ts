export const API_BASE_URL = process.env.NEXT_PUBLIC_SPRING_URL;

async function handle<T>(res: Response): Promise<T> {
  const contentType = res.headers.get("content-type") || "";
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `HTTP ${res.status}`);
  }
  if (contentType.includes("application/json")) {
    return (await res.json()) as T;
  }
  const text = await res.text().catch(() => "");
  return text as unknown as T;
}

export const apiClient = {
  get: async <T>(path: string, init?: RequestInit) => {
    const res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      method: "GET",
      headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
      credentials: "include",
    });
    return handle<T>(res);
  },
  post: async <T>(path: string, body?: unknown, init?: RequestInit) => {
    const res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      method: "POST",
      headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
    return handle<T>(res);
  },
  put: async <T>(path: string, body?: unknown, init?: RequestInit) => {
    const res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      method: "PUT",
      headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
    return handle<T>(res);
  },
  delete: async <T>(path: string, init?: RequestInit) => {
    const res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      method: "DELETE",
      headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
      credentials: "include",
    });
    return handle<T>(res);
  },
};
