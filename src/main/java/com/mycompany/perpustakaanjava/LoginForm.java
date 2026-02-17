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
        setLocationRelativeTo(null);
        
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
        btnLogin.addActionListener(e -> performLogin(txtUsername.getText(), new String(txtPassword.getPassword())));
        
        // Enter key on password field triggers login
        txtPassword.addActionListener(e -> performLogin(txtUsername.getText(), new String(txtPassword.getPassword())));

        btnBatal.addActionListener(e -> System.exit(0));

        add(btnLogin, "span 2, split 2, align left, gaptop 10");
        add(btnBatal);
    }

    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String hashedPassword = getMd5(password);
            
            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM user WHERE username = ? AND password = ?")) {
                
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword); // Use hashed password

                try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // Login Sukses
                        this.dispose(); 
                        new Dashboard().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getMd5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            java.math.BigInteger no = new java.math.BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
