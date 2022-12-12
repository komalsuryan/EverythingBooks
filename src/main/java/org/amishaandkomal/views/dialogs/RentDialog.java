package org.amishaandkomal.views.dialogs;

import org.amishaandkomal.utilities.ImageFilter;

import javax.swing.*;
import java.awt.event.*;

public class RentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField idLocationTextField;
    private JButton idBrowseButton;
    private JLabel idLabel;
    public boolean isCancelled;
    public String idLocation;

    public RentDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // set the labels
        idLabel.setText("Please upload a valid ID to rent a book");

        // set the button text
        buttonOK.setText("Upload");
        buttonCancel.setText("Cancel");
        idBrowseButton.setText("Browse");

        // disable the id location text field
        idLocationTextField.setEnabled(false);

        // set the button listeners
        idBrowseButton.addActionListener(e -> onIdBrowse());

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

    private void onIdBrowse() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an ID");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            idLocationTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onOK() {
        // check if the idLocation is not empty
        if (idLocationTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select an image");
            return;
        }
        idLocation = idLocationTextField.getText();
        dispose();
    }

    private void onCancel() {
        isCancelled = true;
        dispose();
    }
}
