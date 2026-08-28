const CHECKOUT_KEY_STORAGE = "checkout-idempotency-key"

// Sinh 1 UUID duy nhất cho 1 lần "ý định" đặt hàng, giữ nguyên qua các lần
// retry (mất mạng, timeout) cho tới khi đặt hàng thành công.
export function getOrCreateIdempotencyKey(): string {
  if (typeof window === "undefined") return crypto.randomUUID()
  let key = sessionStorage.getItem(CHECKOUT_KEY_STORAGE)
  if (!key) {
    key = crypto.randomUUID()
    sessionStorage.setItem(CHECKOUT_KEY_STORAGE, key)
  }
  return key
}

// Gọi sau khi đặt hàng thành công để lần đặt hàng tiếp theo dùng key mới.
export function clearIdempotencyKey(): void {
  if (typeof window === "undefined") return
  sessionStorage.removeItem(CHECKOUT_KEY_STORAGE)
}
