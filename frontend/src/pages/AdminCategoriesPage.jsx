import { useEffect, useState } from "react";
import { api } from "../api";

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ name: "", parentId: "", sortOrder: 0 });
  const [editingId, setEditingId] = useState(null);

  const load = () => api.adminCategories().then(setCategories);

  useEffect(() => {
    load();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    const payload = {
      name: form.name,
      parentId: form.parentId ? Number(form.parentId) : null,
      sortOrder: Number(form.sortOrder || 0)
    };
    if (editingId) await api.adminUpdateCategory(editingId, payload);
    else await api.adminCreateCategory(payload);
    setForm({ name: "", parentId: "", sortOrder: 0 });
    setEditingId(null);
    await load();
  };

  return (
    <section>
      <h1>Админ: категории</h1>
      <div className="cards">
        {categories.map((c) => (
          <article className="card" key={c.id}>
            <h3>{c.name}</h3>
            <p>slug: {c.slug}</p>
            <p>sort: {c.sortOrder}</p>
            <button type="button" onClick={() => {
              setEditingId(c.id);
              setForm({ name: c.name, parentId: c.parentId || "", sortOrder: c.sortOrder });
            }}>
              Редактировать
            </button>
            <button type="button" onClick={() => api.adminDeleteCategory(c.id).then(load)}>
              Удалить
            </button>
          </article>
        ))}
      </div>

      <h2>{editingId ? "Изменить категорию" : "Создать категорию"}</h2>
      <form className="form" onSubmit={submit}>
        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Название" />
        <select value={form.parentId} onChange={(e) => setForm({ ...form, parentId: e.target.value })}>
          <option value="">Без родителя</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <input
          value={form.sortOrder}
          onChange={(e) => setForm({ ...form, sortOrder: e.target.value })}
          placeholder="Порядок"
        />
        <button type="submit">{editingId ? "Сохранить" : "Создать"}</button>
      </form>
    </section>
  );
}
