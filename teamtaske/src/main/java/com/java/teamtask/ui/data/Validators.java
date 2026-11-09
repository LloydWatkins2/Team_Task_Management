package com.java.teamtask.ui.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/** Validate dùng chung cho form UI. Thông báo thiếu trường giữ đúng câu bắt buộc. */
public final class Validators {
  public static final String REQUIRED = "Please fill up this box!";

  public static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

  private static final Pattern EMAIL =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private Validators() {}

  public static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  public static boolean hasText(String value) {
    return !blank(value);
  }

  public static boolean validEmail(String value) {
    return hasText(value) && EMAIL.matcher(value.trim()).matches();
  }

  public static boolean validPhone(String value) {
    if (blank(value)) {
      return false;
    }
    String digits = value.replaceAll("[^0-9]", "");
    return digits.length() >= 9 && digits.length() <= 15;
  }

  public static boolean validPassword(String value) {
    return hasText(value) && value.length() >= 6;
  }

  public static LocalDate parseDate(String value) {
    if (blank(value)) {
      return null;
    }
    try {
      return LocalDate.parse(value.trim(), DATE_FORMAT);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  public static boolean validDate(String value) {
    return parseDate(value) != null;
  }

  public static String formatDate(LocalDate date) {
    return date == null ? "" : date.format(DATE_FORMAT);
  }
}
