import { useEffect, useState } from "react";
import { api } from "../api";
import PaymentModal from "../components/PaymentModal";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");
  const [paymentModal, setPaymentModal] = useState(null); // { orderId, amount, receipt? }

  useEffect(() => {
    api.getOrders().then(setOrders).catch((e) => setError(e.message));
  }, []);

  const makeMockTxId = (id) => {
    const suffix = String(id).split("").reduce((acc, ch) => (acc + Number(ch)) % 1000, 0);
    return `MOCK-${id}-${String(suffix).padStart(3, "0")}`;
  };

  const reload = async () => {
    setError("");
    const pageOrders = await api.getOrders();
    setOrders(pageOrders);
  };

  if (error) return <p className="error">{error}</p>;

  return (
    <section>
      <h1>Мои заказы</h1>
      {orders.length === 0 ? (
        <p>Заказов пока нет</p>
      ) : (
        <>
          {orders.map((order) => (
            <article className="card orders-card" key={order.id}>
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

              {order.orderStatus === "NEW" && (
                <div className="orders-actions">
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={() => {
                      setPaymentModal({ orderId: order.id, amount: order.totalAmount });
                    }}
                  >
                    Оплатить (псевдо)
                  </button>
                </div>
              )}

              {order.orderStatus === "PAID" && (
                <div className="orders-actions">
                  <span className="mock-payment-pill">Тестовая оплата</span>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    onClick={() => {
                      const paidAt = order.updatedAt || order.createdAt;
                      setPaymentModal({
                        orderId: order.id,
                        amount: order.totalAmount,
                        receipt: {
                          orderStatus: order.orderStatus,
                          paidAt,
                          txId: makeMockTxId(order.id),
                          amount: order.totalAmount
                        }
                      });
                    }}
                  >
                    Чек
                  </button>
                </div>
              )}
            </article>
          ))}
          {paymentModal && (
            <PaymentModal
              orderId={paymentModal.orderId}
              amount={paymentModal.amount}
              receipt={paymentModal.receipt}
              onClose={async ({ paid } = {}) => {
                setPaymentModal(null);
                if (paid) await reload().catch(() => {});
              }}
              onPay={async () => {
                // Ошибки оплаты покажет сама модалка.
                return api.mockPayOrder(paymentModal.orderId);
              }}
            />
          )}
        </>
      )}
    </section>
  );
}
