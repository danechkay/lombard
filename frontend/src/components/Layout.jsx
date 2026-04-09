import { useEffect, useState } from "react";
import { Link, NavLink, useLocation } from "react-router-dom";

export default function Layout({ user, onLogout, theme, onToggleTheme, children }) {
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

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

  const canViewAdmin = user?.role === "ADMIN" || user?.role === "MANAGER";

  return (
    <div className="app">
      <div className="topbar">
        <div className="topbar-inner">
          <span>Симферополь</span>
          <span className="topbar-right">
            <span className="top-icon"><VkIcon /></span>
            <span className="top-icon"><TgIcon /></span>
            <span>8 800 775-82-86</span>
          </span>
        </div>
      </div>
      <header className="header">
        <div className="nav">
          <Link to="/" className="brand">
            <span className="brand-badge">K</span>
            <span>Ломбард</span>
          </Link>

          <button
            type="button"
            className="mobile-menu-toggle"
            aria-expanded={mobileMenuOpen}
            aria-label="Открыть меню"
            onClick={() => setMobileMenuOpen((v) => !v)}
          >
            ☰
          </button>

          <nav className={`nav-links ${mobileMenuOpen ? "is-open" : ""}`}>
            <NavLink to="/">Главная</NavLink>
            <NavLink to="/catalog">Каталог</NavLink>
            <NavLink to="/discounts">Акции</NavLink>
            <NavLink to="/valuation">Оценка</NavLink>
            <NavLink to="/departments">Отделения</NavLink>
            {user?.authenticated && <NavLink to="/cart">Корзина</NavLink>}
            {user?.authenticated && <NavLink to="/account">Кабинет</NavLink>}
            {canViewAdmin && <NavLink to="/admin">Админка</NavLink>}
          </nav>
          <div className={`nav-actions ${mobileMenuOpen ? "is-open" : ""}`}>
            <button type="button" className="theme-toggle" onClick={onToggleTheme}>
              {theme === "light" ? "Тёмная тема" : "Светлая тема"}
            </button>
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

      <nav className="mobile-bottom-nav">
        <NavLink to="/">Главная</NavLink>
        <NavLink to="/catalog">Каталог</NavLink>
        <NavLink to="/discounts">Акции</NavLink>
        {user?.authenticated ? <NavLink to="/cart">Корзина</NavLink> : <NavLink to="/login">Вход</NavLink>}
        {user?.authenticated ? <NavLink to="/account">Кабинет</NavLink> : <NavLink to="/register">Аккаунт</NavLink>}
      </nav>
    </div>
  );
}
