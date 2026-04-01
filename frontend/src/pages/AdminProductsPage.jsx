import { useEffect, useState } from "react";
import { api } from "../api";

function toFormData(form, withStatus = false) {
  const fd = new FormData();
  fd.set("name", form.name);
  fd.set("description", form.description || "");
  fd.set("categoryId", form.categoryId);
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
  const [selectedId, setSelectedId] = useState(null);
  const [form, setForm] = useState({
    name: "",
    description: "",
    categoryId: "",
    condition: "USED",
    price: "",
    quantity: 1,
    year: "",
    status: "DRAFT",
    images: null
  });

  const load = async () => {
    try {
      const [items, categoriesList] = await Promise.all([
        api.adminProducts("page=0"),
        api.adminCategories()
      ]);
      setProducts(items || []);
      setCategories(categoriesList || []);
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

  const create = async (event) => {
    event.preventDefault();
    try {
      await api.adminCreateProduct(toFormData(form, false));
      setForm({ ...form, name: "", description: "", price: "", year: "", images: null });
      await load();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error("Ошибка создания товара:", error);
      window.alert(`Не удалось создать товар: ${error.message}`);
    }
  };

  const loadProduct = async (id) => {
    const p = await api.adminProductById(id);
    setSelectedId(id);
    setForm({
      name: p.name,
      description: p.description || "",
      categoryId: p.categoryId,
      condition: p.condition,
      price: p.price,
      quantity: p.quantity,
      year: p.year || "",
      status: p.status,
      images: null
    });
  };

  const update = async (event) => {
    event.preventDefault();
    if (!selectedId) return;
    try {
      await api.adminUpdateProduct(selectedId, toFormData(form, true));
      await load();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error("Ошибка обновления товара:", error);
      window.alert(`Не удалось сохранить товар: ${error.message}`);
    }
  };

  const remove = async (id) => {
    const confirmed = window.confirm("Удалить товар из базы полностью?");
    if (!confirmed) return;
    await api.adminDeleteProduct(id);
    if (selectedId === id) {
      setSelectedId(null);
      setForm({
        name: "",
        description: "",
        categoryId: "",
        condition: "USED",
        price: "",
        quantity: 1,
        year: "",
        status: "DRAFT",
        images: null
      });
    }
    await load();
  };

  return (
    <section>
      <h1>Админ: товары</h1>
      <div className="cards">
        {products.map((p) => (
          <article className="card" key={p.id}>
            <h3>{p.name}</h3>
            <p>{p.status}</p>
            <p>{p.price} ₽</p>
            <button type="button" onClick={() => loadProduct(p.id)}>
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

      <h2>{selectedId ? `Редактирование #${selectedId}` : "Создание товара"}</h2>
      <form className="form" onSubmit={selectedId ? update : create}>
        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Название" />
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          placeholder="Описание"
        />
        <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
          <option value="">Выбери категорию</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <select value={form.condition} onChange={(e) => setForm({ ...form, condition: e.target.value })}>
          <option value="NEW">NEW</option>
          <option value="USED">USED</option>
        </select>
        <input value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} placeholder="Цена" />
        <input
          value={form.quantity}
          onChange={(e) => setForm({ ...form, quantity: e.target.value })}
          placeholder="Количество"
        />
        <input value={form.year} onChange={(e) => setForm({ ...form, year: e.target.value })} placeholder="Год" />
        {selectedId && (
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            <option value="DRAFT">DRAFT</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="ARCHIVED">ARCHIVED</option>
          </select>
        )}
        <input type="file" multiple onChange={(e) => setForm({ ...form, images: e.target.files })} />
        <button type="submit">{selectedId ? "Сохранить" : "Создать"}</button>
      </form>
    </section>
  );
}
