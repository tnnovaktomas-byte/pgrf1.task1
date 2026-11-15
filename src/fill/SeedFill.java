package fill;

import model.Point2D;
import raster.Raster;

import java.util.ArrayDeque;
import java.util.Deque;

public class SeedFill implements Filler {
    private final Raster raster;
    private Point2D seed;

    private int backgroundColor, fillColor;
    private int boundaryColor = 0xFFFFFF;

    // hraniční podmínka: false = omezení barvou pozadí, true = omezení barvou hranice
    private boolean useBoundaryColor = false;

    // zásobník (push/pop) – pro frontu stačí místo push použít addLast (offer)
    private final Deque<Point2D> stack = new ArrayDeque<>();

    public SeedFill(Raster raster) {
        this.raster = raster;
        this.fillColor = 0x00ff00;
    }

    @Override
    public void fill() {
        if (seed == null)
            return;

        backgroundColor = raster.getPixel(seed.getX(), seed.getY());

        if (backgroundColor == fillColor)
            return;

        if (!belongsToArea(seed.getX(), seed.getY()))
            return;

        stack.clear();
        nextPixel(seed.getX(), seed.getY()); // startovní pixel = semínko

        while (!stack.isEmpty()) {
            Point2D p = stack.pop();
            int x = p.getX();
            int y = p.getY();

            // čtyři sousedé (4-souvislost)
            nextPixel(x + 1, y);
            nextPixel(x - 1, y);
            nextPixel(x, y + 1);
            nextPixel(x, y - 1);
        }
    }

    // patří-li pixel do oblasti, obarví ho a uloží do zásobníku
    private void nextPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= raster.getWidth() || y >= raster.getHeight())
            return;

        if (belongsToArea(x, y)) {
            raster.setPixel(x, y, fillColor);
            stack.push(new Point2D(x, y));
        }
    }

    // hraniční podmínka vyplňování
    private boolean belongsToArea(int x, int y) {
        int color = raster.getPixel(x, y);

        if (useBoundaryColor) {
            return color != boundaryColor && color != fillColor;
        } else {
            return color == backgroundColor;
        }
    }

    public void setSeed(Point2D seed) {
        this.seed = seed;
    }

    public void setBoundaryColor(int boundaryColor) {
        this.boundaryColor = boundaryColor;
    }

    public void setUseBoundaryColor(boolean useBoundaryColor) {
        this.useBoundaryColor = useBoundaryColor;
    }

    public boolean isUseBoundaryColor() {
        return useBoundaryColor;
    }

    @Override
    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }
}