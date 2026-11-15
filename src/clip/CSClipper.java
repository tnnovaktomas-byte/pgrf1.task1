package clip;

import model.Line;
import model.Point2D;

// Cohen–Sutherland
public class CSClipper {
    // kódy polohy
    private static final int INSIDE = 0; // 0000
    private static final int LEFT = 1;   // 0001  x < xMin
    private static final int RIGHT = 2;  // 0010  x > xMax
    private static final int TOP = 4;    // 0100  y < yMin
    private static final int BOTTOM = 8; // 1000  y > yMax

    private final int xMin, yMin, xMax, yMax;

    public CSClipper(int xMin, int yMin, int xMax, int yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    // null = celá venku
    public Line clip(Line line) {
        double x1 = line.getStart().getX();
        double y1 = line.getStart().getY();
        double x2 = line.getEnd().getX();
        double y2 = line.getEnd().getY();

        int code1 = computeCode(x1, y1);
        int code2 = computeCode(x2, y2);

        while (true) {
            if ((code1 | code2) == 0) {
                // triviální přijetí
                return new Line(
                        new Point2D((int) Math.round(x1), (int) Math.round(y1)),
                        new Point2D((int) Math.round(x2), (int) Math.round(y2)));
            }

            if ((code1 & code2) != 0) {
                // triviální zamítnutí
                return null;
            }

            // bod venku
            int codeOut;
            if (code1 != 0) {
                codeOut = code1;
            } else {
                codeOut = code2;
            }

            // průsečík
            double x, y;
            if ((codeOut & LEFT) != 0) {
                x = xMin;
                y = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
            } else if ((codeOut & RIGHT) != 0) {
                x = xMax;
                y = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
            } else if ((codeOut & TOP) != 0) {
                y = yMin;
                x = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
            } else {
                y = yMax;
                x = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
            }

            // nahrazení bodu
            if (codeOut == code1) {
                x1 = x;
                y1 = y;
                code1 = computeCode(x1, y1);
            } else {
                x2 = x;
                y2 = y;
                code2 = computeCode(x2, y2);
            }
        }
    }

    private int computeCode(double x, double y) {
        int code = INSIDE;
        if (x < xMin) {
            code |= LEFT;
        } else if (x > xMax) {
            code |= RIGHT;
        }
        if (y < yMin) {
            code |= TOP;
        } else if (y > yMax) {
            code |= BOTTOM;
        }
        return code;
    }
}