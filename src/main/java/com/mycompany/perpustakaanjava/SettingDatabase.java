package com.mycompany.perpustakaanjava;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.prefs.Preferences;

public class SettingDatabase extends JDialog {

    private JTextField txtUser;
    private JPasswordField txtPassword;
    private JButton btnConnect;
    private JButton btnCreateDb;
    private JButton btnImport;

    public SettingDatabase(Frame parent, boolean modal) {
        super(parent, "Pengaturan Database", modal);
        initUI();
    }
    
    // Constructor for standalone usage (e.g. startup)
    public SettingDatabase() {
        this(null, true);
    }

    private void initUI() {
        setSize(500, 350); // Small compact size for settings
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Or EXIT_ON_CLOSE if it's the only window? 
        // For startup logic, we might need to handle close separately. 
        
        setLayout(new MigLayout("insets 20", "[][grow]", "[]10[]20[]10[]10[]"));

        // Header
        JLabel lblTitle = new JLabel("Konfigurasi Database");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(lblTitle, "span, wrap");

        // Credentials Section
        add(new JLabel("Database User:"));
        txtUser = new JTextField(DatabaseConnection.USER, 20);
        add(txtUser, "growx, wrap");

        add(new JLabel("Database Password:"));
        txtPassword = new JPasswordField(DatabaseConnection.PASSWORD, 20);
        add(txtPassword, "growx, wrap");

        // Connect Button
        btnConnect = new JButton("Hubungkan");
        btnConnect.addActionListener(e -> testAndSaveConnection());
        add(btnConnect, "span, growx, h 40!, wrap");
        
        // Divider
        add(new JSeparator(), "span, growx, wrap");
        
        // Utilities Section (Only enabled if connection is verified or for initial setup?)
        // Actually "Buat Database Kosong" might be needed IF connection to server works but DB doesn't exist.
        // But DatabaseConnection connects to specific DB URL.
        
        JLabel lblUtils = new JLabel("Utilities:");
        lblUtils.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(lblUtils, "span, wrap");

        btnCreateDb = new JButton("Buat Database Kosong");
        btnCreateDb.setToolTipText("Eksekusi dump_database.sql");
        btnCreateDb.addActionListener(e -> executeDumpDatabase());
        add(btnCreateDb, "span, growx, split 2");

        btnImport = new JButton("Import SQL");
        btnImport.setToolTipText("Restore dari file .sql backup");
        btnImport.addActionListener(e -> openImportData());
        add(btnImport, "growx, wrap");
    }

    private void testAndSaveConnection() {
        String user = txtUser.getText();
        String pass = new String(txtPassword.getPassword());
        
        // Update credentials temporarily to test
        DatabaseConnection.updateCredentials(user, pass);
        
        if (DatabaseConnection.checkServerConnection()) {
            JOptionPane.showMessageDialog(this, "Koneksi ke Server Berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            // Keep form open
        } else {
             JOptionPane.showMessageDialog(this, "Koneksi Gagal!\nPeriksa Username dan Password Database.", "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDumpDatabase() {
        // We need to connect to MySQL server WITHOUT specifying database name first, 
        // because the DB might not exist yet.
        String user = txtUser.getText();
        String pass = new String(txtPassword.getPassword());
        
        // 1. Try to read dump file
        File dumpFile = new File("src/main/java/com/mycompany/perpustakaanjava/dump_database.sql");
        if (!dumpFile.exists()) {
            // Try relative path from execution dir
             dumpFile = new File("dump_database.sql");
             if (!dumpFile.exists()) {
                 JOptionPane.showMessageDialog(this, "File dump_database.sql tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
             }
        }

        // 2. Use ProcessBuilder to execute mysql command
        // mysql -u root -p < dump_database.sql
        
        // Find mariadb/mysql executable
        String mariadbPath = "mariadb"; 
        File[] possiblePaths = {
            new File("C:\\Program Files\\MariaDB 11.4\\bin\\mariadb.exe"),
            new File("C:\\Program Files\\MariaDB 11.8\\bin\\mariadb.exe"),
            new File("C:\\Program Files\\MariaDB 10.11\\bin\\mariadb.exe"),
            new File("C:\\xampp\\mysql\\bin\\mariadb.exe"),
            new File("C:\\Program Files\\MariaDB 11.8\\bin\\mysql.exe") 
        };

        for (File p : possiblePaths) {
            if (p.exists()) {
                mariadbPath = p.getAbsolutePath();
                break;
            }
        }
        
        java.util.List<String> commands = new java.util.ArrayList<>();
        commands.add(mariadbPath);
        commands.add("--user=" + user);
        if (!pass.isEmpty()) {
            commands.add("--password=" + pass);
        }
        
        // ProcessBuilder doesn't support < redirection directly in arguments for all shells consistently
        // We will feed input stream
        
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        
        try {
            Process process = pb.start();
            
            try (java.io.OutputStream os = process.getOutputStream();
                 java.io.FileInputStream fis = new java.io.FileInputStream(dumpFile)) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                 JOptionPane.showMessageDialog(this, "Database berhasil dibuat dari dump_database.sql!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                java.io.InputStream is = process.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String errorMsg = s.hasNext() ? s.next() : "";
                JOptionPane.showMessageDialog(this, "Gagal execute dump.\nError: " + errorMsg, "Gagal", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openImportData() {
        ImportData importForm = new ImportData(this);
        importForm.setVisible(true);
    }
}
