package com.mycompany.perpustakaanjava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Dashboard extends JFrame {

    public Dashboard() {
        // 1. Pengaturan Window Otomatis Full Screen (Maximized)
        setTitle("Dashboard Utama - Perpustakaan");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 2. Inisialisasi Menu Bar
        initMenuBar();

        // 3. Konten Utama (Wallpaper Full Screen)
        setContentPane(new BackgroundPanel()); 
    }

    // --- Inner Class for Background Image ---
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                // Mencoba memuat gambar dari classpath
                java.net.URL imgUrl = getClass().getResource("/com/mycompany/perpustakaanjava/WallpaperHome.jpg");
                if (imgUrl == null) {
                    // Coba tanpa path absolut jika di root
                    imgUrl = getClass().getResource("WallpaperHome.jpg");
                }
                
                if (imgUrl != null) {
                    backgroundImage = new ImageIcon(imgUrl).getImage();
                } else {
                    System.err.println("Gagal memuat gambar: WallpaperHome.jpg tidak ditemukan di classpath.");
                    // Fallback: Set warna background jika gambar gagal
                    setBackground(new Color(240, 240, 240)); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Menggambar gambar agar memenuhi panel (stretch)
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // --- DEFINISI MENU UTAMA ---
        JMenu MenuDataMaster = new JMenu("Master Data");
        JMenu MenuAktifitas = new JMenu("Aktifitas");
        JMenu MenuLaporan = new JMenu("Laporan");
        JMenu MenuCetak = new JMenu("Cetak");
        JMenu MenuAudit = new JMenu("Audit Perpustakaan");
        JMenu MenuAdministrasi = new JMenu("Administrasi"); 
        JMenu MenuPengaturan = new JMenu("Pengaturan");       

        // ======================= 1. MENU MASTER DATA =======================
        JMenuItem itemAnggota = new JMenuItem("Anggota");
        JMenuItem itemBuku = new JMenuItem("Buku");
        JMenuItem itemKategori = new JMenuItem("Kategori");
        JMenuItem itemPenerbit = new JMenuItem("Penerbit");
        JMenuItem itemPengarang = new JMenuItem("Pengarang");
        JMenuItem itemRak = new JMenuItem("Rak Lemari");
        
        // Listeners Master Data
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

        // ======================= 2. MENU AKTIFITAS =======================
        JMenuItem itemPeminjaman = new JMenuItem("Peminjaman Buku");
        JMenuItem itemPeminjamanDetail = new JMenuItem("Peminjaman Buku Detail");
        JMenuItem itemPengembalian = new JMenuItem("Pengembalian");
        JMenuItem itemReservasi = new JMenuItem("Reservasi");
        
        // Listeners Aktifitas
        itemPeminjaman.addActionListener(e -> {
            Peminjaman f = new Peminjaman();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
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
        
        // ======================= 3. MENU LAPORAN =======================
        JMenuItem itemLaporanBulan = new JMenuItem("Laporan Peminjaman Buku");
        JMenuItem itemLaporanPengembalian = new JMenuItem("Laporan Pengembalian Buku");
        JMenuItem itemLaporanBuku = new JMenuItem("Laporan Buku");
        JMenuItem itemLaporanAuditBuku = new JMenuItem("Laporan Audit Buku");
        JMenuItem itemLaporanDenda = new JMenuItem("Laporan Denda");
        
        // Listeners Laporan (Placeholder)
        itemLaporanBulan.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Laporan belum tersedia."));
        
        MenuLaporan.add(itemLaporanBulan);
        MenuLaporan.add(itemLaporanPengembalian);
        MenuLaporan.add(itemLaporanBuku);
        MenuLaporan.add(itemLaporanAuditBuku);
        MenuLaporan.add(itemLaporanDenda);
        
        // ======================= 4. MENU CETAK =======================
        JMenuItem itemCetak = new JMenuItem("Cetak Kartu");
        itemCetak.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Cetak belum tersedia."));
        MenuCetak.add(itemCetak);
        
        // ======================= 5. MENU AUDIT PERPUSTAKAAN =======================
        JMenuItem itemAuditBuku = new JMenuItem("Audit Pengembalian Buku");
        JMenuItem itemAuditUser = new JMenuItem("Audit User");
        JMenuItem itemAuditProperti = new JMenuItem("Audit Properti");
        
        itemAuditBuku.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Audit belum tersedia."));
        
        MenuAudit.add(itemAuditBuku);
        MenuAudit.add(itemAuditUser);
        MenuAudit.add(itemAuditProperti);
        
        // ======================= 6. MENU ADMINISTRASI =======================
        JMenuItem itemAdministrasiAnggota = new JMenuItem("Administrasi Anggota");
        JMenuItem itemAdministrasiUser = new JMenuItem("Administrasi User");
        
        // Listeners Administrasi
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
        
        // ======================= 7. MENU PENGATURAN =======================
        JMenuItem itemPengaturanTema = new JMenuItem("Tema Windows");
        JMenuItem itemPengaturanBackup = new JMenuItem("Backup Data");
        JMenuItem itemLogout = new JMenuItem("Logout");
        JMenuItem itemExit = new JMenuItem("Keluar Aplikasi");
        
        itemPengaturanTema.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Tema belum tersedia."));
        
        // Listeners Pengaturan
        itemLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginForm().setVisible(true);
            }
        });
        itemExit.addActionListener(e -> System.exit(0));

        MenuPengaturan.add(itemPengaturanTema);
        MenuPengaturan.add(itemPengaturanBackup);
        MenuPengaturan.addSeparator();
        MenuPengaturan.add(itemLogout);
        MenuPengaturan.add(itemExit);

        // --- MEMASUKKAN SEMUA MENU KE BAR ---
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
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}