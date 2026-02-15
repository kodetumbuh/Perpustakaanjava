package com.mycompany.perpustakaanjava;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import net.miginfocom.swing.MigLayout;

public class ImportData extends JFrame {

    private JTextField txtPath;
    private JButton btnBrowse;
    private JButton btnImport;

    public ImportData() {
        initUI();
    }

    private void initUI() {
        setTitle("Import Data Perpustakaan");
        setSize(500, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20", "[grow][]", "[]10[]20[]"));

        // Header
        JLabel lblInfo = new JLabel("Pilih file backup database (.sql) untuk di-restore");
        lblInfo.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblInfo, "span, wrap");
        
        JLabel lblWarning = new JLabel("PERINGATAN: Data saat ini akan ditimpa!");
        lblWarning.setForeground(Color.RED);
        add(lblWarning, "span, wrap");

        // Path Selection
        txtPath = new JTextField();
        txtPath.setEditable(false);
        btnBrowse = new JButton("Browse...");
        
        add(txtPath, "growx");
        add(btnBrowse, "wrap");
        
        // Import Button
        btnImport = new JButton("Import Data Sekarang");
        btnImport.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(btnImport, "span, growx, h 40!");

        // Actions
        btnBrowse.addActionListener(e -> chooseFile());
        btnImport.addActionListener(e -> performImport());
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih File Backup");
        
        // Set filter for SQL files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Backup Files", "sql"));
        
        // Default to Documents
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + File.separator + "Documents"));
        
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            txtPath.setText(fileToOpen.getAbsolutePath());
        }
    }

    private void performImport() {
        String path = txtPath.getText();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap pilih file backup!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin melakukan Restore Data?\nData saat ini akan HILANG dan digantikan dengan data dari file backup.", 
            "Konfirmasi Import", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Database Credentials
        String dbName = "perpustakaan_v2";
        String dbUser = "root";
        String dbPass = "cianjurtea";

        // Command: mysql -u root -pcianjurtea perpustakaan_v2 < [file]
        // Note: Java ProcessBuilder doesn't handle '<' redirection directly.
        // We need to use cmd /c for Windows to handle the redirection.
        
        // Find mysql executable
        String mysqlPath = "mysql"; // Default
        File[] possiblePaths = {
            new File("C:\\Program Files\\MariaDB 11.8\\bin\\mysql.exe"),
            new File("C:\\laragon\\bin\\mysql\\mysql-8.4.3-winx64\\bin\\mysql.exe"),
            new File("C:\\xampp\\mysql\\bin\\mysql.exe")
        };

        for (File p : possiblePaths) {
            if (p.exists()) {
                mysqlPath = p.getAbsolutePath();
                break;
            }
        }
        
        // Wrap path in quotes if it contains spaces
        if (mysqlPath.contains(" ")) {
            mysqlPath = "\"" + mysqlPath + "\"";
        }

        ProcessBuilder pb;
        
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            pb = new ProcessBuilder(
                "cmd.exe", 
                "/c", 
                mysqlPath + " --user=" + dbUser + " --password=" + dbPass + " " + dbName + " < \"" + path + "\""
            );
        } else {
             // Bash for Linux/Mac
             pb = new ProcessBuilder(
                "/bin/sh", 
                "-c", 
                mysqlPath + " --user=" + dbUser + " --password=" + dbPass + " " + dbName + " < \"" + path + "\""
            );
        }
        
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                JOptionPane.showMessageDialog(this, "Import Data Berhasil! Aplikasi akan restart.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                // Ideally, restart app here, but closing is safer to force reload
                System.exit(0);
            } else {
                // Read error output
                java.io.InputStream is = process.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String errorMsg = s.hasNext() ? s.next() : "";
                
                JOptionPane.showMessageDialog(this, "Gagal melakukan import.\nKode Error: " + exitCode + "\nPesan: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan sistem: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImportData().setVisible(true));
    }
}
