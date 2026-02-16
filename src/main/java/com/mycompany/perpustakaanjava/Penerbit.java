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
 * Main application frame for managing Penerbit.
 * Includes both UI logic and Data Access Logic (DAO) for table `penerbit`.
 */
public class Penerbit extends JFrame {

    // --- Model Class ---
    public static class PenerbitData {
        private int idPenerbit;
        private String namaPenerbit;
        private String alamat;
        private String kota;
        private String noTelepon;
        private String email;

        public PenerbitData() {}

        public PenerbitData(int idPenerbit, String namaPenerbit, String alamat, String kota, String noTelepon, String email) {
            this.idPenerbit = idPenerbit;
            this.namaPenerbit = namaPenerbit;
            this.alamat = alamat;
            this.kota = kota;
            this.noTelepon = noTelepon;
            this.email = email;
        }

        public int getIdPenerbit() { return idPenerbit; }
        public void setIdPenerbit(int idPenerbit) { this.idPenerbit = idPenerbit; }

        public String getNamaPenerbit() { return namaPenerbit; }
        public void setNamaPenerbit(String namaPenerbit) { this.namaPenerbit = namaPenerbit; }

        public String getAlamat() { return alamat; }
        public void setAlamat(String alamat) { this.alamat = alamat; }

        public String getKota() { return kota; }
        public void setKota(String kota) { this.kota = kota; }

        public String getNoTelepon() { return noTelepon; }
        public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() { return namaPenerbit; }
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
    private String sortColumn = "id_penerbit";
    private String sortOrder = "ASC";

    public Penerbit() {
        setTitle("Master Data Penerbit");
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
        pnlHeader.add(new JLabel("Search:"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Penerbit");
        btnAdd.addActionListener(e -> showPenerbitDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        // Columns: ID, Nama Penerbit, Alamat, Kota, No. Telepon, Email
        String[] columnNames = {"ID", "Nama Penerbit", "Alamat", "Kota", "No. Telepon", "Email"};
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
                String[] dbColumns = {"id_penerbit", "nama_penerbit", "alamat", "kota", "no_telepon", "email"};

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
                        PenerbitData p = getPenerbitById(id);
                        if (p != null) {
                            showPenerbitDialog(p);
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
                PenerbitData p = getPenerbitById(id);
                if (p != null) {
                    showPenerbitDialog(p);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                 int id = (int) table.getValueAt(selectedRow, 0);
                 int confirm = JOptionPane.showConfirmDialog(Penerbit.this, "Are you sure you want to delete this Penerbit?", "Delete", JOptionPane.YES_NO_OPTION);
                 if (confirm == JOptionPane.YES_OPTION) {
                     deletePenerbit(id);
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

        List<PenerbitData> list = getAllPenerbit(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getPenerbitTotalCount(search);

        tableModel.setRowCount(0);
        for (PenerbitData p : list) {
            tableModel.addRow(new Object[]{
                p.getIdPenerbit(),
                p.getNamaPenerbit(),
                p.getAlamat(),
                p.getKota(),
                p.getNoTelepon(),
                p.getEmail()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showPenerbitDialog(PenerbitData penerbit) {
        JDialog dialog = new JDialog(this, penerbit == null ? "Add Penerbit" : "Edit Penerbit", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[][grow]", "[]"));
        dialog.setSize(450, 400); // Adjusted size for more fields
        dialog.setLocationRelativeTo(this);

        JTextField txtNama = new JTextField(20);
        JTextArea txtAlamat = new JTextArea(3, 20); // Use TextArea for address
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        JTextField txtKota = new JTextField(20);
        JTextField txtNoTelepon = new JTextField(20);
        JTextField txtEmail = new JTextField(20);

        if (penerbit != null) {
            txtNama.setText(penerbit.getNamaPenerbit());
            txtAlamat.setText(penerbit.getAlamat());
            txtKota.setText(penerbit.getKota());
            txtNoTelepon.setText(penerbit.getNoTelepon());
            txtEmail.setText(penerbit.getEmail());
        }

        dialog.add(new JLabel("Nama Penerbit:"));
        dialog.add(txtNama, "wrap, growx");
        dialog.add(new JLabel("Alamat:"));
        dialog.add(new JScrollPane(txtAlamat), "wrap, growx, h 60!"); // Scroll pane for TextArea
        dialog.add(new JLabel("Kota:"));
        dialog.add(txtKota, "wrap, growx");
        dialog.add(new JLabel("No. Telepon:"));
        dialog.add(txtNoTelepon, "wrap, growx");
        dialog.add(new JLabel("Email:"));
        dialog.add(txtEmail, "wrap, growx");

        JCheckBox chkKeepOpen = new JCheckBox("Tetap di form");
        if (penerbit == null) {
            dialog.add(chkKeepOpen, "wrap");
        } else {
            dialog.add(new JLabel(""), "wrap");
        }

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            String nama = txtNama.getText();
            String alamat = txtAlamat.getText();
            String kota = txtKota.getText();
            String noTelepon = txtNoTelepon.getText();
            String email = txtEmail.getText();

            PenerbitData newPenerbit = new PenerbitData(
                (penerbit == null) ? 0 : penerbit.getIdPenerbit(),
                nama,
                alamat,
                kota,
                noTelepon,
                email
            );

            if (penerbit == null) {
                savePenerbit(newPenerbit);
                loadData(); 

                if (chkKeepOpen.isSelected()) {
                    txtNama.setText("");
                    txtAlamat.setText("");
                    txtKota.setText("");
                    txtNoTelepon.setText("");
                    txtEmail.setText("");
                    txtNama.requestFocus();
                } else {
                    dialog.dispose();
                }
            } else {
                updatePenerbit(newPenerbit);
                loadData();
                dialog.dispose();
            }
        });

        dialog.add(btnSave, "span, align right");
        dialog.setVisible(true);
    }

    // =================================== DAO Logic ====================================

    private List<PenerbitData> getAllPenerbit(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<PenerbitData> list = new ArrayList<>();

        if (!sortColumn.matches("id_penerbit|nama_penerbit|alamat|kota|no_telepon|email")) {
            sortColumn = "id_penerbit";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String sql = "SELECT id_penerbit, nama_penerbit, alamat, kota, no_telepon, email FROM penerbit " +
                     "WHERE nama_penerbit LIKE ? OR kota LIKE ? OR email LIKE ? " +
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
                    list.add(new PenerbitData(
                        rs.getInt("id_penerbit"),
                        rs.getString("nama_penerbit"),
                        rs.getString("alamat"),
                        rs.getString("kota"),
                        rs.getString("no_telepon"),
                        rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching penerbit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getPenerbitTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM penerbit " +
                     "WHERE nama_penerbit LIKE ? OR kota LIKE ? OR email LIKE ?";

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
             JOptionPane.showMessageDialog(this, "Error counting penerbit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return count;
    }

    private void savePenerbit(PenerbitData penerbit) {
        String sql = "INSERT INTO penerbit (nama_penerbit, alamat, kota, no_telepon, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, penerbit.getNamaPenerbit());
            pstmt.setString(2, penerbit.getAlamat());
            pstmt.setString(3, penerbit.getKota());
            pstmt.setString(4, penerbit.getNoTelepon());
            pstmt.setString(5, penerbit.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving penerbit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePenerbit(PenerbitData penerbit) {
        String sql = "UPDATE penerbit SET nama_penerbit = ?, alamat = ?, kota = ?, no_telepon = ?, email = ? WHERE id_penerbit = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, penerbit.getNamaPenerbit());
            pstmt.setString(2, penerbit.getAlamat());
            pstmt.setString(3, penerbit.getKota());
            pstmt.setString(4, penerbit.getNoTelepon());
            pstmt.setString(5, penerbit.getEmail());
            pstmt.setInt(6, penerbit.getIdPenerbit());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating penerbit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePenerbit(int id) {
        String sql = "DELETE FROM penerbit WHERE id_penerbit = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting penerbit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PenerbitData getPenerbitById(int id) {
        PenerbitData penerbit = null;
        String sql = "SELECT * FROM penerbit WHERE id_penerbit = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    penerbit = new PenerbitData(
                        rs.getInt("id_penerbit"),
                        rs.getString("nama_penerbit"),
                        rs.getString("alamat"),
                        rs.getString("kota"),
                        rs.getString("no_telepon"),
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting penerbit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return penerbit;
    }

    // --- Main Method ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Penerbit().setVisible(true);
        });
    }
}
