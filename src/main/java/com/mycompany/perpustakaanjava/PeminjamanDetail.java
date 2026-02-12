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
import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
// Add missing PeminjamanItem/BukuItem if not static nested (they are nested below)

/**
 * Main application frame for managing PeminjamanDetail.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class PeminjamanDetail extends JFrame {

    // --- Model Class ---
    public static class PeminjamanDetailData {
        private int idDetail;
        private int idPeminjaman;
        private String noPeminjaman; // Display
        private int idBuku;
        private String judulBuku; // Display
        private int qty;
        private String catatan;

        public PeminjamanDetailData() {}

        public PeminjamanDetailData(int idDetail, int idPeminjaman, String noPeminjaman, int idBuku, String judulBuku, int qty, String catatan) {
            this.idDetail = idDetail;
            this.idPeminjaman = idPeminjaman;
            this.noPeminjaman = noPeminjaman;
            this.idBuku = idBuku;
            this.judulBuku = judulBuku;
            this.qty = qty;
            this.catatan = catatan;
        }

        public int getIdDetail() { return idDetail; }
        public void setIdDetail(int idDetail) { this.idDetail = idDetail; }
        public int getIdPeminjaman() { return idPeminjaman; }
        public void setIdPeminjaman(int idPeminjaman) { this.idPeminjaman = idPeminjaman; }
        public String getNoPeminjaman() { return noPeminjaman; }
        public void setNoPeminjaman(String noPeminjaman) { this.noPeminjaman = noPeminjaman; }
        public int getIdBuku() { return idBuku; }
        public void setIdBuku(int idBuku) { this.idBuku = idBuku; }
        public String getJudulBuku() { return judulBuku; }
        public void setJudulBuku(String judulBuku) { this.judulBuku = judulBuku; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }
        public String getCatatan() { return catatan; }
        public void setCatatan(String catatan) { this.catatan = catatan; }

        @Override
        public String toString() { return noPeminjaman + " - " + judulBuku; }
    }

    // Helper classes for ComboBoxes
    public static class PeminjamanItem {
        private int id;
        private String no;
        public PeminjamanItem(int id, String no) { this.id = id; this.no = no; }
        public int getId() { return id; }
        @Override public String toString() { return no; }
    }

    public static class BukuItem {
        private int id;
        private String judul;
        public BukuItem(int id, String judul) { this.id = id; this.judul = judul; }
        public int getId() { return id; }
        @Override public String toString() { return judul; }
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
    private String sortColumn = "id_detail";
    private String sortOrder = "ASC";

    public PeminjamanDetail() {
        setTitle("Master Peminjaman Detail");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadData();
    }

    // Constructor for linking from Peminjaman
    public PeminjamanDetail(String noPeminjamanFilter) {
        this();
        if (noPeminjamanFilter != null && !noPeminjamanFilter.isEmpty()) {
            txtSearch.setText(noPeminjamanFilter);
            loadData();
        }
    }
    
    public void showAddDialog() {
        showDetailDialog(null);
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // === Header / Filter Section ===
        JPanel pnlHeader = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        pnlHeader.add(new JLabel("Search (No Peminjaman/Judul Buku):"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Detail");
        btnAdd.addActionListener(e -> showDetailDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        String[] columnNames = {"ID Detail", "No Peminjaman", "Judul Buku", "Qty", "Catatan"};
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
                String[] dbColumns = {"pd.id_detail", "p.no_peminjaman", "b.judul", "pd.qty", "pd.catatan"};

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
                        PeminjamanDetailData detail = getDetailById(id);
                        if (detail != null) {
                            showDetailDialog(detail);
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
                PeminjamanDetailData detail = getDetailById(id);
                if (detail != null) {
                    showDetailDialog(detail);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) table.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(PeminjamanDetail.this, "Are you sure you want to delete this detail?", "Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteDetail(id);
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

        List<PeminjamanDetailData> list = getAllPeminjamanDetail(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getDetailTotalCount(search);

        tableModel.setRowCount(0);
        for (PeminjamanDetailData d : list) {
            tableModel.addRow(new Object[]{
                d.getIdDetail(),
                d.getNoPeminjaman(),
                d.getJudulBuku(),
                d.getQty(),
                d.getCatatan()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showDetailDialog(PeminjamanDetailData detail) {
        JDialog dialog = new JDialog(this, detail == null ? "Add Detail" : "Edit Detail", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[]"));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JTextField txtNoPeminjaman = new JTextField(20);
        txtNoPeminjaman.setEditable(false);
        
        // Auto-Search Buku
        JTextField txtBuku = new JTextField(20);
        JPopupMenu popupBuku = new JPopupMenu();
        final int[] selectedBukuId = new int[]{-1};
        final int[] selectedPeminjamanId = new int[]{-1}; // Need to resolve ID from No Peminjaman

        txtBuku.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtBuku.getText().trim();
                if (text.length() < 1) {
                    popupBuku.setVisible(false);
                    return;
                }
                
                // Search logic
                List<BukuItem> results = searchBuku(text);
                if (results.isEmpty()) {
                    popupBuku.setVisible(false);
                    return;
                }
                
                popupBuku.removeAll();
                for (BukuItem item : results) {
                    JMenuItem menuItem = new JMenuItem(item.toString());
                    menuItem.addActionListener(ev -> {
                        txtBuku.setText(item.toString());
                        selectedBukuId[0] = item.getId();
                        popupBuku.setVisible(false);
                    });
                    popupBuku.add(menuItem);
                }
                
                // Show popup below text field
                if (!results.isEmpty()) {
                    popupBuku.show(txtBuku, 0, txtBuku.getHeight());
                    txtBuku.requestFocus();
                }
            }
        });

        // Use standard KeyListener for navigation/hiding if needed, but focus is key
        
        JTextField txtQty = new JTextField(20);
        JTextField txtCatatan = new JTextField(20);

        // Remove old loaders needed only if we fallback, but user wants text fields
        // loadPeminjamanCombo(cmbPeminjaman); 
        // loadBukuCombo(cmbBuku);

        if (detail != null) {
            txtNoPeminjaman.setText(detail.getNoPeminjaman());
            selectedPeminjamanId[0] = detail.getIdPeminjaman();
            
            txtBuku.setText(detail.getJudulBuku());
            selectedBukuId[0] = detail.getIdBuku();
            
            txtQty.setText(String.valueOf(detail.getQty()));
            txtCatatan.setText(detail.getCatatan());
        } else {
             // Try to pre-select based on search filter if valid
             String currentSearch = txtSearch.getText().trim();
             if (!currentSearch.isEmpty()) {
                 txtNoPeminjaman.setText(currentSearch);
                 // Resolve ID
                 int pId = getPeminjamanIdByNo(currentSearch);
                 if (pId != -1) {
                     selectedPeminjamanId[0] = pId;
                 }
             }
        }

        dialog.add(new JLabel("No Peminjaman:"));
        dialog.add(txtNoPeminjaman, "wrap, growx");
        dialog.add(new JLabel("Buku (Type to Search):"));
        dialog.add(txtBuku, "wrap, growx");
        dialog.add(new JLabel("Qty:"));
        dialog.add(txtQty, "wrap, growx");
        dialog.add(new JLabel("Catatan:"));
        dialog.add(txtCatatan, "wrap, growx");

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            int pId = selectedPeminjamanId[0];
            int bId = selectedBukuId[0];
            String strQty = txtQty.getText();
            String catatan = txtCatatan.getText();

            if (pId == -1) {
                // Try to resolve again if needed (e.g. if passed as string but regex check failed earlier)
                String no = txtNoPeminjaman.getText().trim();
                pId = getPeminjamanIdByNo(no);
            }

            if (pId == -1 || bId == -1 || strQty.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields (Valid No Peminjaman & Buku).");
                return;
            }

            int qty = 0;
            try {
                qty = Integer.parseInt(strQty);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format for Qty.");
                return;
            }

            PeminjamanDetailData newDetail = new PeminjamanDetailData(
                (detail == null) ? 0 : detail.getIdDetail(),
                pId,
                "", // No handled by DB/DAO on reload
                bId,
                "", // Judul handled by DB/DAO on reload
                qty,
                catatan
            );

            if (detail == null) {
                savePeminjamanDetail(newDetail);
            } else {
                updatePeminjamanDetail(newDetail);
            }
            loadData();
            dialog.dispose();
        });

        dialog.add(btnSave, "span, align right, gaptop 10");
        dialog.setVisible(true);
    }

    private void loadPeminjamanCombo(JComboBox<PeminjamanItem> cmb) {
        cmb.removeAllItems();
        String sql = "SELECT id_peminjaman, no_peminjaman FROM peminjaman ORDER BY no_peminjaman DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cmb.addItem(new PeminjamanItem(rs.getInt("id_peminjaman"), rs.getString("no_peminjaman")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper Method to Search Buku (Limit 10)
    private List<BukuItem> searchBuku(String keyword) {
        List<BukuItem> list = new ArrayList<>();
        String sql = "SELECT id_buku, judul FROM buku WHERE judul LIKE ? ORDER BY judul ASC LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new BukuItem(rs.getInt("id_buku"), rs.getString("judul")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private int getPeminjamanIdByNo(String noPeminjaman) {
        int id = -1;
        String sql = "SELECT id_peminjaman FROM peminjaman WHERE no_peminjaman = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, noPeminjaman);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id_peminjaman");
                }
            }
        } catch (SQLException e) {
             e.printStackTrace();
        }
        return id;
    }

    private void setSelectedPeminjaman(JComboBox<PeminjamanItem> cmb, int id) {
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

    private List<PeminjamanDetailData> getAllPeminjamanDetail(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<PeminjamanDetailData> list = new ArrayList<>();

        if (!sortColumn.matches("pd.id_detail|p.no_peminjaman|b.judul|pd.qty|pd.catatan")) {
            sortColumn = "pd.id_detail";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String sql = "SELECT pd.*, p.no_peminjaman, b.judul " +
                     "FROM peminjaman_detail pd " +
                     "LEFT JOIN peminjaman p ON pd.id_peminjaman = p.id_peminjaman " +
                     "LEFT JOIN buku b ON pd.id_buku = b.id_buku " +
                     "WHERE p.no_peminjaman LIKE ? OR b.judul LIKE ? " +
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
                    list.add(new PeminjamanDetailData(
                        rs.getInt("id_detail"),
                        rs.getInt("id_peminjaman"),
                        rs.getString("no_peminjaman"),
                        rs.getInt("id_buku"),
                        rs.getString("judul"),
                        rs.getInt("qty"),
                        rs.getString("catatan")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching detail: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getDetailTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM peminjaman_detail pd " +
                     "LEFT JOIN peminjaman p ON pd.id_peminjaman = p.id_peminjaman " +
                     "LEFT JOIN buku b ON pd.id_buku = b.id_buku " +
                     "WHERE p.no_peminjaman LIKE ? OR b.judul LIKE ?";

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

    private void savePeminjamanDetail(PeminjamanDetailData pd) {
        String sql = "INSERT INTO peminjaman_detail (id_peminjaman, id_buku, qty, catatan) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pd.getIdPeminjaman());
            pstmt.setInt(2, pd.getIdBuku());
            pstmt.setInt(3, pd.getQty());
            pstmt.setString(4, pd.getCatatan());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving detail: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePeminjamanDetail(PeminjamanDetailData pd) {
        String sql = "UPDATE peminjaman_detail SET id_peminjaman=?, id_buku=?, qty=?, catatan=? WHERE id_detail=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pd.getIdPeminjaman());
            pstmt.setInt(2, pd.getIdBuku());
            pstmt.setInt(3, pd.getQty());
            pstmt.setString(4, pd.getCatatan());
            pstmt.setInt(5, pd.getIdDetail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating detail: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDetail(int id) {
        String sql = "DELETE FROM peminjaman_detail WHERE id_detail = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting detail: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PeminjamanDetailData getDetailById(int id) {
        PeminjamanDetailData pd = null;
        String sql = "SELECT pd.*, p.no_peminjaman, b.judul " +
                     "FROM peminjaman_detail pd " +
                     "LEFT JOIN peminjaman p ON pd.id_peminjaman = p.id_peminjaman " +
                     "LEFT JOIN buku b ON pd.id_buku = b.id_buku " +
                     "WHERE pd.id_detail = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pd = new PeminjamanDetailData(
                        rs.getInt("id_detail"),
                        rs.getInt("id_peminjaman"),
                        rs.getString("no_peminjaman"),
                        rs.getInt("id_buku"),
                        rs.getString("judul"),
                        rs.getInt("qty"),
                        rs.getString("catatan")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pd;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PeminjamanDetail().setVisible(true);
        });
    }
}
