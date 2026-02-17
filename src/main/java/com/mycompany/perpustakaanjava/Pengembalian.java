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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.awt.Dimension;

/**
 * Main application frame for managing Pengembalian.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class Pengembalian extends JFrame {

    // --- Model Class ---
    public static class PengembalianData {
        private int idPengembalian;
        private int idPeminjaman;
        private String noPeminjaman; // Display
        private int idUser;
        private String username; // Display
        private Date tglPengembalian;
        private Date tglRencana; // From peminjaman, for calc
        private int hariTerlambat;
        private String tarifDendaPerHari; // Use String for formatting
        private String totalDenda; // Use String for formatting
        private String kondisiBuku;
        private String statusPembayaran;

        public PengembalianData() {}

        public PengembalianData(int idPengembalian, int idPeminjaman, String noPeminjaman, int idUser, String username,
                                Date tglPengembalian, Date tglRencana, int hariTerlambat, String tarifDendaPerHari,
                                String totalDenda, String kondisiBuku, String statusPembayaran) {
            this.idPengembalian = idPengembalian;
            this.idPeminjaman = idPeminjaman;
            this.noPeminjaman = noPeminjaman;
            this.idUser = idUser;
            this.username = username;
            this.tglPengembalian = tglPengembalian;
            this.tglRencana = tglRencana;
            this.hariTerlambat = hariTerlambat;
            this.tarifDendaPerHari = tarifDendaPerHari;
            this.totalDenda = totalDenda;
            this.kondisiBuku = kondisiBuku;
            this.statusPembayaran = statusPembayaran;
        }

        public int getIdPengembalian() { return idPengembalian; }
        public void setIdPengembalian(int idPengembalian) { this.idPengembalian = idPengembalian; }
        public int getIdPeminjaman() { return idPeminjaman; }
        public void setIdPeminjaman(int idPeminjaman) { this.idPeminjaman = idPeminjaman; }
        public String getNoPeminjaman() { return noPeminjaman; }
        public void setNoPeminjaman(String noPeminjaman) { this.noPeminjaman = noPeminjaman; }
        public int getIdUser() { return idUser; }
        public void setIdUser(int idUser) { this.idUser = idUser; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Date getTglPengembalian() { return tglPengembalian; }
        public void setTglPengembalian(Date tglPengembalian) { this.tglPengembalian = tglPengembalian; }
        public Date getTglRencana() { return tglRencana; }
        public void setTglRencana(Date tglRencana) { this.tglRencana = tglRencana; }
        public int getHariTerlambat() { return hariTerlambat; }
        public void setHariTerlambat(int hariTerlambat) { this.hariTerlambat = hariTerlambat; }
        public String getTarifDendaPerHari() { return tarifDendaPerHari; }
        public void setTarifDendaPerHari(String tarifDendaPerHari) { this.tarifDendaPerHari = tarifDendaPerHari; }
        public String getTotalDenda() { return totalDenda; }
        public void setTotalDenda(String totalDenda) { this.totalDenda = totalDenda; }
        public String getKondisiBuku() { return kondisiBuku; }
        public void setKondisiBuku(String kondisiBuku) { this.kondisiBuku = kondisiBuku; }
        public String getStatusPembayaran() { return statusPembayaran; }
        public void setStatusPembayaran(String statusPembayaran) { this.statusPembayaran = statusPembayaran; }

        @Override
        public String toString() { return noPeminjaman; }
    }

    // Helper classes for ComboBoxes
    public static class PeminjamanItem {
        private int id;
        private String no;
        public PeminjamanItem(int id, String no) { this.id = id; this.no = no; }
        public int getId() { return id; }
        @Override public String toString() { return no; }
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
    private String sortColumn = "id_pengembalian";
    private String sortOrder = "DESC";

    public Pengembalian() {
        setTitle("Master Pengembalian");
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // === Header / Filter Section ===
        JPanel pnlHeader = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        pnlHeader.add(new JLabel("Search (No Peminjaman):"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Pengembalian");
        btnAdd.addActionListener(e -> showPengembalianDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        String[] columnNames = {"ID", "No Peminjaman", "Petugas", "Tgl Kembali", "Tgl Rencana", "Terlambat (Hari)", "Denda/Hari", "Total Denda", "Kondisi", "Status Bayar"};
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
                String[] dbColumns = {"pg.id_pengembalian", "p.no_peminjaman", "u.username", "pg.tgl_pengembalian", "pg.tgl_rencana", "pg.hari_terlambat", "pg.tarif_denda_per_hari", "pg.total_denda", "pg.kondisi_buku", "pg.status_pembayaran"};

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
                        PengembalianData pengembalian = getPengembalianById(id);
                        if (pengembalian != null) {
                            showPengembalianDialog(pengembalian);
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
                PengembalianData pengembalian = getPengembalianById(id);
                if (pengembalian != null) {
                    showPengembalianDialog(pengembalian);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) table.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(Pengembalian.this, "Are you sure you want to delete this pengembalian?", "Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deletePengembalian(id);
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

        List<PengembalianData> list = getAllPengembalian(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getPengembalianTotalCount(search);

        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (PengembalianData p : list) {
            tableModel.addRow(new Object[]{
                p.getIdPengembalian(),
                p.getNoPeminjaman(),
                p.getUsername(),
                p.getTglPengembalian() != null ? sdf.format(p.getTglPengembalian()) : "",
                p.getTglRencana() != null ? sdf.format(p.getTglRencana()) : "",
                p.getHariTerlambat(),
                p.getTarifDendaPerHari(),
                p.getTotalDenda(),
                p.getKondisiBuku(),
                p.getStatusPembayaran()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showPengembalianDialog(PengembalianData pengembalian) {
        JDialog dialog = new JDialog(this, pengembalian == null ? "Add Pengembalian" : "Edit Pengembalian", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[]"));
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JComboBox<PeminjamanItem> cmbPeminjaman = new JComboBox<>();
        JComboBox<UserItem> cmbUser = new JComboBox<>();
        JDateChooser dateChooserPengembalian = new JDateChooser();
        JDateChooser dateChooserRencana = new JDateChooser();
        
        // --- Added: Member Search Fields ---
        JTextField txtNoAnggota = new JTextField(20); // Barcode Input
        JTextField txtNama = new JTextField(20); txtNama.setEditable(false);
        JTextField txtJK = new JTextField(20); txtJK.setEditable(false);
        JTextField txtAlamat = new JTextField(20); txtAlamat.setEditable(false);
        JTextField txtNoTelepon = new JTextField(20); txtNoTelepon.setEditable(false);
        JTextField txtNoIdentitas = new JTextField(20); txtNoIdentitas.setEditable(false);

        // Define search logic here (need forward declaration or final array)
        final int[] selectedAnggotaId = new int[]{-1}; 
        
        JTextField txtHariTerlambat = new JTextField(20);
        JTextField txtTarifDenda = new JTextField(20);
        JTextField txtTotalDenda = new JTextField(20);
        JComboBox<String> cmbKondisiBuku = new JComboBox<>(new String[]{"Baik", "Rusak Ringan", "Rusak Berat", "Hilang"});
        JComboBox<String> cmbStatusPembayaran = new JComboBox<>(new String[]{"Lunas", "Belum Lunas", "Waived"});

        dateChooserPengembalian.setLocale(new Locale("id"));
        dateChooserPengembalian.setDateFormatString("yyyy-MM-dd");
        dateChooserRencana.setLocale(new Locale("id"));
        dateChooserRencana.setDateFormatString("yyyy-MM-dd");

        dateChooserRencana.setDateFormatString("yyyy-MM-dd");

        // --- Logic for Member Search ---
        java.awt.event.ActionListener searchAction = e -> {
            String keyword = txtNoAnggota.getText().trim();
            if (keyword.isEmpty()) return;
            
            // Reuse Anggota Data Inner Class (Requires Anggota.java to have public static inner class AnggotaData)
            // Since Peminjaman.java uses AnggotaData, I assume it's accessible or I need to import/duplicate it.
            // Based on previous file view, Anggota.AnggotaData is public static.
            Anggota.AnggotaData anggota = getAnggotaByBarcodeOrId(keyword);
            
            if (anggota != null) {
                selectedAnggotaId[0] = anggota.getIdAnggota();
                txtNama.setText(anggota.getNama());
                txtJK.setText(anggota.getJenisKelamin());
                txtAlamat.setText(anggota.getAlamat());
                txtNoTelepon.setText(anggota.getNoTelepon());
                txtNoIdentitas.setText(anggota.getNoIdentitas());
                txtNoAnggota.setText(anggota.getNoAnggota());
                
                // Load Active Loans for this Member
                loadActivePeminjamanByAnggota(cmbPeminjaman, anggota.getIdAnggota());
            } else {
                selectedAnggotaId[0] = -1;
                txtNama.setText("");
                txtJK.setText("");
                txtAlamat.setText("");
                txtNoTelepon.setText("");
                txtNoIdentitas.setText("");
                cmbPeminjaman.removeAllItems(); // Clear loans if member not found
                JOptionPane.showMessageDialog(dialog, "Anggota not found for: " + keyword, "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        };
        txtNoAnggota.addActionListener(searchAction);

        // loadPeminjamanCombo(cmbPeminjaman); // Removed default load, only load when member found or editing
        loadUserCombo(cmbUser);

        loadUserCombo(cmbUser);

        if (pengembalian != null) {
             // If editing, we need to load the Peminjaman combo correctly 
             // Ideally we should know the member ID from the existing loan record.
             // But existing PengembalianData doesn't store member ID directly (only through Peminjaman).
             // We can fetch member ID based on pengembalian.getIdPeminjaman().
             // For simplicity, let's load ALL active loans or specifically the current one + others for that member.
             // Let's implement helper getAnggotaIdByPeminjamanId
             int memberId = getAnggotaIdByPeminjamanId(pengembalian.getIdPeminjaman());
             if (memberId != -1) {
                  Anggota.AnggotaData a = getAnggotaById(memberId);
                  if (a != null) {
                      selectedAnggotaId[0] = a.getIdAnggota();
                      txtNoAnggota.setText(a.getNoAnggota());
                      txtNama.setText(a.getNama());
                      txtJK.setText(a.getJenisKelamin());
                      txtAlamat.setText(a.getAlamat());
                      txtNoTelepon.setText(a.getNoTelepon());
                      txtNoIdentitas.setText(a.getNoIdentitas());
                      loadActivePeminjamanByAnggota(cmbPeminjaman, memberId); // Load loans for this member
                  }
             }
             // Then select the current one
            setSelectedPeminjaman(cmbPeminjaman, pengembalian.getIdPeminjaman());
            setSelectedUser(cmbUser, pengembalian.getIdUser());
            dateChooserPengembalian.setDate(pengembalian.getTglPengembalian());
            dateChooserRencana.setDate(pengembalian.getTglRencana());
            txtHariTerlambat.setText(String.valueOf(pengembalian.getHariTerlambat()));
            txtTarifDenda.setText(pengembalian.getTarifDendaPerHari());
            txtTotalDenda.setText(pengembalian.getTotalDenda());
            cmbKondisiBuku.setSelectedItem(pengembalian.getKondisiBuku());
            cmbStatusPembayaran.setSelectedItem(pengembalian.getStatusPembayaran());
        } else {
            dateChooserPengembalian.setDate(new Date()); // Default today
            txtHariTerlambat.setText("0");
            txtTarifDenda.setText("0"); 
            txtTotalDenda.setText("0");
        }

        // Add Logic to Auto Calc Late Days
        dateChooserPengembalian.addPropertyChangeListener(e -> calculateLateDays(dateChooserPengembalian, dateChooserRencana, txtHariTerlambat, txtTarifDenda, txtTotalDenda));
        dateChooserRencana.addPropertyChangeListener(e -> calculateLateDays(dateChooserPengembalian, dateChooserRencana, txtHariTerlambat, txtTarifDenda, txtTotalDenda));
        // Add Logic to Auto Calc Total Denda if tarif changes
        txtTarifDenda.addActionListener(e -> calculateLateDays(dateChooserPengembalian, dateChooserRencana, txtHariTerlambat, txtTarifDenda, txtTotalDenda));

        // KeyListener for formatting Tarif Denda
        txtTarifDenda.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    String text = txtTarifDenda.getText().replaceAll(",", "");
                    if (!text.isEmpty()) {
                        long number = Long.parseLong(text);
                        String formatted = NumberFormat.getNumberInstance(Locale.US).format(number);
                        if (!txtTarifDenda.getText().equals(formatted)) {
                             txtTarifDenda.setText(formatted);
                        }
                    }
                } catch (NumberFormatException ex) {
                }
            }
        });

        // KeyListener for formatting Total Denda
        txtTotalDenda.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    String text = txtTotalDenda.getText().replaceAll(",", "");
                    if (!text.isEmpty()) {
                        long number = Long.parseLong(text);
                        String formatted = NumberFormat.getNumberInstance(Locale.US).format(number);
                        if (!txtTotalDenda.getText().equals(formatted)) {
                             txtTotalDenda.setText(formatted);
                        }
                    }
                } catch (NumberFormatException ex) {
                }
            }
        });


        
        dialog.add(new JLabel("Scan Barcode / No Anggota:"));
        dialog.add(txtNoAnggota, "wrap, growx");
        dialog.add(new JLabel("(Press Enter to Search)"), "wrap, al right");

        dialog.add(new JLabel("Nama:"));
        dialog.add(txtNama, "wrap, growx");
        dialog.add(new JLabel("Jenis Kelamin:"));
        dialog.add(txtJK, "wrap, growx");
        dialog.add(new JLabel("Alamat:"));
        dialog.add(txtAlamat, "wrap, growx");
        dialog.add(new JLabel("No Telepon:"));
        dialog.add(txtNoTelepon, "wrap, growx");
        dialog.add(new JLabel("No Identitas:"));
        dialog.add(txtNoIdentitas, "wrap, growx");
        
        dialog.add(new JLabel("No Peminjaman (Active):"));
        dialog.add(cmbPeminjaman, "wrap, growx");
        dialog.add(new JLabel("Petugas (User):"));
        dialog.add(cmbUser, "wrap, growx");
        dialog.add(new JLabel("Tgl Pengembalian:"));
        dialog.add(dateChooserPengembalian, "wrap, growx");
        dialog.add(new JLabel("Tgl Rencana Kembali:"));
        dialog.add(dateChooserRencana, "wrap, growx");
        dialog.add(new JLabel("Hari Terlambat:"));
        dialog.add(txtHariTerlambat, "wrap, growx");
        dialog.add(new JLabel("Tarif Denda/Hari:"));
        dialog.add(txtTarifDenda, "wrap, growx");
        dialog.add(new JLabel("Total Denda:"));
        dialog.add(txtTotalDenda, "wrap, growx");
        dialog.add(new JLabel("Kondisi Buku:"));
        dialog.add(cmbKondisiBuku, "wrap, growx");
        dialog.add(new JLabel("Status Pembayaran:"));
        dialog.add(cmbStatusPembayaran, "wrap, growx");

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            PeminjamanItem selectedPeminjaman = (PeminjamanItem) cmbPeminjaman.getSelectedItem();
            UserItem selectedUser = (UserItem) cmbUser.getSelectedItem();
            Date tglKembali = dateChooserPengembalian.getDate();
            Date tglRencana = dateChooserRencana.getDate();
            String strTerlambat = txtHariTerlambat.getText();
            String strTarif = txtTarifDenda.getText();
            String strTotal = txtTotalDenda.getText();
            String kondisi = (String) cmbKondisiBuku.getSelectedItem();
            String status = (String) cmbStatusPembayaran.getSelectedItem();

            if (selectedPeminjaman == null || selectedUser == null || tglKembali == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in required fields.");
                return;
            }

            int terlambat = 0;
            // Removed double vars for tarif/total as they are strings now, handled below
            try {
                terlambat = Integer.parseInt(strTerlambat);
                Double.parseDouble(strTarif.replaceAll(",", "")); // Validate format
                Double.parseDouble(strTotal.replaceAll(",", "")); // Validate format
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format.");
                return;
            }

            PengembalianData newPengembalian = new PengembalianData(
                (pengembalian == null) ? 0 : pengembalian.getIdPengembalian(),
                selectedPeminjaman.getId(),
                "", // No handled by DAO
                selectedUser.getId(),
                "", // Name handled by DAO
                tglKembali,
                tglRencana,
                terlambat,
                strTarif, // Store string
                strTotal, // Store string
                kondisi,
                status
            );

            if (pengembalian == null) {
                savePengembalian(newPengembalian);
            } else {
                updatePengembalian(newPengembalian);
            }
            loadData();
            dialog.dispose();
        });

        dialog.add(btnSave, "span, align right, gaptop 10");
        dialog.setVisible(true);
    }

    private void calculateLateDays(JDateChooser d1, JDateChooser d2, JTextField txtDays, JTextField txtTarif, JTextField txtTotal) {
        Date returned = d1.getDate();
        Date planned = d2.getDate();
        if (returned != null && planned != null) {
            long diffInMillies = returned.getTime() - planned.getTime();
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            int days = (int) diff;
            if (days < 0) days = 0;
            txtDays.setText(String.valueOf(days));

            try {
                double tarif = Double.parseDouble(txtTarif.getText().replaceAll(",", ""));
                double total = days * tarif;
                txtTotal.setText(NumberFormat.getNumberInstance(Locale.US).format(total));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
    }

    // --- Helper for Scanning Anggota ---
    private Anggota.AnggotaData getAnggotaByBarcodeOrId(String keyword) {
        Anggota.AnggotaData a = null;
        String sql = "SELECT * FROM anggota WHERE no_anggota = ? OR no_barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword);
            pstmt.setString(2, keyword);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    a = new Anggota.AnggotaData(
                        rs.getInt("id_anggota"),
                        rs.getString("no_anggota"),
                        rs.getString("nama"),
                        rs.getString("jenis_kelamin"),
                        rs.getString("tempat_lahir"),
                        rs.getDate("tanggal_lahir"),
                        rs.getString("alamat"),
                        rs.getString("no_telepon"),
                        rs.getString("email"),
                        rs.getString("no_identitas"),
                        rs.getDate("tgl_daftar"),
                        rs.getDate("tgl_expired"),
                        rs.getString("status"),
                        rs.getString("no_barcode"),
                        rs.getString("nama_photo")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching anggota: " + e.getMessage());
        }
        return a;
    }
    
    private Anggota.AnggotaData getAnggotaById(int id) {
        Anggota.AnggotaData a = null;
        String sql = "SELECT * FROM anggota WHERE id_anggota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    a = new Anggota.AnggotaData(
                        rs.getInt("id_anggota"),
                        rs.getString("no_anggota"),
                        rs.getString("nama"),
                        rs.getString("jenis_kelamin"),
                        rs.getString("tempat_lahir"),
                        rs.getDate("tanggal_lahir"),
                        rs.getString("alamat"),
                        rs.getString("no_telepon"),
                        rs.getString("email"),
                        rs.getString("no_identitas"),
                        rs.getDate("tgl_daftar"),
                        rs.getDate("tgl_expired"),
                        rs.getString("status"),
                        rs.getString("no_barcode"),
                        rs.getString("nama_photo")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return a;
    }

    // Helper to filtered active loans by member
    private void loadActivePeminjamanByAnggota(JComboBox<PeminjamanItem> cmb, int memberId) {
        cmb.removeAllItems();
        // Load only loans with status 'Dipinjam' (or whatever indicates active) for this member
        String sql = "SELECT id_peminjaman, no_peminjaman FROM peminjaman WHERE id_anggota = ? AND status = 'Dipinjam' ORDER BY no_peminjaman DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, memberId);
             try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cmb.addItem(new PeminjamanItem(rs.getInt("id_peminjaman"), rs.getString("no_peminjaman")));
                }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private int getAnggotaIdByPeminjamanId(int peminjamanId) {
        int id = -1;
        String sql = "SELECT id_anggota FROM peminjaman WHERE id_peminjaman = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, peminjamanId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id_anggota");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private void loadUserCombo(JComboBox<UserItem> cmb) {
        cmb.removeAllItems();
        String sql = "SELECT id_user, username FROM user ORDER BY username ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cmb.addItem(new UserItem(rs.getInt("id_user"), rs.getString("username")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setSelectedPeminjaman(JComboBox<PeminjamanItem> cmb, int id) {
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

    private List<PengembalianData> getAllPengembalian(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<PengembalianData> list = new ArrayList<>();

        if (!sortColumn.matches("pg.id_pengembalian|p.no_peminjaman|u.username|pg.tgl_pengembalian|pg.tgl_rencana|pg.hari_terlambat|pg.tarif_denda_per_hari|pg.total_denda|pg.kondisi_buku|pg.status_pembayaran")) {
            sortColumn = "pg.id_pengembalian";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "DESC"; // Default newest
        }

        String sql = "SELECT pg.id_pengembalian, pg.id_peminjaman, pg.id_user, pg.tgl_pengembalian, pg.tgl_rencana, pg.hari_terlambat, " +
                     "FORMAT(pg.tarif_denda_per_hari, 0) as tarif_denda_per_hari, " +
                     "FORMAT(pg.total_denda, 0) as total_denda, " +
                     "pg.kondisi_buku, pg.status_pembayaran, p.no_peminjaman, u.username " +
                     "FROM pengembalian pg " +
                     "LEFT JOIN peminjaman p ON pg.id_peminjaman = p.id_peminjaman " +
                     "LEFT JOIN user u ON pg.id_user = u.id_user ";

        boolean hasSearch = searchKeyword != null && !searchKeyword.trim().isEmpty();
        if (hasSearch) {
            sql += "WHERE p.no_peminjaman LIKE ? OR u.username LIKE ? ";
        }

        sql += "ORDER BY " + sortColumn + " " + sortOrder + " " +
               "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (hasSearch) {
                String searchPattern = "%" + searchKeyword + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            pstmt.setInt(paramIndex++, limit);
            pstmt.setInt(paramIndex++, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PengembalianData(
                        rs.getInt("id_pengembalian"),
                        rs.getInt("id_peminjaman"),
                        rs.getString("no_peminjaman"),
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getDate("tgl_pengembalian"),
                        rs.getDate("tgl_rencana"),
                        rs.getInt("hari_terlambat"),
                        rs.getString("tarif_denda_per_hari"),
                        rs.getString("total_denda"),
                        rs.getString("kondisi_buku"),
                        rs.getString("status_pembayaran")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching pengembalian: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getPengembalianTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM pengembalian pg " +
                     "LEFT JOIN peminjaman p ON pg.id_peminjaman = p.id_peminjaman " +
                     "LEFT JOIN user u ON pg.id_user = u.id_user ";

        boolean hasSearch = searchKeyword != null && !searchKeyword.trim().isEmpty();
        if (hasSearch) {
            sql += "WHERE p.no_peminjaman LIKE ? OR u.username LIKE ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (hasSearch) {
                String searchPattern = "%" + searchKeyword + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }

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

    private void savePengembalian(PengembalianData pg) {
        String sql = "INSERT INTO pengembalian (id_peminjaman, id_user, tgl_pengembalian, tgl_rencana, hari_terlambat, tarif_denda_per_hari, total_denda, kondisi_buku, status_pembayaran) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pg.getIdPeminjaman());
            pstmt.setInt(2, pg.getIdUser());
            pstmt.setDate(3, new java.sql.Date(pg.getTglPengembalian().getTime()));
            pstmt.setDate(4, pg.getTglRencana() != null ? new java.sql.Date(pg.getTglRencana().getTime()) : null);
            pstmt.setInt(5, pg.getHariTerlambat());
            pstmt.setDouble(6, Double.parseDouble(pg.getTarifDendaPerHari().replaceAll(",", "")));
            pstmt.setDouble(7, Double.parseDouble(pg.getTotalDenda().replaceAll(",", "")));
            pstmt.setString(8, pg.getKondisiBuku());
            pstmt.setString(9, pg.getStatusPembayaran());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving pengembalian: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePengembalian(PengembalianData pg) {
        String sql = "UPDATE pengembalian SET id_peminjaman=?, id_user=?, tgl_pengembalian=?, tgl_rencana=?, hari_terlambat=?, tarif_denda_per_hari=?, total_denda=?, kondisi_buku=?, status_pembayaran=? " +
                     "WHERE id_pengembalian=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pg.getIdPeminjaman());
            pstmt.setInt(2, pg.getIdUser());
            pstmt.setDate(3, new java.sql.Date(pg.getTglPengembalian().getTime()));
            pstmt.setDate(4, pg.getTglRencana() != null ? new java.sql.Date(pg.getTglRencana().getTime()) : null);
            pstmt.setInt(5, pg.getHariTerlambat());
            pstmt.setDouble(6, Double.parseDouble(pg.getTarifDendaPerHari().replaceAll(",", "")));
            pstmt.setDouble(7, Double.parseDouble(pg.getTotalDenda().replaceAll(",", "")));
            pstmt.setString(8, pg.getKondisiBuku());
            pstmt.setString(9, pg.getStatusPembayaran());
            pstmt.setInt(10, pg.getIdPengembalian());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating pengembalian: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePengembalian(int id) {
        String sql = "DELETE FROM pengembalian WHERE id_pengembalian = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting pengembalian: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PengembalianData getPengembalianById(int id) {
        PengembalianData pg = null;
        String sql = "SELECT pg.id_pengembalian, pg.id_peminjaman, pg.id_user, pg.tgl_pengembalian, pg.tgl_rencana, pg.hari_terlambat, " +
                     "FORMAT(pg.tarif_denda_per_hari, 0) as tarif_denda_per_hari, " +
                     "FORMAT(pg.total_denda, 0) as total_denda, " +
                     "pg.kondisi_buku, pg.status_pembayaran, p.no_peminjaman, u.username " +
                     "FROM pengembalian pg " +
                     "LEFT JOIN peminjaman p ON pg.id_peminjaman = p.id_peminjaman " +
                     "LEFT JOIN user u ON pg.id_user = u.id_user " +
                     "WHERE pg.id_pengembalian = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pg = new PengembalianData(
                        rs.getInt("id_pengembalian"),
                        rs.getInt("id_peminjaman"),
                        rs.getString("no_peminjaman"),
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getDate("tgl_pengembalian"),
                        rs.getDate("tgl_rencana"),
                        rs.getInt("hari_terlambat"),
                        rs.getString("tarif_denda_per_hari"),
                        rs.getString("total_denda"),
                        rs.getString("kondisi_buku"),
                        rs.getString("status_pembayaran")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pg;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Pengembalian().setVisible(true);
        });
    }
}
