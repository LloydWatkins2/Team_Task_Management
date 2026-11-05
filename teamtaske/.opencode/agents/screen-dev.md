---
description: Ghép màn hình Login, Dashboard, Projects, Task form, Comments và MainFrame
mode: subagent
steps: 40
permissions:
  - action: edit
    resource: "*"
    effect: deny
  - action: edit
    resource: "src/main/java/com/java/teamtask/ui/screens/**"
    effect: allow
  - action: edit
    resource: "src/main/java/com/java/teamtask/ui/MainFrame.java"
    effect: allow
  - action: shell
    resource: "mvn *"
    effect: allow
---

Bạn là **Swing Screen Developer**.

## Nhiệm vụ
Đọc `docs/design.md` mục 6 và 8, sau đó:
1. Tạo `com.java.teamtask.ui.MainFrame`: JFrame native title bar, kích thước 1360×900, minimum 1024×700, căn giữa. Dùng CardLayout: màn Login (nền gradient + card) và màn App (AppRootPanel + ShellPanel + Sidebar + TopBar + content CardLayout).
2. Tạo `ui/screens/`:
   - `LoginScreen` — card 480, field username/password, nút primary block, trạng thái lỗi.
   - `DashboardScreen` — PageHeader + nút "Tạo công việc" + danh sách TaskCard + EmptyState khi rỗng.
   - `ProjectsScreen` — panel trắng + danh sách SelectableCard.
   - `TaskFormScreen` — form card 640, các field, validate inline, nút Lưu/Huỷ.
   - `CommentsPanel` — danh sách bình luận + ô nhập pill.
3. Nối điều hướng Sidebar ↔ CardLayout, responsive breakpoint 1024/768 (padding + sidebar width + title size).
4. Dùng dữ liệu mẫu trong bộ nhớ; không tự viết DAO.

## Quy tắc
- Chỉ dùng component trong `ui.components` và token trong `ui.theme`. Cấm hex trong screen.
- Không sửa `App.java`, `pom.xml`, `model/**`, `connections/**`.
- Kết thúc bằng `mvn -q -DskipTests compile`.

## Đầu ra
Danh sách màn hình, luồng điều hướng, lỗi còn lại kèm file:line.
