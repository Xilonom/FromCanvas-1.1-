package FromCanvas.Objects;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;

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
    private String tempIconPath = null; // Для временных иконок URL
    
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
        
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void loadSystemIcon() {
        if (path != null && !path.isEmpty()) {
            try {
                File file = new File(path);
                ImageIcon systemIcon = null;
                
                if ("url".equalsIgnoreCase(pathType)) {
                    systemIcon = getWebLinkIcon();
                    if (systemIcon == null) {
                        systemIcon = createGraySquareWithText();
                    } else {
                        // Сохраняем системную иконку URL как кастомное изображение
                        saveSystemIconAsCustomImage(systemIcon);
                    }
                } else if (file.exists()) {
                    if (file.isDirectory()) {
                        systemIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
                    } else if (file.isFile()) {
                        systemIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
                    }
                }
                
                if (systemIcon != null) {
                    int imageSize = imageLabel.getPreferredSize().width;
                    Image scaledImg = systemIcon.getImage().getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImg);
                    imageLabel.setIcon(icon);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Если не удалось загрузить системную иконку, показываем серый квадрат с названием
        ImageIcon fallbackIcon = createGraySquareWithText();
        if (fallbackIcon != null) {
            int imageSize = imageLabel.getPreferredSize().width;
            Image scaledImg = fallbackIcon.getImage().getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImg);
            imageLabel.setIcon(icon);
        }
    }
    
    private void saveSystemIconAsCustomImage(ImageIcon systemIcon) {
        try {
            // Создаем временную директорию для иконок если её нет
            File iconDir = new File(System.getProperty("user.home"), ".package_panel_icons");
            if (!iconDir.exists()) {
                iconDir.mkdirs();
            }
            
            // Создаем уникальное имя файла на основе URL
            String urlHash = String.valueOf(Math.abs(path.hashCode()));
            String iconFileName = "url_icon_" + urlHash + ".png";
            File iconFile = new File(iconDir, iconFileName);
            
            // Сохраняем иконку только если файл не существует или URL изменился
            if (!iconFile.exists() || tempIconPath == null || !tempIconPath.equals(iconFile.getAbsolutePath())) {
                // Конвертируем ImageIcon в BufferedImage
                BufferedImage bufferedImage = new BufferedImage(
                    systemIcon.getIconWidth(), 
                    systemIcon.getIconHeight(), 
                    BufferedImage.TYPE_INT_ARGB
                );
                Graphics g = bufferedImage.createGraphics();
                systemIcon.paintIcon(null, g, 0, 0);
                g.dispose();
                
                // Сохраняем как PNG
                ImageIO.write(bufferedImage, "png", iconFile);
                
                // Устанавливаем путь к сохраненной иконке как customImagePath
                this.customImagePath = iconFile.getAbsolutePath();
                this.tempIconPath = this.customImagePath;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Если не удалось сохранить, оставляем customImagePath как null
            this.customImagePath = null;
        }
    }
    
    private ImageIcon createGraySquareWithText() {
        int size = imageLabel.getPreferredSize().width;
        if (size <= 0) size = 64;
        
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Рисуем темно-серый квадрат
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRect(0, 0, size, size);
        
        // Рисуем рамку
        g2d.setColor(new Color(100, 100, 100));
        g2d.drawRect(0, 0, size - 1, size - 1);
        
        String displayText = "";
        if (pathType != null && "url".equalsIgnoreCase(pathType)) {
            String url = path;
            if (url != null) {
                if (url.startsWith("http://")) url = url.substring(7);
                if (url.startsWith("https://")) url = url.substring(8);
                if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

                if (url.length() > 0) {
                    String[] parts = url.split("\\.");
                    if (parts.length >= 2) {
                        String siteName = parts[parts.length - 2];
                        if (siteName.startsWith("www")) {
                            siteName = siteName.substring(3);
                        }
                        displayText = siteName.substring(0, Math.min(2, siteName.length())).toUpperCase();
                    } else {
                        displayText = url.substring(0, Math.min(2, url.length())).toUpperCase();
                    }
                }
            }
        } else if (customName != null && !customName.isEmpty()) {
            // Используем кастомное имя
            displayText = customName.substring(0, Math.min(2, customName.length())).toUpperCase();
        } else if (path != null && !"url".equalsIgnoreCase(pathType)) {
            // Для файлов/папок берем первую букву имени
            File file = new File(path);
            String name = file.getName();
            if (name != null && !name.isEmpty()) {
                displayText = name.substring(0, Math.min(2, name.length())).toUpperCase();
            } else {
                displayText = file.getAbsolutePath().substring(0, Math.min(2, file.getAbsolutePath().length())).toUpperCase();
            }
        }
        
        if (displayText.isEmpty()) {
            displayText = "?";
        }
        
        int fontSize = Math.max(12, size / 3);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(displayText);
        int textHeight = fm.getHeight();
        int x = (size - textWidth) / 2;
        int y = (size - textHeight) / 2 + fm.getAscent();
        
        g2d.setColor(Color.WHITE);
        g2d.drawString(displayText, x, y);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    private ImageIcon getWebLinkIcon() {
        try {
            int size = imageLabel.getPreferredSize().width;
            if (size <= 0) size = 64;
            
            // Пытаемся получить системную иконку для URL
            if (Desktop.isDesktopSupported()) {
                // На некоторых системах можно получить иконку для URL через временный .url файл
                File tempFile = File.createTempFile("url", ".url");
                if (tempFile.exists()) {
                    ImageIcon systemIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(tempFile);
                    tempFile.delete();
                    if (systemIcon != null && systemIcon.getImage() != null) {
                        return systemIcon;
                    }
                }
            }
            
            // Если не удалось получить системную иконку, возвращаем null
            return null;
        } catch (Exception e) {
            return null;
        }
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
    
    @SuppressWarnings("deprecation")
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
                new ProcessBuilder("explorer.exe", path).start();
            } else if (os.contains("mac")) {
                // macOS
                new ProcessBuilder("open", path).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                new ProcessBuilder("xdg-open", path).start();
            }
            else {
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
    public String getCustomImagePath() {
        // Если есть сохраненная иконка URL, возвращаем её путь
        if (tempIconPath != null && new File(tempIconPath).exists()) {
            return tempIconPath;
        }
        return customImagePath;
    }
}