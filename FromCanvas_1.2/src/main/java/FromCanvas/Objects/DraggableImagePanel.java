package FromCanvas.Objects;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import FromCanvas.GUI.EditorFrame;

public class DraggableImagePanel extends JPanel {
    private Point startDragPos;
    private boolean editing = false;
    private boolean dragging = false;
    private boolean resizing = false;
    private boolean mouseOver = false;
    private JPanel buttonPanel;
    private JButton changeImageButton;

    Color color1;
    Color color2;

    String Type = "ImgPanel";

    private Image originalImage;
    String imagePath = "";
    
    private BufferedImage processedImage;
    
    private static final int RESIZE_HANDLE_SIZE = 20;
    private static final int MIN_SIZE = 100;
    private static final int MAX_SIZE = 800;
    private static final int DEFAULT_SIZE = 200;
    
    private Rectangle resizeZone;
    private int handleOffset = 5;
    
    private double imageAspectRatio = 1.0;
    
    private static final int PADDING = 5;
    private static final int PANEL_CORNER_RADIUS = 25;
    private static final int IMAGE_CORNER_RADIUS = 20;


    public DraggableImagePanel(int posX, int posY, String imgPath, Color clr1, Color clr2) {
        this(posX, posY, imgPath, clr1, clr2, -1, -1);
    }

    public DraggableImagePanel(int posX, int posY, String imgPath, Color clr1, Color clr2, int width, int height) {
        color1 = clr1;
        color2 = clr2;


        int initialWidth = (width > 0) ? width : DEFAULT_SIZE;
        int initialHeight = (height > 0) ? height : DEFAULT_SIZE;

        setOpaque(false);
        setPreferredSize(new Dimension(initialWidth, initialHeight));
        setLocation(new Point(posX, posY));
        setSize(initialWidth, initialHeight);
        setLayout(null);

        if (imgPath != null && !imgPath.isEmpty()) {
            imagePath = imgPath;
            loadImage(imagePath);
            
            if (width <= 0 || height <= 0) {
                adjustSizeToImageAspectRatio();
            }
            
            updateProcessedImage();
        }

        updateResizeZone();
        initButtonPanel();
        addMouseListeners();
    }

    private void adjustSizeToImageAspectRatio() {
        if (originalImage == null || imageAspectRatio <= 0) return;
        
        int newWidth, newHeight;
        
        if (imageAspectRatio >= 1.0) {
            // Горизонтальное или квадратное изображение
            newWidth = DEFAULT_SIZE;
            newHeight = (int) (DEFAULT_SIZE / imageAspectRatio);
            
            // Если высота выходит за пределы, пересчитываем от высоты
            if (newHeight < MIN_SIZE) {
                newHeight = MIN_SIZE;
                newWidth = (int) (newHeight * imageAspectRatio);
            } else if (newHeight > MAX_SIZE) {
                newHeight = MAX_SIZE;
                newWidth = (int) (newHeight * imageAspectRatio);
            }
        } else {
            // Вертикальное изображение
            newHeight = DEFAULT_SIZE;
            newWidth = (int) (DEFAULT_SIZE * imageAspectRatio);
            
            // Если ширина выходит за пределы, пересчитываем от ширины
            if (newWidth < MIN_SIZE) {
                newWidth = MIN_SIZE;
                newHeight = (int) (newWidth / imageAspectRatio);
            } else if (newWidth > MAX_SIZE) {
                newWidth = MAX_SIZE;
                newHeight = (int) (newWidth / imageAspectRatio);
            }
        }
        
        newWidth = constrain(newWidth, MIN_SIZE, MAX_SIZE);
        newHeight = constrain(newHeight, MIN_SIZE, MAX_SIZE);
        
        if (newWidth != getWidth() || newHeight != getHeight()) {
            setSize(newWidth, newHeight);
            setPreferredSize(new Dimension(newWidth, newHeight));
        }
    }

    private void initButtonPanel() {
        buttonPanel = new JPanel(null);
        buttonPanel.setOpaque(false);
        buttonPanel.setVisible(false);
        buttonPanel.setBounds(0, 0, getWidth(), getHeight());

        changeImageButton = new JButton("Img") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        changeImageButton.setToolTipText("Change image");
        changeImageButton.setPreferredSize(new Dimension(42, 20));
        changeImageButton.setMargin(new Insets(1, 1, 1, 1));
        changeImageButton.setFont(new Font("Consolas", Font.BOLD, 10));
        changeImageButton.setFocusable(false);
        changeImageButton.setOpaque(false);
        changeImageButton.setContentAreaFilled(false);
        changeImageButton.setBorderPainted(true);
        changeImageButton.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100, 150), 1));
        changeImageButton.setForeground(new Color(228, 228, 230, 190));
        changeImageButton.addActionListener(e -> openImageChooser());

        buttonPanel.add(changeImageButton);
        updateEditButtonBounds();
        add(buttonPanel);
        setComponentZOrder(buttonPanel, 0);
    }

    private void updateEditButtonBounds() {
        if (buttonPanel == null || changeImageButton == null) {
            return;
        }

        int panelWidth = Math.max(1, getWidth());
        int panelHeight = Math.max(1, getHeight());
        buttonPanel.setBounds(0, 0, panelWidth, panelHeight);

        int buttonWidth = Math.min(42, Math.max(12, panelWidth - 6));
        int buttonHeight = Math.min(20, Math.max(12, panelHeight - 6));
        int x = panelWidth - buttonWidth - 20;
        int y = 6;

        x = Math.max(0, Math.min(x, panelWidth - buttonWidth));
        y = Math.max(0, Math.min(y, panelHeight - buttonHeight));

        changeImageButton.setBounds(x, y, buttonWidth, buttonHeight);
    }

    private void loadImage(String path) {
        Image tempImage = Toolkit.getDefaultToolkit().createImage(path);
        
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(tempImage, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ignored) {}
        
        originalImage = tempImage;
        imagePath = path;
        
        int imgWidth = originalImage.getWidth(null);
        int imgHeight = originalImage.getHeight(null);
        if (imgWidth > 0 && imgHeight > 0) {
            imageAspectRatio = (double) imgWidth / imgHeight;
        }
    }

    private void openImageChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Изображения", "jpg", "jpeg", "png", "gif", "bmp"));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            System.out.println(imagePath);
            loadImage(imagePath);
            
            SwingUtilities.invokeLater(() -> {
                adjustSizeToImageAspectRatio();
                updateProcessedImage();
                revalidate();
                repaint();
            });
        }
    }

    private void updateProcessedImage() {
        if (originalImage != null) {
            int panelWidth = getWidth() - 2 * PADDING;
            int panelHeight = getHeight() - 2 * PADDING;
            
            if (panelWidth <= 0 || panelHeight <= 0) return;
            
            BufferedImage tempImage = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tempImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

            g2d.drawImage(originalImage, 0, 0, panelWidth, panelHeight, null);
            g2d.dispose();

            processedImage = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
            g2d = processedImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D roundedRect = new RoundRectangle2D.Float(0, 0, panelWidth, panelHeight, 
                                                                       IMAGE_CORNER_RADIUS, IMAGE_CORNER_RADIUS);

            g2d.setClip(roundedRect);

            g2d.drawImage(tempImage, 0, 0, null);
            
            g2d.dispose();
        }
    }

    private void addMouseListeners() {
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }
    
    private boolean isInResizeZone(Point point) {
        if (resizeZone == null) return false;
        return resizeZone.contains(point);
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

    private void enterEditMode() {
        if (originalImage == null) {
            return;
        }
        if (EditorFrame.currentlyEditingImagePanel == this) {
            return;
        }
        EditorFrame.beginImageEditing(this);
        editing = true;
        buttonPanel.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
        revalidate();
        repaint();
    }

    public void exitEditMode() {
        if (EditorFrame.currentlyEditingImagePanel == this) {
            EditorFrame.currentlyEditingImagePanel = null;
            EditorFrame.IsEditing = false;
        }
        editing = false;
        resizing = false;
        buttonPanel.setVisible(false);
        if (mouseOver) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
        revalidate();
        repaint();
    }
    
    private class MouseHandler extends MouseAdapter {
        private Point dragStartScreen;
        private Rectangle startBounds;
        private boolean movedSincePress;
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            
            Point point = e.getPoint();
            movedSincePress = false;
            
            if (editing && isInResizeZone(point)) {
                resizing = true;
                dragging = false;

                dragStartScreen = e.getLocationOnScreen();
                startBounds = getBounds();
                SwingUtilities.convertPoint(DraggableImagePanel.this, point, getParent());
                getSize();
                
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                e.consume();
            } 
            else {
                dragging = true;
                resizing = false;
                startDragPos = SwingUtilities.convertPoint(DraggableImagePanel.this, point, getParent());
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                e.consume();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            if (movedSincePress || dragging || resizing) return;

            if (originalImage == null) {
                if (e.getClickCount() == 1) {
                    openImageChooser();
                }
                return;
            }

            if (e.getClickCount() == 2) {
                if (editing) {
                    exitEditMode();
                } else {
                    enterEditMode();
                }
                return;
            }

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragging || resizing) {
                dragging = false;
                resizing = false;
                
                if (mouseOver) {
                    Point point = e.getPoint();
                    if (editing && isNearResizeZone(point)) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                
                updateResizeZone();
                revalidate();
                repaint();
                e.consume();
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
            mouseOver = true;
            Point point = e.getPoint();
            if (editing && isNearResizeZone(point)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            repaint();
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            mouseOver = false;
            if (!dragging && !resizing) {
                setCursor(Cursor.getDefaultCursor());
            }
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                EditorFrame.IsCursorBusy = false;
                return;
            }

            EditorFrame.IsCursorBusy = true;
            
            if (resizing && dragStartScreen != null && startBounds != null) {
                movedSincePress = true;
                Point currentScreen = e.getLocationOnScreen();
                int dx = currentScreen.x - dragStartScreen.x;
                int dy = currentScreen.y - dragStartScreen.y;

                int delta = Math.max(dx, dy);

                int newWidth, newHeight;
                
                if (imageAspectRatio >= 1.0) {
                    newWidth = constrain(startBounds.width + delta, MIN_SIZE, MAX_SIZE);
                    newHeight = (int) (newWidth / imageAspectRatio);

                    if (newHeight > MAX_SIZE) {
                        newHeight = MAX_SIZE;
                        newWidth = (int) (newHeight * imageAspectRatio);
                    } else if (newHeight < MIN_SIZE) {
                        newHeight = MIN_SIZE;
                        newWidth = (int) (newHeight * imageAspectRatio);
                    }
                } else {
                    newHeight = constrain(startBounds.height + delta, MIN_SIZE, MAX_SIZE);
                    newWidth = (int) (newHeight * imageAspectRatio);

                    if (newWidth > MAX_SIZE) {
                        newWidth = MAX_SIZE;
                        newHeight = (int) (newWidth / imageAspectRatio);
                    } else if (newWidth < MIN_SIZE) {
                        newWidth = MIN_SIZE;
                        newHeight = (int) (newWidth / imageAspectRatio);
                    }
                }

                setBounds(startBounds.x, startBounds.y, newWidth, newHeight);

                updateProcessedImage();

                updateResizeZone();
                
                revalidate();
                repaint();
            } 
            else if (dragging && startDragPos != null) {
                movedSincePress = true;
                Point currentPos = SwingUtilities.convertPoint(DraggableImagePanel.this, e.getPoint(), getParent());
                int dx = currentPos.x - startDragPos.x;
                int dy = currentPos.y - startDragPos.y;
                Point currentLocation = getLocation();
                setLocation(currentLocation.x + dx, currentLocation.y + dy);
                startDragPos = currentPos;
            }
            else {
                EditorFrame.IsCursorBusy = false;
            }
            e.consume();
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            Point point = e.getPoint();
            if (editing && isNearResizeZone(point)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }
    }
    
    private int constrain(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
       
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        RoundRectangle2D panelShape = new RoundRectangle2D.Double(5, 5, width - 10, height - 10, PANEL_CORNER_RADIUS, PANEL_CORNER_RADIUS);

        GradientPaint gradient = new GradientPaint(0, 0, color1, width, height, color2);
        g2d.setPaint(gradient);
        g2d.fill(panelShape);

        if (processedImage != null) {
            g2d.drawImage(processedImage, PADDING, PADDING, null);
        } else {
            drawEmptyState(g2d, width, height);
        }

        if (mouseOver || editing) {
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(new Color(0, 100, 200, 150));
            g2d.draw(panelShape);
        }
        
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.draw(panelShape);
        
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(0, 0, 0, 30));
        RoundRectangle2D innerRect = new RoundRectangle2D.Double(6, 6, width - 12, height - 12, PANEL_CORNER_RADIUS - 2, PANEL_CORNER_RADIUS - 2);
        g2d.draw(innerRect);
        
        if (editing && resizeZone != null) {
            drawResizeHandle(g2d);
        }
        
        g2d.dispose();
    }
    
    private void drawResizeHandle(Graphics2D g2d) {
        int x = resizeZone.x;
        int y = resizeZone.y;
        int size = RESIZE_HANDLE_SIZE;
        
        g2d.setColor(new Color(50, 50, 50, 100));
        g2d.fillOval(x, y, size, size);
        
        g2d.setColor(new Color(228, 228, 230));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x - 2, y - 2, size + 2, size + 2);
    }

    private void drawEmptyState(Graphics2D g2d, int width, int height) {
        int innerX = PADDING;
        int innerY = PADDING;
        int innerWidth = width - 2 * PADDING;
        int innerHeight = height - 2 * PADDING;
        if (innerWidth <= 0 || innerHeight <= 0) return;

        g2d.setColor(new Color(205, 205, 206));
        g2d.fillRoundRect(innerX, innerY, innerWidth, innerHeight, 20, 20);

        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2.2f));

        int centerX = width / 2;
        int centerY = height / 2;
        int size = Math.min(innerWidth, innerHeight) / 4;
        g2d.drawLine(centerX - size, centerY, centerX + size, centerY);
        g2d.drawLine(centerX, centerY - size, centerX, centerY + size);

        int minSide = Math.min(innerWidth, innerHeight);
        int fontSize = constrain(Math.round(minSide * 0.08f), 10, 22);
        int textOffsetY = constrain(Math.round(minSide * 0.11f), 12, 28);

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        g2d.setColor(new Color(100, 100, 100));
        String text = "Нажмите для выбора изображения";
        int textAreaWidth = Math.max(20, innerWidth - 16);
        int textStartY = centerY + size + textOffsetY;
        drawFittedCenteredText(g2d, text, centerX, textStartY, textAreaWidth, fontSize);
    }

    private void drawFittedCenteredText(Graphics2D g2d, String text, int centerX, int baseY, int maxWidth, int startFontSize) {
        int fontSize = startFontSize;
        List<String> lines = new ArrayList<>();

        while (fontSize >= 8) {
            g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
            lines = wrapText(text, g2d, maxWidth);

            boolean fits = true;
            for (String line : lines) {
                if (g2d.getFontMetrics().stringWidth(line) > maxWidth) {
                    fits = false;
                    break;
                }
            }
            if (fits) break;
            fontSize--;
        }

        int lineHeight = g2d.getFontMetrics().getHeight();
        int y = baseY;
        for (String line : lines) {
            int lineWidth = g2d.getFontMetrics().stringWidth(line);
            g2d.drawString(line, centerX - lineWidth / 2, y);
            y += lineHeight;
        }
    }

    private List<String> wrapText(String text, Graphics2D g2d, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (g2d.getFontMetrics().stringWidth(candidate) <= maxWidth) {
                currentLine = new StringBuilder(candidate);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        updateProcessedImage();
        updateResizeZone();
        updateEditButtonBounds();
        repaint();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        updateProcessedImage();
        updateResizeZone();
        updateEditButtonBounds();
        repaint();
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        updateResizeZone();
        updateEditButtonBounds();
        repaint();
    }

   private String storedImageData; // Для хранения Base64 данных изображения

    public String getStoredImageData() {
        return storedImageData;
    }

    public void setStoredImageData(String imageData) {
        this.storedImageData = imageData;
    }

    public int getPosX() {
        return (int) this.getLocation().getX();
    }

    public int getPosY() {
        return (int) this.getLocation().getY();
    }

    public String getType() {
        return Type;
    }
    
    public String getImg() {
        return imagePath;
    }
    
    public Color getColor1() {
        return color1;
    }
    
    public Color getColor2() {
        return color2;
    }
    
    public int getPanelWidth() {
        return getWidth();
    }
    
    public int getPanelHeight() {
        return getHeight();
    }
    
    public Dimension getPanelSize() {
        return getSize();
    }
    
    public double getImageAspectRatio() {
        return imageAspectRatio;
    }
    
    public boolean hasImage() {
        return originalImage != null;
    }
}