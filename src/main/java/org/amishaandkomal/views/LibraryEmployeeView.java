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
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static org.amishaandkomal.utilities.EmailSender.sendEmail;
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
    private JButton lateReturnButton;
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
            // highlight the overdue rentals
            for (int i = 0; i < rentalsTable.getRowCount(); i++) {
                LocalDate dueDate = LocalDate.parse(rentalsTable.getValueAt(i, 5).toString());
                if (dueDate.isBefore(LocalDate.now())) {
                    rentalsTable.setRowSelectionInterval(i, i);
                    rentalsTable.setSelectionBackground(Color.RED);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get rentals from the database: " + e);
        }
    }

    private void configureRentalsPanel() {
        createRentalsTable();
        viewIdButton.setText("View ID");
        updateStatusButton.setText("Update Status");
        lateReturnButton.setText("Remind Return");
        viewIdButton.setEnabled(false);
        updateStatusButton.setEnabled(false);
        lateReturnButton.setEnabled(false);
        rentalsTable.getSelectionModel().addListSelectionListener(e -> {
            if (rentalsTable.getSelectedRow() != -1) {
                viewIdButton.setEnabled(true);
                updateStatusButton.setEnabled(true);
                // check if the rental is overdue
                LocalDate dueDate = LocalDate.parse(rentalsTable.getValueAt(rentalsTable.getSelectedRow(), 5).toString());
                lateReturnButton.setEnabled(dueDate.isBefore(LocalDate.now()));
            } else {
                viewIdButton.setEnabled(false);
                updateStatusButton.setEnabled(false);
                lateReturnButton.setEnabled(false);
            }
        });
        viewIdButton.addActionListener(e -> onViewId(Integer.parseInt(rentalsTable.getValueAt(rentalsTable.getSelectedRow(), 0).toString())));
        updateStatusButton.addActionListener(e -> onUpdateStatus(Integer.parseInt(rentalsTable.getValueAt(rentalsTable.getSelectedRow(), 0).toString())));
        lateReturnButton.addActionListener(e -> onRemindLateReturn(Integer.parseInt(rentalsTable.getValueAt(rentalsTable.getSelectedRow(), 0).toString()), true));
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

        // check if the return date is after the due date
        String dueDateSql = "SELECT due_date, return_date FROM rentals WHERE rent_id = " + rentId;
        LocalDate dueDate = null;
        LocalDate returnDate = null;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(dueDateSql);
            if (rs.next()) {
                dueDate = LocalDate.parse(rs.getString("due_date"));
                returnDate = LocalDate.parse(rs.getString("return_date"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get rentals from the database: " + e);
        }
        assert returnDate != null;
        if (returnDate.isAfter(dueDate)) {
            JOptionPane.showMessageDialog(null, "The book was returned after the due date. An invoice will be sent to the user.");
            // send an invoice to the user
            onRemindLateReturn(rentId, false);
        }

        configureRentalsPanel();
    }

    private void onRemindLateReturn(int rentId, boolean isReminder) {
        // get the user_id, firstname, lastname and email of the user who rented the book
        String userSql = "SELECT user_id, firstname, lastname, email FROM users, rentals WHERE users.id = rentals.user_id AND rentals.rent_id = " + rentId;
        String firstname = "";
        String lastname = "";
        String email = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(userSql);
            if (rs.next()) {
                firstname = rs.getString("firstname");
                lastname = rs.getString("lastname");
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get user details from the database: " + e);
        }

        // get the book name, library name, fine and due_date
        String bookSql = "SELECT books.name, library.name, fine, due_date FROM rentals, books, libraries WHERE rentals.isbn = books.isbn AND rentals.library_id = libraries.id AND rentals.rent_id = " + rentId;
        String bookName = "";
        String libraryName = "";
        double fine = 0;
        LocalDate dueDate = null;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(bookSql);
            if (rs.next()) {
                bookName = rs.getString("books.name");
                libraryName = rs.getString("library.name");
                fine = rs.getDouble("fine");
                dueDate = LocalDate.parse(rs.getString("due_date"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get book details from the database: " + e);
        }

        // check if the book is due
        if (LocalDate.now().isBefore(dueDate)) {
            JOptionPane.showMessageDialog(null, "The book is not due yet");
            return;
        }

        // get the number of days the book is due
        assert dueDate != null;
        int daysDue = (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        double totalFine = daysDue * fine;

        // get the employee firstname and lastname
        String employeeSql = "SELECT firstname, lastname FROM users WHERE email = " + "'" + email + "'";
        String employeeFirstname = "";
        String employeeLastname = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(employeeSql);
            if (rs.next()) {
                employeeFirstname = rs.getString("firstname");
                employeeLastname = rs.getString("lastname");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get employee details from the database: " + e);
        }

        // send email to the user
        String subject = "Reminder: The book " + bookName + " is due";
        String body = """
                Dear %s %s,
                The book %s is due at %s.
                Return data of the book was %s.
                You will have to pay a fine of $ %f (Based on rate of $ %f per day). Please return the book as soon as possible.
                
                Regards,
                %s %s
                %s
                """.formatted(firstname, lastname, bookName, libraryName, dueDate.toString(), totalFine, fine, employeeFirstname, employeeLastname, libraryName);

        if (!isReminder) {
            // get the return_date
            String returnDateSql = "SELECT return_date FROM rentals WHERE rent_id = " + rentId;
            LocalDate returnDate = null;
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                var statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(returnDateSql);
                if (rs.next()) {
                    returnDate = LocalDate.parse(rs.getString("return_date"));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not get return date from the database: " + e);
            }

            assert returnDate != null;
            if (!returnDate.isAfter(dueDate)) {
                JOptionPane.showMessageDialog(null, "The book was returned before the due date. No invoice will be sent to the user.");
                return;
            }
            subject = "Invoice: The book " + bookName + " was returned after the due date";
            body = """
                    Dear %s %s,
                    The book %s was returned after the due date.
                    Due Date of the book was %s.
                    Return data of the book was %s.
                    You will have to pay a fine of $ %f (Based on rate of $ %f per day).
                    
                    Regards,
                    %s %s
                    %s
                    """.formatted(firstname, lastname, bookName, dueDate.toString(), returnDate.toString(), totalFine, fine, employeeFirstname, employeeLastname, libraryName);
        }
        sendEmail(email, subject, body);
    }

    public JPanel getMainPanel() {
        return panel1;
    }
}
