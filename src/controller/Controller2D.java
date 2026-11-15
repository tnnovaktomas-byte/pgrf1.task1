package controller;

import fill.Filler;
import clip.SHClipper;
import fill.ScanLineFill;
import fill.SeedFill;
import model.Line;
import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.PolygonRasterizer;
import view.Panel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {
    // Barvy
    private static final int BOUNDARY_COLOR = 0xFFFFFF;
    private static final int FILL_COLOR = 0x00FF00;
    private static final int SCANLINE_COLOR = 0x3366FF; // modrá
    private static final int CLIP_COLOR = 0xFFFF00;     // žlutá

    private final Panel panel;

    //Rasterizace
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;
    private final PolygonRasterizer clipRasterizer;

    // Ořezávání
    private final SHClipper shClipper = new SHClipper();
    private final Polygon clipPolygon;

    //Fillers
    private SeedFill seedFiller;
    private final ScanLineFill scanLineFill;

    // Uložené objekty
    private final List<Line> lines = new ArrayList<>();
    private final Polygon polygon = new Polygon();

    private Point2D tempLineStart;
    private Point2D tempLineEnd;
    private Point2D mousePosition; // skutečná poloha myši při tažení úsečky

    private Point2D tempPolygonPoint;

    public Controller2D(Panel panel) {
        this.panel = panel;
        //Triviální Rasterizers
        LineRasterizerTrivial trivialRasterizer = new LineRasterizerTrivial(panel.getRaster());
        trivialRasterizer.setColor(BOUNDARY_COLOR); // hranice (úsečky i polygon)
        this.lineRasterizer = trivialRasterizer;
        this.polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        LineRasterizerTrivial clipLineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        clipLineRasterizer.setColor(CLIP_COLOR);
        this.clipRasterizer = new PolygonRasterizer(clipLineRasterizer);

        // ořezávací šestiúhelník
        int width = panel.getRaster().getWidth();
        int height = panel.getRaster().getHeight();
        clipPolygon = Polygon.createRegular(width / 2, height / 2, Math.min(width, height) / 3, 6);

        // Fillers
        seedFiller = new SeedFill(panel.getRaster());
        seedFiller.setFillColor(FILL_COLOR);
        seedFiller.setBoundaryColor(BOUNDARY_COLOR);

        scanLineFill = new ScanLineFill(panel.getRaster(), polygonRasterizer);
        scanLineFill.setFillColor(SCANLINE_COLOR);

        initListeners();
        updateTitle();
        drawScene();
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
                    mousePosition = new Point2D(e.getX(), e.getY());
                    tempLineEnd = lineEnd(mousePosition, e.isShiftDown());
                    drawScene();
                } else if (SwingUtilities.isRightMouseButton(e) && tempPolygonPoint != null) {
                    tempPolygonPoint = new Point2D(e.getX(), e.getY());
                    drawScene();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && tempLineStart != null) {
                    tempLineEnd = lineEnd(new Point2D(e.getX(), e.getY()), e.isShiftDown());
                    lines.add(new Line(tempLineStart, tempLineEnd));
                    tempLineStart = null;
                    tempLineEnd = null;
                    mousePosition = null;
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

                // přepnutí podmínky
                if (e.getKeyCode() == KeyEvent.VK_B) {
                    seedFiller.setUseBoundaryColor(!seedFiller.isUseBoundaryColor());
                    updateTitle();
                    drawScene();
                }

                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    updateLineEnd(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    updateLineEnd(false);
                }
            }
        });
    }

    private void updateLineEnd(boolean shift) {
        if (tempLineStart == null || mousePosition == null) return;
        tempLineEnd = lineEnd(mousePosition, shift);
        drawScene();
    }

    // Konec úsečky
    private Point2D lineEnd(Point2D mouse, boolean shift) {
        if (shift) {
            return tempLineStart.snapEnd(mouse);
        } else {
            return mouse;
        }
    }

    private void clearAll() {
        lines.clear();
        polygon.clear();
        tempLineStart = null;
        tempLineEnd = null;
        mousePosition = null;
        tempPolygonPoint = null;
        seedFiller.setSeed(null); // bez semínka se nic nevyplňuje
        seedFiller.setUseBoundaryColor(false);
        updateTitle();
        drawScene();
    }

    // režim v titulku
    private void updateTitle() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        if (frame == null) return;

        if (seedFiller.isUseBoundaryColor()) {
            frame.setTitle("PGRF1 – seed fill: barva hranice [B]");
        } else {
            frame.setTitle("PGRF1 – seed fill: barva pozadí [B]");
        }
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

        // ořezání + výplň
        if (polygon.getSize() >= 3) {
            Polygon clipped = shClipper.clip(polygon, clipPolygon);
            scanLineFill.setPolygon(clipped);
            scanLineFill.fill();
        }
        polygonRasterizer.rasterize(polygon);
        clipRasterizer.rasterize(clipPolygon);

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