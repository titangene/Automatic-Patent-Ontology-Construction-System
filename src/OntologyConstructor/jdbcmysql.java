package OntologyConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class jdbcmysql {
	public Connection con = null; // Database objects
	public Statement stat = null; // 執行,傳入之sql為完整字串
	public ResultSet rs = null; // 結果集
	// 執行,傳入之sql為預儲之字申,需要傳入變數之位置 。先利用?來做標示
	private PreparedStatement pst = null;
	
	public jdbcmysql() {
		try {
			// 註冊driver
			Class.forName("com.mysql.jdbc.Driver");
			// 取得connection
			con = DriverManager.getConnection("jdbc:mysql://localhost/tw_patent", "root", "titan");
			System.out.println("MySQL Connection");
		} catch (ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		} catch (SQLException x) {
			System.out.println("Exception :" + x.toString());
		}
	}
	
	// 查詢資料
	// 可以看看回傳結果集及取得資料方式
	public void SelectTable() {
		String selectSQL = "select * from crawler";
		try {
			stat = con.createStatement();
			rs = stat.executeQuery(selectSQL);
			int count = 0;
			while (rs.next()) {
				System.out.println(rs.getString("id") + "\t" + rs.getString("name"));
				count++;
				if (count == 3) break;
			}
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	// 完整使用完資料庫後,記得要關閉所有Object
	// 否則在等待Timeout時,可能會有Connection poor的狀況
	public void Close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stat != null) {
				stat.close();
				stat = null;
			}
			if (pst != null) {
				pst.close();
				pst = null;
			}
			con.close();
		} catch (SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}
}
