package FromCanvas.GUI;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;


import FromCanvas.Customization.*;
import FromCanvas.Data.*;
import FromCanvas.Objects.*;

public class EditorFrame extends JFrame implements ActionListener {
    private static final int FRONT_LAYER = 99;
    private static final int DEFAULT_SPAWN_SIZE = 150;
    private static final int TEMPLATE_AUTO_POSITION_SENTINEL = 1462683;
    private static final int GRID_STEP = 40;
    private static final int DOT_SIZE = 3;
    private static final int CONTEXT_MENU_PADDING = 4;

    public static boolean IsCursorBusy;
    public static boolean IsEditing;
    public static DraggableTextPanel currentlyEditingPanel = null;
    public static Arrow currentlyEditingArrow = null;
    public static DraggableImagePanel currentlyEditingImagePanel = null;
    static Container contentPane;
    static final ArrayList<DraggableTextPanel> panels = new ArrayList<>();
    static final ArrayList<PackagePanel> PackagePanels = new ArrayList<>();
    final ArrayList<DraggableImagePanel> imgPanels = new ArrayList<>();
    final ArrayList<Arrow> ArrowPanels = new ArrayList<>();
    public static JLayeredPane BackgroundPanel;

    private static int canvasOffsetX = 0;
    private static int canvasOffsetY = 0;
    private Point dragStart;
    private boolean isPanning = false;
    private JPopupMenu contextMenu;
    private Component currentContextComponent;

    JMenu fileMenu = new JMenu("File");
    static JMenu addMenu = new JMenu("Add");
    JMenu SettingsMenu = new JMenu("Settings");

    public EditorFrame() {
        initializeFrame();
        initializeBackgroundPanel();
        createContextMenu();
        setupGlobalMouseListeners();
        setupMenuBar();
        setupContainerListener();
    }

    private void initializeFrame() {
        setTitle("FromCanvas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
    }

    private void initializeBackgroundPanel() {
        BackgroundPanel = new JLayeredPane() {
            private final Color BACKGROUND_COLOR = new Color(0, 0, 0);
            private final Color DOT_COLOR = new Color(45, 45, 45);

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2d.setColor(DOT_COLOR);
                
                int startX = -(canvasOffsetX % GRID_STEP);
                int startY = -(canvasOffsetY % GRID_STEP);
                
                for (int x = startX; x < getWidth(); x += GRID_STEP) {
                    for (int y = startY; y < getHeight(); y += GRID_STEP) {
                        g2d.fillOval(x, y, DOT_SIZE, DOT_SIZE);
                    }
                }
                
                g2d.dispose();
            }
        };
        
        BackgroundPanel.setBounds(0, 0, getWidth(), getHeight());
        setContentPane(BackgroundPanel);
        getContentPane().setLayout(null);
        contentPane = getContentPane();
    }

    private void createContextMenu() {
        contextMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setForeground(new Color(255, 100, 100));
        deleteItem.setFont(deleteItem.getFont().deriveFont(12f));
        deleteItem.addActionListener(e -> {
            if (currentContextComponent != null) {
                deleteCurrentComponent();
            }
            contextMenu.setVisible(false);
        });
        
        contextMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
            BorderFactory.createEmptyBorder(CONTEXT_MENU_PADDING, CONTEXT_MENU_PADDING, 
                                           CONTEXT_MENU_PADDING, CONTEXT_MENU_PADDING)
        ));
        contextMenu.setBackground(new Color(32, 32, 32));
        contextMenu.setForeground(Color.WHITE);
        contextMenu.add(deleteItem);
    }

    private void setupGlobalMouseListeners() {
        contentPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    clearEditingState();
                    if (contextMenu.isVisible()) {
                        contextMenu.setVisible(false);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    Component clickedComponent = findComponentUnderCursor(e.getPoint());
                    
                    if (clickedComponent != null && clickedComponent != contentPane) {
                        currentContextComponent = clickedComponent;
                        contextMenu.show(contentPane, e.getX(), e.getY());
                        isPanning = false;
                    } else {
                        currentContextComponent = null;
                        isPanning = true;
                        dragStart = e.getPoint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (isPanning) {
                        isPanning = false;
                        dragStart = null;
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        });
        
        contentPane.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning && dragStart != null && SwingUtilities.isRightMouseButton(e)) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    
                    canvasOffsetX -= dx;
                    canvasOffsetY -= dy;
                    
                    for (Component comp : contentPane.getComponents()) {
                        comp.setLocation(comp.getX() + dx, comp.getY() + dy);
                    }
                    
                    dragStart = e.getPoint();
                    repaint();
                }
            }
        });
    }

    private void setupMenuBar() {
        JMenuBar menu = new JMenuBar();
        menu.setOpaque(false);
        menu.setBackground(new Color(0, 0, 0, 0));
        menu.setForeground(Color.WHITE);
        menu.setBorderPainted(false);
        menu.setMargin(new Insets(0, 0, 0, 0));
        setJMenuBar(menu);
        
        JMenuItem saveItem = createMenuItem("Save", e -> DataManager.save(panels, imgPanels, ArrowPanels, PackagePanels));
        JMenuItem saveAsItem = createMenuItem("Save As", e -> DataManager.saveAs(panels, imgPanels, ArrowPanels, PackagePanels));
        JMenuItem loadItem = createMenuItem("Load", this::loadFile);
        JMenuItem panelItem = createMenuItem("Text Panel", e -> createTextPanel());
        JMenuItem imgPanelItem = createMenuItem("Image Panel", e -> createImagePanel());
        JMenuItem arrowItem = createMenuItem("Arrow", e -> createArrow());
        JMenuItem PackagePanelItem = createMenuItem("Package Panel", e -> createPackagePanel(null,null));
        JMenuItem customizeItem = createMenuItem("Customization", e -> new PanelCustomization());
        JMenuItem templateMenuItem = createMenuItem("Template List", e -> new TemplateMenu());
        
        applyMenuMargin(saveItem, saveAsItem, loadItem, panelItem, imgPanelItem,PackagePanelItem, customizeItem, templateMenuItem, arrowItem);
        
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(loadItem);
        
        addMenu.add(imgPanelItem);
        addMenu.add(panelItem);
        addMenu.add(arrowItem);
        addMenu.add(PackagePanelItem);
        
        SettingsMenu.add(customizeItem);
        SettingsMenu.add(templateMenuItem);
        
        menu.add(fileMenu);
        menu.add(addMenu);
        menu.add(SettingsMenu);
    }
    
    private JMenuItem createMenuItem(String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }
    
    private void loadFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            DataManager.load(getContentPane(), file, panels, imgPanels, ArrowPanels,PackagePanels);
            for (Component comp : contentPane.getComponents()) {
                if (comp instanceof DraggableTextPanel || 
                    comp instanceof DraggableImagePanel || 
                    comp instanceof Arrow ||
                    comp instanceof PackagePanel) {
                    addContextMenuToComponent(comp);
                }
            }
            refreshUi();
        }
    }
    
    public void createTextPanel() {
        try {
            DraggableTextPanel panel = new DraggableTextPanel(
                TEMPLATE_AUTO_POSITION_SENTINEL, 
                TEMPLATE_AUTO_POSITION_SENTINEL, 
                "", 
                new Color(24, 24, 26), 
                new Color(24, 24, 26),2
            );
            addAndRegisterComponent(panel, panels);
        } catch (Exception ex) {
            showError("",ex);
        }
    }

    public void createPackagePanel(String text, String type) {
        try {
            Point pos = getRandomVisiblePosition(DEFAULT_SPAWN_SIZE, DEFAULT_SPAWN_SIZE);
            PackagePanel panel = new PackagePanel(pos.x, pos.y, 150, 150,text,type,null,null);
            addAndRegisterComponent(panel, PackagePanels);
        } catch (Exception ex) {
            showError("",ex);
        }
    }
    
    public void createImagePanel() {
        try {
            Point pos = getRandomVisiblePosition(DEFAULT_SPAWN_SIZE, DEFAULT_SPAWN_SIZE);
            DraggableImagePanel panel = new DraggableImagePanel(
                pos.x, pos.y, null, 
                new Color(24, 24, 26), 
                new Color(24, 24, 26)
            );
            addAndRegisterComponent(panel, imgPanels);
        } catch (Exception ex) {
            showError("", ex);
        }
    }
    
    public void createArrow() {
        try {
            Point pos = getRandomVisiblePosition(DEFAULT_SPAWN_SIZE, DEFAULT_SPAWN_SIZE);
            Arrow arrow = new Arrow(pos.x, pos.y, pos.x - 75, pos.y - 75, null, Color.LIGHT_GRAY, 12);
            addAndRegisterComponent(arrow, ArrowPanels);
        } catch (Exception ex) {
            showError("", ex);
        }
    }
    
    private <T extends Component> void addAndRegisterComponent(T component, ArrayList<T> list) {
        contentPane.add(component);
        list.add(component);
        BackgroundPanel.setLayer(component, FRONT_LAYER);
        addContextMenuToComponent(component);
        refreshUi();
    }
    
    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(this, message + ": " + ex.getMessage());
    }
    
    private void setupContainerListener() {
        getContentPane().addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                revalidate();
                repaint();
            }
            
            @Override
            public void componentRemoved(ContainerEvent e) {
                revalidate();
                repaint();
            }
        });
    }
    
    private void addContextMenuToComponent(Component comp) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    currentContextComponent = comp;
                    contextMenu.show(comp, e.getX(), e.getY());
                    e.consume();
                }
            }
        });
    }
    
    private void deleteCurrentComponent() {
        if (currentContextComponent == null) return;
        
        clearEditingState();
        
        if (currentContextComponent instanceof DraggableTextPanel) {
            panels.remove(currentContextComponent);
        } else if (currentContextComponent instanceof DraggableImagePanel) {
            imgPanels.remove(currentContextComponent);
        } else if (currentContextComponent instanceof Arrow) {
            ArrowPanels.remove(currentContextComponent);
        }
        
        contentPane.remove(currentContextComponent);
        refreshUi();
        currentContextComponent = null;
    }
    
    private Component findComponentUnderCursor(Point point) {
        Component[] components = contentPane.getComponents();
        for (int i = components.length - 1; i >= 0; i--) {
            Component comp = components[i];
            if (comp.isVisible() && comp.getBounds().contains(point)) {
                return comp;
            }
        }
        return null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {}

    public static void AddTemplateItem(JMenuItem itm) {
        for (int i = 0; i < addMenu.getItemCount(); i++) {
            JMenuItem existingItem = addMenu.getItem(i);
            if (existingItem != null && itm.getText() != null && 
                existingItem.getText().equals(itm.getText())) {
                addMenu.remove(existingItem);
                addMenu.add(itm);
                return;
            }
        }
        addMenu.add(itm);
    }
    
    private static Rectangle getVisibleArea() {
        return new Rectangle(-canvasOffsetX, -canvasOffsetY, contentPane.getWidth(), contentPane.getHeight());
    }
    
    public static Point getRandomVisiblePosition(int panelWidth, int panelHeight) {
        Rectangle visible = getVisibleArea();
        int minX = visible.x;
        int maxX = visible.x + visible.width - panelWidth;
        int minY = visible.y;
        int maxY = visible.y + visible.height - panelHeight;
        
        maxX = Math.max(maxX, minX);
        maxY = Math.max(maxY, minY);
        
        for (int attempt = 0; attempt < 120; attempt++) {
            int x = minX + (int) (Math.random() * (maxX - minX + 1));
            int y = minY + (int) (Math.random() * (maxY - minY + 1));
            int screenX = x + canvasOffsetX;
            int screenY = y + canvasOffsetY;
            
            if (!intersectsExistingObjects(screenX, screenY, panelWidth, panelHeight)) {
                return new Point(screenX, screenY);
            }
        }
        
        int x = minX + (int) (Math.random() * (maxX - minX + 1));
        int y = minY + (int) (Math.random() * (maxY - minY + 1));
        return new Point(x + canvasOffsetX, y + canvasOffsetY);
    }
    
    private static boolean intersectsExistingObjects(int x, int y, int width, int height) {
        if (contentPane == null) return false;
        
        Rectangle newBounds = new Rectangle(x, y, width, height);
        newBounds.grow(8, 8);
        
        for (Component comp : contentPane.getComponents()) {
            if (comp != null && comp.isVisible() && newBounds.intersects(comp.getBounds())) {
                return true;
            }
        }
        return false;
    }
    
    public static void AddNewJMenuItem(JMenuItem btn, String text, Color color1, Color color2) {
        btn.addActionListener(e -> {
            DraggableTextPanel panel = new DraggableTextPanel(
                TEMPLATE_AUTO_POSITION_SENTINEL, 
                TEMPLATE_AUTO_POSITION_SENTINEL, 
                text, color1, color2, 2
            );
            contentPane.add(panel);
            panels.add(panel);
            BackgroundPanel.setLayer(panel, FRONT_LAYER);
            if (contentPane != null) {
                contentPane.revalidate();
                contentPane.repaint();
            }
        });
    }
    
    private void applyMenuMargin(JMenuItem... items) {
        Insets margin = new Insets(2, 10, 2, 10);
        for (JMenuItem item : items) {
            item.setMargin(margin);
        }
    }
    
    private void refreshUi() {
        revalidate();
        repaint();
    }
    
    public static void beginPanelEditing(DraggableTextPanel panel) {
        if (panel == null || currentlyEditingPanel == panel) return;
        
        exitAllEdits();
        currentlyEditingPanel = panel;
        IsEditing = true;
    }
    
    public static void beginArrowEditing(Arrow arrow) {
        if (arrow == null || currentlyEditingArrow == arrow) return;
        
        exitAllEdits();
        currentlyEditingArrow = arrow;
        IsEditing = true;
    }
    
    public static void beginImageEditing(DraggableImagePanel imagePanel) {
        if (imagePanel == null || currentlyEditingImagePanel == imagePanel) return;
        
        exitAllEdits();
        currentlyEditingImagePanel = imagePanel;
        IsEditing = true;
    }
    
    private static void exitAllEdits() {
        if (currentlyEditingPanel != null) {
            currentlyEditingPanel.exitEditMode();
        }
        if (currentlyEditingArrow != null) {
            currentlyEditingArrow.exitEdit();
        }
        if (currentlyEditingImagePanel != null) {
            currentlyEditingImagePanel.exitEditMode();
        }
    }
    
    public static void clearEditingState() {
        exitAllEdits();
        currentlyEditingPanel = null;
        currentlyEditingArrow = null;
        currentlyEditingImagePanel = null;
        IsEditing = false;
    }
}