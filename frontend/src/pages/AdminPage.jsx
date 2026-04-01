import { Link } from "react-router-dom";

export default function AdminPage() {
  return (
    <section>
      <h1>Админ-панель</h1>
      <div className="admin-links admin-grid">
        <Link to="/admin/products">Товары</Link>
        <Link to="/admin/promotions">Акции</Link>
        <Link to="/admin/categories">Категории</Link>
        <Link to="/admin/orders">Заказы (только ADMIN)</Link>
        <Link to="/admin/users">Пользователи (только ADMIN)</Link>
        <Link to="/admin/valuations">Заявки на оценку</Link>
      </div>
    </section>
  );
}
