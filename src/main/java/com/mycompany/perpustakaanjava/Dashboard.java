/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.perpustakaanjava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Dashboard extends JFrame {

    public Dashboard() {
        // 1. Pengaturan Window Otomatis Full Screen (Maximized)
        setTitle("Dashboard Utama");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 2. Inisialisasi Menu Bar
        initMenuBar();

        // 3. Konten Utama (Placeholder)
        JPanel mainContent = new JPanel();
        mainContent.setBackground(new Color(240, 240, 240));
        mainContent.add(new JLabel("Selamat Datang di Dashboard"));
        add(mainContent, BorderLayout.CENTER);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Membuat Menu Utama
        JMenu MenuDataMaster = new JMenu("Master Data");
        
        // Menu Kedua
        JMenu MenuAktifitas = new JMenu("Aktifitas");
        
        // Menu Ketiga
        JMenu MenuLaporan = new JMenu("Laporan");
        
        // Menu Keempat
        JMenu MenuCetak = new JMenu("Cetak");
        
        // Menu Kelima
        JMenu MenuAudit = new JMenu("Audit Perpustakaan");
        
        // Menu Keenam
        JMenu MenuAdminitrasi = new JMenu("Adminitrasi");
        
        // Menu Ketujuh
        JMenu MenuPengaturan = new JMenu("Pengaturan");       

        // Membuat Item untuk Menu Master Data
        JMenuItem itemBuku = new JMenuItem("Buku");
        JMenuItem itemKategori = new JMenuItem("Kategori");
        JMenuItem itemPenerbit = new JMenuItem("Penerbit");
        JMenuItem itemPengarang = new JMenuItem("Pengarang");
        JMenuItem itemRak = new JMenuItem("Rak Lemari");
        
        // Membuat Item untuk menu Aktifitas
        JMenuItem itemPeminjaman = new JMenuItem("Peminjaman Buku");
        JMenuItem itemPeminjamanDetail = new JMenuItem("Peminjaman Buku Detail");
        JMenuItem itemPengembalian = new JMenuItem("Pengembalian");
        JMenuItem Reservasi = new JMenuItem("Reservasi");

        
        // Membuat Item untuk menu Laporan
        JMenuItem itemLaporanBulan = new JMenuItem("Peminjaman Buku");
        JMenuItem itemLaporanPengembalian = new JMenuItem("Pengembalian Buku");
        JMenuItem itemLaporanBuku = new JMenuItem("Buku");
        JMenuItem itemLaporanAuditBuku = new JMenuItem("Audit Buku");
        JMenuItem itemLaporanDenda = new JMenuItem("Audit Buku");
        
        // Membuat Item untuk cetak
        JMenuItem itemCetak = new JMenuItem("Peminjaman Cetak");
        
        // Membuat Item untuk menu audit perpustakaan
        JMenuItem itemAuditBuku = new JMenuItem("Pengembalian Buku");
        JMenuItem itemAuditUser = new JMenuItem("Buku");
        JMenuItem itemAuditProperti = new JMenuItem("Audit Buku");
        
        // Membuat Item untuk adminitrasi user dan anggota
        JMenuItem itemAdmnitrasiAnggota = new JMenuItem("Pengembalian Buku");
        JMenuItem itemAdmnitrasiUser = new JMenuItem("Buku");
       
        
        // Membuat menu pengaturan
        JMenuItem itemPengaturanTema = new JMenuItem("Tema Windows");
        JMenuItem itemPengaturanBackup = new JMenuItem("Backup Data");

        
        // Event Listener: Buka Kategori
        itemKategori.addActionListener(e -> {
            Kategori frameKategori = new Kategori();
            frameKategori.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Agar tidak menutup seluruh aplikasi
            frameKategori.setVisible(true);
        });


        // Menyusun hirarki menu
        MenuDataMaster.add(itemBuku);
        MenuDataMaster.add(itemKategori);
        MenuDataMaster.add(itemPenerbit);
        MenuDataMaster.add(itemPengarang);
        MenuDataMaster.add(itemRak);


        // Menyusun hirarki aktifitas
        MenuAktifitas.add(itemPeminjaman); 
        MenuAktifitas.add(itemPeminjamanDetail); 
        MenuAktifitas.add(itemPengembalian); 
        MenuAktifitas.add(Reservasi); 
        
        
        // Menyusun hirarki laporan
        MenuLaporan.add(itemLaporanBulan);
        MenuLaporan.add(itemLaporanPengembalian);
        MenuLaporan.add(itemLaporanBuku);
        MenuLaporan.add(itemLaporanAuditBuku);


        // Memasukkan menu ke bar
        menuBar.add(MenuDataMaster);
        menuBar.add(MenuAktifitas);
        menuBar.add(MenuLaporan);
        menuBar.add(MenuAudit);

        // Memasang Menu Bar ke Dashboard
        setJMenuBar(menuBar);
    }

    
    // --- Main Method ---
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                new Dashboard().setVisible(true);
            });
        }

}