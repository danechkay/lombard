import { useEffect, useState } from "react";
import { api } from "../api";

function toFormData(form, withStatus = false) {
  const fd = new FormData();
  fd.set("name", form.name);
  fd.set("description", form.description || "");
  fd.set("categoryId", form.categoryId);
  fd.set("storeId", form.storeId);
  fd.set("condition", form.condition);
  fd.set("price", form.price);
  fd.set("quantity", form.quantity);
  // Год: отправляем только если это корректное положительное число
  const yearNumber = parseInt(form.year, 10);
  if (!Number.isNaN(yearNumber) && yearNumber > 0) {
    fd.set("year", String(yearNumber));
  }
  if (withStatus) fd.set("status", form.status);
  if (form.images?.length) {
    Array.from(form.images).forEach((file) => fd.append("images", file));
  }
  return fd;
}

export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [stores, setStores] = useState([]);

  const [filterCategoryId, setFilterCategoryId] = useState("");
  const [filterNameQuery, setFilterNameQuery] = useState("");

  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create"); // 'create' | 'edit'
  const [selectedId, setSelectedId] = useState(null);
  const [modalLoading, setModalLoading] = useState(false);

  const [form, setForm] = useState({
    name: "",
    description: "",
    categoryId: "",
    storeId: "",
    condition: "USED",
    price: "",
    quantity: 1,
    year: "",
    status: "DRAFT",
    images: null
  });

  const load = async () => {
    try {
      const [items, categoriesList, storesList] = await Promise.all([
        // Тянем больше товаров одним запросом, чтобы фильтры работали по всем существующим товарам.
        api.adminProducts("page=0&size=200"),
        api.adminCategories(),
        api.getStores()
      ]);
      setProducts(items || []);
      setCategories(categoriesList || []);
      setStores(storesList || []);
    } catch (error) {
      // временно выводим ошибку в консоль и в alert,
      // чтобы понять, почему не грузятся товары
      // (403/500/ошибка сети и т.п.)
      // eslint-disable-next-line no-console
      console.error("Ошибка загрузки товаров админки:", error);
      window.alert(`Не удалось загрузить список товаров: ${error.message}`);
    }
  };

  useEffect(() => {
    load();
  }, []);

  useEffect(() => {
    if (!modalOpen) return;
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prevOverflow;
    };
  }, [modalOpen]);

  const statusToRu = (status) => {
    const s = String(status ?? "").toUpperCase();
    if (s === "DRAFT") return "Черновик";
    if (s === "PUBLISHED") return "Опубликован";
    if (s === "ARCHIVED") return "Архив";
    return status ?? "";
  };

  const closeModal = () => {
    setModalOpen(false);
    setModalMode("create");
    setSelectedId(null);
    setModalLoading(false);
  };

  const openCreate = () => {
    setModalMode("create");
    setSelectedId(null);
    setModalLoading(false);
    setForm({
      name: "",
      description: "",
      categoryId: filterCategoryId || "",
      storeId: stores[0]?.id != null ? String(stores[0].id) : "",
      condition: "USED",
      price: "",
      quantity: 1,
      year: "",
      status: "DRAFT",
      images: null
    });
    setModalOpen(true);
  };

  const openEdit = async (id) => {
    // Открываем окно сразу (как “маленькую страницу”), а данные подгружаем внутрь.
    setSelectedId(id);
    setModalMode("edit");
    setModalLoading(true);
    setModalOpen(true);
    try {
      const p = await api.adminProductById(id);
      setForm({
        name: p.name || "",
        description: p.description || "",
        categoryId: p.categoryId ?? "",
        storeId: p.storeId != null ? String(p.storeId) : "",
        condition: p.condition ?? "USED",
        price: p.price ?? "",
        quantity: p.quantity ?? 1,
        year: p.year || "",
        status: p.status ?? "DRAFT",
        images: null
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error("Ошибка загрузки товара для редактирования:", error);
      window.alert(`Не удалось открыть редактирование: ${error.message}`);
      closeModal();
    } finally {
      setModalLoading(false);
    }
  };

  const submit = async (event) => {
    event.preventDefault();
    try {
      if (modalMode === "edit" && selectedId) {
        await api.adminUpdateProduct(selectedId, toFormData(form, true));
      } else {
        await api.adminCreateProduct(toFormData(form, false));
      }
      closeModal();
      await load();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error("Ошибка сохранения товара:", error);
      window.alert(`Не удалось сохранить товар: ${error.message}`);
    }
  };

  const remove = async (id) => {
    const confirmed = window.confirm("Удалить товар из базы полностью?");
    if (!confirmed) return;
    await api.adminDeleteProduct(id);
    if (modalOpen && selectedId === id) closeModal();
    await load();
  };

  const filteredProducts = products.filter((p) => {
    const matchesCategory =
      filterCategoryId === "" ? true : String(p.categoryId ?? "") === String(filterCategoryId);

    const q = filterNameQuery.trim().toLowerCase();
    const matchesName =
      q === "" ? true : String(p.name ?? "").toLowerCase().includes(q);

    return matchesCategory && matchesName;
  });

  return (
    <section className="admin-products-page">
      <h1 className="admin-products-title">Админ: товары</h1>
      <div className="admin-products-layout">
        <aside className="admin-products-sidebar">
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            Создать товар
          </button>

          <div className="filter-card" style={{ marginTop: 14 }}>
            <div className="field">
              <label htmlFor="admin-filter-name" style={{ display: "block", marginBottom: 6, fontWeight: 700 }}>
                Фильтр: имя
              </label>
              <input
                id="admin-filter-name"
                value={filterNameQuery}
                onChange={(e) => setFilterNameQuery(e.target.value)}
                placeholder="Например, iPhone"
                style={{ width: "100%", boxSizing: "border-box" }}
              />
            </div>

            <div className="field" style={{ marginTop: 12 }}>
              <label htmlFor="admin-filter-category" style={{ display: "block", marginBottom: 6, fontWeight: 700 }}>
                Фильтр: категория
              </label>
              <select
                id="admin-filter-category"
                value={filterCategoryId}
                onChange={(e) => setFilterCategoryId(e.target.value)}
              >
                <option value="">Все категории</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </aside>

        <div className="admin-products-main">
          <div className="cards catalog-grid v2 admin-products-cards">
            {filteredProducts.map((p) => (
              <article className="card" key={p.id}>
                <h3>{p.name}</h3>
                <p style={{ fontSize: 13, color: "var(--muted)", margin: "4px 0" }}>{p.storeName || "—"}</p>
                <p>{statusToRu(p.status)}</p>
                <p>{p.price} ₽</p>
                <button type="button" onClick={() => openEdit(p.id)}>
                  Редактировать
                </button>
                <button type="button" onClick={() => api.adminPublishProduct(p.id).then(load)}>
                  Опубликовать
                </button>
                <button type="button" onClick={() => api.adminUnpublishProduct(p.id).then(load)}>
                  Снять с публикации
                </button>
                <button type="button" onClick={() => remove(p.id)}>
                  Удалить
                </button>
              </article>
            ))}
          </div>

          {filteredProducts.length === 0 && <p style={{ marginTop: 16, color: "var(--muted)" }}>Нет товаров</p>}
        </div>
      </div>

      {modalOpen && (
        <div
          className="modal-backdrop"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) closeModal();
          }}
        >
          <div className="modal admin-products-modal" role="dialog" aria-modal="true">
            <div className="modal-header">
              <h2 style={{ margin: 0 }}>
                {modalMode === "edit" ? `Редактирование #${selectedId}` : "Создание товара"}
              </h2>
              <button type="button" className="modal-close" onClick={closeModal} aria-label="Закрыть">
                ×
              </button>
            </div>

            <div className="modal-body">
              {modalLoading ? (
                <p className="muted-text" style={{ margin: 0 }}>
                  Загружаю параметры товара…
                </p>
              ) : (
                <form className="form" onSubmit={submit}>
                <label style={{ fontWeight: 600 }}>Название</label>
                <input
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="Название"
                  required
                />
                <label style={{ fontWeight: 600 }}>Описание</label>
                <textarea
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  placeholder="Описание"
                />
                <label style={{ fontWeight: 600 }}>Категория</label>
                <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} required>
                  <option value="">Выбери категорию</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
                <label style={{ fontWeight: 600 }}>Магазин</label>
                <select value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} required>
                  <option value="">Выбери магазин</option>
                  {stores.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
                <label style={{ fontWeight: 600 }}>Состояние</label>
                <select value={form.condition} onChange={(e) => setForm({ ...form, condition: e.target.value })}>
                  <option value="NEW">Новый</option>
                  <option value="USED">Б/у</option>
                </select>
                <label style={{ fontWeight: 600 }}>Цена (₽)</label>
                <input value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} placeholder="Цена" />
                <label style={{ fontWeight: 600 }}>Количество</label>
                <input
                  value={form.quantity}
                  onChange={(e) => setForm({ ...form, quantity: e.target.value })}
                  placeholder="Количество"
                  required
                />
                <label style={{ fontWeight: 600 }}>Год</label>
                <input value={form.year} onChange={(e) => setForm({ ...form, year: e.target.value })} placeholder="Год" />

                {modalMode === "edit" && (
                  <>
                    <label style={{ fontWeight: 600 }}>Статус</label>
                    <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                      <option value="DRAFT">Черновик</option>
                      <option value="PUBLISHED">Опубликован</option>
                      <option value="ARCHIVED">Архив</option>
                    </select>
                  </>
                )}

                <label style={{ fontWeight: 600 }}>Фото</label>
                <input type="file" multiple onChange={(e) => setForm({ ...form, images: e.target.files })} />

                <div className="modal-actions">
                  <button type="button" className="btn" onClick={closeModal}>
                    Отмена
                  </button>
                  <button type="submit" className="btn btn-primary">
                    {modalMode === "edit" ? "Сохранить" : "Создать"}
                  </button>
                </div>
                </form>
              )}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
