# IT342-Salonga-AssetFlow

Short three-tier Asset/Equipment management system for lab and organization use. This repository contains the backend (Spring Boot), web client (React + TypeScript), and mobile client (Android Kotlin) used for the IT342 final project submission.

Status: feature-complete for most coursework requirements. One required item (External Public API integration) was intentionally omitted and is documented below.

Repository layout
- backend/ — Spring Boot 3.x, Java 17+, Spring Security + JWT, JPA/Hibernate
- web/ — React 18, TypeScript, Tailwind CSS, consumes backend REST API
- mobile/ — Android (Kotlin, XML layouts, Retrofit), targets API Level 34
- docs/ — architecture, test plan and submission notes

Quick start

Backend (requires JDK 17+, configured DB and SMTP environment variables):

```bash
cd backend
./mvnw spring-boot:run
```

Web (requires Node/Bun installed):

```bash
cd web
# install then start (example using bun)
bun install
bun run dev
```

Mobile (Android Studio recommended):

Import the `mobile/` Gradle project into Android Studio and run on an emulator or device (API 34). Ensure the backend URL is reachable from the emulator/device.

Important notes
- Authentication & Security: Registration, login, JWT, password hashing (BCrypt), `/api/auth/me`, and protected routes are implemented. The mobile app uses EncryptedSharedPreferences for JWT storage.
- Role-Based Access Control: Roles (ADMIN, USER) exist; API-level restrictions added via `@PreAuthorize` on sensitive endpoints. UI adapts based on role.
- Core business module: `Asset` + `Category` + `Transaction` implemented with full CRUD, validation, and file uploads (asset images, avatars).
- Google OAuth: implemented server-side with custom JWT creation after successful OAuth sign-in.
- File uploads: stored and served by the backend; endpoints exist for upload/download.
- Email (SMTP): `EmailService` sends real emails when SMTP environment variables are configured. No console-only emails.
- Real-time: polling is implemented in the web/mobile clients. WebSocket was not used.