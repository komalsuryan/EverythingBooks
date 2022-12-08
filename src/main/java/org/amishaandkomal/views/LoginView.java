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

    public LoginView() {
        // assign label text
        emailLabel.setText("Email");
        passwordLabel.setText("Password");

        // assign button text
        loginButton.setText("Login to Continue");
        signInButton.setText("Not a registered user? Sign Up");

        // assign button action
        loginButton.addActionListener(e -> {
            if (emailTextField.getText().equals("admin") && String.valueOf(passwordTextField.getPassword()).equals("admin")) {
                JOptionPane.showMessageDialog(null, "Login Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        signInButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Sign In Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
