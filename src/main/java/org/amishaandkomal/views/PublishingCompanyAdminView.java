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

    public JPanel getMainPanel() {
        return panel1;
    }
}
