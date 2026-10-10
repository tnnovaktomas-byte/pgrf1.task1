package rasterize;

import model.Point2D;
import model.Polygon;

public class PolygonRasterizer {
    private LineRasterizer lineRasterizer;

    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void setLineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon) {
        for (int i = 0; i < polygon.getSize(); i++) {
            //TODO: když nemá aspoň 3 vrcholy tak "returrn"/ zavolá se linerasterizer
            //TODO: podmínka, aby se našlaa velikost listu a nešlo se víc (nebo modulem %)
            Point2D pointA = polygon.getPoint(i);
            Point2D pointB = polygon.getPoint(i+1);
        }
    }
}
