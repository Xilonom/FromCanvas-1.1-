package FromCanvas;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;

import FromCanvas.Customization.TemplateMenu;
import FromCanvas.Data.DataManager;
import FromCanvas.GUI.EditorFrame;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                
                UIManager.setLookAndFeel(new FlatDarculaLaf());
            } catch (Exception e) {
                System.err.println("Ошибка при установке FlatLaf: " + e.getMessage());
            }

            EditorFrame frame = new EditorFrame();
            
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            

            frame.setVisible(true);
            File dir = new File(TemplateMenu.RESOURCES_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (int i = files.length - 1; i >= 0; i--) {
                if (files[i].isFile()) {
                    DataManager.loadTemplate(files[i]);
                }
            }
        });
    }
}
