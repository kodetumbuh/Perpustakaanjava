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

public class LaporanBuku extends JFrame {
    
    private JDateChooser dateStart;
    private JDateChooser dateEnd;
    private JButton btnGenerate;

    public LaporanBuku() {
        initUI();
    }

    private void initUI() {
        setTitle("Laporan Data Buku");
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
            
            // 1. Check if there's data in the date range
            String sqlCheck = "SELECT count(*) FROM buku WHERE tgl_masuk BETWEEN ? AND ?";
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
                    .setPadding(5);
            StyleBuilder detailStyle = stl.style()
                    .setBorder(stl.pen1Point())
                    .setPadding(5);

            // 2. Main Query for the Table (Detail)
            String mainQuery = "SELECT " +
                    "b.isbn, " +
                    "b.no_barcode, " +
                    "b.judul, " +
                    "p.nama_pengarang, " +
                    "pn.nama_penerbit, " +
                    "k.nama_kategori, " +
                    "b.tahun_terbit, " +
                    "b.edisi, " +
                    "b.halaman, " +
                    "b.bahasa, " +
                    "b.tgl_masuk, " +
                    "b.status " +
                    "FROM buku b " +
                    "LEFT JOIN pengarang p ON b.id_pengarang = p.id_pengarang " +
                    "LEFT JOIN penerbit pn ON b.id_penerbit = pn.id_penerbit " +
                    "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori " +
                    "WHERE b.tgl_masuk BETWEEN '" + strStart + "' AND '" + strEnd + "' " +
                    "ORDER BY b.tgl_masuk DESC";

            // Chart Query (Grouped Data by Status)
            String chartQuery = "SELECT b.status, COUNT(*) as jumlah " +
                                "FROM buku b " +
                                "WHERE b.tgl_masuk BETWEEN '" + strStart + "' AND '" + strEnd + "' " +
                                "GROUP BY b.status";

            // 3. Build Chart Subreport
            JasperReportBuilder chartReport = report();
            chartReport
                .setTemplate(Templates.reportTemplate)
                .setPageFormat(net.sf.dynamicreports.report.constant.PageType.A4, net.sf.dynamicreports.report.constant.PageOrientation.LANDSCAPE)
                .title(
                    Components.text("Distribusi Status Buku").setStyle(boldCenteredStyle),
                    Components.verticalGap(10)
                )
                .summary(
                    cht.pieChart()
                        .setTitle("Status Buku (Tersedia, Rusak, Hilang, Habis)")
                        .setKey(col.column("Status", "status", type.stringType()))
                        .series(cht.serie(col.column("Jumlah", "jumlah", type.integerType())))
                        .setFixedHeight(400)
                )
                .setDataSource(chartQuery, conn);

            // 4. Main Report with Table and Chart
            JasperPrint jasperPrint = report()
                .setPageFormat(net.sf.dynamicreports.report.constant.PageType.A4, net.sf.dynamicreports.report.constant.PageOrientation.LANDSCAPE)
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(detailStyle)
                .highlightDetailEvenRows()
                .columns(
                    col.column("ISBN", "isbn", type.stringType()).setWidth(80),
                    col.column("Barcode", "no_barcode", type.stringType()).setWidth(80),
                    col.column("Judul", "judul", type.stringType()).setWidth(150),
                    col.column("Pengarang", "nama_pengarang", type.stringType()).setWidth(100),
                    col.column("Penerbit", "nama_penerbit", type.stringType()).setWidth(100),
                    col.column("Kategori", "nama_kategori", type.stringType()).setWidth(80),
                    col.column("Tahun", "tahun_terbit", type.integerType()).setWidth(50).setPattern("0"),
                    col.column("Edisi", "edisi", type.stringType()).setWidth(60),
                    col.column("Hal", "halaman", type.integerType()).setWidth(40),
                    col.column("Bahasa", "bahasa", type.stringType()).setWidth(60),
                    col.column("Tgl Masuk", "tgl_masuk", type.dateType()).setWidth(70),
                    col.column("Status", "status", type.stringType()).setWidth(60)
                )
                .title(
                    Components.text("Laporan Data Buku (" + strStart + " - " + strEnd + ")")
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setStyle(boldStyle),
                    Components.verticalGap(20)
                )
                .summary(
                    Components.pageBreak(),
                    cmp.subreport(chartReport)
                )
                .pageFooter(Components.pageXofY())
                .setDataSource(mainQuery, conn)
                .toJasperPrint();
            
            // Display combined report
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Laporan Data Buku");
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
