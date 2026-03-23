import { useEffect, useState } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { api } from "./api";
import Layout from "./components/Layout";
import CatalogPage from "./pages/CatalogPage";
import ProductPage from "./pages/ProductPage";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AccountPage from "./pages/AccountPage";
import OrdersPage from "./pages/OrdersPage";
import AdminPage from "./pages/AdminPage";
import AdminProductsPage from "./pages/AdminProductsPage";
import AdminCategoriesPage from "./pages/AdminCategoriesPage";
import AdminOrdersPage from "./pages/AdminOrdersPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import DepartmentsPage from "./pages/DepartmentsPage";

function ProtectedRoute({ user, children }) {
  if (!user?.authenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

function RoleRoute({ user, roles, children }) {
  if (!user?.authenticated) return <Navigate to="/login" replace />;
  if (!roles.includes(user.role)) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  const loadUser = async () => {
    try {
      const me = await api.getMe();
      setUser(me);
    } catch (error) {
      setUser({ authenticated: false });
    }
  };

  useEffect(() => {
    loadUser();
  }, []);

  const handleLogout = async () => {
    try {
      await api.logout();
    } finally {
      await loadUser();
      navigate("/");
    }
  };

  return (
    <Layout user={user} onLogout={handleLogout}>
      <Routes>
        <Route path="/" element={<CatalogPage user={user} />} />
        <Route path="/departments" element={<DepartmentsPage />} />
        <Route path="/product/:slug" element={<ProductPage user={user} />} />
        <Route
          path="/cart"
          element={
            <ProtectedRoute user={user}>
              <CartPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/checkout"
          element={
            <ProtectedRoute user={user}>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<LoginPage onLoginSuccess={loadUser} />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/account"
          element={
            <ProtectedRoute user={user}>
              <AccountPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/account/orders"
          element={
            <ProtectedRoute user={user}>
              <OrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <RoleRoute user={user} roles={["ADMIN", "MANAGER"]}>
              <AdminPage />
            </RoleRoute>
          }
        />
        <Route
          path="/admin/products"
          element={
            <RoleRoute user={user} roles={["ADMIN", "MANAGER"]}>
              <AdminProductsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/admin/categories"
          element={
            <RoleRoute user={user} roles={["ADMIN", "MANAGER"]}>
              <AdminCategoriesPage />
            </RoleRoute>
          }
        />
        <Route
          path="/admin/orders"
          element={
            <RoleRoute user={user} roles={["ADMIN"]}>
              <AdminOrdersPage />
            </RoleRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <RoleRoute user={user} roles={["ADMIN"]}>
              <AdminUsersPage />
            </RoleRoute>
          }
        />
      </Routes>
    </Layout>
  );
}
