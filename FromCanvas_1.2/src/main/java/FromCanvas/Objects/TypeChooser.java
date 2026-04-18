package FromCanvas.Objects;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;


public class TypeChooser extends JFrame {
    private JRadioButton fileRadio;
    private JRadioButton urlRadio;
    private ButtonGroup group;
    
    private JTextField pathField;
    private JTextField urlField;
    
    private JTextField nameField;
    private JTextField imageField;
    private JButton browseImageButton;
    private JButton browsePathButton;
    
    private JButton confirmButton;
    private JButton cancelButton;
    private PackagePanel parentPanel;
    private boolean confirmed = false;
    
    public TypeChooser(PackagePanel parent) {
        this.parentPanel = parent;
        setTitle("Source Chooser");
        setSize(550, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!confirmed) {
                    removeParentPanel();
                }
            }
        });
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(43, 43, 43));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        
        JLabel titleLabel = new JLabel("Create New Package Item");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(255, 255, 255));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);
        
        JLabel nameLabel = new JLabel("Name (optional):");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(nameLabel, gbc);
        
        nameField = createTextField();
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(nameField, gbc);
        
        JLabel imageLabel = new JLabel("Image Path (optional):");
        imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        imageLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(imageLabel, gbc);
        
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setBackground(new Color(43, 43, 43));
        
        imageField = createTextField();
        browseImageButton = createBrowseButton("Browse");
        browseImageButton.addActionListener(e -> chooseImageFile());
        
        GridBagConstraints imageGbc = new GridBagConstraints();
        imageGbc.fill = GridBagConstraints.HORIZONTAL;
        imageGbc.weightx = 1.0;
        imageGbc.gridx = 0;
        imageGbc.insets = new Insets(0, 0, 0, 8);
        imagePanel.add(imageField, imageGbc);
        
        imageGbc.fill = GridBagConstraints.NONE;
        imageGbc.weightx = 0;
        imageGbc.gridx = 1;
        imageGbc.insets = new Insets(0, 0, 0, 0);
        imagePanel.add(browseImageButton, imageGbc);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 25, 0);
        mainPanel.add(imagePanel, gbc);
        
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(80, 80, 80));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(separator, gbc);
        
        JLabel typeLabel = new JLabel("Select Source Type");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        typeLabel.setForeground(new Color(220, 220, 220));
        typeLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(typeLabel, gbc);
        
        fileRadio = createRadioButton("File / Folder");
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(fileRadio, gbc);
        
        JPanel pathPanel = new JPanel(new GridBagLayout());
        pathPanel.setBackground(new Color(43, 43, 43));
        
        pathField = createTextField();
        browsePathButton = createBrowseButton("Browse");
        browsePathButton.addActionListener(e -> browseFileOrFolder());
        
        GridBagConstraints pathGbc = new GridBagConstraints();
        pathGbc.fill = GridBagConstraints.HORIZONTAL;
        pathGbc.weightx = 1.0;
        pathGbc.gridx = 0;
        pathGbc.insets = new Insets(0, 0, 0, 8);
        pathPanel.add(pathField, pathGbc);
        
        pathGbc.fill = GridBagConstraints.NONE;
        pathGbc.weightx = 0;
        pathGbc.gridx = 1;
        pathGbc.insets = new Insets(0, 0, 0, 0);
        pathPanel.add(browsePathButton, pathGbc);
        
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(pathPanel, gbc);
        
        urlRadio = createRadioButton("URL");
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(urlRadio, gbc);
        
        urlField = createTextField();
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 25, 0);
        mainPanel.add(urlField, gbc);
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(new Color(43, 43, 43));
        
        confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setBackground(new Color(76, 175, 80));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        confirmButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        confirmButton.addActionListener(e -> confirmSelection());
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setBackground(new Color(100, 100, 100));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        cancelButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> cancelSelection());
        
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.gridx = 0;
        btnGbc.gridy = 0;
        btnGbc.insets = new Insets(0, 0, 0, 15);
        buttonPanel.add(confirmButton, btnGbc);
        
        btnGbc.gridx = 1;
        btnGbc.insets = new Insets(0, 15, 0, 0);
        buttonPanel.add(cancelButton, btnGbc);
        
        gbc.gridy = 11;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(buttonPanel, gbc);
        
        group = new ButtonGroup();
        group.add(fileRadio);
        group.add(urlRadio);
        
        setupListeners();
        
        fileRadio.setSelected(true);
        pathField.setEnabled(true);
        pathField.setBackground(new Color(60, 63, 65));
        pathField.setForeground(Color.WHITE);
        urlField.setEnabled(false);
        
        add(mainPanel);
    }
    
    private JButton createBrowseButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }
    
    private void browseFileOrFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File or Folder");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            pathField.setText(path);
        }
    }
    
    private void chooseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Images (jpg, png, jpeg, gif, bmp, svg)", 
            "jpg", "jpeg", "png", "gif", "bmp", "svg"
        );
        fileChooser.setFileFilter(filter);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            imageField.setText(path);
        }
    }
    
    private void confirmSelection() {
        String selectedPath = null;
        String type = "";
        String customName = nameField.getText().trim();
        String customImage = imageField.getText().trim();
        
        if (fileRadio.isSelected()) {
            selectedPath = pathField.getText().trim();
            type = "file";
            
            if (selectedPath.isEmpty()) {
                showError("Please enter or select a file or folder path!");
                return;
            }
        } else if (urlRadio.isSelected()) {
            selectedPath = urlField.getText().trim();
            type = "url";
            
            if (selectedPath.isEmpty()) {
                showError("Please enter a URL!");
                return;
            }
            if (!selectedPath.matches("^(https?|ftp)://.*$")) {
                showError("Please enter a valid URL\n(must start with http://, https:// or ftp://)");
                return;
            }
        }
        
        confirmed = true;
        
        if (parentPanel != null) {
            parentPanel.setPath(selectedPath, type);
            if (!customName.isEmpty()) {
                parentPanel.setCustomName(customName);
            }
            if (!customImage.isEmpty()) {
                parentPanel.setCustomImage(customImage);
            }
        }
        
        dispose();
    }
    
    private void cancelSelection() {
        confirmed = false;
        removeParentPanel();
        dispose();
    }
    
    private void removeParentPanel() {
        if (parentPanel != null && parentPanel.getParent() != null) {
            java.awt.Container parent = parentPanel.getParent();
            parent.remove(parentPanel);
            parent.revalidate();
            parent.repaint();
        }
    }
    
    private void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(this,
            message,
            "Error",
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    private JRadioButton createRadioButton(String text) {
        JRadioButton radio = new JRadioButton(text);
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        radio.setBackground(new Color(43, 43, 43));
        radio.setForeground(new Color(220, 220, 220));
        radio.setFocusPainted(false);
        radio.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        radio.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return radio;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(60, 63, 65));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(66, 133, 244));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(66, 133, 244), 2),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
        
        return field;
    }
    
    private void setupListeners() {
        fileRadio.addActionListener(e -> {
            pathField.setEnabled(true);
            pathField.setBackground(new Color(60, 63, 65));
            pathField.setForeground(Color.WHITE);
            browsePathButton.setEnabled(true);
            browsePathButton.setBackground(new Color(66, 133, 244));
            
            urlField.setEnabled(false);
            urlField.setBackground(new Color(50, 53, 55));
            urlField.setForeground(new Color(150, 150, 150));
            
            pathField.requestFocus();
        });
        
        urlRadio.addActionListener(e -> {
            pathField.setEnabled(false);
            pathField.setBackground(new Color(50, 53, 55));
            pathField.setForeground(new Color(150, 150, 150));
            browsePathButton.setEnabled(false);
            browsePathButton.setBackground(new Color(80, 80, 80));
            
            urlField.setEnabled(true);
            urlField.setBackground(new Color(60, 63, 65));
            urlField.setForeground(Color.WHITE);
            
            urlField.requestFocus();
        });
        
        pathField.addActionListener(e -> confirmSelection());
        urlField.addActionListener(e -> confirmSelection());
        nameField.addActionListener(e -> confirmSelection());
    }
}