/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.perpustakaanjava;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EmployeeManagementUI extends JFrame {

    public EmployeeManagementUI() {
        setTitle("Employee Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Panel Utama dengan MigLayout
        // "fill" agar komponen memenuhi ruang, "ins 20" untuk padding luar
        JPanel mainPanel = new JPanel(new MigLayout("fillx, ins 20", "[grow][pref!][pref!]", "[]10[]10[grow]10[]"));

        // --- BARIS 1: Header (Search & Add) ---
        JLabel lblSearch = new JLabel("Search:");
        JTextField txtSearch = new JTextField();
        JButton btnSearch = new JButton("Search");
        JButton btnAdd = new JButton("Add New Employee");

        mainPanel.add(lblSearch, "split 2"); // split 2 menggabungkan label dan textfield di satu sel
        mainPanel.add(txtSearch, "growx, pushx");
        mainPanel.add(btnSearch, "w 100!");
        mainPanel.add(btnAdd, "w 150!, gapleft 20, wrap");

        // --- BARIS 2 & 3: Tabel ---
        String[] columnNames = {"ID", "Name", "Email", "Department"};
        Object[][] data = {
            {"1", "John Doe", "john@example.com", "Human Resources"},
            {"2", "Jane Doe", "jane@example.com", "IT"},
            {"4", "Bob Brown", "bob@example.com", "IT"},
            {"5", "Charlie Davis", "charlie@example.com", "Marketing"},
            {"6", "Eva White", "eva@example.com", "Sales"},
            {"7", "Frank Miller", "frank@example.com", "Finance"},
            {"8", "Grace Wilson", "grace@example.com", "Human Resources"},
            {"9", "Henry Moore", "henry@example.com", "Finance"},
            {"11", "Jack Anderson", "jack@example.com", "Sales"},
            {"12", "Kelly Thomas", "kelly@example.com", "IT"},
            {"13", "Liam Haling", "liam@example.com", "Sales"},
            {"14", "Mia Robinson", "mia@example.com", "Finance"},
            {"15", "Noah Clark", "noah@example.com", "Marketing"},
            {"16", "Olivia Rodriguez", "olivia@example.com", "Sales"},
            {"17", "User Mu", "user2@example.com", "IT"}
            // Tambahkan data lainnya sesuai kebutuhan
        };

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        
        mainPanel.add(scrollPane, "span, grow, wrap"); // span mengambil semua kolom

        // --- BARIS 4: Footer (Pagination) ---
        JPanel footerPanel = new JPanel(new MigLayout("ins 0", "[left][grow][right]"));
        
        // Rows per page
        JLabel lblRows = new JLabel("Rows per page:");
        JComboBox<Integer> comboRows = new JComboBox<>(new Integer[]{10, 25, 50, 100});
        comboRows.setSelectedItem(25);
        
        // Page info
        JLabel lblPageInfo = new JLabel("Page 1 of 3 (Total: 66)");
        
        // Navigation buttons
        JButton btnPrev = new JButton("Previous");
        btnPrev.setEnabled(false);
        JButton btnNext = new JButton("Next");

        footerPanel.add(lblRows, "split 2");
        footerPanel.add(comboRows);
        footerPanel.add(lblPageInfo, "center, pushx");
        footerPanel.add(btnPrev, "split 2, w 100!");
        footerPanel.add(btnNext, "w 100!");

        mainPanel.add(footerPanel, "span, growx");

        add(mainPanel);
    }

}