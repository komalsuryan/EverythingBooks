package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.views.dialogs.AddEditPublisherDialog;

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
    private JButton deleteUserButton;
    private JButton addUserButton;
    private JTable publishersTable;
    private JButton editPublisherButton;
    private JButton deletePublisherButton;
    private JButton addPublisherButton;
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

        // assign button action
        editInfoButton.addActionListener(e -> onEditInfo());
        logoutButton.addActionListener(e -> onLogout());

        // configure the panels
        configurePublishersPanel();
        configureUsersPanel();
    }

    private void onEditInfo() {
        SignUpView signUpView = new SignUpView(true, true, adminId);
        signUpView.pack();
        signUpView.setVisible(true);
        createUsersTable();
    }

    private void createPublishersTable() {
        // assign table data
        String sql = "SELECT publishing_company.id, name, warehouse_location, firstname, lastname FROM publishing_company, users WHERE publishing_company.admin_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            publishersTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        publishersTable.setShowGrid(true);
        publishersTable.getTableHeader().setBackground(Color.RED);
        publishersTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditPublisher(int id) {
        AddEditPublisherDialog addEditPublisherDialog = new AddEditPublisherDialog(true, id);
        addEditPublisherDialog.pack();
        addEditPublisherDialog.setVisible(true);
        createPublishersTable();
    }

    private void onDeletePublisher(int id) {
        // get the publisher admin id
        String sql = "SELECT admin_id FROM publishing_company WHERE id = " + id;
        int adminId = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                adminId = resultSet.getInt("admin_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the publisher
        sql = "DELETE FROM publishing_company WHERE id = " + id;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the publisher admin role
        sql = "DELETE FROM user_roles WHERE user_id = " + adminId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createPublishersTable();
    }

    private void onAddPublisher() {
        AddEditPublisherDialog addEditPublisherDialog = new AddEditPublisherDialog(false, -1);
        addEditPublisherDialog.pack();
        addEditPublisherDialog.setVisible(true);
        createPublishersTable();
    }

    private void configurePublishersPanel() {
        // create the table
        createPublishersTable();

        editPublisherButton.setText("Edit Publisher");
        deletePublisherButton.setText("Delete Publisher");
        addPublisherButton.setText("Add Publisher");

        // disable the edit and delete buttons
        editPublisherButton.setEnabled(false);
        deletePublisherButton.setEnabled(false);

        // if any row is selected, enable the edit and delete buttons
        publishersTable.getSelectionModel().addListSelectionListener(e -> {
            if (publishersTable.getSelectedRow() != -1) {
                editPublisherButton.setEnabled(true);
                deletePublisherButton.setEnabled(true);
            } else {
                editPublisherButton.setEnabled(false);
                deletePublisherButton.setEnabled(false);
            }
        });

        // assign button actions
        editPublisherButton.addActionListener(e -> {
            int row = publishersTable.getSelectedRow();
            if (row != -1) {
                int id = (int) publishersTable.getValueAt(row, 0);
                onEditPublisher(id);
            }
        });
        deletePublisherButton.addActionListener(e -> {
            int row = publishersTable.getSelectedRow();
            if (row != -1) {
                int id = (int) publishersTable.getValueAt(row, 0);
                onDeletePublisher(id);
            }
        });
        addPublisherButton.addActionListener(e -> onAddPublisher());
    }

    //region Users Panel
    private void createUsersTable() {
        // assign table data
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            usersTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // show table border
        usersTable.setShowGrid(true);

        // change the color of the table header
        usersTable.getTableHeader().setBackground(Color.RED);
        usersTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditUser(int userId) {
        SignUpView signUpView = new SignUpView(true, true, userId);
        signUpView.pack();
        signUpView.setVisible(true);
        createUsersTable();
    }

    private void onDeleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = " + userId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            JOptionPane.showMessageDialog(null, "User deleted successfully");
            createUsersTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onAddUser() {
        SignUpView signUpView = new SignUpView(true, false, adminId);
        signUpView.pack();
        signUpView.setVisible(true);
        createUsersTable();
    }

    private void configureUsersPanel() {
        // configure the users table
        createUsersTable();

        // set the button labels
        editUserButton.setText("Edit User");
        deleteUserButton.setText("Delete User");
        addUserButton.setText("Add User");

        // disable the edit and delete buttons
        editUserButton.setEnabled(false);
        deleteUserButton.setEnabled(false);

        // if any row is selected, enable the edit and delete buttons
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (usersTable.getSelectedRow() != -1) {
                editUserButton.setEnabled(true);
                deleteUserButton.setEnabled(true);
            } else {
                editUserButton.setEnabled(false);
                deleteUserButton.setEnabled(false);
            }
        });

        editUserButton.addActionListener(e -> onEditUser(Integer.parseInt(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString())));
        deleteUserButton.addActionListener(e -> onDeleteUser(Integer.parseInt(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString())));
        addUserButton.addActionListener(e -> onAddUser());
    }
    //endregion

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
