package ru.mikst74.mikstcraft.settings;

import org.joml.Vector2f;

public class GameDynamicProperties {
    public static int guiGridHalfSizeInScreenPixel = 20;

    // calculated
    public static Vector2f guiGridHalfSizeGlView=new Vector2f(); // depends on screen width & height

    public static void useScreenSize(int width, int height){
        guiGridHalfSizeGlView.x = (float) (2 * guiGridHalfSizeInScreenPixel) / width;
        guiGridHalfSizeGlView.y = (float) (2 * guiGridHalfSizeInScreenPixel) / height;
    }
}
