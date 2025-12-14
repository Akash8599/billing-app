# Billing System - Hardware Shop (JWT Secured, No Trademark)

**Your own custom billing & inventory system for hardware shops. Licensed for your use, no copyright conflicts.**

## 🎯 What You Have

✅ **Complete billing system** with JWT authentication  
✅ **Sales invoicing** with GST calculations  
✅ **Inventory management** with stock tracking  
✅ **Purchase orders** for wholesaler buying  
✅ **PDF invoice generation**  
✅ **User authentication** with role-based access  
✅ **Role-based permissions**: ADMIN, MANAGER, CASHIER  

---

## 🚀 Quick Start (5 Minutes)

### Backend
```bash
cd billing-system
mvn clean install
mvn spring-boot:run
```

Wait for: `Started BillingSystemApplication`  
Backend on: `http://localhost:8080`

### Frontend (new terminal)
```bash
cd billing-system/frontend
npm install
npm start
```

Frontend on: `http://localhost:3000`

### Login
- Username: `admin`
- Password: `admin123`

---

## 🔐 JWT Authentication Explained

### How It Works

1. User submits login credentials
2. Backend validates and generates JWT token
3. Token stored in browser localStorage
4. Token sent with every API request
5. Backend validates token before processing
6. If expired (24h), user logs out automatically

### Token Contents

```
{
  "username": "admin",
  "role": "ADMIN",
  "iat": 1702567890,
  "exp": 1702654290
}
```

### API Request Example

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## 👥 User Roles & Permissions

### ADMIN
- ✓ Create/Edit/Delete products
- ✓ Create/Edit/Delete customers
- ✓ Create sales invoices
- ✓ Create/Manage purchase orders
- ✓ Delete any data
- ✓ Manage users

### MANAGER
- ✓ Create/Edit products (no delete)
- ✓ Create/Edit customers
- ✓ Create sales invoices
- ✓ Create/Manage purchase orders
- ✗ Cannot delete
- ✗ Cannot manage users

### CASHIER
- ✓ Create sales invoices
- ✓ View products & customers
- ✗ Cannot create/edit products
- ✗ Cannot create purchase orders
- ✗ Cannot manage customers

---

## 📦 Backend Structure

```
src/main/java/com/billingsystem/
├── BillingSystemApplication.java      ← Main + default user setup
├── config/
│   └── SecurityConfig.java            ← CORS & security rules
├── security/
│   ├── JwtProvider.java              ← Generate/validate tokens
│   └── JwtAuthenticationFilter.java  ← Check token on requests
├── controller/
│   ├── AuthController.java           ← Login/register
│   ├── ProductController.java        ← Products API
│   ├── CustomerController.java       ← Customers API
│   ├── InvoiceController.java        ← Invoices API
│   └── PurchaseOrderController.java  ← PO API
├── service/
│   ├── AuthService.java              ← Login logic
│   ├── ProductService.java           ← Product business logic
│   ├── InvoiceService.java           ← Invoice & GST logic
│   └── ...other services
├── model/
│   ├── User.java                     ← User with JWT support
│   ├── Product.java
│   ├── Customer.java
│   ├── Invoice.java
│   └── ...
└── repository/
    ├── UserRepository.java
    ├── ProductRepository.java
    └── ...
```

---

## 💻 Frontend Structure

```
frontend/src/
├── pages/
│   └── Login.jsx                     ← Login with JWT handling
├── components/
│   └── Dashboard.jsx                 ← Main app (integrate other components)
├── services/
│   └── api.js                        ← Axios config with Bearer token
├── App.jsx                           ← Auth logic & routing
└── index.js                          ← Entry point
```

---

## 🔑 Key JWT Components

### Backend: JwtProvider.java
```java
// Generate token
String token = jwtProvider.generateToken("admin", "ADMIN");

// Validate token
boolean valid = jwtProvider.validateToken(token);

// Extract data
String username = jwtProvider.getUsernameFromToken(token);
String role = jwtProvider.getRoleFromToken(token);
```

### Backend: JwtAuthenticationFilter.java
- Runs on every request
- Extracts token from `Authorization: Bearer {token}`
- Validates token signature & expiry
- Sets user in Spring Security context

### Backend: SecurityConfig.java
- Defines which endpoints need which roles
- CORS configuration
- Session management (stateless)

### Frontend: App.jsx
- Checks localStorage for existing token
- Sets axios default header with token
- Handles logout by clearing storage

---

## 🛡️ Security Features

1. **Password Hashing**: BCrypt (one-way, salted)
2. **JWT Tokens**: 24-hour expiration
3. **CORS Protection**: Whitelisted origins only
4. **Role-Based Access**: Different endpoints for different roles
5. **Token Validation**: Every request checked
6. **Secure Headers**: No sensitive data in URLs

---

## 🔧 API Endpoints

### Authentication (Public)
```
POST /api/auth/login          → {username, password}
POST /api/auth/register       → {username, email, password, role}
```

### Products (Protected)
```
GET    /api/products                 → All roles
POST   /api/products                 → ADMIN, MANAGER
PUT    /api/products/{id}            → ADMIN, MANAGER
DELETE /api/products/{id}            → ADMIN only
```

### Customers (Protected)
```
GET    /api/customers                → ADMIN, MANAGER, CASHIER
POST   /api/customers                → ADMIN, MANAGER, CASHIER
PUT    /api/customers/{id}           → ADMIN, MANAGER
DELETE /api/customers/{id}           → ADMIN only
```

### Invoices (Protected)
```
POST   /api/invoices                 → ADMIN, MANAGER, CASHIER
GET    /api/invoices                 → ADMIN, MANAGER, CASHIER
GET    /api/invoices/{id}/pdf        → Download PDF
POST   /api/invoices/{id}/cancel     → Cancel & reverse stock
```

---

## 🏗️ Production Deployment

### 1. Change Default Credentials
Edit `BillingSystemApplication.java` line 22:
```java
admin.setPassword(passwordEncoder.encode("YourStrongPassword123!"));
```

### 2. Change JWT Secret
Edit `application.properties`:
```properties
jwt.secret=YourVeryLongRandomSecureKeyWithAtLeast32CharactersPleaseChangeThis
jwt.expiration=86400000
```

### 3. Switch to PostgreSQL
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
spring.webmvc.cors.allowed-origins=https://yourdomain.com,https://www.yourdomain.com
```

### 5. Build & Deploy
```bash
mvn clean package
java -jar target/billing-system-1.0.0.jar
```

### 6. Frontend Build
```bash
npm run build
# Deploy 'build' folder to Netlify, Vercel, or S3
```

---

## 🧪 Testing JWT Auth

### Test 1: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Response:
```json
{
  "token": "eyJhbGc...",
  "username": "admin",
  "role": "ADMIN",
  "message": "Login successful"
}
```

### Test 2: Use Token in Request
```bash
TOKEN="eyJhbGc..."
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"
```

### Test 3: Invalid Token (Should Fail)
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer invalid_token"
# Returns: 401 Unauthorized
```

### Test 4: Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager1",
    "email": "manager@shop.com",
    "password": "manager123",
    "confirmPassword": "manager123",
    "role": "MANAGER"
  }'
```

### Test 5: Permission Denied (CASHIER can't delete product)
```bash
TOKEN="cashier_token_here"
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN"
# Returns: 403 Forbidden
```

---

## 🚨 Troubleshooting

### "401 Unauthorized" on API calls
- Token expired (24 hours) → Login again
- Token not sent → Check Authorization header
- Token malformed → Clear localStorage, re-login

### "403 Forbidden"
- User role lacks permission
- Example: CASHIER trying to delete product
- Solution: Use ADMIN or MANAGER account

### "Invalid credentials" on login
- Username/password typo
- Case-sensitive password
- Default: admin / admin123

### CORS errors in browser
- Frontend origin not in allowed-origins
- Update `application.properties`
- Check console for exact error

### Token not persisting after refresh
- localStorage cleared
- Private/Incognito mode
- Browser settings disabled localStorage

---

## 📝 Customization Guide

### 1. Rename Project
Search & replace everywhere:
- `com.billingsystem` → `com.yourcompany`
- `BillingSystem` → `YourAppName`
- `billing-system` → `your-app-name`

### 2. Change App Branding
- Frontend: Update title, logo, colors in CSS
- Backend: Update error messages
- Update company name in templates

### 3. Add More Roles
Edit `SecurityConfig.java` to add custom roles like `SUPER_ADMIN`, `AUDITOR`, etc.

### 4. Modify Token Expiry
```properties
jwt.expiration=3600000  # 1 hour
jwt.expiration=604800000  # 1 week
```

### 5. Add Email Verification
Extend `AuthService` to send verification email

---

## 📊 Features You Can Add

- Email notifications for invoices
- SMS alerts for low stock
- Multi-warehouse support
- Advanced reporting & analytics
- Mobile app (React Native)
- Barcode scanning
- Customer credit tracking
- Automated backup system
- Payment gateway integration

---

## 🎓 Code Examples

### Frontend: Make API call with JWT
```javascript
const response = await fetch('http://localhost:8080/api/products', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
  }
});
```

### Backend: Check User Role in Controller
```java
@PostMapping("/products")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public ResponseEntity<Product> createProduct(@RequestBody Product product) {
  return ResponseEntity.ok(productService.createProduct(product));
}
```

### Backend: Get Current User
```java
@GetMapping("/me")
public ResponseEntity<String> getCurrentUser(Principal principal) {
  return ResponseEntity.ok("Logged in as: " + principal.getName());
}
```

---

## 🔒 Security Checklist

- [ ] Change default admin password
- [ ] Change JWT secret
- [ ] Update CORS allowed origins
- [ ] Use HTTPS in production
- [ ] Setup database backups
- [ ] Enable audit logging
- [ ] Use strong passwords (min 12 chars)
- [ ] Implement rate limiting on login
- [ ] Setup firewall rules
- [ ] Regular security updates

---

## 📞 Support Resources

- **Spring Security**: https://spring.io/projects/spring-security
- **JWT**: https://jwt.io
- **React**: https://react.dev
- **PostgreSQL**: https://www.postgresql.org/docs

---

## ✅ What's Complete

✓ JWT authentication & authorization  
✓ User login/register with role assignment  
✓ Password hashing with BCrypt  
✓ Token generation & validation  
✓ Role-based endpoint protection  
✓ Frontend login page with JWT handling  
✓ Axios interceptor for Bearer tokens  
✓ CORS configuration for security  
✓ Default admin user creation  

## ⚠️ What You Need to Complete

1. Integrate remaining dashboard components (from previous system)
2. Update all API calls to include Authorization header
3. Add error handling for 401/403 responses
4. Implement token refresh (optional, currently 24h expiry)
5. Add logout functionality
6. Add role-based UI element visibility
7. Test with different roles
8. Setup production database
9. Deploy to cloud

---

**Build it. Secure it. Deploy it. Own your business system.**
