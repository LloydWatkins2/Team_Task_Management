package com.java.teamtask.ui.data;

/** Dữ liệu form đăng ký, ánh xạ 1-1 với tham số của {@code system.register}. */
public record Registration(
    String lname,
    String fname,
    String username,
    String password,
    String gmail,
    String phone,
    String dob) {}
