package com.java.teamtask.elements;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Kiểm tra chuyển đổi ngày dùng cho đăng ký/cập nhật hồ sơ. */
class DatesTest {
  @Test
  void parsesUiFormat() {
    assertEquals(LocalDate.of(2000, 8, 15), Dates.parse("15/08/2000"));
  }

  @Test
  void parsesIsoFormatFromDatabase() {
    assertEquals(LocalDate.of(2000, 8, 15), Dates.parse("2000-08-15"));
  }

  @Test
  void convertsToSqlDateForPostgres() {
    assertEquals(Date.valueOf(LocalDate.of(2000, 8, 15)), Dates.toSqlDate("15/08/2000"));
  }

  @Test
  void formatsDatabaseDateForUi() {
    assertEquals("15/08/2000", Dates.toUiDate("2000-08-15"));
    assertEquals("15/08/2000", Dates.formatUi(Date.valueOf(LocalDate.of(2000, 8, 15))));
  }

  @Test
  void blankDateBecomesNull() {
    assertNull(Dates.parse("  "));
    assertNull(Dates.toSqlDate(null));
    assertNull(Dates.formatUi(null));
  }

  @Test
  void invalidDateIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> Dates.parse("31/02/2000"));
    assertThrows(IllegalArgumentException.class, () -> Dates.toSqlDate("2000-13-01"));
  }
}
