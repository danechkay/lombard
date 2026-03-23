import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";

export default function RegisterPage() {
  const [form, setForm] = useState({
    email: "",
    password: "",
    fullName: "",
    phone: ""
  });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      const result = await api.register(form);
      if (result.error) {
        setError(result.error);
        return;
      }
      alert(result.message || "Регистрация успешна");
      navigate("/login");
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <section>
      <h1>Регистрация</h1>
      <div className="auth-wrap">
        <form className="form" onSubmit={submit}>
          <input
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            placeholder="Email"
          />
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            placeholder="Пароль"
          />
          <input
            value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            placeholder="ФИО"
          />
          <input
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
            placeholder="Телефон"
          />
          <button type="submit">Создать аккаунт</button>
        </form>
        {error && <p className="error">{error}</p>}
        <p>
          Уже есть аккаунт? <Link to="/login" className="card-link">Войти</Link>
        </p>
      </div>
    </section>
  );
}
