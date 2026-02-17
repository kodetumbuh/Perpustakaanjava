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
import java.awt.Dimension;

/**
 * Main application frame for managing Pengarang.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class Pengarang extends JFrame {

    // --- Model Class ---
    public static class PengarangData {
        private int idPengarang;
        private String namaPengarang;
        private String negara;
        private String biografi;
        private java.sql.Date tglLahir;

        public PengarangData() {}

        public PengarangData(int idPengarang, String namaPengarang, String negara, String biografi, java.sql.Date tglLahir) {
            this.idPengarang = idPengarang;
            this.namaPengarang = namaPengarang;
            this.negara = negara;
            this.biografi = biografi;
            this.tglLahir = tglLahir;
        }

        public int getIdPengarang() { return idPengarang; }
        public void setIdPengarang(int idPengarang) { this.idPengarang = idPengarang; }

        public String getNamaPengarang() { return namaPengarang; }
        public void setNamaPengarang(String namaPengarang) { this.namaPengarang = namaPengarang; }

        public String getNegara() { return negara; }
        public void setNegara(String negara) { this.negara = negara; }

        public String getBiografi() { return biografi; }
        public void setBiografi(String biografi) { this.biografi = biografi; }

        public java.sql.Date getTglLahir() { return tglLahir; }
        public void setTglLahir(java.sql.Date tglLahir) { this.tglLahir = tglLahir; }

        @Override
        public String toString() { return namaPengarang; }
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
    private String sortColumn = "id_pengarang";
    private String sortOrder = "ASC";

    public Pengarang() {
        setTitle("Master Data Pengarang");
        setMinimumSize(new Dimension(900, 600));
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
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Pengarang");
        btnAdd.addActionListener(e -> showPengarangDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        String[] columnNames = {"ID", "Nama Pengarang", "Negara", "Tanggal Lahir", "Biografi"};
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
                String[] dbColumns = {"id_pengarang", "nama_pengarang", "negara", "tgl_lahir", "biografi"};

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
                        PengarangData p = getPengarangById(id);
                        if (p != null) {
                            showPengarangDialog(p);
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
                PengarangData p = getPengarangById(id);
                if (p != null) {
                    showPengarangDialog(p);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                 int id = (int) table.getValueAt(selectedRow, 0);
                 int confirm = JOptionPane.showConfirmDialog(Pengarang.this, "Are you sure you want to delete this Pengarang?", "Delete", JOptionPane.YES_NO_OPTION);
                 if (confirm == JOptionPane.YES_OPTION) {
                     deletePengarang(id);
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

        List<PengarangData> list = getAllPengarang(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getPengarangTotalCount(search);

        tableModel.setRowCount(0);
        for (PengarangData p : list) {
            tableModel.addRow(new Object[]{
                p.getIdPengarang(),
                p.getNamaPengarang(),
                p.getNegara(),
                p.getTglLahir(),
                p.getBiografi()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showPengarangDialog(PengarangData pengarang) {
        JDialog dialog = new JDialog(this, pengarang == null ? "Add Pengarang" : "Edit Pengarang", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[][grow]", "[]"));
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);

        JTextField txtNama = new JTextField(20);
        JTextField txtNegara = new JTextField(20);
        JTextArea txtBiografi = new JTextArea(5, 20);
        txtBiografi.setLineWrap(true);
        txtBiografi.setWrapStyleWord(true);
        
        // Date Picker using JDateChooser (jcalendar)
        com.toedter.calendar.JDateChooser dateChooser = new com.toedter.calendar.JDateChooser();
        dateChooser.setLocale(new java.util.Locale("id", "ID"));
        dateChooser.setDateFormatString("dd-MM-yyyy");

        if (pengarang != null) {
            txtNama.setText(pengarang.getNamaPengarang());
            txtNegara.setText(pengarang.getNegara());
            txtBiografi.setText(pengarang.getBiografi());
            if (pengarang.getTglLahir() != null) {
                dateChooser.setDate(pengarang.getTglLahir());
            }
        }

        dialog.add(new JLabel("Nama Pengarang:"));
        dialog.add(txtNama, "wrap, growx");
        dialog.add(new JLabel("Negara:"));
        dialog.add(txtNegara, "wrap, growx");
        dialog.add(new JLabel("Tanggal Lahir:"));
        dialog.add(dateChooser, "wrap, growx");
        dialog.add(new JLabel("Biografi:"));
        dialog.add(new JScrollPane(txtBiografi), "wrap, grow, h 100!");

        JCheckBox chkKeepOpen = new JCheckBox("Tetap di form");
        if (pengarang == null) {
            dialog.add(chkKeepOpen, "wrap");
        } else {
            dialog.add(new JLabel(""), "wrap");
        }

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            String nama = txtNama.getText();
            String negara = txtNegara.getText();
            String biografi = txtBiografi.getText();
            java.util.Date utilDate = dateChooser.getDate();
            java.sql.Date tglLahir = (utilDate != null) ? new java.sql.Date(utilDate.getTime()) : null;

            PengarangData newPengarang = new PengarangData(
                (pengarang == null) ? 0 : pengarang.getIdPengarang(),
                nama,
                negara,
                biografi,
                tglLahir
            );

            if (pengarang == null) {
                savePengarang(newPengarang);
                loadData(); 

                if (chkKeepOpen.isSelected()) {
                    txtNama.setText("");
                    txtNegara.setText("");
                    txtBiografi.setText("");
                    dateChooser.setDate(null);
                    txtNama.requestFocus();
                } else {
                    dialog.dispose();
                }
            } else {
                updatePengarang(newPengarang);
                loadData();
                dialog.dispose();
            }
        });

        dialog.add(btnSave, "span, align right");
        dialog.setVisible(true);
    }

    // =================================== DAO Logic ====================================

    private List<PengarangData> getAllPengarang(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<PengarangData> list = new ArrayList<>();

        if (!sortColumn.matches("id_pengarang|nama_pengarang|negara|biografi|tgl_lahir")) {
            sortColumn = "id_pengarang";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String sql = "SELECT id_pengarang, nama_pengarang, negara, biografi, tgl_lahir FROM pengarang " +
                     "WHERE nama_pengarang LIKE ? OR negara LIKE ? " +
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
                    list.add(new PengarangData(
                        rs.getInt("id_pengarang"),
                        rs.getString("nama_pengarang"),
                        rs.getString("negara"),
                        rs.getString("biografi"),
                        rs.getDate("tgl_lahir")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching pengarang: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getPengarangTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM pengarang " +
                     "WHERE nama_pengarang LIKE ? OR negara LIKE ?";

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
            JOptionPane.showMessageDialog(this, "Error counting pengarang: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return count;
    }

    private void savePengarang(PengarangData pengarang) {
        String sql = "INSERT INTO pengarang (nama_pengarang, negara, biografi, tgl_lahir) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pengarang.getNamaPengarang());
            pstmt.setString(2, pengarang.getNegara());
            pstmt.setString(3, pengarang.getBiografi());
            pstmt.setDate(4, pengarang.getTglLahir());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving pengarang: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePengarang(PengarangData pengarang) {
        String sql = "UPDATE pengarang SET nama_pengarang = ?, negara = ?, biografi = ?, tgl_lahir = ? WHERE id_pengarang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pengarang.getNamaPengarang());
            pstmt.setString(2, pengarang.getNegara());
            pstmt.setString(3, pengarang.getBiografi());
            pstmt.setDate(4, pengarang.getTglLahir());
            pstmt.setInt(5, pengarang.getIdPengarang());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating pengarang: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePengarang(int id) {
        String sql = "DELETE FROM pengarang WHERE id_pengarang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting pengarang: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PengarangData getPengarangById(int id) {
        PengarangData pengarang = null;
        String sql = "SELECT * FROM pengarang WHERE id_pengarang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pengarang = new PengarangData(
                        rs.getInt("id_pengarang"),
                        rs.getString("nama_pengarang"),
                        rs.getString("negara"),
                        rs.getString("biografi"),
                        rs.getDate("tgl_lahir")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting pengarang: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return pengarang;
    }

    // --- Main Method ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Pengarang().setVisible(true);
        });
    }
}
