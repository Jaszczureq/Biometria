package pl.edu.pb.wi.shared;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageSharedOperations {

    public static BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException ex) {
            System.out.println("Error has occured during file reading: " + ex.getMessage());
        }
        return image;
    }

    public static void saveImage(BufferedImage img, String path) {
        try {
            ImageIO.write(img, "jpg", new File(path));
        } catch (IOException ex) {
            System.out.println("Error has occured during file writing: " + ex.getMessage());
        }
    }

    public static BufferedImage convertIconToImage(ImageIcon icon) {
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.createGraphics();
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();
        return image;
    }

}
