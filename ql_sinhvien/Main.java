package qlsv;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public Main() {
        setTitle("QUẢN LÝ SINH VIÊN");
        setSize(380, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("QUẢN LÝ SINH VIÊN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton btnSV = new JButton("Quản lý Sinh viên");
        JButton btnLop = new JButton("Quản lý Lớp");
        JButton btnDiem = new JButton("Quản lý Điểm");

        btnSV.addActionListener(e -> new SinhVienForm().setVisible(true));
        btnLop.addActionListener(e -> new LopForm().setVisible(true));
        btnDiem.addActionListener(e -> new DiemForm().setVisible(true));

        JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));
        p.add(btnSV); p.add(btnLop); p.add(btnDiem);

        add(title, BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
