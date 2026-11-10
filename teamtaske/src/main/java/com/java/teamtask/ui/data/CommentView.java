package com.java.teamtask.ui.data;

/** Bình luận đã ánh xạ sang dữ liệu hiển thị. */
public record CommentView(int id, String author, String time, String content) {
  public CommentView(String author, String time, String content) {
    this(0, author, time, content);
  }
}
