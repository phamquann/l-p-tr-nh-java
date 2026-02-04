# 📚 HUTECH Bookstore REST API Documentation

## 🔗 Base URL
```
http://localhost:8081/api
```

## 🔐 Authentication
API sử dụng **HTTP Basic Authentication**. Bạn cần gửi credentials trong header:
```
Authorization: Basic <base64(username:password)>
```

### Demo Accounts
- **Admin**: `admin:admin123`
- **User**: `user:user123`

---

## 📖 Book Endpoints

### 1. Get All Books
Lấy danh sách tất cả sách (User & Admin có thể truy cập)

**Request:**
```http
GET /api/books
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Lấy danh sách sách thành công",
  "data": [
    {
      "id": 1,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "price": 350000.0,
      "categoryId": 1,
      "categoryName": "Công nghệ thông tin"
    },
    {
      "id": 2,
      "title": "Design Patterns",
      "author": "Gang of Four",
      "price": 450000.0,
      "categoryId": 1,
      "categoryName": "Công nghệ thông tin"
    }
  ],
  "timestamp": "2026-02-01T10:30:00"
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8081/api/books" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

### 2. Get Book By ID
Lấy thông tin chi tiết 1 cuốn sách

**Request:**
```http
GET /api/books/{id}
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Lấy thông tin sách thành công",
  "data": {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "price": 350000.0,
    "categoryId": 1,
    "categoryName": "Công nghệ thông tin"
  },
  "timestamp": "2026-02-01T10:35:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Không tìm thấy sách với ID: 999",
  "status": 404,
  "error": "Not Found",
  "errors": null,
  "timestamp": "2026-02-01T10:35:00"
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8081/api/books/1" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

### 3. Create New Book
Tạo sách mới (**Chỉ ADMIN**)

**Request:**
```http
POST /api/books
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "title": "Spring Boot in Action",
  "author": "Craig Walls",
  "price": 500000.0,
  "categoryId": 1
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Thêm sách thành công",
  "data": {
    "id": 3,
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
    "price": 500000.0,
    "categoryId": 1,
    "categoryName": "Công nghệ thông tin"
  },
  "timestamp": "2026-02-01T10:40:00"
}
```

**Validation Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "status": 400,
  "error": "Validation Error",
  "errors": [
    "Tiêu đề không được để trống",
    "Giá phải lớn hơn 0"
  ],
  "timestamp": "2026-02-01T10:40:00"
}
```

**Access Denied (403 Forbidden) - Nếu User thường gọi:**
```json
{
  "success": false,
  "message": "Bạn không có quyền thực hiện thao tác này",
  "status": 403,
  "error": "Forbidden",
  "errors": null,
  "timestamp": "2026-02-01T10:40:00"
}
```

**cURL Example:**
```bash
curl -X POST "http://localhost:8081/api/books" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
    "price": 500000.0,
    "categoryId": 1
  }'
```

---

### 4. Update Book
Cập nhật thông tin sách (**Chỉ ADMIN**)

**Request:**
```http
PUT /api/books/{id}
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "title": "Clean Code - Updated",
  "author": "Robert C. Martin",
  "price": 380000.0,
  "categoryId": 1
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cập nhật sách thành công",
  "data": {
    "id": 1,
    "title": "Clean Code - Updated",
    "author": "Robert C. Martin",
    "price": 380000.0,
    "categoryId": 1,
    "categoryName": "Công nghệ thông tin"
  },
  "timestamp": "2026-02-01T10:45:00"
}
```

**cURL Example:**
```bash
curl -X PUT "http://localhost:8081/api/books/1" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code - Updated",
    "author": "Robert C. Martin",
    "price": 380000.0,
    "categoryId": 1
  }'
```

---

### 5. Delete Book
Xóa sách (**Chỉ ADMIN**)

**Request:**
```http
DELETE /api/books/{id}
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xóa sách thành công",
  "data": null,
  "timestamp": "2026-02-01T10:50:00"
}
```

**cURL Example:**
```bash
curl -X DELETE "http://localhost:8081/api/books/1" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

### 6. Count Books
Đếm tổng số sách

**Request:**
```http
GET /api/books/count
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đếm sách thành công",
  "data": 15,
  "timestamp": "2026-02-01T10:55:00"
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8081/api/books/count" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

## 🧪 Testing with Postman

### 1. Setup Authorization
1. Mở Postman → Chọn tab **Authorization**
2. Type: **Basic Auth**
3. Username: `admin`
4. Password: `admin123`

### 2. Test Scenarios

#### ✅ Scenario 1: Admin CRUD Operations
```
1. GET /api/books → Lấy danh sách (200 OK)
2. POST /api/books → Tạo sách mới (201 Created)
3. PUT /api/books/1 → Cập nhật sách (200 OK)
4. DELETE /api/books/1 → Xóa sách (200 OK)
```

#### ❌ Scenario 2: User Restricted Access
```
1. Login as user:user123
2. GET /api/books → Success (200 OK)
3. POST /api/books → Forbidden (403)
4. PUT /api/books/1 → Forbidden (403)
5. DELETE /api/books/1 → Forbidden (403)
```

---

## 📋 Response Format

### Success Response
```json
{
  "success": true,
  "message": "Thành công",
  "data": { /* ... */ },
  "timestamp": "2026-02-01T10:00:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Mô tả lỗi",
  "status": 400,
  "error": "Bad Request",
  "errors": ["Chi tiết lỗi 1", "Chi tiết lỗi 2"],
  "timestamp": "2026-02-01T10:00:00"
}
```

---

## 🔒 Security Notes

1. **CSRF Protection**: Tắt cho `/api/**` endpoints
2. **CORS**: Cho phép tất cả origins (development mode)
3. **Session**: Stateless cho API
4. **Authentication**: HTTP Basic Auth
5. **Authorization**: Role-based (ADMIN, USER)

---

## 🚀 Quick Test Script (JavaScript/Fetch)

```javascript
// Get all books
fetch('http://localhost:8081/api/books', {
  headers: {
    'Authorization': 'Basic ' + btoa('admin:admin123')
  }
})
.then(res => res.json())
.then(data => console.log(data));

// Create book
fetch('http://localhost:8081/api/books', {
  method: 'POST',
  headers: {
    'Authorization': 'Basic ' + btoa('admin:admin123'),
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    title: 'Test Book',
    author: 'Test Author',
    price: 100000,
    categoryId: 1
  })
})
.then(res => res.json())
.then(data => console.log(data));
```

---

## 📞 Support
- Email: admin@hutech.edu.vn
- Version: 1.0.0
- Last Updated: February 1, 2026
