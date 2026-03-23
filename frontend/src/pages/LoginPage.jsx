import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";

export default function LoginPage({ onLoginSuccess }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      await api.login({ email, password });
      await onLoginSuccess();
      navigate("/");
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <section>
      <h1>Вход</h1>
      <div className="auth-wrap">
        <form className="form" onSubmit={submit}>
          <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Пароль"
          />
          <button type="submit">Войти</button>
        </form>
        {error && <p className="error">{error}</p>}
        <p>
          Нет аккаунта? <Link to="/register" className="card-link">Зарегистрироваться</Link>
        </p>
      </div>
    </section>
  );
}
