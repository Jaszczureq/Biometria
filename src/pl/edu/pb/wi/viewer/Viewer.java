package pl.edu.pb.wi.viewer;

import com.sun.istack.internal.NotNull;
import pl.edu.pb.wi.shared.ImageSharedOperations;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Viewer extends JFrame {

    private BufferedImage img;

    private JPanel jPanel = new JPanel();
    private final JToolBar jToolBar = new JToolBar();
    private final JLabel imageLabel = new JLabel();
    private final JLabel locationlabel = new JLabel();
    private final JLabel locationlabel2 = new JLabel();
    private JMenuBar menuBar = new JMenuBar();
    private volatile boolean dragging = false;

    private MyMouseAdapter mouse;

    @NotNull
    private
    double magnify_ratio = 1;
    private int offsetX = 0, offsetY = 0;
    private int curX, curY;

    private int[] histImgRed = new int[256];
    private int[] histImgGreen = new int[256];
    private int[] histImgBlue = new int[256];

    public Viewer() {
        this.setLayout(new BorderLayout());
        this.setTitle("Podstawy Biometrii");
//        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(640, 480);
        this.setVisible(true);

        JMenu files = new JMenu("File");
        menuBar.add(files);
        JMenuItem loadImage = new JMenuItem("Load image");
        files.add(loadImage);
        JMenuItem saveImage = new JMenuItem("Save image");
        files.add(saveImage);
        System.out.println("Test1");
        mouse = new MyMouseAdapter();

        this.add(jPanel);
        jPanel.setLayout(new BorderLayout());
        jPanel.add(menuBar, BorderLayout.NORTH);
        jPanel.add(this.imageLabel, BorderLayout.CENTER);
        imageLabel.setLocation(0, 0);
//        imageLabel.setHorizontalAlignment(JLabel.LEFT);
//        imageLabel.setVerticalAlignment(JLabel.NORTH);

        jPanel.add(jToolBar, BorderLayout.SOUTH);
        jToolBar.setLayout(new GridLayout(0, 6));

        jToolBar.add(locationlabel);
        jToolBar.add(locationlabel2);


        imageLabel.addMouseMotionListener(mouse);
        imageLabel.addMouseListener(mouse);
        imageLabel.addMouseWheelListener(mouse);

        loadImage.addActionListener((ActionEvent e) -> {
            JFileChooser imageOpener = new JFileChooser();
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

            int returnValue = imageOpener.showDialog(null, "Select image");
            if (returnValue == JFileChooser.APPROVE_OPTION) {
//                String temp=imageOpener.getSelectedFile().getPath();
                img = ImageSharedOperations.loadImage(imageOpener.getSelectedFile().getPath());
                this.imageLabel.setIcon(new ImageIcon(img));
            }
        });

        saveImage.addActionListener((ActionEvent e) -> {
            JFileChooser imageSaver = new JFileChooser();
            boolean accept = false;
            File f;
            imageSaver.setFileFilter(new FileFilter() {
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
                int returnValue = imageSaver.showDialog(null, "Select path");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    f = imageSaver.getSelectedFile();
                    if (f.exists()) {
                        int result = JOptionPane.showConfirmDialog(this, "The file exists,overwrite?",
                                "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (result == JOptionPane.YES_OPTION)
                            accept = true;
                    } else
                        accept = true;
                    String path = imageSaver.getSelectedFile().getPath();
                    BufferedImage img = ImageSharedOperations.convertIconToImage((ImageIcon) this.imageLabel.getIcon());
                    ImageSharedOperations.saveImage(img, path);
                }
            }
            while (!accept);
        });
    }

    private void makeHist() {
        Arrays.fill(histImgRed, 0);
        Arrays.fill(histImgGreen, 0);
        Arrays.fill(histImgBlue, 0);
        int temp;
        Color c;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                temp = img.getRGB(i, j);
                c = new Color(temp, true);
                histImgRed[c.getRed()]++;
                histImgGreen[c.getGreen()]++;
                histImgBlue[c.getBlue()]++;

            }
        }
    }

    private class MyMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e.getButton() == MouseEvent.BUTTON1) {
                curX = e.getX();
                curY = e.getY();
            }
//            jPanel.remove(imageLabel);
        }


        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (!dragging) {
                dragging = true;
                System.out.println("Dragging");

            }
//            int x = imageLabel.getX();
//            int y = imageLabel.getY();
//            imageLabel.setLocation(e.getX(), e.getY());
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            super.mouseWheelMoved(e);

            if (e.getWheelRotation() < 0) {
                if (magnify_ratio < 8)
                    magnify_ratio *= 2;
            } else {
                if (magnify_ratio > 0.25)
                    magnify_ratio /= 2;
            }
//            int x =int(img.getWidth() * magnify_ratio);
            BufferedImage temp = new BufferedImage((int) Math.round(img.getWidth() * magnify_ratio), (int) Math.round(img.getHeight() * magnify_ratio), img.getType());
            System.out.println("Magnify_ratio: " + magnify_ratio);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);

            String location = String.format("[%.0f, %.0f]", e.getX() / magnify_ratio, e.getY() / magnify_ratio);
            System.out.println(location);
            if (e.getButton() == MouseEvent.BUTTON1) {

                if (img != null && !dragging) {
                    Color newColor = JColorChooser.showDialog(jPanel, "Choose Pixel Color", Color.WHITE);
                    if (newColor != null) {
                        img.setRGB(e.getX() + offsetY, e.getY() + offsetY, newColor.getRGB());
                        imageLabel.setIcon(new ImageIcon(img));
                    }
                }

                if (dragging) {
                    System.out.println("Dragged");
                    offsetX -= e.getX() - curX;
                    offsetY -= e.getY() - curY;
                    if (offsetX < 0)
                        offsetX = 0;
                    if (offsetY < 0)
                        offsetY = 0;
                    if (offsetX > imageLabel.getWidth())
                        offsetX = imageLabel.getWidth();
                    if (offsetY > imageLabel.getHeight())
                        offsetY = imageLabel.getHeight();
                    String location3 = String.format("Offsets [%d, %d]", offsetX, offsetY);
                    System.out.println(location3);
                    try {
                        BufferedImage temp = img.getSubimage(offsetX, offsetY, imageLabel.getWidth(), imageLabel.getHeight());
                        imageLabel.setIcon(new ImageIcon(temp));
                    } catch (RasterFormatException ex) {

                    }
                    dragging = false;
                }
//                jPanel.add(imageLabel, BorderLayout.CENTER);
                repaint();
            }
            else if(e.getButton()==MouseEvent.BUTTON2){
                makeHist();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            try {
                String location = String.format("[%.0f, %.0f]", e.getX() * magnify_ratio, e.getY() * magnify_ratio);
                locationlabel.setText(location);
                if (img != null) {
                    int imagem = img.getRGB(e.getX(), e.getY());
                    Color color = new Color(imagem, true);
                    String location2 = String.format("[%d, %d, %d]", color.getRed(), color.getGreen(), color.getBlue());
                    locationlabel2.setText(location2);

                }
                repaint();
            } catch (ArrayIndexOutOfBoundsException ex) {
            }
        }
    }

}


//TODO add some label flying next to mouse pointer
