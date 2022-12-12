package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.views.dialogs.AddEditLibraryDialog;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class LibraryAdminView {
    private JButton editLibraryButton;
    private JButton logoutButton;
    private JButton editInfoButton;
    private JTable employeesTable;
    private JButton removeEmployeeButton;
    private JButton addEmployeeButton;
    private JLabel headingLabel;
    private JPanel mainPanel;
    private JComboBox<String> addEmployeeFromUsersComboBox;
    private JButton addEmployeeFromUsersButton;
    private int libId;
    private int userId;

    public LibraryAdminView(String email) {
        // get the userId from the email
        String userSql = "SELECT * FROM users WHERE email = '" + email + "'";
        userId = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(userSql);
            if (rs.next()) {
                userId = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library employee details from the database: " + e);
        }

        // get the libraryId from the userId
        String libSql = "SELECT id FROM library WHERE admin_id = " + userId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(libSql);
            if (rs.next()) {
                libId = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library details from the database: " + e);
        }

        // set the labels
        headingLabel.setText(getHeadingLabel());

        // set the buttons text
        editLibraryButton.setText("Edit Library Info");
        editInfoButton.setText("Edit My Info");
        logoutButton.setText("Logout");

        // set the action listeners
        editLibraryButton.addActionListener(e -> onEditLibrary());
        editInfoButton.addActionListener(e -> {
            onEditInfo(userId);
            headingLabel.setText(getHeadingLabel());
        });
        logoutButton.addActionListener(e -> onLogout());

        configureEmployeesPanel();
    }

    private String getHeadingLabel() {
        String userSql = "SELECT * FROM users WHERE id = " + userId;
        String firstName = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(userSql);
            if (rs.next()) {
                firstName = rs.getString("firstname");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library employee details from the database: " + e);
        }
        // get the library name from the libraryId
        String libNameSql = "SELECT name FROM library WHERE id = " + libId;
        String libName = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(libNameSql);
            if (rs.next()) {
                libName = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library employee details from the database: " + e);
        }
        return "Welcome %s (%s)".formatted(firstName, libName);
    }

    private void onEditLibrary() {
        AddEditLibraryDialog dialog = new AddEditLibraryDialog(true, libId);
        dialog.adminComboBox.setEnabled(false);
        dialog.pack();
        dialog.setVisible(true);
        headingLabel.setText(getHeadingLabel());
    }

    //region Employee Panel
    private void createEmployeesTable() {
        String employeesSql = "SELECT users.id, users.firstname, users.lastname, users.email FROM employees, users WHERE library = ? AND employees.user_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(employeesSql);
            statement.setInt(1, libId);
            ResultSet rs = statement.executeQuery();
            employeesTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library employee details from the database: " + e);
        }
    }

    private void configureEmployeesPanel() {
        createEmployeesTable();

        // set the button text
        removeEmployeeButton.setText("Remove Employee");
        addEmployeeButton.setText("Register New Employee");
        addEmployeeFromUsersButton.setText("Add Employee From Users");

        // disable the buttons
        removeEmployeeButton.setEnabled(false);
        addEmployeeFromUsersButton.setEnabled(false);

        // populate the addEmployeeFromUsersComboBox
        String usersSql = "SELECT * FROM users WHERE id NOT IN (SELECT user_id FROM user_roles)";
        // empty the comboBox
        addEmployeeFromUsersComboBox.removeAllItems();
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(usersSql);
            while (rs.next()) {
                addEmployeeFromUsersComboBox.addItem(rs.getString("firstname") + " " + rs.getString("lastname") + " - " + rs.getString("email"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library employee details from the database: " + e);
        }

        // set the action listeners
        employeesTable.getSelectionModel().addListSelectionListener(e -> removeEmployeeButton.setEnabled(employeesTable.getSelectedRow() != -1));
        removeEmployeeButton.addActionListener(e -> {
            if (employeesTable.getSelectedRow() != -1) {
                onRemoveEmployee(Integer.parseInt(employeesTable.getValueAt(employeesTable.getSelectedRow(), 0).toString()));
            }
        });
        addEmployeeFromUsersComboBox.addActionListener(e -> addEmployeeFromUsersButton.setEnabled(addEmployeeFromUsersComboBox.getSelectedIndex() != -1));
        addEmployeeButton.addActionListener(e -> onAddEmployee());
        addEmployeeFromUsersButton.addActionListener(e -> onAddEmployeeFromUsers());
    }

    private void onRemoveEmployee(int userId) {
        String removeEmployeeSql = "DELETE FROM employees WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(removeEmployeeSql);
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove employee from the database: " + e);
        }
        String removeUserRolesSql = "DELETE FROM user_roles WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(removeUserRolesSql);
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove employee role from the database: " + e);
        }
        configureEmployeesPanel();
    }

    private void onAddEmployee() {
        SignUpView dialog = new SignUpView(false, false, -1);
        dialog.pack();
        dialog.setVisible(true);
        String email = dialog.email;
        setPrivileges(email);
        configureEmployeesPanel();
    }

    private void onAddEmployeeFromUsers() {
        String email = Objects.requireNonNull(addEmployeeFromUsersComboBox.getSelectedItem()).toString().split(" - ")[1];
        setPrivileges(email);
        configureEmployeesPanel();
    }

    private void setPrivileges(String email) {
        // get the user id from the email
        String userIdSql = "SELECT id FROM users WHERE email = ?";
        int userId = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(userIdSql);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get user id from the database: " + e);
        }
        // add the user to the employees table
        String addEmployeeSql = "INSERT INTO employees (user_id, library) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addEmployeeSql);
            statement.setInt(1, userId);
            statement.setInt(2, libId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee to the database: " + e);
        }
        // add the user to the user_roles table
        String addUserRolesSql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addUserRolesSql);
            statement.setInt(1, userId);
            statement.setString(2, "LIB_EMPLOYEE");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee role to the database: " + e);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
