import { useEffect, useState } from "react";
import { api } from "../api";

const roles = ["USER", "MANAGER", "ADMIN"];

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [stores, setStores] = useState([]);
  const [error, setError] = useState("");
  const [searchName, setSearchName] = useState("");
  const [searchEmail, setSearchEmail] = useState("");
  const [searchPhone, setSearchPhone] = useState("");

  const load = async (filters = {}) => {
    const query = new URLSearchParams();
    if (filters.name?.trim()) query.set("name", filters.name.trim());
    if (filters.email?.trim()) query.set("email", filters.email.trim());
    if (filters.phone?.trim()) query.set("phone", filters.phone.trim());
    setError("");
    try {
      const usersResponse = await api.adminUsers(query.toString());
      // Defensive handling: backend can return either array or page-like object.
      const usersList = Array.isArray(usersResponse)
        ? usersResponse
        : Array.isArray(usersResponse?.content)
          ? usersResponse.content
          : [];
      setUsers(usersList);
    } catch (e) {
      setUsers([]);
      setError(e.message || "Не удалось загрузить пользователей");
    }

    try {
      const storesList = await api.getStores();
      setStores(Array.isArray(storesList) ? storesList : []);
    } catch {
      // Stores are secondary for this page; do not block users list.
      setStores([]);
    }
  };

  const updateRole = async (user, nextRole) => {
    const storeId =
      nextRole === "MANAGER" ? user.storeId ?? stores[0]?.id ?? null : null;
    await api.adminUserRole(user.id, nextRole, storeId);
    load({ name: searchName, email: searchEmail, phone: searchPhone });
  };

  useEffect(() => {
    load();
  }, []);

  const applySearch = () => {
    load({ name: searchName, email: searchEmail, phone: searchPhone });
  };

  const clearSearch = () => {
    setSearchName("");
    setSearchEmail("");
    setSearchPhone("");
    load();
  };

  return (
    <section>
      <h1>Админ: пользователи</h1>
      {error ? <p className="error">{error}</p> : null}
      <div className="filter-card" style={{ marginBottom: 14 }}>
        <div className="field">
          <span>Поиск по имени</span>
          <input
            value={searchName}
            onChange={(e) => setSearchName(e.target.value)}
            placeholder="Например: Иван Петров"
          />
        </div>
        <div className="field">
          <span>Поиск по email</span>
          <input
            value={searchEmail}
            onChange={(e) => setSearchEmail(e.target.value)}
            placeholder="Например: user@mail.ru"
          />
        </div>
        <div className="field">
          <span>Поиск по телефону</span>
          <input
            value={searchPhone}
            onChange={(e) => setSearchPhone(e.target.value)}
            placeholder="Например: +7 999 123-45-67"
          />
        </div>
        <div className="row-actions">
          <button type="button" onClick={applySearch}>
            Найти
          </button>
          <button type="button" onClick={clearSearch}>
            Сбросить
          </button>
        </div>
      </div>
      {!error && users.length === 0 ? <p>Пользователи не найдены.</p> : null}
      {users.map((u) => (
        <article className="card" key={u.id}>
          <h3>{u.fullName}</h3>
          <p>{u.email}</p>
          <p>Роль: {u.role}</p>
          <p>Магазин: {u.storeName || "Не назначен"}</p>
          <p>Статус: {u.blocked ? "Заблокирован" : "Активен"}</p>
          <div className="row-actions">
            {u.blocked ? (
              <button
                type="button"
                onClick={() =>
                  api.adminUnblockUser(u.id).then(() => load({ name: searchName, email: searchEmail, phone: searchPhone }))
                }
              >
                Разблокировать
              </button>
            ) : (
              <button
                type="button"
                onClick={() =>
                  api.adminBlockUser(u.id).then(() => load({ name: searchName, email: searchEmail, phone: searchPhone }))
                }
              >
                Заблокировать
              </button>
            )}
            <select defaultValue={u.role} onChange={(e) => updateRole(u, e.target.value)}>
              {roles.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
            {u.role === "MANAGER" ? (
              <select
                value={u.storeId ?? ""}
                onChange={(e) => {
                  const storeId = e.target.value ? Number(e.target.value) : null;
                  api.adminUserRole(u.id, "MANAGER", storeId).then(() =>
                    load({ name: searchName, email: searchEmail, phone: searchPhone })
                  );
                }}
              >
                <option value="">Выберите магазин</option>
                {stores.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            ) : null}
          </div>
        </article>
      ))}
    </section>
  );
}
