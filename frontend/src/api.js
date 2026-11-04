// Single place that talks to the Spring Boot REST API.
const BASE = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export function getAuth() {
  try {
    return JSON.parse(localStorage.getItem("auth"));
  } catch {
    return null;
  }
}

export function saveAuth(auth) {
  localStorage.setItem("auth", JSON.stringify(auth));
}

export function clearAuth() {
  localStorage.removeItem("auth");
}

export async function api(path, { method = "GET", body } = {}) {
  const auth = getAuth();
  const res = await fetch(BASE + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(auth ? { Authorization: `Bearer ${auth.token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && auth) {
    clearAuth();
    window.location.href = "/login";
    throw new Error("Your session expired. Sign in again.");
  }
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(data?.message || "Something went wrong. Try again.");
  return data;
}

export const money = (n) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(n ?? 0);
