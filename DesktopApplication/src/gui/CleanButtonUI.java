package gui;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Graphics;

/** Paints button fills consistently across native Swing look-and-feels. */
public class CleanButtonUI extends BasicButtonUI {
    @Override
    public void paint(Graphics graphics, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        Color fill = button.getBackground();
        if (!button.isEnabled()) fill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 120);
        else if (button.getModel().isPressed()) fill = fill.darker();
        else if (button.getModel().isRollover()) fill = fill.brighter();

        graphics.setColor(fill);
        graphics.fillRect(0, 0, component.getWidth(), component.getHeight());
        super.paint(graphics, component);
    }
}
