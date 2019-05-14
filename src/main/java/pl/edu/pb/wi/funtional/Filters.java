package pl.edu.pb.wi.funtional;

import pl.edu.pb.wi.shared.JDialogArrayClass;
import pl.edu.pb.wi.shared.JDialogClass;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Filter;

public class Filters {
    public Filters() {

    }

    //    public void mask_3_by_3() {
//        if (!isImgLoaded()) {
//            return;
//        }
//        BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//        int m;
//        try {
//        try {
//            m = Integer.parseInt(JDialogClass.getInput("Enter radius of mask", new JFrame()));
//        } catch (NumberFormatException ex) {
//            JOptionPane.showMessageDialog(new JPanel(),"Param is not a integer");
//            return;
//        }
//        if (m < 1) {
//            JOptionPane.showMessageDialog(new JPanel(),"Param is less than 1");
//            return;
//        }
//        m = 2 * m + 1;
//        String[] temp = JDialogArrayClass.getInput("Test", (int) Math.pow(m, 2.0), new JFrame());
//        int test = 0;
//        for (String t : temp) {
//            try {
//                test = Integer.parseInt(t);
//            } catch (NumberFormatException ex) {
//                JOptionPane.showMessageDialog(new JPanel(),"Value is not a integer");
//                return;
//            }
//        }
//
//        for (int i = 1; i < img.getWidth() - 1; i++) {
//            for (int j = 1; j < img.getHeight() - 1; j++) {
//                Color c = doMask(i, j, temp);
//                deepCopy.setRGB(i, j, c.getRGB());
////                    deepCopy.setRGB(i, j, new Color(c.getRed(), c.getRed(), c.getRed()).getRGB());
//            }
//        }
//        imageLabel.setIcon(new ImageIcon(deepCopy));
//        JOptionPane.showMessageDialog(new JPanel(),"Done");
//    }
//
//    public void kuwahara() {
//        if (!isImgLoaded()) {
//            return;
//        }
//        int margin = 2;
//        BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
////            String[] temp = JDialogArrayClass.getInput("Test", (int) Math.pow(5, 2.0), new JFrame());
//
//        for (int i = margin; i < img.getWidth() - margin; i++) {
//            for (int j = margin; j < img.getHeight() - margin; j++) {
//                deepCopy.setRGB(i, j, doKuwahara(i, j));
//            }
//        }
//        imageLabel.setIcon(new ImageIcon(deepCopy));
//        JOptionPane.showMessageDialog(new JPanel(),"Done");
//    }
//
//    public void median() {
//        if (!isImgLoaded()) {
//            return;
//        }
//        BufferedImage deepCopy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//        int m;
//        try {
//            m = Integer.parseInt(JDialogClass.getInput("Enter radius of mask", new JFrame()));
//        } catch (NumberFormatException ex) {
//            JOptionPane.showMessageDialog(new JPanel(),"Param is not a integer");
//            return;
//        }
//        if (m < 1) {
//            JOptionPane.showMessageDialog(new JPanel(),"Param is less than 1");
//            return;
//        } else if (m > 2) {
//            JOptionPane.showMessageDialog(new JPanel(),"Param is greater than 2");
//            return;
//        }
//        for (int i = 0; i < img.getWidth(); i++) {
//            for (int j = 0; j < img.getHeight(); j++) {
//                deepCopy.setRGB(i, j, doMedian(i, j, m));
//            }
//        }
//        img = deepCopy;
//        imageLabel.setIcon(new ImageIcon(img));
//        JOptionPane.showMessageDialog(new JPanel(),"Done");
//    }
//    private Color doMask(int i, int j, String[] mask) {
//        int x = 0, y = 0, z = 0, counter = 0, sum = 0;
//        double sqrt = Math.sqrt(mask.length);
//        int r = (int) sqrt;
//        r = (r - 1) / 2;
//
//        for (String a : mask) {
//            sum += Integer.parseInt(a);
//        }
//
//        for (int k = i - r; k <= i + r; k++) {
//            for (int l = j - r; l <= j + r; l++) {
//                try {
//                    Color temp = new Color(img.getRGB(k, l));
//                    x += temp.getRed() * Integer.parseInt(mask[counter]);
//                    y += temp.getGreen() * Integer.parseInt(mask[counter]);
//                    z += temp.getBlue() * Integer.parseInt(mask[counter]);
//                    counter++;
//                } catch (ArrayIndexOutOfBoundsException ignored) {
//                }
//            }
//        }
//        if (sum != 0) {
//            x = x / sum;
//            y = y / sum;
//            z = z / sum;
//        }
//
//        return new Color(inBetweenOf(x), inBetweenOf(y), inBetweenOf(z));
//    }

}
