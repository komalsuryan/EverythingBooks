package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.views.dialogs.AddEditBookDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class PublishingCompanyEmployeeView {
    private JPanel panel1;
    private JButton editInfoButton;
    private JButton logoutButton;
    private JTable ordersTable;
    private JButton processOrderButton;
    private JButton completeOrderButton;
    private JButton scheduleDeliveryButton;
    private JLabel headingLabel;
    private JTable booksTable;
    private JButton editBookButton;
    private JButton deleteBookButton;
    private JButton addBookButton;
    private int userID;
    private int companyID;

    public PublishingCompanyEmployeeView(String email) {
        // get the user id from the email
        String userIDQuery = "SELECT id FROM users WHERE email = '" + email + "'";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(userIDQuery);
            rs.next();
            userID = rs.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // get the company id from the user id
        String companyIDQuery = "SELECT publisher FROM employees WHERE user_id = " + userID;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(companyIDQuery);
            rs.next();
            companyID = rs.getInt("publisher");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // get the user's first name
        String firstNameQuery = "SELECT firstname FROM users WHERE id = " + userID;
        AtomicReference<String> firstName = new AtomicReference<>("");
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(firstNameQuery);
            rs.next();
            firstName.set(rs.getString("firstname"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // get the company name
        String companyNameQuery = "SELECT name FROM publishing_company WHERE id = " + companyID;
        String companyName = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(companyNameQuery);
            rs.next();
            companyName = rs.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // assign the heading
        headingLabel.setText("Welcome, " + firstName + " (" + companyName + ")");

        // set text for buttons
        editInfoButton.setText("Edit Info");
        logoutButton.setText("Logout");

        // add listeners to buttons
        String finalCompanyName = companyName;
        editInfoButton.addActionListener(e -> {
            onEditInfo(userID);
            // reassigning the heading
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                Statement statement = connection.createStatement();
                firstName.set(statement.executeQuery(firstNameQuery).getString("firstname"));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            headingLabel.setText("Welcome, " + firstName + " (" + finalCompanyName + ")");
        });
        logoutButton.addActionListener(e -> onLogout());

        configureBooksPanel();
        configureOrdersPanel();
    }

    //region Books Panel
    private void createBooksTable() {
        // assign table data
        String sql = "SELECT books.*, publishing_company.name FROM books, publishing_company WHERE pub_id = " + companyID + " AND publishing_company.id = books.pub_id";
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


    private void createOrdersTable() {
        String sql = "(SELECT orders.order_id, books.name AS 'Book', orders.user_id, 'GENERAL USER' AS 'User type', quantity, order_status, delivery_needed, delivery_location FROM orders, books, publishing_company WHERE orders.isbn = books.isbn AND user_id IS NOT NULL AND sold_by_publisher_id = ?) UNION (SELECT orders.order_id, books.name AS 'Book', orders.library_id, 'LIBRARY' AS 'User type', quantity, order_status, delivery_needed, delivery_location FROM orders, books, publishing_company WHERE orders.isbn = books.isbn AND library_id IS NOT NULL AND sold_by_publisher_id = ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, companyID);
            statement.setInt(2, companyID);
            ResultSet rs = statement.executeQuery();
            ordersTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void onProcessOrder(int orderId) {
        // get the current status of the order
        String orderStatusSql = "SELECT * FROM orders WHERE order_id = ?";
        ResultSet rs;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(orderStatusSql);
            statement.setInt(1, orderId);
            rs = statement.executeQuery();
            rs.next();
            if (!rs.getString("order_status").equalsIgnoreCase("PLACED")) {
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String statusUpdateSql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(statusUpdateSql);
            statement.setString(1, "PROCESSED");
            statement.setInt(2, orderId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createOrdersTable();
    }

    private void configureOrdersPanel() {
        createOrdersTable();

        // set button text
        processOrderButton.setText("Process");
        scheduleDeliveryButton.setText("Schedule Delivery");
        completeOrderButton.setText("Mark Order as Completed");

        // disable all the buttons
        processOrderButton.setEnabled(false);
        scheduleDeliveryButton.setEnabled(false);
        completeOrderButton.setEnabled(false);

        // add listeners to ordersTable
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (ordersTable.getSelectedRow() != 1) {
                processOrderButton.setEnabled(true);
                if (Integer.parseInt(ordersTable.getValueAt(ordersTable.getSelectedRow(), 6).toString()) == 1) {
                    scheduleDeliveryButton.setEnabled(true);
                } else {
                    completeOrderButton.setEnabled(true);
                }
            } else {
                processOrderButton.setEnabled(false);
                scheduleDeliveryButton.setEnabled(false);
                completeOrderButton.setEnabled(false);
            }
        });
        // add action listeners to buttons
        processOrderButton.addActionListener(e -> onProcessOrder(Integer.parseInt(ordersTable.getValueAt(ordersTable.getSelectedRow(), 0).toString())));
    }

    public JPanel getMainPanel() {
        return panel1;
    }
}
