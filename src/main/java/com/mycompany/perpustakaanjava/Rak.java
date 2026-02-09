package com.mycompany.perpustakaanjava;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application frame for managing Rak.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class Rak extends JFrame {

    // --- Model Class ---
    public static class RakData {
        private int idRak;
        private String kodeRak;
        private String lokasi;
        private int kapasitas;
        private String status; // Misalnya: "Penuh", "Tersedia"

        public RakData() {}

        public RakData(int idRak, String kodeRak, String lokasi, int kapasitas, String status) {
            this.idRak = idRak;
            this.kodeRak = kodeRak;
            this.lokasi = lokasi;
            this.kapasitas = kapasitas;
            this.status = status;
        }

        public int getIdRak() { return idRak; }
        public void setIdRak(int idRak) { this.idRak = idRak; }
        
        public String getKodeRak() { return kodeRak; }
        public void setKodeRak(String kodeRak) { this.kodeRak = kodeRak; }
        
        public String getLokasi() { return lokasi; }
        public void setLokasi(String lokasi) { this.lokasi = lokasi; }
        
        public int getKapasitas() { return kapasitas; }
        public void setKapasitas(int kapasitas) { this.kapasitas = kapasitas; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() { return kodeRak + " - " + lokasi; }
    }

    // --- UI Logic ---
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;

    // Pagination UI
    private JButton btnPrevious;
    private JButton btnNext;
    private JComboBox<Integer> cmbPageSize;
    private JLabel lblPageInfo;

    // Pagination State
    private int currentPage = 1;
    private int totalRecords = 0;

    // Sorting State
    private String sortColumn = "id_rak";
    private String sortOrder = "ASC";

    public Rak() {
        setTitle("Master Data Rak");
        // Use standard frame setup
        setSize(1000, 700); // Standard size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose by default for sub-windows
        setLocationRelativeTo(null);

        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // === Header / Filter Section ===
        JPanel pnlHeader = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        pnlHeader.add(new JLabel("Search:"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Rak");
        btnAdd.addActionListener(e -> showRakDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        // Kolom: ID, Kode, Lokasi, Kapasitas, Status
        String[] columnNames = {"ID", "Kode Rak", "Lokasi", "Kapasitas", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        // Sorting Click Handler
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                String[] dbColumns = {"id_rak", "kode_rak", "lokasi", "kapasitas", "status"};

                if (col >= 0 && col < dbColumns.length) {
                    String clickedColumn = dbColumns[col];
                    if (clickedColumn.equals(sortColumn)) {
                        sortOrder = sortOrder.equals("ASC") ? "DESC" : "ASC";
                    } else {
                        sortColumn = clickedColumn;
                        sortOrder = "ASC";
                    }
                    loadData();
                }
            }
        });

        // Double click to edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        int id = (int) table.getValueAt(selectedRow, 0);
                        RakData rak = getRakById(id);
                        if (rak != null) {
                            showRakDialog(rak);
                        }
                    }
                }
            }
        });

        // Popup Menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mnEdit = new JMenuItem("Edit");
        JMenuItem mnDelete = new JMenuItem("Delete");

        mnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
             if (selectedRow != -1) {
                int id = (int) table.getValueAt(selectedRow, 0);
                RakData rak = getRakById(id);
                if (rak != null) {
                    showRakDialog(rak);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                 int id = (int) table.getValueAt(selectedRow, 0);
                 int confirm = JOptionPane.showConfirmDialog(Rak.this, "Are you sure you want to delete this Rak?", "Delete", JOptionPane.YES_NO_OPTION);
                 if (confirm == JOptionPane.YES_OPTION) {
                     deleteRak(id);
                     loadData();
                 }
            }
        });

        popupMenu.add(mnEdit);
        popupMenu.add(mnDelete);
        table.setComponentPopupMenu(popupMenu);

        add(new JScrollPane(table), "wrap, grow, push");

        // === Pagination Section ===
        add(createPaginationPanel(), "center");
    }

    private JPanel createPaginationPanel() {
        JPanel pnlPagination = new JPanel(new MigLayout("", "[][push][]", "[]"));

        btnPrevious = new JButton("Previous");
        btnNext = new JButton("Next");
        Integer[] pageSizes = {25, 50, 100};
        cmbPageSize = new JComboBox<>(pageSizes);
        lblPageInfo = new JLabel("Page 1");

        btnPrevious.addActionListener(e -> {
            currentPage--;
            loadData();
        });

        btnNext.addActionListener(e -> {
            currentPage++;
            loadData();
        });

        cmbPageSize.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        pnlPagination.add(new JLabel("Rows per page:"));
        pnlPagination.add(cmbPageSize);
        pnlPagination.add(lblPageInfo, "gapleft 20");
        pnlPagination.add(btnPrevious, "gapleft push");
        pnlPagination.add(btnNext);

        return pnlPagination;
    }

    private void updatePageInfo(int currentPage, int totalPages, int totalRecords) {
        lblPageInfo.setText(String.format("Page %d of %d (Total: %d)", currentPage, totalPages, totalRecords));

        btnPrevious.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }

    private void loadData() {
        int pageSize = (int) cmbPageSize.getSelectedItem();
        int offset = (currentPage - 1) * pageSize;
        String search = txtSearch.getText();

        List<RakData> list = getAllRak(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getRakTotalCount(search);

        tableModel.setRowCount(0);
        for (RakData r : list) {
            tableModel.addRow(new Object[]{
                r.getIdRak(),
                r.getKodeRak(),
                r.getLokasi(),
                r.getKapasitas(),
                r.getStatus()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showRakDialog(RakData rak) {
        JDialog dialog = new JDialog(this, rak == null ? "Add Rak" : "Edit Rak", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[][grow]", "[]"));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JTextField txtKode = new JTextField(20);
        JTextField txtLokasi = new JTextField(20);
        JSpinner spnKapasitas = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        
        String[] statusOptions = {"Tersedia", "Penuh", "Rusak"};
        JComboBox<String> cmbStatus = new JComboBox<>(statusOptions);

        if (rak != null) {
            txtKode.setText(rak.getKodeRak());
            txtLokasi.setText(rak.getLokasi());
            spnKapasitas.setValue(rak.getKapasitas());
            cmbStatus.setSelectedItem(rak.getStatus());
        }

        dialog.add(new JLabel("Kode Rak:"));
        dialog.add(txtKode, "wrap, growx");
        dialog.add(new JLabel("Lokasi:"));
        dialog.add(txtLokasi, "wrap, growx");
        dialog.add(new JLabel("Kapasitas:"));
        dialog.add(spnKapasitas, "wrap, growx");
        dialog.add(new JLabel("Status:"));
        dialog.add(cmbStatus, "wrap, growx");

        JCheckBox chkKeepOpen = new JCheckBox("Tetap di form");
        if (rak == null) {
            dialog.add(chkKeepOpen, "wrap");
        } else {
            dialog.add(new JLabel(""), "wrap");
        }

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            String kode = txtKode.getText();
            String lokasi = txtLokasi.getText();
            int kapasitas = (int) spnKapasitas.getValue();
            String status = (String) cmbStatus.getSelectedItem();

            RakData newRak = new RakData(
                (rak == null) ? 0 : rak.getIdRak(),
                kode,
                lokasi,
                kapasitas,
                status
            );

            if (rak == null) {
                saveRak(newRak);
                loadData(); 

                if (chkKeepOpen.isSelected()) {
                    txtKode.setText("");
                    txtLokasi.setText("");
                    spnKapasitas.setValue(0);
                    txtKode.requestFocus();
                } else {
                    dialog.dispose();
                }
            } else {
                updateRak(newRak);
                loadData();
                dialog.dispose();
            }
        });

        dialog.add(btnSave, "span, align right");
        dialog.setVisible(true);
    }

    // =================================== DAO Logic ====================================

    private List<RakData> getAllRak(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<RakData> list = new ArrayList<>();

        if (!sortColumn.matches("id_rak|kode_rak|lokasi|kapasitas|status")) {
            sortColumn = "id_rak";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String sql = "SELECT id_rak, kode_rak, lokasi, kapasitas, status FROM rak " +
                     "WHERE kode_rak LIKE ? OR lokasi LIKE ? OR status LIKE ? " +
                     "ORDER BY " + sortColumn + " " + sortOrder + " " +
                     "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchKeyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setInt(4, limit);
            pstmt.setInt(5, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new RakData(
                        rs.getInt("id_rak"),
                        rs.getString("kode_rak"),
                        rs.getString("lokasi"),
                        rs.getInt("kapasitas"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching rak: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getRakTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM rak " +
                     "WHERE kode_rak LIKE ? OR lokasi LIKE ? OR status LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchKeyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error counting rak: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return count;
    }

    private void saveRak(RakData rak) {
        String sql = "INSERT INTO rak (kode_rak, lokasi, kapasitas, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rak.getKodeRak());
            pstmt.setString(2, rak.getLokasi());
            pstmt.setInt(3, rak.getKapasitas());
            pstmt.setString(4, rak.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving rak: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRak(RakData rak) {
        String sql = "UPDATE rak SET kode_rak = ?, lokasi = ?, kapasitas = ?, status = ? WHERE id_rak = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rak.getKodeRak());
            pstmt.setString(2, rak.getLokasi());
            pstmt.setInt(3, rak.getKapasitas());
            pstmt.setString(4, rak.getStatus());
            pstmt.setInt(5, rak.getIdRak());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating rak: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRak(int id) {
        String sql = "DELETE FROM rak WHERE id_rak = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting rak: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private RakData getRakById(int id) {
        RakData rak = null;
        String sql = "SELECT * FROM rak WHERE id_rak = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    rak = new RakData(
                        rs.getInt("id_rak"),
                        rs.getString("kode_rak"),
                        rs.getString("lokasi"),
                        rs.getInt("kapasitas"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting rak: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return rak;
    }

    // --- Main Method ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Rak().setVisible(true);
        });
    }
}
