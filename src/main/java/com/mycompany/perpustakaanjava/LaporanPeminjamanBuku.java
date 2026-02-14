package com.mycompany.perpustakaanjava;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.miginfocom.swing.MigLayout;
import com.toedter.calendar.JDateChooser;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class LaporanPeminjamanBuku extends JFrame {
    
    private JDateChooser dateStart;
    private JDateChooser dateEnd;
    private JButton btnGenerate;

    public LaporanPeminjamanBuku() {
        initUI();
    }

    private void initUI() {
        setTitle("Laporan Peminjaman Buku");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20", "[][grow]", "[]10[]20[]"));

        // Components
        dateStart = new JDateChooser();
        dateStart.setDateFormatString("yyyy-MM-dd");
        
        dateEnd = new JDateChooser();
        dateEnd.setDateFormatString("yyyy-MM-dd");
        
        btnGenerate = new JButton("Hasil Laporan");

        // Layout
        add(new JLabel("Tanggal Mulai:"));
        add(dateStart, "growx, wrap");
        
        add(new JLabel("Tanggal Selesai:"));
        add(dateEnd, "growx, wrap");
        
        add(btnGenerate, "span, growx, h 40!");

        // Action Listener
        btnGenerate.addActionListener(e -> generateReport());
    }

    private void generateReport() {
        Date start = dateStart.getDate();
        Date end = dateEnd.getDate();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this, "Harap pilih rentang tanggal lengkap!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (start.after(end)) {
            JOptionPane.showMessageDialog(this, "Tanggal mulai tidak boleh lebih besar dari tanggal selesai!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strStart = sdf.format(start);
        String strEnd = sdf.format(end);

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // 1. Build Report Query (Table Data)
            String sqlCheck = "SELECT count(*) FROM peminjaman WHERE tgl_peminjaman BETWEEN ? AND ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlCheck)) {
                pst.setString(1, strStart);
                pst.setString(2, strEnd);
                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                     JOptionPane.showMessageDialog(this, "Tidak ada data pada rentang tanggal tersebut.", "Info", JOptionPane.INFORMATION_MESSAGE);
                     return;
                }
            }

            // Defines styles
            StyleBuilder boldStyle = stl.style().bold();
            StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
            StyleBuilder columnTitleStyle = stl.style(boldCenteredStyle)
                    .setBorder(stl.pen1Point())
                    .setBackgroundColor(Color.LIGHT_GRAY)
                    .setPadding(5); // Add padding to header
            StyleBuilder detailStyle = stl.style()
                    .setBorder(stl.pen1Point())
                    .setPadding(5); // Add padding to data cells (small gap)

            // 2. Report Configuration
            // Main Query for the Table (Detail)
            String mainQuery = "SELECT " +
                    "a.nama as nama_peminjam, " +
                    "b.judul as nama_buku, " + 
                    "p.tgl_peminjaman, " +
                    "p.tgl_kembali_rencana, " +
                    "p.tgl_kembali_aktual, " +
                    "p.status as status_buku " +
                    "FROM peminjaman_detail pd " +
                    "JOIN peminjaman p ON pd.id_peminjaman = p.id_peminjaman " +
                    "JOIN anggota a ON p.id_anggota = a.id_anggota " +
                    "JOIN buku b ON pd.id_buku = b.id_buku " +
                    "WHERE p.tgl_peminjaman BETWEEN '" + strStart + "' AND '" + strEnd + "' " +
                    "ORDER BY p.tgl_peminjaman DESC";

            // Chart Query (Grouped Data)
            String chartQuery = "SELECT p.status, COUNT(*) as jumlah " +
                                "FROM peminjaman p " +
                                "WHERE p.tgl_peminjaman BETWEEN '" + strStart + "' AND '" + strEnd + "' " +
                                "GROUP BY p.status";

            // 3. Subreport for Chart
            JasperReportBuilder chartReport = report();
            chartReport
                .setTemplate(Templates.reportTemplate) // Assuming minimal template or ignoring if not present
                .title(
                    Components.text("Statistik Status Buku").setStyle(boldCenteredStyle),
                    Components.verticalGap(10) // Small gap below chart title
                )
                .summary(
                    cht.pieChart()
                        .setTitle("Status Buku (Dipinjam vs Dikembalikan)")
                        .setKey(col.column("Status", "status", type.stringType()))
                        .series(cht.serie(col.column("Jumlah", "jumlah", type.integerType())))
                        .setFixedHeight(300)
                )
                .setDataSource(chartQuery, conn);

            // 4. Main Report
            JasperPrint jasperPrint = report()
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(detailStyle) // Apply border to all data cells
                .highlightDetailEvenRows()
                .columns(
                    col.column("Nama Peminjam", "nama_peminjam", type.stringType()),
                    col.column("Nama Buku", "nama_buku", type.stringType()),
                    col.column("Tgl Pinjam", "tgl_peminjaman", type.dateType()),
                    col.column("Tgl Kembali (Rencana)", "tgl_kembali_rencana", type.dateType()),
                    col.column("Tgl Kembali (Aktual)", "tgl_kembali_aktual", type.dateType()),
                    col.column("Status", "status_buku", type.stringType())
                )
                .title(
                    Components.text("Laporan Peminjaman Buku (" + strStart + " - " + strEnd + ")")
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setStyle(boldStyle),
                    Components.verticalGap(20) // Gap between main title and table
                )
                .summary(
                    Components.verticalGap(30), // Gap between table and subreport
                    cmp.subreport(chartReport)
                )
                .pageFooter(Components.pageXofY())
                .setDataSource(mainQuery, conn)
                .toJasperPrint();
            
            JasperViewer viewer = new JasperViewer(jasperPrint, false); // false = don't exit app on close
            viewer.setTitle("Laporan Peminjaman Buku");
            viewer.setExtendedState(JFrame.MAXIMIZED_BOTH);
            viewer.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal membuat laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Simple Templates replacement since we don't have the user's Templates class
    private static class Templates {
        public static final net.sf.dynamicreports.report.builder.ReportTemplateBuilder reportTemplate = template().setLocale(java.util.Locale.ENGLISH);
    }
}
