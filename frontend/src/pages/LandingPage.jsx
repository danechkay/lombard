import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";
import { showToast } from "../toast";

const services = [
  {
    id: "loan",
    title: "Заём под залог",
    text: "Оценка за 5 минут, понятные условия, без лишних документов.",
    href: "/valuation"
  },
  {
    id: "buyout",
    title: "Скупка",
    text: "Предложим рыночную цену и оформим в день обращения.",
    href: "/valuation"
  },
  {
    id: "catalog",
    title: "Каталог товаров",
    text: "Золото, техника и электроника — смотрите наличие и цены онлайн.",
    href: "/catalog"
  }
];

const steps = [
  { id: "1", title: "Принесите или отправьте фото", text: "Подскажем стоимость и условия заранее." },
  { id: "2", title: "Оценка и договор", text: "Оформление по документу, быстро и прозрачно." },
  { id: "3", title: "Получите деньги", text: "Наличными или на карту — как вам удобнее." }
];

function formatPrice(value) {
  return new Intl.NumberFormat("ru-RU").format(value);
}

export default function LandingPage() {
  const [popular, setPopular] = useState(null);
  const [amount, setAmount] = useState(120000);
  const [days, setDays] = useState(21);

  useEffect(() => {
    api
      .getCatalog("page=0")
      .then((res) => setPopular(res?.content?.slice(0, 8) || []))
      .catch(() => setPopular([]));
  }, []);

  const estimate = useMemo(() => {
    const dailyRate = days <= 30 ? 0.0025 : 0.0019;
    const fee = Math.round(amount * dailyRate * days);
    return { fee, total: amount + fee };
  }, [amount, days]);

  const handleSubmit = (event) => {
    event.preventDefault();
    showToast("Спасибо! Мы свяжемся с вами в ближайшее время.");
  };

  return (
    <div className="landing-page landing-v2">
      <section className="hero-v2">
        <div className="hero-v2-inner">
          <p className="hero-kicker">Симферополь • Ломбард «Капитал»</p>
          <h1>Деньги под залог — быстро и спокойно</h1>
          <p className="hero-text">
            Оценка за несколько минут, прозрачный расчёт, безопасное хранение. Каталог товаров —
            онлайн 24/7.
          </p>
          <div className="hero-pills">
            <span className="pill">Оценка за 5 минут</span>
            <span className="pill">До 80% от стоимости</span>
            <span className="pill">Хранение и страхование</span>
          </div>
          <div className="landing-actions">
            <a className="btn btn-primary" href="#calc">
              Рассчитать
            </a>
            <Link to="/catalog" className="btn btn-ghost">
              Каталог
            </Link>
          </div>
        </div>
        <div className="hero-v2-aside panel-soft">
          <div className="mini-card">
            <div className="mini-card-title">Быстрая заявка</div>
            <div className="mini-card-text">Оставьте контакты — перезвоним и подскажем условия.</div>
            <form className="mini-form" onSubmit={handleSubmit}>
              <input name="phone" placeholder="Телефон" inputMode="tel" />
              <button type="submit">Позвоните мне</button>
            </form>
            <div className="mini-hint">Без спама. Только по заявке.</div>
          </div>
        </div>
      </section>

      <section className="landing-section">
        <div className="section-head compact">
          <h2>Чем можем помочь</h2>
          <p>Коротко и по делу — выберите нужный сценарий.</p>
        </div>
        <div className="cards service-grid v2">
          {services.map((service) => (
            <Link key={service.id} to={service.href} className="service-card-link">
              <article className="card service-card v2">
                <h3>{service.title}</h3>
                <p>{service.text}</p>
              </article>
            </Link>
          ))}
        </div>
      </section>

      <section className="landing-section">
        <div className="section-head compact">
          <h2>Популярное из каталога</h2>
          <p>Аккуратная подборка — без лишнего текста.</p>
        </div>
        <div className="product-strip">
          {!popular &&
            Array.from({ length: 4 }).map((_, idx) => (
              <div key={`s-${idx}`} className="card product-card skeleton-card">
                <div className="skeleton skeleton-image" />
                <div className="skeleton skeleton-line" />
                <div className="skeleton skeleton-line short" />
              </div>
            ))}
          {popular?.map((p) => (
            <article key={p.id} className="card product-card product-card-compact">
              <Link to={`/product/${p.slug}`} className="product-cover">
                {p.mainImageUrl ? <img src={p.mainImageUrl} alt={p.name} /> : <div className="img-fallback" />}
              </Link>
              <div className="product-body">
                <h3 className="product-title">{p.name}</h3>
                <p className="product-subtitle">{p.categoryName}</p>
                <div className="product-bottom">
                  <div className="price">{formatPrice(p.price)} ₽</div>
                  <Link to={`/product/${p.slug}`} className="btn btn-sm btn-ghost">
                    Смотреть
                  </Link>
                </div>
              </div>
            </article>
          ))}
        </div>
        <div className="section-actions">
          <Link to="/catalog" className="btn btn-ghost">
            Весь каталог
          </Link>
        </div>
      </section>

      <section className="landing-section">
        <div className="section-head compact">
          <h2>Как это работает</h2>
          <p>Три простых шага — и вы с деньгами.</p>
        </div>
        <div className="cards steps-grid">
          {steps.map((s) => (
            <article key={s.id} className="card step-card">
              <div className="step-number">{s.id}</div>
              <h3>{s.title}</h3>
              <p className="muted-text">{s.text}</p>
            </article>
          ))}
        </div>
      </section>

      <section id="calc" className="landing-section">
        <div className="section-head compact">
          <h2>Калькулятор</h2>
          <p>Предварительный расчёт — итоговые условия подтвердим при оценке.</p>
        </div>
        <div className="calc-row">
          <form className="panel calculator v2" onSubmit={handleSubmit}>
            <label>
              Сумма займа: <strong>{formatPrice(amount)} ₽</strong>
              <input
                type="range"
                min="10000"
                max="500000"
                step="1000"
                value={amount}
                onChange={(event) => setAmount(Number(event.target.value))}
              />
            </label>
            <label>
              Срок: <strong>{days} дней</strong>
              <input
                type="range"
                min="7"
                max="90"
                step="1"
                value={days}
                onChange={(event) => setDays(Number(event.target.value))}
              />
            </label>
            <div className="calculator-result">
              <p>Проценты: {formatPrice(estimate.fee)} ₽</p>
              <p>К возврату: {formatPrice(estimate.total)} ₽</p>
            </div>
            <button type="submit">Оставить заявку</button>
          </form>
          <div className="panel-soft calc-aside">
            <div className="calc-note">
              <div className="mini-card-title">Что влияет на сумму</div>
              <ul className="clean-list">
                <li>Состояние и комплектность</li>
                <li>Документы (если есть)</li>
                <li>Спрос на модель / металл</li>
              </ul>
              <Link to="/valuation" className="btn btn-ghost btn-sm">
                Оценка онлайн
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
