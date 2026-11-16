package qlsv;

import java.sql.*;

public class DBConnection {
    // ĐỔI mật khẩu root MySQL của bạn ở đây
    private static final String URL = "jdbc:mysql://localhost:3306/QLSinhVien"
            + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh";
    private static final String USER = "root";
    private static final String PASS = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
