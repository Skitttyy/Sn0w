package me.skitttyy.kami.api.gui.helpers;

import me.skitttyy.kami.api.feature.hud.HudComponent;

public interface IDraggable {

    default void drag(Rect dims, int dragX, int dragY, MouseHelper mouse){
        dims.setX(mouse.getX() - dragX);
        dims.setY(mouse.getY() - dragY);
    }
    default void dragEditor(Rect dims, int dragX, int dragY, MouseHelper mouse, HudComponent hudComponent){
        if (mouse.getX() - dragX >= hudComponent.xPos.getMin().intValue() && mouse.getX() - dragX <= hudComponent.xPos.getMax().intValue()) {
            dims.setX(mouse.getX() - dragX);
            hudComponent.xPos.setValue(mouse.getX() - dragX);
        }
        if(mouse.getY() - dragY >= hudComponent.yPos.getMin().intValue() && mouse.getY() - dragY <= hudComponent.yPos.getMax().intValue()) {
            dims.setY(mouse.getY() - dragY);
            hudComponent.yPos.setValue(mouse.getY() - dragY);
        }

    }
}
