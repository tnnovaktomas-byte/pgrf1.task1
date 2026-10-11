package controller;

import model.Line;
import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.PolygonRasterizer;
import view.Panel;

import javax.swing.SwingUtilities;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {
    private final Panel panel;

    //Rasterizace
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;

    // Uložené objekty
    private final List<Line> lines = new ArrayList<>();
    private final Polygon polygon = new Polygon();

    // Dočasné body pro pružné čáry
    private Point2D tempLineStart;
    private Point2D tempLineEnd;

    private Point2D tempPolygonPoint;

    public Controller2D(Panel panel) {
        this.panel = panel;

        this.lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        this.polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        initListeners();
    }

    private void initListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Začátek zadávání pružné úsečky
                    tempLineStart = new Point2D(e.getX(), e.getY());
                    tempLineEnd = tempLineStart;
                    drawScene();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Začátek zadávání vrcholu polygonu
                    tempPolygonPoint = new Point2D(e.getX(), e.getY());
                    drawScene();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && tempLineStart != null) {
                    // Aktualizace pružné úsečky
                    tempLineEnd = new Point2D(e.getX(), e.getY());
                    drawScene();
                } else if (SwingUtilities.isRightMouseButton(e) && tempPolygonPoint != null) {
                    // Aktualizace pružných hran polygonu
                    tempPolygonPoint = new Point2D(e.getX(), e.getY());
                    drawScene();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && tempLineStart != null) {
                    // Potvrzení a uložení dokončené úsečky
                    tempLineEnd = new Point2D(e.getX(), e.getY());
                    lines.add(new Line(tempLineStart, tempLineEnd));
                    tempLineStart = null;
                    tempLineEnd = null;
                    drawScene();
                } else if (SwingUtilities.isRightMouseButton(e) && tempPolygonPoint != null) {
                    // Potvrzení a přidání vrcholu do polygonu
                    polygon.addPoint(new Point2D(e.getX(), e.getY()));
                    tempPolygonPoint = null;
                    drawScene();
                }
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        // Klávesa 'C' pro vymazání plátna a struktur
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    clearAll();
                }
            }
        });
    }

    private void clearAll() {
        lines.clear();
        polygon.clear();
        tempLineStart = null;
        tempLineEnd = null;
        tempPolygonPoint = null;
        drawScene();
    }

    private void drawScene() {
        panel.getRaster().clear();

        // 1. Vykreslení všech potvrzených úseček
        for (Line line : lines) {
            lineRasterizer.rasterize(line);
        }

        // 2. Vykreslení aktuální pružné úsečky
        if (tempLineStart != null && tempLineEnd != null) {
            lineRasterizer.rasterize(tempLineStart, tempLineEnd);
        }

        // 3. Vykreslení existujícího polygonu
        polygonRasterizer.rasterize(polygon);

        // 4. Vykreslení pružných hran pro zadávaný vrchol polygonu
        if (tempPolygonPoint != null) {
            int size = polygon.getSize();
            if (size == 1) {
                // Pouze jedna hrana k jedinému existujícímu bodu
                lineRasterizer.rasterize(polygon.getPoint(0), tempPolygonPoint);
            } else if (size >= 2) {
                // Dvě pružné hrany: z posledního bodu a do prvního bodu
                Point2D lastPoint = polygon.getPoint(size - 1);
                Point2D firstPoint = polygon.getPoint(0);
                lineRasterizer.rasterize(lastPoint, tempPolygonPoint);
                lineRasterizer.rasterize(tempPolygonPoint, firstPoint);
            }
        }

        panel.repaint();
    }
}