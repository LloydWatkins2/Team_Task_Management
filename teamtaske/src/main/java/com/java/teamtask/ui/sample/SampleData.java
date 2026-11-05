package com.java.teamtask.ui.sample;

import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.theme.Palette;

import java.util.ArrayList;
import java.util.List;

/** Dữ liệu mẫu dùng trong lúc chưa nối DAO. */
public final class SampleData {
    public static final String CURRENT_USER = "Nguyễn An";

    private SampleData() {
    }

    public static List<TaskItem> tasks() {
        List<TaskItem> tasks = new ArrayList<>();
        tasks.add(new TaskItem(
                "Thiết kế trang Tổng quan",
                "Wireframe và bố cục dashboard",
                TaskCard.Status.IN_PROGRESS,
                "Website Redesign",
                "Nguyễn An",
                "Hôm nay, 17:00",
                Palette.TASK_BLUE,
                "dashboard",
                "Hoàn thiện wireframe dashboard, bổ sung trạng thái rỗng và kiểm tra ở 3 breakpoint."));
        tasks.add(new TaskItem(
                "Hoàn thiện luồng đăng nhập",
                "Kiểm thử trạng thái lỗi và ghi nhớ đăng nhập",
                TaskCard.Status.DONE,
                "Website Redesign",
                "Trần Minh",
                "02/11/2026",
                Palette.TASK_YELLOW,
                "lock",
                "Đã xử lý validate, thông báo lỗi chung và điều hướng sau khi đăng nhập thành công."));
        tasks.add(new TaskItem(
                "Tài liệu API quản lý công việc",
                "Mô tả endpoint, dữ liệu mẫu và mã lỗi",
                TaskCard.Status.TODO,
                "Mobile App",
                "Lê Hà",
                "08/11/2026",
                Palette.TASK_PURPLE,
                "tasks",
                "Viết tài liệu cho các endpoint CRUD công việc, bình luận và tìm kiếm."));
        tasks.add(new TaskItem(
                "Chiến dịch ra mắt Q4",
                "Chuẩn bị nội dung mạng xã hội",
                TaskCard.Status.OVERDUE,
                "Marketing Q4",
                "Phạm Khoa",
                "30/10/2026",
                Palette.TASK_YELLOW,
                "flag",
                "Chốt lịch đăng bài, hình ảnh và thông điệp chính cho chiến dịch ra mắt."));
        return tasks;
    }

    public static List<CommentItem> comments() {
        List<CommentItem> comments = new ArrayList<>();
        comments.add(new CommentItem("Trần Minh", "2 giờ trước",
                "Mình đã cập nhật bản wireframe mới, nhờ An xem giúp phần trạng thái rỗng."));
        comments.add(new CommentItem("Nguyễn An", "1 giờ trước",
                "Đã xem, phần empty state ổn rồi. Mình sẽ chỉnh lại khoảng cách ở task card."));
        comments.add(new CommentItem("Lê Hà", "25 phút trước",
                "Nhớ kiểm tra lại ở mức zoom 125% và 150% trước khi chốt nhé."));
        return comments;
    }
}
