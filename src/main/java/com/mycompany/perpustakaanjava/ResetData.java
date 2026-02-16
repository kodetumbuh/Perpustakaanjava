package com.mycompany.perpustakaanjava;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Interface for Reset Data / Format Database.
 * Wipes all business data but keeps users.
 */
public class ResetData extends JDialog {

    private JTextField txtConfirm;
    private JButton btnReset;
    private static final String CONFIRM_CODE = "RESET";

    public ResetData(Frame parent) {
        super(parent, "Reset Data", true);
        initUI();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][][][grow]"));
        setSize(500, 300);
        setLocationRelativeTo(getParent());

        JLabel lblTitle = new JLabel("PERINGATAN: TINDAKAN INI TIDAK DAPAT DIBATALKAN!");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitle.setForeground(Color.RED);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, "growx, wrap, gapbottom 5");

        JLabel lblMsg1 = new JLabel("Semua data (buku, anggota, peminjaman, dll) akan dihapus secara permanen.");
        lblMsg1.setForeground(Color.RED);
        lblMsg1.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblMsg1, "growx, wrap");
        
        JLabel lblMsg2 = new JLabel("Akun pengguna TIDAK akan dihapus.");
        lblMsg2.setForeground(Color.RED);
        lblMsg2.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblMsg2, "growx, wrap, gapbottom 20");

        add(new JLabel("Ketik '" + CONFIRM_CODE + "' untuk konfirmasi:"), "wrap");
        
        txtConfirm = new JTextField();
        add(txtConfirm, "growx, wrap, gapbottom 20");

        btnReset = new JButton("RESET DATA");
        btnReset.setBackground(Color.RED);
        btnReset.setForeground(Color.WHITE);
        btnReset.setEnabled(false);
        btnReset.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(btnReset, "growx, height 40!");

        // Document Listener to enable button only when code matches
        txtConfirm.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { checkInput(); }
            @Override public void removeUpdate(DocumentEvent e) { checkInput(); }
            @Override public void changedUpdate(DocumentEvent e) { checkInput(); }
        });

        btnReset.addActionListener(e -> performReset());
    }

    private void checkInput() {
        btnReset.setEnabled(txtConfirm.getText().equals(CONFIRM_CODE));
    }

    private void performReset() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin menghapus semua data?", 
                "Konfirmasi Akhir", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (wipeDatabase()) {
                JOptionPane.showMessageDialog(this, "Data berhasil di-reset.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal me-reset data. Cek log.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean wipeDatabase() {
        // Order matters if FK checks are on, but we'll disable them to be safe and easy
        String[] tables = {
            "peminjaman_detail", 
            "peminjaman", 
            "buku", 
            "anggota", 
            "pengarang", 
            "penerbit", 
            "kategori", 
            "rak",
            "properti",
            "pengembalian"
            // "user" - Excluded intentionally
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            conn.setAutoCommit(false);
            
            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                
                for (String table : tables) {
                    stmt.executeUpdate("TRUNCATE TABLE " + table);
                }
                
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
