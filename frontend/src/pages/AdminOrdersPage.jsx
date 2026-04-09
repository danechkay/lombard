import { useEffect, useState } from "react";
import { api } from "../api";

const statuses = ["NEW", "PAID", "SHIPPED", "COMPLETED", "CANCELLED"];

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");

  const statusRu = (status) => {
    if (status === "NEW") return "Новый";
    if (status === "PAID") return "Оплачен (бронь)";
    if (status === "SHIPPED") return "Готов к выдаче";
    if (status === "COMPLETED") return "Выдан";
    if (status === "CANCELLED") return "Отменен";
    return status;
  };

  const load = async () => {
    try {
      setError("");
      const page = await api.adminOrders("page=0");
      setOrders(page.content || []);
    } catch (e) {
      setOrders([]);
      setError(e.message || "Не удалось загрузить заказы");
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <section>
      <h1>Админ: заказы</h1>
      {error ? <p className="error">{error}</p> : null}
      {orders.map((o) => (
        <article className="card" key={o.id}>
          <h3>Заказ #{o.id}</h3>
          <p>Пользователь: {o.userEmail}</p>
          <p>Магазин: {o.storeName || "-"}</p>
          <p>Статус: {statusRu(o.orderStatus)}</p>
          {o.reserved ? <p>Бронь: за клиентом {o.reservedFor}</p> : null}
          {o.pickupCode ? <p>Код выдачи: {o.pickupCode}</p> : null}
          <p>Сумма: {o.totalAmount} ₽</p>
          <select
            defaultValue={o.orderStatus}
            onChange={(e) => api.adminOrderStatus(o.id, e.target.value).then(load)}
          >
            {statuses.map((s) => (
              <option key={s} value={s}>
                {statusRu(s)}
              </option>
            ))}
          </select>
        </article>
      ))}
    </section>
  );
}
