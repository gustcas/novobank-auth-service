# novobank-auth-service

Microservicio independiente de autenticación para el ecosistema NovoBanco.  
Responsabilidad única: validar credenciales y emitir tokens JWT.

## Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2 + Spring Security 6 |
| JWT | jjwt 0.12.x |
| Base de datos | PostgreSQL 16 (`novobank_auth`) |
| ORM | Spring Data JPA |
| Contraseñas | BCryptPasswordEncoder (strength 12) |
| Infra | Docker + Docker Compose |
| Puerto | 8081 |

## Arquitectura hexagonal

```
com.novobanco.auth/
├── domain/
│   ├── model/          ← User, RefreshToken (POJOs puros)
│   ├── exception/      ← excepciones de dominio
│   └── port/
│       ├── in/         ← RegisterUseCase, LoginUseCase, RefreshUseCase,
│       │                  LogoutUseCase, GetCurrentUserUseCase
│       └── out/        ← UserRepository, RefreshTokenRepository (interfaces)
├── application/
│   └── usecase/        ← implementaciones de los 5 casos de uso
├── infrastructure/
│   ├── persistence/    ← entidades JPA, Spring Data repos, adaptadores
│   ├── security/       ← JwtService, JwtAuthenticationFilter, SecurityConfig
│   └── web/            ← AuthController, DTOs (records Java 17), GlobalExceptionHandler
└── config/             ← OpenApiConfig
```

**Reglas de dependencia:** dominio → sin Spring/JPA. Use cases → solo interfaces de puerto.

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Registrar usuario |
| POST | `/api/v1/auth/login` | Login → accessToken + refreshToken |
| POST | `/api/v1/auth/refresh` | Renovar accessToken |
| POST | `/api/v1/auth/logout` | Invalidar sesión (Bearer required) |
| GET | `/api/v1/auth/me` | Datos del usuario autenticado |

## Levantar en local

### Opción 1 — Solo DB en Docker, app con Maven

```bash
# 1. Levantar solo PostgreSQL
docker-compose up postgres-auth

# 2. Crear archivo de propiedades local
cat > src/main/resources/application-local.properties << 'EOF'
spring.datasource.url=jdbc:postgresql://localhost:5433/novobank_auth
spring.datasource.username=postgres
spring.datasource.password=postgres
jwt.secret=local_secret_minimo_256_bits_para_desarrollo_only
jwt.expiration-ms=900000
jwt.refresh-expiration-ms=604800000
cors.allowed-origins=http://localhost:5173,http://localhost:3000
EOF

# 3. Ejecutar la app
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

- App: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui.html

### Opción 2 — Todo en Docker Desktop

```bash
# 1. Copiar y rellenar variables de entorno
cp .env.example .env
# Editar .env con valores reales (especialmente JWT_SECRET)

# 2. Levantar todo el ecosistema
docker-compose up --build
```

Servicios levantados:
- `postgres-auth` → puerto 5433
- `auth-service` → http://localhost:8081
- `pgAdmin` → http://localhost:5050

## Variables de entorno

Ver `.env.example` para la lista completa. **Nunca** commitear `.env`.

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `JWT_SECRET` | Clave HMAC-SHA256 (≥256 bits) | `base64encodedSecret` |
| `JWT_EXPIRATION_MS` | Duración del accessToken | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION_MS` | Duración del refreshToken | `604800000` (7 días) |
| `DB_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://...` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (CSV) | `http://localhost:5173` |

## Ejecutar tests

```bash
# Tests unitarios (sin Spring, sin DB)
mvn test

# Tests de integración (Testcontainers — necesita Docker)
mvn verify -DskipUnitTests
```

## Pruebas manuales

- **REST Client (VS Code):** `novobank-auth.http`
- **Postman:** importar `novobank-auth-collection.postman_collection.json`

## JWT — Claims del accessToken

```json
{
  "sub": "<userId UUID>",
  "email": "usuario@novobanco.com",
  "customerId": "<UUID>",
  "role": "CUSTOMER",
  "iat": 1234567890,
  "exp": 1234568790
}
```

`customerId` es el enlace con `novobank-account-service` — el frontend lo extrae del token para filtrar cuentas.

## Seguridad

- Contraseñas hasheadas con **BCrypt strength 12**
- RefreshTokens hasheados con **SHA-256** antes de almacenar (nunca texto plano)
- Error de login siempre genérico — nunca revela si fue email o password
- CORS configurado desde variable de entorno `CORS_ALLOWED_ORIGINS`
- Sesión completamente **stateless** (JWT)
