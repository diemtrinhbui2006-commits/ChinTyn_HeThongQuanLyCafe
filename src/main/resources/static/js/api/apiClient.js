/**
 * API Client - Hàm gọi HTTP chung (fetch)
 */

export async function request(endpoint, options = {}) {
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const res = await fetch(endpoint, config);
    if (res.status === 204) return null;
    const data = await res.json().catch(() => null);

    if (!res.ok) {
      const err = data?.message || data?.error || `Lỗi HTTP ${res.status}: ${res.statusText}`;
      throw new Error(err);
    }
    return data;
  } catch (error) {
    console.error(`[API Error] ${endpoint}:`, error);
    throw error;
  }
}
