const EVENT_NAME = "app:toast";
const dedupeStore = new Map();

export function showToast(message, type = "success", options = {}) {
  const dedupeKey = options.dedupeKey || null;
  const dedupeTtlMs = Number(options.dedupeTtlMs || 0);

  if (dedupeKey && dedupeTtlMs > 0) {
    const now = Date.now();
    const lastShownAt = dedupeStore.get(dedupeKey) || 0;
    if (now - lastShownAt < dedupeTtlMs) {
      return;
    }
    dedupeStore.set(dedupeKey, now);
  }

  window.dispatchEvent(
    new CustomEvent(EVENT_NAME, {
      detail: { message, type }
    })
  );
}

export function subscribeToToast(handler) {
  const listener = (event) => handler(event.detail);
  window.addEventListener(EVENT_NAME, listener);
  return () => window.removeEventListener(EVENT_NAME, listener);
}
