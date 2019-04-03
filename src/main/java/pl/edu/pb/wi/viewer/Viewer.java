package pl.edu.pb.wi.viewer;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import pl.edu.pb.wi.shared.ImageSharedOperations;
import pl.edu.pb.wi.shared.JDialogArrayClass;
import pl.edu.pb.wi.shared.JDialogClass;

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
import java.util.*;
import java.util.List;

public class Viewer extends JFrame {

    private BufferedImage img;
    private BufferedImage imageCopy;

    private JMenuItem loadImage, saveImage, loadImg, calculateHistograms, lightenHistogram,
            dimHistogram, stretchHistogram, equalizeHistograms, changeMode, undo,
            treasholdingRed, treasholdingGreen, treasholdingBlue, treasholdingAvg,
            bernsens, manual, otsu, niblack, mask_3_by_3, Kuhawara, median;

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
    String lastPath;

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
                lastPath = imageOpener.getSelectedFile().getPath();
                img = ImageSharedOperations.loadImage(lastPath);
                this.imageLabel.setIcon(new ImageIcon(img));
            }
        });
        loadImg.addActionListener((ActionEvent e) -> {
            String temp;
            try {
                System.out.println("Smth");
                temp = new File(".").getCanonicalPath() + "/test_rgb.png";
                img = ImageSharedOperations.loadImage(temp);
                this.imageLabel.setIcon(new ImageIcon(img));
            } catch (IOException ex) {
                ex.printStackTrace();
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
            if (!isImgLoaded())
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
            if (!isImgLoaded())
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
            BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
            int r = Integer.parseInt(JDialogClass.getInput("Enter radius", new JFrame()));

            Thread th = new Thread(() -> {
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        int[] temp = findMinMaxGrey(i, j, r);
                        int t = (temp[0] + temp[1]) / 2;
                        int l = (temp[1] - temp[0]);
                        deepCopy.setRGB(i, j, (l < t) ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
                    }
                }
            });
            th.start();
            imageLabel.setIcon(new ImageIcon(deepCopy));
            doneDialog("Done");
        });
        manual.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded())
                return;
            int threshold = Integer.parseInt(JDialogClass.getInput("Enter threshold", new JFrame()));
            if (threshold > 255 || threshold < 0) {
                JOptionPane.showMessageDialog(jPanel, "Wrong parameter");
                return;
            }
            Thread t = new Thread(() -> {
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        img.setRGB(i, j, (c.getRed() < threshold) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                    }
                }
                imageLabel.setIcon(new ImageIcon(img));
                doneDialog("Done");
            });
            t.start();
        });
        otsu.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded())
                return;
            makeHist();
            List<Double> list = new ArrayList<>();
//            int T = 0;
//            double threshold = 256.0;
            double sum = img.getHeight() * img.getWidth();
            for (int i = 1; i < 255; i++) {

                double wB = 0;
                for (int j = 0; j < i; j++) {
                    wB += histImgRed[j] / sum;
                }

                double wF = 0;
                for (int j = i; j < histImgRed.length; j++) {
                    wF += histImgRed[j] / sum;
                }

                double blockAvgB = 0.0;
                for (int j = 0; j < i; j++) {
                    blockAvgB += (histImgRed[j] / sum) * j / wB;
                }

                double blockAvgF = 0.0;
                for (int j = i; j < histImgRed.length; j++) {
                    blockAvgF += (histImgRed[j] / sum) * j / wF;
                }

                double sigmaB = 0.0;
                for (int j = 0; j < i; j++) {
                    sigmaB += (histImgRed[j] / sum) * Math.pow((j - blockAvgB), 2) / wB;
                }

                double sigmaF = 0.0;
                for (int j = i; j < histImgRed.length; j++) {
//                    for (int k = 0; k < histImgRed.length; k++) {
                    sigmaF += (histImgRed[j] / sum) * Math.pow((j - blockAvgF), 2) / wF;
//                    }
                }
                double sigmaW = wF * Math.pow(sigmaF, 2) + wB * Math.pow(sigmaB, 2);
                list.add(sigmaW);
            }
//            final double temp = threshold;
            Thread t = new Thread(() -> {
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        img.setRGB(i, j, (c.getRed() < list.indexOf(Collections.min(list))) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                    }
                }
                imageLabel.setIcon(new ImageIcon(img));
                doneDialog("Done");
            });
            t.start();
            System.out.println("Threashold counted by Otsu's method is: " + Collections.min(list) + ", for T: " + list.indexOf(Collections.min(list)));

        });
        niblack.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded())
                return;
            BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
            double k = Double.parseDouble(JDialogClass.getInput("Enter k param", new JFrame()));
            int r = Integer.parseInt(JDialogClass.getInput("Enter r param", new JFrame()));

            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    double avg = findAvgGrey(i, j, r);
                    double sd = standardDiviation(i, j, r, avg);
                    double threshold = avg + (k * sd);
                    Color c = new Color(img.getRGB(i, j));
                    deepCopy.setRGB(i, j, (c.getRed() < threshold) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            imageLabel.setIcon(new ImageIcon(deepCopy));
            doneDialog("Done");
        });
        mask_3_by_3.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded()) {
                return;
            }
            BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
            int m;
            try {
                m = Integer.parseInt(JDialogClass.getInput("Enter radius of mask", new JFrame()));
            } catch (NumberFormatException ex) {
                doneDialog("Param is not a integer");
                return;
            }
            if (m < 1) {
                doneDialog("Param is less than 1");
                return;
            }
            m = 2 * m + 1;
            String[] temp = JDialogArrayClass.getInput("Test", (int) Math.pow(m, 2.0), new JFrame());
            int test = 0;
            for (String t : temp) {
                try {
                    test = Integer.parseInt(t);
                } catch (NumberFormatException ex) {
                    doneDialog("Value is not a integer");
                    return;
                }
            }

            for (int i = 1; i < img.getWidth() - 1; i++) {
                for (int j = 1; j < img.getHeight() - 1; j++) {
                    Color c = doMask(i, j, temp);
                    deepCopy.setRGB(i, j, c.getRGB());
//                    deepCopy.setRGB(i, j, new Color(c.getRed(), c.getRed(), c.getRed()).getRGB());
                }
            }
            imageLabel.setIcon(new ImageIcon(deepCopy));
            doneDialog("Done");
        });
        median.addActionListener((ActionEvent e) -> {
            if (!isImgLoaded()) {
                return;
            }
            BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
            int m;
            try {
                m = Integer.parseInt(JDialogClass.getInput("Enter radius of mask", new JFrame()));
            } catch (NumberFormatException ex) {
                doneDialog("Param is not a integer");
                return;
            }
            if (m < 1) {
                doneDialog("Param is less than 1");
                return;
            } else if (m > 2) {
                doneDialog("Param is greater than 2");
                return;
            }
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    deepCopy.setRGB(i, j, doMedian(i, j, m));
                }
            }
            img=deepCopy;
            imageLabel.setIcon(new ImageIcon(img));
            doneDialog("Done");
        });
    }

    private int doMedian(int i, int j, int r) {
//        int[] arr = new int[(int) Math.pow(2 * r + 1, 2)];
        LinkedList<Integer> arr = new LinkedList<>();
        for (int k = i - r; k <= i + r; k++) {
            for (int l = j - r; l <= j + r; l++) {
                try {
                    arr.add(img.getRGB(k,l));
                } catch (ArrayIndexOutOfBoundsException ex) {
                }
            }
        }
        Collections.sort(arr);
        int out=arr.get(arr.size() / 2);
        return out;
    }

    private Color doMask(int i, int j, String[] mask) {
        int x = 0, y = 0, z = 0, counter = 0, sum = 0;
        double sqrt = Math.sqrt(mask.length);
        int r = (int) sqrt;
        r = (r - 1) / 2;

        for (String a : mask) {
            sum += Integer.parseInt(a);
        }

        for (int k = i - r; k <= i + r; k++) {
            for (int l = j - r; l <= j + r; l++) {
                try {
                    Color temp = new Color(img.getRGB(k, l));
                    x += temp.getRed() * Integer.parseInt(mask[counter]);
                    y += temp.getGreen() * Integer.parseInt(mask[counter]);
                    z += temp.getBlue() * Integer.parseInt(mask[counter]);
                    counter++;
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
        if (sum != 0) {
            x = x / sum;
            y = y / sum;
            z = z / sum;
        }

        return new Color(inBetweenOf(x), inBetweenOf(y), inBetweenOf(z));
    }

    private double standardDiviation(int i, int j, int r, double avg) {
        double sum = 0;
        int counter = 0;
        for (int k = i - r; k <= i + r; k++) {
            for (int l = j - r; l <= j + r; l++) {
                try {
                    Color c = new Color(img.getRGB(k, l));
                    sum += Math.pow((double) c.getRed() - avg, 2);
                    counter++;
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
        return Math.sqrt(sum / counter);
    }

    private void doneDialog(String msg) {
        JOptionPane.showMessageDialog(jPanel, msg);
    }

    private int[] findMinMaxGrey(int i, int j, int r) {
        int[] arr = {256, -1};

        for (int k = i - r; k <= i + r; k++) {
            for (int l = j - r; l <= j + r; l++) {
                try {
                    if (!(k == i && l == j)) {
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

    private double findAvgGrey(int i, int j, int r) {
        int sum = 0, counter = 0;

        for (int k = i - r; k <= i + r; k++) {
            for (int l = j - r; l <= j + r; l++) {
                try {
                    Color c = new Color(img.getRGB(k, l));
                    sum += c.getRed();
                    counter++;
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
        return sum / counter;
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
        JMenu binarization = new JMenu("Binarization");
        menuBar.add(binarization);
        JMenu greyScale = new JMenu("Grey Scale");
        binarization.add(greyScale);
        JMenu filters = new JMenu("Filters");
        menuBar.add(filters);
        loadImage = new JMenuItem("Load image");
        files.add(loadImage);
        loadImg = new JMenuItem("LoadImg");
        loadImg.setAccelerator(KeyStroke.getKeyStroke("L"));
        files.add(loadImg);
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
        binarization.add(bernsens);
        manual = new JMenuItem("Manual");
        binarization.add(manual);
        otsu = new JMenuItem("Otsu's");
        binarization.add(otsu);
        niblack = new JMenuItem("Niblack's");
        binarization.add(niblack);
        changeMode = new JMenuItem("Change mode");
        histogram.add(changeMode);
        mask_3_by_3 = new JMenuItem("Low-Pass");
        filters.add(mask_3_by_3);
        Kuhawara = new JMenuItem("Kuhawara");
        filters.add(Kuhawara);
        median = new JMenuItem("Median");
        filters.add(median);

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
