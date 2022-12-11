package org.amishaandkomal.views.dialogs;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class OtpVerificationDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea msgTextArea;
    private JTextField otpTextField;
    private JLabel otpErrorLabel;
    private boolean isVerified = false;

    public OtpVerificationDialog(String email) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // assign labels and text
        msgTextArea.setText("An OTP has been sent to " + email + ". Please enter the OTP below to verify your email address.");
        otpTextField.setText("");
        buttonOK.setText("Verify");
        buttonCancel.setText("Cancel");

        // configure error label
        otpErrorLabel.setVisible(false);
        otpErrorLabel.setForeground(java.awt.Color.RED);

        // make text area look like a label
        msgTextArea.setEditable(false);
        msgTextArea.setOpaque(false);
        msgTextArea.setFocusable(false);
        msgTextArea.setHighlighter(null);
        msgTextArea.setBorder(null);

        buttonOK.addActionListener(e -> onOK(email));

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK(String email) {
        if (otpTextField.getText().isEmpty()) {
            otpErrorLabel.setText("Please enter an OTP.");
            otpErrorLabel.setVisible(true);
            return;
        }
        if (otpTextField.getText().length() != 6) {
            otpErrorLabel.setText("OTP must be 6 digits long.");
            otpErrorLabel.setVisible(true);
            return;
        }
        try {
            int otp = Integer.parseInt(otpTextField.getText());
            if (org.amishaandkomal.utilities.EmailVerification.verifyOtp(email, otp)) {
                isVerified = true;
                dispose();
            } else {
                otpErrorLabel.setText("Invalid OTP.");
                otpErrorLabel.setVisible(true);
            }
        } catch (NumberFormatException e) {
            otpErrorLabel.setText("OTP must be a number.");
            otpErrorLabel.setVisible(true);
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public boolean isVerified() {
        return isVerified;
    }
}
