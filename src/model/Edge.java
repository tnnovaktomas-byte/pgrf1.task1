package model;

public class Edge {
    // nejsou final, aby je šlo v orientate() prohodit
    private int x1, y1, x2, y2;

    public Edge(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Edge(Point2D a, Point2D b) {
        this.x1 = a.getX();
        this.y1 = a.getY();
        this.x2 = b.getX();
        this.y2 = b.getY();
    }

    public boolean isHorizontal() {
        return y1 == y2;
    }

    /**
     * Zorientuje hranu shora dolů – po zavolání platí y1 < y2.
     */
    public void orientate() {
        if (y1 > y2) {
            int tmp = x1; x1 = x2; x2 = tmp;
            tmp = y1; y1 = y2; y2 = tmp;
        }
    }

    /**
     * Existuje průsečík se scan-line y?
     * Interval je zprava otevřený (y < y2) = zkrácení hrany o 1 pixel na konci.
     * Díky tomu se společný vrchol dvou navazujících hran nezapočítá dvakrát.
     * Předpokládá zorientovanou hranu (y1 < y2).
     */
    public boolean isIntersection(int y) {
        return y >= y1 && y < y2;
    }

    /**
     * x-ová souřadnice průsečíku hrany se scan-line y.
     * Používáme převrácenou směrnici 1/k = dx/dy, protože hrana není vodorovná
     * (dy != 0), kdežto svislá hrana (dx = 0) by s k = dy/dx dělila nulou.
     */
    public int getIntersection(int y) {
        double invK = (double) (x2 - x1) / (y2 - y1);
        return (int) Math.round(x1 + (y - y1) * invK);
    }

    /**
     * Test polohy bodu vůči přímce hrany (bod v polorovině).
     * 1. tečný vektor t = P2 - P1
     * 2. normála n = (-t.y, t.x)
     * 3. vektor k bodu v = P - P1
     * 4. skalární součin n · v
     *
     * Na obrazovce roste osa y dolů, proto normála (-t.y, t.x) míří
     * vpravo od směru hrany
     *   > 0  bod leží vpravo
     *   < 0  bod leží vlevo
     *   = 0  bod leží na přímce
     * Pozor: orientate() může hranu otočit, a tím prohodit strany.
     */
    public int side(int x, int y) {
        int tx = x2 - x1;
        int ty = y2 - y1;
        int vx = x - x1;
        int vy = y - y1;
        return -ty * vx + tx * vy;
    }

    public int side(Point2D p) {
        return side(p.getX(), p.getY());
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }
}