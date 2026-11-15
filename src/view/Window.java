package view;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    private final Panel panel;
    private final ClipToolbar clipToolbar;

    public Window(int width, int height) {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("PGRF1 2024/2025");
        setVisible(true);

        clipToolbar = new ClipToolbar();
        add(clipToolbar, BorderLayout.NORTH);

        panel = new Panel(width, height);
        add(panel, BorderLayout.CENTER);
        pack();

        panel.setFocusable(true);
        panel.grabFocus();
    }

    public Panel getPanel() {
        return panel;
    }

    public ClipToolbar getClipToolbar() {
        return clipToolbar;
    }
}