package org.amishaandkomal.views.dialogs;

import org.amishaandkomal.Database;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Objects;

public class AddEditBookDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel headingLabel;
    private JLabel isbnLabel;
    private JLabel nameLabel;
    private JLabel editionLabel;
    private JLabel pubYearLabel;
    private JLabel authorNameLabel;
    private JLabel publicationLabel;
    private JLabel retailPriceLabel;
    private JLabel genreLabel;
    private JTextField isbnTextField;
    private JTextField nameTextField;
    private JTextField editionTextField;
    private JSpinner pubYearSpinner;
    private JTextField authorNameTextField;
    private JComboBox<String> publicationComboBox;
    private JSpinner retailPriceSpinner;
    private JTextField genreTextField;

    public AddEditBookDialog(boolean edit, String isbn) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // assign the heading
        headingLabel.setText(edit ? "Edit Book" : "Add Book");

        // assign the labels
        isbnLabel.setText("ISBN");
        nameLabel.setText("Name");
        editionLabel.setText("Edition");
        pubYearLabel.setText("Publication Year");
        authorNameLabel.setText("Author Name");
        publicationLabel.setText("Publication");
        retailPriceLabel.setText("Retail Price");
        genreLabel.setText("Genre");

        // populate the publication combo box
        String pubSql = "SELECT * FROM publishing_company";
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(pubSql);
            while (resultSet.next()) {
                publicationComboBox.addItem(resultSet.getString("id") + " - " + resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // set the spinner values
        // get the current year
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        SpinnerNumberModel pubYearModel = new SpinnerNumberModel(currentYear, 0, currentYear, 1);
        pubYearSpinner.setModel(pubYearModel);
        SpinnerNumberModel retailPriceModel = new SpinnerNumberModel(0, 0, 100000, 1);
        retailPriceSpinner.setModel(retailPriceModel);

        if (edit) {
            String sql = "SELECT * FROM books WHERE isbn = " + isbn;
            try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    isbnTextField.setText(String.valueOf(resultSet.getLong("isbn")));
                    nameTextField.setText(resultSet.getString("name"));
                    editionTextField.setText(resultSet.getString("edition"));
                    pubYearSpinner.setValue(resultSet.getInt("pub_year"));
                    authorNameTextField.setText(resultSet.getString("author_name"));
                    retailPriceSpinner.setValue(resultSet.getInt("retail"));
                    genreTextField.setText(resultSet.getString("genre"));

                    isbnTextField.setEditable(false);

                    // set the combo box value
                    int pubId = resultSet.getInt("pub_id");
                    String pubNameSql = "SELECT * FROM publishing_company WHERE id = " + pubId;
                    Statement pubNameStatement = connection.createStatement();
                    ResultSet pubNameResultSet = pubNameStatement.executeQuery(pubNameSql);
                    if (pubNameResultSet.next()) {
                        publicationComboBox.setSelectedItem(pubId + " - " + pubNameResultSet.getString("name"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // assign the button labels
        buttonOK.setText(edit ? "Save" : "Add");
        buttonCancel.setText("Cancel");

        buttonOK.addActionListener(e -> onOK(edit));

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

    private void onOK(boolean edit) {
        // verify the fields
        if (isbnTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ISBN cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nameTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (publicationComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Publication cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // get the values, if empty then set to null
        String isbn = isbnTextField.getText();
        String name = nameTextField.getText();
        String edition = editionTextField.getText();
        int pubYear = (int) pubYearSpinner.getValue();
        String authorName = authorNameTextField.getText();
        String publication = Objects.requireNonNull(publicationComboBox.getSelectedItem()).toString();
        int retailPrice = (int) retailPriceSpinner.getValue();
        String genre = genreTextField.getText();

        // get the publication id
        String pubId = publication.split(" - ")[0];

        String sql;
        if (edit) {
            sql = "UPDATE books SET isbn = ?, name = ?, edition = ?, pub_year = ?, author_name = ?, pub_id = ?, retail = ?, genre = ? WHERE isbn = " + isbn;
        } else {
            sql = "INSERT INTO books (isbn, name, edition, pub_year, author_name, pub_id, retail, genre) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }
        try (Connection connection = DriverManager.getConnection(Database.databaseUrl)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, isbn);
            statement.setString(2, name);
            statement.setString(3, edition.isEmpty() ? null : edition);
            statement.setInt(4, pubYear);
            statement.setString(5, authorName.isEmpty() ? null : authorName);
            statement.setInt(6, Integer.parseInt(pubId));
            statement.setInt(7, retailPrice);
            statement.setString(8, genre.isEmpty() ? null : genre);
            statement.executeUpdate();
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
