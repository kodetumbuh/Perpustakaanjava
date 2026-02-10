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
 * Main application frame for managing User.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class User extends JFrame {

    // --- Model Class ---
    public static class UserData {
        private int idUser;
        private String username;
        private String password;
        private String namaLengkap;
        private String noTelepon;
        private String role;
        private String status;
        private Date tglDibuat;

        public UserData() {}

        public UserData(int idUser, String username, String password, String namaLengkap, String noTelepon, String role, String status, Date tglDibuat) {
            this.idUser = idUser;
            this.username = username;
            this.password = password;
            this.namaLengkap = namaLengkap;
            this.noTelepon = noTelepon;
            this.role = role;
            this.status = status;
            this.tglDibuat = tglDibuat;
        }

        public int getIdUser() { return idUser; }
        public void setIdUser(int idUser) { this.idUser = idUser; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNamaLengkap() { return namaLengkap; }
        public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
        public String getNoTelepon() { return noTelepon; }
        public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getTglDibuat() { return tglDibuat; }
        public void setTglDibuat(Date tglDibuat) { this.tglDibuat = tglDibuat; }

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
    private String sortColumn = "id_user";
    private String sortOrder = "ASC";

    public User() {
        setTitle("Master User");
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
        pnlHeader.add(new JLabel("Search (Username/Nama):"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New User");
        btnAdd.addActionListener(e -> showUserDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        // Exclude Password from table view
        String[] columnNames = {"ID", "Username", "Nama Lengkap", "No Telepon", "Role", "Status", "Tgl Dibuat"};
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
                String[] dbColumns = {"id_user", "username", "nama_lengkap", "no_telepon", "role", "status", "tgl_dibuat"};

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
                        UserData user = getUserById(id);
                        if (user != null) {
                            showUserDialog(user);
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
                UserData user = getUserById(id);
                if (user != null) {
                    showUserDialog(user);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                 int id = (int) table.getValueAt(selectedRow, 0);
                 int confirm = JOptionPane.showConfirmDialog(User.this, "Are you sure you want to delete this user?", "Delete", JOptionPane.YES_NO_OPTION);
                 if (confirm == JOptionPane.YES_OPTION) {
                     deleteUser(id);
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

        List<UserData> list = getAllUser(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getUserTotalCount(search);

        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (UserData u : list) {
            tableModel.addRow(new Object[]{
                u.getIdUser(),
                u.getUsername(),
                u.getNamaLengkap(),
                u.getNoTelepon(),
                u.getRole(),
                u.getStatus(),
                u.getTglDibuat() != null ? sdf.format(u.getTglDibuat()) : ""
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private void showUserDialog(UserData user) {
        JDialog dialog = new JDialog(this, user == null ? "Add User" : "Edit User", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[]"));
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);

        JTextField txtUsername = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JTextField txtNamaLengkap = new JTextField(20);
        JTextField txtNoTelepon = new JTextField(20);
        String[] roles = {"Admin", "Petugas", "Pustakawan"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        String[] statuses = {"Aktif", "Nonaktif"};
        JComboBox<String> cmbStatus = new JComboBox<>(statuses);
        JDateChooser dateChooserDibuat = new JDateChooser();
        dateChooserDibuat.setLocale(new Locale("id"));
        dateChooserDibuat.setDateFormatString("yyyy-MM-dd");

        if (user != null) {
            txtUsername.setText(user.getUsername());
            txtPassword.setText(user.getPassword());
            txtNamaLengkap.setText(user.getNamaLengkap());
            txtNoTelepon.setText(user.getNoTelepon());
            cmbRole.setSelectedItem(user.getRole());
            cmbStatus.setSelectedItem(user.getStatus());
            dateChooserDibuat.setDate(user.getTglDibuat());
        } else {
            dateChooserDibuat.setDate(new Date()); // Default today
        }

        dialog.add(new JLabel("Username:"));
        dialog.add(txtUsername, "wrap, growx");
        
        dialog.add(new JLabel("Password:"));
        dialog.add(txtPassword, "wrap, growx");
        
        dialog.add(new JLabel("Nama Lengkap:"));
        dialog.add(txtNamaLengkap, "wrap, growx");
        
        dialog.add(new JLabel("No Telepon:"));
        dialog.add(txtNoTelepon, "wrap, growx");
        
        dialog.add(new JLabel("Role:"));
        dialog.add(cmbRole, "wrap, growx");
        
        dialog.add(new JLabel("Status:"));
        dialog.add(cmbStatus, "wrap, growx");
        
        dialog.add(new JLabel("Tgl Dibuat:"));
        dialog.add(dateChooserDibuat, "wrap, growx");

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword()); // Plain text for now
            String namaLengkap = txtNamaLengkap.getText();
            String noTelepon = txtNoTelepon.getText();
            String role = (String) cmbRole.getSelectedItem();
            String status = (String) cmbStatus.getSelectedItem();
            Date tglDibuat = dateChooserDibuat.getDate();

            if (username.isEmpty() || password.isEmpty() || namaLengkap.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Username, Password, and Nama Lengkap.");
                return;
            }

            UserData newUser = new UserData(
                (user == null) ? 0 : user.getIdUser(),
                username,
                password,
                namaLengkap,
                noTelepon,
                role,
                status,
                tglDibuat
            );

            if (user == null) {
                saveUser(newUser);
            } else {
                updateUser(newUser);
            }
            loadData();
            dialog.dispose();
        });

        dialog.add(btnSave, "span, align right, gaptop 10");
        dialog.setVisible(true);
    }

    // =================================== DAO Logic ====================================

    private List<UserData> getAllUser(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {
        List<UserData> list = new ArrayList<>();

        if (!sortColumn.matches("id_user|username|nama_lengkap|no_telepon|role|status|tgl_dibuat")) {
            sortColumn = "id_user";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String sql = "SELECT id_user, username, password, nama_lengkap, no_telepon, role, status, tgl_dibuat FROM user " +
                     "WHERE username LIKE ? OR nama_lengkap LIKE ? " +
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
                    list.add(new UserData(
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("nama_lengkap"),
                        rs.getString("no_telepon"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getDate("tgl_dibuat")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getUserTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM user " +
                     "WHERE username LIKE ? OR nama_lengkap LIKE ?";

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

    private void saveUser(UserData user) {
        String sql = "INSERT INTO user (username, password, nama_lengkap, no_telepon, role, status, tgl_dibuat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNamaLengkap());
            pstmt.setString(4, user.getNoTelepon());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getStatus());
            pstmt.setDate(7, new java.sql.Date(user.getTglDibuat().getTime()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser(UserData user) {
        String sql = "UPDATE user SET username=?, password=?, nama_lengkap=?, no_telepon=?, role=?, status=?, tgl_dibuat=? WHERE id_user=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNamaLengkap());
            pstmt.setString(4, user.getNoTelepon());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getStatus());
            pstmt.setDate(7, new java.sql.Date(user.getTglDibuat().getTime()));
            pstmt.setInt(8, user.getIdUser());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser(int id) {
        String sql = "DELETE FROM user WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private UserData getUserById(int id) {
        UserData user = null;
        String sql = "SELECT * FROM user WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new UserData(
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("nama_lengkap"),
                        rs.getString("no_telepon"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getDate("tgl_dibuat")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new User().setVisible(true);
        });
    }
}
