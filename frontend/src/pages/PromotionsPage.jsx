import { useEffect, useState } from "react";
import { api } from "../api";

export default function PromotionsPage() {
  const [promotions, setPromotions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getPromotions()
      .then(setPromotions)
      .finally(() => setLoading(false));
  }, []);

  return (
    <section>
      <h1>Акции</h1>
      <div className="promo-list">
        {loading &&
          Array.from({ length: 3 }).map((_, i) => (
            <div className="promo-card skeleton-card" key={`promo-skeleton-${i}`}>
              <div className="skeleton skeleton-block" />
            </div>
          ))}
        {!loading && promotions.length === 0 && <p>Пока активных акций нет.</p>}
        {promotions.map((promo) => (
          <article className="promo-card" key={promo.id}>
            <div className="promo-content">
              <h2>{promo.title}</h2>
              {promo.subtitle && <p className="promo-subtitle">{promo.subtitle}</p>}
              {promo.description && <p>{promo.description}</p>}
              <button type="button">{promo.buttonText || "Подробнее"}</button>
            </div>
            <div className="promo-image-wrap">
              {promo.imageUrl ? (
                <img src={promo.imageUrl} alt={promo.title} className="promo-image" />
              ) : (
                <div className="promo-image promo-image-placeholder">Акция</div>
              )}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
