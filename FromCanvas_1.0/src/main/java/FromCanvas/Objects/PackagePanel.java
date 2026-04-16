package FromCanvas.Objects;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import FromCanvas.GUI.EditorFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PackagePanel extends JPanel {
    
    private Point startDragPos;
    private boolean dragging = false;
    public String path;
    public String pathType;
    
    private JLabel imageLabel;
    private JLabel nameLabel;
    private ImageIcon icon;

    private String customName = null;
    private String customImagePath = null;
    
    public PackagePanel(int posX, int posY, int width, int height, String folderPath, String pathType,String Name, String ImgPath) {
        setBounds(posX, posY, width, height);
        setBackground(new Color(125, 125, 125,0));
        setLayout(new BorderLayout(0, 5));
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        
        initComponents(width, height);
        
        if (folderPath != null && !folderPath.isEmpty()) {
            path = folderPath;
            this.pathType = pathType;
            updateDisplay();
            setVisible(true);
        } else {
            initTypeChooser();
            setVisible(false);
        }

        if (Name != null) {
            setCustomName(Name);
        }
        if (ImgPath != null) {
            setCustomImage(ImgPath);
        }
        
        addMouseListeners();
        addActionListeners();
    }

    public void setCustomName(String name) {
        this.customName = name;
        if (name != null && !name.isEmpty()) {
            nameLabel.setText(name);
        }
    }

    public void setCustomImagePath(String path) {
        this.customImagePath = path;
    }

    public void setCustomImage(String imagePath) {
        this.customImagePath = imagePath;
        loadCustomImage(imagePath);
    }

    private void loadCustomImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            try {
                ImageIcon originalIcon = new ImageIcon(imagePath);
                Image img = originalIcon.getImage();
                int imageSize = imageLabel.getPreferredSize().width;
                Image scaledImg = img.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);
                imageLabel.setIcon(icon);
            } catch (Exception e) {
                e.printStackTrace();
                loadSystemIcon();
            }
        } else {
            loadSystemIcon();
        }
    }
    
    private void initComponents(int panelWidth, int panelHeight) {
        // Верхняя панель для картинки
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        
        int imageSize = Math.min(panelWidth - 20, panelHeight - 50);
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(imageSize, imageSize));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        topPanel.add(imageLabel);
        add(topPanel, BorderLayout.CENTER);
        
        // Нижняя панель для названия
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setOpaque(false);
        
        nameLabel = new JLabel();
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        bottomPanel.add(nameLabel);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Добавляем отступы
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setDefaultImage(int size) {
        // Если нет кастомной картинки и нет системной иконки, показываем заглушку
        if (customImagePath == null || customImagePath.isEmpty()) {
            loadSystemIcon();
        } else {
            Image img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) img.getGraphics();
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, size, size);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(0, 0, size - 1, size - 1);
            g2d.dispose();
            
            icon = new ImageIcon(img);
            imageLabel.setIcon(icon);
        }
    }
    
    private void loadSystemIcon() {
        if (path != null && !path.isEmpty()) {
            try {
                File file = new File(path);
                ImageIcon systemIcon = null;
                
                if (file.exists()) {
                    if ("url".equalsIgnoreCase(pathType)) {
                        // Для URL используем стандартную иконку веб-ссылки
                        systemIcon = getWebLinkIcon();
                    } else if (file.isDirectory()) {
                        // Получаем иконку папки из системы
                        systemIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
                    } else if (file.isFile()) {
                        // Получаем иконку файла из системы
                        systemIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
                    }
                    
                    if (systemIcon != null) {
                        int imageSize = imageLabel.getPreferredSize().width;
                        Image scaledImg = systemIcon.getImage().getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(scaledImg);
                        imageLabel.setIcon(icon);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Если не удалось загрузить системную иконку, показываем заглушку
        int size = imageLabel.getPreferredSize().width;
        Image img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, size, size);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, size - 1, size - 1);
        
        // Рисуем простую иконку файла/папки
        if (path != null && !"url".equalsIgnoreCase(pathType)) {
            File file = new File(path);
            if (file.isDirectory()) {
                // Рисуем иконку папки
                g2d.setColor(new Color(255, 200, 100));
                g2d.fillRect(size/4, size/3, size/2, size/3);
                g2d.setColor(new Color(255, 180, 80));
                g2d.fillRect(size/4, size/3, size/2, size/6);
            } else {
                // Рисуем иконку файла
                g2d.setColor(new Color(200, 200, 255));
                g2d.fillRect(size/4, size/4, size/2, size/2);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(size/3, size/3, size/3, size/4);
            }
        } else if ("url".equalsIgnoreCase(pathType)) {
            // Рисуем иконку ссылки
            g2d.setColor(new Color(100, 200, 255));
            g2d.fillOval(size/3, size/3, size/3, size/3);
            g2d.setColor(Color.WHITE);
            g2d.drawLine(size/2, size/3, size/2, 2*size/3);
            g2d.drawLine(size/3, size/2, 2*size/3, size/2);
        }
        
        g2d.dispose();
        icon = new ImageIcon(img);
        imageLabel.setIcon(icon);
    }
    
    private ImageIcon getWebLinkIcon() {
        int size = imageLabel.getPreferredSize().width;
        Image img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Рисуем иконку веб-ссылки
        g2d.setColor(new Color(100, 200, 255));
        g2d.fillOval(size/4, size/4, size/2, size/2);
        g2d.setColor(Color.WHITE);
        
        // Рисуем символ ссылки
        Font font = new Font("Arial", Font.BOLD, size/2);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        String linkSymbol = "🔗";
        int x = (size - fm.stringWidth(linkSymbol)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(linkSymbol, x, y);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    private void updateDisplay() {
        if (path != null && !path.isEmpty()) {
            if (pathType != null && pathType.equals("url")) {
                String name = path;
                if (name.startsWith("http://")) name = name.substring(7);
                if (name.startsWith("https://")) name = name.substring(8);
                if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
                nameLabel.setText(name);
            } else {
                nameLabel.setText(new File(path).getName());
            }
        } else {
            nameLabel.setText("null");
        }
        
        // Загружаем картинку (если нет кастомной, используем системную иконку)
        if (customImagePath == null || customImagePath.isEmpty()) {
            loadSystemIcon();
        } else {
            loadCustomImage(customImagePath);
        }
    }
    
    private void loadImage() {
        // Этот метод оставлен для совместимости, но теперь используем updateDisplay логику
        if (customImagePath != null && !customImagePath.isEmpty()) {
            loadCustomImage(customImagePath);
        } else {
            loadSystemIcon();
        }
        
        revalidate();
        repaint();
    }
    
    public void setPath(String newPath, String newType) {
        this.path = newPath;
        this.pathType = newType;
        System.out.println(newType);
        updateDisplay();
        setVisible(true);
    }
    
    private void initTypeChooser() {
        TypeChooser frame = new TypeChooser(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        frame.setVisible(true);
    }
    
    private void addActionListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    System.out.println(path + "__");
                    
                    // Проверяем тип пути
                    if ("url".equalsIgnoreCase(pathType)) {
                        openBrowser(path);
                    } else {
                        openFileExplorer(path);
                    }
                }
            }
        });
    }
    
    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragging = true;
                    startDragPos = SwingUtilities.convertPoint(PackagePanel.this, e.getPoint(), getParent());
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging && startDragPos != null) {
                    Point currentPos = SwingUtilities.convertPoint(PackagePanel.this, e.getPoint(), getParent());
                    int dx = currentPos.x - startDragPos.x;
                    int dy = currentPos.y - startDragPos.y;
                    setLocation(getLocation().x + dx, getLocation().y + dy);
                    startDragPos = currentPos;
                }
            }
        });
    }
    
    private static void openBrowser(String url) {
        if (url == null || url.isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "URL не указан", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Добавляем http:// если URL не содержит протокол
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                Runtime.getRuntime().exec("xdg-open " + url);
            } else {
                // Fallback - использовать Desktop API (Java 6+)
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    JOptionPane.showMessageDialog(null,
                        "Не удалось открыть браузер: неподдерживаемая ОС",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Не удалось открыть браузер: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void openFileExplorer(String path) {
        File folder = new File(path);
        
        if (!folder.exists()) {
            JOptionPane.showMessageDialog(null, 
                "Папка не существует: " + path, 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("explorer.exe " + path);
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("open " + path);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                Runtime.getRuntime().exec("xdg-open " + path);
            } else {
                JOptionPane.showMessageDialog(null,
                    "Неподдерживаемая операционная система",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Не удалось открыть проводник: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public String getType() {return "PackagePnl";}
    public int getPosX() { return getLocation().x; }
    public int getPosY() { return getLocation().y; }
    public int getPanelWidth() { return getWidth(); }
    public int getPanelHeight() { return getHeight(); }
    public String getPath() {return path;}
    public String getPathType() {return pathType;}
    public String getCustomName() {return customName;}
    public String getCustomImagePath() {return customImagePath;}
}