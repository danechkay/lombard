import { useEffect, useState } from "react";
import { api } from "../api";

const formatPrice = (v) => {
  if (v === null || v === undefined || Number.isNaN(Number(v))) return "-";
  return `${Number(v).toLocaleString("ru-RU")} ₽`;
};

export default function ValuationsPage() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .getMyValuations()
      .then((page) => setItems(page?.content || page || []))
      .catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="error">{error}</p>;

  return (
    <section>
      <h1>Мои оценки</h1>
      {items.length === 0 ? (
        <p>Пока нет заявок на оценку</p>
      ) : (
        <div className="cards">
          {items.map((v) => (
            <article className="card" key={v.id}>
              <h3>Заявка #{v.id}</h3>
              <p>Статус: {v.status}</p>
              <p>
                {v.modelName} • {v.condition}
                {v.year ? ` • ${v.year}` : ""}
              </p>
              {v.description && (
                <p className="muted-text">
                  Описание: {v.description}
                </p>
              )}
              <p>
                Аналоги: <b>{v.analogMatchedCount}</b>
              </p>
              <p>
                Ориентир: {v.analogEstimatedPrice ? formatPrice(v.analogEstimatedPrice) : "-"}
              </p>
              {v.status === "PRICED" && (
                <>
                  <p>
                    Назначенная цена: <b>{formatPrice(v.managerPrice)}</b>
                  </p>
                  {v.managerNote && <p className="muted-text">Комментарий менеджера: {v.managerNote}</p>}
                </>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

