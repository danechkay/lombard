import { Link } from "react-router-dom";

export default function Layout({ user, onLogout, children }) {
  const VkIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 7h2.3c.2 0 .4.1.5.3l1.5 3.2c.3.7.8 1.3 1.4 1.8V7h2v4.2c.6-.1 1.2-.6 1.5-1.2l1.4-2.8c.1-.2.3-.3.5-.3H19l-1.8 3.4c-.4.8-1.1 1.4-1.8 1.9.9.4 1.6 1.1 2.2 1.9L19 17h-2.3c-.2 0-.4-.1-.5-.3l-1.2-2c-.4-.7-1.1-1.3-1.9-1.4V17h-1.6c-2.7 0-5.2-1.6-6.4-4L4 7z" />
    </svg>
  );
  const TgIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M20.7 4.2 3.9 10.7c-.8.3-.8 1.4 0 1.7l4.2 1.3 1.6 4.8c.2.6 1 .8 1.5.3l2.3-2.2 4.4 3.2c.7.5 1.6.1 1.8-.7L22 5.7c.2-1-.6-1.8-1.5-1.5Zm-9.8 9.1-.6 3.3-.9-2.7 8.3-6.8-6.8 6.2Z" />
    </svg>
  );

  return (
    <div className="app">
      <div className="topbar">
        <div className="topbar-inner">
          <span>Крым, Симферополь</span>
          <span className="topbar-right">
            <span className="top-icon"><VkIcon /></span>
            <span className="top-icon"><TgIcon /></span>
            <span>8 800 775 82 86</span>
          </span>
        </div>
      </div>
      <header className="header">
        <div className="nav">
          <Link to="/" className="brand">
            <span className="brand-badge">K</span>
            <span>Ламбарда</span>
          </Link>
          <nav className="nav-links">
            <Link to="/">Каталог</Link>
            <Link to="/departments">Отделения</Link>
            {user?.authenticated && <Link to="/cart">Корзина</Link>}
            {user?.authenticated && <Link to="/account">Кабинет</Link>}
            {(user?.role === "ADMIN" || user?.role === "MANAGER") && <Link to="/admin">Админка</Link>}
          </nav>
          <div className="nav-actions">
            {user?.authenticated ? (
              <button type="button" onClick={onLogout} className="link-button">
                Выйти
              </button>
            ) : (
              <>
                <Link to="/login">Вход</Link>
                <Link to="/register" className="cta-link">
                  Регистрация
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="container">{children}</main>
    </div>
  );
}
