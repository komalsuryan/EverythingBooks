package org.amishaandkomal.utilities;

import org.amishaandkomal.App;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;

public class FrequentGuiMethods {
    public static void onLogout() {
        // destroy all windows
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
        // call the main method of the App class
        // this will restart the app
        App.main(null);
    }

    public static TableModel resultSetToTableModel(ResultSet rs) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int numberOfColumns = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            // Get the column names
            for (int column = 0; column < numberOfColumns; column++) {
                columnNames.addElement(metaData.getColumnLabel(column + 1));
            }
            // Get all rows.
            Vector<Vector<Object>> rows = new Vector<>();
            while (rs.next()) {
                Vector<Object> newRow = new Vector<>();
                for (int i = 1; i <= numberOfColumns; i++) {
                    newRow.addElement(rs.getObject(i));
                }
                rows.addElement(newRow);
            }
            return new DefaultTableModel(rows, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
