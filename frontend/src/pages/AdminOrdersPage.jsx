import { useEffect, useState } from "react";
import { api } from "../api";

const statuses = ["NEW", "PAID", "SHIPPED", "COMPLETED", "CANCELLED"];

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);

  const load = async () => {
    const page = await api.adminOrders("page=0");
    setOrders(page.content || []);
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <section>
      <h1>Админ: заказы</h1>
      {orders.map((o) => (
        <article className="card" key={o.id}>
          <h3>Заказ #{o.id}</h3>
          <p>Пользователь: {o.userEmail}</p>
          <p>Статус: {o.orderStatus}</p>
          <p>Сумма: {o.totalAmount} ₽</p>
          <select
            defaultValue={o.orderStatus}
            onChange={(e) => api.adminOrderStatus(o.id, e.target.value).then(load)}
          >
            {statuses.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </article>
      ))}
    </section>
  );
}
