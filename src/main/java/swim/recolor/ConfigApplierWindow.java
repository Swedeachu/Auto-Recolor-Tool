package swim.recolor;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FilenameUtils;

public class ConfigApplierWindow extends SwingWorker<Void, Void> {

    private static JFrame configApplierFrame;
    private static JPanel configPanel;
    private static HashMap<File, Boolean> configFiles = new HashMap<File, Boolean>();
    public static ArrayList<String> names;

    /**
     * @wbp.parser.entryPoint
     */
    public static void ShowConfigApplier() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        configApplierFrame = new JFrame();
        configApplierFrame.setType(Type.POPUP);
        configApplierFrame.setResizable(false);
        configApplierFrame.setTitle("Apply Configs");
        configApplierFrame.setBounds(100, 100, 474, 356);
        configApplierFrame.getContentPane().setBackground(Color.DARK_GRAY);
        configApplierFrame.getContentPane().setLayout(null);

        configPanel = new JPanel();
        configPanel.setLayout(new GridLayout(0, 1));
        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scrollPane.setBounds(10, 11, 439, 234);
        scrollPane.add(configPanel);
        configApplierFrame.getContentPane().add(scrollPane);

        configFiles = new HashMap<File, Boolean>(); // clear it on each time this window opens

        // now add check boxes to the scroll pane
        ArrayList<String> configsList = getConfigsList();
        for (String config : configsList) {
            if (new File(config).exists()) {
                JCheckBox box = new JCheckBox();
                box.setText(FilenameUtils.removeExtension(FilenameUtils.getName(config)));
                box.setSelected(false);
                configPanel.add(box);
                box.setVisible(true);
                configFiles.put(new File(config), false);
                box.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        configFiles.put(new File(config), box.isSelected());
                    }
                });
            }
        }

        // refresh UI instantly
        configPanel.setVisible(false);
        configPanel.setVisible(true);
        configPanel.validate();

        // apply button
        JButton applyButton = new JButton("Apply Selected Configs");
        applyButton.setFont(new Font("Tahoma", Font.PLAIN, 32));
        applyButton.setBounds(10, 256, 439, 49);
        configApplierFrame.getContentPane().add(applyButton);

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ArrayList<String> namesList = new ArrayList<String>();
                for (Entry<File, Boolean> entry : configFiles.entrySet()) {
                    File config = entry.getKey();
                    Boolean enabled = entry.getValue();
                    if (config.exists() && enabled == true) {
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(config));
                            String line = reader.readLine();
                            while (line != null) {
                                if (!namesList.contains(line)) {
                                    namesList.add(line);
                                }
                                line = reader.readLine();
                            }
                            reader.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                // close
                configApplierFrame.dispatchEvent(new WindowEvent(configApplierFrame, WindowEvent.WINDOW_CLOSING));
                // run thread on own class <0.0>
                names = namesList;
                ConfigApplierWindow task = new ConfigApplierWindow();
                task.execute();
            }
        });

        configApplierFrame.setVisible(true);
    }

    private static ArrayList<String> getConfigsList() {
        ArrayList<String> configsList = new ArrayList<String>();
        try {
            File[] files = FileManager.configPath.listFiles();
            for (File file : files) {
                String name = FilenameUtils.getName(file.getAbsolutePath());
                if (FilenameUtils.getExtension(name).equals("txt")) {
                    configsList.add(file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configsList;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // modify buttons from here in a thread which will update the loading window
        try {
            FileManager.modifyButtonsFromConfig(names);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
