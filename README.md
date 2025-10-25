# Text Polisher AI — korektor polskiego tekstu (Web + Android)

Aplikacja do szybkiej korekty błędów ortograficznych i gramatycznych w języku polskim. Zachowuje styl i znaczenie oryginalnego tekstu, a poprawiony wynik możesz łatwo skopiować. Działa w przeglądarce i jako aplikacja mobilna dzięki Capacitor.

## Funkcje
- Korekta tekstu jednym kliknięciem (model `gpt-4o-mini`)
- Wprowadzanie i trwałe zapisywanie klucza API OpenAI (Capacitor Preferences + fallback do `localStorage`)
- Przejrzysty interfejs: pole wejściowe, wynik i przycisk kopiowania
- Pre‑wypełnianie tekstu przez parametr URL `?text=...`
- Obsługa zdarzeń `appUrlOpen` na Androidzie (deep linki po odpowiedniej konfiguracji)

## Wymagania
- Node.js 18+ i npm

## Instalacja i uruchomienie (Web)
```sh
npm i
npm run dev
```
Otwórz `http://localhost:5173/` w przeglądarce.

## Użycie
- Ustaw klucz API: kliknij ikonę `Ustawienia` w aplikacji i wklej swój klucz OpenAI (zapisuje się lokalnie na urządzeniu).
- Wklej lub wpisz tekst, następnie kliknij `Popraw tekst`.
- Skopiuj wynik przyciskiem `Kopiuj`.
- Pre‑wypełnianie przez URL: przykładowo `http://localhost:5173/?text=To%20jest%20przyk%C5%82ad`.

## Build (Web)
```sh
npm run build
npm run preview  # opcjonalny lokalny podgląd produkcyjny
```

## Android (Capacitor)
```sh
npm run build
npx cap sync android
npx cap open android  # otwórz projekt w Android Studio i uruchom na urządzeniu/emulatorze
```
Uwagi:
- Aplikacja nasłuchuje `appUrlOpen`, dzięki czemu może reagować na deep linki (wymagana konfiguracja intent filters po stronie Androida). W web odczytuje parametr `?text=` z URL.
- Klucz API przechowywany jest lokalnie na urządzeniu (Capacitor Preferences oraz fallback do `localStorage`).

## Konfiguracja
- `capacitor.config.ts`: `appId: com.example.app`, `appName: vite_react_shadcn_ts`, `webDir: dist`.
- Nazwę aplikacji możesz zmienić w `capacitor.config.ts` (`appName`).

## Stos technologiczny
- React + Vite + TypeScript
- Tailwind CSS i shadcn‑ui
- Capacitor (`@capacitor/app`, `@capacitor/preferences`)

## Bezpieczeństwo klucza API
Klucz API OpenAI jest zapisywany lokalnie na urządzeniu użytkownika i nie jest wysyłany na żaden backend tej aplikacji. Dbaj o jego poufność (np. nie udostępniaj urządzenia osobom trzecim).
