package model;

public class Point2D {
    private final int x, y;

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public Point2D snapEnd(Point2D target) {
        int dx = target.getX() - x;
        int dy = target.getY() - y;
        int adx = Math.abs(dx);
        int ady = Math.abs(dy);

        double distHorizontal = ady;
        double distVertical = adx;
        double distDiagonal = Math.abs(adx - ady) / Math.sqrt(2);

        if (distHorizontal <= distVertical && distHorizontal <= distDiagonal) {
            // vodorovná
            return new Point2D(target.getX(), y);
        }
        if (distVertical <= distDiagonal) {
            // svislá
            return new Point2D(x, target.getY());
        }
        // úhlopříčná
        int d = (adx + ady) / 2;
        // směr úhlopříčky v ose x
        int sx;
        if (dx >= 0) {
            sx = 1;
        } else {
            sx = -1;
        }

        // směr úhlopříčky v ose y
        int sy;
        if (dy >= 0) {
            sy = 1;
        } else {
            sy = -1;
        }

        return new Point2D(x + sx * d, y + sy * d);
    }
}