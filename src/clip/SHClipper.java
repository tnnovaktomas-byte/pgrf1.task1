package clip;

import model.Edge;
import model.Point2D;
import model.Polygon;

// Sutherland–Hodgman
public class SHClipper {

    public Polygon clip(Polygon polygon, Polygon clipPolygon) {
        if (polygon.getSize() < 3 || clipPolygon.getSize() < 3) {
            return new Polygon();
        }

        // orientace
        Polygon clipper = clipPolygon;
        if (!clipper.isClockwise()) {
            clipper = clipper.reversed();
        }

        Polygon in = polygon;

        for (int i = 0; i < clipper.getSize(); i++) {
            Edge edge = new Edge(clipper.getPoint(i), clipper.getPoint((i + 1) % clipper.getSize()));
            Polygon out = new Polygon();

            if (in.getSize() == 0) {
                break; // vše venku
            }

            Point2D v1 = in.getPoint(in.getSize() - 1);

            for (int j = 0; j < in.getSize(); j++) {
                Point2D v2 = in.getPoint(j);

                if (isInside(v2, edge)) {
                    if (!isInside(v1, edge)) {
                        out.addPoint(edge.intersection(v1, v2)); // vstup
                    }
                    out.addPoint(v2);
                } else {
                    if (isInside(v1, edge)) {
                        out.addPoint(edge.intersection(v1, v2)); // výstup
                    }
                }

                v1 = v2;
            }

            in = out;
        }

        return in;
    }

    // vnitřek vpravo
    private boolean isInside(Point2D p, Edge edge) {
        return edge.side(p) >= 0;
    }
}