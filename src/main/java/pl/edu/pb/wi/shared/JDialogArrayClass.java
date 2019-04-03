package pl.edu.pb.wi.shared;

import javax.swing.*;
import java.awt.*;

public class JDialogArrayClass {

    private JDialog dialog;
    private JTextField[] textField;

    private JDialogArrayClass(String title, int n, JFrame frame) {
        dialog = new JDialog(frame, title, true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setMinimumSize(new Dimension(200, 50));
        init(n);
    }

    private void setVisible() {
        dialog.setVisible(true);
    }

    public static String[] getInput(String title, int n, JFrame frame) {
        JDialogArrayClass input = new JDialogArrayClass(title, n, frame);
        input.setVisible();
        String[] out = new String[n];
        for (int i = 0; i < out.length; i++) {
            out[i] = input.textField[i].getText();
        }
        return out;
    }

    private void init(int n) {

        textField = new JTextField[n];
        for (int i = 0; i < textField.length; i++) {
            textField[i] = new JTextField();
        }
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        dialog.setLayout(new GridLayout((int)Math.sqrt(n)+1, (int)Math.sqrt(n), 3, 3));

        for (JTextField aTextField : textField) {
            dialog.add(aTextField);
        }
        dialog.add(okButton);
        dialog.pack();
    }
//    }
}