package org.amishaandkomal;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.amishaandkomal.views.LoginView;


import javax.swing.*;
import java.awt.*;

import static org.amishaandkomal.DatabaseSetup.*;

public class App {
    public static void main(String[] args) {
        // write your code here
        try {
            setupDatabase();
            setUpGlobalGui();
            JFrame jFrame = new JFrame("Everything Books");
            jFrame.setContentPane(new LoginView().getMainPanel());
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setLocationRelativeTo(null);
            jFrame.pack();
            jFrame.setVisible(true);
            setupGlobalExceptionHandling();
        } catch (Exception e) {
            // create a dialog to show the error
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void setupGlobalExceptionHandling() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> handleException(e));
    }

    public static void handleException(Throwable e) {
        // create a dialog to show the error
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void setUpGlobalGui() {
        try {
            // set up dracula theme as look and feel
            FlatDarculaLaf.setup();
            // set text fields to have rounded corners
            UIManager.put("TextComponent.arc", 5);
            // set buttons to hava a bold font
            UIManager.put("Button.font", UIManager.getFont("Button.font").deriveFont(Font.BOLD));
        } catch (Exception e) {
            handleException(e);
        }
    }
}