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
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import de.vwsoft.barcodelib4j.oned.Barcode;
import de.vwsoft.barcodelib4j.oned.BarcodeType;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Main application frame for managing Anggota.
 * Includes both UI logic and Data Access Logic (DAO).
 */
public class Anggota extends JFrame {

    // --- Model Class ---
    public static class AnggotaData {
        private int idAnggota;
        private String noAnggota;
        private String nama;
        private String jenisKelamin;
        private String tempatLahir;
        private Date tanggalLahir;
        private String alamat;
        private String noTelepon;
        private String email;
        private String noIdentitas;
        private Date tglDaftar;
        private Date tglExpired;
        private String status;
        private String noBarcode;
        private String namaPhoto;

        public AnggotaData() {}

        public AnggotaData(int idAnggota, String noAnggota, String nama, String jenisKelamin, String tempatLahir,
                           Date tanggalLahir, String alamat, String noTelepon, String email, String noIdentitas,
                           Date tglDaftar, Date tglExpired, String status) {
            this.idAnggota = idAnggota;
            this.noAnggota = noAnggota;
            this.nama = nama;
            this.jenisKelamin = jenisKelamin;
            this.tempatLahir = tempatLahir;
            this.tanggalLahir = tanggalLahir;
            this.alamat = alamat;
            this.noTelepon = noTelepon;
            this.email = email;
            this.noIdentitas = noIdentitas;
            this.tglDaftar = tglDaftar;
            this.tglExpired = tglExpired;
            this.tglExpired = tglExpired;
            this.status = status;
            this.noBarcode = "";
            this.namaPhoto = "";
        }

        public AnggotaData(int idAnggota, String noAnggota, String nama, String jenisKelamin, String tempatLahir,
                           Date tanggalLahir, String alamat, String noTelepon, String email, String noIdentitas,
                           Date tglDaftar, Date tglExpired, String status, String noBarcode, String namaPhoto) {
            this.idAnggota = idAnggota;
            this.noAnggota = noAnggota;
            this.nama = nama;
            this.jenisKelamin = jenisKelamin;
            this.tempatLahir = tempatLahir;
            this.tanggalLahir = tanggalLahir;
            this.alamat = alamat;
            this.noTelepon = noTelepon;
            this.email = email;
            this.noIdentitas = noIdentitas;
            this.tglDaftar = tglDaftar;
            this.tglExpired = tglExpired;
            this.status = status;
            this.noBarcode = noBarcode;
            this.namaPhoto = namaPhoto;
        }

        public int getIdAnggota() { return idAnggota; }
        public void setIdAnggota(int idAnggota) { this.idAnggota = idAnggota; }
        public String getNoAnggota() { return noAnggota; }
        public void setNoAnggota(String noAnggota) { this.noAnggota = noAnggota; }
        public String getNama() { return nama; }
        public void setNama(String nama) { this.nama = nama; }
        public String getJenisKelamin() { return jenisKelamin; }
        public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }
        public String getTempatLahir() { return tempatLahir; }
        public void setTempatLahir(String tempatLahir) { this.tempatLahir = tempatLahir; }
        public Date getTanggalLahir() { return tanggalLahir; }
        public void setTanggalLahir(Date tanggalLahir) { this.tanggalLahir = tanggalLahir; }
        public String getAlamat() { return alamat; }
        public void setAlamat(String alamat) { this.alamat = alamat; }
        public String getNoTelepon() { return noTelepon; }
        public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNoIdentitas() { return noIdentitas; }
        public void setNoIdentitas(String noIdentitas) { this.noIdentitas = noIdentitas; }
        public Date getTglDaftar() { return tglDaftar; }
        public void setTglDaftar(Date tglDaftar) { this.tglDaftar = tglDaftar; }
        public Date getTglExpired() { return tglExpired; }
        public void setTglExpired(Date tglExpired) { this.tglExpired = tglExpired; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNoBarcode() { return noBarcode; }
        public void setNoBarcode(String noBarcode) { this.noBarcode = noBarcode; }
        public String getNamaPhoto() { return namaPhoto; }
        public void setNamaPhoto(String namaPhoto) { this.namaPhoto = namaPhoto; }

        @Override
        public String toString() { return nama; }
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
    private String sortColumn = "id_anggota";
    private String sortOrder = "ASC";

    public Anggota() {
        setTitle("Master Anggota");
        setSize(900, 600);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // === Header / Filter Section ===
        JPanel pnlHeader = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        pnlHeader.add(new JLabel("Search (Nama/No Anggota):"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); }); // Enter key
        pnlHeader.add(txtSearch, "growx");

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlHeader.add(btnSearch);

        JButton btnAdd = new JButton("Add New Anggota");
        btnAdd.addActionListener(e -> showAnggotaDialog(null));
        pnlHeader.add(btnAdd, "gapleft 20");

        add(pnlHeader, "wrap, growx");

        // === Table Section ===
        String[] columnNames = {"ID", "No Anggota", "Nama", "JK", "Tmp Lahir", "Tgl Lahir", "Alamat", "Telp", "Email", "No ID", "Tgl Daftar", "Tgl Expired", "Status"};
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
                String[] dbColumns = {"id_anggota", "no_anggota", "nama", "jenis_kelamin", "tempat_lahir", "tanggal_lahir", "alamat", "no_telepon", "email", "no_identitas", "tgl_daftar", "tgl_expired", "status"};

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
                        AnggotaData anggota = getAnggotaById(id);
                        if (anggota != null) {
                            showAnggotaDialog(anggota);
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
                AnggotaData anggota = getAnggotaById(id);
                if (anggota != null) {
                    showAnggotaDialog(anggota);
                }
            }
        });

        mnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) table.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(Anggota.this, "Are you sure you want to delete this anggota?", "Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteAnggota(id);
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

        List<AnggotaData> list = getAllAnggota(pageSize, offset, sortColumn, sortOrder, search);
        totalRecords = getAnggotaTotalCount(search);

        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (AnggotaData a : list) {
            tableModel.addRow(new Object[]{
                a.getIdAnggota(),
                a.getNoAnggota(),
                a.getNama(),
                a.getJenisKelamin(),
                a.getTempatLahir(),
                a.getTanggalLahir() != null ? sdf.format(a.getTanggalLahir()) : "",
                a.getAlamat(),
                a.getNoTelepon(),
                a.getEmail(),
                a.getNoIdentitas(),
                a.getTglDaftar() != null ? sdf.format(a.getTglDaftar()) : "",
                a.getTglExpired() != null ? sdf.format(a.getTglExpired()) : "",
                a.getStatus()
            });
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) totalPages = 1;

        updatePageInfo(currentPage, totalPages, totalRecords);
    }

    private BufferedImage capturedImage = null;

    private void showAnggotaDialog(AnggotaData anggota) {
        JDialog dialog = new JDialog(this, anggota == null ? "Add Anggota" : "Edit Anggota", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[320!][grow]", "[grow][]"));
        dialog.setSize(900, 650);
        dialog.setLocationRelativeTo(this);

        // --- Left Column: Webcam & Photo ---
        JPanel pnlCamera = new JPanel(new MigLayout("fill", "[center]", "[][][]"));
        
        Webcam webcam = Webcam.getDefault();
        WebcamPanel webcamPanel = null;
        JLabel lblPhotoPreview = new JLabel();
        lblPhotoPreview.setPreferredSize(new Dimension(300, 300));
        lblPhotoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        if (webcam != null) {
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcamPanel = new WebcamPanel(webcam);
            webcamPanel.setMirrored(false);
            webcamPanel.setPreferredSize(new Dimension(300, 225));
            pnlCamera.add(webcamPanel, "wrap, w 300!, h 225!");
        } else {
            pnlCamera.add(new JLabel("No Webcam Detected"), "wrap, w 300!, h 225!");
        }

        JButton btnAmbilPhoto = new JButton("Ambil Photo");
        JButton btnBatalPhoto = new JButton("Batal");
        
        pnlCamera.add(btnAmbilPhoto, "split 2, growx");
        pnlCamera.add(btnBatalPhoto, "growx, wrap");
        pnlCamera.add(lblPhotoPreview, "w 300!, h 225!");
        
        dialog.add(pnlCamera, "growy, top");

        // --- Right Column: Form Fields ---
        JPanel pnlForm = new JPanel(new MigLayout("fillx", "[right][grow]", "[]"));
        
        JTextField txtNoAnggota = new JTextField(20);
        txtNoAnggota.setEditable(false);
        JTextField txtNama = new JTextField(20);
        JComboBox<String> cmbJenisKelamin = new JComboBox<>(new String[]{"L", "P"});
        JTextField txtTempatLahir = new JTextField(20);
        JDateChooser dateChooserLahir = new JDateChooser();
        JTextField txtAlamat = new JTextField(20);
        JTextField txtNoTelepon = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtNoIdentitas = new JTextField(20);
        JDateChooser dateChooserDaftar = new JDateChooser();
        JDateChooser dateChooserExpired = new JDateChooser();
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Aktif", "Nonaktif", "Blokir"});
        JTextField txtNoBarcode = new JTextField(20);
        txtNoBarcode.setEditable(false);
        JLabel lblBarcodeImage = new JLabel();

        dateChooserLahir.setLocale(new Locale("id"));
        dateChooserLahir.setDateFormatString("yyyy-MM-dd");
        dateChooserDaftar.setLocale(new Locale("id"));
        dateChooserDaftar.setDateFormatString("yyyy-MM-dd");
        dateChooserExpired.setLocale(new Locale("id"));
        dateChooserExpired.setDateFormatString("yyyy-MM-dd");

        if (anggota != null) {
            txtNoAnggota.setText(anggota.getNoAnggota());
            txtNama.setText(anggota.getNama());
            cmbJenisKelamin.setSelectedItem(anggota.getJenisKelamin());
            txtTempatLahir.setText(anggota.getTempatLahir());
            dateChooserLahir.setDate(anggota.getTanggalLahir());
            txtAlamat.setText(anggota.getAlamat());
            txtNoTelepon.setText(anggota.getNoTelepon());
            txtEmail.setText(anggota.getEmail());
            txtNoIdentitas.setText(anggota.getNoIdentitas());
            dateChooserDaftar.setDate(anggota.getTglDaftar());
            dateChooserExpired.setDate(anggota.getTglExpired());
            cmbStatus.setSelectedItem(anggota.getStatus());
            txtNoBarcode.setText(anggota.getNoBarcode());
            if (anggota.getNoBarcode() != null && !anggota.getNoBarcode().isEmpty()) {
                lblBarcodeImage.setIcon(generateBarcodeImage(anggota.getNoBarcode()));
            }
            if (anggota.getNamaPhoto() != null && !anggota.getNamaPhoto().isEmpty()) {
                 String userHome = System.getProperty("user.home");
                 File photoFile = new File(userHome + "/Pictures/kartu-perpustakaan/" + anggota.getNamaPhoto());
                 if (photoFile.exists()) {
                     try {
                         BufferedImage img = ImageIO.read(photoFile);
                         java.awt.Image dimg = img.getScaledInstance(300, 225, java.awt.Image.SCALE_SMOOTH);
                         lblPhotoPreview.setIcon(new ImageIcon(dimg));
                     } catch (IOException ex) {
                         ex.printStackTrace();
                     }
                 }
            }
        } else {
            dateChooserDaftar.setDate(new Date());
            String generatedCode = String.valueOf(System.currentTimeMillis());
            txtNoBarcode.setText(generatedCode);
            lblBarcodeImage.setIcon(generateBarcodeImage(generatedCode));
            
            int randomNumAnggota = (int)(Math.random() * 900000) + 100000;
            txtNoAnggota.setText("ANG" + randomNumAnggota);
        }

        pnlForm.add(new JLabel("No Barcode:"));
        pnlForm.add(txtNoBarcode, "wrap, growx");
        pnlForm.add(new JLabel("Barcode Image:"));
        pnlForm.add(lblBarcodeImage, "wrap, height 60!"); 
        pnlForm.add(new JLabel("No Anggota:"));
        pnlForm.add(txtNoAnggota, "wrap, growx");
        pnlForm.add(new JLabel("Nama:"));
        pnlForm.add(txtNama, "wrap, growx");
        pnlForm.add(new JLabel("Jenis Kelamin:"));
        pnlForm.add(cmbJenisKelamin, "wrap, growx");
        pnlForm.add(new JLabel("Tempat Lahir:"));
        pnlForm.add(txtTempatLahir, "wrap, growx");
        pnlForm.add(new JLabel("Tanggal Lahir:"));
        pnlForm.add(dateChooserLahir, "wrap, growx");
        pnlForm.add(new JLabel("Alamat:"));
        pnlForm.add(txtAlamat, "wrap, growx");
        pnlForm.add(new JLabel("No Telepon:"));
        pnlForm.add(txtNoTelepon, "wrap, growx");
        pnlForm.add(new JLabel("Email:"));
        pnlForm.add(txtEmail, "wrap, growx");
        pnlForm.add(new JLabel("No Identitas:"));
        pnlForm.add(txtNoIdentitas, "wrap, growx");
        pnlForm.add(new JLabel("Tgl Daftar:"));
        pnlForm.add(dateChooserDaftar, "wrap, growx");
        pnlForm.add(new JLabel("Tgl Expired:"));
        pnlForm.add(dateChooserExpired, "wrap, growx");
        pnlForm.add(new JLabel("Status:"));
        pnlForm.add(cmbStatus, "wrap, growx");

        dialog.add(pnlForm, "grow, wrap");

        capturedImage = null;
        
        btnAmbilPhoto.addActionListener(e -> {
            if (webcam != null && webcam.isOpen()) {
                capturedImage = webcam.getImage();
                if (capturedImage != null) {
                   java.awt.Image dimg = capturedImage.getScaledInstance(300, 225, java.awt.Image.SCALE_SMOOTH);
                   lblPhotoPreview.setIcon(new ImageIcon(dimg));
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Webcam is not open or not detected.");
            }
        });

        btnBatalPhoto.addActionListener(e -> {
            capturedImage = null;
            lblPhotoPreview.setIcon(null);
        });

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            String no = txtNoAnggota.getText();
            String nama = txtNama.getText();
            String jk = (String) cmbJenisKelamin.getSelectedItem();
            String tmpLahir = txtTempatLahir.getText();
            Date tglLahir = dateChooserLahir.getDate();
            String alamat = txtAlamat.getText();
            String telp = txtNoTelepon.getText();
            String email = txtEmail.getText();
            String noId = txtNoIdentitas.getText();
            Date tglDaftar = dateChooserDaftar.getDate();
            Date tglExpired = dateChooserExpired.getDate();
            String status = (String) cmbStatus.getSelectedItem();
            String noBarcode = txtNoBarcode.getText();

            if (no.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in No Anggota and Nama.");
                return;
            }
            
            String photoFilename = (anggota != null) ? anggota.getNamaPhoto() : "";

            // Start of modified photo handling logic - no file saving for raw photo
            if (capturedImage != null) {
                // We do NOT save the file to disk as per request.
                // We just set a marker filename so DB is not empty.
                photoFilename = "CAPTURED_PHOTO";
            }
            // End of modified photo handling logic

            AnggotaData newAnggota = new AnggotaData(
                (anggota == null) ? 0 : anggota.getIdAnggota(),
                no, nama, jk, tmpLahir, tglLahir, alamat, telp, email, noId, tglDaftar, tglExpired, status, noBarcode, photoFilename
            );

            if (anggota == null) {
                saveAnggota(newAnggota);
            } else {
                updateAnggota(newAnggota);
            }
            
            generateLibraryCard(newAnggota, capturedImage);
            
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
            
            loadData();
            dialog.dispose();
        });
        
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (webcam != null && webcam.isOpen()) {
                    webcam.close();
                }
            }
        });

        dialog.add(btnSave, "span 2, align right, gaptop 10");
        dialog.setVisible(true);
    }

    private void generateLibraryCard(AnggotaData anggota, BufferedImage capturedPhoto) {
        try {
            // 1. Load Template
            BufferedImage template = ImageIO.read(getClass().getResource("/com/mycompany/perpustakaanjava/Templatecard.png"));
            if (template == null) {
               System.err.println("Template not found in resources!");
               return; 
            }
            
            int width = template.getWidth();
            int height = template.getHeight();
            
            BufferedImage cardImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = cardImage.createGraphics();
            
            // Draw Background
            g2d.drawImage(template, 0, 0, null);
            
            // --- BARCODE (Above Nomor Anggota) ---
            int barcodeHeight = 120; // Bigger height
            int barcodeWidth = 600;  // Bigger width
            int barcodeX = 50;       
            int barcodeY = 200;      // Move down significantly (was 100)

            if (anggota.getNoBarcode() != null && !anggota.getNoBarcode().isEmpty()) {
                ImageIcon barcodeIcon = generateBarcodeImage(anggota.getNoBarcode());
                if (barcodeIcon != null) {
                    java.awt.Image barcodeImg = barcodeIcon.getImage();
                    g2d.drawImage(barcodeImg, barcodeX, barcodeY, barcodeWidth, barcodeHeight, null); 
                }
            }

            // --- PHOTO (Right Side) ---
            int photoWidth = 200;
            int photoHeight = 250;
            int photoX = width - photoWidth - 50; // 50px padding from right
            int photoY = 220;
            
            // Draw Red Box Placeholder if no photo
            g2d.setColor(Color.RED);
            g2d.fillRect(photoX, photoY, photoWidth, photoHeight);

            if (capturedPhoto != null) {
                // Priority: Use captured photo from memory
                java.awt.Image scaled = capturedPhoto.getScaledInstance(photoWidth, photoHeight, java.awt.Image.SCALE_SMOOTH);
                g2d.drawImage(scaled, photoX, photoY, null);
            } else if (anggota.getNamaPhoto() != null && !anggota.getNamaPhoto().isEmpty() && !anggota.getNamaPhoto().equals("CAPTURED_PHOTO")) {
                 // Fallback: Use stored photo from disk (legacy or manually added)
                String userHome = System.getProperty("user.home");
                File photoFile = new File(userHome + "/Pictures/kartu-perpustakaan/" + anggota.getNamaPhoto());
                if (photoFile.exists()) {
                    BufferedImage photo = ImageIO.read(photoFile);
                    g2d.drawImage(photo, photoX, photoY, photoWidth, photoHeight, null);
                }
            }

            // --- TEXT DETAILS (Left Side) ---
            g2d.setColor(Color.BLACK);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 30));
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            int textX = 50;
            int textY = 350; // Adjusted text start Y to be below the lower barcode
            int lineHeight = 50; // Increased line height for bigger font
            
            g2d.drawString("Nomor Anggota : " + anggota.getNoAnggota(), textX, textY); textY += lineHeight;
            g2d.drawString("Nama : " + anggota.getNama(), textX, textY); textY += lineHeight;
            g2d.drawString("Tempat Lahir : " + anggota.getTempatLahir(), textX, textY); textY += lineHeight;
            g2d.drawString("Tanggal Lahir : " + (anggota.getTanggalLahir() != null ? sdf.format(anggota.getTanggalLahir()) : "-"), textX, textY); textY += lineHeight;
            g2d.drawString("Alamat : " + anggota.getAlamat(), textX, textY);
            
            g2d.dispose();
            
            // 5. Save Card Image
            String userHome = System.getProperty("user.home");
            File folder = new File(userHome + "/Pictures/kartu-perpustakaan");
            if (!folder.exists()) folder.mkdirs();
            
            File outputFile = new File(folder, "card_" + anggota.getNoAnggota() + ".png");
            ImageIO.write(cardImage, "png", outputFile);
            
            System.out.println("Library card generated at: " + outputFile.getAbsolutePath());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating library card: " + ex.getMessage());
        }
    }

    // =================================== DAO Logic ====================================

    private List<AnggotaData> getAllAnggota(int limit, int offset, String sortColumn, String sortOrder, String searchKeyword) {

        List<AnggotaData> list = new ArrayList<>();

        if (!sortColumn.matches("id_anggota|no_anggota|nama|jenis_kelamin|tempat_lahir|tanggal_lahir|alamat|no_telepon|email|no_identitas|tgl_daftar|tgl_expired|status")) {
            sortColumn = "id_anggota";
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String sql = "SELECT * FROM anggota " +
                     "WHERE no_anggota LIKE ? OR nama LIKE ? " +
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
                    list.add(new AnggotaData(
                        rs.getInt("id_anggota"),
                        rs.getString("no_anggota"),
                        rs.getString("nama"),
                        rs.getString("jenis_kelamin"),
                        rs.getString("tempat_lahir"),
                        rs.getDate("tanggal_lahir"),
                        rs.getString("alamat"),
                        rs.getString("no_telepon"),
                        rs.getString("email"),
                        rs.getString("no_identitas"),
                        rs.getDate("tgl_daftar"),
                        rs.getDate("tgl_expired"),
                        rs.getString("status"),
                        rs.getString("no_barcode"),
                        rs.getString("nama_photo")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching anggota: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int getAnggotaTotalCount(String searchKeyword) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM anggota " +
                     "WHERE no_anggota LIKE ? OR nama LIKE ?";

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

    private void saveAnggota(AnggotaData a) {
        String sql = "INSERT INTO anggota (no_anggota, nama, jenis_kelamin, tempat_lahir, tanggal_lahir, alamat, no_telepon, email, no_identitas, tgl_daftar, tgl_expired, status, no_barcode, nama_photo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getNoAnggota());
            pstmt.setString(2, a.getNama());
            pstmt.setString(3, a.getJenisKelamin());
            pstmt.setString(4, a.getTempatLahir());
            pstmt.setDate(5, a.getTanggalLahir() != null ? new java.sql.Date(a.getTanggalLahir().getTime()) : null);
            pstmt.setString(6, a.getAlamat());
            pstmt.setString(7, a.getNoTelepon());
            pstmt.setString(8, a.getEmail());
            pstmt.setString(9, a.getNoIdentitas());
            pstmt.setDate(10, a.getTglDaftar() != null ? new java.sql.Date(a.getTglDaftar().getTime()) : null);
            pstmt.setDate(11, a.getTglExpired() != null ? new java.sql.Date(a.getTglExpired().getTime()) : null);
            pstmt.setString(12, a.getStatus());
            pstmt.setString(13, a.getNoBarcode());
            pstmt.setString(14, a.getNamaPhoto());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving anggota: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAnggota(AnggotaData a) {
        String sql = "UPDATE anggota SET no_anggota=?, nama=?, jenis_kelamin=?, tempat_lahir=?, tanggal_lahir=?, alamat=?, no_telepon=?, email=?, no_identitas=?, tgl_daftar=?, tgl_expired=?, status=?, no_barcode=?, nama_photo=? " +
                     "WHERE id_anggota=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getNoAnggota());
            pstmt.setString(2, a.getNama());
            pstmt.setString(3, a.getJenisKelamin());
            pstmt.setString(4, a.getTempatLahir());
            pstmt.setDate(5, a.getTanggalLahir() != null ? new java.sql.Date(a.getTanggalLahir().getTime()) : null);
            pstmt.setString(6, a.getAlamat());
            pstmt.setString(7, a.getNoTelepon());
            pstmt.setString(8, a.getEmail());
            pstmt.setString(9, a.getNoIdentitas());
            pstmt.setDate(10, a.getTglDaftar() != null ? new java.sql.Date(a.getTglDaftar().getTime()) : null);
            pstmt.setDate(11, a.getTglExpired() != null ? new java.sql.Date(a.getTglExpired().getTime()) : null);
            pstmt.setString(12, a.getStatus());
            pstmt.setString(13, a.getNoBarcode());
            pstmt.setString(14, a.getNamaPhoto());
            pstmt.setInt(15, a.getIdAnggota());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating anggota: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAnggota(int id) {
        String sql = "DELETE FROM anggota WHERE id_anggota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting anggota: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private AnggotaData getAnggotaById(int id) {
        AnggotaData a = null;
        String sql = "SELECT * FROM anggota WHERE id_anggota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    a = new AnggotaData(
                        rs.getInt("id_anggota"),
                        rs.getString("no_anggota"),
                        rs.getString("nama"),
                        rs.getString("jenis_kelamin"),
                        rs.getString("tempat_lahir"),
                        rs.getDate("tanggal_lahir"),
                        rs.getString("alamat"),
                        rs.getString("no_telepon"),
                        rs.getString("email"),
                        rs.getString("no_identitas"),
                        rs.getDate("tgl_daftar"),
                        rs.getDate("tgl_expired"),
                        rs.getString("status"),
                        rs.getString("no_barcode"),
                        rs.getString("nama_photo")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return a;
    }

    private ImageIcon generateBarcodeImage(String text) {
        try {
            if (text == null || text.isEmpty()) return null;
            
            Barcode barcode = Barcode.newInstance(BarcodeType.CODE128);
            barcode.setContent(text, false, false);
            
            int width = 250;
            int height = 50;
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);
            g2.setColor(Color.BLACK);
            
            barcode.draw(g2, 10, 5, width - 20, height - 10);
            g2.dispose();
            
            return new ImageIcon(img);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Anggota().setVisible(true);
        });
    }
}
