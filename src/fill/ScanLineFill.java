package fill;

import model.Edge;
import model.Point2D;
import model.Polygon;
import raster.Raster;
import rasterize.PolygonRasterizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanLineFill implements Filler {
    private final Raster raster;
    private final PolygonRasterizer polygonRasterizer;

    private int fillColor;
    private Polygon polygon;

    public ScanLineFill(Raster raster, PolygonRasterizer polygonRasterizer) {
        this.raster = raster;
        this.polygonRasterizer = polygonRasterizer;
        setFillColor(0x00ff00);
    }

    @Override
    public void fill() {
        if (polygon == null || polygon.getSize() < 3) return;

        // Seznam hran
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < polygon.getSize(); i++) {
            Point2D pointA = polygon.getPoint(i);
            Point2D pointB = polygon.getPoint((i + 1) % polygon.getSize());

            Edge edge = new Edge(pointA, pointB);
            if (!edge.isHorizontal()) {
                edge.orientate();
                edges.add(edge);
            }
        }

        // MinY/MaxY
        int yMin = polygon.getPoint(0).getY();
        int yMax = yMin;
        for (int i = 1; i < polygon.getSize(); i++) {
            int y = polygon.getPoint(i).getY();
            if (y < yMin) yMin = y;
            if (y > yMax) yMax = y;
        }

        // jen viditelné řádky
        int yStart = Math.max(yMin, 0);
        int yEnd = Math.min(yMax, raster.getHeight() - 1);

        // Hledání Průsečíků
        for (int y = yStart; y <= yEnd; y++) {
            List<Integer> intersections = new ArrayList<>();

            for (Edge edge : edges) {
                if (!edge.isIntersection(y)) continue;

                intersections.add(edge.getIntersection(y));
            }

            Collections.sort(intersections);

            // Spojení Lichý a sudý
            for (int i = 0; i + 1 < intersections.size(); i += 2) {
                int xStart = Math.max(intersections.get(i), 0); // jen viditelné
                int xEnd = Math.min(intersections.get(i + 1), raster.getWidth() - 1);
                for (int x = xStart; x <= xEnd; x++) {
                    raster.setPixel(x, y, fillColor);
                }
            }
        }


        polygonRasterizer.rasterize(polygon);
    }

    @Override
    public void setFillColor(int color) {
        this.fillColor = color;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }
}