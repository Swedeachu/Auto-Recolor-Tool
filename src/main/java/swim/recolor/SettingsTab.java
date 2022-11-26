package swim.recolor;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SettingsTab {

    /**
     * @throws IOException
     * @wbp.parser.entryPoint
     */
    public static void ShowSettingsTab() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame settingsFrame = new JFrame();
        settingsFrame.setType(Type.POPUP);
        settingsFrame.setResizable(false);
        settingsFrame.setTitle("Settings");
        settingsFrame.setBounds(100, 100, 465, 345);
        settingsFrame.getContentPane().setBackground(Color.DARK_GRAY);
        settingsFrame.getContentPane().setLayout(null);

        JTextPane configsFolderText = new JTextPane();
        configsFolderText.setFont(new Font("Tahoma", Font.PLAIN, 14));
        configsFolderText.setBackground(Color.LIGHT_GRAY);
        configsFolderText.setEditable(false);
        configsFolderText.setText("Configs Folder: (DOES NOT CHANGE) \n " + FileManager.configPath + "\nExport Folder: \n " + FileManager.exportPath);
        configsFolderText.setBounds(10, 11, 429, 89);
        settingsFrame.getContentPane().add(configsFolderText);

        JButton changeExportPath = new JButton("Change Export Path");
        changeExportPath.setFont(new Font("Tahoma", Font.PLAIN, 19));
        changeExportPath.setBounds(239, 120, 200, 83);
        settingsFrame.getContentPane().add(changeExportPath);

        changeExportPath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(settingsFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    FileManager.exportPath = file;
                    configsFolderText.setText("Configs Folder: (DOES NOT CHANGE) \n " + FileManager.configPath + "\nExport Folder: \n " + FileManager.exportPath);
                    JOptionPane.showMessageDialog(null, "Changed Export Folder to: " + file.getAbsolutePath(), "Update!", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        FileManager.updateExportConfig(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        JButton configApplier = new JButton("Apply Config");
        configApplier.setFont(new Font("Tahoma", Font.PLAIN, 26));
        configApplier.setBounds(10, 120, 200, 83);
        settingsFrame.getContentPane().add(configApplier);

        configApplier.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // open generated drop down list of check boxes for which configs to apply
                try {
                    ConfigApplierWindow.ShowConfigApplier();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        JButton viewConfigsButton = new JButton("View Configs");
        viewConfigsButton.setFont(new Font("Tahoma", Font.PLAIN, 26));
        viewConfigsButton.setBounds(10, 214, 200, 83);
        settingsFrame.getContentPane().add(viewConfigsButton);

        viewConfigsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(FileManager.configPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        JButton viewExportsButton = new JButton("View Exports");
        viewExportsButton.setFont(new Font("Tahoma", Font.PLAIN, 26));
        viewExportsButton.setBounds(239, 214, 200, 83);
        settingsFrame.getContentPane().add(viewExportsButton);

        viewExportsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(FileManager.exportPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        settingsFrame.setVisible(true);

    }
}
