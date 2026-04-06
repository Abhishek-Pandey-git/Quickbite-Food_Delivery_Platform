and  # QuickBite API Documentation & System Flows
# QuickBite API Documentation

This document outlines the REST API endpoints available in the QuickBite backend application, including their required payload structures, expected responses, and the complete end-to-end user flows.
This document outlines the REST API endpoints available in the QuickBite backend application, including their required payload structures and descriptions.

---

## 1. Authentication Endpoints

### 1.1 Customer Registration
* **Endpoint:** `POST /api/auth/register/customer`
* **Description:** Registers a new user with the `CUSTOMER` role.
* **Request Body:**
  ```json
  {
    "fullName": "John Doe",
    "email": "user@example.com",
    "password": "password123",
    "phone": "9876543210"
  }
  ```

### 1.2 Restaurant Owner Registration
* **Endpoint:** `POST /api/auth/register/restaurant`
* **Description:** Registers a new user with the `RESTAURANT_OWNER` role and creates a corresponding restaurant record in the database.
* **Request Body:**
  ```json
  {
    "fullName": "Jane Smith",
    "email": "owner@example.com",
    "password": "password123",
    "phone": "9876543210",
    "restaurantName": "My Restaurant",
    "address": "123 Main St, Bangalore",
    "cuisineType": "Indian"
  }
  ```

### 1.3 Delivery Agent Registration
* **Endpoint:** `POST /api/auth/register/agent`
* **Description:** Registers a new user with the `DELIVERY_AGENT` role and creates a corresponding agent record with vehicle details.
* **Request Body:**
  ```json
  {
    "fullName": "Mike Johnson",
    "email": "agent@example.com",
    "password": "password123",
    "phone": "9876543210",
    "vehicleNumber": "KA01AB1234",
    "address": "Bangalore"
  }
  ```

### 1.4 User Login
* **Endpoint:** `POST /api/auth/login`
* **Description:** Authenticates a user (regardless of role) and returns a JWT token.
* **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
* **Response (Success):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "role": "CUSTOMER",
    "email": "user@example.com",
    "message": "Login successful"
  }
  ```

---

## 2. OAuth2 Endpoints

### 2.1 Google OAuth2 Login Initiation
* **Endpoint:** `GET /oauth2/authorization/google`
* **Description:** Initiates the Google OAuth2 flow. Redirects the browser to the Google login screen.
* **Request Body:** None

### 2.2 OAuth2 Token Exchange
* **Endpoint:** `POST /api/auth/oauth2/token`
* **Description:** Exchanges the authorization code received from Google for a QuickBite JWT token.
* **Request Body:**
  ```json
  {
    "code": "authorization_code_from_google",
    "redirectUri": "http://localhost:5174/auth/callback"
    "redirectUri": "http://localhost:3000/auth/callback"
  }
  ```

---

## 3. Email OTP Verification Endpoints

### 3.1 Send OTP
* **Endpoint:** `POST /api/otp/send`
* **Description:** Generates and sends a 6-digit OTP to the provided email.
* **Request Body:**
  ```json
  {
    "email": "user@example.com"
  }
  ```

### 3.2 Verify OTP
* **Endpoint:** `POST /api/otp/verify`
* **Description:** Verifies the OTP sent to the email. Expires in 5 minutes.
* **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "otpCode": "123456"
  }
  ```

### 3.3 Forgot Password (Send Reset OTP)
* **Endpoint:** `POST /api/auth/forgot-password`
* **Description:** Sends a password reset OTP to the user's email.
* **Request Body:**
  ```json
  {
    "email": "user@example.com"
  }
  ```
* **Response (Success):**
  ```json
  {
    "message": "Password reset code sent to your email",
    "email": "user@example.com"
  }
  ```

### 3.4 Reset Password (Verify OTP and Change Password)
* **Endpoint:** `POST /api/auth/reset-password`
* **Description:** Verifies the OTP and resets the user's password.
* **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "otpCode": "123456",
    "newPassword": "newSecurePassword123"
  }
  ```
* **Response (Success):**
  ```json
  {
    "message": "Password reset successful. You can now login with your new password."
  }
  ```

---

## 4. Complete System Flows & Architecture

The QuickBite platform handles three distinct user roles utilizing a "Hub and Spoke" database architecture. The backend API interactions differ slightly depending on the flow initiated by the React frontend.

### 4.1 Customer Flow
1. **Standard Registration:** The user fills out the Customer signup form. The frontend calls `POST /api/auth/register/customer`. Upon success, the user is redirected to login.
2. **Standard Login:** The user enters their email and password. The frontend calls `POST /api/auth/login`. The backend verifies the credentials and returns a JWT token with the `CUSTOMER` role.
3. **Google OAuth2 (Alternative):**
   - User clicks "Sign in with Google".
   - Frontend redirects the browser to `GET /oauth2/authorization/google`.
   - User authenticates on Google's prompt.
   - Google redirects back to the frontend's OAuth callback URL (`http://localhost:5174/auth/callback`) with an authorization `code`.
   - Frontend exchanges this code by calling `POST /api/auth/oauth2/token`.
   - Backend validates the code with Google, creates the user if they don't exist, and returns a QuickBite JWT token.
4. **Dashboard Access:** The frontend stores the JWT token in `localStorage` and includes it as a Bearer token in the `Authorization` header for all subsequent requests to protected endpoints.

### 4.2 Restaurant Owner Flow
1. **Registration:** The user fills out the Restaurant Owner signup form, which includes both personal details (Name, Email, Password, Phone) and business details (Restaurant Name, Address/City).
2. **API Call:** The frontend posts this combined payload to `POST /api/auth/register/restaurant`.
3. **Database Action:** The backend creates a record in the `users` table (the Hub) with the `RESTAURANT_OWNER` role, and a linked record in the `restaurants` table (the Spoke) associated with that owner's ID.
4. **Login:** The user logs in via `POST /api/auth/login`. The backend returns a JWT token with the `RESTAURANT_OWNER` role.
5. **Routing:** The frontend detects the `RESTAURANT_OWNER` role in the JWT payload and routes the user to the `Restaurant Dashboard` instead of the standard Customer Dashboard.

### 4.3 Delivery Agent Flow
1. **Registration:** The user fills out the Delivery Partner signup form, providing personal details and vehicle information (e.g., Vehicle Number, Address/City).
2. **API Call:** The frontend posts this payload to `POST /api/auth/register/agent`.
3. **Database Action:** The backend creates a record in the `users` table with the `DELIVERY_AGENT` role, and a linked record in the `delivery_agents` table.
4. **Login:** The user logs in via `POST /api/auth/login`. The backend returns a JWT token with the `DELIVERY_AGENT` role.
5. **Routing:** The frontend routes the user to the specific `Delivery Agent Dashboard`.

### 4.4 Email OTP Verification Flow (Security Extension)
*Can be integrated before finalizing registration or for password resets.*
1. **Initiation:** The frontend calls `POST /api/otp/send` with the user's email address.
2. **Email Sent:** The backend generates a 6-digit code, stores it temporarily with a 5-minute expiration, and uses Gmail SMTP to email the user.
3. **Verification:** The user types the code into the frontend. The frontend calls `POST /api/otp/verify`.
4. **Success/Failure:** The backend validates the code. If successful, the backend marks the email as verified, and the frontend allows the user to proceed to the next step.

---

## 5. Restaurant Dashboard Endpoints

### 5.1 Restaurant Profile Management

#### Get Restaurant Profile
* **Endpoint:** `GET /api/restaurant/profile`
* **Description:** Get the profile of the authenticated restaurant owner's restaurant.
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required
* **Response (Success):**
  ```json
  {
    "id": 1,
    "restaurantName": "My Restaurant",
    "address": "Bangalore",
    "cuisineType": "Indian",
    "isOpen": true,
    "isApproved": false,
    "ownerName": "Jane Smith",
    "ownerEmail": "owner@example.com",
    "ownerPhone": "9876543210"
  }
  ```

#### Update Restaurant Profile
* **Endpoint:** `PUT /api/restaurant/profile`
* **Description:** Update the restaurant profile. Only provided fields will be updated.
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required
* **Request Body:**
  ```json
  {
    "restaurantName": "Updated Restaurant Name",
    "address": "Bangalore",
    "cuisineType": "Multi-Cuisine",
    "isOpen": true
  }
  ```

#### Toggle Restaurant Status
* **Endpoint:** `PATCH /api/restaurant/profile/toggle-status`
* **Description:** Quick toggle for open/closed status.
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required

### 5.2 Menu Management

#### Add Menu Item
* **Endpoint:** `POST /api/restaurant/menu`
* **Description:** Add a new menu item to the restaurant.
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required
* **Request Body:**
  ```json
  {
    "name": "Butter Chicken",
    "description": "Creamy tomato-based curry with tender chicken",
    "price": 299.00,
    "category": "Main Course",
    "isAvailable": true
  }
  ```
* **Response (201 Created):**
  ```json
  {
    "id": 1,
    "name": "Butter Chicken",
    "description": "Creamy tomato-based curry with tender chicken",
    "price": 299.00,
    "category": "Main Course",
    "isAvailable": true,
    "restaurantId": 1
  }
  ```

#### Get All Menu Items (Owner)
* **Endpoint:** `GET /api/restaurant/menu`
* **Description:** Get all menu items for the authenticated owner's restaurant (includes unavailable items).
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required

#### Get Menu Items by Category
* **Endpoint:** `GET /api/restaurant/menu/category/{category}`
* **Description:** Get menu items filtered by category.
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required

#### Update Menu Item
* **Endpoint:** `PUT /api/restaurant/menu/{id}`
* **Description:** Update an existing menu item.
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required
* **Request Body:**
  ```json
  {
    "name": "Butter Chicken (Spicy)",
    "price": 329.00,
    "isAvailable": true
  }
  ```

#### Delete Menu Item
* **Endpoint:** `DELETE /api/restaurant/menu/{id}`
* **Description:** Delete a menu item.
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required
* **Response:**
  ```json
  {
    "message": "Menu item deleted successfully"
  }
  ```

#### Toggle Item Availability
* **Endpoint:** `PATCH /api/restaurant/menu/{id}/toggle-availability`
* **Description:** Toggle the availability status of a menu item (useful for marking items as sold out).
* **Authorization:** Bearer token with `RESTAURANT_OWNER` role required

### 5.3 Public Endpoints (For Customers)

#### Get All Restaurants
* **Endpoint:** `GET /api/restaurants`
* **Description:** Get list of all approved restaurants. Supports optional filters.
* **Authorization:** None (public endpoint)
* **Query Parameters:**
  - `city` (optional): Filter by city name (e.g., "Bangalore")
  - `cuisine` (optional): Filter by cuisine type (e.g., "Italian")
  - `openOnly` (optional): If `true`, only return open restaurants
* **Examples:**
  - `GET /api/restaurants` - All restaurants
  - `GET /api/restaurants?city=Bangalore` - Restaurants in Bangalore
  - `GET /api/restaurants?city=Bangalore&openOnly=true` - Open restaurants in Bangalore
  - `GET /api/restaurants?cuisine=Italian` - Italian restaurants
* **Response:**
  ```json
  [
    {
      "id": 1,
      "restaurantName": "Pizza Palace",
      "address": "123 MG Road, Bangalore",
      "cuisineType": "Italian",
      "isOpen": true,
      "menuItemCount": 15
    }
  ]
  ```

#### Get Restaurant Details with Menu
* **Endpoint:** `GET /api/restaurants/{id}`
* **Description:** Get detailed view of a restaurant including its menu items.
* **Authorization:** None (public endpoint)
* **Response:**
  ```json
  {
    "id": 1,
    "restaurantName": "Pizza Palace",
    "address": "123 MG Road, Bangalore",
    "cuisineType": "Italian",
    "isOpen": true,
    "menuItems": [
      {
        "id": 1,
        "name": "Margherita Pizza",
        "description": "Classic Italian pizza",
        "price": 299.00,
        "category": "Pizza",
        "isAvailable": true,
        "restaurantId": 1
      }
    ]
  }
  ```

#### Get Restaurant Menu Only
* **Endpoint:** `GET /api/restaurant/{restaurantId}/menu`
* **Description:** Get only the available menu items for a restaurant.
* **Authorization:** None (public endpoint)