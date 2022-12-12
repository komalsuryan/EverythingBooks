package org.amishaandkomal.views.dialogs;

import org.amishaandkomal.Database;

import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OrderDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel headingLabel;
    private JLabel bookLabel;
    private JLabel quantityLabel;
    private JLabel pickupOrDeliveryLabel;
    private JTextField bookNameTextField;
    private JSpinner quantitySpinner;
    private JCheckBox deliverToYourPlaceCheckBox;
    private JLabel deliveryLocationLabel;
    private JTextField deliveryLocationTextField;
    public int quantity;
    public boolean deliverToYourPlace;
    public String deliveryLocation;
    public boolean isCancelled;

    public OrderDialog(long isbn) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // assign label text
        headingLabel.setText("Order Details");
        bookLabel.setText("Book Name");
        quantityLabel.setText("Quantity");
        pickupOrDeliveryLabel.setText("Pickup or Delivery");
        deliveryLocationLabel.setText("Delivery Location");

        // configure spinner
        quantitySpinner.setModel(new SpinnerNumberModel(1, 1, 50, 1));

        // assign button text
        buttonOK.setText("Order");
        buttonCancel.setText("Cancel");

        // fill book name
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            var statement = connection.prepareStatement(sql);
            statement.setLong(1, isbn);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                bookNameTextField.setText(resultSet.getString("name"));
                bookNameTextField.setEditable(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        deliveryLocationTextField.setEnabled(false);

        // add listener to checkbox
        deliverToYourPlaceCheckBox.addActionListener(e -> {
            if (deliverToYourPlaceCheckBox.isSelected()) {
                deliveryLocationTextField.setEnabled(true);
            } else {
                deliveryLocationTextField.setText("");
                deliveryLocationTextField.setEnabled(false);
            }
        });

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
        quantity = (int) quantitySpinner.getValue();
        deliverToYourPlace = deliverToYourPlaceCheckBox.isSelected();
        deliveryLocation = deliveryLocationTextField.getText();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        isCancelled = true;
        dispose();
    }
}
