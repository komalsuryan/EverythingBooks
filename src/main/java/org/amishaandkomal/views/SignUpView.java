package org.amishaandkomal.views;

import com.password4j.Hash;
import com.password4j.Password;
import org.amishaandkomal.DatabaseSetup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class SignUpView extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField firstNameTextField;
    private JTextField lastNameTextField;
    private JTextField emailTextField;
    private JPasswordField enterPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel headingLabel;
    private JLabel firstNameLabel;
    private JLabel lastNameLabel;
    private JLabel emailLabel;
    private JLabel passswordLabel;
    private JLabel confirmPasswordLabel;
    private JLabel firstnameErrorLabel;
    private JLabel lastnameErrorLabel;
    private JLabel emailErrorLabel;
    private JLabel passwordErrorLabel;
    private JLabel mismatchPasswordErrorLabel;

    public SignUpView() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // set the labels
        headingLabel.setText("Sign Up to Continue");
        firstNameLabel.setText("First Name");
        lastNameLabel.setText("Last Name");
        emailLabel.setText("Email");
        passswordLabel.setText("Password");
        confirmPasswordLabel.setText("Confirm Password");
        buttonOK.setText("Sign Up");
        buttonCancel.setText("Cancel");

        // hide the error labels
        firstnameErrorLabel.setVisible(false);
        lastnameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        mismatchPasswordErrorLabel.setVisible(false);

        // set the error label text color to red
        firstnameErrorLabel.setForeground(Color.RED);
        lastnameErrorLabel.setForeground(Color.RED);
        emailErrorLabel.setForeground(Color.RED);
        passwordErrorLabel.setForeground(Color.RED);
        mismatchPasswordErrorLabel.setForeground(Color.RED);

        // add key up listeners to the text fields
        firstNameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (firstNameTextField.getText().isEmpty()) {
                    firstnameErrorLabel.setText("First Name cannot be empty");
                    firstnameErrorLabel.setVisible(true);
                } else if (!firstNameTextField.getText().matches("[a-zA-Z]+")) {
                    firstnameErrorLabel.setText("First Name can only contain alphabets");
                    firstnameErrorLabel.setVisible(true);
                } else {
                    firstnameErrorLabel.setVisible(false);
                }
            }
        });

        lastNameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (lastNameTextField.getText().isEmpty()) {
                    lastnameErrorLabel.setText("Last Name cannot be empty");
                    lastnameErrorLabel.setVisible(true);
                } else if (!lastNameTextField.getText().matches("[a-zA-Z]+")) {
                    lastnameErrorLabel.setText("Last Name can only contain alphabets");
                    lastnameErrorLabel.setVisible(true);
                } else {
                    lastnameErrorLabel.setVisible(false);
                }
            }
        });

        emailTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (emailTextField.getText().isEmpty()) {
                    emailErrorLabel.setText("Email cannot be empty");
                    emailErrorLabel.setVisible(true);
                } else if (!emailTextField.getText().matches("^(?=.{1,64}@)[A-Za-z0-9\\\\+_-]+(\\\\.[A-Za-z0-9\\\\+_-]+)*@[^-][A-Za-z0-9+-]+(\\.[A-Za-z0-9+-]+)*(\\.[A-Za-z]{2,})$")) {
                    emailErrorLabel.setText("Invalid Email");
                    emailErrorLabel.setVisible(true);
                } else {
                    // check if the email is already registered
                    try {
                        Connection connection = DriverManager.getConnection(DatabaseSetup.databaseUrl);
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE email = '" + emailTextField.getText() + "'");
                        if (resultSet.next()) {
                            emailErrorLabel.setText("Email already registered");
                            emailErrorLabel.setVisible(true);
                        } else {
                            emailErrorLabel.setVisible(false);
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });

        enterPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (enterPasswordField.getPassword().length == 0) {
                    passwordErrorLabel.setText("Password cannot be empty");
                    passwordErrorLabel.setVisible(true);
                } else if (enterPasswordField.getPassword().length < 8) {
                    passwordErrorLabel.setText("Password must be at least 8 characters long");
                    passwordErrorLabel.setVisible(true);
                } else {
                    passwordErrorLabel.setVisible(false);
                }
            }
        });

        confirmPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (confirmPasswordField.getPassword().length == 0) {
                    mismatchPasswordErrorLabel.setText("Confirm Password cannot be empty");
                    mismatchPasswordErrorLabel.setVisible(true);
                } else if (!String.valueOf(confirmPasswordField.getPassword()).equals(String.valueOf(enterPasswordField.getPassword()))) {
                    mismatchPasswordErrorLabel.setText("Passwords do not match");
                    mismatchPasswordErrorLabel.setVisible(true);
                } else {
                    mismatchPasswordErrorLabel.setVisible(false);
                }
            }
        });

        buttonOK.addActionListener(e -> onOK());

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

    private void onOK() {
        // check the validity of the fields
        if (firstNameTextField.getText().isEmpty()) {
            firstnameErrorLabel.setText("First Name cannot be empty");
            firstnameErrorLabel.setVisible(true);
        } else if (!firstNameTextField.getText().matches("[a-zA-Z]+")) {
            firstnameErrorLabel.setText("First Name can only contain alphabets");
            firstnameErrorLabel.setVisible(true);
        } else if (lastNameTextField.getText().isEmpty()) {
            lastnameErrorLabel.setText("Last Name cannot be empty");
            lastnameErrorLabel.setVisible(true);
        } else if (!lastNameTextField.getText().matches("[a-zA-Z]+")) {
            lastnameErrorLabel.setText("Last Name can only contain alphabets");
            lastnameErrorLabel.setVisible(true);
        } else if (emailTextField.getText().isEmpty()) {
            emailErrorLabel.setText("Email cannot be empty");
            emailErrorLabel.setVisible(true);
        } else if (!emailTextField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailErrorLabel.setText("Invalid Email");
            emailErrorLabel.setVisible(true);
        } else if (enterPasswordField.getPassword().length == 0) {
            passwordErrorLabel.setText("Password cannot be empty");
            passwordErrorLabel.setVisible(true);
        } else if (enterPasswordField.getPassword().length < 8) {
            passwordErrorLabel.setText("Password must be at least 8 characters long");
            passwordErrorLabel.setVisible(true);
        } else if (confirmPasswordField.getPassword().length == 0) {
            mismatchPasswordErrorLabel.setText("Confirm Password cannot be empty");
            mismatchPasswordErrorLabel.setVisible(true);
        } else if (!String.valueOf(confirmPasswordField.getPassword()).equals(String.valueOf(enterPasswordField.getPassword()))) {
            mismatchPasswordErrorLabel.setText("Passwords do not match");
            mismatchPasswordErrorLabel.setVisible(true);
        } else {
            // check if the email is already registered
            try {
                Connection connection = DriverManager.getConnection(DatabaseSetup.databaseUrl);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE email = '" + emailTextField.getText() + "'");
                if (resultSet.next()) {
                    emailErrorLabel.setText("Email already registered");
                    emailErrorLabel.setVisible(true);
                    return;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            // get the hash of the password
            String password = String.valueOf(enterPasswordField.getPassword());
            Hash hash = Password.hash(password).withBcrypt();
            String sql = "INSERT INTO users (firstname, lastname, email, password_hash) VALUES ('%s','%s','%s','%s');".formatted(firstNameTextField.getText(), lastNameTextField.getText(), emailTextField.getText(), hash.getResult());
            try {
                Connection connection = DriverManager.getConnection(DatabaseSetup.databaseUrl);
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            JOptionPane.showMessageDialog(null, "Account created successfully");
            dispose();
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SignUpView dialog = new SignUpView();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
