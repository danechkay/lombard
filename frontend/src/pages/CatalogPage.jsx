import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../api";

export default function CatalogPage({ user }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const page = Number(searchParams.get("page") || 0);

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    const query = new URLSearchParams(searchParams);
    api
      .getCatalog(query.toString())
      .then(setData)
      .catch((e) => setError(e.message));
  }, [searchParams]);

  const onFilterSubmit = (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const next = new URLSearchParams();
    ["categoryId", "minPrice", "maxPrice", "condition", "search"].forEach((key) => {
      const value = form.get(key);
      if (value) {
        next.set(key, value.toString());
      }
    });
    next.set("page", "0");
    setSearchParams(next);
  };

  const movePage = (nextPage) => {
    const next = new URLSearchParams(searchParams);
    next.set("page", String(nextPage));
    setSearchParams(next);
  };

  return (
    <section>
      <div className="hero-panel">
        <div>
          <p className="hero-kicker">Комиссионный магазин</p>
          <h1>Каталог товаров</h1>
          <p className="hero-text">
            Покупайте проверенные товары по выгодной цене. Для заказа добавьте товар в корзину и оформите покупку.
          </p>
        </div>
        <div className="hero-badge">Выгодно каждый день</div>
      </div>
      <form className="grid-form" onSubmit={onFilterSubmit}>
        <select name="categoryId" defaultValue={searchParams.get("categoryId") || ""}>
          <option value="">Все категории</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <input name="search" defaultValue={searchParams.get("search") || ""} placeholder="Поиск" />
        <input name="minPrice" defaultValue={searchParams.get("minPrice") || ""} placeholder="Цена от" />
        <input name="maxPrice" defaultValue={searchParams.get("maxPrice") || ""} placeholder="Цена до" />
        <select name="condition" defaultValue={searchParams.get("condition") || ""}>
          <option value="">Состояние: любое</option>
          <option value="new">Новое</option>
          <option value="used">Б/у</option>
        </select>
        <button type="submit">Фильтр</button>
      </form>

      {error && <p className="error">{error}</p>}

      <div className="cards catalog-grid">
        {data?.content?.map((p) => (
          <article key={p.id} className="card product-card">
            {p.mainImageUrl && <img src={p.mainImageUrl} alt={p.name} />}
            <h3 className="product-title">{p.name}</h3>
            <p className="product-subtitle">{p.categoryName}</p>
            <p className="price">{p.price} ₽</p>
            <Link to={`/product/${p.slug}`} className="card-link product-open">
              Открыть
            </Link>
            {user?.authenticated ? (
              <button
                className="buy-button"
                type="button"
                onClick={async () => {
                  try {
                    await api.addToCart({ productId: p.id, quantity: 1 });
                    alert("Товар добавлен в корзину");
                  } catch (e) {
                    alert(e.message);
                  }
                }}
              >
                Купить
              </button>
            ) : (
              <Link to="/login" className="card-link muted product-open">
                Войти для покупки
              </Link>
            )}
          </article>
        ))}
      </div>

      {data && (
        <div className="pager">
          <button type="button" disabled={page <= 0} onClick={() => movePage(page - 1)}>
            Назад
          </button>
          <span>
            Страница {data.number + 1} из {data.totalPages || 1}
          </span>
          <button
            type="button"
            disabled={data.last}
            onClick={() => movePage(page + 1)}
          >
            Вперед
          </button>
        </div>
      )}
    </section>
  );
}
