package me.skitttyy.kami.api.gui.helpers;

import me.skitttyy.kami.impl.gui.ClickGui;

public class Rect {

    int x;
    int y;
    int width;
    int height;

    public Rect(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

//    public boolean collideWithMouse(MouseHelper mouse){
//        return width < 0 ? mouse.getX() <= this.getX() : mouse.getX() >= this.getX() && width < 0 ? mouse.getX() >= this.getX() + this.getWidth() : mouse.getX() <= this.getX() + this.getWidth() && height < 0 ? mouse.getY() <= this.getY() : mouse.getY() >= this.getY() && height < 0 ? mouse.getY() >= this.getY() + this.getHeight()  : mouse.getY() <= this.getY() + this.getHeight();
//
//
////        if(width < 0){
////            return mouse.getX() <= this.getX() && mouse.getX() >= this.getX() + this.getWidth() && mouse.getY() >= this.getY() && mouse.getY() <= this.getY() + this.getHeight();
////        }else
////        {
////            return mouse.getX() >= this.getX() && mouse.getX() <= this.getX() + this.getWidth() && mouse.getY() >= this.getY() && mouse.getY() <= this.getY() + this.getHeight();
////        }
//    }

    public boolean collideWithMouse(MouseHelper mouse)
    {
        // Check if the mouse's X coordinate is within the horizontal bounds
        boolean withinXBounds;
        int posY = height < 0 ? y + ClickGui.CONTEXT.getRenderer().getTextHeight("AAA") : y;
        if (width < 0)
        {
            withinXBounds = mouse.getX() <= this.getX() && mouse.getX() >= this.getX() + width;
        } else
        {
            withinXBounds = mouse.getX() >= this.getX() && mouse.getX() <= this.getX() + width;
        }

        // Check if the mouse's Y coordinate is within the vertical bounds
        boolean withinYBounds;
        if (height < 0)
        {
            withinYBounds = mouse.getY() <= posY && mouse.getY() >= posY + height;
        } else
        {
            withinYBounds = mouse.getY() >= posY && mouse.getY() <= posY + height;
        }

        // Return true only if both X and Y coordinates are within bounds
        return withinXBounds && withinYBounds;
    }

    /**
     * fixes special case negative width
     */
    public Rect fixRect()
    {
        int posY = height < 0 ? y + ClickGui.CONTEXT.getRenderer().getTextHeight("AAA") : y;
        Rect rect = new Rect(x, y, width, height);
        if (width < 0)
        {
            rect.setX(rect.x + rect.getWidth());
            rect.setWidth(-rect.getWidth());
        }
        if (height < 0)
        {

            rect.setY(posY + rect.getHeight());
            rect.setHeight(-rect.getHeight());
        }
        return rect;
    }

}
