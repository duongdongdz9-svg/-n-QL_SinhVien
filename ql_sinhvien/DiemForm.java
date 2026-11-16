package qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DiemForm extends JFrame {
    JComboBox<String> cboSV = new JComboBox<>();
    JComboBox<String> cboMon = new JComboBox<>();
    JTextField txtDiem = new JTextField(), txtTim = new JTextField(15);
    DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SV", "Họ tên", "Mã môn", "Tên môn", "Điểm"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model);

    public DiemForm() {
        setTitle("Quản lý Điểm");
        setSize(700, 520);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnTim = new JButton("Tìm");
        top.add(new JLabel("Tìm theo tên SV:")); top.add(txtTim); top.add(btnTim);

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Sinh viên:")); form.add(cboSV);
        form.add(new JLabel("Môn học:")); form.add(cboMon);
        form.add(new JLabel("Điểm (0-10):")); form.add(txtDiem);

        JButton btnThem = new JButton("Thêm"), btnSua = new JButton("Sửa"),
                btnXoa = new JButton("Xóa"), btnMoi = new JButton("Làm mới");
        JPanel btns = new JPanel();
        btns.add(btnThem); btns.add(btnSua); btns.add(btnXoa); btns.add(btnMoi);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottom.add(form, BorderLayout.CENTER);
        bottom.add(btns, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0 && !e.getValueIsAdjusting()) {
                cboSV.setSelectedItem(model.getValueAt(r, 0) + " - " + model.getValueAt(r, 1));
                cboMon.setSelectedItem(model.getValueAt(r, 2) + " - " + model.getValueAt(r, 3));
                txtDiem.setText(String.valueOf(model.getValueAt(r, 4)));
                cboSV.setEnabled(false); cboMon.setEnabled(false); // khóa khóa chính khi sửa
            }
        });

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnMoi.addActionListener(e -> lamMoi());
        btnTim.addActionListener(e -> load(txtTim.getText().trim()));

        loadCombo();
        load("");
    }

    String maSV() { return ((String) cboSV.getSelectedItem()).split(" - ")[0]; }
    String maMon() { return ((String) cboMon.getSelectedItem()).split(" - ")[0]; }

    void loadCombo() {
        cboSV.removeAllItems(); cboMon.removeAllItems();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT MaSV, HoTen FROM SinhVien");
            while (rs.next()) cboSV.addItem(rs.getString(1) + " - " + rs.getString(2));
            rs = st.executeQuery("SELECT MaMon, TenMon FROM MonHoc");
            while (rs.next()) cboMon.addItem(rs.getString(1) + " - " + rs.getString(2));
        } catch (SQLException ex) { loi(ex); }
    }

    void load(String kw) {
        model.setRowCount(0);
        String sql = "SELECT d.MaSV, s.HoTen, d.MaMon, m.TenMon, d.Diem FROM Diem d "
                + "JOIN SinhVien s ON d.MaSV=s.MaSV JOIN MonHoc m ON d.MaMon=m.MaMon "
                + "WHERE s.HoTen LIKE ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5)});
        } catch (SQLException ex) { loi(ex); }
    }

    Double docDiem() {
        try {
            double d = Double.parseDouble(txtDiem.getText().trim());
            if (d < 0 || d > 10) throw new NumberFormatException();
            return d;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số từ 0 đến 10!");
            return null;
        }
    }

    void them() {
        if (cboSV.getSelectedItem() == null || cboMon.getSelectedItem() == null) return;
        Double d = docDiem(); if (d == null) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO Diem(MaSV, MaMon, Diem) VALUES(?,?,?)")) {
            ps.setString(1, maSV()); ps.setString(2, maMon()); ps.setDouble(3, d);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void sua() {
        if (table.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa!"); return; }
        Double d = docDiem(); if (d == null) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Diem SET Diem=? WHERE MaSV=? AND MaMon=?")) {
            ps.setDouble(1, d); ps.setString(2, maSV()); ps.setString(3, maMon());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sửa thành công!");
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void xoa() {
        if (table.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Chọn 1 dòng để xóa!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa điểm này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM Diem WHERE MaSV=? AND MaMon=?")) {
            ps.setString(1, maSV()); ps.setString(2, maMon());
            ps.executeUpdate();
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void lamMoi() {
        txtDiem.setText("");
        cboSV.setEnabled(true); cboMon.setEnabled(true);
        table.clearSelection();
        loadCombo();
        load("");
    }

    void loi(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
    }
}
