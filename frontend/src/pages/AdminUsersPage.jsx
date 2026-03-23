import { useEffect, useState } from "react";
import { api } from "../api";

const roles = ["USER", "MANAGER", "ADMIN"];

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);

  const load = () => api.adminUsers().then(setUsers);

  useEffect(() => {
    load();
  }, []);

  return (
    <section>
      <h1>Админ: пользователи</h1>
      {users.map((u) => (
        <article className="card" key={u.id}>
          <h3>{u.fullName}</h3>
          <p>{u.email}</p>
          <p>Роль: {u.role}</p>
          <p>Статус: {u.blocked ? "Заблокирован" : "Активен"}</p>
          <div className="row-actions">
            {u.blocked ? (
              <button type="button" onClick={() => api.adminUnblockUser(u.id).then(load)}>
                Разблокировать
              </button>
            ) : (
              <button type="button" onClick={() => api.adminBlockUser(u.id).then(load)}>
                Заблокировать
              </button>
            )}
            <select defaultValue={u.role} onChange={(e) => api.adminUserRole(u.id, e.target.value).then(load)}>
              {roles.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>
        </article>
      ))}
    </section>
  );
}
