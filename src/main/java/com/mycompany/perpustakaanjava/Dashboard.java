package com.mycompany.perpustakaanjava;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends JFrame {

    private JComboBox<String> cmbPeriode;
    private JPanel pnlChart;
    
    // UI Table Components
    private javax.swing.JTable table;
    private DefaultTableModel tableModel;
    
    // Pagination UI
    private JButton btnPrevious;
    private JButton btnNext;
    private JComboBox<Integer> cmbPageSize;
    private JLabel lblPageInfo;
    
    // Pagination State
    private int currentPage = 1;
    private int totalRecords = 0;

    public Dashboard() {
        // 1. Auto Full Screen Window Settings (Maximized)
        setTitle("Dashboard Utama - Perpustakaan");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 2. Initialize Menu Bar
        initMenuBar();

        // 3. Main Content
        initUI();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 10, aligny top", "[grow]", "[]0[]0[grow]0[]")); // Modified layout to fill and have grow row

        // === Header / Control Section ===
        // Set insets to 0 to remove padding
        JPanel pnlHeader = new JPanel(new MigLayout("insets 0", "[][][]", "[]"));
        
        JLabel lblPeriode = new JLabel("Pilih Periode:");
        lblPeriode.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        cmbPeriode = new JComboBox<>(new String[]{"Mingguan", "Bulanan", "Tahunan"});
        cmbPeriode.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Listener to update chart when combobox changes
        cmbPeriode.addActionListener(e -> refreshChart((String) cmbPeriode.getSelectedItem()));

        pnlHeader.add(lblPeriode, "gapright 10");
        pnlHeader.add(cmbPeriode);

        // Gap below header set to 0
        add(pnlHeader, "wrap, growx, gapbottom 0");

        // === Chart Section ===
        pnlChart = new JPanel(new BorderLayout());
        pnlChart.setBorder(BorderFactory.createEtchedBorder());
        // Updated to full width (growx) and smaller height (300px), with gaptop 0, aligny top
        add(pnlChart, "growx, h 300!, gaptop 20, gapbottom 20, wrap");

        // Load Default Chart (Mingguan)
        refreshChart("Mingguan");
        
        // === Table Section ===
        String[] columnNames = {"Nama Peminjam", "Nama Buku", "Status", "Tgl Pinjam", "Tgl Kembali (Rencana)", "Tgl Kembali (Aktual)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, "grow, pushy, wrap");
        
        // === Pagination Section ===
        add(createPaginationPanel(), "center");
        
        // Load Initial Table Data
        loadTableData();
    }

    private void refreshChart(String period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "";
        String dateLabelFormat = "";

        // Determine SQL Query based on Period
        if ("Mingguan".equals(period)) {
            // 7 Days (Daily)
            sql = "SELECT tgl_peminjaman as periode, COUNT(*) as jumlah FROM peminjaman " +
                  "WHERE tgl_peminjaman >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                  "GROUP BY tgl_peminjaman " +
                  "ORDER BY tgl_peminjaman ASC";
            dateLabelFormat = "dd MMM"; 
        } else if ("Bulanan".equals(period)) {
            // 1 Month (Daily) - Updated: 1 MONTH interval
            sql = "SELECT tgl_peminjaman as periode, COUNT(*) as jumlah FROM peminjaman " +
                  "WHERE tgl_peminjaman >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) " +
                  "GROUP BY tgl_peminjaman " +
                  "ORDER BY tgl_peminjaman ASC";
            dateLabelFormat = "dd MMM";
        } else if ("Tahunan".equals(period)) {
            // 1 Year (Monthly)
            sql = "SELECT DATE_FORMAT(tgl_peminjaman, '%Y-%m-01') as periode, COUNT(*) as jumlah FROM peminjaman " +
                  "WHERE tgl_peminjaman >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                  "GROUP BY DATE_FORMAT(tgl_peminjaman, '%Y-%m') " +
                  "ORDER BY periode ASC";
            dateLabelFormat = "MMMM yyyy";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfLabel = new SimpleDateFormat(dateLabelFormat, new java.util.Locale("id")); // Indonesian Locale

            while (rs.next()) {
                String rawDate = rs.getString("periode");
                int count = rs.getInt("jumlah");
                
                String label = rawDate;
                try {
                    // Try to format date nicely
                    if (rawDate != null) {
                        label = sdfLabel.format(sdfInput.parse(rawDate));
                    }
                } catch (Exception e) {
                    // Fallback if parsing fails
                    label = rawDate;
                }

                dataset.addValue(count, "Peminjaman", label);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching chart data: " + e.getMessage());
        }

        // Create Chart
        JFreeChart chart = ChartFactory.createBarChart(
            "Statistik Peminjaman (" + period + ")", // Chart Title
            "Periode", // X-Axis Label
            "Jumlah Peminjaman", // Y-Axis Label
            dataset, // Dataset
            PlotOrientation.VERTICAL,
            false, // Legend
            true, // Tooltips
            false // URLs
        );

        // Customize Chart for better aesthetics
        chart.setBackgroundPaint(new Color(240, 240, 240));
        chart.getTitle().setFont(new Font("SansSerif", Font.PLAIN, 15));

        // Configure Axis to show only integers
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
        
        // Create Chart Panel with custom size
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPreferredSize(new Dimension(1200, 400)); // Wide width, short height

        // Update Panel
        pnlChart.removeAll();
        pnlChart.add(chartPanel, BorderLayout.CENTER);
        pnlChart.revalidate();
        pnlChart.repaint();
    }


    
    // --- Pagination Methods ---
    private JPanel createPaginationPanel() {
        JPanel pnlPagination = new JPanel(new MigLayout("", "[][push][]", "[]"));
        
        btnPrevious = new JButton("Previous");
        btnNext = new JButton("Next");
        Integer[] pageSizes = {10, 25, 50, 100};
        cmbPageSize = new JComboBox<>(pageSizes);
        lblPageInfo = new JLabel("Page 1");

        btnPrevious.addActionListener(e -> {
            currentPage--;
            loadTableData();
        });
        
        btnNext.addActionListener(e -> {
            currentPage++;
            loadTableData();
        });
        
        cmbPageSize.addActionListener(e -> {
            currentPage = 1;
            loadTableData();
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
    
    private void loadTableData() {
        int pageSize = (int) cmbPageSize.getSelectedItem();
        int offset = (currentPage - 1) * pageSize;
        
        // Query with JOIN
        String sql = "SELECT a.nama as nama_peminjam, b.judul as nama_buku, p.status, p.tgl_peminjaman, p.tgl_kembali_rencana, p.tgl_kembali_aktual " +
                     "FROM peminjaman_detail pd " +
                     "JOIN peminjaman p ON pd.id_peminjaman = p.id_peminjaman " +
                     "JOIN anggota a ON p.id_anggota = a.id_anggota " +
                     "JOIN buku b ON pd.id_buku = b.id_buku " + // Fixed Join condition
                     "ORDER BY p.tgl_peminjaman DESC " +
                     "LIMIT ? OFFSET ?";

        totalRecords = getPeminjamanDetailTotalCount();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                tableModel.setRowCount(0);
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString("nama_peminjam"),
                        rs.getString("nama_buku"),
                        rs.getString("status"),
                        rs.getDate("tgl_peminjaman"),
                        rs.getDate("tgl_kembali_rencana"),
                        rs.getDate("tgl_kembali_aktual")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching table data: " + e.getMessage());
        }
        
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;
        updatePageInfo(currentPage, totalPages, totalRecords);
    }
    
    private int getPeminjamanDetailTotalCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM peminjaman_detail";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }


    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // --- MAIN MENU DEFINITION ---
        JMenu MenuDataMaster = new JMenu("Master Data");
        JMenu MenuAktifitas = new JMenu("Aktifitas");
        JMenu MenuLaporan = new JMenu("Laporan");
        JMenu MenuCetak = new JMenu("Cetak");
        JMenu MenuAudit = new JMenu("Audit Perpustakaan");
        JMenu MenuAdministrasi = new JMenu("Administrasi"); 
        JMenu MenuPengaturan = new JMenu("Pengaturan");       

        // ======================= 1. MASTER DATA MENU =======================
        JMenuItem itemAnggota = new JMenuItem("Anggota");
        JMenuItem itemBuku = new JMenuItem("Buku");
        JMenuItem itemKategori = new JMenuItem("Kategori");
        JMenuItem itemPenerbit = new JMenuItem("Penerbit");
        JMenuItem itemPengarang = new JMenuItem("Pengarang");
        JMenuItem itemRak = new JMenuItem("Rak Lemari");
        
        // Master Data Listeners
        itemAnggota.addActionListener(e -> {
            Anggota f = new Anggota();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });        
        
        itemBuku.addActionListener(e -> {
            Buku f = new Buku();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        itemKategori.addActionListener(e -> {
            Kategori f = new Kategori();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        itemPenerbit.addActionListener(e -> {
            Penerbit f = new Penerbit();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        itemPengarang.addActionListener(e -> {
            Pengarang f = new Pengarang();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        itemRak.addActionListener(e -> {
            Rak f = new Rak();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        MenuDataMaster.add(itemAnggota);
        MenuDataMaster.add(itemBuku);        
        MenuDataMaster.add(itemKategori);
        MenuDataMaster.add(itemPenerbit);
        MenuDataMaster.add(itemPengarang);
        MenuDataMaster.add(itemRak);

        // ======================= 2. ACTIVITY MENU =======================
        JMenuItem itemPeminjaman = new JMenuItem("Peminjaman Buku");
        JMenuItem itemPeminjamanDetail = new JMenuItem("Peminjaman Buku Detail");
        JMenuItem itemPengembalian = new JMenuItem("Pengembalian");
        JMenuItem itemReservasi = new JMenuItem("Reservasi");
        
        // Activity Listeners
        itemPeminjaman.addActionListener(e -> {
            Peminjaman f = new Peminjaman();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
            // Refresh chart when borrowing window is closed (optional, for real-time update)
            f.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    refreshChart((String) cmbPeriode.getSelectedItem());
                }
            });
        });
        
        itemPeminjamanDetail.addActionListener(e -> {
            PeminjamanDetail f = new PeminjamanDetail();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        itemPengembalian.addActionListener(e -> {
            Pengembalian f = new Pengembalian();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        itemReservasi.addActionListener(e -> {
            Reservasi f = new Reservasi();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });

        MenuAktifitas.add(itemPeminjaman); 
        MenuAktifitas.add(itemPeminjamanDetail); 
        MenuAktifitas.add(itemPengembalian); 
        MenuAktifitas.add(itemReservasi); 
        
        // ======================= 3. REPORT MENU =======================
        JMenuItem itemLaporanBulan = new JMenuItem("Laporan Peminjaman Buku");
        JMenuItem itemLaporanPengembalian = new JMenuItem("Laporan Pengembalian Buku");
        JMenuItem itemLaporanBuku = new JMenuItem("Laporan Buku");
        JMenuItem itemLaporanAuditBuku = new JMenuItem("Laporan Audit Buku");
        JMenuItem itemLaporanDenda = new JMenuItem("Laporan Denda");
        
        // Report Listeners
        itemLaporanBulan.addActionListener(e -> {
            LaporanBuku f = new LaporanBuku();
            f.setVisible(true);
        });
        
        MenuLaporan.add(itemLaporanBulan);
        MenuLaporan.add(itemLaporanPengembalian);
        MenuLaporan.add(itemLaporanBuku);
        MenuLaporan.add(itemLaporanAuditBuku);
        MenuLaporan.add(itemLaporanDenda);
        
        // ======================= 4. PRINT MENU =======================
        JMenuItem itemCetak = new JMenuItem("Cetak Kartu");
        itemCetak.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Cetak belum tersedia."));
        MenuCetak.add(itemCetak);
        
        // ======================= 5. LIBRARY AUDIT MENU =======================
        JMenuItem itemAuditBuku = new JMenuItem("Audit Pengembalian Buku");
        JMenuItem itemAuditUser = new JMenuItem("Audit User");
        JMenuItem itemAuditProperti = new JMenuItem("Audit Properti");
        
        itemAuditBuku.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Audit belum tersedia."));
        
        MenuAudit.add(itemAuditBuku);
        MenuAudit.add(itemAuditUser);
        MenuAudit.add(itemAuditProperti);
        
        // ======================= 6. ADMINISTRATION MENU =======================
        JMenuItem itemAdministrasiAnggota = new JMenuItem("Administrasi Anggota");
        JMenuItem itemAdministrasiUser = new JMenuItem("Administrasi User");
        
        // Administration Listeners
        itemAdministrasiAnggota.addActionListener(e -> {
            Anggota f = new Anggota();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        itemAdministrasiUser.addActionListener(e -> {
            User f = new User();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        });
        
        MenuAdministrasi.add(itemAdministrasiAnggota);
        MenuAdministrasi.add(itemAdministrasiUser);
        
        // ======================= 7. SETTINGS MENU =======================
        JMenuItem itemPengaturanTema = new JMenuItem("Tema Windows");
        JMenuItem itemPengaturanBackup = new JMenuItem("Backup Data");
        JMenuItem itemLogout = new JMenuItem("Logout");
        // Report Listeners
        itemLaporanBulan.addActionListener(e -> {
            LaporanBuku f = new LaporanBuku();
            f.setVisible(true);
        });
        
        itemPengaturanTema.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Tema belum tersedia."));
        
        // Settings Listeners
        itemLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });


        MenuPengaturan.add(itemPengaturanTema);
        MenuPengaturan.add(itemPengaturanBackup);
        MenuPengaturan.addSeparator();
        MenuPengaturan.add(itemLogout);

        // --- ADD ALL MENUS TO BAR ---
        menuBar.add(MenuDataMaster);
        menuBar.add(MenuAktifitas);
        menuBar.add(MenuLaporan);
        menuBar.add(MenuCetak);
        menuBar.add(MenuAudit);
        menuBar.add(MenuAdministrasi);
        menuBar.add(MenuPengaturan);

        setJMenuBar(menuBar);
    }

    // --- Main Method ---
    public static void main(String[] args) {
        // Set Look and Feel (Optional, makes it look better)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}