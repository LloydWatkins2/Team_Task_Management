package com.java.teamtask.ui.data;

/** Lỗi tầng dữ liệu được chuyển thành runtime để UI hiển thị thông báo thân thiện. */
public class DataAccessException extends RuntimeException {
  public DataAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataAccessException(String message) {
    super(message);
  }
}
