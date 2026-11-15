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


    public boolean isInside(Point2D p) {
        if (points.size() < 3) return false;

        boolean hasLeft = false, hasRight = false;
        for (int i = 0; i < points.size(); i++) {
            Edge edge = new Edge(points.get(i), points.get((i + 1) % points.size()));
            int side = edge.side(p);
            if (side > 0) hasRight = true;
            if (side < 0) hasLeft = true;

            if (hasLeft && hasRight) return false;
        }
        return true;
    }
}