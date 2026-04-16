package FromCanvas.Customization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class TemplateMenu extends JFrame {

    public static final String RESOURCES_DIR = "src/main/resources/";
    private DefaultListModel<String> listModel;
    private JList<String> fileList;
    private final Set<String> listedFiles = new HashSet<>();

    public TemplateMenu() {
        setTitle("Template");
        setSize(300, 400);
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        setVisible(true);
        setResizable(false);

        listModel = new DefaultListModel<>();

        fileList = new JList<>(listModel);
        fileList.setBackground(Color.DARK_GRAY);
        fileList.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(fileList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);

        JButton addButton = new JButton("Добавить файлы");
        addButton.setBackground(Color.GRAY);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addFiles());

        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadFilesFromResources(); 
    }

    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();

            for (File file : selectedFiles) {
                try {
                    Path targetPath = Paths.get(RESOURCES_DIR, file.getName());
                    Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    addFileToList(file.getName());
                    
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Ошибка при копировании файла: " + file.getName() + "\n" + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }


    private void loadFilesFromResources() {
        File dir = new File(RESOURCES_DIR);


        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.isDirectory()) {
            listModel.addElement("Ошибка: путь не является директорией");
            return;
        }

        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            listModel.addElement("В папке нет файлов");
            return;
        }


        for (int i = files.length - 1; i >= 0; i--) {
            if (files[i].isFile()) {
                addFileToList(files[i].getName());
            }

        }

        
    }

    private void addFileToList(String fileName) {
        if (listedFiles.add(fileName)) {
            listModel.addElement(fileName);
        }
    }

}
