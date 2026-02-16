/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.perpustakaanjava;

import javax.swing.JOptionPane;

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

        // Check Database Connection
        if (!DatabaseConnection.checkConnection()) {
             // Connection failed or database doesn't exist
             // Show SettingDatabase dialog
             final SettingDatabase settings = new SettingDatabase(null, true);
             settings.setVisible(true);
             
             // Re-check after settings closed
             if (!DatabaseConnection.checkServerConnection()) {
                 JOptionPane.showMessageDialog(null, "Gagal terhubung ke database. Aplikasi akan ditutup.", "Error", JOptionPane.ERROR_MESSAGE);
                 System.exit(0);
             }
        }
        
        // Launch Main App if connection successful
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
