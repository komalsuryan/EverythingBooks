package org.amishaandkomal.views;

import org.amishaandkomal.Database;
import org.amishaandkomal.views.dialogs.AddEditBookStoreDialog;
import org.amishaandkomal.views.dialogs.AddEditDeliveryCompanyDialog;
import org.amishaandkomal.views.dialogs.AddEditLibraryDialog;
import org.amishaandkomal.views.dialogs.AddEditPublisherDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.amishaandkomal.utilities.FrequentGuiMethods.*;

public class AdminView {

    private JButton editInfoButton;
    private JLabel headingLabel;
    private JButton logoutButton;
    private JPanel mainPanel;
    private JTable usersTable;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton addUserButton;
    private JTable publishersTable;
    private JButton editPublisherButton;
    private JButton deletePublisherButton;
    private JButton addPublisherButton;
    private JButton editBookStore;
    private JButton deleteBookStore;
    private JButton addBookStore;
    private JTable librariesTable;
    private JButton editLibrary;
    private JButton deleteLibrary;
    private JButton addLibrary;
    private JTable bookStoresTable;
    private JTable deliveryCompaniesTable;
    private JButton editDeliveryCompany;
    private JButton deleteDeliveryCompany;
    private JButton addDeliveryCompany;
    private int adminId;

    public AdminView(String email) {
        // assign heading text
        AtomicReference<String> sql = new AtomicReference<>("SELECT * FROM users WHERE email = '" + email + "'");
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql.get());
            if (resultSet.next()) {
                headingLabel.setText("Welcome, " + resultSet.getString("firstname"));
                adminId = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // set the edit info button label
        editInfoButton.setText("Edit Profile");
        logoutButton.setText("Logout");

        // assign button action
        editInfoButton.addActionListener(e -> {
            onEditInfo(adminId);
            // reassigning the heading text
            sql.set("SELECT * FROM users WHERE email = '" + email + "'");
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql.get());
                if (resultSet.next()) {
                    headingLabel.setText("Welcome, " + resultSet.getString("firstname"));
                    adminId = resultSet.getInt("id");
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            createUsersTable();
        });
        logoutButton.addActionListener(e -> onLogout());

        // configure the panels
        configurePublishersPanel();
        configureBookStorePanel();
        configureLibrariesPanel();
        configureDeliveryCompaniesPanel();
        configureUsersPanel();
    }

    //region Publishers Panel
    private void createPublishersTable() {
        // assign table data
        String sql = "SELECT publishing_company.id, name, warehouse_location, firstname, lastname FROM publishing_company, users WHERE publishing_company.admin_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            publishersTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        publishersTable.setShowGrid(true);
        publishersTable.getTableHeader().setBackground(Color.RED);
        publishersTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditPublisher(int id) {
        AddEditPublisherDialog addEditPublisherDialog = new AddEditPublisherDialog(true, id);
        addEditPublisherDialog.pack();
        addEditPublisherDialog.setVisible(true);
        createPublishersTable();
    }

    private void onDeletePublisher(int id) {
        // get the publisher admin id
        String sql = "SELECT admin_id FROM publishing_company WHERE id = " + id;
        int adminId = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                adminId = resultSet.getInt("admin_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the publisher
        sql = "DELETE FROM publishing_company WHERE id = " + id;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the publisher admin role
        sql = "DELETE FROM user_roles WHERE user_id = " + adminId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createPublishersTable();
    }

    private void onAddPublisher() {
        AddEditPublisherDialog addEditPublisherDialog = new AddEditPublisherDialog(false, -1);
        addEditPublisherDialog.pack();
        addEditPublisherDialog.setVisible(true);
        createPublishersTable();
    }

    private void configurePublishersPanel() {
        // create the table
        createPublishersTable();

        editPublisherButton.setText("Edit Publisher");
        deletePublisherButton.setText("Delete Publisher");
        addPublisherButton.setText("Add Publisher");

        // disable the edit and delete buttons
        editPublisherButton.setEnabled(false);
        deletePublisherButton.setEnabled(false);

        // if any row is selected, enable the edit and delete buttons
        publishersTable.getSelectionModel().addListSelectionListener(e -> {
            if (publishersTable.getSelectedRow() != -1) {
                editPublisherButton.setEnabled(true);
                deletePublisherButton.setEnabled(true);
            } else {
                editPublisherButton.setEnabled(false);
                deletePublisherButton.setEnabled(false);
            }
        });

        // assign button actions
        editPublisherButton.addActionListener(e -> {
            int row = publishersTable.getSelectedRow();
            if (row != -1) {
                int id = (int) publishersTable.getValueAt(row, 0);
                onEditPublisher(id);
            }
        });
        deletePublisherButton.addActionListener(e -> {
            int row = publishersTable.getSelectedRow();
            if (row != -1) {
                int id = (int) publishersTable.getValueAt(row, 0);
                onDeletePublisher(id);
            }
        });
        addPublisherButton.addActionListener(e -> onAddPublisher());
    }
    //endregion

    //region Book Stores Panel
    private void createBookStoresTable() {
        // assign table data
        String sql = "SELECT book_store.id, name, location, firstname, lastname FROM book_store, users WHERE book_store.admin_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            bookStoresTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        bookStoresTable.setShowGrid(true);
        bookStoresTable.getTableHeader().setBackground(Color.RED);
        bookStoresTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditBookStore(int id) {
        AddEditBookStoreDialog addEditBookStoreDialog = new AddEditBookStoreDialog(true, id);
        addEditBookStoreDialog.pack();
        addEditBookStoreDialog.setVisible(true);
        createBookStoresTable();
    }

    private void onDeleteBookStore(int id) {
        // get the book store admin id
        String sql = "SELECT admin_id FROM book_store WHERE id = " + id;
        int adminId = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                adminId = resultSet.getInt("admin_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the publisher
        sql = "DELETE FROM book_store WHERE id = " + id;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the publisher admin role
        sql = "DELETE FROM user_roles WHERE user_id = " + adminId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createBookStoresTable();
    }

    private void onAddBookStore() {
        AddEditBookStoreDialog addEditBookStoreDialog = new AddEditBookStoreDialog(false, -1);
        addEditBookStoreDialog.pack();
        addEditBookStoreDialog.setVisible(true);
        createBookStoresTable();
    }

    private void configureBookStorePanel() {
        // create the table
        createBookStoresTable();

        editBookStore.setText("Edit Book Store");
        deleteBookStore.setText("Delete Book Store");
        addBookStore.setText("Add Book Store");

        // disable the edit and delete buttons
        editBookStore.setEnabled(false);
        deleteBookStore.setEnabled(false);

        // if any row is selected, enable the edit and delete buttons
        bookStoresTable.getSelectionModel().addListSelectionListener(e -> {
            if (bookStoresTable.getSelectedRow() != -1) {
                editBookStore.setEnabled(true);
                deleteBookStore.setEnabled(true);
            } else {
                editBookStore.setEnabled(false);
                deleteBookStore.setEnabled(false);
            }
        });

        // assign button actions
        editBookStore.addActionListener(e -> {
            int row = bookStoresTable.getSelectedRow();
            if (row != -1) {
                int id = (int) bookStoresTable.getValueAt(row, 0);
                onEditBookStore(id);
            }
        });
        deleteBookStore.addActionListener(e -> {
            int row = bookStoresTable.getSelectedRow();
            if (row != -1) {
                int id = (int) bookStoresTable.getValueAt(row, 0);
                onDeleteBookStore(id);
            }
        });
        addBookStore.addActionListener(e -> onAddBookStore());
    }
    //endregion

    //region libraries Panel
    private void createLibrariesTable() {
        // assign table data
        String sql = "SELECT library.id, name, location, rent_period, fine, firstname, lastname FROM library, users WHERE library.admin_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            librariesTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        librariesTable.setShowGrid(true);
        librariesTable.getTableHeader().setBackground(Color.RED);
        librariesTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditLibrary(int id) {
        AddEditLibraryDialog addEditLbraryDialog = new AddEditLibraryDialog(true, id);
        addEditLbraryDialog.pack();
        addEditLbraryDialog.setVisible(true);
        createLibrariesTable();
    }

    private void onDeleteLibrary(int id) {
        // get the library admin id
        String sql = "SELECT admin_id FROM library WHERE id = " + id;
        int adminId = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                adminId = resultSet.getInt("admin_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the library
        sql = "DELETE FROM library WHERE id = " + id;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the library admin role
        sql = "DELETE FROM user_roles WHERE user_id = " + adminId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createLibrariesTable();
    }

    private void onAddLibrary() {
        AddEditLibraryDialog addEditLibraryDialog = new AddEditLibraryDialog(false, -1);
        addEditLibraryDialog.pack();
        addEditLibraryDialog.setVisible(true);
        createLibrariesTable();
    }

    private void configureLibrariesPanel() {
        // create the table
        createLibrariesTable();

        editLibrary.setText("Edit library");
        deleteLibrary.setText("Delete library");
        addLibrary.setText("Add library");

        // disable the edit and delete buttons
        editLibrary.setEnabled(false);
        deleteLibrary.setEnabled(false);

        // if any row is selected, enable the edit and delete buttons
        librariesTable.getSelectionModel().addListSelectionListener(e -> {
            if (librariesTable.getSelectedRow() != -1) {
                editLibrary.setEnabled(true);
                deleteLibrary.setEnabled(true);
            } else {
                editLibrary.setEnabled(false);
                deleteLibrary.setEnabled(false);
            }
        });

        // assign button actions
        editLibrary.addActionListener(e -> {
            int row = librariesTable.getSelectedRow();
            if (row != -1) {
                int id = (int) librariesTable.getValueAt(row, 0);
                onEditLibrary(id);
            }
        });
        deleteLibrary.addActionListener(e -> {
            int row = librariesTable.getSelectedRow();
            if (row != -1) {
                int id = (int) librariesTable.getValueAt(row, 0);
                onDeleteLibrary(id);
            }
        });
        addLibrary.addActionListener(e -> onAddLibrary());
    }
    //endregion


    //region Users Panel
    private void createUsersTable() {
        // assign table data
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            usersTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // show table border
        usersTable.setShowGrid(true);

        // change the color of the table header
        usersTable.getTableHeader().setBackground(Color.RED);
        usersTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditUser(int userId) {
        SignUpView signUpView = new SignUpView(true, true, userId);
        signUpView.pack();
        signUpView.setVisible(true);
        createUsersTable();
    }

    private void onDeleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = " + userId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            JOptionPane.showMessageDialog(null, "User deleted successfully");
            createUsersTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onAddUser() {
        SignUpView signUpView = new SignUpView(true, false, adminId);
        signUpView.pack();
        signUpView.setVisible(true);
        createUsersTable();
    }

    private void configureUsersPanel() {
        // configure the users table
        createUsersTable();

        // set the button labels
        editUserButton.setText("Edit User");
        deleteUserButton.setText("Delete User");
        addUserButton.setText("Add User");

        // disable the edit and delete buttons
        editUserButton.setEnabled(false);
        deleteUserButton.setEnabled(false);

        // if any row is selected, enable the edit and delete buttons
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (usersTable.getSelectedRow() != -1) {
                editUserButton.setEnabled(true);
                deleteUserButton.setEnabled(true);
            } else {
                editUserButton.setEnabled(false);
                deleteUserButton.setEnabled(false);
            }
        });

        editUserButton.addActionListener(e -> onEditUser(Integer.parseInt(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString())));
        deleteUserButton.addActionListener(e -> onDeleteUser(Integer.parseInt(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString())));
        addUserButton.addActionListener(e -> onAddUser());
    }
    //endregion

    //region Delivery Companies Panel
    private void createDeliveryCompaniesTable() {
        // assign table data
        String sql = "SELECT delivery_company.id, name, delivery_type, firstname, lastname FROM delivery_company, users WHERE delivery_company.admin_id = users.id";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            deliveryCompaniesTable.setModel(Objects.requireNonNull(resultSetToTableModel(resultSet)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        deliveryCompaniesTable.setShowGrid(true);
        deliveryCompaniesTable.getTableHeader().setBackground(Color.RED);
        deliveryCompaniesTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void onEditDelivery(int id) {
        AddEditDeliveryCompanyDialog addEditDeliveryCompanyDialog = new AddEditDeliveryCompanyDialog(true, id);
        addEditDeliveryCompanyDialog.pack();
        addEditDeliveryCompanyDialog.setVisible(true);
        createDeliveryCompaniesTable();
    }

    private void onDeleteDelivery(int id) {
        // get the library admin id
        String sql = "SELECT admin_id FROM delivery_company WHERE id = " + id;
        int adminId = 0;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                adminId = resultSet.getInt("admin_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the delivery_company
        sql = "DELETE FROM delivery_company WHERE id = " + id;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete the delivery_company admin role
        sql = "DELETE FROM user_roles WHERE user_id = " + adminId;
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createDeliveryCompaniesTable();
    }

    private void onAddDelivery() {
        AddEditDeliveryCompanyDialog addEditDeliveryDialog = new AddEditDeliveryCompanyDialog(false, -1);
        addEditDeliveryDialog.pack();
        addEditDeliveryDialog.setVisible(true);
        createDeliveryCompaniesTable();
    }

    private void configureDeliveryCompaniesPanel() {
        // create the table
        createDeliveryCompaniesTable();

        editDeliveryCompany.setText("Edit Delivery Company");
        deleteDeliveryCompany.setText("Delete Delivery Company");
        addDeliveryCompany.setText("Add Delivery Company");

        // disable the edit and delete buttons
        editDeliveryCompany.setEnabled(false);
        deleteDeliveryCompany.setEnabled(false);

        // if any row is selected, enable the edit and delete buttons
        deliveryCompaniesTable.getSelectionModel().addListSelectionListener(e -> {
            if (deliveryCompaniesTable.getSelectedRow() != -1) {
                editDeliveryCompany.setEnabled(true);
                deleteDeliveryCompany.setEnabled(true);
            } else {
                editDeliveryCompany.setEnabled(false);
                deleteDeliveryCompany.setEnabled(false);
            }
        });

        // assign button actions
        editDeliveryCompany.addActionListener(e -> {
            int row = deliveryCompaniesTable.getSelectedRow();
            if (row != -1) {
                int id = (int) deliveryCompaniesTable.getValueAt(row, 0);
                onEditDelivery(id);
            }
        });
        deleteDeliveryCompany.addActionListener(e -> {
            int row = deliveryCompaniesTable.getSelectedRow();
            if (row != -1) {
                int id = (int) deliveryCompaniesTable.getValueAt(row, 0);
                onDeleteDelivery(id);
            }
        });
        addDeliveryCompany.addActionListener(e -> onAddDelivery());
    }
//endregion


    public JPanel getMainPanel() {
        return mainPanel;
    }
}
