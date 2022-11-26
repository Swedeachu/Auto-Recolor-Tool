package swim.recolor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class PackBuilder {

    private static ArrayList<String> dirs = new ArrayList<String>();

    private static int count;

    private static void countPack(File[] files) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                countPack(file.listFiles());
            } else {
                count++;
            }
        }
    }

    public static void buildPack(String packDir, boolean isMCPE) throws IOException {
        try {
            String packName = ExportWindow.packNameField.getText();
            ExportWindow.closeExportWindow();
            LoadingWindow.showLoadingWindow();
            LoadingWindow.setValue(0);
            LoadingWindow.loadingLabel.setVisible(true);
            count = 0;
            countPack(new File(packDir).listFiles());
            LoadingWindow.setMaximum(count);
            if (isMCPE == true) {
                newManifest(packDir, packName);
            }
            File export = FileManager.exportPath;
            LoadingWindow.loadingLabel.setVisible(false);
            recolorPack(new File(packDir).listFiles());
            FileUtils.copyDirectoryToDirectory(new File(packDir), export);
            String newName = "";
            if (isMCPE == true) {
                export = new File(export + "\\" + FilenameUtils.getName(packDir));
                File pack = compressMCPACK(export.getAbsolutePath());
                pack = new File(pack.getParent() + "\\" + FilenameUtils.getBaseName(pack.getName()) + ".mcpack");
                newName = pack.getParent() + "\\" + packName + ".mcpack";
                pack.renameTo(new File(newName));
            } else {
                File pack = new File(export + "\\" + FilenameUtils.getBaseName(packDir));
                newName = pack.getParent() + "\\" + packName;
                pack.renameTo(new File(newName));
            }
            LoadingWindow.closeLoadingWindow();
            JOptionPane.showMessageDialog(null, "Recolored Pack Successfully Exported to " + newName, "Success!", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void recolorPack(File[] files) throws IOException {
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    recolorPack(file.listFiles());
                } else {
                    LoadingWindow.updateProgress();
                    if (Recolor.recolorFiles.containsKey(file.getAbsolutePath())) {
                        BufferedImage recolored = Recolor.recolorFiles.get(file.getAbsolutePath());
                        FileUtils.forceDelete(file);
                        ImageIO.write(recolored, "png", file);
                        LoadingWindow.setLoadingPreview(recolored);
                    } else {
                        BufferedImage img = ImageIO.read(file);
                        if (img != null) {
                            LoadingWindow.setLoadingPreview(img);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void newManifest(String packDir, String packName) throws IOException {
        File manifest = new File(packDir + "\\manifest.json");
        if (manifest.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(manifest));
                ArrayList<String> lines = new ArrayList<String>();
                String line = reader.readLine();
                while (line != null) {
                    if (line.contains("\"uuid\"")) {
                        String UUID = UUIDGenerator.generateType1UUID().toString();
                        String newLine = "\t\"uuid\": \"" + UUID + "\",";
                        lines.add(newLine);
                    } else if (line.contains("\"name\"")) {
                        String newLine = "\t\"name\": \"" + packName + "\",";
                        lines.add(newLine);
                    } else {
                        lines.add(line);
                    }
                    line = reader.readLine();
                }
                reader.close();
                File temp = new File(packDir + "\\temp.json");
                if (temp.createNewFile()) {
                    FileWriter writer = new FileWriter(temp);
                    for (String currentLine : lines) {
                        writer.write(currentLine + "\n");
                    }
                    writer.close();
                    FileUtils.forceDelete(manifest);
                    temp.renameTo(manifest);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File compressMCPACK(String dirPath) throws IOException {
        final Path sourceDir = Paths.get(dirPath);
        String zipFileName = dirPath.concat(".zip");
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
            File zippedPack = new File(dirPath + ".zip");
            File mcpack = new File(dirPath + ".mcpack");
            zippedPack.renameTo(mcpack);
            FileUtils.forceDelete(new File(dirPath));
            return zippedPack;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(dirPath);
    }

    public static void createAllowedDirs() {
        dirs.add("item");
        dirs.add("block");
        dirs.add("items");
        dirs.add("blocks");
        dirs.add("armor");
        dirs.add("background");
        dirs.add("world0");
        dirs.add("sky");
        dirs.add("overworld_cubemap");
        dirs.add("ui");
        dirs.add("gui");
        dirs.add("particle");
        dirs.add("environment");
        dirs.add("container");
        dirs.add("achievements");
    }

    public static ArrayList<String> getDirs() {
        return dirs;
    }

    public static class UUIDGenerator {
        public static UUID generateType1UUID() {
            long most64SigBits = get64MostSignificantBitsForVersion1();
            long least64SigBits = get64LeastSignificantBitsForVersion1();
            return new UUID(most64SigBits, least64SigBits);
        }

        private static long get64LeastSignificantBitsForVersion1() {
            Random random = new Random();
            long random63BitLong = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
            long variant3BitFlag = 0x8000000000000000L;
            return random63BitLong + variant3BitFlag;
        }

        private static long get64MostSignificantBitsForVersion1() {
            LocalDateTime start = LocalDateTime.of(1582, 10, 15, 0, 0, 0);
            Duration duration = Duration.between(start, LocalDateTime.now());
            long seconds = duration.getSeconds();
            long nanos = duration.getNano();
            long timeForUuidIn100Nanos = seconds * 10000000 + nanos * 100;
            long least12SignificatBitOfTime = (timeForUuidIn100Nanos & 0x000000000000FFFFL) >> 4;
            long version = 1 << 12;
            return (timeForUuidIn100Nanos & 0xFFFFFFFFFFFF0000L) + version + least12SignificatBitOfTime;
        }
    }

}
