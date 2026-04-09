import { useEffect, useState } from "react";
import { api } from "../api";
import { showToast } from "../toast";

export default function AccountPage() {
  const [account, setAccount] = useState(null);
  const [loans, setLoans] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [editingPhone, setEditingPhone] = useState(false);
  const [phoneDraft, setPhoneDraft] = useState("");

  useEffect(() => {
    let mounted = true;
    Promise.all([api.getAccount(), api.getMyLoans()])
      .then(([acc, list]) => {
        if (!mounted) return;
        setAccount(acc);
        setPhoneDraft(acc?.phone || "");
        setLoans(Array.isArray(list) ? list : []);
      })
      .catch((e) => {
        if (!mounted) return;
        setError(e.message);
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });
    return () => {
      mounted = false;
    };
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (loading || !account) return <p>Загрузка...</p>;

  return (
    <section className="loan-account-page">
      <h1>Личный кабинет</h1>

      <div className="loan-top-grid">
        <aside className="panel loan-profile">
          <div className="loan-profile-head">
            <div className="avatar-circle">{initialsOf(account.fullName)}</div>
            <div>
              <strong>{account.fullName}</strong>
              <p className="muted-text">Клиент ломбарда</p>
            </div>
          </div>
          <p>Телефон: {account.phone || "-"}</p>
          {editingPhone ? (
            <div className="row-actions">
              <input
                value={phoneDraft}
                onChange={(e) => setPhoneDraft(e.target.value)}
                placeholder="+7..."
              />
              <button
                type="button"
                className="btn btn-sm"
                onClick={async () => {
                  try {
                    await api.updateAccountPhone(phoneDraft);
                    setAccount((prev) => ({ ...prev, phone: phoneDraft.trim() }));
                    setEditingPhone(false);
                    showToast("Телефон сохранен", "success");
                  } catch (e) {
                    showToast(e.message || "Не удалось сохранить телефон", "error");
                  }
                }}
              >
                Сохранить
              </button>
              <button
                type="button"
                className="btn btn-sm btn-ghost"
                onClick={() => {
                  setPhoneDraft(account.phone || "");
                  setEditingPhone(false);
                }}
              >
                Отмена
              </button>
            </div>
          ) : (
            <button type="button" className="btn btn-sm btn-ghost" onClick={() => setEditingPhone(true)}>
              Изменить телефон
            </button>
          )}
          <p>Email: {account.email || "-"}</p>
        </aside>

        <article className="panel loan-active-card">
          <h2>Мои займы</h2>
          <p className="muted-text">Займы добавляются менеджером после оформления договора в отделении.</p>
          {loans.length === 0 ? (
            <p>Пока нет привязанных займов.</p>
          ) : (
            <div className="loan-history-grid">
              {loans.map((loan) => (
                <article className="card loan-history-card" key={loan.id}>
                  <div className="loan-card-head">
                    <h3>Договор №{loan.contractNumber}</h3>
                    <span className={`status-pill status-${statusTone(loan.status)}`}>{statusLabel(loan.status)}</span>
                  </div>
                  <p>Сумма займа: {formatCurrency(loan.loanAmount)}</p>
                  <p>К погашению: {formatCurrency(loan.repayAmount)}</p>
                  <p>Ставка в день: {loan.interestRateDaily}%</p>
                  <p>Дата выдачи: {loan.issueDate}</p>
                  <p>Срок до: {loan.dueDate}</p>
                  <p>Залог: {loan.collateralDescription}</p>
                  {loan.notes ? <p>Примечание: {loan.notes}</p> : null}
                  <p className="muted-text">Оплата онлайн: {loan.paymentAllowed ? "доступна" : "недоступна"}</p>
                </article>
              ))}
            </div>
          )}
        </article>
      </div>
    </section>
  );
}

function formatCurrency(value) {
  return `${Number(value || 0).toLocaleString("ru-RU")} ₽`;
}

function initialsOf(fullName) {
  const parts = String(fullName || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (!parts.length) return "ЛК";
  return parts
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join("");
}

function statusLabel(status) {
  if (status === "ACTIVE") return "Активен";
  if (status === "OVERDUE") return "Просрочен";
  if (status === "CLOSED") return "Закрыт";
  return status;
}

function statusTone(status) {
  if (status === "ACTIVE") return "green";
  if (status === "OVERDUE") return "red";
  return "yellow";
}
