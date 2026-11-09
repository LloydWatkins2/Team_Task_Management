package com.java.teamtask.elements;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Chuyển đổi ngày giữa định dạng UI {@code dd/MM/yyyy}, ISO {@code yyyy-MM-dd} và {@link Date}.
 *
 * <p>UI dùng {@code dd/MM/yyyy}, còn PostgreSQL trả về {@code yyyy-MM-dd} khi đọc cột {@code DATE}.
 */
public final class Dates {
  private static final DateTimeFormatter UI_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
  private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

  private Dates() {}

  /**
   * Phân tích ngày từ UI ({@code dd/MM/yyyy}) hoặc từ DB ({@code yyyy-MM-dd}). Trả về {@code null}
   * nếu chuỗi rỗng và ném {@link IllegalArgumentException} nếu ngày không hợp lệ.
   */
  public static LocalDate parse(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String text = value.trim();
    DateTimeFormatter formatter = text.contains("/") ? UI_FORMAT : ISO_FORMAT;
    try {
      return LocalDate.parse(text, formatter);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Ngày sinh không hợp lệ (dd/MM/yyyy).", e);
    }
  }

  /** Chuyển ngày sang kiểu DATE để bind vào cột {@code DATE} của PostgreSQL. */
  public static Date toSqlDate(String value) {
    LocalDate date = parse(value);
    return date == null ? null : Date.valueOf(date);
  }

  /** Chuẩn hoá giá trị đọc từ DB ({@code yyyy-MM-dd}) sang {@code dd/MM/yyyy} cho UI. */
  public static String toUiDate(String value) {
    LocalDate date = parse(value);
    return date == null ? null : date.format(UI_FORMAT);
  }

  /** Định dạng {@link Date} đọc từ ResultSet sang {@code dd/MM/yyyy} cho UI. */
  public static String formatUi(Date date) {
    return date == null ? null : date.toLocalDate().format(UI_FORMAT);
  }
}
