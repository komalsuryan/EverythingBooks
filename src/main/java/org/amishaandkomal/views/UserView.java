package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.utilities.EmailSender;
import org.amishaandkomal.views.dialogs.OrderDialog;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class UserView {
    private JPanel panel1;
    private JButton editProfileButton;
    private JButton logoutButton;
    private JLabel headingLabel;
    private JTable booksTable;
    private JTextField searchTextField;
    private JLabel searchLabel;
    private JButton orderFromPubButton;
    private JButton checkBookStoresButton;
    private JButton checkLibrariesButton;
    private JTable resultsTable;
    private JButton orderFromStoreButton;
    private JButton rentFromLibraryButton;
    private JTable ordersTable;
    private int userId;

    public UserView(String email) {
        // get the user
        AtomicReference<String> sql = new AtomicReference<>("SELECT * FROM users WHERE email = ?");
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql.get());
            statement.setString(1, email);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("id");
                headingLabel.setText("Welcome, " + resultSet.getString("firstname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // assign button text
        editProfileButton.setText("Edit Profile");
        logoutButton.setText("Logout");

        // assign button action
        editProfileButton.addActionListener(e -> {
            onEditInfo(userId);
            // reassign the heading text
            sql.set("SELECT * FROM users WHERE id = ?");
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                var statement = connection.prepareStatement(sql.get());
                statement.setInt(1, userId);
                var resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    headingLabel.setText("Welcome, " + resultSet.getString("firstname"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        logoutButton.addActionListener(e -> onLogout());

        // configure panels
        configureBooksPanel();
        configureOrdersPanel();
    }

    //region Books Panel
    private void createBooksTable() {
        String sql = "SELECT * FROM books";
        TableModel tableModel = null;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            var resultSet = statement.executeQuery();
            tableModel = Objects.requireNonNull(resultSetToTableModel(resultSet));
            booksTable.setModel(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        booksTable.setRowSorter(sorter);
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                String text = searchTextField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        booksTable.setShowGrid(true);
        booksTable.getTableHeader().setBackground(Color.RED);
        booksTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onOrderFromPublisher(long isbn) {
        OrderDialog orderDialog = new OrderDialog(isbn);
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
        sql = "INSERT INTO orders (user_id, sold_by_publisher_id, isbn, quantity, delivery_needed, delivery_location, order_status, order_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, pubId);
            statement.setLong(3, isbn);
            statement.setInt(4, quantity);
            statement.setBoolean(5, orderDialog.deliverToYourPlace);
            statement.setString(6, deliveryAddress);
            statement.setString(7, "Placed");
            statement.setNull(8, java.sql.Types.INTEGER);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Order placed successfully! Order details along with delivery/pickup status will be sent to your email address.");
            createOrdersTable();
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

    private void onOrderFromStore(int store_id, long isbn) {
        // get the quantity of books available
        int availableQuantity;
        String quantitySql = "SELECT no_available FROM store_stockings WHERE store_id = ? AND isbn = ?;";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(quantitySql);
            statement.setInt(1, store_id);
            statement.setLong(2, isbn);
            ResultSet rs = statement.executeQuery();
            rs.next();
            availableQuantity = rs.getInt("no_available");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        OrderDialog orderDialog = new OrderDialog(isbn);
        orderDialog.quantitySpinner.setModel(new SpinnerNumberModel(1, 1, availableQuantity, 1));
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
        int retailPrice = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setLong(1, isbn);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                retailPrice = resultSet.getInt("retail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to order " + quantity + " copies of this book from the store for $ " + (quantity * retailPrice) + "?", "Confirm Order", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // insert into orders
        String insertSql = "INSERT INTO orders (user_id, sold_by_book_store_id, isbn, quantity, delivery_needed, delivery_location, order_status, order_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(insertSql);
            statement.setInt(1, userId);
            statement.setInt(2, store_id);
            statement.setLong(3, isbn);
            statement.setInt(4, quantity);
            statement.setBoolean(5, orderDialog.deliverToYourPlace);
            statement.setString(6, deliveryAddress);
            statement.setString(7, "Placed");
            statement.setNull(8, java.sql.Types.INTEGER);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Order placed successfully! Order details along with delivery/pickup status will be sent to your email address.");
            createOrdersTable();
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
        String decreaseQuantitySql = "UPDATE store_stockings SET no_available = ? WHERE isbn = ? AND store_id = ?;";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(decreaseQuantitySql);
            statement.setInt(1, availableQuantity - quantity);
            statement.setLong(2, isbn);
            statement.setInt(3, store_id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void onCheckBookStores(long isbn) {
        String sql = "SELECT id, name, location FROM book_store, store_stockings WHERE book_store.id = store_stockings.store_id AND isbn = ? AND no_available > 0";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setLong(1, isbn);
            var resultSet = statement.executeQuery();
            var tableModel = Objects.requireNonNull(resultSetToTableModel(resultSet));
            resultsTable.setModel(tableModel);
            resultsTable.setToolTipText("Book Stores");
            resultsTable.setShowGrid(true);
            resultsTable.getTableHeader().setBackground(Color.RED);
            resultsTable.getTableHeader().setForeground(Color.WHITE);
            resultsTable.setVisible(true);
            orderFromStoreButton.addActionListener(e -> onOrderFromStore(Integer.parseInt(resultsTable.getValueAt(resultsTable.getSelectedRow(), 0).toString()), isbn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configureBooksPanel() {
        createBooksTable();

        // assign labels
        searchLabel.setText("Search:");

        // assign button text
        orderFromPubButton.setText("Order from Publisher");
        checkBookStoresButton.setText("Check Book Stores");
        checkLibrariesButton.setText("Check Libraries");
        orderFromStoreButton.setText("Order from Store");
        rentFromLibraryButton.setText("Order from Library");

        // hide the results table and related buttons
        resultsTable.setVisible(false);
        orderFromStoreButton.setVisible(false);
        rentFromLibraryButton.setVisible(false);
        getMainPanel().revalidate();
        getMainPanel().repaint();

        // disable buttons
        orderFromPubButton.setEnabled(false);
        checkBookStoresButton.setEnabled(false);
        checkLibrariesButton.setEnabled(false);

        // enable buttons when a row is selected
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (booksTable.getSelectedRow() != -1) {
                orderFromPubButton.setEnabled(true);
                checkBookStoresButton.setEnabled(true);
                checkLibrariesButton.setEnabled(true);
            } else {
                orderFromPubButton.setEnabled(false);
                checkBookStoresButton.setEnabled(false);
                checkLibrariesButton.setEnabled(false);
                resultsTable.setVisible(false);
            }
        });

        // set the order button to visible if any store is selected
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (resultsTable.getSelectedRow() != -1) {
                if (resultsTable.getToolTipText().equals("Book Stores")) {
                    orderFromStoreButton.setVisible(true);
                    rentFromLibraryButton.setVisible(false);
                } else {
                    orderFromStoreButton.setVisible(false);
                    rentFromLibraryButton.setVisible(true);
                }
            } else {
                if (resultsTable.getToolTipText().equals("Book Stores")) {
                    orderFromStoreButton.setVisible(false);
                } else {
                    rentFromLibraryButton.setVisible(false);
                }
            }
        });

        // assign button actions
        orderFromPubButton.addActionListener(e -> onOrderFromPublisher(Long.parseLong(booksTable.getValueAt(booksTable.getSelectedRow(), 0).toString())));
        checkBookStoresButton.addActionListener(e -> onCheckBookStores(Long.parseLong(booksTable.getValueAt(booksTable.getSelectedRow(), 0).toString())));
//        checkLibrariesButton.addActionListener(e -> onCheckLibraries());
    }
    //endregion

    //region Orders Panel
    private void createOrdersTable() {
        String sql = "(SELECT orders.order_id, books.name AS 'Book', publishing_company.name AS 'Purchased From', 'Publisher' AS 'Seller type', quantity, order_status FROM orders, books, publishing_company WHERE orders.isbn = books.isbn AND sold_by_publisher_id IS NOT NULL AND sold_by_publisher_id = publishing_company.id AND user_id = ?) UNION (SELECT orders.order_id, books.name AS 'Book', book_store.name AS 'Purchased From', 'Book Store' AS 'Seller type', quantity, order_status FROM orders, books, book_store WHERE orders.isbn = books.isbn AND sold_by_book_store_id IS NOT NULL AND sold_by_book_store_id = book_store.id AND user_id = ?)";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            var resultSet = statement.executeQuery();
            var tableModel = Objects.requireNonNull(resultSetToTableModel(resultSet));
            ordersTable.setModel(tableModel);
            ordersTable.setShowGrid(true);
            ordersTable.getTableHeader().setBackground(Color.RED);
            ordersTable.getTableHeader().setForeground(Color.WHITE);
            ordersTable.setVisible(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureOrdersPanel() {
        createOrdersTable();
    }

    public JPanel getMainPanel() {
        return panel1;
    }
}
