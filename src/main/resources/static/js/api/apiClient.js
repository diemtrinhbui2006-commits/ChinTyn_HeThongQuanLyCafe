/**
 * API Client - Hàm gọi HTTP chung (fetch)
 */

let inMemoryCsrfToken = null;

function readCookie(name) {
  const prefix = `${name}=`;
  const entry = document.cookie.split('; ').find(cookie => cookie.startsWith(prefix));
  return entry ? decodeURIComponent(entry.slice(prefix.length)) : null;
}

export async function request(endpoint, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = new Headers({
    'Content-Type': 'application/json',
    ...options.headers,
  });
  const csrfToken = readCookie('XSRF-TOKEN') || inMemoryCsrfToken;
  if (csrfToken && !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  try {
    const res = await fetch(endpoint, {
      ...options,
      method,
      headers,
      credentials: 'same-origin',
    });
    if (res.status === 204) return null;
    const data = await res.json().catch(() => null);

    if (data && data.token && data.parameterName === '_csrf') {
      inMemoryCsrfToken = data.token;
    }

    if (!res.ok) {
      if (res.status === 401 && !endpoint.startsWith('/api/auth/')) {
        document.body.classList.add('auth-required');
        location.reload();
      }
      const err = data?.message || data?.error || `Lỗi HTTP ${res.status}: ${res.statusText}`;
      throw new Error(err);
    }
    return data;
  } catch (error) {
    console.error(`[API Error] ${endpoint}:`, error);
    throw error;
  }
}
