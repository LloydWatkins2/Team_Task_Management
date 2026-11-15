# Collabify — Hệ thống quản lý công việc nhóm

## 1. Mục đích của dự án

**Collabify** là ứng dụng desktop viết bằng Java Swing giúp các nhóm làm việc quản lý dự án, công việc và bình luận nội bộ. Ứng dụng hỗ trợ:

- Đăng ký và đăng nhập tài khoản (7 trường bắt buộc).
- Quản lý danh sách dự án riêng theo từng người dùng.
- Phân quyền rõ ràng: **Chủ dự án (Owner)** có quyền tạo/sửa/xoá dự án, thêm/xoá thành viên, giao công việc; **Thành viên (Member)** chỉ xem và bình luận.
- Tìm kiếm dự án trên trang tổng quan.
- Xem chi tiết dự án chia thành hai cột: danh sách thành viên bên trái và danh sách công việc bên phải.
- Quản lý công việc: tạo, cập nhật trạng thái, xem chi tiết và bình luận từng công việc.
- Chỉnh sửa hồ sơ cá nhân.

Mục tiêu là cung cấp một công cụ quản lý tác vụ nhóm nhẹ, chạy độc lập trên máy tính, không phụ thuộc nền web.

---

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | Java 17 |
| Giao diện | Swing + FlatLaf 3.8 (Look & Feel hiện đại) |
| Cơ sở dữ liệu | PostgreSQL |
| Kết nối DB | HikariCP (connection pool) + JDBC |
| Quản lý dự án | Apache Maven |
| Kiểm thử | JUnit 5 |
| Biến môi trường | dotenv-java |

> `pom.xml` cũng khai báo driver SQLite, nhưng mã nguồn hiện tại dùng cú pháp PostgreSQL (`BIGSERIAL`, `RETURNING`, …), nên cơ sở dữ liệu được hỗ trợ chính thức là PostgreSQL.

---

## 3. Cấu trúc dự án

```
Team_Task_Management/
├── README.md
├── LICENSE
├── .gitignore
└── teamtaske/                               # Module Maven chính
    ├── pom.xml                              # Cấu hình Maven và dependencies
    ├── .env                                 # Biến môi trường (url, DB_USER, DB_PASSWORD)
    └── src/
        ├── main/
        │   ├── java/com/java/teamtask/
        │   │   ├── App.java                 # Điểm khởi động ứng dụng
        │   │   ├── connections/
        │   │   │   └── SQL.java             # Khởi tạo HikariCP + toàn bộ truy vấn SQL
        │   │   ├── elements/                # Tầng nghiệp vụ (domain)
        │   │   │   ├── Dates.java           # Chuyển đổi định dạng ngày
        │   │   │   ├── users.java           # Người dùng + CRUD tài khoản
        │   │   │   ├── projects.java        # Dự án + quản lý task
        │   │   │   ├── projectsAssignments.java  # Liên kết user–project (role)
        │   │   │   ├── projectsManage.java  # Interface quyền Owner
        │   │   │   ├── seeProjects.java     # Interface quyền xem (Owner/Member)
        │   │   │   ├── owners.java          # Triển khai quyền Owner
        │   │   │   ├── members.java         # Triển khai quyền Member
        │   │   │   ├── system.java          # Đăng ký, đăng nhập, phân quyền
        │   │   │   └── tasks.java           # Công việc + bình luận
        │   │   └── ui/
        │   │       ├── AppRootPanel.java    # Vùng chứa shell gradient
        │   │       ├── Card.java            # Card trắng bo góc
        │   │       ├── GradientPanel.java   # Nền gradient
        │   │       ├── MainFrame.java       # Cửa sổ chính
        │   │       ├── ShellPanel.java      # Khung trắng bo góc, cắt component con
        │   │       ├── components/          # Thành phần giao diện tái sử dụng
        │   │       │   ├── AppButton.java
        │   │       │   ├── Avatar.java
        │   │       │   ├── AvatarButton.java
        │   │       │   ├── Badge.java
        │   │       │   ├── CircleIconPanel.java
        │   │       │   ├── EmptyState.java
        │   │       │   ├── FormField.java
        │   │       │   ├── IconButton.java
        │   │       │   ├── NavItem.java
        │   │       │   ├── PageHeader.java
        │   │       │   ├── ProgressBar.java
        │   │       │   ├── ProjectCard.java
        │   │       │   ├── SearchField.java
        │   │       │   ├── SelectableCard.java
        │   │       │   ├── SelectableGroup.java
        │   │       │   ├── Sidebar.java
        │   │       │   ├── StackPanel.java
        │   │       │   ├── TaskCard.java
        │   │       │   └── TopBar.java
        │   │       ├── data/                # DTO và cổng dữ liệu (Gateway)
        │   │       │   ├── CommentView.java
        │   │       │   ├── DataAccessException.java
        │   │       │   ├── DbSessionGateway.java    # Gateway trên PostgreSQL
        │   │       │   ├── MemberInfoView.java
        │   │       │   ├── MemberTaskView.java
        │   │       │   ├── ProfileChange.java
        │   │       │   ├── ProjectSearch.java       # Tìm kiếm dự án trong bộ nhớ
        │   │       │   ├── Registration.java
        │   │       │   ├── SessionGateway.java      # Lớp trừu tượng cổng dữ liệu
        │   │       │   ├── TaskView.java
        │   │       │   ├── UiMapper.java            # Ánh xạ entity → dữ liệu hiển thị
        │   │       │   ├── UserSession.java         # Phiên đăng nhập hiện tại
        │   │       │   └── Validators.java          # Validate form
        │   │       ├── sample/              # Dữ liệu mẫu khi phát triển giao diện
        │   │       │   ├── CommentItem.java
        │   │       │   ├── SampleData.java
        │   │       │   └── TaskItem.java
        │   │       ├── screens/             # Các màn hình ứng dụng
        │   │       │   ├── CommentsPanel.java
        │   │       │   ├── DashboardScreen.java
        │   │       │   ├── DialogSizer.java
        │   │       │   ├── LoginScreen.java
        │   │       │   ├── MemberInfoDialog.java
        │   │       │   ├── ProfileScreen.java
        │   │       │   ├── ProjectDetailScreen.java
        │   │       │   ├── ProjectFormDialog.java
        │   │       │   ├── ProjectsScreen.java
        │   │       │   ├── RegisterScreen.java
        │   │       │   ├── TaskCreateDialog.java
        │   │       │   ├── TaskDetailDialog.java
        │   │       │   └── TaskFormScreen.java
        │   │       └── theme/               # Hệ thống thiết kế
        │   │           ├── Icons.java
        │   │           ├── Layout.java
        │   │           ├── Painters.java
        │   │           ├── Palette.java
        │   │           ├── Radius.java
        │   │           ├── Shadows.java
        │   │           ├── Space.java
        │   │           ├── TaskiaLaf.java
        │   │           ├── Theme.java
        │   │           ├── Typography.java
        │   │           └── UiScale.java
        │   └── resources/                   # Tài nguyên ứng dụng
        └── test/
            └── java/com/java/teamtask/      # Kiểm thử JUnit 5
                ├── AppTest.java
                ├── elements/
                │   └── DatesTest.java
                └── ui/
                    ├── CommentsPanelTest.java
                    ├── ComponentRenderTest.java
                    ├── FormValidationTest.java
                    ├── KeyboardAccessTest.java
                    ├── LoginFlowTest.java
                    ├── MemberInfoDialogTest.java
                    ├── ProjectFlowTest.java
                    ├── SearchFlowTest.java
                    ├── ShellLayoutTest.java
                    ├── TaskDetailLayoutTest.java
                    ├── TestGateway.java     # Gateway giả lập cho test UI
                    ├── ThemeSmokeTest.java
                    ├── TokenTest.java
                    ├── UiSnapshotTest.java
                    └── data/
                        └── ProjectSearchTest.java
```

### Kiến trúc tóm tắt

```
App.main()
  → SQL.connectionsPoolInit()      # Tạo HikariCP pool
  → SQL.createTables()             # Tự tạo bảng nếu chưa có
  → Theme.install()                # FlatLaf + Collabify LAF
  → MainFrame(DbSessionGateway)    # UI shell
       └── screens → SessionGateway → DbSessionGateway → SQL / elements
```

---

## 4. Cài đặt và chạy

### 4.1. Yêu cầu hệ thống

- **JDK 17** hoặc cao hơn (`java -version`).
- **Apache Maven** 3.6+ (`mvn -version`).
- **PostgreSQL** đã cài đặt và đang chạy.
- Môi trường có giao diện đồ hoạ (không chạy ở chế độ headless).

### 4.2. Lấy mã nguồn

```bash
git clone <repo-url>
cd Team_Task_Management/teamtaske
```

> Mọi lệnh Maven bên dưới phải chạy **trong thư mục `teamtaske/`** (nơi có `pom.xml`), vì ứng dụng đọc file `.env` từ thư mục làm việc hiện tại.

### 4.3. Cấu hình cơ sở dữ liệu

Tạo file `.env` trong thư mục `teamtaske/`:

```env
url="jdbc:postgresql://<host>:<port>/<database>?sslmode=require"
DB_USER="<username>"
DB_PASSWORD="<password>"
```

Ví dụ với PostgreSQL chạy trên máy:

```env
url="jdbc:postgresql://localhost:5432/collabify"
DB_USER="postgres"
DB_PASSWORD="matkhaucuaban"
```

Ghi chú:

- Nếu thiếu `url`, ứng dụng hiển thị hộp thoại lỗi cấu hình và không khởi động.
- File `.env` đã nằm trong `.gitignore` — không commit thông tin đăng nhập.
- Các bảng (`users`, `projects`, `projectsassgiments`, `tasks`, `comments`) được tạo tự động ở lần chạy đầu tiên.

### 4.4. Chạy ứng dụng

```bash
mvn compile exec:java

```
## 5. Tính năng chính

| Tính năng | Mô tả |
|-----------|-------|
| Đăng ký / Đăng nhập | 7 trường bắt buộc khi đăng ký; thiếu trường hiển thị `Please fill up this box!` |
| Phân quyền | Owner (chủ dự án) và Member (thành viên) có quyền hạn khác nhau |
| Dashboard | Danh sách dự án của chính người dùng, kèm vai trò; tìm kiếm theo từ khoá |
| Chi tiết dự án | Hai cột: Thành viên bên trái, Công việc bên phải; font tự co giãn theo độ rộng |
| Quản lý công việc | Tạo, cập nhật trạng thái, xem chi tiết, bình luận |
| Chi tiết công việc | Hộp thoại mô tả, trạng thái, danh sách bình luận, nút Gửi và Làm mới |
| Hồ sơ cá nhân | Chỉnh sửa họ, tên, email, điện thoại, ngày sinh, mật khẩu |
| Tìm kiếm dự án | Không phân biệt dấu tiếng Việt, yêu cầu khớp tất cả từ khoá |

---

## 6. Giấy phép

Xem file [LICENSE](LICENSE). Dự án được phát triển cho mục đích học tập và thực hành lập trình hướng đối tượng với Java Swing.
