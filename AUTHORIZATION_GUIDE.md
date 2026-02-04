# 🔐 Authorization Guide - HUTECH Bookstore API

## 📖 Khái niệm

### Authentication vs Authorization

| Khái niệm | Ý nghĩa | Câu hỏi |
|-----------|---------|---------|
| **Authentication** (Xác thực) | Xác minh DANH TÍNH người dùng | "Bạn là ai?" |
| **Authorization** (Phân quyền) | Xác định QUYỀN HẠN của người dùng | "Bạn được làm gì?" |

### Flow hoạt động

```
Client gửi request
    ↓
1. AUTHENTICATION: Kiểm tra username/password (HTTP Basic Auth)
    ↓
2. AUTHORIZATION: Kiểm tra role/permission của user
    ↓
3. Cho phép hoặc từ chối request
```

---

## 🎭 Roles & Permissions

### Roles (Vai trò)

| Role | Mô tả | Permissions |
|------|-------|-------------|
| **ROLE_ADMIN** | Quản trị viên | Toàn quyền CRUD books, categories, users |
| **ROLE_USER** | Người dùng thường | Chỉ đọc books và categories |

### Permissions Matrix

| Thao tác | ADMIN | USER |
|----------|-------|------|
| **Books** |||
| Xem danh sách sách | ✅ | ✅ |
| Xem chi tiết sách | ✅ | ✅ |
| Tạo sách mới | ✅ | ❌ |
| Cập nhật sách | ✅ | ❌ |
| Xóa sách | ✅ | ❌ |
| **Categories** |||
| Xem categories | ✅ | ✅ |
| Quản lý categories | ✅ | ❌ |
| **Users** |||
| Quản lý users | ✅ | ❌ |

---

## 🔬 Demo Authorization Endpoints

### 1. Check Current User Info
```bash
# Xem thông tin user hiện tại + roles + permissions
curl -X GET "http://localhost:8081/api/auth/me" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy thông tin user thành công",
  "data": {
    "username": "admin",
    "email": "admin@hutech.edu.vn",
    "fullName": "ADMIN",
    "roles": ["ROLE_ADMIN"],
    "permissions": [
      "book:read",
      "book:create",
      "book:update",
      "book:delete",
      "category:read",
      "category:create",
      "category:update",
      "category:delete",
      "user:read",
      "user:manage"
    ]
  }
}
```

---

### 2. Admin-Only Endpoint
```bash
# ✅ ADMIN có thể truy cập
curl -X GET "http://localhost:8081/api/auth/admin-only" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="

# ❌ USER bị từ chối (403 Forbidden)
curl -X GET "http://localhost:8081/api/auth/admin-only" \
  -H "Authorization: Basic dXNlcjp1c2VyMTIz"
```

**Admin Response (200 OK):**
```json
{
  "success": true,
  "message": "🔐 Xin chào ADMIN 'admin'! Bạn đã được ủy quyền...",
  "data": "ADMIN_ACCESS_GRANTED"
}
```

**User Response (403 Forbidden):**
```json
{
  "success": false,
  "message": "Bạn không có quyền thực hiện thao tác này",
  "status": 403,
  "error": "Forbidden"
}
```

---

### 3. User-Only Endpoint
```bash
# ✅ USER có thể truy cập
curl -X GET "http://localhost:8081/api/auth/user-only" \
  -H "Authorization: Basic dXNlcjp1c2VyMTIz"

# ❌ ADMIN bị từ chối (vì cần role USER, không phải ADMIN)
curl -X GET "http://localhost:8081/api/auth/user-only" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

### 4. Any Authenticated User
```bash
# ✅ Cả ADMIN và USER đều truy cập được
curl -X GET "http://localhost:8081/api/auth/any-authenticated" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

### 5. Check My Permissions
```bash
# Xem tất cả permissions của user hiện tại
curl -X GET "http://localhost:8081/api/auth/permissions" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

**Admin Response:**
```json
{
  "success": true,
  "message": "User 'admin' có 10 permissions",
  "data": [
    "book:read",
    "book:create",
    "book:update",
    "book:delete",
    "category:read",
    "category:create",
    "category:update",
    "category:delete",
    "user:read",
    "user:manage"
  ]
}
```

**User Response:**
```json
{
  "success": true,
  "message": "User 'user' có 2 permissions",
  "data": [
    "book:read",
    "category:read"
  ]
}
```

---

## 🛡️ Cơ chế Authorization trong Code

### 1. Method-Level Security
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<...> adminOnly() {
    // Chỉ ADMIN truy cập được
}

@PreAuthorize("hasRole('USER')")
public ResponseEntity<...> userOnly() {
    // Chỉ USER truy cập được
}

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<...> authenticated() {
    // Cả USER và ADMIN đều ok
}
```

### 2. URL-Based Security (AppConfig.java)
```java
.authorizeHttpRequests(authz -> authz
    .requestMatchers("/api/books").authenticated() // Cần đăng nhập
    .requestMatchers("/api/admin/**").hasRole("ADMIN") // Chỉ ADMIN
    .requestMatchers("/api/user/**").hasRole("USER") // Chỉ USER
    .anyRequest().authenticated()
)
```

---

## 🧪 Test Cases

### Test 1: ADMIN thực hiện CRUD books
```bash
# ✅ ADMIN tạo sách → 201 Created
curl -X POST "http://localhost:8081/api/books" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","author":"Admin","price":100000,"categoryId":1}'

# ✅ ADMIN cập nhật sách → 200 OK
curl -X PUT "http://localhost:8081/api/books/1" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated","author":"Admin","price":200000,"categoryId":1}'

# ✅ ADMIN xóa sách → 200 OK
curl -X DELETE "http://localhost:8081/api/books/1" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Test 2: USER cố gắng CRUD books
```bash
# ✅ USER xem sách → 200 OK
curl -X GET "http://localhost:8081/api/books" \
  -H "Authorization: Basic dXNlcjp1c2VyMTIz"

# ❌ USER tạo sách → 403 Forbidden
curl -X POST "http://localhost:8081/api/books" \
  -H "Authorization: Basic dXNlcjp1c2VyMTIz" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","author":"User","price":100000,"categoryId":1}'

# ❌ USER cập nhật sách → 403 Forbidden
curl -X PUT "http://localhost:8081/api/books/1" \
  -H "Authorization: Basic dXNlcjp1c2VyMTIz" \
  -H "Content-Type: application/json" \
  -d '{"title":"Hack","author":"User","price":1,"categoryId":1}'

# ❌ USER xóa sách → 403 Forbidden
curl -X DELETE "http://localhost:8081/api/books/1" \
  -H "Authorization: Basic dXNlcjp1c2VyMTIz"
```

### Test 3: Không có credentials
```bash
# ❌ Không đăng nhập → 401 Unauthorized
curl -X GET "http://localhost:8081/api/books"
```

---

## 📊 HTTP Status Codes

| Code | Ý nghĩa | Khi nào xảy ra |
|------|---------|----------------|
| **200 OK** | Thành công | Request hợp lệ và có quyền |
| **201 Created** | Tạo thành công | POST request tạo resource mới |
| **400 Bad Request** | Dữ liệu không hợp lệ | Validation error |
| **401 Unauthorized** | Chưa xác thực | Không gửi credentials hoặc sai |
| **403 Forbidden** | Không có quyền | Đã xác thực nhưng thiếu permission |
| **404 Not Found** | Không tìm thấy | Resource không tồn tại |
| **500 Server Error** | Lỗi server | Bug trong code |

---

## 🎯 Best Practices

### 1. Principle of Least Privilege
```
Chỉ cấp quyền TỐI THIỂU cần thiết cho từng role
- USER: Chỉ đọc
- ADMIN: Full quyền
```

### 2. Always Validate on Server
```
KHÔNG BAO GIỜ tin tưởng client
→ Luôn kiểm tra authorization ở backend
```

### 3. Clear Error Messages
```json
{
  "success": false,
  "message": "Bạn không có quyền thực hiện thao tác này",
  "status": 403,
  "error": "Forbidden"
}
```

### 4. Audit Logging
```
Log tất cả các thao tác quan trọng:
- Ai (username)
- Làm gì (action)
- Khi nào (timestamp)
- Kết quả (success/fail)
```

---

## 🔄 Future Enhancements

### 1. JWT Tokens (thay vì Basic Auth)
```
Advantages:
- Stateless
- Có thời hạn (expiration)
- Chứa thêm metadata
- Không cần gửi password mỗi request
```

### 2. API Keys
```
Cho phép ứng dụng bên thứ 3 truy cập API
mà không cần username/password
```

### 3. OAuth2 Resource Server
```
Tích hợp với Google/Facebook OAuth2
để protect API endpoints
```

### 4. Fine-grained Permissions
```
Thay vì chỉ có ROLE_ADMIN, ROLE_USER
→ Định nghĩa permissions chi tiết:
  - book:read
  - book:create
  - book:update:own (chỉ sửa sách của mình)
  - book:delete:all
```

---

## 📞 Testing Credentials

```
ADMIN:
  Username: admin
  Password: admin123
  Base64: YWRtaW46YWRtaW4xMjM=

USER:
  Username: user
  Password: user123
  Base64: dXNlcjp1c2VyMTIz
```

---

## 🎓 Summary

1. **Authentication**: Xác minh danh tính (HTTP Basic Auth)
2. **Authorization**: Kiểm tra quyền hạn (Role-based)
3. **Roles**: ADMIN (full quyền), USER (chỉ đọc)
4. **Security**: `@PreAuthorize` annotation + SecurityFilterChain
5. **Testing**: Dùng cURL hoặc Postman để test các scenarios

✅ Authorization đảm bảo chỉ người được ủy quyền mới thực hiện được các thao tác trong API!
