import { useEffect, useState } from "react";

export default function PaymentModal({ orderId, amount, receipt: receiptProp, onClose, onPay }) {
  const [phase, setPhase] = useState("PAY"); // PAY | RECEIPT
  const [isPaying, setIsPaying] = useState(false);
  const [error, setError] = useState("");
  const [receipt, setReceipt] = useState(null);
  const [didPay, setDidPay] = useState(false);

  useEffect(() => {
    setIsPaying(false);
    setError("");

    if (receiptProp) {
      // Режим просмотра чека: без выполнения onPay.
      setPhase("RECEIPT");
      setReceipt(receiptProp);
      setDidPay(false);
      return;
    }

    // Режим оплаты (PAY): чистим состояние.
    setPhase("PAY");
    setReceipt(null);
    setDidPay(false);
  }, [orderId, receiptProp]);

  const close = () => {
    if (typeof onClose === "function") onClose({ paid: didPay });
  };

  const handlePay = async () => {
    setIsPaying(true);
    setError("");
    try {
      const result = await onPay();
      setDidPay(true);
      setReceipt({
        orderStatus: result?.orderStatus || "PAID",
        paidAt: new Date().toISOString(),
        txId: `MOCK-${Math.random().toString(16).slice(2, 10).toUpperCase()}`,
        pickupCode: result?.pickupCode || "",
        amount: amount ?? null
      });
      setPhase("RECEIPT");
    } catch (e) {
      setError(e?.message || "Ошибка оплаты");
    } finally {
      setIsPaying(false);
    }
  };

  const formatAmount = (v) => {
    if (v === null || v === undefined || Number.isNaN(Number(v))) return "-";
    return `${Number(v).toLocaleString("ru-RU")} ₽`;
  };

  return (
    <div
      className="modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-label="Оплата заказа"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !isPaying) close();
      }}
    >
      <div className="modal">
        <div className="modal-header">
          <h2>{phase === "PAY" ? "Оплата заказа" : "Чек об оплате"}</h2>
          <button
            type="button"
            className="modal-close"
            aria-label="Закрыть"
            disabled={isPaying}
            onClick={() => {
              if (isPaying) return;
              close();
            }}
          >
            ×
          </button>
        </div>

        <div className="modal-body">
          <p className="modal-note">
            Заказ <b>#{orderId}</b>
          </p>

          {phase === "PAY" && (
            <>
              <p className="muted-text">
                Это псевдо-оплата для прототипа: заказ будет переведён в статус <b>PAID</b> без реального платежа.
              </p>
              <p className="muted-text">
                Сумма: <b>{formatAmount(amount)}</b>
              </p>
              {error && <p className="error">{error}</p>}
            </>
          )}

          {phase === "RECEIPT" && receipt && (
            <>
              <div className="panel-soft">
                <p style={{ margin: 0 }}>
                  Статус: <b>{receipt.orderStatus}</b>
                </p>
                <p style={{ margin: "8px 0 0" }}>
                  Сумма: <b>{formatAmount(receipt.amount)}</b>
                </p>
                <p style={{ margin: "8px 0 0" }}>
                  Реквизит: <b>{receipt.txId}</b>
                </p>
                {receipt.pickupCode ? (
                  <p style={{ margin: "8px 0 0" }}>
                    Код выдачи: <b>{receipt.pickupCode}</b>
                  </p>
                ) : null}
                <p style={{ margin: "8px 0 0" }}>
                  Дата: <b>{new Date(receipt.paidAt).toLocaleString("ru-RU")}</b>
                </p>
              </div>
              <p className="muted-text">
                Оплата выполнена в тестовом режиме. Заказ готов к обработке.
              </p>
            </>
          )}
        </div>

        <div className="modal-actions">
          {phase === "PAY" ? (
            <>
              <button type="button" className="btn btn-ghost" disabled={isPaying} onClick={() => close()}>
                Отмена
              </button>
              <button
                type="button"
                className="btn btn-primary"
                disabled={isPaying}
                onClick={handlePay}
                aria-busy={isPaying}
              >
                {isPaying ? "Выполняем псевдо-оплату..." : "Оплатить (псевдо)"}
              </button>
            </>
          ) : (
            <button type="button" className="btn btn-primary" onClick={() => close()}>
              ОК
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

