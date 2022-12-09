package org.amishaandkomal.views;

import javax.swing.*;

public class LoginView {
    private JPanel mainPanel;
    private JTextField emailTextField;
    private JPasswordField passwordTextField;
    private JButton loginButton;
    private JButton signInButton;
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JLabel emailErrorLabel;
    private JLabel passwordErrorLabel;

    public LoginView() {
        // assign label text
        emailLabel.setText("Email");
        passwordLabel.setText("Password");

        // assign button text
        loginButton.setText("Login to Continue");
        signInButton.setText("Not a registered user? Sign Up");

        // assign button action
        loginButton.addActionListener(e -> {
            if (emailTextField.getText().isEmpty()) {
                emailErrorLabel.setText("Email cannot be empty");
                emailErrorLabel.setVisible(true);
                return;
            }
            if (String.valueOf(passwordTextField.getPassword()).isEmpty()) {
                passwordErrorLabel.setText("Password cannot be empty");
                passwordErrorLabel.setVisible(true);
                return;
            }
            if (emailTextField.getText().equals("admin") && String.valueOf(passwordTextField.getPassword()).equals("admin")) {
                JOptionPane.showMessageDialog(null, "Login Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        signInButton.addActionListener(e -> JOptionPane.showMessageDialog(null, "Sign In Successful", "Success", JOptionPane.INFORMATION_MESSAGE));

        // assign error label listeners
        emailTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                emailErrorLabel.setVisible(false);
            }
        });
        passwordTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordErrorLabel.setVisible(false);
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
