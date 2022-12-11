package org.amishaandkomal.views;

import com.password4j.Password;
import org.amishaandkomal.Database;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginView {
    private JPanel mainPanel;
    private JTextField emailTextField;
    private JPasswordField passwordTextField;
    private JButton loginButton;
    private JButton signUpButton;
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
        signUpButton.setText("Not a registered user? Sign Up");

        // assign enter key to 'login' button
        mainPanel.registerKeyboardAction(e -> loginButton.doClick(), KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // assign button action
        loginButton.addActionListener(e -> onLogin());
        signUpButton.addActionListener(e -> onSignUp());

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

    public void onLogin() {
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
        // check if user email is in the database
        String sql = "SELECT * FROM users WHERE email = '" + emailTextField.getText() + "';";
        int userId = -1;
        try (Connection conn = DriverManager.getConnection(Database.databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // check if user email is in the database
            if (!rs.next()) {
                emailErrorLabel.setText("Incorrect email or password");
                emailErrorLabel.setVisible(true);
                return;
            }
            // check if password is correct
            if (!Password.check(String.valueOf(passwordTextField.getPassword()), rs.getString("password_hash")).withBcrypt()) {
                passwordErrorLabel.setText("Incorrect email or password");
                passwordErrorLabel.setVisible(true);
                return;
            }
            userId = rs.getInt("id");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        sql = "SELECT * FROM user_roles WHERE user_id = " + userId + ";";
        try (Connection conn = DriverManager.getConnection(Database.databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // check if user is an admin
            if (rs.next() && rs.getString("role").equalsIgnoreCase("admin")) {
                AdminView adminView = new AdminView(emailTextField.getText());
                JFrame frame = new JFrame("EverythingBooks - Admin");
                frame.setContentPane(adminView.getMainPanel());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                SwingUtilities.getWindowAncestor(mainPanel).dispose();
            } else {
                JOptionPane.showMessageDialog(null, "You are not an admin", "Error", JOptionPane.ERROR_MESSAGE);
//                UserView userView = new UserView(emailTextField.getText());
//                JFrame frame = new JFrame("User View");
//                frame.setContentPane(userView.userTabbedPane);
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.pack();
//                frame.setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void onSignUp() {
        SignUpView signUpView = new SignUpView(false, false, -1);
        signUpView.pack();
        signUpView.setVisible(true);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
