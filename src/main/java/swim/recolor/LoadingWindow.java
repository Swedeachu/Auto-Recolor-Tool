package swim.recolor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window.Type;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LoadingWindow {

    public static JFrame frmPackProgress;
    private static JProgressBar packProgress;
    private static JLabel loadingPreview;
    public static JLabel loadingLabel;

    /**
     * @throws UnsupportedLookAndFeelException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @wbp.parser.entryPoint
     */
    public static void showLoadingWindow() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        frmPackProgress = new JFrame();
        frmPackProgress.setAlwaysOnTop(true);
        frmPackProgress.setType(Type.POPUP);
        frmPackProgress.setResizable(false);
        frmPackProgress.getContentPane().setBackground(Color.DARK_GRAY);
        frmPackProgress.getContentPane().setLayout(null);

        packProgress = new JProgressBar();
        packProgress.setFont(new Font("Tahoma", Font.PLAIN, 52));
        packProgress.setStringPainted(true);
        packProgress.setForeground(Color.GREEN);
        packProgress.setBackground(Color.RED);
        packProgress.setBounds(24, 149, 395, 95);
        frmPackProgress.getContentPane().add(packProgress);

        loadingPreview = new JLabel("");
        loadingPreview.setBounds(155, 10, 128, 128);
        frmPackProgress.getContentPane().add(loadingPreview);

        loadingLabel = new JLabel("Preparing...");
        loadingLabel.setFont(new Font("Tahoma", Font.PLAIN, 56));
        loadingLabel.setBounds(74, 37, 286, 101);
        frmPackProgress.getContentPane().add(loadingLabel);
        frmPackProgress.setTitle("Pack Progress");
        frmPackProgress.setBounds(100, 100, 450, 300);
        frmPackProgress.setVisible(true);
    }

    public static void closeLoadingWindow() {
        frmPackProgress.dispose();
    }

    public static void setLoadingPreview(BufferedImage image) {
        Image imageScalePreview = image.getScaledInstance(loadingPreview.getWidth(), loadingPreview.getHeight(), Image.SCALE_SMOOTH);
        loadingPreview.setIcon(new ImageIcon(imageScalePreview));
        frmPackProgress.revalidate();
    }

    public static void setMaximum(int progress) {
        packProgress.setMaximum(progress);
        frmPackProgress.revalidate();
    }

    public static void setValue(int progress) {
        packProgress.setValue(progress);
        frmPackProgress.revalidate();
    }

    public static void updateProgress() {
        packProgress.setValue(getPackProgress() + 1);
        frmPackProgress.revalidate();
    }

    public static int getPackProgress() {
        return packProgress.getValue();
    }
}
