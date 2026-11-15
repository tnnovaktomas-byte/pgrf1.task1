package raster;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RasterBufferedImage implements Raster {

    private BufferedImage image;

    public RasterBufferedImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        if(x< 0 || x >= image.getWidth() || y< 0 || y >= image.getHeight()) {
            return;
        }
        image.setRGB(x, y, color);
    }

    @Override
    public int getPixel(int x, int y) {
        if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
            return -1;
        }
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    public BufferedImage getImage() {
        return image;
    }

    // nový obrázek
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (width == image.getWidth() && height == image.getHeight()) return;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void clear() {
        image.getGraphics().clearRect(
                0,
                0,
                image.getWidth(),
                image.getHeight()
        );
    }
}