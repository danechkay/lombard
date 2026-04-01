import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { showToast } from "../toast";
import PaymentModal from "../components/PaymentModal";

export default function CheckoutPage() {
  const [checkout, setCheckout] = useState(null);
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const [paymentOrderId, setPaymentOrderId] = useState(null);
  const [isPaymentOpen, setIsPaymentOpen] = useState(false);
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);

  useEffect(() => {
    api.getCheckout().then(setCheckout).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!checkout) {
    return (
      <section>
        <h1>Оформление заказа</h1>
        <div className="panel">
          <div className="skeleton skeleton-line" />
          <div className="skeleton skeleton-line" />
          <div className="skeleton skeleton-line medium" />
          <div className="skeleton skeleton-block" />
        </div>
      </section>
    );
  }

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
            setError("");
            setIsPlacingOrder(true);
            try {
              const result = await api.placeOrder({ comment });
              showToast(`Заказ №${result.orderId} оформлен. Переходим к оплате.`);
              setPaymentOrderId(result.orderId);
              setIsPaymentOpen(true);
            } catch (e) {
              setError(e.message);
              showToast(e.message, "error");
            } finally {
              setIsPlacingOrder(false);
            }
          }}
          disabled={isPlacingOrder}
        >
          Подтвердить заказ
        </button>
      </div>

      {isPaymentOpen && paymentOrderId && (
        <PaymentModal
          orderId={paymentOrderId}
          amount={checkout.total}
          onClose={({ paid } = {}) => {
            setIsPaymentOpen(false);
            if (paid) navigate("/account/orders");
          }}
          onPay={async () => {
            setError("");
            return api.mockPayOrder(paymentOrderId);
          }}
        />
      )}
    </section>
  );
}
