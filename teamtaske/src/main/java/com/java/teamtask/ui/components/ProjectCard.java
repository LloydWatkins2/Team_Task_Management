package com.java.teamtask.ui.components;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Card dự án trên dashboard: tên, mô tả, hạn chót và role của người dùng hiện tại. */
public final class ProjectCard extends Card {
  private Runnable onOpen = () -> {};

  public ProjectCard(
      String name,
      String description,
      String deadline,
      String roleLabel,
      Badge.Kind roleKind,
      Runnable onOpen) {
    super(new BorderLayout(Space.XXL, 0));
    this.onOpen = onOpen == null ? () -> {} : onOpen;
    setHoverable(true);
    setBorder(new EmptyBorder(Space.XL, Space.X3L, Space.XL, Space.X3L));

    JPanel main = new JPanel(new BorderLayout(Space.MD, 0));
    main.setOpaque(false);
    main.add(
        new CircleIconPanel(48, Palette.PRIMARY_SOFT, "projects", Palette.PRIMARY_DARK),
        BorderLayout.WEST);

    JPanel texts = new JPanel();
    texts.setOpaque(false);
    texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

    JLabel titleLabel = new JLabel(name);
    titleLabel.setFont(Typography.cardTitle());
    titleLabel.setForeground(Palette.TEXT);
    titleLabel.setAlignmentX(LEFT_ALIGNMENT);

    JLabel descriptionLabel = new JLabel(description == null || description.isBlank()
        ? "Chưa có mô tả" : description);
    descriptionLabel.setFont(Typography.body());
    descriptionLabel.setForeground(Palette.TEXT_MUTED_STRONG);
    descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
    descriptionLabel.setBorder(new EmptyBorder(2, 0, 0, 0));

    JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.XL, 0));
    meta.setOpaque(false);
    meta.setAlignmentX(LEFT_ALIGNMENT);
    meta.setBorder(new EmptyBorder(Space.SM, 0, 0, 0));
    JLabel due =
        new JLabel(deadline == null || deadline.isBlank() ? "Chưa đặt hạn" : deadline,
            Icons.muted("calendar", 18), JLabel.LEFT);
    due.setFont(Typography.meta());
    due.setForeground(Palette.TEXT_MUTED_STRONG);
    due.setIconTextGap(Space.XXS);
    meta.add(due);

    texts.add(titleLabel);
    texts.add(descriptionLabel);
    texts.add(meta);
    main.add(texts, BorderLayout.CENTER);
    add(main, BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
    actions.setOpaque(false);
    actions.add(new Badge(roleLabel, roleKind));
    IconButton open = new IconButton("chevron-right", "Mở dự án");
    open.addActionListener(e -> this.onOpen.run());
    actions.add(open);
    add(actions, BorderLayout.EAST);

    MouseAdapter opener =
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ProjectCard.this.onOpen.run();
          }
        };
    addMouseListener(opener);
    main.addMouseListener(opener);
    texts.addMouseListener(opener);
    titleLabel.addMouseListener(opener);
    descriptionLabel.addMouseListener(opener);
  }

  public void setOnOpen(Runnable onOpen) {
    this.onOpen = onOpen == null ? () -> {} : onOpen;
  }
}
