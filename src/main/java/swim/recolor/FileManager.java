package swim.recolor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.swing.SwingWorker;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.zeroturnaround.zip.ZipUtil;

public class FileManager extends SwingWorker<Void, Void> {

    private static HashMap<String, String> packFiles = new HashMap<String, String>();
    public static HashMap<String, String> packDirs = new HashMap<String, String>();
    public static String packDir = "none";
    public static boolean hideAllFiles = false;
    public static int count = 0;

    @Override
    protected Void doInBackground() throws Exception {
        handlePack(Window.publicPack);
        return null;
    }

    public static boolean legalFileName(String filename) {
        final String REGEX_PATTERN = "^[\\w\\-. ]+$";
        if (filename == null) {
            return false;
        }
        return filename.matches(REGEX_PATTERN);
    }

    private static void countFiles(String dirPath) {
        File f = new File(dirPath);
        ArrayList<String> allowedDirs = PackBuilder.getDirs();
        File[] files = f.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    countFiles(file.getAbsolutePath());
                } else if (FilenameUtils.getExtension(file.getAbsolutePath().toLowerCase()).equals("png")) {
                    String parentFolderName = FilenameUtils.getName(file.getParent()).toLowerCase();
                    if (allowedDirs.contains(parentFolderName) || hideAllFiles) {
                        setCount(getCount() + 1);
                    }
                }
            }
        }
    }

    public static void handlePack(File pack) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        setCount(0);
        LoadingWindow.showLoadingWindow();
        LoadingWindow.loadingLabel.setVisible(true);
        Window.clearPreviewImage();
        Window.clearButtons();
        PackBuilder.createAllowedDirs(); // pngs to load from
        Recolor.recolorFiles = new HashMap<String, BufferedImage>(); // clear it
        setPackFiles(new HashMap<String, String>()); // clear it
        packDirs = new HashMap<String, String>(); // clear it
        Window.buttons = new HashMap<String, JButton>(); // clear it
        Recolor.isMCPE = false; // default
        String extension = FilenameUtils.getExtension(pack.getAbsolutePath().toLowerCase());
        File tempDir = new File(System.getenv("APPDATA") + "\\swim_recolor");
        createTempDir(tempDir);
        // now that we have the temp dir copy the pack to it
        try {
            File newPack = new File(tempDir.getAbsolutePath() + "\\" + pack.getName());
            // if its a zip or mcpack, unpack it
            if (extension.equals("zip") || extension.equals("mcpack")) {
                FileUtils.copyFile(pack, newPack);
                File dest = new File(newPack.getParentFile() + "\\" + FilenameUtils.removeExtension(newPack.getName()));
                ZipUtil.unpack(newPack, dest);
                FileUtils.forceDelete(newPack); // dont need the archive of it anymore
                preProcessFiles(dest);
            } else if (pack.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(pack, newPack.getParentFile());
                File dest = new File(newPack.getParentFile() + "\\" + FilenameUtils.getName(newPack.getAbsolutePath()));
                preProcessFiles(dest);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to Handle Pack", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to Move Pack to Temp Directory", "Error!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        LoadingWindow.closeLoadingWindow();
    }

    private static void preProcessFiles(File dest) {
        countFiles(dest.getPath());
        LoadingWindow.setValue(0);
        LoadingWindow.setMaximum(count);
        LoadingWindow.loadingLabel.setVisible(false);
        processFiles(dest);
        Window.refreshDirFilterList();
    }

    public static BufferedImage imgo;

    public static void processFiles(File pack) {
        try {
            ArrayList<String> allowedDirs = PackBuilder.getDirs();
            File[] directoryListing = pack.listFiles();
            if (directoryListing != null) {
                for (File file : directoryListing) {
                    if (file.isDirectory()) {
                        processFiles(file);
                    } else if (FilenameUtils.getExtension(file.getAbsolutePath().toLowerCase()).equals("png")) {
                        String parentFolderName = FilenameUtils.getName(file.getParent()).toLowerCase();
                        String base = FilenameUtils.getBaseName(file.getAbsolutePath());
                        if (allowedDirs.contains(parentFolderName) || base.equals("pack") || base.equals("pack_icon") || hideAllFiles) {
                            LoadingWindow.updateProgress();
                            try { // fix background
                                BufferedImage img = ImageIO.read(file);
                                BufferedImage fixedImg = Recolor.transparencyFix(img);
                                ImageIO.write(fixedImg, "png", file);
                                LoadingWindow.setLoadingPreview(fixedImg);
                                imgo = fixedImg;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            getPackFiles().put(file.getAbsolutePath(), file.getName());
                            Window.addButton(file.getAbsolutePath(), packFiles.get(file.getAbsolutePath()));
                            File parent = file.getParentFile();
                            if (!packDirs.containsKey(parent.getAbsolutePath())) {
                                packDirs.put(parent.getAbsolutePath(), parent.getName());
                            }
                        }
                    } else if (file.getName().toLowerCase().equals("manifest.json")) {
                        packDir = file.getParentFile().getAbsolutePath();
                        Recolor.isMCPE = true;
                        System.out.println("is mcpe pack");
                    } else if (file.getName().toLowerCase().equals("pack.mcmeta")) {
                        packDir = file.getParentFile().getAbsolutePath();
                        System.out.println(packDir);
                        Recolor.isMCPE = false;
                        System.out.println("is not an mcpe pack");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renderFiles(HashMap<String, String> packFiles) {
        for (String i : packFiles.keySet()) {
            Window.addButton(i, packFiles.get(i));
        }
    }

    public static void modifyButtonsFromConfig(ArrayList<String> namesList)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        LoadingWindow.showLoadingWindow();
        LoadingWindow.setValue(0);
        LoadingWindow.loadingLabel.setVisible(true);
        int c = 0;
        for (Entry<String, JButton> entry : Window.buttons.entrySet()) {
            if (namesList.contains(FilenameUtils.getBaseName(entry.getKey()))) {
                c++;
            }
        }
        LoadingWindow.setMaximum(c);
        int width = 64;
        int height = 64;
        int mode = Window.recolorMethod.getSelectedIndex();
        LoadingWindow.loadingLabel.setVisible(false);
        for (Entry<String, JButton> entry : Window.buttons.entrySet()) {
            String path = entry.getKey();
            JButton button = entry.getValue();
            if (namesList.contains(FilenameUtils.getBaseName(path))) {
                if (mode == 1) { // for tint (only works good for dark images that need to be brute force recolored)
                    LoadingWindow.updateProgress();
                    BufferedImage recoloredPreview = Recolor.tint(ImageIO.read(new File(path)), Recolor.color);
                    recoloredPreview = Recolor.transparencyFix(recoloredPreview);
                    Image imageScale = recoloredPreview.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    ImageIcon iconImage = new ImageIcon(imageScale);
                    button.setIcon(iconImage);
                    LoadingWindow.setLoadingPreview(recoloredPreview);
                    Recolor.recolorFiles.put(path, recoloredPreview);
                } else if (mode == 0) { // for hueshift (superior method for pretty much any color)
                    LoadingWindow.updateProgress();
                    int red = Recolor.color.getRed();
                    int green = Recolor.color.getGreen();
                    int blue = Recolor.color.getBlue();
                    BufferedImage recoloredPreview = Recolor.hueShift(ImageIO.read(new File(path)), Recolor.getHue(red, green, blue));
                    recoloredPreview = Recolor.transparencyFix(recoloredPreview);
                    Image imageScale = recoloredPreview.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    ImageIcon iconImage = new ImageIcon(imageScale);
                    button.setIcon(iconImage);
                    LoadingWindow.setLoadingPreview(recoloredPreview);
                    Recolor.recolorFiles.put(path, recoloredPreview);
                }
                // refresh render after image icon modification
                button.setVisible(false);
                button.setVisible(true);
                button.revalidate();
            }
        }
        // refresh whole pannel as well
        Window.buttonPanel.setVisible(false);
        Window.buttonPanel.setVisible(true);
        Window.buttonPanel.revalidate();
        LoadingWindow.closeLoadingWindow(); // don't forget to close
    }

    public static void clearTempDir(File tempDir) {
        for (File file : tempDir.listFiles()) {
            try {
                if (!file.getName().equals("configs")) {
                    FileUtils.forceDelete(file);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static File configPath; // where config files are stored

    private static void createConfigsDir(File dirPath) {
        File configDir = new File(dirPath.getAbsolutePath() + "\\configs");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        configPath = configDir;
        loadExportPath(configDir);
        createDefaultConfig();
    }

    public static void createDefaultConfig() {
        try {
            File defaultConfig = new File(configPath.getAbsolutePath() + "\\default.txt");
            if (!defaultConfig.exists()) {
                FileWriter writer = new FileWriter(defaultConfig);
                String[] defaults = DefaultFiles.getDefaultFileNameArray();
                for (String str : defaults) {
                    writer.write(str + "\n");
                }
                writer.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void createTempDir(File tempDir) {
        if (!tempDir.exists()) { // first check if temp dir exists
            tempDir.mkdirs();
        } else {
            clearTempDir(tempDir); // if it does exist make sure to clear all files inside it, if any
        }
        createConfigsDir(tempDir);
    }

    public static File exportPath; // where packs get exported to

    private static void loadExportPath(File configDir) {
        File exportConfig = new File(configDir + "\\export.config");
        if (exportConfig.exists()) {
            readExportPath(exportConfig);
        } else {
            createExportPath(exportConfig);
        }
    }

    private static void createExportPath(File exportConfig) {
        try {
            FileWriter myWriter = new FileWriter(exportConfig);
            String home = System.getProperty("user.home") + "\\Downloads";
            myWriter.write(home);
            myWriter.close();
            exportPath = new File(System.getProperty("user.home") + "\\Downloads");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readExportPath(File exportConfig) {
        try {
            Scanner reader = new Scanner(exportConfig);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if (new File(data).exists()) {
                    exportPath = new File(data);
                    reader.close();
                    return;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void updateExportConfig(String newExportPath) throws IOException {
        try {
            File config = new File(System.getenv("APPDATA") + "\\swim_recolor\\configs\\export.config");
            if (config.exists()) {
                FileWriter fw = new FileWriter(config, false);
                PrintWriter pw = new PrintWriter(fw, false);
                pw.flush();
                fw.write(newExportPath);
                pw.close();
                fw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkIfImageSelectedForRecolor(HashMap<String, BufferedImage> map, String key) {
        Iterator<Entry<String, BufferedImage>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, BufferedImage> entry = iterator.next();
            if (key == entry.getKey()) {
                return true;
            }
        }
        return false;
    }

    public static HashMap<String, String> getPackFiles() {
        return packFiles;
    }

    public static void setPackFiles(HashMap<String, String> packFiles) {
        FileManager.packFiles = packFiles;
    }

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        FileManager.count = count;
    }

}
