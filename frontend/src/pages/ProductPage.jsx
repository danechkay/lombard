import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api";
import { showToast } from "../toast";

export default function ProductPage({ user }) {
  const { slug } = useParams();
  const [product, setProduct] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .getProduct(slug)
      .then(setProduct)
      .catch((e) => setError(e.message));
  }, [slug]);

  if (error) return <p className="error">{error}</p>;
  if (!product) {
    return (
      <section className="product-detail-page">
        <div className="details-grid">
          <div className="details-media skeleton skeleton-block" />
          <div className="details-info">
            <div className="skeleton skeleton-line" />
            <div className="skeleton skeleton-line short" />
            <div className="skeleton skeleton-line medium" />
            <div className="skeleton skeleton-line" />
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="product-detail-page">
      <div className="details-grid">
        <div className="details-media">
          {product.mainImageUrl && <img className="product-image" src={product.mainImageUrl} alt={product.name} />}
        </div>
        <div className="details-info">
          <h1>{product.name}</h1>
          <p className="price">{product.price} ₽</p>
          <p className="meta">Состояние: {product.condition}</p>
          <p className="meta">Категория: {product.categoryName}</p>
          {(product.storeName || product.storeAddress) && (
            <p className="meta">
              Магазин: {product.storeName || "—"}
              {product.storeAddress ? ` — ${product.storeAddress}` : ""}
            </p>
          )}
          <p>{product.description}</p>
          {user?.authenticated ? (
            <button
              type="button"
              onClick={async () => {
                try {
                  await api.addToCart({ productId: product.id, quantity: 1 });
                  showToast("Товар добавлен в корзину");
                } catch (e) {
                  showToast(e.message, "error");
                }
              }}
            >
              Купить
            </button>
          ) : (
            <Link to="/login" className="card-link">
              Войти для покупки
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}
