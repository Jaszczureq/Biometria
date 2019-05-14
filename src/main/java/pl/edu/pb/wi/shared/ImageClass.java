package pl.edu.pb.wi.shared;

import com.sun.istack.internal.Nullable;
import pl.edu.pb.wi.viewer.Viewer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageClass {

    private BufferedImage originalImg;
    private JFileChooser imageOpener;
    private Viewer context;

    private boolean isImageLoaded;

    public ImageClass(Viewer m) {
        imageOpener = new JFileChooser();
        this.context = m;
        isImageLoaded=false;

        try {
            imageOpener.setCurrentDirectory(new File(new File(".").getCanonicalPath()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadImage(@Nullable String path) {
        imageOpener.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String fileName = f.getName().toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".png")
                        || fileName.endsWith(".tiff") || fileName.endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "Image files (.jpg, .png, .tiff, .bmp)";
            }
        });

        if (path != null) {
            originalImg = ImageSharedOperations.loadImage(path);
            isImageLoaded=true;
            return;
        }
        int returnValue = imageOpener.showDialog(null, "Select image");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String lastPath = imageOpener.getSelectedFile().getPath();
            originalImg = ImageSharedOperations.loadImage(lastPath);
            isImageLoaded=true;
//            this.imageLabel.setIcon(new ImageIcon(img));
        }
    }

    public void saveImage() {
        boolean accept = false;
        File f;
        imageOpener.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String fileName = f.getName().toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".png")
                        || fileName.endsWith(".tiff") || fileName.endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "Image files (.jpg, .png, .tiff, .bmp)";
            }
        });
        do {
            int returnValue = imageOpener.showDialog(null, "Select path");
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                f = imageOpener.getSelectedFile();
                if (f.exists()) {
                    int result = JOptionPane.showConfirmDialog(context, "The file exists,overwrite?",
                            "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (result == JOptionPane.YES_OPTION)
                        accept = true;
                } else
                    accept = true;
                String path = imageOpener.getSelectedFile().getPath();
                //BufferedImage img = ImageSharedOperations.convertIconToImage((ImageIcon) this.imageLabel.getIcon());
                ImageSharedOperations.saveImage(originalImg, path);
            }
        }
        while (!accept);
    }

    public boolean isImageLoaded() {
        return isImageLoaded;
    }

    public BufferedImage getOriginalImg() {
        return originalImg;
    }

    public BufferedImage getDeepCopyImg() {
        BufferedImage deepCopyImg = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), originalImg.getType());
        for (int i = 0; i < originalImg.getWidth(); i++) {
            for (int j = 0; j < originalImg.getHeight(); j++) {
                deepCopyImg.setRGB(i, j, originalImg.getRGB(i, j));
            }
        }
        return deepCopyImg;
    }

    public void setImage(BufferedImage imageCopy) {
        originalImg= new BufferedImage(imageCopy.getWidth(), imageCopy.getHeight(), imageCopy.getType());
        for (int i = 0; i < imageCopy.getWidth(); i++) {
            for (int j = 0; j < imageCopy.getHeight(); j++) {
                originalImg.setRGB(i, j, imageCopy.getRGB(i, j));
            }
        }
        context.imageLabel.setIcon(new ImageIcon(originalImg));
    }
}
