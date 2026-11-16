package qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SinhVienForm extends JFrame {
    JTextField txtMa = new JTextField(), txtTen = new JTextField(),
            txtNgay = new JTextField(), txtDiaChi = new JTextField(), txtTim = new JTextField(15);
    JComboBox<String> cboGT = new JComboBox<>(new String[]{"Nam", "Nữ"});
    JComboBox<String> cboLop = new JComboBox<>();
    DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SV", "Họ tên", "Ngày sinh", "Giới tính", "Địa chỉ", "Mã lớp"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model);

    public SinhVienForm() {
        setTitle("Quản lý Sinh viên");
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnTim = new JButton("Tìm");
        top.add(new JLabel("Tìm theo tên / mã SV:")); top.add(txtTim); top.add(btnTim);

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.add(new JLabel("Mã SV:")); form.add(txtMa);
        form.add(new JLabel("Họ tên:")); form.add(txtTen);
        form.add(new JLabel("Ngày sinh (yyyy-MM-dd):")); form.add(txtNgay);
        form.add(new JLabel("Giới tính:")); form.add(cboGT);
        form.add(new JLabel("Địa chỉ:")); form.add(txtDiaChi);
        form.add(new JLabel("Lớp:")); form.add(cboLop);

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
                txtMa.setText(str(r, 0)); txtTen.setText(str(r, 1)); txtNgay.setText(str(r, 2));
                cboGT.setSelectedItem(str(r, 3)); txtDiaChi.setText(str(r, 4));
                cboLop.setSelectedItem(str(r, 5));
                txtMa.setEditable(false);
            }
        });

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnMoi.addActionListener(e -> lamMoi());
        btnTim.addActionListener(e -> load(txtTim.getText().trim()));

        loadLop();
        load("");
    }

    String str(int r, int c) {
        Object o = model.getValueAt(r, c);
        return o == null ? "" : o.toString();
    }

    void loadLop() {
        cboLop.removeAllItems();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT MaLop FROM Lop")) {
            while (rs.next()) cboLop.addItem(rs.getString(1));
        } catch (SQLException ex) { loi(ex); }
    }

    void load(String kw) {
        model.setRowCount(0);
        String sql = "SELECT MaSV, HoTen, NgaySinh, GioiTinh, DiaChi, MaLop FROM SinhVien "
                + "WHERE HoTen LIKE ? OR MaSV LIKE ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getDate(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)});
        } catch (SQLException ex) { loi(ex); }
    }

    boolean hopLe() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập mã SV và họ tên!"); return false;
        }
        try {
            if (!txtNgay.getText().trim().isEmpty()) Date.valueOf(txtNgay.getText().trim());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Ngày sinh phải đúng dạng yyyy-MM-dd (VD: 2005-03-15)!");
            return false;
        }
        return true;
    }

    void setParams(PreparedStatement ps, boolean insert) throws SQLException {
        int i = 1;
        if (insert) ps.setString(i++, txtMa.getText().trim());
        ps.setString(i++, txtTen.getText().trim());
        String ns = txtNgay.getText().trim();
        if (ns.isEmpty()) ps.setNull(i++, Types.DATE); else ps.setDate(i++, Date.valueOf(ns));
        ps.setString(i++, (String) cboGT.getSelectedItem());
        ps.setString(i++, txtDiaChi.getText().trim());
        ps.setString(i++, (String) cboLop.getSelectedItem());
        if (!insert) ps.setString(i, txtMa.getText().trim());
    }

    void them() {
        if (!hopLe()) return;
        String sql = "INSERT INTO SinhVien(MaSV, HoTen, NgaySinh, GioiTinh, DiaChi, MaLop) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, true);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void sua() {
        if (table.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa!"); return; }
        if (!hopLe()) return;
        String sql = "UPDATE SinhVien SET HoTen=?, NgaySinh=?, GioiTinh=?, DiaChi=?, MaLop=? WHERE MaSV=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, false);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sửa thành công!");
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void xoa() {
        if (table.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Chọn 1 dòng để xóa!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa sinh viên này (và điểm của sinh viên)?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement p1 = c.prepareStatement("DELETE FROM Diem WHERE MaSV=?")) {
                p1.setString(1, txtMa.getText().trim()); p1.executeUpdate();
            }
            try (PreparedStatement p2 = c.prepareStatement("DELETE FROM SinhVien WHERE MaSV=?")) {
                p2.setString(1, txtMa.getText().trim()); p2.executeUpdate();
            }
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void lamMoi() {
        txtMa.setText(""); txtTen.setText(""); txtNgay.setText(""); txtDiaChi.setText("");
        txtMa.setEditable(true);
        table.clearSelection();
        loadLop();
        load("");
    }

    void loi(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
    }
}
