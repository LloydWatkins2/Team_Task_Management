# Taskia — Design Guidelines (Java Swing)

> Tài liệu định hướng giao diện cho ứng dụng quản lý công việc nhóm **Team Task Management**.
> Chuyển thể bộ style CSS "Taskia Dashboard" sang Java Swing bằng FlatLaf, kèm kiến trúc UI,
> thông số component và hướng dẫn triển khai.

| Thông tin | Giá trị |
|---|---|
| Phiên bản tài liệu | 1.0 |
| Cập nhật | 05/11/2026 |
| Ngôn ngữ | Tiếng Việt (tên kỹ thuật giữ tiếng Anh) |
| Nền tảng | Java 17, Swing, SQLite (JDBC) |
| Look & Feel | FlatLaf 3.8 + FlatLaf Extras 3.8 (SVG) |
| Phong cách | Tím gradient, khung bo góc lớn, pill, vàng nhấn mạnh |

**Mục lục**

1. Tổng quan
2. Nguyên tắc thiết kế
3. Design tokens (màu, gradient, typography, spacing, radius, shadow, icon)
4. Tích hợp FlatLaf (dependency, theme, font)
5. Kiến trúc UI (package, luồng khởi động, quy ước)
6. Layout (shell, số đo, responsive, HiDPI)
7. Component library (spec + trạng thái)
8. Màn hình (Login, Dashboard, Projects, Task form, Comments)
9. Kỹ thuật render (gradient, clip, shadow, focus ring, animation)
10. Mapping dữ liệu
11. Ghi chú schema SQLite
12. Lộ trình triển khai
13. Checklist QA
14. Phụ lục đối chiếu CSS ↔ Swing
15. Definition of Done

---

## 1. Tổng quan

Taskia là giao diện desktop quản lý dự án và công việc theo nhóm. Mục tiêu thiết kế:

- **Một "shell" trắng bo 48px** nổi trên nền tím gradient, tạo cảm giác mềm và hiện đại.
- **Điều hướng dạng pill**: mỗi mục menu là một viên thuốc trắng, mục đang chọn tô vàng `#FFD66B`.
- **Nội dung sáng, thoáng**: nền `#ECEEF8`, card trắng bo 24px, bóng rất nhẹ.
- **Nhấn mạnh bằng màu trạng thái**: đỏ cho quá hạn, xanh cho hoàn thành, cam cho đang làm.
- **Thao tác chính luôn là nút tím gradient** với bóng tím, các thao tác phụ dùng outline.

Tài liệu này là nguồn tham chiếu duy nhất (single source of truth) cho màu sắc, typography,
khoảng cách, component và cách render trong Swing. Mọi màn hình mới **không được hard-code**
màu/kích thước, mà phải lấy từ package `ui.theme`.

### 1.1. Phạm vi

| Trong phạm vi | Ngoài phạm vi |
|---|---|
| Design tokens, theme, component library | Thiết kế logo, illustration gốc |
| Layout shell, sidebar, topbar, responsive | Ứng dụng web/mobile |
| Spec các màn hình Login, Dashboard, Projects, Task form, Comments | Hạ tầng CI/CD, đóng gói installer |
| Quy ước code UI | Nghiệp vụ phân quyền chi tiết |

---

## 2. Nguyên tắc thiết kế

1. **Nhất quán trước, sáng tạo sau.** Mọi nút, input, card dùng chung token và component. Không tự vẽ lại ở từng màn hình.
2. **Phân cấp thị giác rõ.** Tiêu đề 32/800 → tiêu đề card 18/700 → nội dung 14/400 → phụ 12/400 muted.
3. **Phản hồi tức thì.** Mọi phần tử tương tác có tối thiểu 4 trạng thái: default, hover, focus, disabled (thêm active/selected/error khi áp dụng).
4. **Bo tròn có chủ đích.** Card 24, input/button pill, shell 48. Không dùng bo góc tuỳ tiện.
5. **Màu có nghĩa.** Tím = hành động, vàng = đang chọn, đỏ = nguy hiểm/quá hạn, xanh = thành công, cam = cảnh báo.
6. **Truy cập được.** Focus ring luôn hiển thị khi dùng bàn phím; vùng bấm ≥ 44×44; tương phản chữ đạt WCAG AA.
7. **Chuyển động có kiểm soát.** Hover 150–200ms; tôn trọng cài đặt giảm chuyển động.

---

## 3. Design tokens

### 3.1. Màu sắc

| Token | Hex / giá trị | Hằng số Java | Dùng cho |
|---|---|---|---|
| `primary` | `#7A5CFA` | `Palette.PRIMARY` | Viền focus, icon nhấn, nút default |
| `primary-dark` | `#5B3FE0` | `Palette.PRIMARY_DARK` | Chữ/viền nhấn đậm, trạng thái selected |
| `primary-soft` | `#E9E4FF` | `Palette.PRIMARY_SOFT` | Focus ring, hover nền nhẹ, badge primary |
| `primary-gradient` | `#8B6DFF → #6A4BF0` (180°) | `Palette.PRIMARY_GRADIENT_*` | Nền nút primary |
| `bg-page` | `#7B5CFF → #4A2BD9` (135°) | `Palette.PAGE_GRADIENT_*` | Nền cửa sổ |
| `bg-shell` | `#FFFFFF` | `Palette.SHELL` | Sidebar, khung ngoài |
| `bg-content` | `#ECEEF8` | `Palette.CONTENT` | Vùng nội dung chính |
| `bg-card` | `#FFFFFF` | `Palette.CARD` | Card, topbar, panel |
| `text` | `#14122B` | `Palette.TEXT` | Chữ chính |
| `text-muted` | `#8D90A6` | `Palette.TEXT_MUTED` | Chữ phụ, ngày tháng |
| `text-muted-strong` | `#6E7189` (bổ sung) | `Palette.TEXT_MUTED_STRONG` | Chữ phụ cỡ nhỏ đạt AA |
| `text-placeholder` | `#A9ABBD` | `Palette.TEXT_PLACEHOLDER` | Placeholder |
| `border` | `#E7E9F3` | `Palette.BORDER` | Viền input, card, divider |
| `accent` | `#FFD66B` | `Palette.ACCENT` | Menu đang chọn |
| `accent-shadow` | `rgba(255,193,59,0.45)` | `Palette.ACCENT_SHADOW` | Bóng menu đang chọn |
| `focus-ring` | `rgba(122,92,250,0.45)` | `Palette.FOCUS_RING` | Viền focus 3–4px |
| `danger` | `#F04E4E` | `Palette.DANGER` | Lỗi, quá hạn, xoá |
| `danger-soft` | `#FFF1F1` | `Palette.DANGER_SOFT` | Nền hover nút nguy hiểm |
| `success` | `#1FA85A` | `Palette.SUCCESS` | Hoàn thành, dấu ✓ |
| `warning` | `#F5A524` | `Palette.WARNING` | Đang làm, sắp hạn |
| `task-blue` | `#CDEEFF` | `Palette.TASK_BLUE` | Nền icon task |
| `task-yellow` | `#FFE3A3` | `Palette.TASK_YELLOW` | Nền icon task |
| `task-purple` | `#D9D1FF` | `Palette.TASK_PURPLE` | Nền icon task |
| `success-soft` | `#E3F8EC` | `Palette.SUCCESS_SOFT` | Badge success |
| `warning-soft` | `#FFF1D1` | `Palette.WARNING_SOFT` | Badge warning |
| `empty-icon` | `#FFE9B8` | `Palette.EMPTY_ICON` | Vòng tròn empty state |

> **Lưu ý tương phản:** `#8D90A6` trên trắng chỉ đạt ~3.15:1, không đủ AA cho chữ 14px.
> Vì vậy thêm `TEXT_MUTED_STRONG = #6E7189` (~4.8:1) và dùng nó cho mọi đoạn chữ phụ dưới 18px.
> `#8D90A6` chỉ dùng cho chữ ≥ 18px hoặc chi tiết trang trí.

### 3.2. Gradient

| Gradient | Hướng | Điểm đầu → cuối | Ghi chú |
|---|---|---|---|
| Nền trang | 135° (trên-trái → dưới-phải) | `#7B5CFF` → `#4A2BD9` | `GradientPaint(0,0, w,h)` |
| Nút primary | 180° (trên → dưới) | `#8B6DFF` → `#6A4BF0` | `GradientPaint(0,y, 0,y+h)` |
| Vòng focus | phẳng | `rgba(122,92,250,0.45)` | Vẽ viền ngoài 3–4px |

### 3.3. Typography

Font thương hiệu: **Plus Jakarta Sans** (Google Fonts), weights 400/500/600/700/800.
Fallback: `Inter` → `Segoe UI` → `SansSerif`.

| Vai trò | Size (CSS px) | Weight | Hằng số |
|---|---|---|---|
| Logo chữ | 30 | 800 | `Typography.logo()` |
| Page title | 32 | 800 | `Typography.h1()` |
| Empty title / Panel title | 20 / 18 | 800 / 700 | `Typography.h2()` / `h3()` |
| Card title | 18 | 700 | `Typography.cardTitle()` |
| Button / Nav | 14 | 700 / 600 | `Typography.button()` / `nav()` |
| Body | 14 | 400 | `Typography.body()` |
| Label form | 14 | 700 | `Typography.label()` |
| Meta | 14 | 600 | `Typography.meta()` |
| Small / Badge | 12 | 700 | `Typography.small()` |
| Placeholder | 14 | 500 | `Typography.placeholder()` |

Quy ước:

- `line-height: 1.5` chỉ áp dụng cho văn bản nhiều dòng. Swing không có line-height → dùng `JTextArea` không viền + `BorderFactory` đệm, hoặc HTML có kiểm soát.
- `letter-spacing: -0.03em` (logo) và `-0.02em` (page title) → `TextAttribute.TRACKING` (`-0.03f`, `-0.02f`).
- **Quy đổi px → pt:** `pt = px × 0.75` (1 CSS px = 0.75 pt ở 96 DPI). Tập trung trong `UiScale.font()` để đổi một chỗ nếu muốn chữ to hơn.
- Kích thước layout (chiều cao nút, padding) giữ **logical px** đúng như CSS; Java 17 + FlatLaf tự nhân theo `uiScale`.

```java
public final class UiScale {
    /** Hệ số người dùng, mặc định 1.0 — có thể tăng nếu cần chữ lớn hơn. */
    public static final float USER = 1.0f;

    public static int px(float cssPx) {
        return Math.round(cssPx * USER);
    }

    public static float font(float cssPx) {
        return cssPx * 0.75f * USER;
    }
}
```

### 3.4. Spacing

Thang khoảng cách lấy trực tiếp từ CSS (đơn vị logical px):

| Token | Giá trị | Ứng dụng tiêu biểu |
|---|---|---|
| `Space.XXS` | 4 | Khoảng giữa icon và nhãn nhỏ |
| `Space.XS` | 8 | Gap giữa meta, icon–text |
| `Space.SM` | 12 | Gap nav item, gap button trong empty state |
| `Space.MD` | 16 | Gap topbar, padding select-card |
| `Space.LG` | 18 | Gap task card, margin field |
| `Space.XL` | 22 | Padding dọc task card, margin field |
| `Space.XXL` | 24 | Padding panel, gap section |
| `Space.X3L` | 28 | Padding shell/page, padding button |
| `Space.X4L` | 32 | Padding main @1024 |
| `Space.X5L` | 36 | Margin sidebar logo, margin topbar |
| `Space.X6L` | 48 | Padding main, sidebar logo padding |
| `Space.X7L` | 56 | Padding đáy main |

### 3.5. Bo góc

| Token | Giá trị | Dùng cho |
|---|---|---|
| `Radius.XS` | 10 | Chi tiết nhỏ |
| `Radius.SM` | 16 | Select card, bảng nhỏ |
| `Radius.MD` | 24 | Card, topbar, panel, empty state |
| `Radius.LG` | 32 | Main panel khi mobile |
| `Radius.XL` | 48 | Shell ngoài |
| `Radius.PILL` | `height / 2` | Button, input, nav item, badge |

Trong FlatLaf: `arc` là **bán kính góc** (bị kẹp về `min(width, height) / 2`), nên
`arc = 24` tương đương CSS `border-radius: 24px`. Pill: đặt `arc = 999`, FlatLaf tự kẹp thành `height / 2`.

> **Khác biệt quan trọng:** `Graphics2D.fillRoundRect` và `RoundRectangle2D` dùng **đường kính**
> của cung góc (`arcWidth = 2 × bán kính`). Vì vậy khi tự vẽ bằng Java2D phải truyền
> `radius × 2`, còn khi đặt qua FlatLaf (`arc`, style string) thì truyền `radius`.
> Quy ước trong code: hằng số `Radius.*` luôn là **bán kính**; helper `Painters.roundRect(...)`
> tự nhân đôi khi vẽ.

### 3.6. Đổ bóng

| Token | CSS | Cách dựng trong Swing |
|---|---|---|
| `shadow-sm` | `0 2 6 rgba(40,30,110,.04)` | 2–3 lớp viền alpha thấp |
| `shadow-md` | `0 8 24 rgba(40,30,110,.08)` | 4–6 lớp, offset y 4 |
| `shadow-primary` | `0 8 20 rgba(122,92,250,.35)` | Lớp màu tím, chỉ dưới nút primary |
| `shadow-accent` | `0 8 18 rgba(255,193,59,.45)` | Chỉ dưới nav item đang chọn |
| `shadow-shell` | `0 30 60 rgba(20,10,90,.25)` | Blur lớn, cache thành `BufferedImage` |

Swing không có `box-shadow`. Dùng một trong ba cách:

1. **Nhiều lớp `RoundRectangle2D`** với alpha giảm dần — đơn giản, đủ đẹp cho card/nút.
2. **`ConvolveOp` Gaussian blur** trên `BufferedImage` — đúng blur lớn, dùng cho shell.
3. **`FlatClientProperties.STYLE`** cho component có sẵn bóng (popup, tooltip) — không dùng cho panel thường.

Khuyến nghị: (1) cho component, (2) cho shell và cache lại, chỉ vẽ lại khi kích thước đổi.

### 3.7. Icon

- Định dạng nguồn: **SVG** (phong cách Lucide/Feather, stroke 2, `currentColor`).
- Kích thước hiển thị: 18, 20, 22, 40, 48 px (vector nên không cần @2x).
- Màu: tint bằng `FlatSVGIcon.ColorFilter` theo ngữ cảnh (primary, muted, danger, trắng).
- Đặt tại `src/main/resources/icons/`.

Bộ icon tối thiểu:

```
logo, dashboard, projects, tasks, calendar, users, settings,
search, bell, plus, check, clock, flag, trash, edit, chevron-right
```

---

## 4. Tích hợp FlatLaf

### 4.1. Dependency

Thêm vào `pom.xml` (cùng version, cập nhật khi nâng cấp):

```xml
<dependency>
  <groupId>com.formdev</groupId>
  <artifactId>flatlaf</artifactId>
  <version>3.8</version>
</dependency>
<dependency>
  <groupId>com.formdev</groupId>
  <artifactId>flatlaf-extras</artifactId>
  <version>3.8</version>
</dependency>
```

`flatlaf-extras` cung cấp `FlatSVGIcon` (đọc SVG, tint màu, scale vector) và các tiện ích icon.

### 4.2. TaskiaLaf

Kế thừa `FlatLightLaf` để có một L&F riêng, dễ mở rộng:

```java
public final class TaskiaLaf extends FlatLightLaf {
    public static boolean setup() {
        return FlatLaf.setup(new TaskiaLaf());
    }

    @Override
    public String getName() { return "Taskia Light"; }
}
```

### 4.3. Thứ tự khởi tạo

```java
public final class Theme {
    public static void install() {
        Typography.load();                 // 1. nạp font trước
        FlatLaf.setGlobalExtraDefaults(Map.ofEntries(
            Map.entry("Component.arc", "16"),
            Map.entry("Button.arc", "999"),
            Map.entry("TextComponent.arc", "999"),
            Map.entry("Component.focusWidth", "3"),
            Map.entry("Component.innerFocusWidth", "1"),
            Map.entry("Component.focusColor", "#7A5CFA"),
            Map.entry("Component.borderColor", "#E7E9F3"),
            Map.entry("ScrollBar.width", "10"),
            Map.entry("ScrollBar.thumbArc", "999"),
            Map.entry("ScrollBar.track", "rgba(0,0,0,0)"),
            Map.entry("ScrollBar.thumb", "#D6D9E9")
        ));
        TaskiaLaf.setup();                 // 2. cài L&F
        applyDefaults();                   // 3. Color/Font (không nhận string)
        FlatLaf.updateUI();                // 4. áp dụng cho component đã tạo
    }

    private static void applyDefaults() {
        UIManager.put("defaultFont", Typography.body());
        UIManager.put("Label.foreground", Palette.TEXT);
        UIManager.put("Label.disabledForeground", Palette.TEXT_MUTED);
        UIManager.put("Panel.background", Palette.CONTENT);
        UIManager.put("Button.foreground", Palette.TEXT);
        UIManager.put("TextField.foreground", Palette.TEXT);
        UIManager.put("TextField.placeholderForeground", Palette.TEXT_PLACEHOLDER);
        UIManager.put("Component.focusColor", Palette.FOCUS_RING);
        UIManager.put("ToolTip.background", Palette.TEXT);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("Table.rowHeight", UiScale.px(48));
    }
}
```

> `setGlobalExtraDefaults` chỉ nhận `String`, phải gọi **trước** `setup()`.
> Các giá trị `Color`, `Font`, `Integer` đặt bằng `UIManager.put` **sau** `setup()`.

### 4.4. Nạp Plus Jakarta Sans

```java
public final class Typography {
    public enum Weight { REGULAR, MEDIUM, SEMIBOLD, BOLD, EXTRABOLD }

    private static final Map<Weight, Font> FONTS = new EnumMap<>(Weight.class);

    public static void load() {
        register(Weight.REGULAR,   "/fonts/PlusJakartaSans-Regular.ttf");
        register(Weight.MEDIUM,    "/fonts/PlusJakartaSans-Medium.ttf");
        register(Weight.SEMIBOLD,  "/fonts/PlusJakartaSans-SemiBold.ttf");
        register(Weight.BOLD,      "/fonts/PlusJakartaSans-Bold.ttf");
        register(Weight.EXTRABOLD, "/fonts/PlusJakartaSans-ExtraBold.ttf");
    }

    private static void register(Weight weight, String resource) {
        try (InputStream in = Typography.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Thiếu font " + resource);
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            FONTS.put(weight, font);
        } catch (Exception e) {
            FONTS.put(weight, new Font("Segoe UI", styleOf(weight), 14));
        }
    }

    private static int styleOf(Weight w) {
        return switch (w) {
            case REGULAR -> Font.PLAIN;
            case MEDIUM, SEMIBOLD -> Font.PLAIN;   // file font đã chứa weight
            case BOLD, EXTRABOLD -> Font.BOLD;
        };
    }

    public static Font of(Weight w, float pt) {
        return FONTS.get(w).deriveFont(pt);
    }

    public static Font body()      { return of(Weight.REGULAR,  UiScale.font(14)); }
    public static Font label()     { return of(Weight.BOLD,     UiScale.font(14)); }
    public static Font meta()      { return of(Weight.SEMIBOLD, UiScale.font(14)); }
    public static Font small()     { return of(Weight.BOLD,     UiScale.font(12)); }
    public static Font button()    { return of(Weight.BOLD,     UiScale.font(14)); }
    public static Font nav()       { return of(Weight.SEMIBOLD, UiScale.font(14)); }
    public static Font cardTitle() { return of(Weight.BOLD,     UiScale.font(18)); }
    public static Font h3()        { return of(Weight.BOLD,     UiScale.font(18)); }
    public static Font h2()        { return of(Weight.EXTRABOLD, UiScale.font(20)); }
    public static Font h1()        { return tracked(of(Weight.EXTRABOLD, UiScale.font(32)), -0.02f); }
    public static Font logo()      { return tracked(of(Weight.EXTRABOLD, UiScale.font(30)), -0.03f); }

    private static Font tracked(Font font, float em) {
        return font.deriveFont(Map.of(TextAttribute.TRACKING, em));
    }
}
```

### 4.5. Dùng style string của FlatLaf cho component đơn giản

FlatLaf cho phép khai báo style inline, rất tiện để tránh hard-code rải rác:

```java
panel.putClientProperty(FlatClientProperties.STYLE,
        "arc: 24; background: #FFFFFF; borderColor: #E7E9F3");
```

Các key hỗ trợ phổ biến: `arc`, `background`, `foreground`, `borderColor`, `focusWidth`,
`focusColor`, `innerFocusWidth`, `padding`, `minimumWidth`, `toolbar`.

> Quy ước dự án: chỉ dùng style string cho **biến thể cục bộ**; token toàn cục vẫn nằm trong
> `Palette`/`Typography`. Không viết hex trực tiếp trong màn hình.

---

## 5. Kiến trúc UI

### 5.1. Cấu trúc package

```
com.java.teamtask
├── App.java                          # điểm vào, gọi Theme.install()
├── connections/
│   └── SQL.java                      # tạo bảng, kết nối SQLite
├── model/                            # POJO: User, Project, Task, Comment
├── dao/                              # truy vấn JDBC
├── service/                          # nghiệp vụ (tuỳ chọn)
└── ui/
    ├── MainFrame.java                # JFrame native title bar + CardLayout
    ├── AppRootPanel.java             # nền gradient + đặt ShellPanel
    ├── theme/
    │   ├── Palette.java              # màu
    │   ├── Typography.java           # font, weight
    │   ├── Space.java                # spacing
    │   ├── Radius.java               # bo góc
    │   ├── Shadows.java              # vẽ bóng
    │   ├── Icons.java                # FlatSVGIcon + tint
    │   ├── UiScale.java              # quy đổi px/pt
    │   ├── Theme.java                # cài đặt L&F
    │   └── TaskiaLaf.java
    ├── components/
    │   ├── ShellPanel.java           # bo 48, clip con
    │   ├── GradientPanel.java        # nền gradient
    │   ├── Card.java                 # panel bo 24 + shadow-sm
    │   ├── AppButton.java            # primary / outline / danger-outline / block
    │   ├── NavItem.java              # JToggleButton pill
    │   ├── SearchField.java
    │   ├── IconButton.java           # tròn 48 + dot
    │   ├── Avatar.java
    │   ├── PageHeader.java
    │   ├── TaskCard.java
    │   ├── Badge.java
    │   ├── FormField.java
    │   ├── SelectableCard.java
    │   ├── EmptyState.java
    │   └── TopBar.java
    └── screens/
        ├── LoginScreen.java
        ├── DashboardScreen.java
        ├── ProjectsScreen.java
        ├── TaskFormScreen.java
        └── CommentsPanel.java
```

### 5.2. Luồng khởi động

```
App.main
  └── Theme.install()
        ├── Typography.load()
        ├── load global defaults
        └── TaskiaLaf.setup()
  └── SwingUtilities.invokeLater
        └── new MainFrame()
              ├── AppRootPanel (gradient)
              │     └── ShellPanel
              │           ├── Sidebar (270px)
              │           └── MainPanel (BorderLayout)
              │                 ├── North: TopBar
              │                 └── Center: JScrollPane(CardLayout screens)
              └── setMinimumSize(1024 × 700)
```

### 5.3. Quy ước code UI

- Mỗi component công khai một **API ngữ nghĩa**, không lộ chi tiết paint:
  `new AppButton("Tạo dự án", Variant.PRIMARY)`, `new Badge("Hoàn thành", Badge.Kind.SUCCESS)`.
- Màu/kích thước chỉ lấy từ `Palette`, `Space`, `Radius`, `Typography`.
- Không đặt `setBackground`/`setForeground` bằng hex trong `screens/`.
- Mỗi component tự xử lý focus ring và trạng thái disabled.
- Ưu tiên layout manager; chỉ override `doLayout()` cho shell và các trường hợp đặc biệt.
- Tên class: `PascalCase`; hằng số: `UPPER_SNAKE`; biến: `camelCase`.
- Comment ngắn bằng tiếng Việt hoặc tiếng Anh, thống nhất trong một file.

---

## 6. Layout

### 6.1. Cấu trúc shell

```
AppRootPanel  (nền gradient 135°, padding 28)
└── ShellPanel (trắng, bo 48, shadow-shell, clip con)
    ├── Sidebar (270, padding 36 28)
    │   ├── Logo (40 icon + chữ 30/800)
    │   ├── Label "MENU"
    │   ├── Nav list (gap 12)
    │   ├── Divider (1px #E7E9F3)
    │   ├── Label "PROJECTS"
    │   └── Nav list
    └── MainPanel (nền #ECEEF8, bo trái 48, padding 48 48 56)
        ├── TopBar (white, radius 24, padding 16 24, margin-bottom 36)
        │   ├── SearchField (max 400, height 48)
        │   └── Actions: IconButton(bell, dot) + User (avatar 48 + tên)
        ├── PageHeader (title 32/800 + subtitle + action)
        └── Content (scroll)
```

### 6.2. Số đo chuẩn

| Khu vực | Giá trị |
|---|---|
| Padding ngoài trang | 28 |
| Shell max width | 1280, căn giữa |
| Shell radius | 48 |
| Sidebar width | 270 (220 ở ≤1024) |
| Sidebar padding | 36 28 (28 18 ở ≤1024) |
| Logo | icon 40, chữ 30/800, margin-bottom 36 |
| Nav item | cao 48, padding 0 18, gap 12, icon 20, radius pill |
| Divider | 1px, margin 28 0 24 |
| Main padding | 48 48 56 |
| TopBar | radius 24, padding 16 24, margin-bottom 36 |
| Search | cao 48, max-width 400, icon phải 22 |
| IconButton | 48 × 48, tròn |
| Avatar | 48 × 48, tròn |
| Page title | 32/800 |
| Page header margin-bottom | 24 |
| Task list gap | 18 |
| Task card | radius 24, padding 22 28, gap 24 |
| Button | cao 48, padding 0 28, radius pill |
| Input | cao 52, padding 0 18 |
| Field margin-bottom | 22 |
| Badge | cao 28, padding 0 12, 12/700 |
| Empty state | max-width 340, padding 28 32 32, lề trên 72 |
| Empty icon | 80 × 80, nền #FFE9B8 |

### 6.3. Căn shell bằng `doLayout`

Không dùng `GridBagLayout` cho shell vì khó giới hạn `max-width` chính xác. Override `doLayout`:

```java
public final class AppRootPanel extends GradientPanel {
    private final ShellPanel shell;

    public AppRootPanel(ShellPanel shell) {
        this.shell = shell;
        setLayout(null);
        add(shell);
    }

    @Override
    public void doLayout() {
        Insets pad = Layout.paddingFor(getWidth());
        int w = Math.min(getWidth() - pad.left - pad.right, Layout.SHELL_MAX_WIDTH);
        int h = getHeight() - pad.top - pad.bottom;
        int x = (getWidth() - w) / 2;
        shell.setBounds(x, pad.top, w, h);
    }
}
```

### 6.4. Responsive

Swing không có media query → dùng breakpoint trong `Layout` và gọi lại khi cửa sổ đổi kích thước:

```java
public final class Layout {
    public static final int SHELL_MAX_WIDTH = 1280;
    public static final int BP_MD = 1024;
    public static final int BP_SM = 768;

    public static Insets paddingFor(int width) {
        if (width < BP_SM) return new Insets(0, 0, 0, 0);
        if (width < BP_MD) return new Insets(20, 20, 20, 20);
        return new Insets(28, 28, 28, 28);
    }

    public static boolean isCompact(int width) { return width < BP_MD; }
    public static boolean isStacked(int width) { return width < BP_SM; }
}
```

| Breakpoint | Sidebar | Main | Khác |
|---|---|---|---|
| ≥ 1024 | 270 / padding 36 28 | padding 48 48 56 | Mặc định |
| 768–1023 | 220 / padding 28 18 | padding 32 28 | Topbar gọn |
| < 768 | full width, nằm trên | radius trên 32, padding 24 16 40 | Nav wrap ngang, search xuống dòng, task card dọc, title 26 |

`MainFrame` gắn `ComponentListener`:

```java
addComponentListener(new ComponentAdapter() {
    @Override public void componentResized(ComponentEvent e) {
        layoutManager.applyBreakpoint(getContentPane().getWidth());
        revalidate();
        repaint();
    }
});
```

### 6.5. HiDPI

- Java 17 tự bật `sun.java2d.uiScale` trên Windows; giữ nguyên, không tự nhân đôi kích thước.
- Icon SVG luôn sắc nét; ảnh avatar dùng `RenderingHints.VALUE_INTERPOLATION_BICUBIC`.
- Kiểm thử ở scale 100%, 125%, 150%, 200%.
- Không hard-code `setPreferredSize` cho text; dùng font metrics để tránh cắt chữ khi scale.

---

## 7. Component library

### 7.1. Ma trận trạng thái

| Component | Default | Hover | Active/Pressed | Focus | Selected | Disabled | Error |
|---|---|---|---|---|---|---|---|
| `AppButton` (primary) | gradient tím + shadow | shadow đậm hơn | scale 0.98 | ring 3px | — | opacity 50% | — |
| `AppButton` (outline) | trắng, viền text | nền `#F4F4FB` | scale 0.98 | ring 3px | — | opacity 50% | — |
| `AppButton` (danger) | trong suốt, viền đỏ | nền `#FFF1F1` | scale 0.98 | ring 3px | — | opacity 50% | — |
| `NavItem` | trắng, viền border | nền `#F8F7FF` | — | ring 3px | nền vàng + shadow vàng | opacity 50% | — |
| `SearchField` | viền border | — | — | viền primary + ring 4px | — | nền xám nhạt | — |
| `IconButton` | trắng, viền border | nền `#E9E4FF` | scale 0.96 | ring 3px | — | opacity 50% | — |
| `TaskCard` | trắng, radius 24 | shadow-md | — | — | — | — | — |
| `FormField` | viền border | — | — | viền primary + ring 4px | — | nền xám | viền 2px đỏ + text lỗi |
| `SelectableCard` | nền content, viền 2px | nền `#F5F3FF` | — | ring 3px | viền primary-dark + ring + ✓ | opacity 50% | — |
| `Badge` | màu theo kind | — | — | — | — | — | — |

### 7.2. `AppButton`

```java
public final class AppButton extends JButton {
    public enum Variant { PRIMARY, OUTLINE, DANGER_OUTLINE }

    public AppButton(String text, Variant variant) {
        super(text);
        setFont(Typography.button());
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setBorder(new EmptyBorder(0, Space.X3L, 0, Space.X3L));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, UiScale.px(48)));
        // màu chữ theo variant, icon gap 8
        setIconTextGap(Space.XS);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = getHeight();
        boolean pressed = getModel().isArmed() && getModel().isPressed();
        if (pressed) { // scale 0.98 quanh tâm
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            g2.scale(0.98, 0.98);
            g2.translate(-getWidth() / 2.0, -getHeight() / 2.0);
        }

        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);
        switch (variant) {
            case PRIMARY -> {
                if (isEnabled()) {
                    Shadows.paint(g2, getBounds(), getHeight() / 2, Shadows.PRIMARY);
                    g2.setPaint(new GradientPaint(
                            0, 0, Palette.PRIMARY_GRADIENT_TOP,
                            0, getHeight(), Palette.PRIMARY_GRADIENT_BOTTOM));
                } else {
                    g2.setColor(Palette.PRIMARY_SOFT);
                }
                g2.fill(shape);
            }
            case OUTLINE -> {
                g2.setColor(Palette.CARD); g2.fill(shape);
                g2.setColor(Palette.TEXT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(shape);
            }
            case DANGER_OUTLINE -> {
                g2.setColor(Palette.CARD); g2.fill(shape);
                g2.setColor(Palette.DANGER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(shape);
            }
        }

        if (isFocusOwner()) {
            Painters.focusRing(g2, getBounds(), getHeight() / 2);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
```

Quy tắc:

- Cao 48; `block` = `setMaximumSize`/`setPreferredSize` full width (dùng `BoxLayout` hoặc `GridBagLayout`).
- Nút primary luôn có icon trái (nếu có) + gap 8.
- Không dùng nút primary quá 1 cái trong một vùng hành động.

### 7.3. `NavItem`

- `JToggleButton` trong `ButtonGroup` để đảm bảo chỉ một mục active.
- Cao 48, padding 0 18, icon 20, gap 12, radius pill, viền 1px `BORDER`.
- Hover: nền `#F8F7FF`.
- Selected: nền `ACCENT`, viền `ACCENT`, shadow accent; icon và chữ màu `TEXT`.
- Kèm `setFocusable(true)` + ring 3px.
- Nhóm menu có label 12/700 và divider 1px.

### 7.4. `SearchField`

```java
public final class SearchField extends JTextField {
    public SearchField() {
        setFont(Typography.body());
        setPreferredSize(new Dimension(400, UiScale.px(48)));
        setMaximumSize(new Dimension(400, UiScale.px(48)));
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm công việc...");
        putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                Icons.muted("search", 22));
        putClientProperty(FlatClientProperties.STYLE,
                "arc: 999; padding: 0,22,0,22; focusWidth: 3");
    }
}
```

- Focus: viền `PRIMARY`, ring 4px `PRIMARY_SOFT` (FlatLaf `focusWidth` + `focusColor`).
- Bo pill; icon phải 22.
- Có thể bật `showClearButton` khi có nội dung.

### 7.5. `IconButton`

- 48 × 48, tròn (`arc = 999`), viền 1px, nền trắng.
- Hover: nền `PRIMARY_SOFT`.
- Dot thông báo: đường kính 9, viền trắng 2px, đặt góc trên-phải (8, 9), vẽ sau `super.paintComponent`.
- `setToolTipText` bắt buộc cho mọi icon-only button (accessibility).

```java
@Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (!hasDot) return;
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Palette.CARD);
    g2.fillOval(getWidth() - 17, 6, 13, 13);
    g2.setColor(Palette.DANGER);
    g2.fillOval(getWidth() - 15, 8, 9, 9);
    g2.dispose();
}
```

### 7.6. `Avatar`

- 48 × 48, clip tròn, nền `PRIMARY_SOFT` khi chưa có ảnh.
- Ảnh scale `cover` + `VALUE_INTERPOLATION_BICUBIC`.
- Fallback: chữ cái đầu của tên, 16/800, màu `PRIMARY_DARK`.

### 7.7. `TaskCard`

- `Card` bo 24, padding 22 28, gap 24; hover đổi sang `shadow-md`.
- Trái: `TaskIcon` tròn 48 (nền blue/yellow/purple) + title 18/700 + date 14 muted.
- Meta row: gap 8–20, icon 18, màu theo trạng thái (`meta--danger/success/warning`).
- Phải: nhóm hành vi (badge trạng thái, menu `…`, hoặc nút "Chi tiết").
- Responsive `<768`: chuyển `BorderLayout` dọc, căn trái.

Ánh xạ trạng thái:

| `taskStatus` | Màu | Badge |
|---|---|---|
| `TODO` | `TEXT_MUTED` | `Badge.Kind.NEUTRAL` |
| `IN_PROGRESS` | `WARNING` | `WARNING` |
| `DONE` | `SUCCESS` | `SUCCESS` |
| `OVERDUE` | `DANGER` | `DANGER` |

### 7.8. `Badge`

```java
public final class Badge extends JLabel {
    public enum Kind { PRIMARY, SUCCESS, DANGER, WARNING, NEUTRAL }

    public Badge(String text, Kind kind) {
        super(text.toUpperCase(Locale.ROOT));
        setFont(Typography.small());
        setBorder(new EmptyBorder(0, Space.SM, 0, Space.SM));
        setOpaque(false);
        setForeground(colorOf(kind));
        setPreferredSize(new Dimension(getPreferredSize().width, UiScale.px(28)));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundOf(kind));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
```

### 7.9. `FormField`

Cấu trúc:

```
FormField (JPanel, BoxLayout Y)
├── label (14/700)
├── FieldControl (JPanel, pill 52, viền 1px, icon 22 + input)
│   └── JTextField / JPasswordField / JComboBox
└── errorLabel (12/600, DANGER, ẩn mặc định)
```

- Focus-within: thêm `FocusListener` cho input → đổi viền sang `PRIMARY` + ring `PRIMARY_SOFT`.
- Error: `border = 2px DANGER`, hiện `errorLabel`, gọi `revalidate()`.
- `JComboBox` dùng `FlatClientProperties.STYLE` để bỏ mũi tên mặc định và thêm icon `chevron-down` bên phải.
- Không đặt label bên trong input; luôn có label phía trên (trừ search).

### 7.10. `SelectableCard`

- `JToggleButton` trong `ButtonGroup`, radius 16, viền 2px.
- Default: nền `CONTENT`, viền `BORDER`.
- Hover: nền `#F5F3FF`.
- Selected: nền trắng, viền `PRIMARY_DARK`, ring 3px `PRIMARY_SOFT`, vẽ ✓ tròn 22 màu `SUCCESS` ở phải.
- Nội dung: tên 17/700 + mô tả 14 muted.

### 7.11. `EmptyState`

- Card max 340, padding 28 32 32, lề trên 72, căn giữa.
- Icon tròn 80 nền `#FFE9B8`, icon 40 màu `WARNING`.
- Title 20/800, desc muted, 2 nút (primary + outline) gap 12.
- Dùng khi danh sách rỗng; không hiển thị cùng lúc với task list.

### 7.12. `TopBar`

- Card trắng radius 24, padding 16 24, margin-bottom 36.
- `BorderLayout`: trái = search (max 400), phải = actions (bell + user).
- User block: avatar 48 + text phải (`Xin chào` 12 muted / tên 14–700), divider dọc 1px trước user.
- `<768`: search xuống dòng, chiếm toàn bộ, actions giữ trên.

---

## 8. Màn hình

### 8.1. Login

| Thành phần | Spec |
|---|---|
| Nền | gradient trang |
| Card | trắng, max-width 480, radius 24, padding 32 |
| Logo | icon 40 + "Taskia" 30/800 |
| Field | username, password (pill 52) |
| Nút | primary block 48 "Đăng nhập" |
| Lỗi | viền đỏ 2px + text 12 |
| Link | "Quên mật khẩu?" 14/600 màu primary-dark |

### 8.2. Dashboard

- Sidebar active = "Tổng quan".
- TopBar + PageHeader ("Công việc của tôi", subtitle đếm task).
- Nút primary "Tạo công việc" ở góc phải page header.
- Task list dọc, gap 18.
- Rỗng → `EmptyState`.

### 8.3. Projects

- Page header + nút primary "Tạo dự án".
- Panel trắng radius 24, title "Dự án của bạn" 18/700.
- Danh sách `SelectableCard` (2 cột nếu ≥ 1024, 1 cột nếu nhỏ hơn).
- Chọn dự án → cập nhật task list bên phải hoặc chuyển màn hình.

### 8.4. Task form (tạo/sửa)

- `form-card` max-width 640, padding 28, radius 24.
- Field: tên, mô tả (`JTextArea` nhiều dòng), deadline, trạng thái, dự án, người phụ trách, độ ưu tiên.
- Hàng nút: primary "Lưu" + outline "Huỷ", căn phải, gap 12.
- Validate inline: field lỗi hiển thị viền đỏ + message; focus vào field lỗi đầu tiên khi submit.

### 8.5. Comments

- Panel phải (drawer) hoặc dưới task detail.
- Danh sách bình luận: avatar 32 + tên 14/700 + thời gian 12 muted + nội dung 14.
- Ô nhập pill 48 + nút icon gửi.
- Cuộn mượt, tự cuộn xuống bình luận mới.

### 8.6. Thông báo

- `IconButton` chuông có dot khi có thông báo chưa đọc.
- Click mở popup/màn hình danh sách; mỗi item có badge trạng thái.

---

## 9. Kỹ thuật render

### 9.1. Nền gradient

```java
public class GradientPanel extends JPanel {
    public GradientPanel() { setOpaque(false); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(
                0, 0, Palette.PAGE_GRADIENT_START,
                getWidth(), getHeight(), Palette.PAGE_GRADIENT_END));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
```

### 9.2. Shell bo góc + clip con

`JPanel` không tự cắt con theo góc bo. Override `paint`:

```java
public final class ShellPanel extends JPanel {
    private static final int RADIUS = 48;
    private static final int ARC = RADIUS * 2; // Java2D dùng đường kính cung góc

    public ShellPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    @Override public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1) bóng đổ (vẽ trước, ngoài vùng clip)
        Shadows.paintShell(g2, new Rectangle(0, 0, getWidth(), getHeight()), RADIUS);

        // 2) nền trắng bo góc
        g2.setColor(Palette.SHELL);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

        // 3) clip để con không tràn góc
        g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC, ARC));
        super.paint(g2);
        g2.dispose();
    }
}
```

> Lưu ý: `super.paint(g2)` sẽ gọi `paintComponent` (nền trong suốt), `paintBorder`, `paintChildren`.
> Các panel con nên là `opaque = true` để lấp kín vùng, tránh lộ nền gradient ở khe.

### 9.3. Shadow

```java
public final class Shadows {
    public record Shadow(Color color, int blur, int offsetY) {}

    public static final Shadow PRIMARY = new Shadow(new Color(122, 92, 250, 90), 20, 8);
    public static final Shadow ACCENT  = new Shadow(new Color(255, 193, 59, 115), 18, 8);
    public static final Shadow SM      = new Shadow(new Color(40, 30, 110, 10), 6, 2);
    public static final Shadow MD      = new Shadow(new Color(40, 30, 110, 20), 24, 8);

    /** @param radius bán kính góc; helper tự nhân đôi cho Java2D */
    public static void paint(Graphics2D g2, Rectangle r, int radius, Shadow s) {
        for (int i = s.blur; i > 0; i--) {
            int alpha = Math.max(0, s.color.getAlpha() / s.blur);
            int arc = (radius + i / 2) * 2;
            g2.setColor(new Color(s.color.getRed(), s.color.getGreen(), s.color.getBlue(), alpha));
            g2.fillRoundRect(r.x - i / 2, r.y - i / 2 + s.offsetY,
                    r.width + i, r.height + i, arc, arc);
        }
    }

    /** Bóng lớn cho shell: nên cache vào BufferedImage theo kích thước. @param radius bán kính góc */
    public static void paintShell(Graphics2D g2, Rectangle r, int radius) { /* Gaussian blur */ }
}
```

- `shadow-primary` chỉ dành cho nút primary; `shadow-accent` chỉ cho nav active.
- Không lạm dụng bóng: card thường chỉ cần `SM` hoặc không bóng.

### 9.4. Focus ring

```java
public final class Painters {
    /** Vẽ nền bo góc: nhận bán kính, tự nhân đôi cho Java2D. */
    public static void roundRect(Graphics2D g2, int x, int y, int w, int h, int radius, Color color) {
        g2.setColor(color);
        g2.fillRoundRect(x, y, w, h, radius * 2, radius * 2);
    }

    public static void focusRing(Graphics2D g2, Rectangle r, int radius) {
        g2.setColor(Palette.FOCUS_RING);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(1, 1, r.width - 3, r.height - 3, radius * 2, radius * 2);
    }
}
```

- Ring luôn nằm ngoài viền component, không đè nội dung.
- Chỉ vẽ khi `isFocusOwner()` hoặc `hasFocus()`.
- Với input dùng cơ chế `focusWidth` của FlatLaf (đã cấu hình toàn cục).

### 9.5. Chuyển động

```java
public final class Animations {
    public static final boolean ENABLED =
            !Boolean.getBoolean("taskia.reducedMotion");

    public static void hover(JComponent c, Color from, Color to, IntConsumer setter) {
        if (!ENABLED) { setter.accept(to.getRGB()); return; }
        long start = System.nanoTime();
        int durationMs = 180;
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            float t = Math.min(1f, (System.nanoTime() - start) / 1_000_000f / durationMs);
            float eased = 1 - (1 - t) * (1 - t) * (1 - t); // ease-out cubic
            setter.accept(blend(from, to, eased).getRGB());
            if (t >= 1f) ((Timer) e.getSource()).stop();
        });
        timer.start();
    }
}
```

- Hover 150–200ms; pressed scale 0.98 (button) / 0.96 (icon button).
- Tắt toàn bộ khi `-Dtaskia.reducedMotion=true` hoặc khi người dùng chọn trong Settings.
- Không animate layout (gây giật); chỉ animate màu, opacity, scale.

### 9.6. Anti-aliasing & text

- `KEY_ANTIALIASING = ON` cho mọi custom paint.
- `KEY_STROKE_CONTROL = PURE` để viền 1px sắc nét.
- Text: để FlatLaf xử lý; không bật LCD subpixel trên nền gradient/alpha.
- Viền 1px nên vẽ ở toạ độ `x + 0.5f` để không bị nhoè.

### 9.7. Scrollbar & cuộn

- `JScrollPane` trong suốt: `setOpaque(false)`, viewport `setOpaque(false)`, border rỗng.
- Scrollbar mảnh 10px, thumb bo pill, track trong suốt.
- Bật cuộn chuột mượt mặc định; không tự custom wheel nếu không cần.

---

## 10. Mapping dữ liệu

| Màn hình / component | Bảng | Ghi chú |
|---|---|---|
| Login | `users` | `username`, `password`; hiển thị lỗi chung, không tiết lộ trường nào sai |
| Sidebar user | `users` | `fname`, `lname` → tên hiển thị; avatar suy ra từ chữ cái đầu |
| Dashboard task list | `Tasks` + `Projects` | lọc theo `user_id`; join project để hiện tên dự án |
| Projects screen | `Projects`, `ProjectsAssgiments` | `role` = owner/member hiển thị badge |
| Task form | `Tasks` | validate deadline > hiện tại; status enum |
| Comments | `Comments` + `users` | `fromwhom` → tên + avatar; sắp xếp theo thời gian |
| Tìm kiếm | `Tasks`, `Projects` | `LIKE` trên `taskName`, `Name` |

Trạng thái task (đề xuất enum trong Java):

```java
public enum TaskStatus {
    TODO("Cần làm",      Badge.Kind.NEUTRAL, Palette.TEXT_MUTED),
    IN_PROGRESS("Đang làm", Badge.Kind.WARNING, Palette.WARNING),
    DONE("Hoàn thành",    Badge.Kind.SUCCESS, Palette.SUCCESS),
    OVERDUE("Quá hạn",    Badge.Kind.DANGER,  Palette.DANGER);

    public final String label;
    public final Badge.Kind badge;
    public final Color color;
}
```

---

## 11. Ghi chú về schema hiện tại

Các điểm cần sửa trong `connections/SQL.java` để UI hoạt động đúng:

| Vấn đề | Hiện tại | Đề xuất |
|---|---|---|
| Khoá chính tự tăng | `id INT auto_increment primary key` | SQLite yêu cầu `INTEGER PRIMARY KEY AUTOINCREMENT` (hoặc `INTEGER PRIMARY KEY`) |
| Trạng thái task | `taskStatus DATETIME not null` | `taskStatus TEXT NOT NULL DEFAULT 'TODO' CHECK (taskStatus IN ('TODO','IN_PROGRESS','DONE','OVERDUE'))` |
| Mật khẩu | `password varchar(20)` | Không lưu plaintext; hash BCrypt cần `varchar(100)` |
| Email | `gmail varchar(30)` | Đổi tên `email`, `varchar(120)` |
| Thời gian | `DATETIME` | Lưu ISO-8601 `TEXT` (SQLite không có kiểu DATETIME thật) |
| Comments | `taskid INT auto_increment primary key` | `commentid` riêng + `task_id` tham chiếu `Tasks` |
| Tên bảng | `ProjectsAssgiments`, `Create_at` | Thống nhất `project_assignments`, `created_at` (không bắt buộc nhưng nên) |

UI không phụ thuộc trực tiếp vào tên cột: dùng `model` + `dao` để ánh xạ, tránh sửa component khi schema đổi.

---

## 12. Lộ trình triển khai

| Phase | Nội dung | Kết quả |
|---|---|---|
| 1. Nền tảng | Thêm FlatLaf, tạo `theme/` (Palette, Typography, Space, Radius, Shadows, Icons, UiScale, Theme, TaskiaLaf), bundle font + icon | App chạy với font/màu đúng, không hard-code |
| 2. Shell & điều hướng | `AppRootPanel`, `ShellPanel`, `TopBar`, `Sidebar`, `NavItem`, responsive | Khung app giống mockup, resize đúng breakpoint |
| 3. Component | `AppButton`, `SearchField`, `IconButton`, `Avatar`, `Card`, `Badge`, `FormField`, `SelectableCard`, `TaskCard`, `EmptyState` | Bộ component tái sử dụng, đủ trạng thái |
| 4. Màn hình | Login, Dashboard, Projects, Task form, Comments + nối DAO | Luồng nghiệp vụ chạy end-to-end |
| 5. Hoàn thiện | Accessibility, HiDPI, reduced-motion, QA checklist | Đạt Definition of Done |

Thứ tự ưu tiên khi thiếu thời gian: Phase 1 → 2 → 3 (chỉ các component đang dùng) → 4.

---

## 13. Checklist QA giao diện

### Màu & typography

- [ ] Không có hex/rgb nào ngoài `Palette` (grep `#` trong `ui/screens` và `ui/components`).
- [ ] Mọi text dùng đúng cấp `Typography`; không `new Font(...)` rải rác.
- [ ] Chữ phụ dưới 18px dùng `TEXT_MUTED_STRONG` (đạt AA).
- [ ] Gradient đúng hướng: trang 135°, nút primary 180°.

### Layout

- [ ] Shell max 1280, căn giữa, padding 28.
- [ ] Sidebar 270 → 220 → stacked đúng breakpoint.
- [ ] Không cắt chữ ở scale 125%/150%/200%.
- [ ] Cửa sổ tối thiểu 1024×700 vẫn dùng được.

### Component

- [ ] Mọi nút/ô nhập có hover + focus + disabled.
- [ ] Vùng bấm ≥ 44×44; icon-only có tooltip + accessible name.
- [ ] Input lỗi có viền đỏ 2px + message, focus vào field lỗi đầu tiên.
- [ ] Badge và meta task dùng đúng màu trạng thái.
- [ ] Nav active dùng vàng + shadow vàng, chỉ một mục active.

### Hành vi

- [ ] Tab đi hết các phần tử tương tác theo thứ tự thị giác.
- [ ] Enter/Space kích hoạt nút và nav; Esc đóng dialog.
- [ ] `-Dtaskia.reducedMotion=true` tắt mọi animation.
- [ ] Loading/empty/error đều có trạng thái hiển thị rõ ràng.

### HiDPI & đa nền tảng

- [ ] Icon SVG sắc nét ở mọi scale.
- [ ] Font fallback Segoe UI hoạt động khi thiếu file TTF.
- [ ] Không phụ thuộc màu nền hệ điều hành (dark mode của OS không phá giao diện).

---

## 14. Phụ lục — Đối chiếu CSS ↔ Swing

| CSS | Swing / FlatLaf |
|---|---|
| `background: linear-gradient(...)` | `GradientPaint` trong `paintComponent` |
| `border-radius` | `RoundRectangle2D` + `arc` của FlatLaf |
| `box-shadow` | Nhiều lớp `RoundRectangle2D` hoặc `ConvolveOp` blur |
| `border: 1px solid` | `Border` hoặc `FlatClientProperties.STYLE` `borderColor` |
| `outline: 3px` | `focusWidth`/`focusColor` hoặc vẽ ring thủ công |
| `display:flex; gap` | `FlowLayout`, `BoxLayout`, `GridBagLayout` + `EmptyBorder`/`gap` |
| `padding` | `EmptyBorder` |
| `font-size` px | `Font.deriveFont(pt)` với `pt = px × 0.75` |
| `line-height` | Không có; dùng `JTextArea` + padding hoặc HTML |
| `letter-spacing` | `TextAttribute.TRACKING` |
| `::after` (dot, ✓) | Vẽ trong `paintComponent` hoặc overlay `JLabel` |
| `:hover` | `MouseListener` / `ButtonModel.isRollover()` |
| `:active { transform: scale }` | `Graphics2D.scale` khi `isPressed()` |
| `transition` | `javax.swing.Timer` + easing |
| `@media` | `ComponentListener` + breakpoint trong `Layout` |
| `cursor: pointer` | `setCursor(HAND_CURSOR)` |
| `prefers-reduced-motion` | System property `-Dtaskia.reducedMotion=true` |
| SVG inline | `FlatSVGIcon` + `ColorFilter` |

---

## 15. Definition of Done cho một màn hình

Một màn hình được coi là hoàn thành khi:

1. Dùng đúng token, không hard-code màu/kích thước.
2. Có đủ trạng thái: default, hover, focus, disabled, empty, error (nếu áp dụng).
3. Resize đúng ở 3 breakpoint và 4 mức scale.
4. Điều hướng bàn phím đầy đủ, focus ring nhìn thấy rõ.
5. Không có văn bản tiếng Anh lẫn tiếng Việt không chủ đích.
6. Đã đối chiếu với checklist QA ở mục 13.
