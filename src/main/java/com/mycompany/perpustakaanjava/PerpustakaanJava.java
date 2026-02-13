/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.perpustakaanjava;

/**
 *
 * @author ZEMS
 */
public class PerpustakaanJava {

    public static void main(String[] args) {
        // Set Look and Feel from Preferences
        try {
            String theme = java.util.prefs.Preferences.userNodeForPackage(SelectTheme.class)
                    .get("AppTheme", "javax.swing.plaf.nimbus.NimbusLookAndFeel"); // Default to Nimbus
            javax.swing.UIManager.setLookAndFeel(theme);
        } catch (Exception e) {
            // Fallback
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
