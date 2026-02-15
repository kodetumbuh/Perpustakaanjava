package com.mycompany.perpustakaanjava;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.miginfocom.swing.MigLayout;

public class Backup extends JFrame {

    private JTextField txtPath;
    private JButton btnBrowse;
    private JButton btnBackup;

    public Backup() {
        initUI();
    }

    private void initUI() {
        setTitle("Backup Data Perpustakaan");
        setSize(500, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20", "[grow][]", "[]10[]20[]"));

        // Header
        JLabel lblInfo = new JLabel("Pilih lokasi untuk menyimpan file backup database (.sql)");
        lblInfo.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblInfo, "span, wrap");

        // Path Selection
        txtPath = new JTextField();
        txtPath.setEditable(false);
        btnBrowse = new JButton("Browse...");
        
        add(txtPath, "growx");
        add(btnBrowse, "wrap");
        
        // Backup Button
        btnBackup = new JButton("Backup Data Sekarang");
        btnBackup.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(btnBackup, "span, growx, h 40!");

        // Actions
        btnBrowse.addActionListener(e -> chooseFile());
        btnBackup.addActionListener(e -> performBackup());
        
        // Default filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultName = "backup_perpustakaan_" + sdf.format(new Date()) + ".sql";
        txtPath.setText(System.getProperty("user.home") + File.separator + "Documents" + File.separator + defaultName);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File Backup");
        fileChooser.setSelectedFile(new File(txtPath.getText()));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure .sql extension
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".sql")) {
                path += ".sql";
            }
            txtPath.setText(path);
        }
    }

    private void performBackup() {
        String path = txtPath.getText();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap pilih lokasi penyimpanan backup!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Database Credentials
        String dbName = "perpustakaan_v2";
        String dbUser = "root";
        String dbPass = "cianjurtea";

        // Command: mysqldump -u root -pcianjurtea -r [path] perpustakaan_v2
        // Note: -p[password] requires no space
        // Using List<String> for ProcessBuilder is safer
        ProcessBuilder pb = new ProcessBuilder(
            "mysqldump",
            "--user=" + dbUser,
            "--password=" + dbPass,
            "--result-file=" + path,
            dbName
        );
        
        // Redirect errors to see them if fails
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                JOptionPane.showMessageDialog(this, "Backup Data Berhasil!\nDisimpan di: " + path, "Sukses", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                // Read error output
                java.io.InputStream is = process.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String errorMsg = s.hasNext() ? s.next() : "";
                
                JOptionPane.showMessageDialog(this, "Gagal melakukan backup.\nKode Error: " + exitCode + "\nPesan: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan sistem: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Backup().setVisible(true));
    }
}
