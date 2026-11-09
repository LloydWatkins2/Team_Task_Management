package com.java.teamtask.ui.data;

/** Các trường người dùng có thể thay đổi trong hồ sơ; null/rỗng nghĩa là giữ nguyên. */
public record ProfileChange(
    String lname,
    String fname,
    String gmail,
    String phone,
    String dob,
    String newPassword) {

  public boolean isEmpty() {
    return blank(lname)
        && blank(fname)
        && blank(gmail)
        && blank(phone)
        && blank(dob)
        && blank(newPassword);
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }
}
