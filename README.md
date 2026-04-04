# 🍕 QuickBite - Food Delivery Platform

A Spring Boot backend for a food delivery application with secure authentication, role-based access, and email verification.

---

## 📅 Development Log

### **April 3, 2026**

#### ✅ Authentication System Complete

**JWT Authentication**
- Implemented stateless JWT token-based authentication
- Token generation with email and role claims
- 24-hour token expiry
- Token validation and extraction utilities
- BCrypt password hashing for secure storage

**Role-Based Registration**
- Customer registration (`/api/auth/register/customer`)
- Restaurant owner registration (`/api/auth/register/restaurant`)
- Delivery agent registration (`/api/auth/register/agent`)
- Hub-and-spoke database model (User table as hub, role-specific tables as spokes)

**Login System**
- Unified login endpoint for all roles (`/api/auth/login`)
- Spring Security integration with AuthenticationManager
- Custom UserDetailsService for credential verification

---

#### ✅ Email OTP Verification

**OTP Generation & Sending**
- 6-digit random OTP generation using SecureRandom
- Gmail SMTP integration for sending verification emails
- Async email sending to prevent blocking
- Professional email template with QuickBite branding

**OTP Verification**
- Verify OTP endpoint (`/api/otp/verify`)
- 5-minute expiration window
- Maximum 3 verification attempts per OTP
- Rate limiting: max 3 OTPs per email per hour
- Email verified flag updated on successful verification

**Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/otp/send` | Send OTP to email |
| `POST` | `/api/otp/verify` | Verify OTP code |

---

#### ✅ Google OAuth2 Integration

**OAuth2 Login Flow**
- Google OAuth2 client configuration
- PKCE (Proof Key for Code Exchange) security
- Custom OAuth2LoginSuccessHandler for JWT generation
- Automatic user creation for new Google users
- Account linking for existing users

**Flow:**
1. User clicks "Continue with Google"
2. Redirects to `/oauth2/authorization/google`
3. Google authenticates user
4. Spring handles callback at `/login/oauth2/code/google`
5. OAuth2LoginSuccessHandler generates JWT
6. Redirects to frontend with token

---

#### ✅ Unit Tests (Mockito)

**AuthServiceTest** - 12 tests
- Customer registration (success, duplicate email)
- Restaurant registration (success, duplicate email)  
- Delivery agent registration (success, duplicate email)
- Login (success, invalid credentials, different roles)

**OtpServiceTest** - 13 tests
- OTP generation (success, rate limiting)
- OTP verification (valid, invalid, expired)
- Attempt tracking and rate limiting

**EmailServiceTest** - 12 tests
- Email sending (success, failure handling)
- Email content formatting
- Special character handling

**JwtUtilTest** - 21 tests
- Token generation and structure
- Username/role extraction
- Token validation (valid, invalid, tampered)

**Run tests:**
```bash
./mvnw test -Dtest="AuthServiceTest,OtpServiceTest,EmailServiceTest,JwtUtilTest"
```

---

#### ✅ Security Configuration

- CORS configured for React frontend (ports 5173-5180)
- CSRF disabled (stateless JWT architecture)
- Public endpoints: `/api/auth/**`, `/api/otp/**`, `/oauth2/**`
- H2 console enabled for development
- Swagger UI accessible at `/swagger-ui.html`

---

# Update Log - April 4, 2026

## Features Completed
- Implemented **Forgot Password** functionality
- Added OTP based password reset with **5-minute expiry**
- Integrated **BCrypt hashing** for new passwords
- Added frontend forgot password flow with redirect to login

## Restaurant Dashboard
- Completed **restaurant profile management**
- View and update restaurant details
- Toggle restaurant **open / closed status**
- Added full **menu CRUD operations**
- Add, update, delete menu items
- Toggle item availability / sold out
- Category based menu grouping

## Public Customer Browsing
- List all restaurants
- Filter by **city**
- Filter by **cuisine**
- Show **open restaurants only**
- Restaurant detail view with menu items

## Testing
- Added comprehensive **controller unit tests**
- MenuControllerTest
- RestaurantProfileControllerTest
- PublicRestaurantControllerTest
- Covered success, validation, edge cases, and exceptions

## Security & Auth
- Added role restriction for `RESTAURANT_OWNER`
- Public browsing endpoints excluded from authentication
- Google OAuth2 login flow completed
- JWT generation after successful OAuth login

## OTP & Email
- Implemented **email OTP verification**
- Added retry limit and rate limiting
- Async Gmail SMTP email sending
---
---


## 🔧 Tech Stack

- **Java 21** + **Spring Boot 4.0.5**
- **Spring Security** + **JWT** (jjwt 0.12.6)
- **Spring Data JPA** + **H2 Database**
- **Spring Mail** (Gmail SMTP)
- **OAuth2 Client** (Google)
- **Lombok** + **SpringDoc OpenAPI**
- **JUnit 5** + **Mockito**

---

## 🚀 Quick Start

```bash
# Run backend
./mvnw spring-boot:run

# Access
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

---

## 📁 Project Structure

```
src/main/java/com/app/quickbite/
├── auth/
│   ├── controller/     # AuthController, OAuth2Controller
│   ├── dto/            # Request/Response DTOs
│   ├── entity/         # User, Restaurant, DeliveryAgent
│   ├── repository/     # JPA repositories
│   ├── security/       # JwtUtil, JwtAuthenticationFilter
│   └── service/        # AuthService, OAuth2UserService
├── config/             # SecurityConfig, OAuth2LoginSuccessHandler
├── email/
│   ├── controller/     # OtpController
│   ├── entity/         # EmailOtp
│   ├── repository/     # EmailOtpRepository
│   └── service/        # OtpService, EmailService
└── exception/          # GlobalExceptionHandler
```

---

## 📋 API Reference

### Authentication
| Method | Endpoint | Body |
|--------|----------|------|
| POST | `/api/auth/register/customer` | `{email, password, fullName, phone}` |
| POST | `/api/auth/register/restaurant` | `{email, password, fullName, phone, restaurantName, address, cuisineType}` |
| POST | `/api/auth/register/agent` | `{email, password, fullName, phone, vehicleNumber, address}` |
| POST | `/api/auth/login` | `{email, password}` |

### OTP Verification
| Method | Endpoint | Body |
|--------|----------|------|
| POST | `/api/otp/send` | `{email}` |
| POST | `/api/otp/verify` | `{email, otpCode}` |

### OAuth2
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/oauth2/authorization/google` | Start Google login |

---

*Last updated: April 3, 2026*
