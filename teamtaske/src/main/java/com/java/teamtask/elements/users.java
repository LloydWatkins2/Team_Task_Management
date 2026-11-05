package com.java.teamtask.elements;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class users{
protected int userId;
    protected String name;
    protected String password;
    protected String email;
    protected Map<Projects, ProjectsAssignments> relatedProjects;

    public users(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.relatedProjects = new HashMap<>();
    }

    public void deleteAccount() {
        // Xóa tài khoản
    }

    public void updateAccount() {
        // Cập nhật thông tin tài khoản
    }

    public Projects createProject() {
        // Tạo project
        return null;
    }

    public List<Projects> viewProjects() {
        // Lấy danh sách project
        return null;
    }

    public String viewProjectStatus(Projects project) {
        // Xem trạng thái project
        return null;
    }

    public void action(Projects project) {
        // Thực hiện hành động với project
    }
}