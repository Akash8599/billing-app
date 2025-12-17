# Billing System - Hardware Shop (JWT Secured)

**Your own branding - no copyright issues. JWT authentication included.**

## Quick Facts

- **Framework**: Spring Boot 3.2 + React 18
- **Auth**: JWT (JSON Web Tokens) with BCrypt password hashing
- **Database**: H2 (dev) / PostgreSQL (production)
- **Features**: Sales invoices, purchase orders, GST calculations, PDF bills
- **Roles**: ADMIN, MANAGER, CASHIER

---

## Part 1: Backend Setup

### Step 1: Start Backend
```bash
cd billing-system
mvn clean install
mvn spring-boot:run
```

**Wait for:** `Started BillingSystemApplication in X seconds`

**Default login:**
- Username: `admin`
- Password: `admin123`

Backend runs on: `http://localhost:8080`

### Step 2: Test Login (cURL)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Response:
```json
{
  "token": "eyJhbGc....",
  "username": "admin",
  "role": "ADMIN",
  "message": "Login successful"
}
```

**Copy this token** - you'll use it for API calls.

### Step 3: Create New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cashier1",
    "email": "cashier@shop.com",
    "password": "cashier123",
    "confirmPassword": "cashier123",
    "role": "CASHIER"
  }'
```

---

## Part 2: Frontend Setup

### Step 1: Start Frontend (new terminal)
```bash
cd billing-system/frontend
npm install
npm start
```

Frontend opens at: `http://localhost:3000`

### Step 2: Login Page
1. Enter username: `admin`
2. Enter password: `admin123`
3. Click "Login"
4. Token saved in localStorage automatically
5. Redirected to dashboard

---

## Part 3: Understanding JWT Auth

### How It Works

```
1. User submits login form
2. Backend validates credentials
3. Backend generates JWT token with user role
4. Frontend stores token in localStorage
5. Frontend sends token with every API request: Authorization: Bearer {token}
6. Backend validates token on each request
7. If token invalid/expired → logout user
8. If valid → process request based on user role
```

### Token Structure
```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "admin",
  "role": "ADMIN",
  "iat": 1702567890,
  "exp": 1702654290
}

Signature: HMACSHA256(header.payload, secret_key)
```

### What Gets Protected

- **Products**: Create/Edit (ADMIN/MANAGER only)
- **Customers**: Create/Edit (ADMIN/MANAGER only)
- **Invoices**: All (ADMIN/MANAGER/CASHIER)
- **Purchase Orders**: Create (ADMIN/MANAGER only)

---

## API Endpoints with Authentication

### Auth (Public)
```
POST /api/auth/login          → Login, get JWT token
POST /api/auth/register       → Register new user
```

### Products (Protected)
```
GET    /api/products                    → Requires any role
POST   /api/products                    → Requires ADMIN or MANAGER
PUT    /api/products/{id}               → Requires ADMIN or MANAGER
DELETE /api/products/{id}               → Requires ADMIN only
```

### Example with Token
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer eyJhbGc...."
```

---

## Frontend Login Implementation

React component handles:
1. Login form submission
2. Send credentials to backend
3. Store token in localStorage
4. Set axios default header: `Authorization: Bearer {token}`
5. Auto-logout on token expiry (24 hours)
6. Redirect to login if 401 Unauthorized

**File:** `frontend/src/pages/Login.jsx`

---

## User Roles & Permissions

### ADMIN
✓ Create/edit/delete products
✓ Create/edit/delete customers
✓ Create sales invoices
✓ Create/manage purchase orders
✓ View reports
✓ Manage users

### MANAGER
✓ Create/edit products
✓ Create/edit customers
✓ Create sales invoices
✓ Create/manage purchase orders
✗ Cannot delete products
✗ Cannot manage users

### CASHIER
✓ Create sales invoices
✓ View products & customers
✗ Cannot create/edit products
✗ Cannot create purchase orders
✗ Cannot manage customers

---

## Security Features Implemented

1. **Password Hashing**: BCrypt (one-way encryption)
2. **JWT Tokens**: Expire after 24 hours
3. **CORS**: Limited to localhost:3000 & 3001 (change in production)
4. **Role-Based Access**: Different endpoints require different roles
5. **Token Validation**: Checked on every protected request
6. **HTTP Only Cookies**: Optional (future enhancement)

---

## Production Checklist

Before deploying, do this:

### 1. Change Default Credentials
Edit `BillingSystemApplication.java`:
```java
admin.setPassword(passwordEncoder.encode("your_strong_password"));
```

### 2. Change JWT Secret
Edit `application.properties`:
```properties
jwt.secret=YourVeryLongSecureRandomStringAtLeast256BitsLongPleaseChange
```

### 3. Switch to PostgreSQL
Install PostgreSQL, then update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/billingsystem
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driverClassName=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

### 4. Update CORS
```properties
spring.webmvc.cors.allowed-origins=https://yourdomain.com
```

### 5. Enable HTTPS
Use Let's Encrypt SSL certificate

### 6. Setup Database Backups
Automated daily backups of PostgreSQL

### 7. Environment Variables
Never hardcode secrets. Use env vars:
```bash
export JWT_SECRET="your_secret"
export DB_PASSWORD="your_password"
```

---

## Troubleshooting

### "Invalid token" or "Unauthorized" on API calls
- Token expired (24 hours) → Login again
- Token not sent in header → Check Authorization header
- Token malformed → Clear localStorage, re-login

### Login page shows "Invalid credentials"
- Check username spelling
- Password is case-sensitive
- Default: admin / admin123

### "401 Unauthorized" on protected endpoints
- Forgot to attach token to request
- Token expired
- User role doesn't have permission

### "CORS error" in browser console
- Frontend and backend not on same allowed origins
- Check `spring.webmvc.cors.allowed-origins` in properties

---

## Testing the System

### Test 1: Login as different roles
```bash
# Register cashier
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cashier1",
    "email": "cashier@shop.com",
    "password": "cashier123",
    "confirmPassword": "cashier123",
    "role": "CASHIER"
  }'

# Login as cashier
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"cashier1","password":"cashier123"}'
```

### Test 2: Permission denied for CASHIER creating product
```bash
# Try to create product (will fail - CASHIER can't)
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer {cashier_token}" \
  -H "Content-Type: application/json" \
  -d '{"sku":"TEST-001","name":"Test","quantity":0,"costPrice":100,"sellingPrice":150,"gstRate":18,"lowStockAlert":5}'

# Response: 403 Forbidden
```

### Test 3: ADMIN can delete products
```bash
# Delete product (admin can)
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer {admin_token}"

# Response: 204 No Content
```

---

## Next Steps

1. **Rename your project** - Change `BillingSystem` to your company name everywhere
2. **Customize branding** - Update frontend logo, colors, company name
3. **Add audit logging** - Log who did what and when
4. **Add email notifications** - Send invoice PDFs via email
5. **Mobile app** - Build React Native version
6. **Analytics dashboard** - Sales trends, profit analysis
7. **Backup system** - Automated daily database backups

---

## File Structure

```
billing-system/
├── pom.xml
├── src/main/java/com/billingsystem/
│   ├── BillingSystemApplication.java
│   ├── config/
│   │   └── SecurityConfig.java          ← CORS & security rules
│   ├── security/
│   │   ├── JwtProvider.java             ← Generate/validate JWT
│   │   └── JwtAuthenticationFilter.java ← Check token on requests
│   ├── controller/
│   │   └── AuthController.java          ← Login/register endpoints
│   ├── service/
│   │   └── AuthService.java             ← Login logic
│   ├── repository/
│   │   └── UserRepository.java          ← Database access
│   ├── model/
│   │   └── User.java                    ← User entity
│   └── ...other services & models
│
└── frontend/
    └── src/pages/
        └── Login.jsx                     ← Login page
```

---

## Default Users After First Run

```
Username: admin
Password: admin123
Role: ADMIN
```

---

## Remember

1. **Change default password in production**
2. **Change JWT secret before deploying**
3. **Use HTTPS only in production**
4. **Backup your database regularly**
5. **Test role-based access thoroughly**
6. **Never commit secrets to Git**

---

**Build, secure, ship. That's it.**
