/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.perpustakaanjava;

import javax.swing.*;
import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author ZEMS
 */
public class LoginForm extends JFrame {
    
    public LoginForm() {
        setTitle("Login Perpustakaan");
        setSize(400, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLayout(new MigLayout("wrap 2, insets 20", "[right][grow]"));
        
        add(new JLabel("Username :"));
        JTextField txtUsername = new JTextField();
        add(txtUsername, "pushx, growx"); // pushx & growx agar field memenuhi lebar

        add(new JLabel("Password :"));
        JPasswordField txtPassword = new JPasswordField();
        add(txtPassword, "pushx, growx");
        
        JButton btnLogin = new JButton("Login");
        JButton btnBatal = new JButton("Batal");
        
        // Menambahkan aksi klik pada tombol Login
        btnLogin.addActionListener(e -> {
            // 1. Menutup atau menyembunyikan form Login saat ini
            this.dispose(); 

            // 2. Memanggil dan menampilkan form EmployeeManagement
            Dashboard empForm = new Dashboard();
            empForm.setVisible(true);
        });
        
        add(btnLogin, "span 2, split 2, align left, gaptop 10");
        add(btnBatal);
        
        
    }
}
