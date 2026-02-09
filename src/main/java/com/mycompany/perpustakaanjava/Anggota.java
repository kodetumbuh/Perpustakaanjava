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
     * Main application frame for managing Kategori.
     * Includes both UI logic and Data Access Logic (DAO).
     */
    public class Anggota extends JFrame {

        // --- Model Class ---
        public static class KategoriData {
            private int idKategori;
            private String kodeKategori;
            private String namaKategori;
            private String deskripsi;

            public KategoriData() {}

            public KategoriData(int idKategori, String kodeKategori, String namaKategori, String deskripsi) {
                this.idKategori = idKategori;
                this.kodeKategori = kodeKategori;
                this.namaKategori = namaKategori;
                this.deskripsi = deskripsi;
            }

            public int getIdKategori() { return idKategori; }
            public void setIdKategori(int idKategori) { this.idKategori = idKategori; }
            public String getKodeKategori() { return kodeKategori; }
            public void setKodeKategori(String kodeKategori) { this.kodeKategori = kodeKategori; }
            public String getNamaKategori() { return namaKategori; }
            public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }
            public String getDeskripsi() { return deskripsi; }
            public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

            @Override
            public String toString() { return namaKategori; }
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
        private String sortColumn = "id_kategori";
        private String sortOrder = "ASC";

        public Anggota() {
            setTitle("Master Anggota");
            // Use standard frame setup, user might want maximization, but standard size is safer for now.
            // User had setExtendedState(JFrame.MAXIMIZED_BOTH) in the file I read. I will keep it.
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
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

            JButton btnAdd = new JButton("Add New Kategori");
            btnAdd.addActionListener(e -> showKategoriDialog(null));
            pnlHeader.add(btnAdd, "gapleft 20");

            add(pnlHeader, "wrap, growx");

            // === Table Section ===
            String[] columnNames = {"ID", "Kode", "Nama Kategori", "Deskripsi"};
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
                    String[] dbColumns = {"id_kategori", "kode_kategori", "nama_kategori", "deskripsi"};

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
                            KategoriData kat = getKategoriById(id);
                            if (kat != null) {
                                showKategoriDialog(kat);
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
                    KategoriData kat = getKategoriById(id);
                    if (kat != null) {
                        showKategoriDialog(kat);
                    }
                }
            });

            mnDelete.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                     int id = (int) table.getValueAt(selectedRow, 0);
                     int confirm = JOptionPane.showConfirmDialog(Anggota.this, "Are you sure you want to delete this category?", "Delete", JOptionPane.YES_NO_OPTION);
                     if (confirm == JOptionPane.YES_OPTION) {
                         deleteKategori(id);
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

            List<KategoriData> list = getAllKategori(pageSize, offset, sortColumn, sortOrder, search);
            totalRecords = getKategoriTotalCount(search);

            tableModel.setRowCount(0);
            for (KategoriData k : list) {
                tableModel.addRow(new Object[]{
                    k.getIdKategori(),
                    k.getKodeKategori(),
                    k.getNamaKategori(),
                    k.getDeskripsi()
                });
            }

            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages == 0) totalPages = 1;

            updatePageInfo(currentPage, totalPages, totalRecords);
        }

        private void showKategoriDialog(KategoriData kategori) {
            JDialog dialog = new JDialog(this, kategori == null ? "Add Kategori" : "Edit Kategori", true);
            dialog.setLayout(new MigLayout("fill, insets 20", "[][grow]", "[]"));
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);

            JTextField txtKode = new JTextField(20);
            JTextField txtNama = new JTextField(20);
            JTextField txtDeskripsi = new JTextField(20);

            if (kategori != null) {
                txtKode.setText(kategori.getKodeKategori());
                txtNama.setText(kategori.getNamaKategori());
                txtDeskripsi.setText(kategori.getDeskripsi());
            }

            dialog.add(new JLabel("Kode Kategori:"));
            dialog.add(txtKode, "wrap, growx");
            dialog.add(new JLabel("Nama Kategori:"));
            dialog.add(txtNama, "wrap, growx");
            dialog.add(new JLabel("Deskripsi:"));
            dialog.add(txtDeskripsi, "wrap, growx");

            JCheckBox chkKeepOpen = new JCheckBox("Tetap di form");
            if (kategori == null) {
                dialog.add(chkKeepOpen, "wrap");
            } else {
                dialog.add(new JLabel(""), "wrap");
            }

            JButton btnSave = new JButton("Save");
            btnSave.addActionListener(e -> {
                String kode = txtKode.getText();
                String nama = txtNama.getText();
                String deskripsi = txtDeskripsi.getText();

                KategoriData newKat = new KategoriData(
                    (kategori == null) ? 0 : kategori.getIdKategori(),
                    kode,
                    nama,
                    deskripsi
                );

                if (kategori == null) {
                    saveKategori(newKat);
                    loadData(); 

                    if (chkKeepOpen.isSelected()) {
                        txtKode.setText("");
                        txtNama.setText("");
                        txtDeskripsi.setText("");
                        txtKode.requestFocus();
                    } else {
                        dialog.dispose();
                    }
                } else {
                    updateKategori(newKat);
                    loadData();
                    dialog.dispose();
                }
            });

            dialog.add(btnSave, "span, align right");
            dialog.setVisible(true);
        }

        // =================================== DAO Logic ====================================

        private List<KategoriData> getAllKategori(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
            List<KategoriData> list = new ArrayList<>();

            if (!sortColumn.matches("id_kategori|kode_kategori|nama_kategori|deskripsi")) {
                sortColumn = "id_kategori";
            }
            if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
                sortOrder = "ASC";
            }

            String sql = "SELECT id_kategori, kode_kategori, nama_kategori, deskripsi FROM kategori " +
                         "WHERE kode_kategori LIKE ? OR nama_kategori LIKE ? OR deskripsi LIKE ? " +
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
                        list.add(new KategoriData(
                            rs.getInt("id_kategori"),
                            rs.getString("kode_kategori"),
                            rs.getString("nama_kategori"),
                            rs.getString("deskripsi")
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error fetching kategori: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            return list;
        }

        private int getKategoriTotalCount(String searchKeyword) {
            int count = 0;
            String sql = "SELECT COUNT(*) FROM kategori " +
                         "WHERE kode_kategori LIKE ? OR nama_kategori LIKE ? OR deskripsi LIKE ?";

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
                 JOptionPane.showMessageDialog(this, "Error counting kategori: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            return count;
        }

        private void saveKategori(KategoriData kategori) {
            String sql = "INSERT INTO kategori (kode_kategori, nama_kategori, deskripsi) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, kategori.getKodeKategori());
                pstmt.setString(2, kategori.getNamaKategori());
                pstmt.setString(3, kategori.getDeskripsi());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving kategori: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updateKategori(KategoriData kategori) {
            String sql = "UPDATE kategori SET kode_kategori = ?, nama_kategori = ?, deskripsi = ? WHERE id_kategori = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, kategori.getKodeKategori());
                pstmt.setString(2, kategori.getNamaKategori());
                pstmt.setString(3, kategori.getDeskripsi());
                pstmt.setInt(4, kategori.getIdKategori());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating kategori: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteKategori(int id) {
            String sql = "DELETE FROM kategori WHERE id_kategori = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting kategori: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private KategoriData getKategoriById(int id) {
            KategoriData kategori = null;
            String sql = "SELECT * FROM kategori WHERE id_kategori = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        kategori = new KategoriData(
                            rs.getInt("id_kategori"),
                            rs.getString("kode_kategori"),
                            rs.getString("nama_kategori"),
                            rs.getString("deskripsi")
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error getting kategori: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            return kategori;
        }

        // --- Main Method ---
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                new Anggota().setVisible(true);
            });
        }
    }
