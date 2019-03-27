package pl.edu.pb.wi.shared;

import javax.swing.*;
import java.awt.*;

public class JDialogClass {

    private JDialog dialog;
    private JTextField textField;

    private JDialogClass(String title, JFrame frame) {
        dialog = new JDialog(frame, title, true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setMinimumSize(new Dimension(200, 50));
        init();
    }

    private void setVisible() {
        dialog.setVisible(true);
    }

    public static String getInput(String title, JFrame frame) {
        JDialogClass input = new JDialogClass(title, frame);
        input.setVisible();
        String text = input.textField.getText();
        return text;
    }

    private void init() {

        textField = new JTextField();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());

        dialog.setLayout(new GridLayout(2, 1, 5, 5));

        dialog.add(textField);
        dialog.add(okButton);
        dialog.pack();
    }

//    public static void main(String args []){
//        String s = getInput("Dialog",null);
//        System.out.println(s);
//    }
}