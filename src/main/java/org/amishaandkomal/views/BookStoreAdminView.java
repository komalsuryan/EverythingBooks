package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.utilities.EmailSender;
import org.amishaandkomal.views.dialogs.AddEditBookStoreDialog;
import org.amishaandkomal.views.dialogs.AddEditStockDialog;
import org.amishaandkomal.views.dialogs.OrderDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class BookStoreAdminView {
    private JButton editBookStoreButton;
    private JPanel panel1;
    private JLabel headingLabel;
    private JButton logoutButton;
    private JButton editInfoButton;
    private JTable employeesTable;
    private JButton addEmployeeButton;
    private JButton removeEmployeeButton;
    private JComboBox<String> addEmployeeFromUsersComboBox;
    private JButton addEmployeeFromUsersButton;
    private JTable booksTable;
    private JButton purchaseBookButton;
    private JLabel purchaseBookLabel;
    private JComboBox<String> selectBookComboBox;
    private JButton editStockButton;
    private JButton addStockButton;
    private JTable purchasesTable;
    private JTable salesTable;
    private int storeId;
    private int userId;

    public BookStoreAdminView(String email) {
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
            throw new RuntimeException("Could not get book store employee details from the database: " + e);
        }

        // get the storeId from the userId
        String storeSql = "SELECT id FROM book_store WHERE admin_id = " + userId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(storeSql);
            if (rs.next()) {
                storeId = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library details from the database: " + e);
        }

        // set the labels
        headingLabel.setText(getHeadingLabel());

        // set the buttons text
        editBookStoreButton.setText("Edit Book Store Info");
        editInfoButton.setText("Edit My Info");
        logoutButton.setText("Logout");

        // set the action listeners
        editBookStoreButton.addActionListener(e -> onEditBookStore());
        editInfoButton.addActionListener(e -> {
            onEditInfo(userId);
            headingLabel.setText(getHeadingLabel());
        });
        logoutButton.addActionListener(e -> onLogout());

        configureEmployeesPanel();
        configureBooksPanel();
        configurePurchasesPanel();
        configureSalesPanel();
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
            throw new RuntimeException("Could not get book store employee details from the database: " + e);
        }
        // get the library name from the libraryId
        String storeNameSql = "SELECT name FROM book_store WHERE id = " + storeId;
        String storeName = "";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(storeNameSql);
            if (rs.next()) {
                storeName = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get library employee details from the database: " + e);
        }
        return "Welcome %s (%s)".formatted(firstName, storeName);
    }

    private void onEditBookStore() {
        AddEditBookStoreDialog dialog = new AddEditBookStoreDialog(true, storeId);
        dialog.adminComboBox.setEnabled(false);
        dialog.pack();
        dialog.setVisible(true);
        headingLabel.setText(getHeadingLabel());
    }

    // region Employee Panel
    private void createEmployeesTable() {
        String employeesSql = "SELECT users.id, users.firstname, users.lastname, users.email FROM employees, users WHERE book_store = ? AND employees.user_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(employeesSql);
            statement.setInt(1, storeId);
            ResultSet rs = statement.executeQuery();
            employeesTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
        } catch (SQLException e) {
            throw new RuntimeException("Could not get book store employee details from the database: " + e);
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
        String addEmployeeSql = "INSERT INTO employees (user_id, book_store) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addEmployeeSql);
            statement.setInt(1, userId);
            statement.setInt(2, storeId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee to the database: " + e);
        }
        // add the user to the user_roles table
        String addUserRolesSql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(addUserRolesSql);
            statement.setInt(1, userId);
            statement.setString(2, "BS_EMPLOYEE");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add employee role to the database: " + e);
        }
    }
    // endregion

    // region Books Panel
    private void createBooksTable() {
        String booksSql = "SELECT books.isbn, books.name, books.edition, books.author_name, publishing_company.name AS 'Publisher', books.genre, store_stockings.no_available AS 'Copies Available', books.retail AS 'Retail Price' FROM books, store_stockings, book_store, publishing_company WHERE books.isbn = store_stockings.isbn AND publishing_company.id = books.pub_id AND book_store.id = store_stockings.store_id AND book_store.id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(booksSql);
            statement.setInt(1, storeId);
            ResultSet rs = statement.executeQuery();
            booksTable.setModel(Objects.requireNonNull(resultSetToTableModel(rs)));
            booksTable.setShowGrid(true);
            booksTable.getTableHeader().setBackground(Color.RED);
            booksTable.getTableHeader().setForeground(Color.WHITE);
        } catch (SQLException e) {
            throw new RuntimeException("Could not get books from the database: " + e);
        }
    }

    private void configureBooksPanel() {
        createBooksTable();

        // set the label
        purchaseBookLabel.setText("Purchase Book");

        // set the button texts
        editStockButton.setText("Edit Stock");
        purchaseBookButton.setText("Purchase Book");
        addStockButton.setText("Add Stock");

        editStockButton.setEnabled(false);
        purchaseBookButton.setEnabled(false);

        // populate the add book combo box
        selectBookComboBox.removeAllItems();
        selectBookComboBox.addItem("Select Book");
        String booksSql = "SELECT * FROM books";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(booksSql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                selectBookComboBox.addItem(rs.getString("isbn") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get books from the database: " + e);
        }
        selectBookComboBox.setSelectedIndex(0);

        // add listener to books table
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (booksTable.getSelectedRow() != -1) {
                editStockButton.setEnabled(true);
                selectBookComboBox.setSelectedItem(booksTable.getValueAt(booksTable.getSelectedRow(), 0) + " - " + booksTable.getValueAt(booksTable.getSelectedRow(), 1));
            } else {
                editStockButton.setEnabled(false);
                selectBookComboBox.setSelectedIndex(0);
            }
        });

        // add listener to select book combo box
        selectBookComboBox.addActionListener(e -> purchaseBookButton.setEnabled(selectBookComboBox.getSelectedIndex() != 0));

        // add listener to edit stock button
        editStockButton.addActionListener(e -> onEditStock(Long.parseLong(Objects.requireNonNull(booksTable.getValueAt(booksTable.getSelectedRow(), 0)).toString())));
        purchaseBookButton.addActionListener(e -> onPurchaseBook(Long.parseLong(Objects.requireNonNull(selectBookComboBox.getSelectedItem()).toString().split(" - ")[0])));
        addStockButton.addActionListener(e -> onAddStock());
    }

    private void onEditStock(long isbn) {
        // get the current stock and available for rent
        String stockSql = "SELECT no_available FROM store_stockings WHERE isbn = ? AND store_id = ?";
        int stock = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(stockSql);
            statement.setLong(1, isbn);
            statement.setInt(2, storeId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                stock = rs.getInt("no_available");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get stock from the database: " + e);
        }
        // show the edit stock dialog
        AddEditStockDialog dialog = new AddEditStockDialog(true, isbn, stock, false);
        dialog.rentableLabel.setVisible(false);
        dialog.rentableCheckBox.setVisible(false);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (dialog.isCancelled) {
            return;
        }

        // update the stock
        String updateStockSql = "UPDATE store_stockings SET no_available = ? WHERE isbn = ? AND store_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(updateStockSql);
            statement.setInt(1, dialog.quantity);
            statement.setLong(2, isbn);
            statement.setInt(3, storeId);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Stock updated successfully!");
            configureBooksPanel();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update stock in the database: " + e);
        }
    }

    private void onPurchaseBook(long isbn) {
        OrderDialog orderDialog = new OrderDialog(isbn);
        orderDialog.deliverToYourPlaceCheckBox.setSelected(true);
        orderDialog.deliveryLocationTextField.setEnabled(true);
        // get location from library_id
        String locationSql = "SELECT location FROM book_store WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(locationSql);
            statement.setInt(1, storeId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                orderDialog.deliveryLocationTextField.setText(rs.getString("location"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get location from the database: " + e);
        }
        orderDialog.pack();
        orderDialog.setLocationRelativeTo(null);
        orderDialog.setVisible(true);
        if (orderDialog.isCancelled) {
            return;
        }
        int quantity = orderDialog.quantity;
        String deliveryAddress = orderDialog.deliverToYourPlace ? orderDialog.deliveryLocation : null;
        // get the publisher id
        String sql = "SELECT * FROM books WHERE isbn = ?";
        int pubId = 0;
        int retailPrice = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setLong(1, isbn);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                pubId = resultSet.getInt("pub_id");
                retailPrice = resultSet.getInt("retail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // show a dialog to confirm the order
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to order " + quantity + " copies of this book from the publisher for $ " + (quantity * retailPrice) + "?", "Confirm Order", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // insert into orders
        sql = "INSERT INTO orders (book_store_id, sold_by_publisher_id, isbn, quantity, delivery_needed, delivery_location, order_status, order_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, storeId);
            statement.setInt(2, pubId);
            statement.setLong(3, isbn);
            statement.setInt(4, quantity);
            statement.setBoolean(5, orderDialog.deliverToYourPlace);
            statement.setString(6, deliveryAddress);
            statement.setString(7, "Placed");
            statement.setNull(8, java.sql.Types.INTEGER);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Order placed successfully! Order details along with delivery/pickup status will be sent to your email address.");
            String email = null;
            sql = "SELECT * FROM users WHERE id = ?";
            try (Connection connection1 = DriverManager.getConnection(Database.databaseUrl)) {
                var statement1 = connection1.prepareStatement(sql);
                statement1.setInt(1, userId);
                var resultSet = statement1.executeQuery();
                if (resultSet.next()) {
                    email = resultSet.getString("email");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            EmailSender.sendEmail(email, "Order Placed", "Your order for " + quantity + " copies of " + isbn + " has been placed successfully. You will be notified when the order is ready for pickup/delivery.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error placing order! Detailed Message: " + e.getMessage());
        }
    }

    private void onAddStock() {
        AddEditStockDialog dialog = new AddEditStockDialog(false, 0, 0, false);
        dialog.selectBookComboBox.removeAllItems();
        dialog.selectBookComboBox.addItem("Select a book");
        // populate the combo box with books that are not already stocked
        String sql = "SELECT * FROM books WHERE isbn NOT IN (SELECT isbn FROM store_stockings WHERE store_id = ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, storeId);
            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dialog.selectBookComboBox.addItem(resultSet.getLong("isbn") + " - " + resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (dialog.isCancelled) {
            return;
        }

        // insert into store_stockings
        String insertStockSql = "INSERT INTO store_stockings (isbn, store_id, no_available) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(insertStockSql);
            statement.setLong(1, dialog.isbn);
            statement.setInt(2, storeId);
            statement.setInt(3, dialog.quantity);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Stock added successfully!");
            configureBooksPanel();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add stock to the database: " + e);
        }
    }
    // endregion

    // region Purchase Panel
    private void createPurchasesTable () {
        String sql = "SELECT orders.order_id, books.name AS 'Book', publishing_company.name AS 'Publisher', quantity, order_status FROM orders, books, publishing_company WHERE orders.isbn = books.isbn AND sold_by_publisher_id = publishing_company.id AND orders.book_store_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, storeId);
            var resultSet = statement.executeQuery();
            purchasesTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
            purchasesTable.setShowGrid(true);
            purchasesTable.getTableHeader().setBackground(Color.RED);
            purchasesTable.getTableHeader().setForeground(Color.WHITE);
        } catch (SQLException e) {
            throw new RuntimeException("Could not get purchases from the database: " + e);
        }
    }

    private void configurePurchasesPanel () {
        createPurchasesTable();
    }
    // endregion

    // region Sales Panel
    private void createOrdersTable() {
        String sql = "SELECT DISTINCT(orders.order_id), books.name AS 'Book', orders.user_id, quantity, order_status, delivery_needed, delivery_location FROM orders, books, book_store WHERE orders.isbn = books.isbn AND user_id IS NOT NULL AND sold_by_book_store_id = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, storeId);
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
