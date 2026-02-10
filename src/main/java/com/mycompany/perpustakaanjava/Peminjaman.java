package com.mycompany.perpustakaanjava;

import com.toedter.calendar.JDateChooser;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main application frame for managing Peminjaman.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class Peminjaman extends JFrame {

    // --- Model Classes ---
    public static class PeminjamanData {
        private int idPeminjaman;
        private String noPeminjaman;
        private int idAnggota;
        private String namaAnggota; // For display
        private int idUser;
        private String username; // For display
        private Date tglPeminjaman;
        private Date tglKembaliRencana;
        private Date tglKembaliAktual;
        private double denda;
        private String status;

        public PeminjamanData() {}

        public PeminjamanData(int idPeminjaman, String noPeminjaman, int idAnggota, String namaAnggota,
                              int idUser, String username, Date tglPeminjaman, Date tglKembaliRencana,
                              Date tglKembaliAktual, double denda, String status) {
            this.idPeminjaman = idPeminjaman;
            this.noPeminjaman = noPeminjaman;
            this.idAnggota = idAnggota;
            this.namaAnggota = namaAnggota;
            this.idUser = idUser;
            this.username = username;
            this.tglPeminjaman = tglPeminjaman;
            this.tglKembaliRencana = tglKembaliRencana;
            this.tglKembaliAktual = tglKembaliAktual;
            this.denda = denda;
            this.status = status;
        }

        // Getters and Setters
        public int getIdPeminjaman() { return idPeminjaman; }
        public void setIdPeminjaman(int idPeminjaman) { this.idPeminjaman = idPeminjaman; }
        public String getNoPeminjaman() { return noPeminjaman; }
        public void setNoPeminjaman(String noPeminjaman) { this.noPeminjaman = noPeminjaman; }
        public int getIdAnggota() { return idAnggota; }
        public void setIdAnggota(int idAnggota) { this.idAnggota = idAnggota; }
        public String getNamaAnggota() { return namaAnggota; }
        public void setNamaAnggota(String namaAnggota) { this.namaAnggota = namaAnggota; }
        public int getIdUser() { return idUser; }
        public void setIdUser(int idUser) { this.idUser = idUser; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Date getTglPeminjaman() { return tglPeminjaman; }
        public void setTglPeminjaman(Date tglPeminjaman) { this.tglPeminjaman = tglPeminjaman; }
        public Date getTglKembaliRencana() { return tglKembaliRencana; }
        public void setTglKembaliRencana(Date tglKembaliRencana) { this.tglKembaliRencana = tglKembaliRencana; }
        public Date getTglKembaliAktual() { return tglKembaliAktual; }
        public void setTglKembaliAktual(Date tglKembaliAktual) { this.tglKembaliAktual = tglKembaliAktual; }
        public double getDenda() { return denda; }
        public void setDenda(double denda) { this.denda = denda; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() { return noPeminjaman; }
    }

    // Helper classes for ComboBoxes
    public static class AnggotaItem {
        private int id;
        private String nama;
        public AnggotaItem(int id, String nama) { this.id = id; this.nama = nama; }
        public int getId() { return id; }
        @Override public String toString() { return nama; }
    }

    public static class UserItem {
        private int id;
        private String username;
        public UserItem(int id, String username) { this.id = id; this.username = username; }
        public int getId() { return id; }
        @Override public String toString() { return username; }
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
    private String sortColumn = "id_peminjaman";
    private String sortOrder = "DESC"; // Default newest first

    public Peminjaman() {
        setTitle("Master Peminjaman");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed to DISPOSE to not exit app
        setLocationRelativeTo(null);

        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // === Header / Filter Section ===
        JPanel pnlHeader = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        pnlHeader.add(new JLabel("Search (No Peminjaman/Anggota):"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Peminjaman");
        btnAdd.addActionListener(e -> showPeminjamanDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        String[] columnNames = {"ID", "No Peminjaman", "Anggota", "User", "Tgl Pinjam", "Tgl Kembali (Rencana)", "Tgl Kembali (Aktual)", "Denda", "Status"};
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
                // Map table columns to DB columns
                String[] dbColumns = {"p.id_peminjaman", "p.no_peminjaman", "a.nama", "u.username", "p.tgl_peminjaman", "p.tgl_kembali_rencana", "p.tgl_kembali_aktual", "p.denda", "p.status"};

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
                        PeminjamanData peminjaman = getPeminjamanById(id);
                        if (peminjaman != null) {
                            showPeminjamanDialog(peminjaman);
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
                PeminjamanData peminjaman = getPeminjamanById(id);
                if (peminjaman != null) {
                    showPeminjamanDialog(peminjaman);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) table.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(Peminjaman.this, "Are you sure you want to delete this peminjaman?", "Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deletePeminjaman(id);
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

        List<PeminjamanData> list = getAllPeminjaman(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getPeminjamanTotalCount(search);

        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (PeminjamanData p : list) {
            tableModel.addRow(new Object[]{
                p.getIdPeminjaman(),
                p.getNoPeminjaman(),
                p.getNamaAnggota(),
                p.getUsername(),
                p.getTglPeminjaman() != null ? sdf.format(p.getTglPeminjaman()) : "",
                p.getTglKembaliRencana() != null ? sdf.format(p.getTglKembaliRencana()) : "",
                p.getTglKembaliAktual() != null ? sdf.format(p.getTglKembaliAktual()) : "",
                p.getDenda(),
                p.getStatus()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showPeminjamanDialog(PeminjamanData peminjaman) {
        JDialog dialog = new JDialog(this, peminjaman == null ? "Add Peminjaman" : "Edit Peminjaman", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[]"));
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);

        JTextField txtNoPeminjaman = new JTextField(20);
        JComboBox<AnggotaItem> cmbAnggota = new JComboBox<>();
        JComboBox<UserItem> cmbUser = new JComboBox<>();
        JDateChooser dateChooserPinjam = new JDateChooser();
        JDateChooser dateChooserKembaliRencana = new JDateChooser();
        JDateChooser dateChooserKembaliAktual = new JDateChooser();
        JTextField txtDenda = new JTextField(20);
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Dipinjam", "Kembali", "Telat", "Hilang"}); // Example statuses

        // Set Local for DateChooser
        dateChooserPinjam.setLocale(new Locale("id"));
        dateChooserPinjam.setDateFormatString("yyyy-MM-dd");
        dateChooserKembaliRencana.setLocale(new Locale("id"));
        dateChooserKembaliRencana.setDateFormatString("yyyy-MM-dd");
        dateChooserKembaliAktual.setLocale(new Locale("id"));
        dateChooserKembaliAktual.setDateFormatString("yyyy-MM-dd");

        // Load ComboBox Data
        loadAnggotaCombo(cmbAnggota);
        loadUserCombo(cmbUser);

        if (peminjaman != null) {
            txtNoPeminjaman.setText(peminjaman.getNoPeminjaman());
            setSelectedAnggota(cmbAnggota, peminjaman.getIdAnggota());
            setSelectedUser(cmbUser, peminjaman.getIdUser());
            dateChooserPinjam.setDate(peminjaman.getTglPeminjaman());
            dateChooserKembaliRencana.setDate(peminjaman.getTglKembaliRencana());
            dateChooserKembaliAktual.setDate(peminjaman.getTglKembaliAktual());
            txtDenda.setText(String.valueOf(peminjaman.getDenda()));
            cmbStatus.setSelectedItem(peminjaman.getStatus());
        } else {
            // Default values for new
            dateChooserPinjam.setDate(new Date());
            txtDenda.setText("0.0");
        }

        dialog.add(new JLabel("No Peminjaman:"));
        dialog.add(txtNoPeminjaman, "wrap, growx");

        dialog.add(new JLabel("Anggota:"));
        dialog.add(cmbAnggota, "wrap, growx");

        dialog.add(new JLabel("User (Petugas):"));
        dialog.add(cmbUser, "wrap, growx");

        dialog.add(new JLabel("Tgl Peminjaman:"));
        dialog.add(dateChooserPinjam, "wrap, growx");

        dialog.add(new JLabel("Tgl Kembali (Rencana):"));
        dialog.add(dateChooserKembaliRencana, "wrap, growx");

        dialog.add(new JLabel("Tgl Kembali (Aktual):"));
        dialog.add(dateChooserKembaliAktual, "wrap, growx");

        dialog.add(new JLabel("Denda:"));
        dialog.add(txtDenda, "wrap, growx");

        dialog.add(new JLabel("Status:"));
        dialog.add(cmbStatus, "wrap, growx");

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            String no = txtNoPeminjaman.getText();
            AnggotaItem selectedAnggota = (AnggotaItem) cmbAnggota.getSelectedItem();
            UserItem selectedUser = (UserItem) cmbUser.getSelectedItem();
            Date tglPinjam = dateChooserPinjam.getDate();
            Date tglRencana = dateChooserKembaliRencana.getDate();
            Date tglAktual = dateChooserKembaliAktual.getDate();
            String strDenda = txtDenda.getText();
            String status = (String) cmbStatus.getSelectedItem();

            if (selectedAnggota == null || selectedUser == null || tglPinjam == null || tglRencana == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields (No, Anggota, User, Tgl Pinjam, Tgl Rencana).");
                return;
            }

            double denda = 0;
            try {
                denda = Double.parseDouble(strDenda);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format for Denda.");
                return;
            }

            PeminjamanData data = new PeminjamanData(
                (peminjaman == null) ? 0 : peminjaman.getIdPeminjaman(),
                no,
                selectedAnggota.getId(),
                "", // namaAnggota handled by DB/DAO on reload
                selectedUser.getId(),
                "", // username handled by DB/DAO on reload
                tglPinjam,
                tglRencana,
                tglAktual,
                denda,
                status
            );

            if (peminjaman == null) {
                savePeminjaman(data);
            } else {
                updatePeminjaman(data);
            }
            loadData();
            dialog.dispose();
        });

        dialog.add(btnSave, "span, align right, gaptop 10");
        dialog.setVisible(true);
    }

    private void loadAnggotaCombo(JComboBox<AnggotaItem> cmb) {
        cmb.removeAllItems();
        String sql = "SELECT id_anggota, nama FROM anggota ORDER BY nama";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cmb.addItem(new AnggotaItem(rs.getInt("id_anggota"), rs.getString("nama")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserCombo(JComboBox<UserItem> cmb) {
        cmb.removeAllItems();
        // Assuming table 'user' and field 'username'
        String sql = "SELECT id_user, username FROM user ORDER BY username";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cmb.addItem(new UserItem(rs.getInt("id_user"), rs.getString("username")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback strategy if query fails (e.g. invalid column name), beneficial during refactoring if schema is unsure
        }
    }

    private void setSelectedAnggota(JComboBox<AnggotaItem> cmb, int id) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).getId() == id) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
    }

    private void setSelectedUser(JComboBox<UserItem> cmb, int id) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).getId() == id) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
    }

    // =================================== DAO Logic ====================================

    private List<PeminjamanData> getAllPeminjaman(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<PeminjamanData> list = new ArrayList<>();

        // Validate Sort Column to prevent SQL Injection
        if (!sortColumn.matches("p.id_peminjaman|p.no_peminjaman|u.username|p.tgl_peminjaman|p.tgl_kembali_rencana|p.tgl_kembali_aktual|p.denda|p.status")) {
            sortColumn = "p.id_peminjaman";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "DESC";
        }

        String sql = "SELECT p.*, a.nama, u.username " +
                     "FROM peminjaman p " +
                     "LEFT JOIN anggota a ON p.id_anggota = a.id_anggota " +
                     "LEFT JOIN user u ON p.id_user = u.id_user " +
                     "WHERE p.no_peminjaman LIKE ? OR a.nama LIKE ? " +
                     "ORDER BY " + sortColumn + " " + sortOrder + " " +
                     "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchKeyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setInt(3, limit);
            pstmt.setInt(4, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PeminjamanData(
                        rs.getInt("id_peminjaman"),
                        rs.getString("no_peminjaman"),
                        rs.getInt("id_anggota"),
                        rs.getString("nama"),
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getDate("tgl_peminjaman"),
                        rs.getDate("tgl_kembali_rencana"),
                        rs.getDate("tgl_kembali_aktual"),
                        rs.getDouble("denda"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching peminjaman: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getPeminjamanTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM peminjaman p " +
                     "LEFT JOIN anggota a ON p.id_anggota = a.id_anggota " +
                     "WHERE p.no_peminjaman LIKE ? OR a.nama LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchKeyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private void savePeminjaman(PeminjamanData p) {
        String sql = "INSERT INTO peminjaman (no_peminjaman, id_anggota, id_user, tgl_peminjaman, tgl_kembali_rencana, tgl_kembali_aktual, denda, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNoPeminjaman());
            pstmt.setInt(2, p.getIdAnggota());
            pstmt.setInt(3, p.getIdUser());
            pstmt.setDate(4, new java.sql.Date(p.getTglPeminjaman().getTime()));
            pstmt.setDate(5, new java.sql.Date(p.getTglKembaliRencana().getTime()));
            if (p.getTglKembaliAktual() != null) {
                pstmt.setDate(6, new java.sql.Date(p.getTglKembaliAktual().getTime()));
            } else {
                pstmt.setNull(6, java.sql.Types.DATE);
            }
            pstmt.setDouble(7, p.getDenda());
            pstmt.setString(8, p.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving peminjaman: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePeminjaman(PeminjamanData p) {
        String sql = "UPDATE peminjaman SET no_peminjaman=?, id_anggota=?, id_user=?, tgl_peminjaman=?, tgl_kembali_rencana=?, tgl_kembali_aktual=?, denda=?, status=? " +
                     "WHERE id_peminjaman=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNoPeminjaman());
            pstmt.setInt(2, p.getIdAnggota());
            pstmt.setInt(3, p.getIdUser());
            pstmt.setDate(4, new java.sql.Date(p.getTglPeminjaman().getTime()));
            pstmt.setDate(5, new java.sql.Date(p.getTglKembaliRencana().getTime()));
            if (p.getTglKembaliAktual() != null) {
                pstmt.setDate(6, new java.sql.Date(p.getTglKembaliAktual().getTime()));
            } else {
                pstmt.setNull(6, java.sql.Types.DATE);
            }
            pstmt.setDouble(7, p.getDenda());
            pstmt.setString(8, p.getStatus());
            pstmt.setInt(9, p.getIdPeminjaman());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating peminjaman: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePeminjaman(int id) {
        String sql = "DELETE FROM peminjaman WHERE id_peminjaman = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting peminjaman: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PeminjamanData getPeminjamanById(int id) {
        PeminjamanData p = null;
        String sql = "SELECT p.*, a.nama, u.username " +
                     "FROM peminjaman p " +
                     "LEFT JOIN anggota a ON p.id_anggota = a.id_anggota " +
                     "LEFT JOIN user u ON p.id_user = u.id_user " +
                     "WHERE p.id_peminjaman = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    p = new PeminjamanData(
                        rs.getInt("id_peminjaman"),
                        rs.getString("no_peminjaman"),
                        rs.getInt("id_anggota"),
                        rs.getString("nama"),
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getDate("tgl_peminjaman"),
                        rs.getDate("tgl_kembali_rencana"),
                        rs.getDate("tgl_kembali_aktual"),
                        rs.getDouble("denda"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Peminjaman().setVisible(true);
        });
    }
}
