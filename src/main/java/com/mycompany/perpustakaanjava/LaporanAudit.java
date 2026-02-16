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

public class LaporanAudit extends JFrame {
    
    private JDateChooser dateStart;
    private JDateChooser dateEnd;
    private JButton btnGenerate;

    public LaporanAudit() {
        initUI();
    }

    private void initUI() {
        setTitle("Laporan Audit Properti");
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
            
            // Check data availability
            String sqlCheck = "SELECT count(*) FROM properti WHERE tgl_input BETWEEN ? AND ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlCheck)) {
                pst.setString(1, strStart + " 00:00:00");
                pst.setString(2, strEnd + " 23:59:59");
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

            // Main Query for the Table (Detail)
            // Note: Join with user table to get username
            // Assuming tgl_input exists in properti table
            String mainQuery = "SELECT " +
                    "u.username as nama_user, " +
                    "p.nama_barang, " +
                    "p.status_barang, " +
                    "p.qty_barang, " +
                    "p.keterangan, " +
                    "p.tgl_input " +
                    "FROM properti p " +
                    "LEFT JOIN user u ON p.id_user = u.id_user " +
                    "WHERE p.tgl_input BETWEEN '" + strStart + " 00:00:00' AND '" + strEnd + " 23:59:59' " +
                    "ORDER BY p.tgl_input DESC";

            // Main Report (Table Only)
            JasperPrint jasperPrint = report()
                .setPageFormat(net.sf.dynamicreports.report.constant.PageType.A4, net.sf.dynamicreports.report.constant.PageOrientation.PORTRAIT)
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(detailStyle)
                .highlightDetailEvenRows()
                .columns(
                    col.column("Nama User", "nama_user", type.stringType()).setWidth(80),
                    col.column("Nama Barang", "nama_barang", type.stringType()).setWidth(100),
                    col.column("Status", "status_barang", type.stringType()).setWidth(80),
                    col.column("Qty", "qty_barang", type.integerType()).setWidth(40),
                    col.column("Keterangan", "keterangan", type.stringType()).setWidth(100),
                    col.column("Tgl Input", "tgl_input", type.dateType()).setWidth(60).setPattern("yyyy-MM-dd")
                )
                .title(
                    Components.text("Laporan Audit Properti (" + strStart + " - " + strEnd + ")")
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setStyle(boldStyle),
                    Components.verticalGap(20)
                )
                .pageFooter(Components.pageXofY())
                .setDataSource(mainQuery, conn)
                .toJasperPrint();
            
            // Display report
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Laporan Audit Properti");
            viewer.setExtendedState(JFrame.MAXIMIZED_BOTH);
            viewer.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal membuat laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
