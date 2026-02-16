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
import java.util.Date;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Function;
import javax.swing.SwingUtilities;
import javax.swing.DefaultComboBoxModel;

/**
 * Main application frame for managing Buku.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class Buku extends JFrame {

    // --- Model Class 
    public static class BukuData {
        private int idBuku;
        private String noBarcode;
        private String isbn;
        private String judul;
        private int idPengarang;
        private String namaPengarang; // For display
        private int idPenerbit;
        private String namaPenerbit; // For display
        private int idKategori;
        private String namaKategori; // For display
        private int tahunTerbit;
        private String edisi;
        private int halaman;
        private String bahasa;
        private int idRak;
        private String lokasiRak; // For display
        private int stokTotal;
        private int stokTersedia;
        private double harga;
        private java.sql.Date tglMasuk;
        private String status;

        public BukuData() {}

        // Constructor with all fields
        public BukuData(int idBuku, String noBarcode, String isbn, String judul, int idPengarang, String namaPengarang, 
                        int idPenerbit, String namaPenerbit, int idKategori, String namaKategori, 
                        int tahunTerbit, String edisi, int halaman, String bahasa, 
                        int idRak, String lokasiRak, int stokTotal, int stokTersedia, 
                        double harga, java.sql.Date tglMasuk, String status) {
            this.idBuku = idBuku;
            this.noBarcode = noBarcode;
            this.isbn = isbn;
            this.judul = judul;
            this.idPengarang = idPengarang;
            this.namaPengarang = namaPengarang;
            this.idPenerbit = idPenerbit;
            this.namaPenerbit = namaPenerbit;
            this.idKategori = idKategori;
            this.namaKategori = namaKategori;
            this.tahunTerbit = tahunTerbit;
            this.edisi = edisi;
            this.halaman = halaman;
            this.bahasa = bahasa;
            this.idRak = idRak;
            this.lokasiRak = lokasiRak;
            this.stokTotal = stokTotal;
            this.stokTersedia = stokTersedia;
            this.harga = harga;
            this.tglMasuk = tglMasuk;
            this.status = status;
        }

        // Getters and Setters
        public int getIdBuku() { return idBuku; }
        public void setIdBuku(int idBuku) { this.idBuku = idBuku; }

        public String getNoBarcode() { return noBarcode; }
        public void setNoBarcode(String noBarcode) { this.noBarcode = noBarcode; }

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }

        public String getJudul() { return judul; }
        public void setJudul(String judul) { this.judul = judul; }

        public int getIdPengarang() { return idPengarang; }
        public void setIdPengarang(int idPengarang) { this.idPengarang = idPengarang; }
        public String getNamaPengarang() { return namaPengarang; }
        public void setNamaPengarang(String namaPengarang) { this.namaPengarang = namaPengarang; }

        public int getIdPenerbit() { return idPenerbit; }
        public void setIdPenerbit(int idPenerbit) { this.idPenerbit = idPenerbit; }
        public String getNamaPenerbit() { return namaPenerbit; }
        public void setNamaPenerbit(String namaPenerbit) { this.namaPenerbit = namaPenerbit; }

        public int getIdKategori() { return idKategori; }
        public void setIdKategori(int idKategori) { this.idKategori = idKategori; }
        public String getNamaKategori() { return namaKategori; }
        public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

        public int getTahunTerbit() { return tahunTerbit; }
        public void setTahunTerbit(int tahunTerbit) { this.tahunTerbit = tahunTerbit; }

        public String getEdisi() { return edisi; }
        public void setEdisi(String edisi) { this.edisi = edisi; }

        public int getHalaman() { return halaman; }
        public void setHalaman(int halaman) { this.halaman = halaman; }

        public String getBahasa() { return bahasa; }
        public void setBahasa(String bahasa) { this.bahasa = bahasa; }

        public int getIdRak() { return idRak; }
        public void setIdRak(int idRak) { this.idRak = idRak; }
        public String getLokasiRak() { return lokasiRak; }
        public void setLokasiRak(String lokasiRak) { this.lokasiRak = lokasiRak; }

        public int getStokTotal() { return stokTotal; }
        public void setStokTotal(int stokTotal) { this.stokTotal = stokTotal; }

        public int getStokTersedia() { return stokTersedia; }
        public void setStokTersedia(int stokTersedia) { this.stokTersedia = stokTersedia; }

        public double getHarga() { return harga; }
        public void setHarga(double harga) { this.harga = harga; }

        public java.sql.Date getTglMasuk() { return tglMasuk; }
        public void setTglMasuk(java.sql.Date tglMasuk) { this.tglMasuk = tglMasuk; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() { return judul; }
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
    private String sortColumn = "id_buku";
    private String sortOrder = "ASC";

    public Buku() {
        setTitle("Master Data Buku");
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
        pnlHeader.add(new JLabel("Search:"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Buku");
        btnAdd.addActionListener(e -> showBukuDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        // ID, ISBN, Judul, Pengarang, Penerbit, Kategori, Tahun, Stok, Lokasi Rak
        String[] columnNames = {"ID", "No Barcode", "ISBN", "Judul", "Pengarang", "Penerbit", "Kategori", "Tahun", "Stok Total", "Stok Tersedia", "Lokasi Rak"};
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
                String[] dbColumns = {"id_buku", "no_barcode", "isbn", "judul", "nama_pengarang", "nama_penerbit", "nama_kategori", "tahun_terbit", "stok_total", "stok_tersedia", "lokasi_rak"};

                if (col >= 0 && col < dbColumns.length) {
                    String clickedColumn = dbColumns[col];
                     // Map display columns to actual DB columns for sorting if needed (handling joined columns)
                     // For simplicity, we use the alias names from the SQL query
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
                        BukuData b = getBukuById(id);
                        if (b != null) {
                            showBukuDialog(b);
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
                BukuData b = getBukuById(id);
                if (b != null) {
                    showBukuDialog(b);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) table.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(Buku.this, "Are you sure you want to delete this Buku?", "Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteBuku(id);
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

        List<BukuData> list = getAllBuku(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getBukuTotalCount(search);

        tableModel.setRowCount(0);
        for (BukuData b : list) {
            tableModel.addRow(new Object[]{
                b.getIdBuku(),
                b.getNoBarcode(),
                b.getIsbn(),
                b.getJudul(),
                b.getNamaPengarang(),
                b.getNamaPenerbit(),
                b.getNamaKategori(),
                b.getTahunTerbit(),
                b.getStokTotal(),
                b.getStokTersedia(),
                b.getLokasiRak()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    // --- Autocomplete ComboBox Inner Class ---
    public static class AutocompleteComboBox<T> extends JComboBox<T> {
        private final Function<String, List<T>> searchFunc;
        private boolean isFiredByKeyboard = false;

        public AutocompleteComboBox(Function<String, List<T>> searchFunc) {
            this.searchFunc = searchFunc;
            setEditable(true);
            
            JTextField editor = (JTextField) getEditor().getEditorComponent();
            editor.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                   if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_ENTER) {
                       return;
                   }
                   char c = e.getKeyChar();
                   if (!Character.isLetterOrDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE && c != ' ') {
                       // ignore
                   }

                   SwingUtilities.invokeLater(() -> {
                       isFiredByKeyboard = true;
                       String text = editor.getText();
                       if (text.length() > 0) {
                           performSearch(text);
                       } else {
                           hidePopup();
                       }
                       isFiredByKeyboard = false;
                   });
                }
                
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                         if (!isPopupVisible()) {
                             showPopup();
                         }
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (isPopupVisible()) {
                             Object selected = getSelectedItem();
                             if (selected != null) {
                                 editor.setText(selected.toString());
                                 hidePopup();
                             }
                        }
                    }
                }
            });
        }
        
        private void performSearch(String text) {
            List<T> results = searchFunc.apply(text);
            DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) getModel();
            model.removeAllElements();
            for (T item : results) {
                model.addElement(item);
            }
            JTextField editor = (JTextField) getEditor().getEditorComponent();
            editor.setText(text);
            editor.setCaretPosition(text.length()); // Maintain cursor position
            if (model.getSize() > 0) {
                showPopup();
            } else {
                hidePopup();
            }
        }
    }
    
    // --- Autocomplete Helper Methods (Direct DB Queries Limit 10) ---
    private List<Pengarang.PengarangData> searchPengarang(String keyword) {
        List<Pengarang.PengarangData> list = new ArrayList<>();
        String sql = "SELECT * FROM pengarang WHERE nama_pengarang LIKE ? LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Pengarang.PengarangData(
                        rs.getInt("id_pengarang"), rs.getString("nama_pengarang"),
                        rs.getString("negara"), rs.getString("biografi"), rs.getDate("tgl_lahir")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
     private List<Penerbit.PenerbitData> searchPenerbit(String keyword) {
        List<Penerbit.PenerbitData> list = new ArrayList<>();
        String sql = "SELECT * FROM penerbit WHERE nama_penerbit LIKE ? LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Penerbit.PenerbitData(
                        rs.getInt("id_penerbit"), rs.getString("nama_penerbit"),
                        rs.getString("alamat"), rs.getString("kota"), rs.getString("no_telepon"), rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
     private List<Kategori.KategoriData> searchKategori(String keyword) {
        List<Kategori.KategoriData> list = new ArrayList<>();
        String sql = "SELECT * FROM kategori WHERE nama_kategori LIKE ? LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Kategori.KategoriData(
                        rs.getInt("id_kategori"), rs.getString("kode_kategori"),
                        rs.getString("nama_kategori"), rs.getString("deskripsi")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
     private List<Rak.RakData> searchRak(String keyword) {
        List<Rak.RakData> list = new ArrayList<>();
        String sql = "SELECT * FROM rak WHERE lokasi LIKE ? OR kode_rak LIKE ? LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Rak.RakData(
                        rs.getInt("id_rak"), rs.getString("kode_rak"),
                        rs.getString("lokasi"), rs.getInt("kapasitas"), rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private void showBukuDialog(BukuData buku) {
        JDialog dialog = new JDialog(this, buku == null ? "Add Buku" : "Edit Buku", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[][grow][][grow]", "[]")); // 4 columns for grid layout
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JTextField txtNoBarcode = new JTextField(20);
        JTextField txtISBN = new JTextField(20);
        JTextField txtJudul = new JTextField(20);
        
        // Autocomplete ComboBoxes
        AutocompleteComboBox<Pengarang.PengarangData> cmbPengarang = new AutocompleteComboBox<>(this::searchPengarang);
        AutocompleteComboBox<Penerbit.PenerbitData> cmbPenerbit = new AutocompleteComboBox<>(this::searchPenerbit);
        AutocompleteComboBox<Kategori.KategoriData> cmbKategori = new AutocompleteComboBox<>(this::searchKategori);
        AutocompleteComboBox<Rak.RakData> cmbRak = new AutocompleteComboBox<>(this::searchRak);
        
        JTextField txtTahun = new JTextField(10);
        JTextField txtEdisi = new JTextField(20);
        JTextField txtHalaman = new JTextField(10);
        JTextField txtBahasa = new JTextField(20);
        
        JTextField txtStokTotal = new JTextField(10);
        JTextField txtStokTersedia = new JTextField(10);
        JTextField txtHarga = new JTextField(15);
        
        com.toedter.calendar.JDateChooser dateChooser = new com.toedter.calendar.JDateChooser();
        dateChooser.setLocale(new java.util.Locale("id", "ID"));
        dateChooser.setDateFormatString("dd-MM-yyyy");
        
        String[] statusOptions = {"Tersedia", "Habis", "Rusak", "Hilang"};
        JComboBox<String> cmbStatus = new JComboBox<>(statusOptions);

        if (buku != null) {
            txtNoBarcode.setText(buku.getNoBarcode() == null ? "" : buku.getNoBarcode());
            txtISBN.setText(buku.getIsbn());
            txtJudul.setText(buku.getJudul());
            
            // Pre-fill ComboBoxes (Create dummy objects with ID and Name for display)
            Pengarang.PengarangData p = new Pengarang.PengarangData(); p.setIdPengarang(buku.getIdPengarang()); p.setNamaPengarang(buku.getNamaPengarang());
            cmbPengarang.addItem(p); cmbPengarang.setSelectedItem(p);
            
            Penerbit.PenerbitData pn = new Penerbit.PenerbitData(); pn.setIdPenerbit(buku.getIdPenerbit()); pn.setNamaPenerbit(buku.getNamaPenerbit());
            cmbPenerbit.addItem(pn); cmbPenerbit.setSelectedItem(pn);
            
            Kategori.KategoriData k = new Kategori.KategoriData(); k.setIdKategori(buku.getIdKategori()); k.setNamaKategori(buku.getNamaKategori());
            cmbKategori.addItem(k); cmbKategori.setSelectedItem(k);
            
            Rak.RakData r = new Rak.RakData(); r.setIdRak(buku.getIdRak()); r.setLokasi(buku.getLokasiRak()); r.setKodeRak("");
            cmbRak.addItem(r); cmbRak.setSelectedItem(r);
            
            txtTahun.setText(String.valueOf(buku.getTahunTerbit()));
            txtEdisi.setText(buku.getEdisi());
            txtHalaman.setText(String.valueOf(buku.getHalaman()));
            txtBahasa.setText(buku.getBahasa());
            
            txtStokTotal.setText(String.valueOf(buku.getStokTotal()));
            txtStokTersedia.setText(String.valueOf(buku.getStokTersedia()));
            txtHarga.setText(String.valueOf(buku.getHarga()));
            
            if (buku.getTglMasuk() != null) {
                dateChooser.setDate(buku.getTglMasuk());
            }
            cmbStatus.setSelectedItem(buku.getStatus());
        } else {
             // Defaults
             txtStokTotal.setText("0");
             txtStokTersedia.setText("0");
             txtHarga.setText("0");
             dateChooser.setDate(new Date());
        }

        // Layout Components
        dialog.add(new JLabel("No Barcode:"));
        dialog.add(txtNoBarcode, "growx");
        dialog.add(new JLabel("ISBN:"));
        dialog.add(txtISBN, "growx, wrap");
        dialog.add(new JLabel("Judul:"));
        dialog.add(txtJudul, "growx");

        dialog.add(new JLabel("Pengarang:"));
        dialog.add(cmbPengarang, "growx, wrap");
        dialog.add(new JLabel("Penerbit:"));
        dialog.add(cmbPenerbit, "growx");
        dialog.add(new JLabel("Kategori:"));
        dialog.add(cmbKategori, "growx, wrap");
        dialog.add(new JLabel("Rak:"));
        dialog.add(cmbRak, "growx");
        
        dialog.add(new JLabel("Tahun Terbit:"));
        dialog.add(txtTahun, "growx, wrap");
        dialog.add(new JLabel("Edisi:"));
        dialog.add(txtEdisi, "growx");
        
        dialog.add(new JLabel("Halaman:"));
        dialog.add(txtHalaman, "growx, wrap");
        dialog.add(new JLabel("Bahasa:"));
        dialog.add(txtBahasa, "growx");
        
        dialog.add(new JLabel("Stok Total:"));
        dialog.add(txtStokTotal, "growx, wrap");
        dialog.add(new JLabel("Stok Tersedia:"));
        dialog.add(txtStokTersedia, "growx");
        
        dialog.add(new JLabel("Harga:"));
        dialog.add(txtHarga, "growx, wrap");
        dialog.add(new JLabel("Tgl Masuk:"));
        dialog.add(dateChooser, "growx");
        
        dialog.add(new JLabel("Status:"));
        dialog.add(cmbStatus, "growx, wrap");

        JCheckBox chkKeepOpen = new JCheckBox("Tetap di form");
        if (buku == null) {
            dialog.add(chkKeepOpen, "span, wrap");
        } else {
            dialog.add(new JLabel(""), "span, wrap");
        }

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            try {
                String noBarcode = txtNoBarcode.getText();
                String isbn = txtISBN.getText();
                String judul = txtJudul.getText();
                
                // Get IDs from ComboBoxes
                int idPengarang = 0;
                Object pObj = cmbPengarang.getSelectedItem();
                if (pObj instanceof Pengarang.PengarangData) idPengarang = ((Pengarang.PengarangData)pObj).getIdPengarang();
                
                int idPenerbit = 0;
                Object pnObj = cmbPenerbit.getSelectedItem();
                if (pnObj instanceof Penerbit.PenerbitData) idPenerbit = ((Penerbit.PenerbitData)pnObj).getIdPenerbit();
                
                int idKategori = 0;
                Object kObj = cmbKategori.getSelectedItem();
                if (kObj instanceof Kategori.KategoriData) idKategori = ((Kategori.KategoriData)kObj).getIdKategori();
                
                int idRak = 0;
                Object rObj = cmbRak.getSelectedItem();
                if (rObj instanceof Rak.RakData) idRak = ((Rak.RakData)rObj).getIdRak();

                int tahun = Integer.parseInt(txtTahun.getText());
                String edisi = txtEdisi.getText();
                int halaman = Integer.parseInt(txtHalaman.getText());
                String bahasa = txtBahasa.getText();
                int stokTotal = Integer.parseInt(txtStokTotal.getText());
                int stokTersedia = Integer.parseInt(txtStokTersedia.getText());
                double harga = Double.parseDouble(txtHarga.getText());
                
                java.util.Date utilDate = dateChooser.getDate();
                java.sql.Date tglMasuk = (utilDate != null) ? new java.sql.Date(utilDate.getTime()) : null;
                String status = (String) cmbStatus.getSelectedItem();
                
                // Validation (minimal)
                if (idPengarang == 0 || idPenerbit == 0 || idKategori == 0 || idRak == 0) {
                     JOptionPane.showMessageDialog(dialog, "Please select valid Pengarang, Penerbit, Kategori, and Rak.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                     return;
                }

                BukuData newBuku = new BukuData(
                    (buku == null) ? 0 : buku.getIdBuku(),
                    noBarcode, isbn, judul, idPengarang, "", idPenerbit, "", idKategori, "",
                    tahun, edisi, halaman, bahasa, idRak, "", stokTotal, stokTersedia, harga, tglMasuk, status
                );

                if (buku == null) {
                    saveBuku(newBuku);
                    loadData();

                    if (chkKeepOpen.isSelected()) {
                        txtNoBarcode.setText("");
                        txtISBN.setText("");
                        txtJudul.setText("");
                        // Reset ComboBoxes
                        cmbPengarang.setSelectedIndex(-1);
                        cmbPenerbit.setSelectedIndex(-1);
                        cmbKategori.setSelectedIndex(-1);
                        cmbRak.setSelectedIndex(-1);
                        
                        txtStokTotal.setText("0");
                        txtStokTersedia.setText("0");
                        txtHarga.setText("0");
                        txtISBN.requestFocus();
                    } else {
                        dialog.dispose();
                    }
                } else {
                    updateBuku(newBuku);
                    loadData();
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format in numeric fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(btnSave, "span, align right");
        dialog.setVisible(true);
    }

    // =================================== DAO Logic ====================================

    private List<BukuData> getAllBuku(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<BukuData> list = new ArrayList<>();

        if (!sortColumn.matches("id_buku|no_barcode|isbn|judul|nama_pengarang|nama_penerbit|nama_kategori|tahun_terbit|stok_total|stok_tersedia|lokasi_rak")) {
            sortColumn = "id_buku";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        // Fix sort column for joined fields to avoid ambiguity or invalid column names
        String orderBy = sortColumn;
        if (sortColumn.equals("nama_pengarang")) orderBy = "p.nama_pengarang";
        else if (sortColumn.equals("nama_penerbit")) orderBy = "pn.nama_penerbit";
        else if (sortColumn.equals("nama_kategori")) orderBy = "k.nama_kategori";
        else if (sortColumn.equals("lokasi_rak")) orderBy = "r.lokasi";
        else orderBy = "b." + sortColumn;


        String sql = "SELECT b.*, p.nama_pengarang, pn.nama_penerbit, k.nama_kategori, r.kode_rak, r.lokasi " +
                     "FROM buku b " +
                     "LEFT JOIN pengarang p ON b.id_pengarang = p.id_pengarang " +
                     "LEFT JOIN penerbit pn ON b.id_penerbit = pn.id_penerbit " +
                     "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori " +
                     "LEFT JOIN rak r ON b.id_rak = r.id_rak " +
                     "WHERE b.judul LIKE ? OR b.isbn LIKE ? OR p.nama_pengarang LIKE ? " +
                     "ORDER BY " + orderBy + " " + sortOrder + " " +
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
                    list.add(new BukuData(
                        rs.getInt("id_buku"),
                        rs.getString("no_barcode"),
                        rs.getString("isbn"),
                        rs.getString("judul"),
                        rs.getInt("id_pengarang"),
                        rs.getString("nama_pengarang"),
                        rs.getInt("id_penerbit"),
                        rs.getString("nama_penerbit"),
                        rs.getInt("id_kategori"),
                        rs.getString("nama_kategori"),
                        rs.getInt("tahun_terbit"),
                        rs.getString("edisi"),
                        rs.getInt("halaman"),
                        rs.getString("bahasa"),
                        rs.getInt("id_rak"),
                        rs.getString("kode_rak") + " - " + rs.getString("lokasi"),
                        rs.getInt("stok_total"),
                        rs.getInt("stok_tersedia"),
                        rs.getDouble("harga"),
                        rs.getDate("tgl_masuk"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching buku: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getBukuTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM buku b " +
                     "LEFT JOIN pengarang p ON b.id_pengarang = p.id_pengarang " +
                     "WHERE b.judul LIKE ? OR b.isbn LIKE ? OR p.nama_pengarang LIKE ?";

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
             JOptionPane.showMessageDialog(this, "Error counting buku: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return count;
    }

    private void saveBuku(BukuData buku) {
        String sql = "INSERT INTO buku (no_barcode, isbn, judul, id_pengarang, id_penerbit, id_kategori, tahun_terbit, edisi, halaman, bahasa, id_rak, stok_total, stok_tersedia, harga, tgl_masuk, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, buku.getNoBarcode());
            pstmt.setString(2, buku.getIsbn());
            pstmt.setString(3, buku.getJudul());
            pstmt.setInt(4, buku.getIdPengarang());
            pstmt.setInt(5, buku.getIdPenerbit());
            pstmt.setInt(6, buku.getIdKategori());
            pstmt.setInt(7, buku.getTahunTerbit());
            pstmt.setString(8, buku.getEdisi());
            pstmt.setInt(9, buku.getHalaman());
            pstmt.setString(10, buku.getBahasa());
            pstmt.setInt(11, buku.getIdRak());
            pstmt.setInt(12, buku.getStokTotal());
            pstmt.setInt(13, buku.getStokTersedia());
            pstmt.setDouble(14, buku.getHarga());
            pstmt.setDate(15, buku.getTglMasuk());
            pstmt.setString(16, buku.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving buku: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBuku(BukuData buku) {
        String sql = "UPDATE buku SET no_barcode = ?, isbn = ?, judul = ?, id_pengarang = ?, id_penerbit = ?, id_kategori = ?, tahun_terbit = ?, edisi = ?, halaman = ?, bahasa = ?, id_rak = ?, stok_total = ?, stok_tersedia = ?, harga = ?, tgl_masuk = ?, status = ? WHERE id_buku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, buku.getNoBarcode());
            pstmt.setString(2, buku.getIsbn());
            pstmt.setString(3, buku.getJudul());
            pstmt.setInt(4, buku.getIdPengarang());
            pstmt.setInt(5, buku.getIdPenerbit());
            pstmt.setInt(6, buku.getIdKategori());
            pstmt.setInt(7, buku.getTahunTerbit());
            pstmt.setString(8, buku.getEdisi());
            pstmt.setInt(9, buku.getHalaman());
            pstmt.setString(10, buku.getBahasa());
            pstmt.setInt(11, buku.getIdRak());
            pstmt.setInt(12, buku.getStokTotal());
            pstmt.setInt(13, buku.getStokTersedia());
            pstmt.setDouble(14, buku.getHarga());
            pstmt.setDate(15, buku.getTglMasuk());
            pstmt.setString(16, buku.getStatus());
            pstmt.setInt(17, buku.getIdBuku());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating buku: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBuku(int id) {
        String sql = "DELETE FROM buku WHERE id_buku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting buku: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BukuData getBukuById(int id) {
        BukuData buku = null;
        String sql = "SELECT b.*, p.nama_pengarang, pn.nama_penerbit, k.nama_kategori, r.kode_rak, r.lokasi " +
                     "FROM buku b " +
                     "LEFT JOIN pengarang p ON b.id_pengarang = p.id_pengarang " +
                     "LEFT JOIN penerbit pn ON b.id_penerbit = pn.id_penerbit " +
                     "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori " +
                     "LEFT JOIN rak r ON b.id_rak = r.id_rak " +
                     "WHERE b.id_buku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    buku = new BukuData(
                        rs.getInt("id_buku"),
                        rs.getString("no_barcode"),
                        rs.getString("isbn"),
                        rs.getString("judul"),
                        rs.getInt("id_pengarang"),
                        rs.getString("nama_pengarang"),
                        rs.getInt("id_penerbit"),
                        rs.getString("nama_penerbit"),
                        rs.getInt("id_kategori"),
                        rs.getString("nama_kategori"),
                        rs.getInt("tahun_terbit"),
                        rs.getString("edisi"),
                        rs.getInt("halaman"),
                        rs.getString("bahasa"),
                        rs.getInt("id_rak"),
                        rs.getString("kode_rak") + " - " + rs.getString("lokasi"),
                        rs.getInt("stok_total"),
                        rs.getInt("stok_tersedia"),
                        rs.getDouble("harga"),
                        rs.getDate("tgl_masuk"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting buku: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return buku;
    }

    // --- Main Method ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Buku().setVisible(true);
        });
    }
}
