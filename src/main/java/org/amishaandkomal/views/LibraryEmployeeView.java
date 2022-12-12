package org.amishaandkomal.views;

import org.amishaandkomal.Database;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;
import static org.amishaandkomal.utilities.ImageOperations.getScaledImageURL;

public class LibraryEmployeeView {
    private JPanel panel1;
    private JButton editInfoButton;
    private JButton logoutButton;
    private JLabel headingLabel;
    private JTable rentalsTable;
    private JButton viewIdButton;
    private JButton updateStatusButton;
    private int libraryId;

    public LibraryEmployeeView(String email) {
        // get the userId from the email
        String userSql = "SELECT * FROM users WHERE email = '" + email + "'";
        int userId = 0;
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
        String libSql = "SELECT library FROM employees WHERE user_id = " + userId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(libSql);
            if (rs.next()) {
                libraryId = rs.getInt("library");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library details from the database: " + e);
        }

        // set the labels
        headingLabel.setText(getHeadingLabel(userId));

        // set the buttons text
        editInfoButton.setText("Edit My Info");
        logoutButton.setText("Logout");

        // set the action listeners
        int finalUserId = userId;
        editInfoButton.addActionListener(e -> {
            onEditInfo(finalUserId);
            headingLabel.setText(getHeadingLabel(finalUserId));
        });
        logoutButton.addActionListener(e -> onLogout());

        configureRentalsPanel();
    }

    private String getHeadingLabel(int userId) {
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
        String libNameSql = "SELECT name FROM library WHERE id = " + libraryId;
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

    //region rentals panel
    private void createRentalsTable() {
        String sql = "SELECT * FROM rentals WHERE library_id = " + libraryId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            rentalsTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
            rentalsTable.setShowGrid(true);
            rentalsTable.getTableHeader().setBackground(Color.RED);
            rentalsTable.getTableHeader().setForeground(Color.WHITE);
        } catch (SQLException e) {
            throw new RuntimeException("Could not get rentals from the database: " + e);
        }
    }

    private void configureRentalsPanel() {
        createRentalsTable();
        viewIdButton.setText("View ID");
        updateStatusButton.setText("Update Status");
        viewIdButton.setEnabled(false);
        updateStatusButton.setEnabled(false);
        rentalsTable.getSelectionModel().addListSelectionListener(e -> {
            if (rentalsTable.getSelectedRow() != -1) {
                viewIdButton.setEnabled(true);
                updateStatusButton.setEnabled(true);
            } else {
                viewIdButton.setEnabled(false);
                updateStatusButton.setEnabled(false);
            }
        });
        viewIdButton.addActionListener(e -> onViewId(Integer.parseInt(rentalsTable.getValueAt(rentalsTable.getSelectedRow(), 0).toString())));
        updateStatusButton.addActionListener(e -> onUpdateStatus(Integer.parseInt(rentalsTable.getValueAt(rentalsTable.getSelectedRow(), 0).toString())));
    }

    private void onViewId(int rentId) {
        String idImageLocationSql = "SELECT id_image_location FROM rentals WHERE rent_id = " + rentId;
        String idImageLocation = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(idImageLocationSql);
            if (rs.next()) {
                idImageLocation = rs.getString("id_image_location");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get rentals from the database: " + e);
        }
        if (idImageLocation.equals("")) {
            JOptionPane.showMessageDialog(null, "No ID image found for this rental");
            return;
        }
        URL url;
        try {
            url = new URL(getScaledImageURL(idImageLocation));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        BufferedImage image;
        try {
            image = ImageIO.read(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(image)));
    }

    private void onUpdateStatus(int rentId) {
        // get the current status
        String statusSql = "SELECT status FROM rentals WHERE rent_id = " + rentId;
        String status = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(statusSql);
            if (rs.next()) {
                status = rs.getString("status");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get rentals from the database: " + e);
        }
        String updateStatusSql;
        String updateStockSql;
        if (status.equalsIgnoreCase("REQUESTED")) {
            // get the rent_period
            String rentPeriodSql = "SELECT rent_period FROM library WHERE id = " + libraryId;
            int rentPeriod = 0;
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                var statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(rentPeriodSql);
                if (rs.next()) {
                    rentPeriod = rs.getInt("rent_period");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not get rentals from the database: " + e);
            }
            // update the status to RENTED and set issue_date to current date and due_date to rent_period days from current date
            updateStatusSql = "UPDATE rentals SET status = 'RENTED', issue_date = '%s', due_date = '%s' WHERE rent_id = %d".formatted(
                    LocalDate.now().toString(),
                    LocalDate.now().plusDays(rentPeriod).toString(),
                    rentId
            );
            updateStockSql = "UPDATE library_stockings, rentals SET library_stockings.no_available = library_stockings.no_available - 1 WHERE rentals.isbn = library_stockings.isbn AND library_stockings.library_id = rentals.library_id AND rentals.rent_id = " + rentId;
        } else if (status.equals("RENTED")) {
            // update the status to RETURNED and set return_date to current date
            updateStatusSql = "UPDATE rentals SET status = 'RETURNED', return_date = '%s' WHERE rent_id = %d".formatted(
                    LocalDate.now().toString(),
                    rentId
            );
            updateStockSql = "UPDATE library_stockings, rentals SET library_stockings.no_available = library_stockings.no_available + 1 WHERE rentals.isbn = library_stockings.isbn AND library_stockings.library_id = rentals.library_id AND rentals.rent_id = " + rentId;
        } else {
            JOptionPane.showMessageDialog(null, "Cannot update status of a returned book");
            return;
        }
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            statement.addBatch(updateStatusSql);
            statement.addBatch(updateStockSql);
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update the database: " + e);
        }
        configureRentalsPanel();
    }

    public JPanel getMainPanel() {
        return panel1;
    }
}
