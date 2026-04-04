# 🍕 QuickBite - Food Delivery Platform

A Spring Boot backend for a food delivery application with secure authentication, role-based access, and email verification.

---

## 📅 Development Log

### **April 4, 2026**

#### ✅ Forgot Password Feature

**Password Reset Flow**
- Forgot password endpoint sends OTP to registered email
- OTP verification with 5-minute expiry
- Password reset with OTP validation
- BCrypt password hashing for new passwords

**Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/forgot-password` | Send password reset OTP |
| `POST` | `/api/auth/reset-password` | Reset password with OTP |

**Frontend Integration**
- Forgot password link on login form
- Step 1: Enter email → sends OTP
- Step 2: Enter OTP + new password → resets password
- Success redirects to login

---

#### ✅ Restaurant Dashboard (Full Functionality)

**Profile Management**
- View restaurant profile (name, address, cuisine, owner info)
- Edit restaurant profile (name, address, cuisine type)
- Toggle open/closed status

**Menu Management**
- View all menu items grouped by category
- Add new menu items (name, description, price, category)
- Edit existing menu items
- Delete menu items
- Toggle item availability (mark as sold out)

**API Integration**
- Connected to backend REST APIs
- Real-time data fetching
- Error handling and loading states

---

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

### Password Reset
| Method | Endpoint | Body |
|--------|----------|------|
| POST | `/api/auth/forgot-password` | `{email}` |
| POST | `/api/auth/reset-password` | `{email, otpCode, newPassword}` |

### OAuth2
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/oauth2/authorization/google` | Start Google login |

### Restaurant Dashboard (Protected - RESTAURANT_OWNER only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/restaurant/profile` | Get restaurant profile |
| PUT | `/api/restaurant/profile` | Update restaurant profile |
| PATCH | `/api/restaurant/profile/toggle-status` | Toggle open/closed |
| POST | `/api/restaurant/menu` | Add menu item |
| GET | `/api/restaurant/menu` | Get all menu items |
| GET | `/api/restaurant/menu/category/{category}` | Get items by category |
| PUT | `/api/restaurant/menu/{id}` | Update menu item |
| DELETE | `/api/restaurant/menu/{id}` | Delete menu item |
| PATCH | `/api/restaurant/menu/{id}/toggle-availability` | Toggle availability |

### Public Endpoints (Customer Browsing)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/restaurants` | List all restaurants |
| GET | `/api/restaurants?city=Bangalore` | Filter by city |
| GET | `/api/restaurants?cuisine=Italian` | Filter by cuisine |
| GET | `/api/restaurants?city=X&openOnly=true` | Only open restaurants |
| GET | `/api/restaurants/{id}` | Restaurant details + menu |
| GET | `/api/restaurant/{restaurantId}/menu` | Menu only |

---

*Last updated: April 4, 2026 - Added comprehensive unit tests for restaurant controller endpoints*

---

### **April 4, 2026**

#### ✅ Restaurant Dashboard Module

**Restaurant Profile Management**
- Get restaurant profile endpoint (`GET /api/restaurant/profile`)
- Update restaurant profile endpoint (`PUT /api/restaurant/profile`)
- Toggle open/closed status (`PATCH /api/restaurant/profile/toggle-status`)
- Profile includes owner details from linked User entity

**Menu Management (Full CRUD)**
- Add menu item (`POST /api/restaurant/menu`)
- Get all menu items for owner (`GET /api/restaurant/menu`)
- Get menu items by category (`GET /api/restaurant/menu/category/{category}`)
- Update menu item (`PUT /api/restaurant/menu/{id}`)
- Delete menu item (`DELETE /api/restaurant/menu/{id}`)
- Toggle item availability (`PATCH /api/restaurant/menu/{id}/toggle-availability`)

**Public Menu Access**
- List restaurants by city (`GET /api/restaurants?city=Bangalore`)
- Filter by cuisine type (`GET /api/restaurants?cuisine=Italian`)
- Filter open restaurants only (`GET /api/restaurants?city=X&openOnly=true`)
- Get restaurant details with menu (`GET /api/restaurants/{id}`)
- Only shows approved restaurants and available items to customers

**New Components:**
- `MenuItem` entity with restaurant relationship (Many-to-One)
- `MenuItemRepository` with custom query methods
- `PublicRestaurantService` for customer browsing
- `PublicRestaurantController` for public endpoints
- `RestaurantListingResponse` DTO for list view
- `RestaurantDetailResponse` DTO for detail view
- `RestaurantProfileService` for profile operations
- `MenuService` for menu CRUD operations
- `RestaurantProfileController` for profile endpoints
- `MenuController` for menu endpoints

**Security Updates:**
- Added `RESTAURANT_OWNER` role restriction for `/api/restaurant/**` endpoints
- Public menu endpoint excluded from authentication

---

#### ✅ Restaurant Controller Unit Tests

**Comprehensive Test Coverage**
- `MenuControllerTest` - 10 test methods covering all menu endpoints
- `RestaurantProfileControllerTest` - 8 test methods for profile management
- `PublicRestaurantControllerTest` - 13 test methods for customer browsing

**Test Features:**
- Mockito-based unit tests using `@ExtendWith(MockitoExtension.class)`
- Complete endpoint coverage with edge cases and error scenarios
- Service layer mocking with proper verification of method calls
- AssertJ assertions for readable and maintainable tests
- Exception handling tests for robust error management

**Test Categories:**
- **Success scenarios**: All CRUD operations work correctly
- **Parameter validation**: URL parameters and request bodies handled properly
- **Authentication logic**: UserDetails extraction and username verification
- **Error handling**: Service exceptions handled gracefully
- **Edge cases**: Empty results, partial updates, null values

**Maven Updates:**
- Fixed test dependencies in `pom.xml`
- Updated from deprecated test starters to `spring-boot-starter-test`
- Added proper Spring Security test support

**Run tests:**
```bash
./mvnw test -Dtest="MenuControllerTest,RestaurantProfileControllerTest,PublicRestaurantControllerTest"
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
├── restaurant/
│   ├── controller/     # RestaurantProfileController, MenuController
│   ├── dto/            # MenuItemRequest, MenuItemResponse, ProfileDTOs
│   ├── entity/         # MenuItem
│   ├── repository/     # MenuItemRepository
│   └── service/        # RestaurantProfileService, MenuService
└── exception/          # GlobalExceptionHandler
```
