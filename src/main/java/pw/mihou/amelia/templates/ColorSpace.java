package pw.mihou.amelia.templates;

import java.awt.*;
import java.util.Random;

public class ColorSpace {

    private static Random rand = new Random();

    public static Color defaultColor(){
        final float hue = rand.nextFloat();
        final float saturation = 0.9f;
        final float luminance = 1.0f;
        return Color.getHSBColor(hue,saturation,luminance);
    }

}
