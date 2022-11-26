package swim.recolor;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.ScrollPane;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class Window {

    public static File publicPack;
    public static File currentlySelectedImage;

    private static JFrame window;
    private static JButton packButton;
    private static ScrollPane scp;
    public static JPanel buttonPanel;
    private static JList<String> dirFilter;
    private static JScrollPane scrollPane;
    public static JButton editorButton;
    @SuppressWarnings("rawtypes")
    public static JComboBox recolorMethod;

    public static HashMap<String, JButton> buttons;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        File tempDir = new File(System.getenv("APPDATA") + "\\swim_recolor");
        FileManager.createTempDir(tempDir);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        window = new JFrame();
        window.getContentPane().setForeground(Color.GRAY);
        window.setResizable(false);
        window.setTitle("Swim Auto Pack Recolor Tool");
        window.getContentPane().setBackground(Color.DARK_GRAY);
        window.getContentPane().setLayout(null);

        try {
            ImageIcon logo = new ImageIcon(Window.class.getResource("/swim_logo.png"));
            window.setIconImage(logo.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        packButton = new JButton("Drag and Drop Pack");
        packButton.setFont(new Font("Tahoma", Font.PLAIN, 20));
        packButton.setToolTipText("Drag and Drop a Texture Pack, or click to select a pack in file explorer");
        packButton.setBackground(Color.GRAY);
        packButton.setBounds(10, 11, 214, 102);
        window.getContentPane().add(packButton);
        window.setBounds(100, 100, 925, 618);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        packButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    int response = fileChooser.showOpenDialog(null);
                    if (response == JFileChooser.APPROVE_OPTION) {
                        String chosenFile = fileChooser.getSelectedFile().getAbsolutePath();
                        String extension = FilenameUtils.getExtension(chosenFile.toLowerCase());
                        File pack = new File(chosenFile);
                        if (FileUtils.isDirectory(pack) || extension.equals("zip") || extension.equals("mcpack")) {
                            publicPack = pack;
                            FileManager task = new FileManager();
                            task.execute();
                        } else {
                            JOptionPane.showMessageDialog(null, "Must be a Texture Pack (zip, mcpack, folder)", "Error!", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        packButton.setDropTarget(new DropTarget() {
            private static final long serialVersionUID = 1L;
            File pack;

            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    int i = 0;
                    for (File currentFile : droppedFiles) {
                        pack = currentFile;
                        i++;
                    }
                    if (i <= 1) {
                        String extension = FilenameUtils.getExtension(pack.getAbsolutePath().toLowerCase());
                        if (FileUtils.isDirectory(pack) || extension.equals("zip") || extension.equals("mcpack")) {
                            publicPack = pack;
                            FileManager task = new FileManager();
                            task.execute();
                        } else {
                            JOptionPane.showMessageDialog(null, "Must be a Texture Pack (zip, mcpack, folder)", "Error!", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "To many Files Dragged! (" + i + ")", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        JButton colorButton = new JButton("");
        colorButton.setFont(new Font("Tahoma", Font.PLAIN, 34));
        colorButton.setBackground(Color.GRAY);
        colorButton.setBounds(234, 11, 214, 102);
        colorButton.setOpaque(false);
        colorButton.setContentAreaFilled(false);
        colorButton.setBorderPainted(false);
        window.getContentPane().add(colorButton);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 6));
        scp = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scp.setBounds(234, 119, 659, 446);
        scp.add(buttonPanel);
        window.getContentPane().add(scp);

        JTextPane colorlabel = new JTextPane();
        colorlabel.setBackground(Recolor.color);
        colorlabel.setFont(new Font("Tahoma", Font.PLAIN, 26));
        colorlabel.setForeground(Recolor.getContrastColor(Recolor.color));
        colorlabel.setText("Current Color: \n(" + Recolor.color.getRed() + ", " + Recolor.color.getGreen() + ", " + Recolor.color.getBlue() + ") \nHex: " + Recolor.getHexFromColor(Recolor.color));
        colorlabel.setBounds(234, 11, 214, 102);
        colorlabel.setEditable(false);
        window.getContentPane().add(colorlabel);

        JButton btnSubmitRecolor = new JButton("Build Pack");
        btnSubmitRecolor.setToolTipText("Drag and Drop a Texture Pack, or click to select a pack in file explorer");
        btnSubmitRecolor.setFont(new Font("Tahoma", Font.PLAIN, 20));
        btnSubmitRecolor.setBackground(Color.GRAY);
        btnSubmitRecolor.setBounds(770, 11, 123, 102);
        window.getContentPane().add(btnSubmitRecolor);

        String methods[] = {" Hue Shift", " Tint"};
        recolorMethod = new JComboBox(methods);
        recolorMethod.setFont(new Font("Tahoma", Font.PLAIN, 27));
        recolorMethod.setToolTipText("The image recoloring algorithim to use");
        recolorMethod.setSelectedIndex(0);
        recolorMethod.setBounds(458, 11, 181, 102);
        window.getContentPane().add(recolorMethod);

        String arr[] = {"all"};
        scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 124, 214, 283);
        window.getContentPane().add(scrollPane);
        dirFilter = new JList(arr);
        dirFilter.setBounds(10, 306, 214, 102);
        dirFilter.setBorder(new LineBorder(new Color(0, 0, 0)));
        dirFilter.setForeground(Color.GREEN);
        dirFilter.setBackground(Color.GRAY);
        dirFilter.setSelectedIndex(0);
        scrollPane.setViewportView(dirFilter);

        JCheckBox smartFilter = new JCheckBox("Smart Filter (Hides excess Images)");
        scrollPane.setColumnHeaderView(smartFilter);
        smartFilter.setSelected(true);
        smartFilter.setToolTipText("Having this enabled hides pngs that are not in common folders such as items and blocks etc");
        smartFilter.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 2) {
                    FileManager.hideAllFiles = true;
                } else {
                    FileManager.hideAllFiles = false;
                }
            }
        });

        editorButton = new JButton("");
        editorButton.setBounds(10, 418, 214, 147);
        window.getContentPane().add(editorButton);

        JButton Settings = new JButton("Settings");
        Settings.setToolTipText("Modify Export and Config Settings");
        Settings.setFont(new Font("Tahoma", Font.PLAIN, 20));
        Settings.setBackground(Color.GRAY);
        Settings.setBounds(643, 11, 123, 102);
        window.getContentPane().add(Settings);

        Settings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    SettingsTab.ShowSettingsTab();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        editorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (currentlySelectedImage.exists()) {
                    try {
                        // TODO custome window frame
                        SpriteViewer.ShowSpriteViewer(currentlySelectedImage.getAbsolutePath());
                    } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                             UnsupportedLookAndFeelException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                boolean adjust = listSelectionEvent.getValueIsAdjusting();
                if (!adjust) {
                    JList list = (JList) listSelectionEvent.getSource();
                    int selections[] = list.getSelectedIndices();
                    for (int i = 0, n = selections.length; i < n; i++) {
                        if (selections[i] - 1 > -1) {
                            String dir = FileManager.packDirs.keySet().toArray()[selections[i] - 1].toString();
                            HashMap<String, String> files = FileManager.getPackFiles();
                            HashMap<String, String> newFiles = new HashMap<String, String>();
                            for (String f : files.keySet()) {
                                File fn = new File(f).getParentFile();
                                if (fn.equals(new File(dir))) {
                                    newFiles.put(f, files.get(f));
                                }
                            }
                            clearButtons();
                            FileManager.renderFiles(newFiles);
                            buttonPanel.revalidate();
                        } else { // all option was selected
                            clearButtons();
                            FileManager.renderFiles(FileManager.getPackFiles());
                            buttonPanel.revalidate();
                        }
                    }
                    buttonPanel.setVisible(false);
                    buttonPanel.setVisible(true);
                    window.validate();
                }
            }
        };
        dirFilter.addListSelectionListener(listSelectionListener);

        btnSubmitRecolor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    if (!FileManager.packDir.equals("none")) {
                        ExportWindow.showExportWindow();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    Color col = JColorChooser.showDialog(colorButton, "Select a Color", Recolor.color);
                    if (!col.equals(null)) {
                        Recolor.color = col;
                        colorlabel.setBackground(Recolor.color);
                        colorlabel.setForeground(Recolor.getContrastColor(Recolor.color));
                        colorlabel.setText("Current Color: \n(" + Recolor.color.getRed() + ", " + Recolor.color.getGreen() + ", " + Recolor.color.getBlue() + ") \nHex: "
                                + Recolor.getHexFromColor(Recolor.color));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        window.setVisible(true); // render everything
    }

    public static void refreshDirFilterList() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("all"); // to show all pack files
        for (String i : FileManager.packDirs.keySet()) {
            String parent = new File(i).getParentFile().getName();
            arr.add(parent + "/" + FileManager.packDirs.get(i));
        }
        createDirFilter(arr.toArray(new String[0]));
    }

    public static void createDirFilter(String arr[]) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (String str : arr) {
            model.addElement(str);
        }
        dirFilter.setModel(model);
        buttonPanel.setVisible(false);
        buttonPanel.setVisible(true);
        window.validate();
    }

    public static void clearPreviewImage() {
        editorButton.setIcon(null);
    }

    public static void clearButtons() {
        buttonPanel.removeAll();
        buttonPanel.validate();
    }

    public static void addButton(String path, String name) {
        try {
            JButton button = new JButton();
            buttons.put(path, button);
            int width = 64;
            int height = 64;
            button.setBounds(width, height, width, height);
            button.setSize(width, height);
            // check if already colored for the icon of the button
            if (Recolor.recolorFiles.containsKey(path)) {
                BufferedImage img = Recolor.recolorFiles.get(path);
                Image image = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(image);
                button.setIcon(icon);
            } else {
                BufferedImage img = ImageIO.read(new File(path));
                Image image = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(image);
                button.setIcon(icon);
            }
            button.setToolTipText(new File(path).getName());
            buttonPanel.add(button);
            button.setVisible(true);
            // add button listener
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        boolean selected = FileManager.checkIfImageSelectedForRecolor(Recolor.recolorFiles, path);
                        if (!selected) {
                            int mode = recolorMethod.getSelectedIndex();
                            currentlySelectedImage = new File(path);
                            if (mode == 1) { // for tint (only works good for dark images that need to be brute force recolored)
                                BufferedImage recoloredPreview = Recolor.tint(ImageIO.read(new File(path)), Recolor.color);
                                Image imageScale = recoloredPreview.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                                ImageIcon iconImage = new ImageIcon(imageScale);
                                button.setIcon(iconImage);
                                Image imageScalePreview = recoloredPreview.getScaledInstance(editorButton.getWidth(), editorButton.getHeight(), Image.SCALE_SMOOTH);
                                editorButton.setIcon(new ImageIcon(imageScalePreview));
                                Recolor.recolorFiles.put(path, recoloredPreview);
                            } else if (mode == 0) { // for hueshift (superior method for pretty much any color)
                                int red = Recolor.color.getRed();
                                int green = Recolor.color.getGreen();
                                int blue = Recolor.color.getBlue();
                                BufferedImage recoloredPreview = Recolor.hueShift(ImageIO.read(new File(path)), Recolor.getHue(red, green, blue));
                                Image imageScale = recoloredPreview.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                                ImageIcon iconImage = new ImageIcon(imageScale);
                                button.setIcon(iconImage);
                                Image imageScalePreview = recoloredPreview.getScaledInstance(editorButton.getWidth(), editorButton.getHeight(), Image.SCALE_SMOOTH);
                                editorButton.setIcon(new ImageIcon(imageScalePreview));
                                Recolor.recolorFiles.put(path, recoloredPreview);
                            }
                        } else {
                            Recolor.recolorFiles.remove(path);
                            BufferedImage img = ImageIO.read(new File(path));
                            Image image = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                            ImageIcon icon = new ImageIcon(image);
                            button.setIcon(icon);
                            Image imageScalePreview = img.getScaledInstance(editorButton.getWidth(), editorButton.getHeight(), Image.SCALE_SMOOTH);
                            editorButton.setIcon(new ImageIcon(imageScalePreview));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
