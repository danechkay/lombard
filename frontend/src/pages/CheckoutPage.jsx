import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";

export default function CheckoutPage() {
  const [checkout, setCheckout] = useState(null);
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    api.getCheckout().then(setCheckout).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!checkout) return <p>Загрузка...</p>;

  return (
    <section>
      <h1>Оформление заказа</h1>
      <div className="panel">
        <p>
          Покупатель: <b>{checkout.user.fullName}</b> ({checkout.user.email})
        </p>
        <ul className="order-list">
          {checkout.cartItems.map((item) => (
            <li key={item.id}>
              {item.productName} - {item.quantity} шт. - {item.subtotal} ₽
            </li>
          ))}
        </ul>
        <p className="price">Итого: {checkout.total} ₽</p>
        <textarea
          placeholder="Комментарий к заказу"
          value={comment}
          onChange={(e) => setComment(e.target.value)}
        />
        <button
          type="button"
          onClick={async () => {
            try {
              const result = await api.placeOrder({ comment });
              alert(`Заказ №${result.orderId} оформлен`);
              navigate("/account/orders");
            } catch (e) {
              setError(e.message);
            }
          }}
        >
          Подтвердить заказ
        </button>
      </div>
    </section>
  );
}
