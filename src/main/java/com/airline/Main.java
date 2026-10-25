package com.airline;

import com.airline.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Configure modern FlatLaf Light Look & Feel
                FlatLightLaf.setup();

                // Refined global UI attributes for consistent spacing and rounded styling
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);
                UIManager.put("ProgressBar.arc", 10);
                UIManager.put("TextComponent.arc", 8);
                UIManager.put("ScrollBar.thumbArc", 999);
                UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
                UIManager.put("TabbedPane.showTabSeparators", true);
                UIManager.put("Table.rowHeight", 34);
                UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
                UIManager.put("Component.focusWidth", 1);
            } catch (Throwable t) {
                // Fallback to system Look & Feel if FlatLaf setup encounters any environment issue
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }
            new LoginFrame().setVisible(true);
        });
    }
}
