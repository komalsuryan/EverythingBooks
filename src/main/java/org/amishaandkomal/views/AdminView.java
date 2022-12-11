package org.amishaandkomal.views;

import org.amishaandkomal.Database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Objects;

import static org.amishaandkomal.utilities.FrequentGuiMethods.onLogout;
import static org.amishaandkomal.utilities.FrequentGuiMethods.resultSetToTableModel;

public class AdminView {

    private JButton editInfoButton;
    private JLabel headingLabel;
    private JButton logoutButton;
    private JPanel mainPanel;
    private JTable usersTable;
    private JButton editUserButton;
    private JButton deleteUserbutton;
    private JButton addUserButton;
    private int adminId;

    public AdminView(String email) {
        // assign heading text
        String sql = "SELECT * FROM users WHERE email = '" + email + "'";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                headingLabel.setText("Welcome, " + resultSet.getString("firstname"));
                adminId = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // set the edit info button label
        editInfoButton.setText("Edit Profile");
        logoutButton.setText("Logout");
        editUserButton.setText("Edit User");
        deleteUserbutton.setText("Delete User");
        addUserButton.setText("Add User");

        // if any row is selected, enable the edit and delete buttons
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (usersTable.getSelectedRow() != -1) {
                editUserButton.setEnabled(true);
                deleteUserbutton.setEnabled(true);
            } else {
                editUserButton.setEnabled(false);
                deleteUserbutton.setEnabled(false);
            }
        });

        // assign button action
        editInfoButton.addActionListener(e -> onEditInfo());
        logoutButton.addActionListener(e -> onLogout());
        editUserButton.addActionListener(e -> onEditUser(Integer.parseInt(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString())));
        deleteUserbutton.addActionListener(e -> onDeleteUser(Integer.parseInt(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString())));
        addUserButton.addActionListener(e -> onAddUser());

        // populate the users table
        createTable();

        // show table border
        usersTable.setShowGrid(true);

        // change the color of the table header
        usersTable.getTableHeader().setBackground(Color.RED);
        usersTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void createTable() {
        // assign table data
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            usersTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onEditInfo() {
        SignUpView signUpView = new SignUpView(true, true, adminId);
        signUpView.pack();
        signUpView.setVisible(true);
        createTable();
    }

    private void onEditUser(int userId) {
        SignUpView signUpView = new SignUpView(true, true, userId);
        signUpView.pack();
        signUpView.setVisible(true);
        createTable();
    }

    private void onDeleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = " + userId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            JOptionPane.showMessageDialog(null, "User deleted successfully");
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onAddUser() {
        SignUpView signUpView = new SignUpView(true, false, adminId);
        signUpView.pack();
        signUpView.setVisible(true);
        createTable();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
