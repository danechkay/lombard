import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";

export default function AccountPage() {
  const [account, setAccount] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.getAccount().then(setAccount).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!account) return <p>Загрузка...</p>;

  return (
    <section>
      <h1>Личный кабинет</h1>
      <div className="panel">
        <p>Имя: {account.fullName}</p>
        <p>Email: {account.email}</p>
        <p>Телефон: {account.phone || "-"}</p>
        <p>Роль: {account.role}</p>
        <Link to="/account/orders" className="cta-link">
          Мои заказы
        </Link>
      </div>
    </section>
  );
}
