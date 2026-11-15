package controller;

import fill.Filler;
import clip.CSClipper;
import clip.SHClipper;
import fill.ScanLineFill;
import fill.SeedFill;
import model.Line;
import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.PolygonRasterizer;
import view.ClipToolbar;
import view.Panel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    private static final int WINDOW_COLOR = 0xFF8800;   // oranžová
    private static final int CLIPPED_LINE_COLOR = 0xFF0000; // červená

    // režim ořezu
    private enum ClipMode {
        NONE,
        POLYGON,
        LINE
    }

    private final Panel panel;
    private final ClipToolbar clipToolbar;
    private ClipMode clipMode = ClipMode.NONE;
    private boolean editingClipPolygon = false; // Ctrl + pravé tlačítko
    private boolean clipDrawingStarted = false; // během jednoho podržení Ctrl
    private boolean customClipPolygon = false;  // vlastní místo šestiúhelníku

    //Rasterizace
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;
    private final LineRasterizer clipLineRasterizer;
    private final PolygonRasterizer clipRasterizer;
    private final PolygonRasterizer windowRasterizer;
    private final LineRasterizer clippedLineRasterizer;

    // Ořezávání
    private final SHClipper shClipper = new SHClipper();
    private Polygon clipPolygon;
    private CSClipper csClipper;
    private Polygon clipWindow;

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

    public Controller2D(Panel panel, ClipToolbar clipToolbar) {
        this.panel = panel;
        this.clipToolbar = clipToolbar;
        //Triviální Rasterizers
        LineRasterizerTrivial trivialRasterizer = new LineRasterizerTrivial(panel.getRaster());
        trivialRasterizer.setColor(BOUNDARY_COLOR); // hranice (úsečky i polygon)
        this.lineRasterizer = trivialRasterizer;
        this.polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        LineRasterizerTrivial yellowRasterizer = new LineRasterizerTrivial(panel.getRaster());
        yellowRasterizer.setColor(CLIP_COLOR);
        this.clipLineRasterizer = yellowRasterizer;
        this.clipRasterizer = new PolygonRasterizer(clipLineRasterizer);

        createClipShapes();

        LineRasterizerTrivial windowLineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        windowLineRasterizer.setColor(WINDOW_COLOR);
        this.windowRasterizer = new PolygonRasterizer(windowLineRasterizer);

        LineRasterizerTrivial redRasterizer = new LineRasterizerTrivial(panel.getRaster());
        redRasterizer.setColor(CLIPPED_LINE_COLOR);
        this.clippedLineRasterizer = redRasterizer;

        // Fillers
        seedFiller = new SeedFill(panel.getRaster());
        seedFiller.setFillColor(FILL_COLOR);
        seedFiller.setBoundaryColor(BOUNDARY_COLOR);

        scanLineFill = new ScanLineFill(panel.getRaster(), polygonRasterizer);
        scanLineFill.setFillColor(SCANLINE_COLOR);

        initListeners();
        initToolbar();
        drawScene();
    }

    // podle velikosti rastru
    private void createClipShapes() {
        int width = panel.getRaster().getWidth();
        int height = panel.getRaster().getHeight();

        // ořezávací šestiúhelník
        if (!customClipPolygon) {
            clipPolygon = Polygon.createRegular(width / 2, height / 2, Math.min(width, height) / 3, 6);
        }

        // ořezávací okno
        int xMin = width / 8;
        int yMin = height / 8;
        int xMax = width - width / 8;
        int yMax = height - height / 8;
        csClipper = new CSClipper(xMin, yMin, xMax, yMax);

        clipWindow = new Polygon();
        clipWindow.addPoint(new Point2D(xMin, yMin));
        clipWindow.addPoint(new Point2D(xMax, yMin));
        clipWindow.addPoint(new Point2D(xMax, yMax));
        clipWindow.addPoint(new Point2D(xMin, yMax));
    }

    private void initToolbar() {
        ActionListener clipListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == clipToolbar.getPolygonClipButton()) {
                    clipMode = ClipMode.POLYGON;
                } else if (e.getSource() == clipToolbar.getLineClipButton()) {
                    clipMode = ClipMode.LINE;
                } else {
                    clipMode = ClipMode.NONE;
                }
                drawScene();
            }
        };

        clipToolbar.getNoClipButton().addActionListener(clipListener);
        clipToolbar.getPolygonClipButton().addActionListener(clipListener);
        clipToolbar.getLineClipButton().addActionListener(clipListener);
    }

    // první Ctrl + klik
    private void startClipPolygon() {
        clipPolygon = new Polygon();
        customClipPolygon = true;
        clipDrawingStarted = true;

        clipMode = ClipMode.POLYGON;
        clipToolbar.getPolygonClipButton().setSelected(true);
    }

    // kam přidává pravé tlačítko
    private Polygon activePolygon() {
        if (editingClipPolygon) {
            return clipPolygon;
        } else {
            return polygon;
        }
    }

    // lze ořezávat?
    private boolean isClipPolygonValid() {
        return clipPolygon.isConvex();
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
                    editingClipPolygon = e.isControlDown();
                    if (editingClipPolygon && !clipDrawingStarted) {
                        startClipPolygon();
                    }
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
                    activePolygon().addPoint(new Point2D(e.getX(), e.getY()));
                    tempPolygonPoint = null;
                    drawScene();
                }
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        // změna velikosti okna
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                panel.getRaster().resize(panel.getWidth(), panel.getHeight());
                createClipShapes();
                drawScene();
            }
        });

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
                    drawScene();
                }

                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    updateLineEnd(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // další podržení = nový polygon
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    clipDrawingStarted = false;
                }

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
        clipMode = ClipMode.NONE;
        clipToolbar.getNoClipButton().setSelected(true);
        editingClipPolygon = false;
        clipDrawingStarted = false;
        customClipPolygon = false;
        createClipShapes(); // zpět šestiúhelník
        drawScene();
    }

    // režim v titulku
    private void updateTitle() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        if (frame == null) return;

        String title;
        if (seedFiller.isUseBoundaryColor()) {
            title = "PGRF1 – seed fill: barva hranice [B]";
        } else {
            title = "PGRF1 – seed fill: barva pozadí [B]";
        }

        if (clipMode == ClipMode.POLYGON && !isClipPolygonValid()) {
            title += " | ořezávací polygon není konvexní – bez ořezu";
        }
        frame.setTitle(title);
    }

    // celá bíle, ořez červeně
    private void drawLine(Line line) {
        lineRasterizer.rasterize(line);

        if (clipMode == ClipMode.LINE) {
            Line clipped = csClipper.clip(line);
            if (clipped != null) {
                clippedLineRasterizer.rasterize(clipped);
            }
        }
    }

    private void drawScene() {
        panel.getRaster().clear();

        for (Line line : lines) {
            drawLine(line);
        }

        // Pružná Úsečka
        if (tempLineStart != null && tempLineEnd != null) {
            drawLine(new Line(tempLineStart, tempLineEnd));
        }

        // ořezávat jen konvexním
        boolean clipping = clipMode == ClipMode.POLYGON && isClipPolygonValid();

        // výplň (případně ořezaná)
        if (polygon.getSize() >= 3) {
            Polygon toFill = polygon;
            if (clipping) {
                toFill = shClipper.clip(polygon, clipPolygon);
            }
            scanLineFill.setPolygon(toFill);
            scanLineFill.fill();
        }

        // obrys (jinak ho kreslí scan-line)
        if (polygon.getSize() < 3 || clipping) {
            polygonRasterizer.rasterize(polygon);
        }

        // ořezávací tvary
        if (clipMode == ClipMode.POLYGON) {
            clipRasterizer.rasterize(clipPolygon);
        }
        if (clipMode == ClipMode.LINE) {
            windowRasterizer.rasterize(clipWindow);
        }

        // Pružná úsečka por polygon
        if (tempPolygonPoint != null) {
            Polygon active = activePolygon();
            LineRasterizer rubberRasterizer;
            if (editingClipPolygon) {
                rubberRasterizer = clipLineRasterizer; // žlutě
            } else {
                rubberRasterizer = lineRasterizer;
            }

            int size = active.getSize();
            if (size == 1) {
                rubberRasterizer.rasterize(active.getPoint(0), tempPolygonPoint);
            } else if (size >= 2) {
                Point2D lastPoint = active.getPoint(size - 1);
                Point2D firstPoint = active.getPoint(0);
                rubberRasterizer.rasterize(lastPoint, tempPolygonPoint);
                rubberRasterizer.rasterize(tempPolygonPoint, firstPoint);
            }
        }

        seedFiller.fill();

        updateTitle();
        panel.repaint();
    }
}