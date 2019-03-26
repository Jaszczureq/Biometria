package pl.edu.pb.wi.viewer;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Viewer extends JFrame {

    private BufferedImage img;
    private BufferedImage imageCopy;

    private JMenuItem loadImage, saveImage, calculateHistograms, lightenHistogram,
            dimHistogram, stretchHistogram, equalizeHistograms, changeMode, undo,
            treasholdingRed, treasholdingGreen, treasholdingBlue, treasholdingAvg,
            bernsens, manual, otsu, niblack;

    private JPanel jPanel = new JPanel();
    private final JLabel imageLabel = new JLabel();
    private final JLabel locationlabel = new JLabel();
    private final JLabel locationlabel2 = new JLabel();
    private final JLabel locationlabel23 = new JLabel();
    private volatile boolean dragging = false;

    private MyMouseAdapter mouse;

    private double magnify_ratio = 1;
    private int offsetX = 0, offsetY = 0;
    private int curX, curY;

    private int n = 256;
    private int margin = 3;
    private double varC = 1;
    private boolean byLecture = false;      //false - wg zajęć, true - wg wykładu dr inż. Bołdaka

    private int[] histImgRed = new int[n];
    private int[] histImgGreen = new int[n];
    private int[] histImgBlue = new int[n];
    private int[] histImgAvg = new int[n];

    public Viewer() {
        this.setLayout(new BorderLayout());
        this.setTitle("Podstawy Biometrii");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(640, 480);
        this.setVisible(true);

        initComponents();
        JToolBar jToolBar = new JToolBar();
        jPanel.add(jToolBar, BorderLayout.SOUTH);
        jToolBar.setLayout(new GridLayout(0, 6));

        jToolBar.add(locationlabel);
        jToolBar.add(locationlabel2);
        jToolBar.add(locationlabel23);


        imageLabel.addMouseMotionListener(mouse);
        imageLabel.addMouseListener(mouse);
        imageLabel.addMouseWheelListener(mouse);

        loadImage.addActionListener((ActionEvent e) -> {
            JFileChooser imageOpener = new JFileChooser();
            try {
                imageOpener.setCurrentDirectory(new File(new File(".").getCanonicalPath()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
                    //BufferedImage img = ImageSharedOperations.convertIconToImage((ImageIcon) this.imageLabel.getIcon());
                    ImageSharedOperations.saveImage(img, path);
                }
            }
            while (!accept);
        });
        undo.addActionListener((ActionEvent e) -> {
            if (img != null && imageCopy != null) {
                img = imageCopy;
                imageLabel.setIcon(new ImageIcon(img));
            }
        });
        changeMode.addActionListener((ActionEvent e) -> byLecture = !byLecture);
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
//                calculated = true;
            } else {
                JOptionPane.showMessageDialog(jPanel, "Brak obrazka");
            }
        });
        lightenHistogram.addActionListener((ActionEvent e) -> {
            if (img != null) {
                imageCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        try {
                            if (!byLecture) {
                                imageCopy.setRGB(i, j, new Color(
                                        (int) (varC * Math.log10(c.getRed() + 1)),
                                        (int) (varC * Math.log10(c.getGreen() + 1)),
                                        (int) (varC * Math.log10(c.getBlue() + 1))
                                ).getRGB());
                            } else {
                                imageCopy.setRGB(i, j, new Color(
                                        inBetweenOf(c.getRed() + (int) varC),
                                        inBetweenOf(c.getGreen() + (int) varC),
                                        inBetweenOf(c.getBlue() + (int) varC)
                                ).getRGB());
                            }
                        } catch (IllegalArgumentException ex) {
//                            System.out.println(i + ", " + j);
                        }
                    }
                }
                imageLabel.setIcon(new ImageIcon(imageCopy));
                img = imageCopy;
            } else {
                JOptionPane.showMessageDialog(jPanel, "Brak obrazka");
            }
        });
        dimHistogram.addActionListener((ActionEvent e) -> {
            if (img != null) {
                imageCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        imageCopy.setRGB(i, j, img.getRGB(i, j));
                    }
                }
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        try {
                            if (!byLecture) {
                                img.setRGB(i, j, new Color(
                                        inBetweenOf((int) (1 / varC * Math.pow(c.getRed(), 2.0))),
                                        inBetweenOf((int) (1 / varC * Math.pow(c.getGreen(), 2.0))),
                                        inBetweenOf((int) (1 / varC * Math.pow(c.getBlue(), 2.0)))
                                ).getRGB());
                            } else {
                                img.setRGB(i, j, new Color(
                                        inBetweenOf(c.getRed() - (int) varC),
                                        inBetweenOf(c.getGreen() - (int) varC),
                                        inBetweenOf(c.getBlue() - (int) varC)
                                ).getRGB());
                            }
                        } catch (IllegalArgumentException ex) {
//                            System.out.println(i + ", " + j);
                        }
                    }
                }
                //imageLabel.setIcon(new ImageIcon(img));
                //img = imageCopy;
            } else {
                JOptionPane.showMessageDialog(jPanel, "Brak obrazka");
            }
        });
        stretchHistogram.addActionListener((ActionEvent e) -> {
            if (img != null) {
                makeHist();
                imageCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        imageCopy.setRGB(i, j, img.getRGB(i, j));
                    }
                }
                int minR = getMin(histImgRed);
                int maxR = getMax(histImgRed);
                System.out.println(minR + ", " + maxR);
                int minG = getMin(histImgGreen);
                int maxG = getMax(histImgGreen);
                System.out.println(minG + ", " + maxG);
                int minB = getMin(histImgBlue);
                int maxB = getMax(histImgBlue);
                System.out.println(minB + ", " + maxB);

                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        double valueR = ((double) c.getRed() - (double) minR) / ((double) maxR - (double) minR) * (n - 1);
                        int newValueR = (int) valueR;
                        newValueR = inBetweenOf(newValueR);
                        double valueG = ((double) c.getGreen() - (double) minG) / ((double) maxG - (double) minG) * (n - 1);
                        int newValueG = (int) valueG;
                        newValueG = inBetweenOf(newValueG);
                        double valueB = ((double) c.getBlue() - (double) minB) / ((double) maxB - (double) minB) * (n - 1);
                        int newValueB = (int) valueB;
                        newValueB = inBetweenOf(newValueB);
                        img.setRGB(i, j, new Color(newValueR, newValueG, newValueB).getRGB());
                    }
                }
//                imageLabel.setIcon(new ImageIcon(img));
//                img = imageCopy;
//                calculated = false;
            } else {
                JOptionPane.showMessageDialog(jPanel, "Brak obrazka");
            }
        });
        equalizeHistograms.addActionListener((ActionEvent e) -> {
            if (img != null) {
                BufferedImage imageCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                makeHist();

                double[] probR = new double[n];
                double[] probG = new double[n];
                double[] probB = new double[n];
                double sum = img.getHeight() * img.getWidth();
                for (int i = 0; i < n; i++) {
                    probR[i] = (double) histImgRed[i] / sum;
                    probG[i] = (double) histImgGreen[i] / sum;
                    probB[i] = (double) histImgBlue[i] / sum;
                }
                double[] cumR = new double[n];
                cumR[0] = probR[0];
                cumR[n - 1] = 1;
                double[] cumG = new double[n];
                cumG[0] = probG[0];
                cumG[n - 1] = 1;
                double[] cumB = new double[n];
                cumB[0] = probB[0];
                cumB[n - 1] = 1;

                for (int i = 1; i < n - 1; i++) {
                    cumR[i] = cumR[i - 1] + probR[i];
                    cumG[i] = cumG[i - 1] + probG[i];
                    cumB[i] = cumB[i - 1] + probB[i];
                }
                System.out.println("Cumulative Sum Done");

                double[] LutR = new double[n];
                double[] LutG = new double[n];
                double[] LutB = new double[n];
                for (int i = 1; i < n; i++) {
                    LutR[i] = (cumR[i] - cumR[0]) / (1 - cumR[0]) * (n - 1);
                    LutG[i] = (cumG[i] - cumG[0]) / (1 - cumG[0]) * (n - 1);
                    LutB[i] = (cumB[i] - cumB[0]) / (1 - cumB[0]) * (n - 1);
                }
                System.out.println("Look-Up-Table Done");

                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        imageCopy.setRGB(i, j, new Color((int) LutR[c.getRed()], (int) LutG[c.getGreen()], (int) LutB[c.getBlue()]).getRGB());
                    }
                }
                imageLabel.setIcon(new ImageIcon(imageCopy));
                img = imageCopy;
//                calculated = false;
            } else {
                JOptionPane.showMessageDialog(jPanel, "Brak obrazka");
            }
        });
        treasholdingRed.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded())
                return;
            System.out.println("Img is loaded");
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color c = new Color(img.getRGB(i, j));
                    int red = c.getRed();
                    img.setRGB(i, j, new Color(red, red, red).getRGB());
                }
            }
            imageLabel.setIcon(new ImageIcon(img));
        });
        treasholdingGreen.addActionListener((ActionEvent e) -> {
            if (isImgLoaded())
                return;
            System.out.println("Img is loaded");
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color c = new Color(img.getRGB(i, j));
                    int green = c.getGreen();
                    img.setRGB(i, j, new Color(green, green, green).getRGB());
                }
            }
            imageLabel.setIcon(new ImageIcon(img));
        });
        treasholdingBlue.addActionListener((ActionEvent e) -> {
            if (isImgLoaded())
                return;
            System.out.println("Img is loaded");
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color c = new Color(img.getRGB(i, j));
                    int blue = c.getBlue();
                    img.setRGB(i, j, new Color(blue, blue, blue).getRGB());
                }
            }
            imageLabel.setIcon(new ImageIcon(img));
        });
        treasholdingAvg.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded())
                return;
            System.out.println("Img is loaded");
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color c = new Color(img.getRGB(i, j));
                    double avg = 0.31 * c.getRed() + 0.58 * c.getGreen() + 0.11 * c.getBlue();
                    img.setRGB(i, j, new Color((int) avg, (int) avg, (int) avg).getRGB());
                }
            }
            imageLabel.setIcon(new ImageIcon(img));
        });
        bernsens.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded())
                return;
            System.out.println("Img is loaded");
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    int temp[] = findMinMaxGrey(i, j, 1);
                    int t = (temp[0] + temp[1]) / 2;
                    int l = temp[1] - temp[0];
                    if (l >= t)
                        img.setRGB(i, j, new Color(0, 0, 0).getRGB());
                    else
                        img.setRGB(i, j, new Color(255, 255, 255).getRGB());
                }
            }
            imageLabel.setIcon(new ImageIcon(img));
        });
    }

    private int[] findMinMaxGrey(int i, int j, int r) {
        int[] arr = {256, -1};
        int counter=0;

        for (int k = i - r; k <= i + r; k++) {
            for (int l = j - r; l <= j + r; l++) {
                counter++;
                try {
                    if (k != i && l != j) {
                        Color c = new Color(img.getRGB(k, l));
                        if (arr[0] > c.getRed())
                            arr[0] = c.getRed();
                        if (arr[1] < c.getRed())
                            arr[1] = c.getRed();
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                }
            }
        }

        return arr;
    }

    private boolean isImgLoaded() {
        if (img == null) {
            JOptionPane.showMessageDialog(jPanel, "Brak obrazka");
            return false;
        } else
            return true;
    }

    private void initComponents() {

        JMenuBar menuBar = new JMenuBar();
        JMenu files = new JMenu("File");
        menuBar.add(files);
        JMenu histogram = new JMenu("Histograms");
        menuBar.add(histogram);
        JMenu binariization = new JMenu("Binarization");
        menuBar.add(binariization);
        JMenu greyScale = new JMenu("Grey Scale");
        binariization.add(greyScale);
        loadImage = new JMenuItem("Load image");
        files.add(loadImage);
        saveImage = new JMenuItem("Save image");
        files.add(saveImage);
        undo = new JMenuItem("Undo");
        undo.setAccelerator(KeyStroke.getKeyStroke("Z"));
        files.add(undo);
        calculateHistograms = new JMenuItem("Calculate histograms");
        histogram.add(calculateHistograms);
        lightenHistogram = new JMenuItem("Lighten histograms");
        histogram.add(lightenHistogram);
        dimHistogram = new JMenuItem("Darken histograms");
        histogram.add(dimHistogram);
        stretchHistogram = new JMenuItem("Stretch histograms");
        histogram.add(stretchHistogram);
        equalizeHistograms = new JMenuItem("Equalize histograms");
        histogram.add(equalizeHistograms);
        treasholdingRed = new JMenuItem("Red");
        greyScale.add(treasholdingRed);
        treasholdingGreen = new JMenuItem("Green");
        greyScale.add(treasholdingGreen);
        treasholdingBlue = new JMenuItem("Blue");
        greyScale.add(treasholdingBlue);
        treasholdingAvg = new JMenuItem("Average");
        greyScale.add(treasholdingAvg);
        bernsens = new JMenuItem("Bernsen's");
        binariization.add(bernsens);
        manual = new JMenuItem("Manual");
        binariization.add(manual);
        otsu = new JMenuItem("Otsu's");
        binariization.add(otsu);
        niblack = new JMenuItem("Niblack's");
        binariization.add(niblack);
        changeMode = new JMenuItem("Change mode");
        histogram.add(changeMode);

        System.out.println("Test1");
        mouse = new MyMouseAdapter();

        this.add(jPanel);
        jPanel.setLayout(new BorderLayout());
        jPanel.add(menuBar, BorderLayout.NORTH);
        jPanel.add(this.imageLabel, BorderLayout.CENTER);
        imageLabel.setLocation(0, 0);
    }

    private int inBetweenOf(int x) {
        if (x > n - 1) {
            x = n - 1;
        }
        if (x < 0) {
            x = 0;
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
                varC *= 2;
            } else {
                if (magnify_ratio > 0.25)
                    magnify_ratio /= 2;
                varC /= 2;
            }
//            int x =int(img.getWidth() * magnify_ratio);
//            BufferedImage temp = new BufferedImage((int) Math.round(img.getWidth() * magnify_ratio), (int) Math.round(img.getHeight() * magnify_ratio), img.getType());
            System.out.println("Magnify_ratio: " + magnify_ratio + ", " + varC);
            locationlabel23.setText("C: " + varC);
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
                    } catch (RasterFormatException ignored) {

                    }
                    dragging = false;
                }
//                jPanel.add(imageLabel, BorderLayout.CENTER);
                repaint();
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
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
    }

}


//TODO add some label flying next to mouse pointer
