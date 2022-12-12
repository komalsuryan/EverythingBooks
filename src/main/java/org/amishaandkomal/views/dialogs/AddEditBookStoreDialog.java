package org.amishaandkomal.views.dialogs;

import org.amishaandkomal.Database;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.Objects;

public class AddEditBookStoreDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel headingLabel;
    private JLabel nameLabel;
    private JLabel locationLabel;
    private JLabel adminLabel;
    private JTextField nameTextField;
    private JTextField locationTextField;
    private JComboBox<String> adminComboBox;

    public AddEditBookStoreDialog(boolean edit, int id) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // set the labels
        if (edit) {
            headingLabel.setText("Edit Book Store");
            buttonOK.setText("Save");
        } else {
            headingLabel.setText("Add Book Store");
            buttonOK.setText("Add");
        }

        nameLabel.setText("Name");
        locationLabel.setText("Location");
        adminLabel.setText("Select Admin");

        // populate the combo box
        adminComboBox.addItem("Select Admin");
        // get the users from the database who don't have a role
        // add them to the combo box
        String sql = "SELECT * FROM users LEFT JOIN user_roles ON users.id = user_roles.user_id WHERE user_roles.user_id IS NULL";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                adminComboBox.addItem(resultSet.getString("id") + " - " + resultSet.getString("firstname") + " " + resultSet.getString("lastname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // if the user is editing, populate the fields
        if (edit) {
            sql = "SELECT name, location, firstname, lastname, admin_id FROM book_store, users WHERE book_store.id = " + id + " AND users.id = book_store.admin_id";
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    nameTextField.setText(resultSet.getString("name"));
                    locationTextField.setText(resultSet.getString("location"));
                    adminComboBox.addItem(resultSet.getString("admin_id") + " - " + resultSet.getString("firstname") + " " + resultSet.getString("lastname"));
                    adminComboBox.setSelectedItem(resultSet.getString("admin_id") + " - " + resultSet.getString("firstname") + " " + resultSet.getString("lastname"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        buttonOK.addActionListener(e -> onOK(edit, id));

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK(boolean edit, int id) {
        // verify that the fields are not empty
        if (nameTextField.getText().equals("") || locationTextField.getText().equals("") || Objects.equals(adminComboBox.getSelectedItem(), "Select Admin")) {
            JOptionPane.showMessageDialog(this, "Please fill in all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // get the admin id
        String adminId = Objects.requireNonNull(adminComboBox.getSelectedItem()).toString().split(" - ")[0];
        // if the user is editing, update the database
        if (edit) {
            // get the current admin id
            String currentAdminSql = "SELECT admin_id FROM book_store WHERE id = " + id;
            int currentAdminId = 0;
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(currentAdminSql);
                if (resultSet.next()) {
                    currentAdminId = resultSet.getInt("admin_id");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String sql = "UPDATE book_store SET name = ?, location = ?, admin_id = ? WHERE id = ?";
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, nameTextField.getText());
                preparedStatement.setString(2, locationTextField.getText());
                preparedStatement.setInt(3, Integer.parseInt(adminId));
                preparedStatement.setInt(4, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // if the admin id has changed, update the user_roles table
            if (currentAdminId != Integer.parseInt(adminId)) {
                String deleteSql = "DELETE FROM user_roles WHERE user_id = ?";
                try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                    PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                    preparedStatement.setInt(1, currentAdminId);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String insertSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
                try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
                    preparedStatement.setInt(1, Integer.parseInt(adminId));
                    preparedStatement.setString(2, "BS_ADMIN");
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // if the user is adding, insert into the database
            String sql = "INSERT INTO book_store (name, location, admin_id) VALUES (?, ?, ?)";
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, nameTextField.getText());
                preparedStatement.setString(2, locationTextField.getText());
                preparedStatement.setInt(3, Integer.parseInt(adminId));
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            sql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, Integer.parseInt(adminId));
                preparedStatement.setString(2, "BS_ADMIN");
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
