package swim.recolor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Recolor {

    public static Color color = Recolor.getRandomColor();
    public static HashMap<String, BufferedImage> recolorFiles = new HashMap<String, BufferedImage>();
    public static boolean isMCPE = false;

    // this function fixes image transparency values to remove the unneeded alpha channels
    public static BufferedImage transparencyFix(BufferedImage raw) {
        int WIDTH = raw.getWidth();
        int HEIGHT = raw.getHeight();
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        int pixels[] = new int[WIDTH * HEIGHT];
        raw.getRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = (pixels[i] & 0xff000000) >>> 24;
            if (pixels[i] >= alpha) {
                pixels[i] = 0x00ffffff;
            }
        }
        image.setRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
        return image;
    }

    public static BufferedImage tint(BufferedImage image, Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = (pixelColor.getRed() + color.getRed()) / 2;
                int g = (pixelColor.getGreen() + color.getGreen()) / 2;
                int b = (pixelColor.getBlue() + color.getBlue()) / 2;
                int a = pixelColor.getAlpha();
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
        return image;
    }

    public static BufferedImage hueShift(BufferedImage image, float iHUE) {
        float hue = iHUE / 360.0f;
        int WIDTH = image.getWidth();
        int HEIGHT = image.getHeight();
        BufferedImage processed = new BufferedImage(WIDTH, HEIGHT, image.getType());
        for (int Y = 0; Y < HEIGHT; Y++) {
            for (int X = 0; X < WIDTH; X++) {
                int RGB = image.getRGB(X, Y);
                if (RGB != 0x00ffffff) {
                    int R = (RGB >> 16) & 0xff;
                    int G = (RGB >> 8) & 0xff;
                    int B = (RGB) & 0xff;
                    float HSV[] = new float[3];
                    Color.RGBtoHSB(R, G, B, HSV);
                    processed.setRGB(X, Y, Color.getHSBColor(hue, HSV[1], HSV[2]).getRGB());
                }
            }
        }
        return processed;
    }

    public static int getHue(int red, int green, int blue) {
        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);
        if (min == max) {
            return 0;
        }
        float hue = 0f;
        if (max == red) {
            hue = (green - blue) / (max - min);

        } else if (max == green) {
            hue = 2f + (blue - red) / (max - min);

        } else {
            hue = 4f + (red - green) / (max - min);
        }
        hue = hue * 60;
        if (hue < 0)
            hue = hue + 360;
        return Math.round(hue);
    }

    public static Color getRandomColor() {
        int red = (int) ((Math.random() * (255)));
        int green = (int) ((Math.random() * (255)));
        int blue = (int) ((Math.random() * (255)));
        return new Color(red, green, blue);
    }

    public static Color getContrastColor(Color color) {
        double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
        return y >= 128 ? Color.black : Color.white;
    }

    public static String getHexFromColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()).toUpperCase();
    }

}
