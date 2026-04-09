import { useEffect, useState } from "react";
import { api } from "../api";

export default function AdminValuationsPage() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  const [priceById, setPriceById] = useState({});
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setError("");
    const page = await api.adminValuations("page=0");
    setItems(page?.content || []);
  };

  useEffect(() => {
    load().catch((e) => setError(e.message));
  }, []);

  const setPrice = async (id) => {
    const raw = priceById[id];
    const priceNum = raw === "" || raw === null || raw === undefined ? NaN : Number(raw);
    if (!Number.isFinite(priceNum)) {
      setError("Введите корректную цену");
      return;
    }
    setLoading(true);
    try {
      await api.adminSetValuationPrice(id, { price: priceNum });
      await load();
      setPriceById((prev) => ({ ...prev, [id]: "" }));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section>
      <h1>Админ: заявки на оценку</h1>
      {error && <p className="error">{error}</p>}
      <div className="cards">
        {items.map((v) => (
          <article className="card" key={v.id}>
            <h3>Заявка #{v.id}</h3>
            <p>
              {v.modelName} • {v.condition}
              {v.year ? ` • ${v.year}` : ""}
            </p>
            {v.description && <p className="muted-text">Описание: {v.description}</p>}
            <p>
              Аналоги: <b>{v.analogMatchedCount}</b>
            </p>
            <p>Ориентир: {v.analogEstimatedPrice ? `${v.analogEstimatedPrice} ₽` : "-"}</p>
            <p>Статус: {v.status}</p>
            {v.status === "NEW" ? (
              <div className="row-actions" style={{ marginTop: 10 }}>
                <input
                  value={priceById[v.id] || ""}
                  onChange={(e) => setPriceById((prev) => ({ ...prev, [v.id]: e.target.value }))}
                  placeholder="Цена (₽)"
                  inputMode="numeric"
                />
                <button type="button" disabled={loading} onClick={() => setPrice(v.id)}>
                  Назначить цену
                </button>
              </div>
            ) : (
              <p className="muted-text">
                Назначено: <b>{v.managerPrice ? `${v.managerPrice} ₽` : "-"}</b>
              </p>
            )}
          </article>
        ))}
      </div>
    </section>
  );
}

