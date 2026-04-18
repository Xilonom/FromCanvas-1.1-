package FromCanvas.Customization;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.Random;

import javax.swing.*;
import javax.swing.event.*;

import FromCanvas.Data.DataManager;
import FromCanvas.Objects.DraggableTextPanel;

public class PanelCustomization extends JFrame {
    
    private JPanel previewPanel;
    private JTextArea previewTextArea;
    private Color panelColor1 = new Color(24, 24, 26);
    private Color panelColor2 = new Color(24, 24, 26);
    private JTextField nameField;
    private JTextField rField, gField, bField;
    private JTextField r2Field, g2Field, b2Field;
    private JTextField txtRField, txtGField, txtBField;
    private JTextField widthField, heightField;
    private JTextField posXField, posYField;
    private JRadioButton centerAlign, rightMiddleAlign, rightTopAlign;
    private JComboBox<String> layerCombo;
    private JCheckBox manualPositionCheckBox;
    private JLabel posXLabel, posYLabel;
    
    private final Random random = new Random();
    private boolean useManualPosition = false;

    public PanelCustomization() {
        setTitle("Panel Settings");
        setSize(550, 720);
        setBackground(Color.BLACK);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 30, 30), 0, getHeight(), new Color(10, 10, 10));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.DARK_GRAY);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBounds(20, 20, 510, 680);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        previewPanel = createPreviewPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(previewPanel, gbc);

        JLabel nameLabel = createStyledLabel("Panel Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(nameLabel, gbc);

        nameField = createStyledTextField("My Panel", 15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        contentPanel.add(nameField, gbc);

        JLabel posLabel = createStyledLabel("Position:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        contentPanel.add(posLabel, gbc);

        JPanel posControlPanel = new JPanel(new GridBagLayout());
        posControlPanel.setOpaque(false);
        
        GridBagConstraints posGbc = new GridBagConstraints();
        posGbc.insets = new Insets(0, 2, 0, 2);
        posGbc.fill = GridBagConstraints.HORIZONTAL;

        manualPositionCheckBox = new JCheckBox("Set manually");
        manualPositionCheckBox.setForeground(new Color(200, 200, 210));
        manualPositionCheckBox.setBackground(new Color(60, 60, 70));
        manualPositionCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        manualPositionCheckBox.setOpaque(false);
        
        posGbc.gridx = 0;
        posGbc.gridy = 0;
        posGbc.gridwidth = 3;
        posGbc.anchor = GridBagConstraints.WEST;
        posControlPanel.add(manualPositionCheckBox, posGbc);

        JPanel posInputPanel = new JPanel();
        posInputPanel.setOpaque(false);
        
        posXField = createStyledNumberField("100", 4);
        posYField = createStyledNumberField("100", 4);
        
        posXField.setEnabled(false);
        posYField.setEnabled(false);
        
        posXLabel = createStyledSmallLabel("Auto");
        posYLabel = createStyledSmallLabel("Auto");
        
        posInputPanel.add(createStyledSmallLabel("X:"));
        posInputPanel.add(posXField);
        posInputPanel.add(posXLabel);
        posInputPanel.add(createStyledSmallLabel("Y:"));
        posInputPanel.add(posYField);
        posInputPanel.add(posYLabel);
        
        posGbc.gridx = 0;
        posGbc.gridy = 1;
        posGbc.gridwidth = 3;
        posGbc.anchor = GridBagConstraints.CENTER;
        posControlPanel.add(posInputPanel, posGbc);

        JButton randomizeBtn = createStyledButton("Random", new Color(70, 70, 90));
        randomizeBtn.setPreferredSize(new Dimension(80, 25));
        randomizeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        randomizeBtn.addActionListener(e -> randomizePosition());
        
        posGbc.gridx = 3;
        posGbc.gridy = 1;
        posGbc.gridwidth = 1;
        posControlPanel.add(randomizeBtn, posGbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        contentPanel.add(posControlPanel, gbc);

        manualPositionCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                useManualPosition = (e.getStateChange() == ItemEvent.SELECTED);
                posXField.setEnabled(useManualPosition);
                posYField.setEnabled(useManualPosition);
                
                if (useManualPosition) {
                    posXLabel.setText("");
                    posYLabel.setText("");
                    if (posXField.getText().isEmpty()) posXField.setText("100");
                    if (posYField.getText().isEmpty()) posYField.setText("100");
                } else {
                    posXLabel.setText("Auto");
                    posYLabel.setText("Auto");
                    posXField.setText("");
                    posYField.setText("");
                }
            }
        });

        JLabel sizeLabel = createStyledLabel("Size (W, H):");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        contentPanel.add(sizeLabel, gbc);

        JPanel sizePanel = new JPanel();
        sizePanel.setOpaque(false);
        widthField = createStyledNumberField("180", 4);
        heightField = createStyledNumberField("180", 4);
        sizePanel.add(createStyledSmallLabel("W:"));
        sizePanel.add(widthField);
        sizePanel.add(createStyledSmallLabel("H:"));
        sizePanel.add(heightField);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        contentPanel.add(sizePanel, gbc);

        JLabel layerLabel = createStyledLabel("Layer:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        contentPanel.add(layerLabel, gbc);

        String[] layers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        layerCombo = new JComboBox<>(layers);
        layerCombo.setSelectedIndex(0);
        layerCombo.setBackground(new Color(60, 60, 70));
        layerCombo.setForeground(new Color(230, 230, 240));
        layerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        contentPanel.add(layerCombo, gbc);

        JLabel bgColorLabel = createStyledLabel("Gradient Color 1:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        contentPanel.add(bgColorLabel, gbc);

        JPanel bgColorPanel = createColorInputPanel(true, "70", "130", "180");
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        contentPanel.add(bgColorPanel, gbc);

        JLabel bgColor2Label = createStyledLabel("Gradient Color 2:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        contentPanel.add(bgColor2Label, gbc);

        JPanel bgColor2Panel = createColorInputPanel(false, "25", "25", "112");
        gbc.gridx = 1;
        gbc.gridy = 6;
        contentPanel.add(bgColor2Panel, gbc);

        JLabel textColorLabel = createStyledLabel("Text Color:");
        gbc.gridx = 0;
        gbc.gridy = 7;
        contentPanel.add(textColorLabel, gbc);

        JPanel textColorPanel = createTextColorInputPanel("228", "228", "230");
        gbc.gridx = 1;
        gbc.gridy = 7;
        contentPanel.add(textColorPanel, gbc);

        JLabel alignLabel = createStyledLabel("Text Alignment:");
        gbc.gridx = 0;
        gbc.gridy = 8;
        contentPanel.add(alignLabel, gbc);

        JPanel alignPanel = createAlignmentPanel();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        contentPanel.add(alignPanel, gbc);

        JLabel textLabel = createStyledLabel("Preview Text:");
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        contentPanel.add(textLabel, gbc);

        JTextField textField = createStyledTextField("Sample Text", 15);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreviewText(); }
            public void removeUpdate(DocumentEvent e) { updatePreviewText(); }
            public void changedUpdate(DocumentEvent e) { updatePreviewText(); }
            private void updatePreviewText() {
                previewTextArea.setText(textField.getText());
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        contentPanel.add(textField, gbc);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        
        JButton confirmBtn = createStyledButton("Create Panel", new Color(46, 125, 50));
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndSavePanel();
                dispose();
            }
        });

        JButton cancelBtn = createStyledButton("Cancel", new Color(198, 40, 40));
        cancelBtn.addActionListener(e -> dispose());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        buttonPanel.add(confirmBtn, gbc);
        
        gbc.gridx = 1;
        buttonPanel.add(cancelBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        contentPanel.add(buttonPanel, gbc);

        mainPanel.add(contentPanel);
        add(mainPanel);

        setupListeners();
        randomizePosition();
    }

    private JPanel createPreviewPanel() {
    JPanel panel = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = DraggableTextPanel.DEFAULT_WIDTH;
            int h = DraggableTextPanel.DEFAULT_HEIGHT;
            
            RoundRectangle2D rect = new RoundRectangle2D.Double(2, 2, w - 4, h - 4, 20, 20);
            GradientPaint gradient = new GradientPaint(0, 0, panelColor1, w, h, panelColor2);
            
            g2.setPaint(gradient);
            g2.fill(rect);
            
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(255, 255, 255, 50));
            g2.draw(rect);
            
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(0, 0, 0, 40));
            RoundRectangle2D innerRect = new RoundRectangle2D.Double(3, 3, w - 6, h - 6, 18, 18);
            g2.draw(innerRect);
            
            g2.dispose();
        }
    };
    
    panel.setPreferredSize(new Dimension(180, 180));
    panel.setMinimumSize(new Dimension(180, 180));
    panel.setMaximumSize(new Dimension(180, 180));
    panel.setOpaque(false);
    
    previewTextArea = new JTextArea("Sample Text");
    previewTextArea.setOpaque(false);
    previewTextArea.setForeground(new Color(228, 228, 230));
    previewTextArea.setLineWrap(true);
    previewTextArea.setWrapStyleWord(true);
    previewTextArea.setEditable(false);
    previewTextArea.setFocusable(false);
    
    int padding = panel.getPreferredSize().width / 10;
    previewTextArea.setBounds(padding, padding, 
                              panel.getPreferredSize().width - 2 * padding, 
                              panel.getPreferredSize().height - 2 * padding);
    
    float fontSize = panel.getPreferredSize().width / 12f;
    previewTextArea.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(12, (int)fontSize)));
    
    panel.add(previewTextArea);
    
    return panel;
}

    private void randomizePosition() {
        int x = 50 + random.nextInt(401);
        int y = 50 + random.nextInt(401);
        
        if (useManualPosition) {
            posXField.setText(String.valueOf(x));
            posYField.setText(String.valueOf(y));
        } else {
            posXLabel.setText(String.valueOf(x));
            posYLabel.setText(String.valueOf(y));
        }
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(200, 200, 210));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private JTextField createStyledTextField(String defaultValue, int columns) {
        JTextField field = new JTextField(defaultValue, columns);
        field.setBackground(new Color(60, 60, 70));
        field.setForeground(new Color(230, 230, 240));
        field.setCaretColor(new Color(230, 230, 240));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return field;
    }

    private JTextField createStyledNumberField(String defaultValue, int columns) {
        JTextField field = new JTextField(defaultValue, columns);
        field.setBackground(new Color(45, 45, 55));
        field.setForeground(new Color(100, 200, 255));
        field.setCaretColor(new Color(230, 230, 240));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 85)),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(new Font("Consolas", Font.BOLD, 12));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateAndFixNumber(field, 0, 1000, 100);
            }
        });
        
        return field;
    }

    private void validateAndFixNumber(JTextField field, int min, int max, int defaultValue) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            field.setText(String.valueOf(defaultValue));
            return;
        }
        
        try {
            int value = Integer.parseInt(text);
            value = Math.max(min, Math.min(max, value));
            field.setText(String.valueOf(value));
        } catch (NumberFormatException ex) {
            field.setText(String.valueOf(defaultValue));
        }
    }

    private JPanel createColorInputPanel(boolean isFirst, String rDefault, String gDefault, String bDefault) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        JTextField rField = new JTextField(rDefault, 3);
        JTextField gField = new JTextField(gDefault, 3);
        JTextField bField = new JTextField(bDefault, 3);
        
        styleColorField(rField);
        styleColorField(gField);
        styleColorField(bField);
        
        if (isFirst) {
            this.rField = rField;
            this.gField = gField;
            this.bField = bField;
        } else {
            this.r2Field = rField;
            this.g2Field = gField;
            this.b2Field = bField;
        }
        
        addNumberValidation(rField, 0, 255, Integer.parseInt(rDefault));
        addNumberValidation(gField, 0, 255, Integer.parseInt(gDefault));
        addNumberValidation(bField, 0, 255, Integer.parseInt(bDefault));
        
        panel.add(createStyledSmallLabel("R:"));
        panel.add(rField);
        panel.add(createStyledSmallLabel("G:"));
        panel.add(gField);
        panel.add(createStyledSmallLabel("B:"));
        panel.add(bField);
        
        return panel;
    }

    private JPanel createTextColorInputPanel(String rDefault, String gDefault, String bDefault) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        txtRField = new JTextField(rDefault, 3);
        txtGField = new JTextField(gDefault, 3);
        txtBField = new JTextField(bDefault, 3);
        
        styleColorField(txtRField);
        styleColorField(txtGField);
        styleColorField(txtBField);
        
        addNumberValidation(txtRField, 0, 255, Integer.parseInt(rDefault));
        addNumberValidation(txtGField, 0, 255, Integer.parseInt(gDefault));
        addNumberValidation(txtBField, 0, 255, Integer.parseInt(bDefault));
        
        panel.add(createStyledSmallLabel("R:"));
        panel.add(txtRField);
        panel.add(createStyledSmallLabel("G:"));
        panel.add(txtGField);
        panel.add(createStyledSmallLabel("B:"));
        panel.add(txtBField);
        
        return panel;
    }

    private void addNumberValidation(JTextField field, int min, int max, int defaultValue) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateInput(); }
            public void removeUpdate(DocumentEvent e) { validateInput(); }
            public void changedUpdate(DocumentEvent e) { validateInput(); }
            
            private void validateInput() {
                String text = field.getText();
                if (text.isEmpty()) {
                    return;
                }
                
                try {
                    int value = Integer.parseInt(text);
                    if (value < min || value > max) {
                        value = Math.max(min, Math.min(max, value));
                        field.setText(String.valueOf(value));
                    }
                } catch (NumberFormatException ex) {
                    field.setText(String.valueOf(defaultValue));
                }
            }
        });
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = field.getText();
                if (text.isEmpty()) {
                    field.setText(String.valueOf(defaultValue));
                } else {
                    try {
                        int value = Integer.parseInt(text);
                        value = Math.max(min, Math.min(max, value));
                        field.setText(String.valueOf(value));
                    } catch (NumberFormatException ex) {
                        field.setText(String.valueOf(defaultValue));
                    }
                }
            }
        });
    }

    private void styleColorField(JTextField field) {
        field.setBackground(new Color(45, 45, 55));
        field.setForeground(new Color(100, 200, 255));
        field.setCaretColor(new Color(230, 230, 240));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 85)),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(new Font("Consolas", Font.BOLD, 12));
    }

    private JLabel createStyledSmallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(150, 150, 160));
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return label;
    }

    private JPanel createAlignmentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        centerAlign = new JRadioButton("Center");
        rightMiddleAlign = new JRadioButton("Right-Middle");
        rightTopAlign = new JRadioButton("Right-Top");
        
        centerAlign.setSelected(true);
        
        centerAlign.setForeground(new Color(200, 200, 210));
        rightMiddleAlign.setForeground(new Color(200, 200, 210));
        rightTopAlign.setForeground(new Color(200, 200, 210));
        
        centerAlign.setBackground(new Color(60, 60, 70));
        rightMiddleAlign.setBackground(new Color(60, 60, 70));
        rightTopAlign.setBackground(new Color(60, 60, 70));
        
        centerAlign.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightMiddleAlign.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightTopAlign.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        ButtonGroup group = new ButtonGroup();
        group.add(centerAlign);
        group.add(rightMiddleAlign);
        group.add(rightTopAlign);
        

        
        panel.add(centerAlign);
        panel.add(rightMiddleAlign);
        panel.add(rightTopAlign);
        
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        
        return button;
    }

    private void setupListeners() {
        DocumentListener bgListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateColors(); }
            public void removeUpdate(DocumentEvent e) { updateColors(); }
            public void changedUpdate(DocumentEvent e) { updateColors(); }
            
            private void updateColors() {
                try {
                    int r = getIntValue(rField, 70);
                    int g = getIntValue(gField, 130);
                    int b = getIntValue(bField, 180);
                    
                    panelColor1 = new Color(r, g, b);
                    previewPanel.repaint();
                } catch (NumberFormatException ignored) {}
            }
        };

        DocumentListener bg2Listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateColors(); }
            public void removeUpdate(DocumentEvent e) { updateColors(); }
            public void changedUpdate(DocumentEvent e) { updateColors(); }
            
            private void updateColors() {
                try {
                    int r = getIntValue(r2Field, 25);
                    int g = getIntValue(g2Field, 25);
                    int b = getIntValue(b2Field, 112);
                    
                    panelColor2 = new Color(r, g, b);
                    previewPanel.repaint();
                } catch (NumberFormatException ignored) {}
            }
        };

        DocumentListener textListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTextColor(); }
            public void removeUpdate(DocumentEvent e) { updateTextColor(); }
            public void changedUpdate(DocumentEvent e) { updateTextColor(); }
            
            private void updateTextColor() {
                try {
                    int r = getIntValue(txtRField, 228);
                    int g = getIntValue(txtGField, 228);
                    int b = getIntValue(txtBField, 230);
                    
                    previewTextArea.setForeground(new Color(r, g, b));
                } catch (NumberFormatException ignored) {}
            }
        };

        if (rField != null) {
            rField.getDocument().addDocumentListener(bgListener);
            gField.getDocument().addDocumentListener(bgListener);
            bField.getDocument().addDocumentListener(bgListener);
        }

        if (r2Field != null) {
            r2Field.getDocument().addDocumentListener(bg2Listener);
            g2Field.getDocument().addDocumentListener(bg2Listener);
            b2Field.getDocument().addDocumentListener(bg2Listener);
        }

        if (txtRField != null) {
            txtRField.getDocument().addDocumentListener(textListener);
            txtGField.getDocument().addDocumentListener(textListener);
            txtBField.getDocument().addDocumentListener(textListener);
        }
    }

    private int getIntValue(JTextField field, int defaultValue) {
        String text = field.getText();
        if (text.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void createAndSavePanel() {
        int x, y;
        
        if (useManualPosition) {
            x = getIntValue(posXField, 100);
            y = getIntValue(posYField, 100);
        } else {
            x = Integer.parseInt(posXLabel.getText());
            y = Integer.parseInt(posYLabel.getText());
        }
        
        int w = getIntValue(widthField, 180);
        int h = getIntValue(heightField, 180);
        
        w = Math.max(80, Math.min(600, w));
        h = Math.max(80, Math.min(600, h));
        
        if (useManualPosition) {
                DataManager.saveTemplate("TxtPanel", 
                                    nameField.getText(), 
                                    new Point(x, y),
                                    w, h,
                                    previewTextArea.getText(), 
                                    panelColor1, 
                                    panelColor2
                                    );
                        }
        else {
            DataManager.saveTemplate("TxtPanel", 
                                    nameField.getText(), 
                                    new Point(1462683,1462683),
                                    w, h,
                                    previewTextArea.getText(), 
                                    panelColor1, 
                                    panelColor2
                                    );
        }
        
        File dir = new File(TemplateMenu.RESOURCES_DIR);
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = files.length - 1; i >= 0; i--) {
                if (files[i].isFile()) {
                    DataManager.loadTemplate(files[i]);
                }
            }
        }
    }
}