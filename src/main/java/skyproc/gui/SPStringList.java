package skyproc.gui;

import lev.gui.LTextField;

import java.awt.*;

/**
 * @author Justin Swanson
 */
public class SPStringList extends SPList<String> {

    final LTextField adder;

    /**
     * @param title
     * @param font
     * @param color
     */
    public SPStringList(String title, Font font, Color color) {
        super(title, font, color);

        adder = new LTextField("Adder");
        adder.addEnterButton("Add", e -> {
            addElement(adder.getText());
            adder.setText("");
        });
        adder.setLocation(0, this.titleLabel.getY() + this.titleLabel.getHeight() + 10);
        Add(adder);

        scroll.setLocation(scroll.getX(), adder.getBottom() + 10);
    }

    /**
     * @param width
     * @param height
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        adder.setSize(width, adder.getHeight());
        scroll.setSize(scroll.getWidth(), height - adder.getHeight() - remove.getHeight() - 52);
        if (accept != null) {
            remove.putUnder(scroll, 0, 10);
            accept.setSize(remove.getSize());
            accept.putUnder(scroll, remove.getRight() + spacing, 10);
        } else {
            remove.centerOn(scroll, scroll.getY() + scroll.getHeight() + 10);
        }
    }


}
