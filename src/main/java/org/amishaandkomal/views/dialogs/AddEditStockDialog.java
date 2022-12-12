package org.amishaandkomal.views.dialogs;

import org.amishaandkomal.Database;

import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class AddEditStockDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner availableTextField;
    private JCheckBox rentableCheckBox;
    private JLabel availableLabel;
    private JLabel rentableLabel;
    private JLabel bookLabel;
    public JComboBox<String> selectBookComboBox;
    public long isbn;
    public int quantity;
    public boolean rentable;
    public boolean isCancelled;

    public AddEditStockDialog(boolean edit, long isbn, int quantity, boolean rentable) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // assign label text
        availableLabel.setText("No. of Copies");
        rentableLabel.setText("Rentable");
        bookLabel.setText("Select Book");

        // assign button text
        buttonOK.setText(edit ? "Save" : "Add");
        buttonCancel.setText("Cancel");

        // fill book name
        selectBookComboBox.addItem("Select Book");
        String sql = "SELECT * FROM books";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                selectBookComboBox.addItem(resultSet.getLong("isbn") + " - " + resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        selectBookComboBox.setSelectedIndex(0);

        // configure spinner
        availableTextField.setModel(new SpinnerNumberModel(1, 1, 50, 1));

        if (edit) {
            selectBookComboBox.setEnabled(false);
            String getBookNameSql = "SELECT * FROM books WHERE isbn = ?";
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                var statement = connection.prepareStatement(getBookNameSql);
                statement.setLong(1, isbn);
                var resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    selectBookComboBox.setSelectedItem(isbn + " - " + resultSet.getString("name"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            availableTextField.setValue(quantity);
            rentableCheckBox.setSelected(rentable);
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
        // verify input
        if (selectBookComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a book", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        isbn = Long.parseLong(Objects.requireNonNull(selectBookComboBox.getSelectedItem()).toString().split(" - ")[0]);
        quantity = (int) availableTextField.getValue();
        rentable = rentableCheckBox.isSelected();
        dispose();
    }

    private void onCancel() {
        isCancelled = true;
        dispose();
    }
}
