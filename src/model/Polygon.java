package model;

import java.awt.*;
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
}
