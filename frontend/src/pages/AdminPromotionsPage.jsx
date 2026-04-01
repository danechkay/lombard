import { useEffect, useState } from "react";
import { api } from "../api";
import { showToast } from "../toast";

const initialForm = {
  title: "",
  subtitle: "",
  description: "",
  buttonText: "Подробнее",
  imageUrl: "",
  active: true,
  sortOrder: 0
};

export default function AdminPromotionsPage() {
  const [items, setItems] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(initialForm);

  const load = () => api.adminPromotions().then(setItems);

  useEffect(() => {
    load();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    if (editingId) {
      await api.adminUpdatePromotion(editingId, { ...form, sortOrder: Number(form.sortOrder) });
      showToast("Акция обновлена");
    } else {
      await api.adminCreatePromotion({ ...form, sortOrder: Number(form.sortOrder) });
      showToast("Акция создана");
    }
    setEditingId(null);
    setForm(initialForm);
    await load();
  };

  return (
    <section>
      <h1>Админ: акции</h1>
      <div className="cards">
        {items.map((item) => (
          <article className="card" key={item.id}>
            <h3>{item.title}</h3>
            <p>{item.subtitle}</p>
            <p>Активна: {item.active ? "Да" : "Нет"}</p>
            <div className="row-actions">
              <button
                type="button"
                onClick={() => {
                  setEditingId(item.id);
                  setForm({
                    title: item.title || "",
                    subtitle: item.subtitle || "",
                    description: item.description || "",
                    buttonText: item.buttonText || "Подробнее",
                    imageUrl: item.imageUrl || "",
                    active: item.active,
                    sortOrder: item.sortOrder ?? 0
                  });
                }}
              >
                Редактировать
              </button>
              <button
                type="button"
                onClick={async () => {
                  await api.adminDeletePromotion(item.id);
                  showToast("Акция удалена");
                  await load();
                }}
              >
                Удалить
              </button>
            </div>
          </article>
        ))}
      </div>

      <h2>{editingId ? "Редактировать акцию" : "Создать акцию"}</h2>
      <form className="form" onSubmit={submit}>
        <input
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
          placeholder="Заголовок"
          required
        />
        <input
          value={form.subtitle}
          onChange={(e) => setForm({ ...form, subtitle: e.target.value })}
          placeholder="Подзаголовок"
        />
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          placeholder="Описание"
        />
        <input
          value={form.buttonText}
          onChange={(e) => setForm({ ...form, buttonText: e.target.value })}
          placeholder="Текст кнопки"
        />
        <input
          value={form.imageUrl}
          onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
          placeholder="URL картинки"
        />
        <input
          type="number"
          value={form.sortOrder}
          onChange={(e) => setForm({ ...form, sortOrder: e.target.value })}
          placeholder="Порядок"
        />
        <label>
          <input
            type="checkbox"
            checked={form.active}
            onChange={(e) => setForm({ ...form, active: e.target.checked })}
          />
          Активна
        </label>
        <button type="submit">{editingId ? "Сохранить" : "Создать"}</button>
      </form>
    </section>
  );
}
