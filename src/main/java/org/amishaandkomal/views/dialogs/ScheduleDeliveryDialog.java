package org.amishaandkomal.views.dialogs;

import com.toedter.calendar.JCalendar;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class ScheduleDeliveryDialog extends JDialog {
    private final JCalendar datePicker;
    private final JSpinner timePicker;
    public boolean isCancelled;
    public String deliveryLocation;
    public String pickupLocation;
    public Timestamp pickupDateTime;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField pickupLocationTextField;
    private JLabel pickupLocationLabel;
    private JLabel dateTimePickerLabel;
    private JTextField deliveryLocationTextField;
    private JPanel dateTimePickerPanel;
    private JLabel headingLabel;
    private JLabel deliveryLocationLabel;


    public ScheduleDeliveryDialog(String pickupLocation, String deliveryLocation) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // set labels
        headingLabel.setText("Schedule Pickup and Delivery");
        pickupLocationLabel.setText("Pickup Location");
        deliveryLocationLabel.setText("Delivery Location");
        dateTimePickerLabel.setText("Pickup Date and Time");

        // set text fields
        if (pickupLocation != null) {
            pickupLocationTextField.setText(pickupLocation);
            pickupLocationTextField.setEditable(false);
        }
        if (deliveryLocation != null) {
            deliveryLocationTextField.setText(deliveryLocation);
            deliveryLocationTextField.setEditable(false);
        }

        // set date time picker
        datePicker = new JCalendar();
        datePicker.setMinSelectableDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        datePicker.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        datePicker.setMaxSelectableDate(Date.from(LocalDateTime.now().plusMonths(1).atZone(ZoneId.systemDefault()).toInstant()));
        datePicker.setTodayButtonVisible(true);


        SpinnerDateModel model = new SpinnerDateModel();
        model.setCalendarField(Calendar.HOUR_OF_DAY);
        timePicker = new JSpinner(model);

        JSpinner.DateEditor editor = new JSpinner.DateEditor(timePicker, "HH:mm");
        timePicker.setEditor(editor);

        dateTimePickerPanel.setLayout(new BoxLayout(dateTimePickerPanel, BoxLayout.X_AXIS));
        dateTimePickerPanel.add(datePicker);
        dateTimePickerPanel.add(timePicker);

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
        // verify if the fields are empty
        if (pickupLocationTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pickup location cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (deliveryLocationTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Delivery location cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // get the values from the fields
        pickupLocation = pickupLocationTextField.getText();
        deliveryLocation = deliveryLocationTextField.getText();
        // get the date as LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.ofInstant(datePicker.getDate().toInstant(), ZoneId.systemDefault());
        // get the time as LocalDateTime
        LocalDateTime time = LocalDateTime.ofInstant(((Date) timePicker.getValue()).toInstant(), ZoneId.systemDefault());
        // set the time to the date
        localDateTime = localDateTime.withHour(time.getHour()).withMinute(time.getMinute());
        // convert to Timestamp
        pickupDateTime = Timestamp.valueOf(localDateTime);
        dispose();
    }

    private void onCancel() {
        isCancelled = true;
        dispose();
    }
}
