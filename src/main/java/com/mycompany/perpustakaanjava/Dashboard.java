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
        JMenu menuFile = new JMenu("File");
        JMenu menuData = new JMenu("Data");
        JMenu menuLaporan = new JMenu("Laporan");
        JMenu menuPengaturan = new JMenu("Pengaturan");

        // Membuat Item untuk Menu File
        JMenuItem itemProfil = new JMenuItem("Profil User");
        JMenuItem itemLogout = new JMenuItem("Logout");
        JMenuItem itemExit = new JMenuItem("Keluar");

        // Menambahkan Shortcut Keyboard (Alt + F4 untuk Keluar)
        itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));

        // Event Listener (Logika klik)
        itemExit.addActionListener(e -> System.exit(0));

        
        // Logika Menu Items
        JMenuItem itemKategori = new JMenuItem("Data Kategori");
        
        // Event Listener: Buka Kategori
        itemKategori.addActionListener(e -> {
            Kategori frameKategori = new Kategori();
            frameKategori.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Agar tidak menutup seluruh aplikasi
            frameKategori.setVisible(true);
        });

        // Event Listener: Logout
        itemLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginForm().setVisible(true);
            }
        });

        // Menyusun hirarki menu
        menuFile.add(itemProfil);
        menuFile.add(itemLogout);
        menuFile.addSeparator(); // Garis pemisah
        menuFile.add(itemExit);
        
        menuData.add(itemKategori); // Tambahkan item Kategori ke menu Data


        // Memasukkan menu ke bar
        menuBar.add(menuFile);
        menuBar.add(menuData);
        menuBar.add(menuLaporan);
        menuBar.add(menuPengaturan);

        // Memasang Menu Bar ke Dashboard
        setJMenuBar(menuBar);
    }


}