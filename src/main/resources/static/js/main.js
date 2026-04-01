// Ламбарда — минимальный JS при необходимости (например, подтверждение удаления)
document.querySelectorAll('form[data-confirm]').forEach(function(form) {
    form.addEventListener('submit', function(e) {
        if (!confirm(form.getAttribute('data-confirm'))) e.preventDefault();
    });
});

// Принудительная навигация по категориям (fallback, если браузер/стили ломают обычный клик по ссылке).
document.querySelectorAll('a[data-nav]').forEach(function(link) {
    var navigate = function(e) {
        e.preventDefault();
        var target = link.getAttribute('data-nav');
        if (target) window.location.assign(target);
    };
    link.addEventListener('click', navigate);
    link.addEventListener('auxclick', function(e) {
        if (e.button === 0) navigate(e);
    });
});

// Автокомпактный размер для длинных цен в карточках каталога.
document.querySelectorAll('.product-card .price').forEach(function(priceEl) {
    var text = (priceEl.textContent || "").replace(/\s+/g, " ").trim();
    if (text.length >= 14) {
        priceEl.classList.add("price-compact");
    }
});
