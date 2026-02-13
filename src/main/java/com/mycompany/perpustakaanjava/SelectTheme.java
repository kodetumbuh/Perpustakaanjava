package com.mycompany.perpustakaanjava;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

public class SelectTheme extends JFrame {

    private JComboBox<UIManager.LookAndFeelInfo> cmbThemes;
    private JButton btnApply, btnCancel;

    public SelectTheme() {
        setTitle("Pilih Tema Windows");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20", "[][grow]", "[]20[]"));

        // Initialize Components
        JLabel lblTheme = new JLabel("Pilih Tema:");
        
        // Get Installed LookAndFeels
        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        cmbThemes = new JComboBox<>(looks);
        
        // Custom Renderer to show only the Name (not the class name)
        cmbThemes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UIManager.LookAndFeelInfo) {
                    setText(((UIManager.LookAndFeelInfo) value).getName());
                }
                return this;
            }
        });

        // Set current selection based on current LookAndFeel
        String currentClass = UIManager.getLookAndFeel().getClass().getName();
        for (int i = 0; i < looks.length; i++) {
            if (looks[i].getClassName().equals(currentClass)) {
                cmbThemes.setSelectedIndex(i);
                break;
            }
        }

        btnApply = new JButton("Simpan & Terapkan");
        btnCancel = new JButton("Batal");

        // Layout
        add(lblTheme, "gapright 10");
        add(cmbThemes, "growx, wrap");
        add(btnApply, "span, split 2, align center, w 150!");
        add(btnCancel, "w 100!");

        // Action Listeners
        btnApply.addActionListener(this::applyTheme);
        btnCancel.addActionListener(e -> dispose());
    }

    private void applyTheme(ActionEvent e) {
        UIManager.LookAndFeelInfo selected = (UIManager.LookAndFeelInfo) cmbThemes.getSelectedItem();
        if (selected == null) return;

        try {
            // 1. Set Look and Feel
            UIManager.setLookAndFeel(selected.getClassName());

            // 2. Update all open windows
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                window.revalidate(); // Ensure layout is recalculated
            }

            // 3. Save Preference
            Preferences prefs = Preferences.userNodeForPackage(SelectTheme.class);
            prefs.put("AppTheme", selected.getClassName());
            prefs.flush(); // Ensure data is persisted immediately

            JOptionPane.showMessageDialog(this, "Tema berhasil diubah dan disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengubah tema: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SelectTheme().setVisible(true));
    }
}
