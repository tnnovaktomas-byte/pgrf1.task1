package rasterize;

import raster.Raster;

public class LineRasterizerTrivial extends LineRasterizer {
    public LineRasterizerTrivial(Raster raster){
        super(raster);
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2){
    }
}
