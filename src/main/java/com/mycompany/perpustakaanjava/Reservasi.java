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
 * Main application frame for managing Reservasi.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class Reservasi extends JFrame {

    // --- Model Class ---
    public static class ReservasiData {
        private int idReservasi;
        private int idAnggota;
        private int idBuku;
        private Date tglReservasi;
        private Date tglBerakhir;
        private String status;

        public ReservasiData() {}

        public ReservasiData(int idReservasi, int idAnggota, int idBuku, Date tglReservasi, Date tglBerakhir, String status) {
            this.idReservasi = idReservasi;
            this.idAnggota = idAnggota;
            this.idBuku = idBuku;
            this.tglReservasi = tglReservasi;
            this.tglBerakhir = tglBerakhir;
            this.status = status;
        }

        public int getIdReservasi() { return idReservasi; }
        public void setIdReservasi(int idReservasi) { this.idReservasi = idReservasi; }
        public int getIdAnggota() { return idAnggota; }
        public void setIdAnggota(int idAnggota) { this.idAnggota = idAnggota; }
        public int getIdBuku() { return idBuku; }
        public void setIdBuku(int idBuku) { this.idBuku = idBuku; }
        public Date getTglReservasi() { return tglReservasi; }
        public void setTglReservasi(Date tglReservasi) { this.tglReservasi = tglReservasi; }
        public Date getTglBerakhir() { return tglBerakhir; }
        public void setTglBerakhir(Date tglBerakhir) { this.tglBerakhir = tglBerakhir; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // Helper classes for ComboBoxes
    public static class AnggotaItem {
        private int id;
        private String nama;
        public AnggotaItem(int id, String nama) { this.id = id; this.nama = nama; }
        public int getId() { return id; }
        @Override public String toString() { return id + " - " + nama; }
    }

    public static class BukuItem {
        private int id;
        private String judul;
        public BukuItem(int id, String judul) { this.id = id; this.judul = judul; }
        public int getId() { return id; }
        @Override public String toString() { return id + " - " + judul; }
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
    private String sortColumn = "id_reservasi";
    private String sortOrder = "DESC";

    public Reservasi() {
        setTitle("Master Reservasi");
        setSize(900, 600);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // === Header / Filter Section ===
        JPanel pnlHeader = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        pnlHeader.add(new JLabel("Search (ID Anggota/Buku):")); // Search by ID
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Reservasi");
        btnAdd.addActionListener(e -> showReservasiDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        String[] columnNames = {"ID Reservasi", "ID Anggota", "ID Buku", "Tgl Reservasi", "Tgl Berakhir", "Status"};
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
                String[] dbColumns = {"id_reservasi", "id_anggota", "id_buku", "tgl_reservasi", "tgl_berakhir", "status"};

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
                        ReservasiData reservasi = getReservasiById(id);
                        if (reservasi != null) {
                            showReservasiDialog(reservasi);
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
                ReservasiData reservasi = getReservasiById(id);
                if (reservasi != null) {
                    showReservasiDialog(reservasi);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) table.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(Reservasi.this, "Are you sure you want to delete this reservasi?", "Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteReservasi(id);
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

        List<ReservasiData> list = getAllReservasi(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getReservasiTotalCount(search);

        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (ReservasiData r : list) {
            tableModel.addRow(new Object[]{
                r.getIdReservasi(),
                r.getIdAnggota(),
                r.getIdBuku(),
                r.getTglReservasi() != null ? sdf.format(r.getTglReservasi()) : "",
                r.getTglBerakhir() != null ? sdf.format(r.getTglBerakhir()) : "",
                r.getStatus()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showReservasiDialog(ReservasiData reservasi) {
        JDialog dialog = new JDialog(this, reservasi == null ? "Add Reservasi" : "Edit Reservasi", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[]"));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JComboBox<AnggotaItem> cmbAnggota = new JComboBox<>();
        JComboBox<BukuItem> cmbBuku = new JComboBox<>();
        JDateChooser dateChooserReservasi = new JDateChooser();
        JDateChooser dateChooserBerakhir = new JDateChooser();
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Aktif", "Selesai", "Batal", "Expired"});

        dateChooserReservasi.setLocale(new Locale("id"));
        dateChooserReservasi.setDateFormatString("yyyy-MM-dd");
        dateChooserBerakhir.setLocale(new Locale("id"));
        dateChooserBerakhir.setDateFormatString("yyyy-MM-dd");

        loadAnggotaCombo(cmbAnggota);
        loadBukuCombo(cmbBuku);

        if (reservasi != null) {
            setSelectedAnggota(cmbAnggota, reservasi.getIdAnggota());
            setSelectedBuku(cmbBuku, reservasi.getIdBuku());
            dateChooserReservasi.setDate(reservasi.getTglReservasi());
            dateChooserBerakhir.setDate(reservasi.getTglBerakhir());
            cmbStatus.setSelectedItem(reservasi.getStatus());
        } else {
            dateChooserReservasi.setDate(new Date()); // Default today
        }

        dialog.add(new JLabel("Anggota:"));
        dialog.add(cmbAnggota, "wrap, growx");
        dialog.add(new JLabel("Buku:"));
        dialog.add(cmbBuku, "wrap, growx");
        dialog.add(new JLabel("Tgl Reservasi:"));
        dialog.add(dateChooserReservasi, "wrap, growx");
        dialog.add(new JLabel("Tgl Berakhir:"));
        dialog.add(dateChooserBerakhir, "wrap, growx");
        dialog.add(new JLabel("Status:"));
        dialog.add(cmbStatus, "wrap, growx");

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            AnggotaItem selectedAnggota = (AnggotaItem) cmbAnggota.getSelectedItem();
            BukuItem selectedBuku = (BukuItem) cmbBuku.getSelectedItem();
            Date tglReservasi = dateChooserReservasi.getDate();
            Date tglBerakhir = dateChooserBerakhir.getDate();
            String status = (String) cmbStatus.getSelectedItem();

            if (selectedAnggota == null || selectedBuku == null || tglReservasi == null || tglBerakhir == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.");
                return;
            }

            ReservasiData newReservasi = new ReservasiData(
                (reservasi == null) ? 0 : reservasi.getIdReservasi(),
                selectedAnggota.getId(),
                selectedBuku.getId(),
                tglReservasi,
                tglBerakhir,
                status
            );

            if (reservasi == null) {
                saveReservasi(newReservasi);
            } else {
                updateReservasi(newReservasi);
            }
            loadData();
            dialog.dispose();
        });

        dialog.add(btnSave, "span, align right, gaptop 10");
        dialog.setVisible(true);
    }

    private void loadAnggotaCombo(JComboBox<AnggotaItem> cmb) {
        cmb.removeAllItems();
        String sql = "SELECT id_anggota, nama FROM anggota ORDER BY nama ASC";
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

    private void loadBukuCombo(JComboBox<BukuItem> cmb) {
        cmb.removeAllItems();
        String sql = "SELECT id_buku, judul FROM buku ORDER BY judul ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cmb.addItem(new BukuItem(rs.getInt("id_buku"), rs.getString("judul")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    private void setSelectedBuku(JComboBox<BukuItem> cmb, int id) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).getId() == id) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
    }

    // =================================== DAO Logic ====================================

    private List<ReservasiData> getAllReservasi(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<ReservasiData> list = new ArrayList<>();

        if (!sortColumn.matches("id_reservasi|id_anggota|id_buku|tgl_reservasi|tgl_berakhir|status")) {
            sortColumn = "id_reservasi";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "DESC";
        }

        // Just select from table, no joins for display as requested ("id only showing integer")
        // But search might need to be by ID if we don't join.
        // Assuming search by ID string if user types number.
        String sql = "SELECT * FROM reservasi " +
                     "WHERE CAST(id_anggota AS CHAR) LIKE ? OR CAST(id_buku AS CHAR) LIKE ? " +
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
                    list.add(new ReservasiData(
                        rs.getInt("id_reservasi"),
                        rs.getInt("id_anggota"),
                        rs.getInt("id_buku"),
                        rs.getDate("tgl_reservasi"),
                        rs.getDate("tgl_berakhir"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching reservasi: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getReservasiTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM reservasi " +
                     "WHERE CAST(id_anggota AS CHAR) LIKE ? OR CAST(id_buku AS CHAR) LIKE ?";

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

    private void saveReservasi(ReservasiData r) {
        String sql = "INSERT INTO reservasi (id_anggota, id_buku, tgl_reservasi, tgl_berakhir, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, r.getIdAnggota());
            pstmt.setInt(2, r.getIdBuku());
            pstmt.setDate(3, new java.sql.Date(r.getTglReservasi().getTime()));
            pstmt.setDate(4, new java.sql.Date(r.getTglBerakhir().getTime()));
            pstmt.setString(5, r.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving reservasi: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReservasi(ReservasiData r) {
        String sql = "UPDATE reservasi SET id_anggota=?, id_buku=?, tgl_reservasi=?, tgl_berakhir=?, status=? WHERE id_reservasi=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, r.getIdAnggota());
            pstmt.setInt(2, r.getIdBuku());
            pstmt.setDate(3, new java.sql.Date(r.getTglReservasi().getTime()));
            pstmt.setDate(4, new java.sql.Date(r.getTglBerakhir().getTime()));
            pstmt.setString(5, r.getStatus());
            pstmt.setInt(6, r.getIdReservasi());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating reservasi: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReservasi(int id) {
        String sql = "DELETE FROM reservasi WHERE id_reservasi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting reservasi: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ReservasiData getReservasiById(int id) {
        ReservasiData reservasi = null;
        String sql = "SELECT * FROM reservasi WHERE id_reservasi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    reservasi = new ReservasiData(
                        rs.getInt("id_reservasi"),
                        rs.getInt("id_anggota"),
                        rs.getInt("id_buku"),
                        rs.getDate("tgl_reservasi"),
                        rs.getDate("tgl_berakhir"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservasi;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Reservasi().setVisible(true);
        });
    }
}
