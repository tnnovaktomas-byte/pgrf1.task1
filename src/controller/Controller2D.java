package controller;

import fill.Filler;
import fill.SeedFill;
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

    //Fillers
    private SeedFill seedFiller;

    // Uložené objekty
    private final List<Line> lines = new ArrayList<>();
    private final Polygon polygon = new Polygon();

    private Point2D tempLineStart;
    private Point2D tempLineEnd;

    private Point2D tempPolygonPoint;

    public Controller2D(Panel panel) {
        this.panel = panel;
        //Triviální Rasterizers
        this.lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        this.polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        // Fillers
        seedFiller = new SeedFill(panel.getRaster());

        initListeners();
    }

    private void initListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            //Začátek modelu
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    tempLineStart = new Point2D(e.getX(), e.getY());
                    tempLineEnd = tempLineStart;
                    drawScene();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    tempPolygonPoint = new Point2D(e.getX(), e.getY());
                    drawScene();
                }

                if(e.getButton() == MouseEvent.BUTTON2) {
                    seedFiller.setSeed(new Point2D(e.getX(), e.getY()));
                    drawScene();
                }
            }
            //Aktualizace souřadnic
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && tempLineStart != null) {
                    tempLineEnd = new Point2D(e.getX(), e.getY());
                    drawScene();
                } else if (SwingUtilities.isRightMouseButton(e) && tempPolygonPoint != null) {
                    tempPolygonPoint = new Point2D(e.getX(), e.getY());
                    drawScene();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && tempLineStart != null) {
                    tempLineEnd = new Point2D(e.getX(), e.getY());
                    lines.add(new Line(tempLineStart, tempLineEnd));
                    tempLineStart = null;
                    tempLineEnd = null;
                    drawScene();
                } else if (SwingUtilities.isRightMouseButton(e) && tempPolygonPoint != null) {
                    polygon.addPoint(new Point2D(e.getX(), e.getY()));
                    tempPolygonPoint = null;
                    drawScene();
                }
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        // Clear
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

        for (Line line : lines) {
            lineRasterizer.rasterize(line);
        }

        // Pružná Úsečka
        if (tempLineStart != null && tempLineEnd != null) {
            lineRasterizer.rasterize(tempLineStart, tempLineEnd);
        }

        polygonRasterizer.rasterize(polygon);

        // Pružná úsečka por polygon
        if (tempPolygonPoint != null) {
            int size = polygon.getSize();
            if (size == 1) {
                lineRasterizer.rasterize(polygon.getPoint(0), tempPolygonPoint);
            } else if (size >= 2) {
                Point2D lastPoint = polygon.getPoint(size - 1);
                Point2D firstPoint = polygon.getPoint(0);
                lineRasterizer.rasterize(lastPoint, tempPolygonPoint);
                lineRasterizer.rasterize(tempPolygonPoint, firstPoint);
            }
        }

        seedFiller.fill();

        panel.repaint();
    }
}