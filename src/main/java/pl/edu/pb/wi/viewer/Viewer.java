package pl.edu.pb.wi.viewer;

import com.sun.istack.internal.NotNull;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Viewer extends JFrame {

    private BufferedImage img;

    private JPanel jPanel = new JPanel();
    private final JToolBar jToolBar = new JToolBar();
    private final JLabel imageLabel = new JLabel();
    private final JLabel locationlabel = new JLabel();
    private final JLabel locationlabel2 = new JLabel();
    private volatile boolean dragging = false;

    private MyMouseAdapter mouse;

    @NotNull
    private
    double magnify_ratio = 1;
    private int offsetX = 0, offsetY = 0;
    private int curX, curY;

    private int n = 256;
    private int margin =12;

    private int[] histImgRed = new int[n];
    private int[] histImgGreen = new int[n];
    private int[] histImgBlue = new int[n];
    private int[] histImgAvg = new int[n];

    public Viewer() {
        this.setLayout(new BorderLayout());
        this.setTitle("Podstawy Biometrii");
//        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(640, 480);
        this.setVisible(true);

        JMenuBar menuBar = new JMenuBar();
        JMenu files = new JMenu("File");
        menuBar.add(files);
        JMenu histogram = new JMenu("Histograms");
        menuBar.add(histogram);
        JMenuItem loadImage = new JMenuItem("Load image");
        files.add(loadImage);
        JMenuItem saveImage = new JMenuItem("Save image");
        files.add(saveImage);
        JMenuItem calculateHistograms = new JMenuItem("Calculate histograms");
        histogram.add(calculateHistograms);
        JMenuItem lightenHistogram = new JMenuItem("Lighten histograms");
        histogram.add(lightenHistogram);
        JMenuItem dimHistogram = new JMenuItem("Darken histograms");
        histogram.add(dimHistogram);
        JMenuItem stretchHistogram = new JMenuItem("Stretch histograms");
        histogram.add(stretchHistogram);
        JMenuItem equalizeHistograms = new JMenuItem("Equalize histograms");
        histogram.add(equalizeHistograms);
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
        calculateHistograms.addActionListener((ActionEvent e) -> {
            if (img != null) {
                makeHist();
                Thread t = new Thread(new Runnable() {
                    CategoryChart chart = null;

                    @Override
                    public void run() {
                        chart = makeChart(histImgRed, "Red color");
                        new SwingWrapper<>(chart).displayChart().setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                        System.out.println("Drew a chart");
                        chart = makeChart(histImgGreen, "Green color");
                        new SwingWrapper<>(chart).displayChart().setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                        chart = makeChart(histImgBlue, "Blue color");
                        new SwingWrapper<>(chart).displayChart().setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                        chart = makeChart(histImgAvg, "Average color");
                        new SwingWrapper<>(chart).displayChart().setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                    }

                });
                t.start();
            }
        });
        lightenHistogram.addActionListener((ActionEvent e) -> {
            if (img != null) {

            }
        });
        dimHistogram.addActionListener((ActionEvent e) -> {
            if (img != null) {
            }
        });
        stretchHistogram.addActionListener((ActionEvent e) -> {
            if (img != null) {
                BufferedImage imageCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

                int minR = getMin(histImgRed);
                int maxR = getMax(histImgRed);
                System.out.println(minR+", "+maxR);
                int minG = getMin(histImgGreen);
                int maxG = getMax(histImgGreen);
                System.out.println(minG+", "+maxG);
                int minB = getMin(histImgBlue);
                int maxB = getMax(histImgBlue);
                System.out.println(minB+", "+maxB);

                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        double valueR = ((double) c.getRed() - (double) minR) / ((double) maxR - (double) minR) * (n - 1);
                        int newValueR = (int) valueR;
                        newValueR = notGreaterThan(newValueR);
                        double valueG = ((double) c.getGreen() - (double) minG) / ((double) maxG - (double) minG) * (n - 1);
                        int newValueG = (int) valueG;
                        newValueG = notGreaterThan(newValueG);
                        double valueB = ((double) c.getBlue() - (double) minB) / ((double) maxB - (double) minB) * (n - 1);
                        int newValueB = (int) valueB;
                        newValueB = notGreaterThan(newValueB);
                        imageCopy.setRGB(i, j, new Color(newValueR, newValueG, newValueB).getRGB());
                    }
                }
                imageLabel.setIcon(new ImageIcon(imageCopy));
                img = imageCopy;
            }
            System.out.println("Stretching is Done");
        });
        equalizeHistograms.addActionListener((ActionEvent e) -> {
            if (img != null) {
                
            }
        });
    }

    private int notGreaterThan(int x) {
        if (x > n - 1) {
            x = n - 1;
            System.out.println("Greater");
        }
        if (x < 0) {
            x = 0;
            System.out.println("Below");
        }
        return x;
    }

    private int getMin(int[] arr) {
        int min = 0, x = margin;
        while (x < n) {
            if (arr[x] > 0) {
                min = x;
                break;
            }
            x++;
        }
        return min;
    }

    private int getMax(int[] arr) {
        int max = 0, x = n - 1 - margin;
        while (x > 0) {
            if (arr[x] > 0) {
                max = x;
                break;
            }
            x--;
        }
        return max;
    }

    private void makeHist() {
        Arrays.fill(histImgRed, 0);
        Arrays.fill(histImgGreen, 0);
        Arrays.fill(histImgBlue, 0);
        Arrays.fill(histImgAvg, 0);
        int temp;
        Color c;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                temp = img.getRGB(i, j);
                c = new Color(temp, true);

                histImgRed[c.getRed()]++;
                histImgGreen[c.getGreen()]++;
                histImgBlue[c.getBlue()]++;
                histImgAvg[(c.getRed() + c.getGreen() + c.getBlue()) / 3]++;

            }
        }
        System.out.println("Made histogram");
    }

    private CategoryChart makeChart(int[] hist, String title) {

        CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title(title).build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setAvailableSpaceFill(.96);
        chart.getStyler().setOverlapped(true);

        List<Integer> xData = new ArrayList<>();
        List<Integer> yData = new ArrayList<>();
        for (int i = 0; i < hist.length; i++) {
            yData.add(hist[i]);
            xData.add(i);
        }

        chart.addSeries(title, xData, yData);

        return chart;
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
            System.out.println(e.getButton());

            String location = String.format("[%.0f, %.0f]", e.getX() / magnify_ratio, e.getY() / magnify_ratio);
            System.out.println(location);
            if (e.getButton() == MouseEvent.BUTTON1) {
                System.out.println("Button1");

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
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                System.out.println("Button3");
                if (img != null && !dragging) {
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            try {
                String location = String.format("[%.0f, %.0f]", e.getX() + offsetX * magnify_ratio, e.getY() + offsetY * magnify_ratio);
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
