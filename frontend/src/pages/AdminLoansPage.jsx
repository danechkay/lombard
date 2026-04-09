import { useEffect, useState } from "react";
import { api } from "../api";
import { showToast } from "../toast";

const initialForm = {
  userId: "",
  contractNumber: "",
  loanAmount: "",
  repayAmount: "",
  interestRateDaily: "",
  collateralDescription: "",
  issueDate: "",
  dueDate: "",
  notes: "",
  paymentAllowed: false
};

export default function AdminLoansPage() {
  const [query, setQuery] = useState("");
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [saving, setSaving] = useState(false);
  const [sessionId, setSessionId] = useState("");
  const [smsCode, setSmsCode] = useState("");
  const [verified, setVerified] = useState(false);
  const [codeHint, setCodeHint] = useState("");

  const loadUsers = async (q = "") => {
    const params = new URLSearchParams();
    if (q.trim()) params.set("q", q.trim());
    const list = await api.adminLoanUsers(params.toString());
    setUsers(Array.isArray(list) ? list : []);
  };

  useEffect(() => {
    loadUsers().catch((e) => {
      showToast(e.message || "Не удалось загрузить пользователей", "error");
    });
  }, []);

  const onSubmit = async (event) => {
    event.preventDefault();
    if (!verified || !sessionId) {
      showToast("Сначала подтвердите SMS-код пользователя", "error");
      return;
    }
    setSaving(true);
    try {
      await api.adminCreateLoan({
        ...form,
        verifySessionId: sessionId,
        userId: Number(form.userId),
        loanAmount: Number(form.loanAmount),
        repayAmount: Number(form.repayAmount),
        interestRateDaily: Number(form.interestRateDaily)
      });
      setForm(initialForm);
      showToast("Заем успешно привязан к пользователю", "success");
    } catch (e) {
      showToast(e.message || "Не удалось сохранить заем", "error");
    } finally {
      setSaving(false);
    }
  };

  return (
    <section>
      <h1>Админ: займы пользователей</h1>
      <p className="muted-text">Создайте заем по данным из офлайн-договора и привяжите его к аккаунту клиента.</p>
      <div className="panel" style={{ marginBottom: 12 }}>
        <div className="row-actions">
          <input
            className="search-input"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Поиск пользователя (имя/email/телефон)"
          />
          <button
            type="button"
            className="btn"
            onClick={() =>
              loadUsers(query).catch((e) => showToast(e.message || "Ошибка поиска пользователей", "error"))
            }
          >
            Найти
          </button>
        </div>
      </div>

      <form className="form" onSubmit={onSubmit} style={{ maxWidth: 760 }}>
        <label>
          Пользователь
          <select
            value={form.userId}
            onChange={(e) => {
              const userId = e.target.value;
              setForm((prev) => ({ ...prev, userId }));
              setSessionId("");
              setSmsCode("");
              setVerified(false);
              setCodeHint("");
            }}
            required
          >
            <option value="">Выберите пользователя</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>
                {u.fullName} ({u.email})
              </option>
            ))}
          </select>
        </label>
        <div className="panel-soft">
          <strong>Подтверждение телефона</strong>
          <p className="muted-text">Привязка займа доступна только после подтверждения кода из SMS.</p>
          <div className="row-actions">
            <button
              type="button"
              className="btn"
              disabled={!form.userId}
              onClick={async () => {
                try {
                  const data = await api.adminSendLoanCode(Number(form.userId));
                  setSessionId(data.sessionId || "");
                  setVerified(false);
                  setCodeHint(data.maskedPhone ? `Код отправлен на ${data.maskedPhone}` : "Код отправлен");
                  if (data.debugCode) {
                    showToast(`Тестовый код: ${data.debugCode}`, "success");
                  } else {
                    showToast("Код отправлен", "success");
                  }
                } catch (e) {
                  showToast(e.message || "Не удалось отправить код", "error");
                }
              }}
            >
              Отправить код
            </button>
            <input
              placeholder="Код из SMS"
              value={smsCode}
              onChange={(e) => setSmsCode(e.target.value)}
              maxLength={6}
            />
            <button
              type="button"
              className="btn"
              disabled={!sessionId || smsCode.length < 4}
              onClick={async () => {
                try {
                  await api.adminVerifyLoanCode(Number(form.userId), sessionId, smsCode.trim());
                  setVerified(true);
                  showToast("Код подтвержден", "success");
                } catch (e) {
                  setVerified(false);
                  showToast(e.message || "Неверный код", "error");
                }
              }}
            >
              Подтвердить код
            </button>
          </div>
          {codeHint ? <p className="muted-text">{codeHint}</p> : null}
          {verified ? <p className="muted-text">Подтверждение пройдено, можно сохранять заем.</p> : null}
        </div>
        <label>
          Номер договора
          <input
            value={form.contractNumber}
            onChange={(e) => setForm((prev) => ({ ...prev, contractNumber: e.target.value }))}
            required
          />
        </label>
        <label>
          Сумма займа
          <input
            type="number"
            min="1"
            value={form.loanAmount}
            onChange={(e) => setForm((prev) => ({ ...prev, loanAmount: e.target.value }))}
            required
          />
        </label>
        <label>
          Сумма к погашению
          <input
            type="number"
            min="1"
            value={form.repayAmount}
            onChange={(e) => setForm((prev) => ({ ...prev, repayAmount: e.target.value }))}
            required
          />
        </label>
        <label>
          Ставка в день (%)
          <input
            type="number"
            step="0.0001"
            min="0.0001"
            value={form.interestRateDaily}
            onChange={(e) => setForm((prev) => ({ ...prev, interestRateDaily: e.target.value }))}
            required
          />
        </label>
        <label>
          Дата выдачи
          <input
            type="date"
            value={form.issueDate}
            onChange={(e) => setForm((prev) => ({ ...prev, issueDate: e.target.value }))}
            required
          />
        </label>
        <label>
          Дата окончания
          <input
            type="date"
            value={form.dueDate}
            onChange={(e) => setForm((prev) => ({ ...prev, dueDate: e.target.value }))}
            required
          />
        </label>
        <label>
          Предмет залога
          <textarea
            value={form.collateralDescription}
            onChange={(e) => setForm((prev) => ({ ...prev, collateralDescription: e.target.value }))}
            required
          />
        </label>
        <label>
          Примечание менеджера
          <textarea value={form.notes} onChange={(e) => setForm((prev) => ({ ...prev, notes: e.target.value }))} />
        </label>
        <label style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <input
            type="checkbox"
            checked={form.paymentAllowed}
            onChange={(e) => setForm((prev) => ({ ...prev, paymentAllowed: e.target.checked }))}
          />
          Разрешить оплату онлайн (опционально)
        </label>
        <button className="btn btn-primary" type="submit" disabled={saving || !verified}>
          {saving ? "Сохраняем..." : "Привязать заем"}
        </button>
      </form>
    </section>
  );
}
