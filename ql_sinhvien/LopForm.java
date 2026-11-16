package qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LopForm extends JFrame {
    JTextField txtMa = new JTextField(), txtTen = new JTextField(),
            txtKhoa = new JTextField(), txtTim = new JTextField(15);
    DefaultTableModel model = new DefaultTableModel(new String[]{"Mã lớp", "Tên lớp", "Khoa"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model);

    public LopForm() {
        setTitle("Quản lý Lớp");
        setSize(650, 520);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnTim = new JButton("Tìm");
        top.add(new JLabel("Tìm theo tên lớp:")); top.add(txtTim); top.add(btnTim);

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Mã lớp:")); form.add(txtMa);
        form.add(new JLabel("Tên lớp:")); form.add(txtTen);
        form.add(new JLabel("Khoa:")); form.add(txtKhoa);

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

        // Click vào dòng -> đổ dữ liệu lên ô nhập
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0 && !e.getValueIsAdjusting()) {
                txtMa.setText(model.getValueAt(r, 0).toString());
                txtTen.setText(model.getValueAt(r, 1).toString());
                Object k = model.getValueAt(r, 2);
                txtKhoa.setText(k == null ? "" : k.toString());
                txtMa.setEditable(false);
            }
        });

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnMoi.addActionListener(e -> lamMoi());
        btnTim.addActionListener(e -> load(txtTim.getText().trim()));

        load("");
    }

    void load(String kw) {
        model.setRowCount(0);
        String sql = "SELECT MaLop, TenLop, Khoa FROM Lop WHERE TenLop LIKE ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
        } catch (SQLException ex) { loi(ex); }
    }

    void them() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập mã lớp và tên lớp!"); return;
        }
        String sql = "INSERT INTO Lop(MaLop, TenLop, Khoa) VALUES(?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txtMa.getText().trim());
            ps.setString(2, txtTen.getText().trim());
            ps.setString(3, txtKhoa.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void sua() {
        if (table.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa!"); return; }
        String sql = "UPDATE Lop SET TenLop=?, Khoa=? WHERE MaLop=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txtTen.getText().trim());
            ps.setString(2, txtKhoa.getText().trim());
            ps.setString(3, txtMa.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sửa thành công!");
            lamMoi();
        } catch (SQLException ex) { loi(ex); }
    }

    void xoa() {
        if (table.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Chọn 1 dòng để xóa!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa lớp này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM Lop WHERE MaLop=?")) {
            ps.setString(1, txtMa.getText().trim());
            ps.executeUpdate();
            lamMoi();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không xóa được (lớp đang có sinh viên?)\n" + ex.getMessage());
        }
    }

    void lamMoi() {
        txtMa.setText(""); txtTen.setText(""); txtKhoa.setText("");
        txtMa.setEditable(true);
        table.clearSelection();
        load("");
    }

    void loi(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
    }
}
