package pl.edu.pb.wi.funtional;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import pl.edu.pb.wi.shared.ImageClass;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Histogram {
    private ImageClass imgObj;
    private BufferedImage img, imageCopy;
    private int n = 256;
    private double varC = 1;
    private int margin = 3;

    private boolean byLecture = false;      //false - wg zajęć, true - wg wykładu dr inż. Bołdaka

    private int[] histImgRed = new int[n];
    private int[] histImgGreen = new int[n];
    private int[] histImgBlue = new int[n];
    private int[] histImgAvg = new int[n];

    public Histogram(ImageClass imgObj) {
        this.imgObj = imgObj;
        this.img=imgObj.getDeepCopyImg();
    }

    public void calculateHistograms() {
        if (imgObj.isImageLoaded()) {
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
            JOptionPane.showMessageDialog(new JPanel(), "Brak obrazka");
        }
    }

    public void lightenHistogram() {
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
            imgObj.setImage(imageCopy);
//            imageLabel.setIcon(new ImageIcon(imageCopy));
//            img = imageCopy;
        } else {
            JOptionPane.showMessageDialog(new JPanel(), "Brak obrazka");
        }
    }

    public void dimHistogram() {
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
            JOptionPane.showMessageDialog(new JPanel(), "Brak obrazka");
        }
    }

    public void stretchHistogram() {
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
            JOptionPane.showMessageDialog(new JPanel(), "Brak obrazka");
        }
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

//        int temp;
        Color c;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
//                temp = img.getRGB(i, j);
                c = new Color(img.getRGB(i, j), true);

                histImgRed[c.getRed()]++;
                histImgGreen[c.getGreen()]++;
                histImgBlue[c.getBlue()]++;
                histImgAvg[(c.getRed() + c.getGreen() + c.getBlue()) / 3]++;

            }
        }
        System.out.println("Made histogram");
    }

    public void equalizeHistogram() {
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
            imgObj.setImage(imageCopy);
//                calculated = false;
        } else {
            JOptionPane.showMessageDialog(new JPanel(), "Brak obrazka");
        }
    }

    private CategoryChart makeChart(int[] hist, String title) {

        CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title(title).build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setAvailableSpaceFill(.96);
        chart.getStyler().setOverlapped(true);

        java.util.List<Integer> xData = new ArrayList<>();
        List<Integer> yData = new ArrayList<>();
        for (int i = 0; i < hist.length; i++) {
            yData.add(hist[i]);
            xData.add(i);
        }

        chart.addSeries(title, xData, yData);

        return chart;
    }

}
