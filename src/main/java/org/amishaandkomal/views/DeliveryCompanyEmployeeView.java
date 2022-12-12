package org.amishaandkomal.views;

import org.amishaandkomal.Database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class DeliveryCompanyEmployeeView {
    private JPanel panel1;
    private JButton editInfoButton;
    private JButton logoutButton;
    private JTable deliveriesTable;
    private JButton updateStatusButton;
    private JLabel headingLabel;
    private int userId;

    public DeliveryCompanyEmployeeView(String email) {
        // get the user id from the database
        AtomicReference<String> firstName = new AtomicReference<>("");
        AtomicReference<String> sql = new AtomicReference<>("SELECT * FROM users WHERE email = ?");
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql.get());
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("id");
                firstName.set(resultSet.getString("firstname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // get the company id from the database
        String companyName = "";
        int companyId = 0;
        sql.set("SELECT delivery_company FROM employees WHERE user_id = ?");
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql.get());
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                companyId = resultSet.getInt("delivery_company");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // get the company name from the database
        sql.set("SELECT name FROM delivery_company WHERE id = ?");
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql.get());
            preparedStatement.setInt(1, companyId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                companyName = resultSet.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // set the heading label
        headingLabel.setText("Welcome " + firstName + " (" + companyName + ")");

        // set the button text
        editInfoButton.setText("Edit Info");
        logoutButton.setText("Logout");

        // set action listeners
        editInfoButton.addActionListener(e -> {
            // open the edit info dialog
            onEditInfo(userId);
            // reassign the heading
            sql.set("SELECT * FROM users WHERE id = ?");
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql.get());
                preparedStatement.setInt(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    firstName.set(resultSet.getString("firstname"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        logoutButton.addActionListener(e -> onLogout());

        configureDeliveriesPanel();
    }

    private void createDeliveriesTable() {
        String sql = "SELECT * FROM deliveries WHERE delivery_employee = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            deliveriesTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        deliveriesTable.setShowGrid(true);
        deliveriesTable.getTableHeader().setBackground(Color.RED);
        deliveriesTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onUpdateStatus(int deliveryId) {
        // get the current status
        String sql = "SELECT * FROM deliveries WHERE order_id = ?";
        String status = "";
        String newStatus = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, deliveryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                status = resultSet.getString("delivery_status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (status.equals("DELIVERED")) {
            JOptionPane.showMessageDialog(null, "This order has already been delivered.");
            return;
        }
        if (status.equals("SCHEDULED")) {
            newStatus = "PICKED_UP";
        } else if (status.equals("PICKED_UP")) {
            newStatus = "DELIVERED";
        }
        // update the order status
        sql = "UPDATE deliveries SET delivery_status = ? WHERE order_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newStatus);
            preparedStatement.setInt(2, deliveryId);
            preparedStatement.executeUpdate();
            configureDeliveriesPanel();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // if new status is delivered, update the order status to 'completed'
        if (newStatus.equals("DELIVERED")) {
            sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, "COMPLETED");
                preparedStatement.setInt(2, deliveryId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void configureDeliveriesPanel() {
        createDeliveriesTable();
        updateStatusButton.setText("Update Status");
        updateStatusButton.setEnabled(false);
        updateStatusButton.addActionListener(e -> {
            int row = deliveriesTable.getSelectedRow();
            if (row == -1) {
                return;
            }
            int deliveryId = (int) deliveriesTable.getValueAt(row, 0);
            onUpdateStatus(deliveryId);
        });

        // add listener to the table
        deliveriesTable.getSelectionModel().addListSelectionListener(e -> updateStatusButton.setEnabled(deliveriesTable.getSelectedRow() != -1));

        // add listener to the update status button
        updateStatusButton.addActionListener(e -> {
            int selectedRow = deliveriesTable.getSelectedRow();
            int deliveryId = (int) deliveriesTable.getValueAt(selectedRow, 0);
            onUpdateStatus(deliveryId);
        });
    }

    public JPanel getMainPanel() {
        return panel1;
    }
}
