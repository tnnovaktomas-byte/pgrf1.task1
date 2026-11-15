package model;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private final List<Point2D> points;

    public Polygon() {
        points = new ArrayList<>();
    }
    //přidání pointu
    public void addPoint(Point2D point) {
        points.add(point);
    }

    public Point2D getPoint(int index) {
        return points.get(index);
    }

    public int getSize() {
        return points.size();
    }

    public void clear() {
        points.clear();
    }

    // pravidelný n-úhelník na clip
    public static Polygon createRegular(int centerX, int centerY, int radius, int count) {
        Polygon result = new Polygon();
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            int x = (int) Math.round(centerX + radius * Math.cos(angle));
            int y = (int) Math.round(centerY + radius * Math.sin(angle));
            result.addPoint(new Point2D(x, y));
        }
        return result;
    }

    // orientace (shoelace)
    public boolean isClockwise() {
        long sum = 0;
        for (int i = 0; i < points.size(); i++) {
            Point2D a = points.get(i);
            Point2D b = points.get((i + 1) % points.size());
            sum += (long) a.getX() * b.getY() - (long) b.getX() * a.getY();
        }
        return sum > 0; // osa y dolů
    }

    // obrácené pořadí
    public Polygon reversed() {
        Polygon result = new Polygon();
        for (int i = points.size() - 1; i >= 0; i--) {
            result.addPoint(points.get(i));
        }
        return result;
    }

    public boolean isInside(Point2D p) {
        if (points.size() < 3) return false;

        boolean hasLeft = false, hasRight = false;
        for (int i = 0; i < points.size(); i++) {
            Edge edge = new Edge(points.get(i), points.get((i + 1) % points.size()));
            int side = edge.side(p);
            if (side > 0) hasRight = true;
            if (side < 0) hasLeft = true;

            // bod je vlevo od jedné hrany a vpravo od jiné → je venku
            if (hasLeft && hasRight) return false;
        }
        return true;
    }
}