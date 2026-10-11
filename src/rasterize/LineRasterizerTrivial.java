package rasterize;

import raster.Raster;

public class LineRasterizerTrivial extends LineRasterizer{

    private int color = 0xFFFFFF;

    public LineRasterizerTrivial(Raster raster) {
        super(raster);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {

        // Svislá úsečka
        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                raster.setPixel(x1, y, color);
            }
            return;
        }

        // 1. Spočítat k a q
        double k = (double) (y2 - y1) / (x2 - x1);
        double q = y1 - k * x1;


        if (Math.abs(k) <= 1) {
            // Řídicí osa je X
            if (x1 > x2) {
                int tmpX = x1; x1 = x2; x2 = tmpX;
            }
            for (int x = x1; x <= x2; x++) {
                int y = (int) Math.round(k * x + q);
                raster.setPixel(x, y, color);
            }
        } else {
            // Řídicí osa je Y
            if (y1 > y2) {
                int tmpY = y1;
                y1 = y2;
                y2 = tmpY;
            }
            for (int y = y1; y <= y2; y++) {
                int x = (int) Math.round((y - q) / k);
                raster.setPixel(x, y, color);
            }
        }
    }
}
