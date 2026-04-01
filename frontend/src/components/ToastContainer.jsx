import { useEffect, useState } from "react";
import { subscribeToToast } from "../toast";

export default function ToastContainer() {
  const [items, setItems] = useState([]);

  useEffect(() => {
    return subscribeToToast((toast) => {
      const id = crypto.randomUUID();
      setItems((prev) => [...prev, { id, ...toast }]);
      setTimeout(() => {
        setItems((prev) => prev.filter((item) => item.id !== id));
      }, 3000);
    });
  }, []);

  return (
    <div className="toast-stack" aria-live="polite">
      {items.map((item) => (
        <div key={item.id} className={`toast ${item.type === "error" ? "toast-error" : "toast-success"}`}>
          {item.message}
        </div>
      ))}
    </div>
  );
}
