package fill;

import model.Point2D;
import raster.Raster;

import java.util.ArrayDeque;
import java.util.Deque;

public class SeedFill implements Filler {
    private final Raster raster;
    private Point2D seed;

    private int backgroundColor, fillColor;

    // zásobník (push/pop) – pro frontu stačí místo push použít addLast (offer)
    private final Deque<Point2D> stack = new ArrayDeque<>();

    public SeedFill(Raster raster) {
        this.raster = raster;
        this.fillColor = 0x00ff00;
    }

    @Override
    public void fill() {
        if (seed == null) return;

        backgroundColor = raster.getPixel(seed.getX(), seed.getY());

        if ((backgroundColor & 0xFFFFFF) == (fillColor & 0xFFFFFF)) return;

        stack.clear();
        nextPixel(seed.getX(), seed.getY()); // semínko

        while (!stack.isEmpty()) {
            Point2D p = stack.pop();
            int x = p.getX();
            int y = p.getY();

            nextPixel(x + 1, y);
            nextPixel(x - 1, y);
            nextPixel(x, y + 1);
            nextPixel(x, y - 1);
        }
    }

    private void nextPixel(int x, int y) {
        // Mimo rastr
        if (x < 0 || y < 0 || x >= raster.getWidth() || y >= raster.getHeight())
            return;

        if (raster.getPixel(x, y) == backgroundColor) {

            raster.setPixel(x, y, fillColor);
            stack.push(new Point2D(x, y));
        }
    }

    public void setSeed(Point2D seed) {
        this.seed = seed;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }
}
