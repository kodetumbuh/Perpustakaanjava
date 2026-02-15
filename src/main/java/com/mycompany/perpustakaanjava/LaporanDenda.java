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

public class LaporanDenda extends JFrame {
    
    private JDateChooser dateStart;
    private JDateChooser dateEnd;
    private JButton btnGenerate;

    public LaporanDenda() {
        initUI();
    }

    private void initUI() {
        setTitle("Laporan Denda");
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
                    .setPadding(5);
            StyleBuilder detailStyle = stl.style()
                    .setBorder(stl.pen1Point())
                    .setPadding(5);

            // 2. Main Query for the Table (Detail)
            String mainQuery = "SELECT " +
                    "a.nama as nama_peminjam, " +
                    "b.judul as nama_buku, " + 
                    "p.denda, " +
                    "p.tgl_peminjaman, " +
                    "p.tgl_kembali_rencana, " +
                    "p.tgl_kembali_aktual " +
                    "FROM peminjaman_detail pd " +
                    "JOIN peminjaman p ON pd.id_peminjaman = p.id_peminjaman " +
                    "JOIN anggota a ON p.id_anggota = a.id_anggota " +
                    "JOIN buku b ON pd.id_buku = b.id_buku " +
                    "WHERE p.tgl_peminjaman BETWEEN '" + strStart + "' AND '" + strEnd + "' " +
                    "ORDER BY p.tgl_peminjaman DESC";

            // 3. Main Report (Table Only)
            JasperPrint jasperPrint = report()
                .setPageFormat(net.sf.dynamicreports.report.constant.PageType.A4, net.sf.dynamicreports.report.constant.PageOrientation.PORTRAIT)
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(detailStyle)
                .highlightDetailEvenRows()
                .columns(
                    col.column("Nama Peminjam", "nama_peminjam", type.stringType()).setWidth(120),
                    col.column("Judul Buku", "nama_buku", type.stringType()).setWidth(150),
                    col.column("Total Denda", "denda", type.bigDecimalType()).setWidth(80),
                    col.column("Tgl Peminjaman", "tgl_peminjaman", type.dateType()).setWidth(90),
                    col.column("Tgl Pengembalian", "tgl_kembali_rencana", type.dateType()).setWidth(90),
                    col.column("Tgl Pengembalian Aktual", "tgl_kembali_aktual", type.dateType()).setWidth(90)
                )
                .title(
                    Components.text("Laporan Denda (" + strStart + " - " + strEnd + ")")
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setStyle(boldStyle),
                    Components.verticalGap(20)
                )
                .pageFooter(Components.pageXofY())
                .setDataSource(mainQuery, conn)
                .toJasperPrint();
            
            // Display report
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Laporan Denda");
            viewer.setExtendedState(JFrame.MAXIMIZED_BOTH);
            viewer.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal membuat laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
