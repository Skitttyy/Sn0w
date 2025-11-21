package me.skitttyy.kami.api.gui.theme;

import java.awt.*;

public interface IColorScheme {
    Color getMainColor(int pos);
    Color getOutlineColor();
    Color getButtonColor();
    Color getBackgroundColor();
    Color getSecondaryBackgroundColor();
    Color getTertiaryBackgroundColor();
    Color getTextColor();
    Color getTextColorHighlight();
    Color getTextColorActive();
    boolean doesTextShadow();
}
