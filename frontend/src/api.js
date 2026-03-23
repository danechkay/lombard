const defaultHeaders = {
  "Content-Type": "application/json"
};

async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: "include",
    headers: {
      ...defaultHeaders,
      ...(options.headers || {})
    },
    ...options
  });

  const isJson = response.headers.get("content-type")?.includes("application/json");
  const data = isJson ? await response.json() : null;

  if (!response.ok) {
    const message = data?.message || data?.error || `Ошибка ${response.status}`;
    throw new Error(message);
  }

  return data;
}

export const api = {
  getMe: () => request("/api/auth/me"),
  register: (payload) =>
    request("/api/auth/register", { method: "POST", body: JSON.stringify(payload) }),
  login: async ({ email, password }) => {
    const body = new URLSearchParams();
    body.set("username", email);
    body.set("password", password);
    const response = await fetch("/login", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body
    });
    if (!response.ok) {
      throw new Error("Неверный логин или пароль");
    }
  },
  logout: async () => {
    const response = await fetch("/logout", {
      method: "POST",
      credentials: "include"
    });
    if (!response.ok) {
      throw new Error("Не удалось выйти из аккаунта");
    }
  },
  getCategories: () => request("/api/catalog/categories"),
  getCatalog: (queryString) => request(`/api/catalog${queryString ? `?${queryString}` : ""}`),
  getProduct: (slug) => request(`/api/catalog/product/${slug}`),
  getCart: () => request("/api/cart"),
  addToCart: (payload) => request("/api/cart/add", { method: "POST", body: JSON.stringify(payload) }),
  updateCart: (payload) => request("/api/cart/update", { method: "POST", body: JSON.stringify(payload) }),
  removeFromCart: (payload) => request("/api/cart/remove", { method: "POST", body: JSON.stringify(payload) }),
  getCheckout: () => request("/api/order/checkout"),
  placeOrder: (payload) => request("/api/order/place", { method: "POST", body: JSON.stringify(payload) }),
  getAccount: () => request("/api/account"),
  getOrders: () => request("/api/account/orders"),
  adminProducts: (queryString) => request(`/api/admin/products${queryString ? `?${queryString}` : ""}`),
  adminProductById: (id) => request(`/api/admin/products/${id}`),
  adminCreateProduct: async (formData) => {
    const response = await fetch("/api/admin/products", {
      method: "POST",
      credentials: "include",
      body: formData
    });
    if (!response.ok) throw new Error(`Ошибка ${response.status}`);
    return response.json();
  },
  adminUpdateProduct: async (id, formData) => {
    const response = await fetch(`/api/admin/products/${id}`, {
      method: "POST",
      credentials: "include",
      body: formData
    });
    if (!response.ok) throw new Error(`Ошибка ${response.status}`);
    return response.json();
  },
  adminPublishProduct: (id) => request(`/api/admin/products/${id}/publish`, { method: "POST", body: "{}" }),
  adminUnpublishProduct: (id) => request(`/api/admin/products/${id}/unpublish`, { method: "POST", body: "{}" }),
  adminCategories: () => request("/api/admin/categories"),
  adminCreateCategory: (payload) =>
    request("/api/admin/categories", { method: "POST", body: JSON.stringify(payload) }),
  adminUpdateCategory: (id, payload) =>
    request(`/api/admin/categories/${id}`, { method: "POST", body: JSON.stringify(payload) }),
  adminDeleteCategory: (id) => request(`/api/admin/categories/${id}/delete`, { method: "POST", body: "{}" }),
  adminOrders: (queryString) => request(`/api/admin/orders${queryString ? `?${queryString}` : ""}`),
  adminOrderById: (id) => request(`/api/admin/orders/${id}`),
  adminOrderStatus: (id, status) =>
    request(`/api/admin/orders/${id}/status`, { method: "POST", body: JSON.stringify({ status }) }),
  adminUsers: () => request("/api/admin/users"),
  adminBlockUser: (id) => request(`/api/admin/users/${id}/block`, { method: "POST", body: "{}" }),
  adminUnblockUser: (id) => request(`/api/admin/users/${id}/unblock`, { method: "POST", body: "{}" }),
  adminUserRole: (id, role) =>
    request(`/api/admin/users/${id}/role`, { method: "POST", body: JSON.stringify({ role }) })
};
