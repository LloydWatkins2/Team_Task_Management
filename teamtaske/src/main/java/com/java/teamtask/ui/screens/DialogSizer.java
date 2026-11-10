package com.java.teamtask.ui.screens;

import com.java.teamtask.ui.theme.UiScale;

import javax.swing.JDialog;
import java.awt.Dimension;

/** Đặt kích thước hộp thoại theo nội dung để vùng đệm bóng của nút không bị cắt. */
final class DialogSizer {
  private DialogSizer() {
  }

  static void fit(JDialog dialog, int designWidth, int minHeight) {
    dialog.pack();
    Dimension preferred = dialog.getSize();
    dialog.setSize(
        Math.max(UiScale.px(designWidth), preferred.width),
        Math.max(UiScale.px(minHeight), preferred.height));
  }
}
