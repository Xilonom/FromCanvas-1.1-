package FromCanvas.Objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import FromCanvas.GUI.EditorFrame;

public class Arrow extends JPanel {
    private Point start, end, control;
    private Color color;
    private int headSize = 10;
    private boolean editing, dragStart, dragEnd, dragControl, dragLine;
    private Point dragOffset;
    private boolean curved = false;
    public int DEFAULT_LAYER = 2;

    public Arrow(int x1, int y1, int x2, int y2, Point cnPoint, Color color, int headSize) {
        EditorFrame.BackgroundPanel.setLayer(this,DEFAULT_LAYER);
        start = new Point(x1, y1);
        end = new Point(x2, y2);
        
        if (cnPoint == null) {
        control = new Point((x1 + x2) / 2, (y1 + y2) / 2);
        } else {
            control = cnPoint;
            curved = (control.x != (x1 + x2) / 2 || control.y != (y1 + y2) / 2);
        }
        this.color = color;
        this.headSize = headSize;

        setOpaque(false);
        setBounds(calcBounds());

        addMouseListener(new MouseAdapter() {
            @Override 
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                
                Point p = e.getPoint();
                Point s = toLocal(start);
                Point eLoc = toLocal(end);
                Point c = toLocal(control);
                
                if (editing) {
                    if (s.distance(p) < 10) {
                        dragStart = true;
                        dragOffset = new Point(p.x - s.x, p.y - s.y);
                    } else if (eLoc.distance(p) < 10) {
                        dragEnd = true;
                        dragOffset = new Point(p.x - eLoc.x, p.y - eLoc.y);
                    } else if (c.distance(p) < 10) {
                        dragControl = true;
                        if (!curved) curved = true;
                        dragOffset = new Point(p.x - c.x, p.y - c.y);
                    } else if (isOnLine(p)) {
                        dragLine = true;
                        dragOffset = new Point(getX() - e.getXOnScreen(), getY() - e.getYOnScreen());
                    } else {
                        exitEdit();
                    }
                } else {
                    if (isOnLine(p)) {
                        dragLine = true;
                        dragOffset = new Point(getX() - e.getXOnScreen(), getY() - e.getYOnScreen());
                    }
                }
            }
            
            @Override 
            public void mouseReleased(MouseEvent e) {
                dragStart = dragEnd = dragControl = dragLine = false;
                updateBounds();
                repaint();
            }
            
            @Override 
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && isOnLine(e.getPoint())) {
                    if (editing) {
                        exitEdit();
                    } else {
                        enterEdit();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override 
            public void mouseDragged(MouseEvent e) {
                if (dragLine) {
                    int x = e.getXOnScreen() + dragOffset.x;
                    int y = e.getYOnScreen() + dragOffset.y;
                    setLocation(x, y);
                } else if (dragStart) {
                    Point p = toGlobal(e.getPoint());
                    start.setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
                    if (!curved) control.setLocation((start.x + end.x) / 2, (start.y + end.y) / 2);
                    updateBounds();
                    repaint();
                } else if (dragEnd) {
                    Point p = toGlobal(e.getPoint());
                    end.setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
                    if (!curved) control.setLocation((start.x + end.x) / 2, (start.y + end.y) / 2);
                    updateBounds();
                    repaint();
                } else if (dragControl) {
                    Point p = toGlobal(e.getPoint());
                    control.setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
                    if (!curved) curved = true;
                    updateBounds();
                    repaint();
                }
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        getActionMap().put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editing) exitEdit();
            }
        });
    }

    public void exitEdit() {
        if (EditorFrame.currentlyEditingArrow == this) {
            EditorFrame.currentlyEditingArrow = null;
            EditorFrame.IsEditing = false;
        }
        editing = false;
        EditorFrame.BackgroundPanel.setLayer(this,DEFAULT_LAYER);
        dragStart = dragEnd = dragControl = dragLine = false;
        repaint();
        if (getParent() != null) getParent().requestFocusInWindow();
    }

    public void enterEdit() {
        if (EditorFrame.currentlyEditingArrow == this) {
            return;
        }

        EditorFrame.beginArrowEditing(this);
        EditorFrame.IsEditing = true;
        editing = true;
        EditorFrame.BackgroundPanel.setLayer(this,10);
        repaint();
        requestFocusInWindow();
    }

    @Override
    public void setLocation(int x, int y) {
        int dx = x - getX();
        int dy = y - getY();
        super.setLocation(x, y);
        start.translate(dx, dy);
        end.translate(dx, dy);
        control.translate(dx, dy);
    }

    private boolean isOnLine(Point p) {
        if (curved) {
            Point s = toLocal(start), e = toLocal(end), c = toLocal(control);
            for (float t = 0; t <= 1; t += 0.05f) {
                double curveX = Math.pow(1 - t, 2) * s.x + 2 * (1 - t) * t * c.x + Math.pow(t, 2) * e.x;
                double curveY = Math.pow(1 - t, 2) * s.y + 2 * (1 - t) * t * c.y + Math.pow(t, 2) * e.y;
                if (p.distance(curveX, curveY) < 8) return true;
            }
            return false;
        } else {
            Point s = toLocal(start), e = toLocal(end);
            return Line2D.ptSegDist(s.x, s.y, e.x, e.y, p.x, p.y) < 5;
        }
    }

    @Override 
    public boolean contains(int x, int y) {
        return isOnLine(new Point(x, y)) || 
               (editing && (toLocal(start).distance(x, y) < 10 || 
                           toLocal(end).distance(x, y) < 10 || 
                           toLocal(control).distance(x, y) < 10));
    }

    private Point toLocal(Point global) { 
        return new Point(global.x - getX(), global.y - getY()); 
    }
    
    private Point toGlobal(Point local) { 
        return new Point(local.x + getX(), local.y + getY()); 
    }

    private Rectangle calcBounds() {
        int minX = Math.min(Math.min(start.x, end.x), control.x) - 20;
        int minY = Math.min(Math.min(start.y, end.y), control.y) - 20;
        int maxX = Math.max(Math.max(start.x, end.x), control.x) + 20;
        int maxY = Math.max(Math.max(start.y, end.y), control.y) + 20;
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private void updateBounds() { 
        setBounds(calcBounds()); 
    }

    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(3f)); 
        g2.setColor(color);

        Point s = toLocal(start), e = toLocal(end), c = toLocal(control);

        if (curved) {
            g2.draw(new QuadCurve2D.Float(s.x, s.y, c.x, c.y, e.x, e.y));
        } else {
            g2.drawLine(s.x, s.y, e.x, e.y);
        }

        drawHead(g2, e, curved ? c : s, 7);

        if (editing) {
            g2.setColor(Color.DARK_GRAY);
            g2.fillOval(s.x - 5, s.y - 5, 10, 10);
            g2.fillOval(e.x - 5, e.y - 5, 10, 10);
            
            int[] xp = {c.x, c.x + 6, c.x, c.x - 6};
            int[] yp = {c.y - 6, c.y, c.y + 6, c.y};
            g2.setColor(new Color(100, 100, 200));
            g2.fillPolygon(xp, yp, 4);
        }
    }

    private void drawHead(Graphics2D g2, Point tip, Point base, double ext) {
        double dx = tip.x - base.x, dy = tip.y - base.y;
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return;
        
        dx /= len; dy /= len;
        int x = (int)(tip.x + dx*ext), y = (int)(tip.y + dy*ext);
        double angle = Math.atan2(dy, dx);

        int[] xp = {x, 
                   (int)(x - headSize*Math.cos(angle - Math.PI/6)),
                   (int)(x - headSize*Math.cos(angle + Math.PI/6))};
        int[] yp = {y,
                   (int)(y - headSize*Math.sin(angle - Math.PI/6)),
                   (int)(y - headSize*Math.sin(angle + Math.PI/6))};
        g2.fillPolygon(xp, yp, 3);
    }

    public String getType() { return "Arrow"; }
    public double getPosX_1() { return start.x; }
    public double getPosY_1() { return start.y; }
    public double getPosX_2() { return end.x; }
    public double getPosY_2() { return end.y; }
    public double getControlX() { return control.x; }
    public double getControlY() { return control.y; }
    public boolean isCurved() { return curved; }
    public Color getColor() { return color; }
    public int getHead_size() { return headSize; }
    public boolean isEditing() { return editing; }
}