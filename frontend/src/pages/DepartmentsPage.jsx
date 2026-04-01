import { useEffect, useMemo, useRef, useState } from "react";
import { showToast } from "../toast";

const departments = [
  { id: 1, address: "ул. Генерала Сталинграда, 3а", hours: "8:00 - 20:00", phone: "+7 978 988 67 24", coords: [44.9291, 34.0831] },
  { id: 2, address: "ул. Залесская, 117", hours: "9:00 - 20:00", phone: "+7 978 900 67 26", coords: [44.9766, 34.1182] },
  { id: 3, address: "ул. Дм. Ульянова, 2в", hours: "8:00 - 20:00", phone: "+7 978 955 57 13", coords: [44.9368, 34.1026] },
  { id: 4, address: "ул. Ленина/Гагарина, 15/1", hours: "9:00 - 20:00", phone: "+7 978 988 67 28", coords: [44.9547, 34.1037] }
];

const CITY_NAME = "Симферополь";
const MAP_OPEN_URL = "https://yandex.ru/maps/?text=Симферополь%20ломбард";
const YMAPS_API_KEY = "e8d92391-38ce-447a-a62f-3caa85a4dea4";

function loadYandexMapsScript() {
  if (window.ymaps) return Promise.resolve(window.ymaps);
  if (window.__ymapsLoadingPromise) return window.__ymapsLoadingPromise;

  window.__ymapsLoadingPromise = new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = `https://api-maps.yandex.ru/2.1/?apikey=${encodeURIComponent(YMAPS_API_KEY)}&lang=ru_RU`;
    script.async = true;
    script.onload = () => resolve(window.ymaps);
    script.onerror = () => reject(new Error("Не удалось загрузить API Яндекс.Карт"));
    document.body.appendChild(script);
  });

  return window.__ymapsLoadingPromise;
}

export default function DepartmentsPage() {
  const mapContainerRef = useRef(null);
  const mapRef = useRef(null);
  const marksRef = useRef([]);

  const [query, setQuery] = useState("");
  const [mapLoading, setMapLoading] = useState(true);
  const [mapError, setMapError] = useState(false);
  const [mapVersion, setMapVersion] = useState(0);

  const filteredDepartments = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return departments;
    return departments.filter(
      (d) =>
        d.address.toLowerCase().includes(normalized) ||
        d.phone.toLowerCase().includes(normalized) ||
        d.hours.toLowerCase().includes(normalized)
    );
  }, [query]);

  const retryMap = () => {
    setMapLoading(true);
    setMapError(false);
    setMapVersion((prev) => prev + 1);
  };

  useEffect(() => {
    let cancelled = false;

    const initMap = async () => {
      setMapLoading(true);
      setMapError(false);

      try {
        const ymaps = await loadYandexMapsScript();
        await new Promise((resolve) => ymaps.ready(resolve));
        if (cancelled || !mapContainerRef.current) return;

        if (!mapRef.current) {
          mapRef.current = new ymaps.Map(mapContainerRef.current, {
            center: [44.9521, 34.1024],
            zoom: 12,
            controls: ["zoomControl", "fullscreenControl"]
          });
        }

        marksRef.current.forEach((mark) => mapRef.current.geoObjects.remove(mark));
        marksRef.current = [];

        const points = [];
        for (const dep of filteredDepartments) {
          try {
            let coords = dep.coords;
            if (!coords) {
              const searchText = `${CITY_NAME}, ${dep.address}`;
              const geoResult = await ymaps.geocode(searchText, { results: 1 });
              if (cancelled) return;
              const first = geoResult.geoObjects.get(0);
              if (!first) continue;
              coords = first.geometry.getCoordinates();
            }
            points.push(coords);

            const mark = new ymaps.Placemark(
              coords,
              {
                hintContent: dep.address,
                balloonContent: `<b>${dep.address}</b><br/>Режим: ${dep.hours}<br/>Телефон: ${dep.phone}`
              },
              {
                preset: "islands#redIcon"
              }
            );

            mapRef.current.geoObjects.add(mark);
            marksRef.current.push(mark);
          } catch (e) {
            // Не прерываем рендер карты из-за ошибки одного адреса.
          }
        }

        if (points.length > 1) {
          mapRef.current.setBounds(ymaps.util.bounds.fromPoints(points), {
            checkZoomRange: true,
            zoomMargin: 30
          });
        } else if (points.length === 1) {
          mapRef.current.setCenter(points[0], 14, { duration: 250 });
        } else {
          mapRef.current.setCenter([44.9521, 34.1024], 11, { duration: 250 });
        }

        setMapLoading(false);
      } catch (error) {
        if (cancelled) return;
        setMapLoading(false);
        setMapError(true);
        showToast("Не удалось загрузить карту. Показан резервный вариант.", "error", {
          dedupeKey: "map-load-failed",
          dedupeTtlMs: 15000
        });
      }
    };

    initMap();

    return () => {
      cancelled = true;
    };
  }, [mapVersion, filteredDepartments]);

  useEffect(() => {
    return () => {
      if (mapRef.current) {
        mapRef.current.destroy();
        mapRef.current = null;
      }
      marksRef.current = [];
    };
  }, []);

  return (
    <section>
      <h1>Карта отделений</h1>
      <div className="departments-grid">
        <aside className="panel">
          <input
            className="search-input"
            placeholder="Поиск адреса..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <div className="branches-list">
            {filteredDepartments.map((d) => (
              <article key={d.id} className="branch-item">
                <h3>{d.address}</h3>
                <p>🕓 {d.hours}</p>
                <p>📞 {d.phone}</p>
              </article>
            ))}
          </div>
        </aside>
        <div className="map-panel">
          {!mapError && <div key={mapVersion} ref={mapContainerRef} className="map-frame" aria-label="Яндекс карта отделений" />}
          {mapLoading && !mapError && (
            <div className="map-loading">
              <p>Загружаем карту отделений...</p>
            </div>
          )}
          {mapError && (
            <div className="map-fallback">
              <h3>Карта временно недоступна</h3>
              <p>Вы можете открыть карту во внешнем окне или выбрать отделение из списка слева.</p>
              <div className="map-fallback-actions">
                <a className="cta-link" href={MAP_OPEN_URL} target="_blank" rel="noreferrer">
                  Открыть карту
                </a>
                <button type="button" onClick={retryMap}>
                  Повторить
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
