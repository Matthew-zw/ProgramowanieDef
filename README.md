# System Zarządzania Projektami

Aplikacja webowa do zarządzania projektami, zbudowana w oparciu o framework **Spring Boot**. Aplikacja umożliwia zarządzanie projektami, zadaniami oraz użytkownikami w oparciu o rozbudowany system ról i uprawnień. Zaimplementowano również mechanizmy bezpieczeństwa, w tym uwierzytelnianie dwuskładnikowe (2FA).

---

## 🧠 Główne Funkcjonalności

*   **System Rejestracji i Logowania:** Użytkownicy mogą samodzielnie tworzyć konta i logować się do systemu.
*   **System Ról i Uprawnień:** Dostęp do poszczególnych funkcji aplikacji jest ściśle kontrolowany przez role:
   *   `ROLE_ADMIN` - Zarządzanie użytkownikami i ich rolami.
   *   `ROLE_PROJECT_MANAGER` - Tworzenie, edycja i usuwanie projektów, przypisywanie użytkowników do projektów.
   *   `ROLE_EMPLOYEE` - Przeglądanie przypisanych projektów i zarządzanie zadaniami.
*   **Zarządzanie Projektami:** Tworzenie projektów z nazwą, opisem oraz datami rozpoczęcia i zakończenia.
*   **Zarządzanie Zadaniami:** Możliwość dodawania, edytowania i usuwania zadań w ramach konkretnych projektów. Każde zadanie posiada tytuł, opis, status (TODO, IN_PROGRESS, etc.) oraz termin wykonania.
*   **Przypisywanie Użytkowników:** Project Manager może przypisywać i odpinać użytkowników (pracowników) od projektów.
*   **Uwierzytelnianie Dwuskładnikowe (2FA):** Użytkownicy mogą włączyć dodatkowe zabezpieczenie swojego konta za pomocą aplikacji typu Google Authenticator lub Authy (mechanizm TOTP).
*   **Panel Administracyjny:** Dedykowany widok dla administratora do zarządzania wszystkimi użytkownikami w systemie.

---

## 🛠️ Technologie i Narzędzia

*   **Backend:**
   *   Java 17
   *   Spring Boot 3
   *   Spring Security (konfiguracja ról, 2FA, ochrona endpointów)
   *   Spring Data JPA & Hibernate (warstwa dostępu do danych)
   *   Lombok
*   **Frontend:**
   *   Thymeleaf (silnik szablonów po stronie serwera)
   *   HTML5
   *   CSS3
*   **Baza Danych:**
   *   H2 Database (wbudowana, do celów deweloperskich)
   *   Możliwość łatwej konfiguracji z inną bazą danych (np. PostgreSQL, MySQL) w pliku `application.properties`.
*   **Narzędzia Budowania:**
   *   Maven

---

## 🚀 Uruchamianie Aplikacji

### Wymagania:
1.  **Java JDK** w wersji 17 lub nowszej.
2.  **Apache Maven**.
3.  (Opcjonalnie) IDE, np. IntelliJ IDEA lub VS Code z rozszerzeniami do Javy.

### Kroki do uruchomienia:

1.  Sklonuj repozytorium na swój lokalny dysk:
    ```bash
    git clone <adres-repozytorium>
    ```

2.  Przejdź do głównego katalogu projektu:
    ```bash
    cd ProgramowanieDef/
    ```

3.  Uruchom aplikację za pomocą wtyczki Spring Boot dla Mavena:
    ```bash
    mvn spring-boot:run
    ```
    Aplikacja uruchomi się z wbudowaną bazą danych H2, więc nie jest wymagana żadna dodatkowa konfiguracja.

4.  Po pomyślnym uruchomieniu, aplikacja będzie dostępna w przeglądarce pod adresem:
    [**http://localhost:8080**](http://localhost:8080)

---

## 👤 Domyślni Użytkownicy

Aplikacja po pierwszym uruchomieniu tworzy trzech domyślnych użytkowników z predefiniowanymi rolami, aby ułatwić testowanie:

*   **Administrator:**
   *   Login: `admin`
   *   Hasło: `admin123`
   *   Role: `ROLE_ADMIN`, `ROLE_PROJECT_MANAGER`, `ROLE_EMPLOYEE`
*   **Manager Projektu:**
   *   Login: `manager`
   *   Hasło: `manager123`
   *   Role: `ROLE_PROJECT_MANAGER`, `ROLE_EMPLOYEE`
*   **Zwykły Użytkownik (Pracownik):**
   *   Login: `user`
   *   Hasło: `user123`
   *   Rola: `ROLE_EMPLOYEE`

---

## 📁 Struktura Projektu

    .
    ├── src
    │   └── main
    │       ├── java/com/example/projekt
    │       │   ├── config/          # Konfiguracja Spring Security, inicjalizacja danych.
    │       │   ├── controller/      # Kontrolery Spring MVC obsługujące żądania HTTP.
    │       │   ├── dto/             # Data Transfer Objects - obiekty do przesyłania danych.
    │       │   ├── Entity/          # Encje JPA mapowane na tabele w bazie danych.
    │       │   ├── exception/       # Niestandardowe klasy wyjątków i globalna obsługa.
    │       │   ├── repository/      # Repozytoria Spring Data JPA do operacji na bazie.
    │       │   ├── service/         # Logika biznesowa aplikacji.
    │       │   └── util/            # Klasy pomocnicze (np. do generowania kodów 2FA).
    │       └── resources
    │           ├── static/          # Pliki statyczne (CSS, JS, obrazy).
    │           └── templates/       # Szablony Thymeleaf (widoki HTML).
    └── pom.xml                      # Plik konfiguracyjny Mavena.