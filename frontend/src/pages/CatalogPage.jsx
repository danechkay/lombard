import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../api";
import { showToast } from "../toast";

export default function CatalogPage({ user }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const page = Number(searchParams.get("page") || 0);
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);
  const [activeRootId, setActiveRootId] = useState(null);
  const selectedCategoryId = searchParams.get("categoryId") || "";
  const categoryQuery = searchParams.get("categorySearch") || "";

  useEffect(() => {
    setMobileFiltersOpen(false);
  }, [searchParams]);

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  const categoryTree = useMemo(() => {
    const byParent = new Map();
    categories.forEach((c) => {
      const parentId = c.parentId ?? 0;
      if (!byParent.has(parentId)) byParent.set(parentId, []);
      byParent.get(parentId).push(c);
    });
    byParent.forEach((list) =>
      list.sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0) || Number(a.id) - Number(b.id))
    );
    return byParent;
  }, [categories]);

  const visibleRootCategories = useMemo(() => {
    const roots = categoryTree.get(0) || [];
    if (!categoryQuery.trim()) return roots;
    const q = categoryQuery.toLowerCase();
    return roots.filter((root) => {
      if (root.name.toLowerCase().includes(q)) return true;
      const children = categoryTree.get(root.id) || [];
      return children.some((ch) => ch.name.toLowerCase().includes(q));
    });
  }, [categoryTree, categoryQuery]);

  useEffect(() => {
    if (!visibleRootCategories.length) {
      setActiveRootId(null);
      return;
    }
    const selectedNum = selectedCategoryId ? Number(selectedCategoryId) : null;
    const selectedCategory = selectedNum ? categories.find((c) => Number(c.id) === selectedNum) : null;
    const preferredRootId = selectedCategory?.parentId ? Number(selectedCategory.parentId) : selectedCategory?.id;
    if (preferredRootId && visibleRootCategories.some((r) => Number(r.id) === Number(preferredRootId))) {
      setActiveRootId(Number(preferredRootId));
      return;
    }
    if (!activeRootId || !visibleRootCategories.some((r) => Number(r.id) === Number(activeRootId))) {
      setActiveRootId(Number(visibleRootCategories[0].id));
    }
  }, [visibleRootCategories, selectedCategoryId, categories, activeRootId]);

  const activeRootCategory = useMemo(() => {
    if (!visibleRootCategories.length) return null;
    return visibleRootCategories.find((r) => Number(r.id) === Number(activeRootId)) || visibleRootCategories[0];
  }, [visibleRootCategories, activeRootId]);

  const activeRootChildren = useMemo(() => {
    if (!activeRootCategory) return [];
    const children = categoryTree.get(activeRootCategory.id) || [];
    if (!categoryQuery.trim()) return children;
    const q = categoryQuery.toLowerCase();
    return children.filter((c) => c.name.toLowerCase().includes(q) || activeRootCategory.name.toLowerCase().includes(q));
  }, [activeRootCategory, categoryTree, categoryQuery]);

  useEffect(() => {
    const query = new URLSearchParams(searchParams);
    api
      .getCatalog(query.toString())
      .then(setData)
      .catch((e) => setError(e.message));
  }, [searchParams, categories]);

  const onFilterSubmit = (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const next = new URLSearchParams();
    ["categoryId", "categorySearch", "minPrice", "maxPrice", "condition", "search"].forEach((key) => {
      const value = form.get(key);
      if (value) {
        next.set(key, value.toString());
      }
    });
    next.set("page", "0");
    setSearchParams(next);
  };

  const clearAll = () => {
    setSearchParams(new URLSearchParams({ page: "0" }));
  };

  const movePage = (nextPage) => {
    const next = new URLSearchParams(searchParams);
    next.set("page", String(nextPage));
    setSearchParams(next);
  };

  const formatPrice = (value) => {
    const num = Number(value);
    if (!Number.isFinite(num)) return `${value} ₽`;
    return `${new Intl.NumberFormat("ru-RU", { maximumFractionDigits: 0 }).format(num)} ₽`;
  };

  return (
    <section className="catalog-page">
      <div className="catalog-head">
        <div>
          <h1>Каталог</h1>
          <p className="muted-text catalog-subtitle">
            Товары в центре, ровная сетка 3 × 5.
          </p>
        </div>
        <div className="catalog-head-actions">
          <button type="button" className="btn btn-sm mobile-filter-open" onClick={() => setMobileFiltersOpen(true)}>
            Фильтры
          </button>
          <Link to="/valuation" className="btn btn-ghost btn-sm">
            Оценка онлайн
          </Link>
        </div>
      </div>

      <div className="catalog-layout">
        <aside className={`catalog-filters ${mobileFiltersOpen ? "is-open" : ""}`}>
          <form className="filter-card" onSubmit={onFilterSubmit}>
            <div className="mobile-filters-head">
              <strong>Фильтры</strong>
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => setMobileFiltersOpen(false)}>
                Закрыть
              </button>
            </div>
            <div className="filter-title">Фильтры</div>
            <label className="field">
              <span>Поиск категорий</span>
              <input name="categorySearch" defaultValue={categoryQuery} placeholder="Например: телефоны, ноутбуки..." />
            </label>

            <div className="avito-tree">
              <div className="avito-tree-roots">
                <button
                  type="button"
                  className={`avito-root-item ${selectedCategoryId ? "" : "is-selected"}`}
                  onClick={() => {
                    const next = new URLSearchParams(searchParams);
                    next.delete("categoryId");
                    next.set("page", "0");
                    setSearchParams(next);
                  }}
                >
                  <span>Все товары</span>
                </button>
                {visibleRootCategories.map((root) => (
                  <button
                    key={root.id}
                    type="button"
                    className={`avito-root-item ${Number(activeRootCategory?.id) === Number(root.id) ? "is-active" : ""}`}
                    onClick={() => {
                      setActiveRootId(Number(root.id));
                    }}
                  >
                    <span>{root.name}</span>
                    <span className="avito-root-arrow">›</span>
                  </button>
                ))}
              </div>

              <div className="avito-tree-content">
                {activeRootCategory ? (
                  <>
                    <div className="avito-tree-title">{activeRootCategory.name}</div>
                    <button
                      type="button"
                      className={`avito-sub-item top ${String(activeRootCategory.id) === String(selectedCategoryId) ? "is-selected" : ""}`}
                      onClick={() => {
                        const next = new URLSearchParams(searchParams);
                        next.set("categoryId", String(activeRootCategory.id));
                        next.set("page", "0");
                        setSearchParams(next);
                      }}
                    >
                      Все в категории
                    </button>
                    <div className="avito-sub-grid">
                      {activeRootChildren.map((child) => (
                        <button
                          key={child.id}
                          type="button"
                          className={`avito-sub-item ${String(child.id) === String(selectedCategoryId) ? "is-selected" : ""}`}
                          onClick={() => {
                            const next = new URLSearchParams(searchParams);
                            next.set("categoryId", String(child.id));
                            next.set("page", "0");
                            setSearchParams(next);
                          }}
                        >
                          {child.name}
                        </button>
                      ))}
                    </div>
                  </>
                ) : (
                  <div className="muted-text">Нет категорий</div>
                )}
              </div>
              <input type="hidden" name="categoryId" value={selectedCategoryId} />
            </div>
            <label className="field">
              <span>Поиск товара</span>
              <input
                name="search"
                defaultValue={searchParams.get("search") || ""}
                placeholder="Например: iPhone, MacBook, Xbox..."
              />
            </label>
            <label className="field">
              <span>Состояние</span>
              <select name="condition" defaultValue={searchParams.get("condition") || ""}>
                <option value="">Любое</option>
                <option value="new">Новое</option>
                <option value="used">Б/у</option>
              </select>
            </label>
            <div className="field-grid">
              <label className="field">
                <span>Цена от</span>
                <input
                  name="minPrice"
                  inputMode="numeric"
                  defaultValue={searchParams.get("minPrice") || ""}
                  placeholder="0"
                />
              </label>
              <label className="field">
                <span>Цена до</span>
                <input
                  name="maxPrice"
                  inputMode="numeric"
                  defaultValue={searchParams.get("maxPrice") || ""}
                  placeholder="∞"
                />
              </label>
            </div>
            <div className="filter-actions">
              <button
                type="submit"
                onClick={() => {
                  setMobileFiltersOpen(false);
                }}
              >
                Применить
              </button>
              <button type="button" className="btn btn-ghost" onClick={clearAll}>
                Сбросить
              </button>
            </div>
          </form>
        </aside>
        {mobileFiltersOpen && <div className="mobile-filters-backdrop" onClick={() => setMobileFiltersOpen(false)} />}

        <div className="catalog-main">
          {error && <p className="error">{error}</p>}

          <div className="cards catalog-grid v2">
            {!data &&
              Array.from({ length: 15 }).map((_, idx) => (
                <article key={`skeleton-${idx}`} className="card product-card skeleton-card">
                  <div className="skeleton skeleton-image" />
                  <div className="skeleton skeleton-line" />
                  <div className="skeleton skeleton-line short" />
                  <div className="skeleton skeleton-line medium" />
                </article>
              ))}
            {data?.content?.map((p) => (
              <article key={p.id} className="card product-card product-card-compact">
                <Link to={`/product/${p.slug}`} className="product-cover">
                  {p.mainImageUrl ? <img src={p.mainImageUrl} alt={p.name} /> : <div className="img-fallback" />}
                </Link>
                <div className="product-body">
                  <h3 className="product-title">{p.name}</h3>
                  <p className="product-subtitle">{p.categoryName}</p>
                  <div className="product-bottom">
                    <div className="price">{formatPrice(p.price)}</div>
                    <Link to={`/product/${p.slug}`} className="btn btn-sm btn-ghost">
                      Открыть
                    </Link>
                  </div>
                  {user?.authenticated ? (
                    <button
                      className="btn btn-primary btn-sm"
                      type="button"
                      onClick={async () => {
                        try {
                          await api.addToCart({ productId: p.id, quantity: 1 });
                          showToast("Товар добавлен в корзину");
                        } catch (e) {
                          showToast(e.message, "error");
                        }
                      }}
                    >
                      В корзину
                    </button>
                  ) : (
                    <Link to="/login" className="card-link muted">
                      Войти для покупки
                    </Link>
                  )}
                </div>
              </article>
            ))}
          </div>

          {data && (
            <div className="pager v2">
              <button type="button" className="btn btn-ghost" disabled={page <= 0} onClick={() => movePage(page - 1)}>
                Назад
              </button>
              <span className="muted-text">
                Страница {data.number + 1} из {data.totalPages || 1}
              </span>
              <button
                type="button"
                className="btn btn-ghost"
                disabled={data.last}
                onClick={() => movePage(page + 1)}
              >
                Вперед
              </button>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
