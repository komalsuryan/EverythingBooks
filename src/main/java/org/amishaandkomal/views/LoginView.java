package org.amishaandkomal.views;

import com.password4j.Password;
import org.amishaandkomal.Database;

import javax.swing.*;
import java.sql.*;

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
        sql = "SELECT * FROM users LEFT JOIN user_roles ON user_roles.user_id = users.id WHERE id = " + userId + ";";
        try (Connection conn = DriverManager.getConnection(Database.databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // check if user is an admin, pub_admin, pub_employee, bs_admin, bs_employee, lib_admin, lib_employee, del_admin, del_employee or user
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "User role not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String role = rs.getString("role") == null ? "user" : rs.getString("role");
            JFrame frame;
            switch (role.toLowerCase()) {
                case "admin" -> {
                    AdminView adminView = new AdminView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Admin");
                    frame.setContentPane(adminView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "pub_admin" -> {
                    PublishingCompanyAdminView publisherAdminView = new PublishingCompanyAdminView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Publishing Company Admin");
                    frame.setContentPane(publisherAdminView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "pub_employee" -> {
                    PublishingCompanyEmployeeView publisherEmployeeView = new PublishingCompanyEmployeeView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Publishing Company Employee");
                    frame.setContentPane(publisherEmployeeView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "bs_admin" -> {
                    BookStoreAdminView bookStoreAdminView = new BookStoreAdminView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Book Store Admin");
                    frame.setContentPane(bookStoreAdminView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "bs_employee" -> {
                    BookStoreEmployeeView bookStoreEmployeeView = new BookStoreEmployeeView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Book Store Employee");
                    frame.setContentPane(bookStoreEmployeeView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "lib_admin" -> {
                    LibraryAdminView libraryAdminView = new LibraryAdminView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Library Admin");
                    frame.setContentPane(libraryAdminView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "lib_employee" -> {
                    LibraryEmployeeView libraryEmployeeView = new LibraryEmployeeView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Library Employee");
                    frame.setContentPane(libraryEmployeeView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "del_admin" -> {
                    DeliveryCompanyAdminView deliveryAdminView = new DeliveryCompanyAdminView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Delivery Admin");
                    frame.setContentPane(deliveryAdminView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                case "del_employee" -> {
                    DeliveryCompanyEmployeeView deliveryEmployeeView = new DeliveryCompanyEmployeeView(emailTextField.getText());
                    frame = new JFrame("EverythingBooks - Delivery Employee");
                    frame.setContentPane(deliveryEmployeeView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
                default -> {
                    UserView userView = new UserView(emailTextField.getText());
                    frame = new JFrame("User View");
                    frame.setContentPane(userView.getMainPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.setVisible(true);
                    SwingUtilities.getWindowAncestor(mainPanel).dispose();
                }
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
