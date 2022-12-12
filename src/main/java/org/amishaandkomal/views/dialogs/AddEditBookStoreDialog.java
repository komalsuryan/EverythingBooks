package org.amishaandkomal.views.dialogs;

import org.amishaandkomal.Database;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

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

        buttonOK.addActionListener(e -> onOK());

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

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
