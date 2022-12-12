package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.views.dialogs.ScheduleDeliveryDialog;

import javax.swing.*;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class BookStoreEmployeeView {
    private JButton editInfoButton;
    private JButton logoutButton;
    private JPanel jPanel1;
    private JLabel headingLabel;
    private JTable ordersTable;
    private JButton processOrderButton;
    private JButton completeOrderButton;
    private JButton scheduleDeliveryButton;
    private int userID;
    private int companyID;

    public BookStoreEmployeeView(String email) {
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
        String companyIDQuery = "SELECT book_store FROM employees WHERE user_id = " + userID;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(companyIDQuery);
            rs.next();
            companyID = rs.getInt("book_store");
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
        String companyNameQuery = "SELECT name FROM book_store WHERE id = " + companyID;
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
        configureOrdersPanel();
    }

    // region Sales panel
    private void createOrdersTable() {
        String sql = "SELECT orders.order_id, books.name AS 'Book', orders.user_id, quantity, order_status, delivery_needed, delivery_location FROM orders, books, book_store WHERE orders.isbn = books.isbn AND user_id IS NOT NULL AND sold_by_book_store_id = ?";
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
        configureOrdersPanel();
    }

    private void onScheduleDelivery(int orderId) {
        // set the pickup location as the bookstore's address
        String pickupLocationSql = "SELECT location FROM book_store WHERE id = ?";
        ResultSet rs;
        String pickupLocation = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(pickupLocationSql);
            statement.setInt(1, companyID);
            rs = statement.executeQuery();
            if (rs.next()) {
                pickupLocation = rs.getString("location");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // get the delivery location
        String deliveryLocationSql = "SELECT delivery_location FROM orders WHERE order_id = ?";
        String deliveryLocation = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(deliveryLocationSql);
            statement.setInt(1, orderId);
            rs = statement.executeQuery();
            if (rs.next()) {
                deliveryLocation = rs.getString("delivery_location");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // show the delivery schedule dialog
        ScheduleDeliveryDialog scheduleDeliveryDialog = new ScheduleDeliveryDialog(pickupLocation, deliveryLocation);
        scheduleDeliveryDialog.pack();
        scheduleDeliveryDialog.setVisible(true);
        if (scheduleDeliveryDialog.isCancelled) {
            return;
        }

        // get the pickup timestamp
        Timestamp pickupTimestamp = scheduleDeliveryDialog.pickupDateTime;
        // if pickup timestamp is less than current timestamp + 1 hour, ask the user to select a different time
        if (pickupTimestamp.before(new Timestamp(System.currentTimeMillis() + 3600000))) {
            JOptionPane.showMessageDialog(null, "Pickup time must be at least 1 hour from now.");
            return;
        }

        // get all the employees who can deliver
        String employeeSql = "SELECT employees.user_id FROM employees WHERE employees.delivery_company IS NOT NULL AND employees.user_id NOT IN (SELECT deliveries.delivery_employee FROM deliveries WHERE deliveries.pickup_time < CURRENT_TIMESTAMP AND NOT(deliveries.delivery_status = 'COMPLETE')) LIMIT 1";
        int employeeId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(employeeSql);
            rs = statement.executeQuery();
            if (rs.next()) {
                employeeId = rs.getInt("user_id");
            } else {
                JOptionPane.showMessageDialog(null, "No employees available to deliver at this time.");
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // create the delivery
        String deliverySql = "INSERT INTO deliveries (order_id, delivery_employee, pickup_location, pickup_time, delivery_location, delivery_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(deliverySql);
            statement.setInt(1, orderId);
            statement.setInt(2, employeeId);
            statement.setString(3, pickupLocation);
            statement.setTimestamp(4, pickupTimestamp);
            statement.setString(5, deliveryLocation);
            statement.setString(6, "SCHEDULED");
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Delivery scheduled successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Error in scheduling delivery: " + e);
        }
    }

    private void onCompleteOrder(int orderId) {
        // get the current status of the order
        String orderStatusSql = "SELECT * FROM orders WHERE order_id = ?";
        ResultSet rs;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(orderStatusSql);
            statement.setInt(1, orderId);
            rs = statement.executeQuery();
            rs.next();
            if (!rs.getString("order_status").equalsIgnoreCase("PROCESSED")) {
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String statusUpdateSql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(statusUpdateSql);
            statement.setString(1, "COMPLETED");
            statement.setInt(2, orderId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        configureOrdersPanel();
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
            if (ordersTable.getSelectedRow() != -1) {
                processOrderButton.setEnabled(true);
                if (ordersTable.getValueAt(ordersTable.getSelectedRow(), 5).toString().equalsIgnoreCase("true")) {
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
        scheduleDeliveryButton.addActionListener(e -> onScheduleDelivery(Integer.parseInt(ordersTable.getValueAt(ordersTable.getSelectedRow(), 0).toString())));
        completeOrderButton.addActionListener(e -> onCompleteOrder(Integer.parseInt(ordersTable.getValueAt(ordersTable.getSelectedRow(), 0).toString())));
    }

    public JPanel getMainPanel() {
        return jPanel1;
    }
}
