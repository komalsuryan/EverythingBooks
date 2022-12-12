package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.views.dialogs.AddEditDeliveryCompanyDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class DeliveryCompanyAdminView {
    private JPanel panel1;
    private JButton editDeliveryCompanyButton;
    private JButton editInfoButton;
    private JButton logoutButton;
    private JTable employeesTable;
    private JButton removeEmployeeButton;
    private JButton addEmployeeButton;
    private JButton addEmployeeFromUsersButton;
    private JComboBox<String> addEmployeeFromUsersComboBox;
    private JLabel headingLabel;
    private JTable deliveriesTable;
    private int delCompId;
    private int userId;

    public DeliveryCompanyAdminView(String email) {
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
            throw new RuntimeException("Could not get delivery company employee details from the database: " + e);
        }

        // get the libraryId from the userId
        String libSql = "SELECT id FROM delivery_company WHERE admin_id = " + userId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(libSql);
            if (rs.next()) {
                delCompId = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get delivery company details from the database: " + e);
        }

        // set the labels
        headingLabel.setText(getHeadingLabel());

        // set the buttons text
        editDeliveryCompanyButton.setText("Edit Delivery CompanyInfo");
        editInfoButton.setText("Edit My Info");
        logoutButton.setText("Logout");

        // set the action listeners
        editDeliveryCompanyButton.addActionListener(e -> onEditDeliveryCompany());
        editInfoButton.addActionListener(e -> {
            onEditInfo(userId);
            headingLabel.setText(getHeadingLabel());
        });
        logoutButton.addActionListener(e -> onLogout());

        configureEmployeesPanel();
        configureDeliveriesPanel();
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
            throw new RuntimeException("Could not get delivery company employee details from the database: " + e);
        }
        // get the delivery company name from the libraryId
        String libNameSql = "SELECT name FROM delivery_company WHERE id = " + delCompId;
        String delCompName = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(libNameSql);
            if (rs.next()) {
                delCompName = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get delivery company employee details from the database: " + e);
        }
        return "Welcome %s (%s)".formatted(firstName, delCompName);
    }

    private void onEditDeliveryCompany() {
        AddEditDeliveryCompanyDialog dialog = new AddEditDeliveryCompanyDialog(true, delCompId);
        dialog.adminComboBox.setEnabled(false);
        dialog.pack();
        dialog.setVisible(true);
        headingLabel.setText(getHeadingLabel());
    }

    //region Employee Panel
    private void createEmployeesTable() {
        String employeesSql = "SELECT users.id, users.firstname, users.lastname, users.email FROM employees, users WHERE delivery_company = ? AND employees.user_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(employeesSql);
            statement.setInt(1, delCompId);
            ResultSet rs = statement.executeQuery();
            employeesTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
        } catch (SQLException e) {
            throw new RuntimeException("Could not get delivery company employee details from the database: " + e);
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
            throw new RuntimeException("Could not get delivery company employee details from the database: " + e);
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
        String addEmployeeSql = "INSERT INTO employees (user_id, delivery_company) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addEmployeeSql);
            statement.setInt(1, userId);
            statement.setInt(2, delCompId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee to the database: " + e);
        }
        // add the user to the user_roles table
        String addUserRolesSql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addUserRolesSql);
            statement.setInt(1, userId);
            statement.setString(2, "DEL_EMPLOYEE");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee role to the database: " + e);
        }
    }
    // endregion

    // region deliveries panel
    private void createDeliveriesTable() {
        String deliveriesSql = "SELECT deliveries.order_id, users.firstname, users.lastname, deliveries.pickup_location, deliveries.pickup_time, deliveries.delivery_location, deliveries.delivery_status FROM deliveries, employees, users WHERE employees.delivery_company = ? AND deliveries.delivery_employee = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(deliveriesSql);
            statement.setInt(1, delCompId);
            ResultSet rs = statement.executeQuery();
            deliveriesTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
            deliveriesTable.setShowGrid(true);
            deliveriesTable.getTableHeader().setBackground(Color.RED);
            deliveriesTable.getTableHeader().setForeground(Color.WHITE);
        } catch (SQLException e) {
            throw new RuntimeException("Could not get delivery company employee details from the database: " + e);
        }
    }

    private void configureDeliveriesPanel() {
        createDeliveriesTable();
    }
    // endregion

    public JPanel getMainPanel() {
        return panel1;
    }
}
