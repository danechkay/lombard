const departments = [
  { id: 1, address: "ул. Генерала Сталинграда, 3а", hours: "8:00 - 20:00", phone: "+7 978 988 67 24" },
  { id: 2, address: "ул. Залесская, 117", hours: "9:00 - 20:00", phone: "+7 978 900 67 26" },
  { id: 3, address: "ул. Дм. Ульянова, 2в", hours: "8:00 - 20:00", phone: "+7 978 955 57 13" },
  { id: 4, address: "ул. Ленина/Гагарина, 15/1", hours: "9:00 - 20:00", phone: "+7 978 988 67 28" }
];

export default function DepartmentsPage() {
  return (
    <section>
      <h1>Карта отделений</h1>
      <div className="departments-grid">
        <aside className="panel">
          <input className="search-input" placeholder="Поиск адреса..." />
          <div className="branches-list">
            {departments.map((d) => (
              <article key={d.id} className="branch-item">
                <h3>{d.address}</h3>
                <p>🕓 {d.hours}</p>
                <p>📞 {d.phone}</p>
              </article>
            ))}
          </div>
        </aside>
        <div className="map-panel">
          <iframe
            className="map-frame"
            title="Карта отделений в Симферополе"
            src="https://www.openstreetmap.org/export/embed.html?bbox=34.028%2C44.915%2C34.180%2C45.015&layer=mapnik&marker=44.9521%2C34.1024"
          />
        </div>
      </div>
    </section>
  );
}
