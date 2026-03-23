import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api";

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
  if (!product) return <p>Загрузка...</p>;

  return (
    <section>
      <div className="details-grid">
        <div className="details-media">
          {product.mainImageUrl && <img className="product-image" src={product.mainImageUrl} alt={product.name} />}
        </div>
        <div className="details-info">
          <h1>{product.name}</h1>
          <p className="price">{product.price} ₽</p>
          <p className="meta">Состояние: {product.condition}</p>
          <p className="meta">Категория: {product.categoryName}</p>
          <p>{product.description}</p>
          {user?.authenticated ? (
            <button
              type="button"
              onClick={async () => {
                try {
                  await api.addToCart({ productId: product.id, quantity: 1 });
                  alert("Товар добавлен в корзину");
                } catch (e) {
                  alert(e.message);
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
