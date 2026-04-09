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
    // Оставляем стандартный redirect-поведение (follow), чтобы браузер
    // гарантированно записал сессию (cookies) после успешного логина.
    const response = await fetch("/login", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body,
      // Отключаем follow редиректа, чтобы не уйти на `http://localhost:8080/...`
      // и не получить "Failed to fetch" на повторных входах из-за редиректов.
      redirect: "manual"
    });

    // Самый надежный критерий успеха — `/api/auth/me`.
    try {
      const me = await request("/api/auth/me");
      if (me?.authenticated) return;
    } catch {
      // Игнорируем, ниже fallback на редирект/статус
    }

    // Фоллбек: смотрим, куда редиректит Spring Security
    const location = response?.headers?.get("Location") || "";
    if (String(location).toLowerCase().includes("login?error")) throw new Error("Неверный логин или пароль");
    throw new Error("Неверный логин или пароль");
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
  getStores: () => request("/api/catalog/stores"),
  getCatalog: (queryString) => request(`/api/catalog${queryString ? `?${queryString}` : ""}`),
  getProduct: (slug) => request(`/api/catalog/product/${slug}`),
  getPromotions: () => request("/api/promotions"),
  getCart: () => request("/api/cart"),
  addToCart: (payload) => request("/api/cart/add", { method: "POST", body: JSON.stringify(payload) }),
  updateCart: (payload) => request("/api/cart/update", { method: "POST", body: JSON.stringify(payload) }),
  removeFromCart: (payload) => request("/api/cart/remove", { method: "POST", body: JSON.stringify(payload) }),
  getCheckout: () => request("/api/order/checkout"),
  placeOrder: (payload) => request("/api/order/place", { method: "POST", body: JSON.stringify(payload) }),
  mockPayOrder: (orderId) => request(`/api/order/${orderId}/mock-pay`, { method: "POST" }),
  getAccount: () => request("/api/account"),
  updateAccountPhone: (phone) => request("/api/account/phone", { method: "POST", body: JSON.stringify({ phone }) }),
  getOrders: () => request("/api/account/orders"),
  getMyLoans: () => request("/api/account/loans"),
  adminLoanUsers: (queryString) => request(`/api/admin/loans/users${queryString ? `?${queryString}` : ""}`),
  adminSendLoanCode: (userId) => request("/api/admin/loans/send-code", { method: "POST", body: JSON.stringify({ userId }) }),
  adminVerifyLoanCode: (userId, sessionId, code) =>
    request("/api/admin/loans/verify-code", { method: "POST", body: JSON.stringify({ userId, sessionId, code }) }),
  adminCreateLoan: (payload) => request("/api/admin/loans", { method: "POST", body: JSON.stringify(payload) }),
  calculateValuation: (payload) =>
    request("/api/valuation/calculate", { method: "POST", body: JSON.stringify(payload) }),
  createValuationRequest: (payload) =>
    request("/api/valuation/requests", { method: "POST", body: JSON.stringify(payload) }),
  getMyValuations: () => request("/api/account/valuations"),
  adminValuations: (queryString) => request(`/api/admin/valuations${queryString ? `?${queryString}` : ""}`),
  adminSetValuationPrice: (id, payload) =>
    request(`/api/admin/valuations/${id}/price`, { method: "POST", body: JSON.stringify(payload) }),
  adminProducts: (queryString) =>
    request(`/api/admin/products${queryString ? `?${queryString}` : ""}`).then((data) => {
      // Бэкенд может вернуть:
      // - Page<ProductDto> с полем content
      // - просто массив продуктов
      if (Array.isArray(data)) return data;
      if (data && Array.isArray(data.content)) return data.content;
      return [];
    }),
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
  adminDeleteProduct: (id) => request(`/api/admin/products/${id}/delete`, { method: "POST", body: "{}" }),
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
  adminUsers: (queryString) => request(`/api/admin/users${queryString ? `?${queryString}` : ""}`),
  adminBlockUser: (id) => request(`/api/admin/users/${id}/block`, { method: "POST", body: "{}" }),
  adminUnblockUser: (id) => request(`/api/admin/users/${id}/unblock`, { method: "POST", body: "{}" }),
  adminUserRole: (id, role, storeId = null) =>
    request(`/api/admin/users/${id}/role`, { method: "POST", body: JSON.stringify({ role, storeId }) }),
  adminPromotions: () => request("/api/admin/promotions"),
  adminCreatePromotion: (payload) =>
    request("/api/admin/promotions", { method: "POST", body: JSON.stringify(payload) }),
  adminUpdatePromotion: (id, payload) =>
    request(`/api/admin/promotions/${id}`, { method: "POST", body: JSON.stringify(payload) }),
  adminDeletePromotion: (id) =>
    request(`/api/admin/promotions/${id}/delete`, { method: "POST", body: "{}" })
};
