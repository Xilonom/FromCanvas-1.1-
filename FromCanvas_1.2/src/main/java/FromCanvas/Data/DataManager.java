package FromCanvas.Data;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import FromCanvas.GUI.EditorFrame;
import FromCanvas.Objects.Arrow;
import FromCanvas.Objects.DraggableImagePanel;
import FromCanvas.Objects.DraggableTextPanel;
import FromCanvas.Objects.PackagePanel;

public class DataManager {
    
    private static final String CANVAS_EXTENSION = ".fcnv";
    private static final String TEMPLATE_EXTENSION = ".fcnt";
    private static final int AUTO_POSITION_SENTINEL = 1462683;

    public static File loadedFile;

    // Метод для конвертации изображения в Base64 строку
    private static String imageToBase64(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Image not found: " + imagePath);
                return null;
            }
            
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                System.err.println("Failed to read image: " + imagePath);
                return null;
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = imagePath.substring(imagePath.lastIndexOf('.') + 1).toLowerCase();
            

            if (!format.equals("png") && !format.equals("jpg") && !format.equals("jpeg") && !format.equals("gif")) {
                format = "png"; 
            }
            
            ImageIO.write(image, format, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64String = Base64.getEncoder().encodeToString(imageBytes);
            

            return "data:image/" + format + ";base64," + base64String;
            
        } catch (IOException e) {
            System.err.println("Failed to convert image to Base64: " + e.getMessage());
            return null;
        }
    }
    

    private static String base64ToTempImage(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        
        try {

            String[] parts = base64String.split(",");
            if (parts.length != 2) {
                return null;
            }
            
            String metadata = parts[0];
            String base64Data = parts[1];
            

            String format = "png";
            if (metadata.contains("image/jpeg") || metadata.contains("image/jpg")) {
                format = "jpg";
            } else if (metadata.contains("image/gif")) {
                format = "gif";
            }
            

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            

            File tempFile = File.createTempFile("project_image_", "." + format);
            tempFile.deleteOnExit();
            

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            ImageIO.write(image, format, tempFile);
            
            return tempFile.getAbsolutePath();
            
        } catch (IOException e) {
            System.err.println("Failed to convert Base64 to image: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveTemplate(String type, String Name, Point pos, int width, int height, String text, Color color1, Color color2) {
        JSONArray objectListJson = new JSONArray();
        File selectedFile = new File("src\\main\\resources\\" + Name + TEMPLATE_EXTENSION);
        
        JSONObject item = new JSONObject();
        item.put("type", type);
        item.put("positionX", pos.getX());
        item.put("positionY", pos.getY());
        item.put("text", text);
        item.put("name", Name);
        item.put("pnl_height", height);
        item.put("pnl_Width", width);
        item.put("color1", String.valueOf(color1.getRGB()));
        item.put("color2", String.valueOf(color2.getRGB()));
        objectListJson.add(item);

        try (FileWriter file = new FileWriter(selectedFile)) {
            file.write(objectListJson.toJSONString());
            System.out.println("Successfully saved as " + selectedFile.getName());
        } catch (IOException e) {
            System.err.println("Failed to save template: " + e.getMessage());
        }
    }

    public static void loadTemplate(File file) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(file));
            JSONArray array = (JSONArray) obj;
            for (Object o : array) {
                JSONObject jsonObj = (JSONObject) o;
                JMenuItem tmplt = new JMenuItem(((String) jsonObj.get("name")));
                String Type = ((String) jsonObj.get("type"));
                String text = ((String) jsonObj.get("text"));
                Color color1 = new Color(Integer.parseInt(((String) jsonObj.get("color1"))), true);
                Color color2 = new Color(Integer.parseInt(((String) jsonObj.get("color2"))), true);

                if ("TxtPanel".equals(Type)) {
                    EditorFrame.AddNewJMenuItem(tmplt, text, color1, color2);
                }
                EditorFrame.AddTemplateItem(tmplt);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Failed to load template: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void save(ArrayList<DraggableTextPanel> panels, ArrayList<DraggableImagePanel> ImgPanels, ArrayList<Arrow> ArrowPanels, ArrayList<PackagePanel> PackagePanels) {
        if (loadedFile != null) { 
            JSONArray objectListJson = new JSONArray();
            
            for (DraggableTextPanel obj : panels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                item.put("positionX", obj.getPosX());
                item.put("positionY", obj.getPosY());
                item.put("text", obj.getText());
                item.put("pnl_height", obj.getPanelHeight());
                item.put("pnl_Width", obj.getPanelWidth());
                item.put("color1", String.valueOf(obj.getColor1().getRGB()));
                item.put("color2", String.valueOf(obj.getColor2().getRGB()));
                item.put("text_align",obj.getAlignment());
                objectListJson.add(item);
            }

            for (Arrow obj : ArrowPanels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                item.put("positionX_1", obj.getPosX_1());
                item.put("positionY_1", obj.getPosY_1());
                item.put("positionX_2", obj.getPosX_2());
                item.put("positionY_2", obj.getPosY_2());
                item.put("control_X", obj.getControlX());
                item.put("control_Y", obj.getControlY());
                item.put("Head_size", obj.getHead_size());
                item.put("color", String.valueOf(obj.getColor().getRGB()));
                objectListJson.add(item);
            }

            for (DraggableImagePanel obj : ImgPanels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                
                // Конвертируем изображение в Base64 и сохраняем
                String base64Image = imageToBase64(obj.getImg());
                if (base64Image != null) {
                    item.put("image_data", base64Image);
                } else {
                    item.put("image_data", "");
                }
                
                item.put("positionX", obj.getPosX());
                item.put("positionY", obj.getPosY());
                item.put("pnl_height", obj.getPanelHeight());
                item.put("pnl_Width", obj.getPanelWidth());
                item.put("color1", String.valueOf(obj.getColor1().getRGB()));
                item.put("color2", String.valueOf(obj.getColor2().getRGB()));
                objectListJson.add(item);
            }

            for (PackagePanel obj : PackagePanels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                item.put("positionX", obj.getPosX());
                item.put("positionY", obj.getPosY());
                item.put("pnl_height", obj.getPanelHeight());
                item.put("pnl_Width", obj.getPanelWidth());
                item.put("path_type", obj.getPathType());
                item.put("path", obj.getPath());
                item.put("Custom_name", obj.getCustomName());
                
                
                String customImgPath = obj.getCustomImagePath();
                if (customImgPath != null && !customImgPath.isEmpty()) {
                    String base64Image = imageToBase64(customImgPath);
                    if (base64Image != null) {
                        item.put("Custom_ImgPath_data", base64Image);
                    } else {
                        item.put("Custom_ImgPath_data", "");
                    }
                } else {
                    item.put("Custom_ImgPath_data", "");
                }
                
                objectListJson.add(item);
            }

            try (FileWriter file = new FileWriter(loadedFile)) {
                file.write(objectListJson.toJSONString());
                System.out.println("Successfully saved to " + loadedFile.getName());
            } catch (IOException e) {
                System.err.println("Failed to save file: " + e.getMessage());
            }
        } else {
            saveAs(panels, ImgPanels, ArrowPanels, PackagePanels); 
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveAs(ArrayList<DraggableTextPanel> panels, ArrayList<DraggableImagePanel> ImgPanels, ArrayList<Arrow> ArrowPanels, ArrayList<PackagePanel> PackagePanels) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null); 

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            if (!selectedFile.getName().endsWith(CANVAS_EXTENSION)) {
                selectedFile = new File(selectedFile.getPath() + CANVAS_EXTENSION);
            }

            loadedFile = selectedFile; 

            JSONArray objectListJson = new JSONArray();
            
            for (DraggableTextPanel obj : panels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                item.put("positionX", obj.getPosX());
                item.put("positionY", obj.getPosY());
                item.put("text", obj.getText());
                item.put("pnl_height", obj.getPanelHeight());
                item.put("pnl_Width", obj.getPanelWidth());
                item.put("color1", String.valueOf(obj.getColor1().getRGB()));
                item.put("color2", String.valueOf(obj.getColor2().getRGB()));
                item.put("text_align",obj.getAlignment());
                objectListJson.add(item);
            }

            for (Arrow obj : ArrowPanels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                item.put("positionX_1", obj.getPosX_1());
                item.put("positionY_1", obj.getPosY_1());
                item.put("positionX_2", obj.getPosX_2());
                item.put("positionY_2", obj.getPosY_2());
                item.put("control_X", obj.getControlX());
                item.put("control_Y", obj.getControlY());
                item.put("Head_size", obj.getHead_size());
                item.put("color", String.valueOf(obj.getColor().getRGB()));
                objectListJson.add(item);
            }

            for (DraggableImagePanel obj : ImgPanels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                
                // Конвертируем изображение в Base64 и сохраняем
                String base64Image = imageToBase64(obj.getImg());
                if (base64Image != null) {
                    item.put("image_data", base64Image);
                } else {
                    item.put("image_data", "");
                }
                
                item.put("positionX", obj.getPosX());
                item.put("positionY", obj.getPosY());
                item.put("pnl_height", obj.getPanelHeight());
                item.put("pnl_Width", obj.getPanelWidth());
                item.put("color1", String.valueOf(obj.getColor1().getRGB()));
                item.put("color2", String.valueOf(obj.getColor2().getRGB()));
                objectListJson.add(item);
            }

            for (PackagePanel obj : PackagePanels) {
                JSONObject item = new JSONObject();
                item.put("type", obj.getType());
                item.put("positionX", obj.getPosX());
                item.put("positionY", obj.getPosY());
                item.put("pnl_height", obj.getPanelHeight());
                item.put("pnl_Width", obj.getPanelWidth());
                item.put("path_type", obj.getPathType());
                item.put("path", obj.getPath());
                item.put("Custom_name", obj.getCustomName());
                
                // Конвертируем изображение PackagePanel в Base64
                String customImgPath = obj.getCustomImagePath();
                if (customImgPath != null && !customImgPath.isEmpty()) {
                    String base64Image = imageToBase64(customImgPath);
                    if (base64Image != null) {
                        item.put("Custom_ImgPath_data", base64Image);
                    } else {
                        item.put("Custom_ImgPath_data", "");
                    }
                } else {
                    item.put("Custom_ImgPath_data", "");
                }
                
                objectListJson.add(item);
            }

            try (FileWriter file = new FileWriter(loadedFile)) {
                file.write(objectListJson.toJSONString());
                System.out.println("Successfully saved as " + loadedFile.getName());
            } catch (IOException e) {
                System.err.println("Failed to save file: " + e.getMessage());
            }
        }
    }

    public static void load(Container contentPane, File file, ArrayList<DraggableTextPanel> pnlList, 
                           ArrayList<DraggableImagePanel> ImgPnlList, ArrayList<Arrow> ArrowPnlList, 
                           ArrayList<PackagePanel> PackagePnlList) {
        try {
            contentPane.removeAll();
            pnlList.clear();
            ImgPnlList.clear();
            ArrowPnlList.clear();
            EditorFrame.clearEditingState();
            loadedFile = file;
            
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(file));
            JSONArray array = (JSONArray) obj;

            for (Object o : array) {
                JSONObject jsonObj = (JSONObject) o;
                String Type = ((String) jsonObj.get("type"));

                if ("TxtPanel".equals(Type)) {
                    String text = ((String) jsonObj.get("text"));
                    long posX = ((Number) jsonObj.get("positionX")).longValue(); 
                    long posY = ((Number) jsonObj.get("positionY")).longValue();
                    Color color1 = new Color(Integer.parseInt(((String) jsonObj.get("color1"))), true);
                    Color color2 = new Color(Integer.parseInt(((String) jsonObj.get("color2"))), true);
                    long height = ((Number) jsonObj.get("pnl_height")).longValue(); 
                    long width = ((Number) jsonObj.get("pnl_Width")).longValue();
                    long txt_align = ((Number) jsonObj.get("text_align")).longValue();

                    boolean isAutoPosition = (int) posX == AUTO_POSITION_SENTINEL || (int) posY == AUTO_POSITION_SENTINEL;
                    DraggableTextPanel panel = new DraggableTextPanel(
                        isAutoPosition ? AUTO_POSITION_SENTINEL : (int) posX,
                        isAutoPosition ? AUTO_POSITION_SENTINEL : (int) posY,
                        text, color1, color2, (int)txt_align
                    );
                    panel.setPanelSize((int) width, (int) height);
                    contentPane.add(panel);
                    pnlList.add(panel);
                }
                else if ("ImgPanel".equals(Type)) {

                    String imageData = (String) jsonObj.get("image_data");
                    String tempImagePath = null;
                    
                    if (imageData != null && !imageData.isEmpty()) {
                        tempImagePath = base64ToTempImage(imageData);
                    }
                    
                    long posX = ((Number) jsonObj.get("positionX")).longValue(); 
                    long posY = ((Number) jsonObj.get("positionY")).longValue();
                    Color color1 = new Color(Integer.parseInt(((String) jsonObj.get("color1"))), true);
                    Color color2 = new Color(Integer.parseInt(((String) jsonObj.get("color2"))), true);
                    long height = ((Number) jsonObj.get("pnl_height")).longValue(); 
                    long width = ((Number) jsonObj.get("pnl_Width")).longValue(); 

                    String imagePath = tempImagePath != null ? tempImagePath : "";
                    DraggableImagePanel panel = new DraggableImagePanel((int) posX, (int) posY, imagePath, color1, color2, (int) width, (int) height);
                    
                   
                    panel.setStoredImageData(imageData);
                    
                    contentPane.add(panel);
                    ImgPnlList.add(panel);
                }
                else if ("Arrow".equals(Type)) {
                    long posX_1 = ((Number) jsonObj.get("positionX_1")).longValue(); 
                    long posY_1 = ((Number) jsonObj.get("positionY_1")).longValue();
                    long posX_2 = ((Number) jsonObj.get("positionX_2")).longValue(); 
                    long posY_2 = ((Number) jsonObj.get("positionY_2")).longValue();
                    long cntrlX = ((Number) jsonObj.get("control_X")).longValue();
                    long cntrlY = ((Number) jsonObj.get("control_Y")).longValue();
                    Color color = new Color(Integer.parseInt(((String) jsonObj.get("color"))), true);
                    long Head_size = ((Number) jsonObj.get("Head_size")).longValue();

                    Arrow arrow = new Arrow((int) posX_1, (int) posY_1, (int) posX_2, (int) posY_2, new Point((int) cntrlX, (int) cntrlY), color, (int) Head_size);
                    contentPane.add(arrow);
                    ArrowPnlList.add(arrow);
                }
                else if ("PackagePnl".equals(Type)) {
                    long posX = ((Number) jsonObj.get("positionX")).longValue(); 
                    long posY = ((Number) jsonObj.get("positionY")).longValue();
                    long height = ((Number) jsonObj.get("pnl_height")).longValue(); 
                    long width = ((Number) jsonObj.get("pnl_Width")).longValue();
                    String path = ((String) jsonObj.get("path"));
                    String pathType = ((String) jsonObj.get("path_type"));
                    String CustomName = ((String) jsonObj.get("Custom_name"));
                    
                    String imageData = (String) jsonObj.get("Custom_ImgPath_data");
                    String tempImagePath = null;
                    
                    if (imageData != null && !imageData.isEmpty()) {
                        tempImagePath = base64ToTempImage(imageData);
                    }
                    
                    String customImgPath = tempImagePath != null ? tempImagePath : "";

                    PackagePanel packagePanel = new PackagePanel((int) posX, (int) posY, (int) width, (int) height, path, pathType, CustomName, customImgPath);
                    contentPane.add(packagePanel);
                    PackagePnlList.add(packagePanel);
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Failed to load file: " + e.getMessage());
        }
    }
}