import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";

export default function CartPage() {
  const [cart, setCart] = useState(null);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      const data = await api.getCart();
      setCart(data);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!cart) return <p>Загрузка...</p>;

  return (
    <section>
      <h1>Корзина</h1>
      {cart.items.length === 0 ? (
        <div className="panel cart-empty">
          <p className="cart-empty-title">Корзина сейчас пуста</p>
          <p className="cart-empty-text">
            Добавьте понравившиеся товары из каталога, и они появятся здесь.
          </p>
          <Link to="/catalog" className="btn btn-sm cart-empty-link">
            Перейти в каталог
          </Link>
        </div>
      ) : (
        <>
          <div className="panel">
            {cart.items.map((item) => (
              <article key={item.id} className="cart-row">
                <div>
                  <h3>{item.productName}</h3>
                  <p>{item.price} ₽ x {item.quantity}</p>
                </div>
                <div className="row-actions">
                  <button
                    type="button"
                    onClick={async () => {
                      await api.updateCart({ cartItemId: item.id, quantity: item.quantity + 1 });
                      await load();
                    }}
                  >
                    +
                  </button>
                  <button
                    type="button"
                    onClick={async () => {
                      await api.updateCart({ cartItemId: item.id, quantity: item.quantity - 1 });
                      await load();
                    }}
                  >
                    -
                  </button>
                  <button
                    type="button"
                    onClick={async () => {
                      await api.removeFromCart({ productId: item.productId });
                      await load();
                    }}
                  >
                    Удалить
                  </button>
                </div>
              </article>
            ))}
            <div className="checkout-bar">
              <h3>Итого: {cart.total} ₽</h3>
              <Link to="/checkout" className="cta-link">
                Оформить заказ
              </Link>
            </div>
          </div>
        </>
      )}
    </section>
  );
}
