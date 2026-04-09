import { useEffect, useState } from "react";
import { api } from "../api";

const formatPrice = (v) => {
  if (v === null || v === undefined || Number.isNaN(Number(v))) return "-";
  return `${Number(v).toLocaleString("ru-RU")} ₽`;
};

export default function ValuationPage() {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    categoryId: "",
    modelName: "",
    description: "",
    condition: "USED",
    year: ""
  });
  const [loading, setLoading] = useState(false);
  const [requestRes, setRequestRes] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .getCategories()
      .then((c) => setCategories(Array.isArray(c) ? c : []))
      .catch(() => setCategories([]));
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setRequestRes(null);
    setLoading(true);
    try {
      const payload = {
        categoryId: form.categoryId ? Number(form.categoryId) : null,
        modelName: form.modelName,
        description: form.description && form.description.trim() ? form.description.trim() : null,
        condition: form.condition,
        year: form.year ? Number(form.year) : null
      };
      const res = await api.createValuationRequest(payload);
      setRequestRes(res);
    } catch (e2) {
      setError(e2.message || "Ошибка расчета");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section>
      <h1>Оценка</h1>
      <div className="panel">
        <form className="valuation-form" onSubmit={submit}>
          <label className="field">
            <span>Категория (необязательно)</span>
            <select
              value={form.categoryId}
              onChange={(e) => setForm((p) => ({ ...p, categoryId: e.target.value }))}
            >
              <option value="">Любая</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Название модели/товара</span>
            <input
              value={form.modelName}
              onChange={(e) => setForm((p) => ({ ...p, modelName: e.target.value }))}
              placeholder="Например: iPhone 13 Pro, Samsung Galaxy S22..."
              required
            />
          </label>

          <label className="field">
            <span>Описание товара (что не так)</span>
            <textarea
              value={form.description}
              onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
              placeholder="Например: трещина на экране, не работает камера, потертости и т.д."
              rows={3}
            />
          </label>

          <label className="field">
            <span>Состояние</span>
            <select
              value={form.condition}
              onChange={(e) => setForm((p) => ({ ...p, condition: e.target.value }))}
            >
              <option value="NEW">Новое</option>
              <option value="USED">Б/у</option>
            </select>
          </label>

          <label className="field">
            <span>Год (необязательно)</span>
            <input
              value={form.year}
              onChange={(e) => setForm((p) => ({ ...p, year: e.target.value }))}
              placeholder="например 2021"
              inputMode="numeric"
            />
          </label>

          <div className="valuation-actions">
            <button type="submit" className="btn btn-primary btn-sm" disabled={loading}>
              {loading ? "Считаем..." : "Рассчитать"}
            </button>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() =>
                setForm({
                  categoryId: "",
                  modelName: "",
                  description: "",
                  condition: "USED",
                  year: ""
                })
              }
              disabled={loading}
            >
              Сбросить
            </button>
          </div>

          {error && <p className="error">{error}</p>}
        </form>

        {requestRes && (
          <div className="panel-soft" style={{ marginTop: 14 }}>
            <p className="muted-text" style={{ margin: "0 0 8px" }}>
              Заявка отправлена менеджеру
            </p>
            <p style={{ margin: 0 }}>
              ID заявки: <b>{requestRes.id}</b>
            </p>
            <div className="price" style={{ margin: "10px 0 6px" }}>
              {requestRes.analogEstimatedPrice ? formatPrice(requestRes.analogEstimatedPrice) : "—"}
            </div>
            <p className="muted-text" style={{ margin: "0 0 8px" }}>
              Аналоги найдено: <b>{requestRes.analogMatchedCount}</b>.
            </p>
            {requestRes.analogEstimatedPrice ? (
              <p style={{ margin: 0 }} className="muted-text">
                Это ориентир. Итоговую цену назначит менеджер в вашем профиле.
              </p>
            ) : (
              <p style={{ margin: 0 }} className="muted-text">
                Аналоги не найдены. Менеджер назначит цену после проверки.
              </p>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
