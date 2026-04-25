package FromCanvas.Objects;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

import FromCanvas.GUI.EditorFrame;

public class DraggableTextPanel extends JPanel {

    private Point startDragPos, resizeStartPos;
    private boolean dragging = false, resizing = false, mouseOver = false;
    private JTextPane textArea;
    private JPanel buttonPanel;
    private final String Type = "TxtPanel";
    private Color color1, color2;
    private int panelWidth, panelHeight;
    private int align;

    private static final int RESIZE_HANDLE_SIZE = 20;
    public static final int DEFAULT_WIDTH = 180, DEFAULT_HEIGHT = 180;
    public static final int MIN_WIDTH = 80, MIN_HEIGHT = 80;
    public static final int MAX_WIDTH = 600, MAX_HEIGHT = 600;
    public int DEFAULT_LAYER = 0;

    private Dimension resizeStartSize;
    private Rectangle resizeZone;
    private int handleOffset = 5;


    public DraggableTextPanel(int posX, int posY, String text, Color clr1, Color clr2, Integer alignment) {
        this(posX, posY, DEFAULT_WIDTH, DEFAULT_HEIGHT, text, clr1, clr2,alignment);
    }

    public DraggableTextPanel(Integer posX, Integer posY, int width, int height, String text, Color clr1, Color clr2,Integer alignment) {
        this.panelWidth = constrain(width, MIN_WIDTH, MAX_WIDTH);
        this.panelHeight = constrain(height, MIN_HEIGHT, MAX_HEIGHT);
        EditorFrame.BackgroundPanel.setLayer(this,DEFAULT_LAYER);
        Point pos = EditorFrame.getRandomVisiblePosition(180, 180);
        if (posX == 1462683 || posY == 1462683) {
            posX = pos.x;
            posY = pos.y;
        }

        setLayout(null);
        setOpaque(false);
        setBounds(posX, posY, panelWidth, panelHeight);
        align = alignment;
        color1 = clr1;
        color2 = clr2;

        initTextArea(text);
        initButtonPanel();
        updateResizeZone();
        addMouseListeners();


        textArea.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) enterEditMode();
                else {
                    MouseEvent parentEvent = SwingUtilities.convertMouseEvent(textArea, e, DraggableTextPanel.this);
                    DraggableTextPanel.this.dispatchEvent(parentEvent);
                }
            }
            @Override public void mousePressed(MouseEvent e) {
                MouseEvent parentEvent = SwingUtilities.convertMouseEvent(textArea, e, DraggableTextPanel.this);
                DraggableTextPanel.this.dispatchEvent(parentEvent);
            }

            @Override public void mouseReleased(MouseEvent e) {
                MouseEvent parentEvent = SwingUtilities.convertMouseEvent(textArea, e, DraggableTextPanel.this);
                DraggableTextPanel.this.dispatchEvent(parentEvent);
            }

            @Override public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                repaint();
            }

            @Override public void mouseExited(MouseEvent e) {
                mouseOver = false;
                repaint();
            }
        });

        textArea.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                MouseEvent parentEvent = SwingUtilities.convertMouseEvent(textArea, e, DraggableTextPanel.this);
                DraggableTextPanel.this.dispatchEvent(parentEvent);
            }

            @Override public void mouseMoved(MouseEvent e) {
                MouseEvent parentEvent = SwingUtilities.convertMouseEvent(textArea, e, DraggableTextPanel.this);
                DraggableTextPanel.this.dispatchEvent(parentEvent);
            }
        });

        textArea.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) exitEditMode();
            }
        });
    }

    private int constrain(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void initTextArea(String text) {
        textArea = new JTextPane();
        textArea.setOpaque(false);
        Color textColor = new Color(228, 228, 230);
        textArea.setForeground(textColor);

        //textArea.setSelectedTextColor(textColor);
        //textArea.setSelectionColor(new Color(50, 50, 50, 100));
        textArea.setDisabledTextColor(textColor);
        textArea.setEnabled(false);
        textArea.setContentType("text/html");
        textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, 23));

        if (align == 2) {
            textArea.setText("<html><body>" +
                    "<p style='text-align: center; margin: 0; padding: 10px;'>" +
                    text +
                    "</p>"+
                    "</body><html>"
            );
        }
        else if (align == 1) {
            textArea.setText("<html><body>" +
                    "<p style='text-align: left; margin: 0; padding: 10px;'>" +
                    text +
                    "</p>"+
                    "</body><html>"
            );
        }
        else if (align == 3) {
            textArea.setText("<html><body>" +
                    "<p style='text-align: right; margin: 0; padding: 10px;'>" +
                    text +
                    "</p>"+
                    "</body><html>"
            );
        }

        textArea.setFocusable(false);

        int padding = Math.min(panelWidth, panelHeight) / 12;
        textArea.setBounds(padding, padding, panelWidth - 2 * padding, panelHeight - 2 * padding);

        add(textArea);
    }

    private void initButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setVisible(false);
        buttonPanel.setBounds(-3, 2, panelWidth, 30);

        JButton LeftAlignmentBtn = new JButton("◀");
        JButton CenterAlignmentBtn = new JButton("☰");
        JButton RightAlignmentBtn = new JButton("▶");



        LeftAlignmentBtn.setBackground(new Color(50, 50, 50));
        LeftAlignmentBtn.setForeground(Color.WHITE);
        LeftAlignmentBtn.setBorderPainted(false);

        RightAlignmentBtn.setBackground(new Color(50, 50, 50));
        RightAlignmentBtn.setForeground(Color.WHITE);
        RightAlignmentBtn.setBorderPainted(false);

        CenterAlignmentBtn.setBackground(new Color(50, 50, 50));
        CenterAlignmentBtn.setForeground(Color.WHITE);
        CenterAlignmentBtn.setBorderPainted(false);

        CenterAlignmentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cleanText = textArea.getText().replaceAll("(?s)<[^>]*>", "");
                textArea.setText("<html><body><p style='text-align: center; margin: 0; padding: 10px;'>"
                        + cleanText + "</p></body></html>");
                align = 2;
            }

        });
        LeftAlignmentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cleanText = textArea.getText().replaceAll("(?s)<[^>]*>", "");
                textArea.setText("<html><body><p style='text-align: left; margin: 0; padding: 10px;'>"
                        + cleanText + "</p></body></html>");
                align = 1;
            }

        });
        RightAlignmentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cleanText = textArea.getText().replaceAll("(?s)<[^>]*>", "");
                textArea.setText("<html><body><p style='text-align: right; margin: 0; padding: 10px;'>"
                        + cleanText + "</p></body></html>");
                align = 3;
            }

        });
        buttonPanel.add(LeftAlignmentBtn);
        buttonPanel.add(CenterAlignmentBtn);
        buttonPanel.add(RightAlignmentBtn);
        add(buttonPanel);
        setComponentZOrder(buttonPanel, 0);
    }

    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;

                Point point = e.getPoint();

                if (textArea.isEnabled() && isInResizeZone(point)) {
                    resizing = true;
                    dragging = false;
                    resizeStartPos = SwingUtilities.convertPoint(DraggableTextPanel.this, point, getParent());
                    resizeStartSize = getSize();
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                }
                else if (!textArea.isEnabled()) {
                    dragging = true;
                    resizing = false;
                    startDragPos = SwingUtilities.convertPoint(DraggableTextPanel.this, point, getParent());
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                e.consume();
            }

            @Override public void mouseReleased(MouseEvent e) {
                if (dragging || resizing) {
                    dragging = false;
                    resizing = false;

                    if (!textArea.isEnabled()) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }

                    updateResizeZone();
                    revalidate();
                    repaint();
                    e.consume();
                }
            }

            @Override public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                if (!textArea.isEnabled()) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                repaint();
            }

            @Override public void mouseExited(MouseEvent e) {
                mouseOver = false;
                if (!dragging && !resizing) {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            }

            @Override public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    EditorFrame.IsCursorBusy = false;
                    return;
                }

                Point currentPos = SwingUtilities.convertPoint(DraggableTextPanel.this, e.getPoint(), getParent());

                EditorFrame.IsCursorBusy = true;

                if (resizing && resizeStartPos != null && resizeStartSize != null) {
                    handleResize(currentPos);
                }
                else if (dragging && startDragPos != null) {
                    handleDrag(currentPos);
                }
                else {
                    EditorFrame.IsCursorBusy = false;
                }
                e.consume();
            }

            @Override public void mouseMoved(MouseEvent e) {
                Point point = e.getPoint();

                if (textArea.isEnabled() && isNearResizeZone(point)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                }
                else if (!textArea.isEnabled() && !dragging) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                else if (textArea.isEnabled() && !isNearResizeZone(point)) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    private boolean isNearResizeZone(Point point) {
        if (resizeZone == null) return false;
        int tolerance = 10;
        Rectangle extendedZone = new Rectangle(
                resizeZone.x - tolerance, resizeZone.y - tolerance,
                resizeZone.width + tolerance * 2, resizeZone.height + tolerance * 2
        );
        return extendedZone.contains(point);
    }

    private void handleDrag(Point currentPos) {
        if (startDragPos == null) return;
        int dx = currentPos.x - startDragPos.x;
        int dy = currentPos.y - startDragPos.y;
        Point currentLocation = getLocation();
        setLocation(currentLocation.x + dx, currentLocation.y + dy);
        startDragPos = currentPos;
        repaint();
    }

    private void handleResize(Point currentPos) {
        if (resizeStartPos == null || resizeStartSize == null) return;

        int deltaX = currentPos.x - resizeStartPos.x;
        int deltaY = currentPos.y - resizeStartPos.y;

        int newWidth = constrain(resizeStartSize.width + deltaX, MIN_WIDTH, MAX_WIDTH);
        int newHeight = constrain(resizeStartSize.height + deltaY, MIN_HEIGHT, MAX_HEIGHT);

        panelWidth = newWidth;
        panelHeight = newHeight;
        setSize(newWidth, newHeight);

        int padding = Math.min(newWidth, newHeight) / 12;
        textArea.setBounds(padding, padding, newWidth - 2 * padding, newHeight - 2 * padding);
        buttonPanel.setBounds(-3, 2, newWidth, 30);

        updateResizeZone();
        //adjustFontSize();
        revalidate();
        repaint();

        resizeStartPos = currentPos;
        resizeStartSize = getSize();
    }

    private void updateResizeZone() {
        int width = getWidth();
        int height = getHeight();
        resizeZone = new Rectangle(
                width - RESIZE_HANDLE_SIZE - handleOffset,
                height - RESIZE_HANDLE_SIZE - handleOffset,
                RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE
        );
    }

    private boolean isInResizeZone(Point point) {
        return resizeZone != null && resizeZone.contains(point);
    }



    private void enterEditMode() {
        if (EditorFrame.currentlyEditingPanel == this) {
            return;
        }

        EditorFrame.beginPanelEditing(this);
        EditorFrame.BackgroundPanel.setLayer(this,10);

        textArea.setEnabled(true);
        textArea.setFocusable(true);
        textArea.requestFocus();
        textArea.setForeground(new Color(228, 228, 230));
        textArea.setCaretColor(new Color(228, 228, 230));
        setCursor(Cursor.getDefaultCursor());
        buttonPanel.setVisible(true);
        updateResizeZone();
        revalidate();
        repaint();
    }

    public void exitEditMode() {
        if (EditorFrame.currentlyEditingPanel == this) {
            EditorFrame.BackgroundPanel.setLayer(this,DEFAULT_LAYER);
            EditorFrame.currentlyEditingPanel = null;
            EditorFrame.IsEditing = false;
        }

        textArea.setEnabled(false);
        textArea.setFocusable(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        buttonPanel.setVisible(false);
        updateResizeZone();
        revalidate();
        repaint();
    }





    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        RoundRectangle2D roundedRect = new RoundRectangle2D.Double(5, 5, width - 10, height - 10, 25, 25);
        GradientPaint gradient = new GradientPaint(0, 0, color1, width, height, color2);

        g2.setPaint(gradient);
        g2.fill(roundedRect);

        if (textArea.isEnabled()) {
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(228, 228, 230));
            g2.draw(roundedRect);

            if (resizeZone != null) drawResizeHandle(g2);
        }
        else if (mouseOver) {
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(0, 100, 200, 150));
            g2.draw(roundedRect);
        }

        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 255, 255, 100));
        g2.draw(roundedRect);

        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(0, 0, 0, 30));
        RoundRectangle2D innerRect = new RoundRectangle2D.Double(6, 6, width - 12, height - 12, 23, 23);
        g2.draw(innerRect);
    }

    private void drawResizeHandle(Graphics2D g2) {
        int x = resizeZone.x;
        int y = resizeZone.y;
        int size = RESIZE_HANDLE_SIZE;

        g2.setColor(new Color(50, 50, 50, 100));
        g2.fillOval(x, y, size, size);

        g2.setColor(new Color(228, 228, 230));
        g2.drawOval(x-2, y-2, size+2, size+2);

        g2.setColor(new Color(100, 100, 100, 220));

    }

    @Override protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintChildren(g);
    }

    public int getHandleOffset() { return handleOffset; }
    public void setHandleOffset(int offset) {
        this.handleOffset = offset;
        updateResizeZone();
        repaint();
    }

    public int getPosX() { return getLocation().x; }
    public int getPosY() { return getLocation().y; }
    public int getPanelWidth() { return panelWidth; }
    public int getPanelHeight() { return panelHeight; }
    public Dimension getPanelSize() { return new Dimension(panelWidth, panelHeight); }
    public int getMinWidth() { return MIN_WIDTH; }
    public int getMinHeight() { return MIN_HEIGHT; }
    public int getMaxWidth() { return MAX_WIDTH; }
    public int getMaxHeight() { return MAX_HEIGHT; }
    public String getText() { return textArea.getText().replaceAll("(?s)<[^>]*>", "");}
    public String getType() { return Type; }
    public Color getColor1() { return color1; }
    public Color getColor2() { return color2; }
    public int getAlignment() {return align;}

    public void setPanelSize(int width, int height) {
        int newWidth = constrain(width, MIN_WIDTH, MAX_WIDTH);
        int newHeight = constrain(height, MIN_HEIGHT, MAX_HEIGHT);

        panelWidth = newWidth;
        panelHeight = newHeight;
        setSize(newWidth, newHeight);

        int padding = Math.min(newWidth, newHeight) / 12;
        textArea.setBounds(padding, padding, newWidth - 2 * padding, newHeight - 2 * padding);
        buttonPanel.setBounds(0, 0, newWidth, 30);

        updateResizeZone();
        revalidate();
        repaint();
    }

    @Override public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        updateResizeZone();
    }

    @Override public void setSize(int width, int height) {
        super.setSize(width, height);
        updateResizeZone();
    }
}