package view;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.FlowLayout;

// volba ořezu
public class ClipToolbar extends JPanel {
    private final JToggleButton noClipButton = new JToggleButton("Bez ořezu");
    private final JToggleButton polygonClipButton = new JToggleButton("Ořez polygonu (S–H)");
    private final JToggleButton lineClipButton = new JToggleButton("Ořez úseček (C–S)");

    public ClipToolbar() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        ButtonGroup group = new ButtonGroup(); // jen jedno
        JToggleButton[] buttons = {noClipButton, polygonClipButton, lineClipButton};
        for (JToggleButton button : buttons) {
            button.setFocusable(false); // klávesy pro panel
            group.add(button);
            add(button);
        }

        noClipButton.setSelected(true);
    }

    public JToggleButton getNoClipButton() {
        return noClipButton;
    }

    public JToggleButton getPolygonClipButton() {
        return polygonClipButton;
    }

    public JToggleButton getLineClipButton() {
        return lineClipButton;
    }
}