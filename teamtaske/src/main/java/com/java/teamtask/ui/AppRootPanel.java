package com.java.teamtask.ui;

import com.java.teamtask.ui.theme.Layout;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Radius;
import com.java.teamtask.ui.theme.Shadows;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

/** Nền gradient + đặt shell căn giữa với max-width 1280. */
public final class AppRootPanel extends GradientPanel {
    private final ShellPanel shell;

    public AppRootPanel(ShellPanel shell) {
        this.shell = shell;
        setLayout(null);
        add(shell);
    }

    public ShellPanel getShell() {
        return shell;
    }

    @Override
    public void doLayout() {
        Insets padding = Layout.pagePadding(getWidth());
        int width = Math.max(0, Math.min(getWidth() - padding.left - padding.right, Layout.SHELL_MAX_WIDTH));
        int height = Math.max(0, getHeight() - padding.top - padding.bottom);
        shell.setBounds((getWidth() - width) / 2, padding.top, width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (shell.getWidth() <= 0 || shell.getHeight() <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        Shadows.paintShell(g2, shell.getBounds(), Radius.XL);
        g2.dispose();
    }
}
