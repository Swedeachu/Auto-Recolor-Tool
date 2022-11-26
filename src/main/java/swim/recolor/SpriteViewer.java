package swim.recolor;

import java.awt.Color;
import java.awt.Image;
import java.awt.Window.Type;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FilenameUtils;

import javax.swing.JLabel;

public class SpriteViewer {

    /**
     * @throws IOException
     * @wbp.parser.entryPoint
     */
    public static void ShowSpriteViewer(String currentlySelectedImage) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        BufferedImage img;
        if (Recolor.recolorFiles.containsKey(currentlySelectedImage)) {
            img = Recolor.recolorFiles.get(currentlySelectedImage);
        } else {
            img = ImageIO.read(new File(currentlySelectedImage));
        }
        Image image = img.getScaledInstance(256, 256, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(image);
        JFrame spriteFrame = new JFrame();
        spriteFrame.setType(Type.POPUP);
        spriteFrame.setResizable(true);
        spriteFrame.setTitle(FilenameUtils.getName(currentlySelectedImage));
        spriteFrame.setBounds(100, 100, 465, 328);
        spriteFrame.getContentPane().setBackground(Color.DARK_GRAY);
        spriteFrame.getContentPane().setLayout(null);

        JLabel imageLabel = new JLabel(icon);
        imageLabel.setBounds(93, 11, 256, 256);
        spriteFrame.getContentPane().add(imageLabel);
        spriteFrame.setVisible(true);
    }

}
