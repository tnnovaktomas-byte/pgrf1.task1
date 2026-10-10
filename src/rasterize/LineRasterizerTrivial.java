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



        // 1. spočítat k a q
        float k = (float) (y2 - y1) / (x2 - x1);
        float q = y1 - k * x1;


        for(int x = x1; x <= x2; x++){
            int y = Math.round(k * x + q);
            raster.setPixel(x, y,color);
        }
    }
}
