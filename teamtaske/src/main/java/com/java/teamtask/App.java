package com.java.teamtask;

import com.java.teamtask.ui.MainFrame;
import com.java.teamtask.ui.theme.Theme;

import javax.swing.SwingUtilities;

/** Điểm vào ứng dụng Taskia. */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
