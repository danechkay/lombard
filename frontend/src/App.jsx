import { useEffect, useState } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { api } from "./api";
import Layout from "./components/Layout";
import CatalogPage from "./pages/CatalogPage";
import LandingPage from "./pages/LandingPage";
import ProductPage from "./pages/ProductPage";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AccountPage from "./pages/AccountPage";
import AdminPage from "./pages/AdminPage";
import AdminProductsPage from "./pages/AdminProductsPage";
import AdminPromotionsPage from "./pages/AdminPromotionsPage";
import AdminCategoriesPage from "./pages/AdminCategoriesPage";
import AdminOrdersPage from "./pages/AdminOrdersPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminValuationsPage from "./pages/AdminValuationsPage";
import AdminLoansPage from "./pages/AdminLoansPage";
import DepartmentsPage from "./pages/DepartmentsPage";
import PromotionsPage from "./pages/PromotionsPage";
import ValuationPage from "./pages/ValuationPage";
import ValuationsPage from "./pages/ValuationsPage";
import ToastContainer from "./components/ToastContainer";

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
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
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

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("theme", theme);
  }, [theme]);

  const handleLogout = async () => {
    try {
      await api.logout();
    } finally {
      await loadUser();
      navigate("/");
    }
  };

  return (
    <Layout
      user={user}
      onLogout={handleLogout}
      theme={theme}
      onToggleTheme={() => setTheme((prev) => (prev === "light" ? "dark" : "light"))}
    >
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/catalog" element={<CatalogPage user={user} />} />
        <Route path="/discounts" element={<PromotionsPage />} />
        <Route
          path="/valuation"
          element={
            <ProtectedRoute user={user}>
              <ValuationPage />
            </ProtectedRoute>
          }
        />
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
              <Navigate to="/account" replace />
            </ProtectedRoute>
          }
        />
        <Route
          path="/account/valuations"
          element={
            <ProtectedRoute user={user}>
              <ValuationsPage />
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
          path="/admin/promotions"
          element={
            <RoleRoute user={user} roles={["ADMIN", "MANAGER"]}>
              <AdminPromotionsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/admin/orders"
          element={
            <RoleRoute user={user} roles={["ADMIN", "MANAGER"]}>
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
        <Route
          path="/admin/valuations"
          element={
            <RoleRoute user={user} roles={["ADMIN", "MANAGER"]}>
              <AdminValuationsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/admin/loans"
          element={
            <RoleRoute user={user} roles={["ADMIN", "MANAGER"]}>
              <AdminLoansPage />
            </RoleRoute>
          }
        />
      </Routes>
      <ToastContainer />
    </Layout>
  );
}
