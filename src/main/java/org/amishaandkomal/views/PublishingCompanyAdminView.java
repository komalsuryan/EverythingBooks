package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.views.dialogs.AddEditBookDialog;
import org.amishaandkomal.views.dialogs.AddEditPublisherDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Objects;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class PublishingCompanyAdminView {
    private JPanel panel1;
    private JButton buttonEditCompany;
    private JButton buttonLogout;
    private JButton buttonEditProfile;
    private JLabel headingLabel;
    private JTable booksTable;
    private JButton editBookButton;
    private JButton deleteBookButton;
    private JButton addBookButton;
    private JTable employeesTable;
    private JButton addEmployeeButton;
    private JButton removeEmployeeButton;
    private JComboBox<String> addEmployeeFromUsersComboBox;
    private JButton addEmployeeFromUsersButton;
    private JTable salesTable;
    private int publishingAdminId;
    private int publishingCompanyId;

    PublishingCompanyAdminView(String email) {
        // assign heading text
        String sql = "SELECT * FROM users WHERE email = '" + email + "'";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                headingLabel.setText("Welcome, " + resultSet.getString("firstname"));
                publishingAdminId = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "SELECT * FROM publishing_company WHERE admin_id = " + publishingAdminId;
        String companyName = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                companyName = resultSet.getString("name");
                publishingCompanyId = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Add the company name to the heading
        headingLabel.setText(headingLabel.getText() + " (" + companyName + ")");

        // assign the button labels
        buttonEditCompany.setText("Edit Company Details");
        buttonEditProfile.setText("Edit Profile");
        buttonLogout.setText("Logout");

        // assign button actions
        buttonEditCompany.addActionListener(e -> onEditCompany());
        buttonEditProfile.addActionListener(e -> onEditInfo(publishingAdminId));
        buttonLogout.addActionListener(e -> onLogout());

        configureBooksPanel();
        configureEmployeesPanel();
        configureSalesPanel();
    }

    private void onEditCompany() {
        AddEditPublisherDialog addEditPublisherDialog = new AddEditPublisherDialog(true, publishingCompanyId);
        addEditPublisherDialog.comboBox1.setEnabled(false);
        addEditPublisherDialog.pack();
        addEditPublisherDialog.setVisible(true);
    }

    //region Books Panel
    private void createBooksTable() {
        // assign table data
        String sql = "SELECT books.*, publishing_company.name FROM books, publishing_company WHERE pub_id = " + publishingCompanyId + " AND publishing_company.id = books.pub_id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            booksTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        booksTable.setShowGrid(true);
        booksTable.getTableHeader().setBackground(Color.RED);
        booksTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditBook(String isbn) {
        AddEditBookDialog addEditBookDialog = new AddEditBookDialog(true, isbn);
        addEditBookDialog.pack();
        addEditBookDialog.setVisible(true);
        createBooksTable();
    }

    private void onDeleteBook(String isbn) {
        String sql = "DELETE FROM books WHERE isbn = '" + isbn + "'";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onAddBook() {
        AddEditBookDialog addEditBookDialog = new AddEditBookDialog(false, "");
        addEditBookDialog.pack();
        addEditBookDialog.setVisible(true);
        createBooksTable();
    }

    private void configureBooksPanel() {
        createBooksTable();

        editBookButton.setText("Edit Book");
        deleteBookButton.setText("Delete Book");
        addBookButton.setText("Add Book");

        editBookButton.setEnabled(false);
        deleteBookButton.setEnabled(false);

        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (booksTable.getSelectedRow() != -1) {
                editBookButton.setEnabled(true);
                deleteBookButton.setEnabled(true);
            } else {
                editBookButton.setEnabled(false);
                deleteBookButton.setEnabled(false);
            }
        });

        editBookButton.addActionListener(e -> onEditBook(String.valueOf(booksTable.getValueAt(booksTable.getSelectedRow(), 0))));
        deleteBookButton.addActionListener(e -> onDeleteBook((String) booksTable.getValueAt(booksTable.getSelectedRow(), 0)));
        addBookButton.addActionListener(e -> onAddBook());
    }
    //endregion

    //region Employees Panel
    private void createEmployeesTable() {
        String employeesSql = "SELECT users.id, users.firstname, users.lastname, users.email FROM employees, users WHERE publisher = ? AND employees.user_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(employeesSql);
            statement.setInt(1, publishingCompanyId);
            ResultSet rs = statement.executeQuery();
            employeesTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
        } catch (SQLException e) {
            throw new RuntimeException("Could not get publishing company employee details from the database: " + e);
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
            throw new RuntimeException("Could not get book store employee details from the database: " + e);
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
        String addEmployeeSql = "INSERT INTO employees (user_id, publishing_company) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addEmployeeSql);
            statement.setInt(1, userId);
            statement.setInt(2, publishingCompanyId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee to the database: " + e);
        }
        // add the user to the user_roles table
        String addUserRolesSql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addUserRolesSql);
            statement.setInt(1, userId);
            statement.setString(2, "PUB_EMPLOYEE");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee role to the database: " + e);
        }
    }
    //endregion

    //region Sales Panel
    // region Sales Panel
    private void createOrdersTable() {
        String sql = "SELECT orders1.*, deliveries.delivery_status, users.firstname, users.lastname FROM ((SELECT orders.order_id, books.name AS 'Book', orders.user_id, 'GENERAL USER' AS 'User type', quantity, order_status, delivery_needed, delivery_location FROM orders, books, publishing_company WHERE orders.isbn = books.isbn AND user_id IS NOT NULL AND sold_by_publisher_id = ?) UNION (SELECT orders.order_id, books.name AS 'Book', orders.library_id, 'LIBRARY' AS 'User type', quantity, order_status, delivery_needed, delivery_location FROM orders, books, publishing_company WHERE orders.isbn = books.isbn AND library_id IS NOT NULL AND sold_by_publisher_id = ?)) AS orders1 LEFT JOIN deliveries ON orders1.order_id = deliveries.order_id LEFT JOIN users ON users.id = deliveries.delivery_employee";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, publishingCompanyId);
            statement.setInt(2, publishingCompanyId);
            statement.setInt(3, publishingCompanyId);
            ResultSet rs = statement.executeQuery();
            salesTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureSalesPanel() {
        createOrdersTable();
    }
    // endregion

    public JPanel getMainPanel() {
        return panel1;
    }
}
