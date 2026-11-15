package rasterize;

import model.Line;
import model.Point2D;
import raster.Raster;

public abstract class LineRasterizer {
    protected Raster raster;
    protected int color = 0xFFFFFF;

    public LineRasterizer(Raster raster) {
        this.raster = raster;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void rasterize(int x1, int y1, int x2, int y2) {
        System.out.println("rasterize předek");
    }

    public void rasterize(Point2D p1, Point2D p2) {
        rasterize(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public void rasterize(Line line) {
        rasterize(line.getStart(), line.getEnd());
    }
}