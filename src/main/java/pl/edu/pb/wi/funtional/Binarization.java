package pl.edu.pb.wi.funtional;

import pl.edu.pb.wi.shared.JDialogClass;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Binarization {
    public Binarization() {
    }

//    public void bernsen() {
//        if (!isImgLoaded())
//            return;
//        System.out.println("Img is loaded");
//        BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//        int r = Integer.parseInt(JDialogClass.getInput("Enter radius", new JFrame()));
//
//        Thread th = new Thread(() -> {
//            for (int i = 0; i < img.getWidth(); i++) {
//                for (int j = 0; j < img.getHeight(); j++) {
//                    int[] temp = findMinMaxGrey(i, j, r);
//                    int t = (temp[0] + temp[1]) / 2;
//                    int l = (temp[1] - temp[0]);
//                    deepCopy.setRGB(i, j, (l < t) ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
//                }
//            }
//        });
//        th.start();
//        imageLabel.setIcon(new ImageIcon(deepCopy));
//        JOptionPane.showMessageDialog(new JPanel(),"Done");
//    }
//
//    public void manual() {
//        if (!isImgLoaded())
//            return;
//        int threshold = Integer.parseInt(JDialogClass.getInput("Enter threshold", new JFrame()));
//        if (threshold > 255 || threshold < 0) {
//            JOptionPane.showMessageDialog(jPanel, "Wrong parameter");
//            return;
//        }
//        Thread t = new Thread(() -> {
//            for (int i = 0; i < img.getWidth(); i++) {
//                for (int j = 0; j < img.getHeight(); j++) {
//                    Color c = new Color(img.getRGB(i, j));
//                    img.setRGB(i, j, (c.getRed() < threshold) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
//                }
//            }
//            imageLabel.setIcon(new ImageIcon(img));
//            JOptionPane.showMessageDialog(new JPanel(),"Done");
//        });
//        t.start();
//    }
//
//    public void otsu() {
//        if (!isImgLoaded())
//            return;
//        makeHist();
//        List<Double> list = new ArrayList<>();
////            int T = 0;
////            double threshold = 256.0;
//        double sum = img.getHeight() * img.getWidth();
//        for (int i = 1; i < 255; i++) {
//
//            double wB = 0;
//            for (int j = 0; j < i; j++) {
//                wB += histImgRed[j] / sum;
//            }
//
//            double wF = 0;
//            for (int j = i; j < histImgRed.length; j++) {
//                wF += histImgRed[j] / sum;
//            }
//
//            double blockAvgB = 0.0;
//            for (int j = 0; j < i; j++) {
//                blockAvgB += (histImgRed[j] / sum) * j / wB;
//            }
//
//            double blockAvgF = 0.0;
//            for (int j = i; j < histImgRed.length; j++) {
//                blockAvgF += (histImgRed[j] / sum) * j / wF;
//            }
//
//            double sigmaB = 0.0;
//            for (int j = 0; j < i; j++) {
//                sigmaB += (histImgRed[j] / sum) * Math.pow((j - blockAvgB), 2) / wB;
//            }
//
//            double sigmaF = 0.0;
//            for (int j = i; j < histImgRed.length; j++) {
////                    for (int k = 0; k < histImgRed.length; k++) {
//                sigmaF += (histImgRed[j] / sum) * Math.pow((j - blockAvgF), 2) / wF;
////                    }
//            }
//            double sigmaW = wF * Math.pow(sigmaF, 2) + wB * Math.pow(sigmaB, 2);
//            list.add(sigmaW);
//        }
////            final double temp = threshold;
//        Thread t = new Thread(() -> {
//            for (int i = 0; i < img.getWidth(); i++) {
//                for (int j = 0; j < img.getHeight(); j++) {
//                    Color c = new Color(img.getRGB(i, j));
//                    img.setRGB(i, j, (c.getRed() < list.indexOf(Collections.min(list))) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
//                }
//            }
//            imageLabel.setIcon(new ImageIcon(img));
//            JOptionPane.showMessageDialog(new JPanel(),"Done");
//        });
//        t.start();
//        System.out.println("Threashold counted by Otsu's method is: " + Collections.min(list) + ", for T: " + list.indexOf(Collections.min(list)));
//    }
//    public void niblack(){
//        if (!isImgLoaded())
//            return;
//        BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//        double k = Double.parseDouble(JDialogClass.getInput("Enter k param", new JFrame()));
//        int r = Integer.parseInt(JDialogClass.getInput("Enter r param", new JFrame()));
//
//        for (int i = 0; i < img.getWidth(); i++) {
//            for (int j = 0; j < img.getHeight(); j++) {
//                double avg = findAvgGrey(i, j, r);
//                double sd = standardDiviation(i, j, r, avg);
//                double threshold = avg + (k * sd);
//                Color c = new Color(img.getRGB(i, j));
//                deepCopy.setRGB(i, j, (c.getRed() < threshold) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
//            }
//        }
//        imageLabel.setIcon(new ImageIcon(deepCopy));
//        JOptionPane.showMessageDialog(new JPanel(),"Done");
//    }
}
