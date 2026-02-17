package com.mycompany.perpustakaanjava;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import net.miginfocom.swing.MigLayout;

public class ImportData extends JDialog {

    private JTextField txtPath;
    private JButton btnBrowse;
    private JButton btnImport;
    private boolean success = false;

    public boolean isSuccess() {
        return success;
    }

    public ImportData(Frame parent) {
        super(parent, "Import Data Perpustakaan", true); // Modal
        initUI();
    }
    
    public ImportData(Dialog parent) {
        super(parent, "Import Data Perpustakaan", true); // Modal
        initUI();
    }

    // Default legacy constructor (for main method testing)
    public ImportData() {
        this((Frame)null);
    }

    private void initUI() {
        // setTitle handled in super
        setSize(500, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));
        
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
            "Apakah Anda yakin ingin melakukan Restore Data?\nData saat ini akan HILANG dan digantikan dengan data dari file backup.\n-Abaikan pesan ini jika anda belum mempunyai data", 
            "Konfirmasi Import", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Database Credentials
        // Database Credentials
        String dbName = DatabaseConnection.DB_NAME;
        String dbUser = DatabaseConnection.USER;
        String dbPass = DatabaseConnection.PASSWORD;

        // Find mariadb executable
        String mariadbPath = "mariadb"; // Default
        // Add more common paths if needed
        File[] possiblePaths = {
            new File("C:\\Program Files\\MariaDB 11.4\\bin\\mariadb.exe"),
            new File("C:\\Program Files\\MariaDB 11.8\\bin\\mariadb.exe"),
            new File("C:\\Program Files\\MariaDB 10.11\\bin\\mariadb.exe"),
            new File("C:\\xampp\\mysql\\bin\\mariadb.exe"),
            // Fallback to mysql if mariadb not found but unlikely if user insisted on mariadb
            new File("C:\\Program Files\\MariaDB 11.8\\bin\\mysql.exe") 
        };

        for (File p : possiblePaths) {
            if (p.exists()) {
                mariadbPath = p.getAbsolutePath();
                break;
            }
        }
        
        // Use ProcessBuilder with direct arguments to avoid Shell/CMD quoting issues
        java.util.List<String> commands = new java.util.ArrayList<>();
        commands.add(mariadbPath);
        commands.add("--user=" + dbUser);
        commands.add("--password=" + dbPass);
        commands.add(dbName);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // Write the SQL file content to the Process OutputStream (STDIN)
            // This replaces the '<' redirection which is shell-specific
            try (java.io.OutputStream os = process.getOutputStream();
                 java.io.FileInputStream fis = new java.io.FileInputStream(path)) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                JOptionPane.showMessageDialog(this, "Import Data Berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                this.success = true;
                this.dispose();
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
