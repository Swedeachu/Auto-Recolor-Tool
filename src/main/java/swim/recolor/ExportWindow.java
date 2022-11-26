package swim.recolor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ExportWindow extends SwingWorker<Void, Void> {

    private static JFrame exportFrame;
    public static JTextField packNameField;

    /**
     * @wbp.parser.entryPoint
     */
    public static void showExportWindow() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        exportFrame = new JFrame();
        exportFrame.setType(Type.POPUP);
        exportFrame.setResizable(false);
        exportFrame.setTitle("Export Recolored Pack");
        exportFrame.setBounds(100, 100, 479, 244);
        exportFrame.getContentPane().setBackground(Color.DARK_GRAY);
        exportFrame.getContentPane().setLayout(null);

        packNameField = new JTextField();
        packNameField.setBounds(10, 41, 448, 59);
        exportFrame.getContentPane().add(packNameField);
        packNameField.setColumns(10);

        JLabel packNameLabel = new JLabel("Pack Name:");
        packNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        packNameLabel.setForeground(Color.LIGHT_GRAY);
        packNameLabel.setBackground(Color.LIGHT_GRAY);
        packNameLabel.setBounds(10, 11, 177, 19);
        exportFrame.getContentPane().add(packNameLabel);

        JButton exportButton = new JButton("Export");
        exportButton.setFont(new Font("Tahoma", Font.PLAIN, 53));
        exportButton.setBounds(10, 111, 448, 88);
        exportFrame.getContentPane().add(exportButton);

        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String text = packNameField.getText();
                if (FileManager.legalFileName(text) == true) {
                    ExportWindow task = new ExportWindow();
                    task.execute();
                } else {
                    JOptionPane.showMessageDialog(null, "Not a legal File Name: " + text, "Error!", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        exportFrame.setVisible(true);
    }

    public static void closeExportWindow() {
        exportFrame.dispatchEvent(new WindowEvent(exportFrame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    protected Void doInBackground() throws Exception {
        PackBuilder.buildPack(FileManager.packDir, Recolor.isMCPE);
        return null;
    }
}
