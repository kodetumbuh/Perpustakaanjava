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
 * Main application frame for Audit Properti.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class AuditProperti extends JFrame {
    
    // --- Model Class ---
    public static class PropertiData {
        private int idProperti;
        private int idUser;
        private String namaUser; // For display
        private String namaBarang;
        private String statusBarang;
        private int qtyBarang;
        private String keterangan;

        public PropertiData() {}

        public PropertiData(int idProperti, int idUser, String namaUser, String namaBarang, String statusBarang, int qtyBarang, String keterangan) {
            this.idProperti = idProperti;
            this.idUser = idUser;
            this.namaUser = namaUser;
            this.namaBarang = namaBarang;
            this.statusBarang = statusBarang;
            this.qtyBarang = qtyBarang;
            this.keterangan = keterangan;
        }

        public int getIdProperti() { return idProperti; }
        public void setIdProperti(int idProperti) { this.idProperti = idProperti; }
        public int getIdUser() { return idUser; }
        public void setIdUser(int idUser) { this.idUser = idUser; }
        public String getNamaUser() { return namaUser; }
        public void setNamaUser(String namaUser) { this.namaUser = namaUser; }
        public String getNamaBarang() { return namaBarang; }
        public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
        public String getStatusBarang() { return statusBarang; }
        public void setStatusBarang(String statusBarang) { this.statusBarang = statusBarang; }
        public int getQtyBarang() { return qtyBarang; }
        public void setQtyBarang(int qtyBarang) { this.qtyBarang = qtyBarang; }
        public String getKeterangan() { return keterangan; }
        public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

        @Override
        public String toString() { return namaBarang; }
    }
    
    // --- Helper Model for User ComboBox ---
    public static class UserItem {
        private int id;
        private String username;
        
        public UserItem(int id, String username) {
            this.id = id;
            this.username = username;
        }
        
        public int getId() { return id; }
        
        @Override
        public String toString() { return username; }
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
    private String sortColumn = "p.id_properti";
    private String sortOrder = "ASC";

    public AuditProperti() {
        setTitle("Audit Properti");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        
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
        
        JButton btnAdd = new JButton("Add New Properti");
        btnAdd.addActionListener(e -> showPropertiDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        String[] columnNames = {"ID", "Pencatat", "Nama Barang", "Status Barang", "Qty", "Keterangan"};
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
                // Map column index to DB column names (using aliases in query)
                String[] dbColumns = {"p.id_properti", "u.username", "p.nama_barang", "p.status_barang", "p.qty_barang", "p.keterangan"};
                
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
                        PropertiData prop = getPropertiById(id);
                        if (prop != null) {
                            showPropertiDialog(prop);
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
                PropertiData prop = getPropertiById(id);
                if (prop != null) {
                    showPropertiDialog(prop);
                }
            }
        });
        
        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                 int id = (int) table.getValueAt(selectedRow, 0);
                 int confirm = JOptionPane.showConfirmDialog(AuditProperti.this, "Are you sure you want to delete this properti?", "Delete", JOptionPane.YES_NO_OPTION);
                 if (confirm == JOptionPane.YES_OPTION) {
                     deleteProperti(id);
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
        Integer[] pageSizes = {10, 25, 50, 100};
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
        
        List<PropertiData> list = getAllProperti(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getPropertiTotalCount(search);
        
        tableModel.setRowCount(0);
        for (PropertiData p : list) {
            tableModel.addRow(new Object[]{
                p.getIdProperti(),
                p.getNamaUser(),
                p.getNamaBarang(),
                p.getStatusBarang(),
                p.getQtyBarang(),
                p.getKeterangan()
            });
        }
        
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;
        
        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showPropertiDialog(PropertiData properti) {
        JDialog dialog = new JDialog(this, properti == null ? "Add Properti" : "Edit Properti", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[][grow]", "[]"));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        // Load users for combo box
        List<UserItem> users = getUsers();
        JComboBox<UserItem> cmbUser = new JComboBox<>(users.toArray(new UserItem[0]));
        
        JTextField txtNamaBarang = new JTextField(20);
        String[] statusOptions = {"Baik", "Rusak Ringan", "Rusak Berat"};
        JComboBox<String> cmbStatusBarang = new JComboBox<>(statusOptions);
        JTextField txtQtyBarang = new JTextField(20);
        JTextArea txtKeterangan = new JTextArea(3, 20);
        JScrollPane scrollKeterangan = new JScrollPane(txtKeterangan);
        
        if (properti != null) {
            // Set selected user
            for (int i = 0; i < cmbUser.getItemCount(); i++) {
                if (cmbUser.getItemAt(i).getId() == properti.getIdUser()) {
                    cmbUser.setSelectedIndex(i);
                    break;
                }
            }
            txtNamaBarang.setText(properti.getNamaBarang());
            cmbStatusBarang.setSelectedItem(properti.getStatusBarang());
            txtQtyBarang.setText(String.valueOf(properti.getQtyBarang()));
            txtKeterangan.setText(properti.getKeterangan());
        }
        
        dialog.add(new JLabel("Pencatat (User):"));
        dialog.add(cmbUser, "wrap, growx");
        
        dialog.add(new JLabel("Nama Barang:"));
        dialog.add(txtNamaBarang, "wrap, growx");
        
        dialog.add(new JLabel("Status Barang:"));
        dialog.add(cmbStatusBarang, "wrap, growx");
        
        dialog.add(new JLabel("Qty Barang:"));
        dialog.add(txtQtyBarang, "wrap, growx");
        
        dialog.add(new JLabel("Keterangan:"));
        dialog.add(scrollKeterangan, "wrap, growx, h 60!");
        
        
        JCheckBox chkKeepOpen = new JCheckBox("Tetap di form");
        if (properti == null) {
            dialog.add(chkKeepOpen, "wrap");
        } else {
            dialog.add(new JLabel(""), "wrap");
        }
        
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            UserItem selectedUser = (UserItem) cmbUser.getSelectedItem();
            if (selectedUser == null) {
                 JOptionPane.showMessageDialog(dialog, "Please select a user.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 return;
            }
            
            String namaBarang = txtNamaBarang.getText();
            String statusBarang = (String) cmbStatusBarang.getSelectedItem();
            String qtyStr = txtQtyBarang.getText();
            String keterangan = txtKeterangan.getText();
            
            if (namaBarang.isEmpty() || statusBarang.isEmpty() || qtyStr.isEmpty()) {
                 JOptionPane.showMessageDialog(dialog, "Please fill all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 return;
            }
            
            int qty = 0;
            try {
                qty = Integer.parseInt(qtyStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Qty must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            PropertiData newProp = new PropertiData(
                (properti == null) ? 0 : properti.getIdProperti(),
                selectedUser.getId(),
                selectedUser.toString(),
                namaBarang,
                statusBarang,
                qty,
                keterangan
            );
            
            if (properti == null) {
                saveProperti(newProp);
                loadData(); 
                
                if (chkKeepOpen.isSelected()) {
                    txtNamaBarang.setText("");
                    cmbStatusBarang.setSelectedIndex(0);
                    txtQtyBarang.setText("");
                    txtKeterangan.setText("");
                    txtNamaBarang.requestFocus();
                } else {
                    dialog.dispose();
                }
            } else {
                updateProperti(newProp);
                loadData();
                dialog.dispose();
            }
        });
        
        dialog.add(btnSave, "span, align right");
        dialog.setVisible(true);
    }

    // =================================== DAO Logic ====================================

    private List<PropertiData> getAllProperti(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<PropertiData> list = new ArrayList<>();
        
        // Basic SQL Injection prevention for sorting
        if (!sortColumn.matches("[a-zA-Z0-9_\\.]+")) {
            sortColumn = "p.id_properti";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String sql = "SELECT p.id_properti, p.id_user, u.username, p.nama_barang, p.status_barang, p.qty_barang, p.keterangan " +
                     "FROM properti p " +
                     "LEFT JOIN user u ON p.id_user = u.id_user " +
                     "WHERE p.nama_barang LIKE ? OR p.status_barang LIKE ? OR u.username LIKE ? " +
                     "ORDER BY " + sortColumn + " " + sortOrder + " " +
                     "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchKeyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern); // Search by username too
            pstmt.setInt(4, limit);
            pstmt.setInt(5, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PropertiData(
                        rs.getInt("id_properti"),
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("nama_barang"),
                        rs.getString("status_barang"),
                        rs.getInt("qty_barang"),
                        rs.getString("keterangan")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching properti: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getPropertiTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM properti p " +
                     "LEFT JOIN user u ON p.id_user = u.id_user " +
                     "WHERE p.nama_barang LIKE ? OR p.status_barang LIKE ? OR u.username LIKE ?";

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
             JOptionPane.showMessageDialog(this, "Error counting properti: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return count;
    }
    
    private List<UserItem> getUsers() {
        List<UserItem> list = new ArrayList<>();
        String sql = "SELECT id_user, username FROM user ORDER BY username ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new UserItem(rs.getInt("id_user"), rs.getString("username")));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching users: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private void saveProperti(PropertiData properti) {
        String sql = "INSERT INTO properti (id_user, nama_barang, status_barang, qty_barang, keterangan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, properti.getIdUser());
            pstmt.setString(2, properti.getNamaBarang());
            pstmt.setString(3, properti.getStatusBarang());
            pstmt.setInt(4, properti.getQtyBarang());
            pstmt.setString(5, properti.getKeterangan());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving properti: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProperti(PropertiData properti) {
        String sql = "UPDATE properti SET id_user = ?, nama_barang = ?, status_barang = ?, qty_barang = ?, keterangan = ? WHERE id_properti = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, properti.getIdUser());
            pstmt.setString(2, properti.getNamaBarang());
            pstmt.setString(3, properti.getStatusBarang());
            pstmt.setInt(4, properti.getQtyBarang());
            pstmt.setString(5, properti.getKeterangan());
            pstmt.setInt(6, properti.getIdProperti());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating properti: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProperti(int id) {
        String sql = "DELETE FROM properti WHERE id_properti = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting properti: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PropertiData getPropertiById(int id) {
        PropertiData properti = null;
        String sql = "SELECT p.*, u.username FROM properti p LEFT JOIN user u ON p.id_user = u.id_user WHERE p.id_properti = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    properti = new PropertiData(
                        rs.getInt("id_properti"),
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("nama_barang"),
                        rs.getString("status_barang"),
                        rs.getInt("qty_barang"),
                        rs.getString("keterangan")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting properti: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return properti;
    }
}
