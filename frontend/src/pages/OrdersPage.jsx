import { useEffect, useState } from "react";
import { api } from "../api";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api.getOrders().then(setOrders).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="error">{error}</p>;

  return (
    <section>
      <h1>Мои заказы</h1>
      {orders.length === 0 ? (
        <p>Заказов пока нет</p>
      ) : (
        orders.map((order) => (
          <article className="card" key={order.id}>
            <h3>Заказ #{order.id}</h3>
            <p>Статус: {order.orderStatus}</p>
            <p>Сумма: {order.totalAmount} ₽</p>
            <p>Комментарий: {order.comment || "-"}</p>
            <ul>
              {order.items.map((item) => (
                <li key={item.id}>
                  {item.productName}: {item.quantity} x {item.price} ₽
                </li>
              ))}
            </ul>
          </article>
        ))
      )}
    </section>
  );
}
