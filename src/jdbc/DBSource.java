// https://github.com/JustinSDK/JavaSE6Tutorial/blob/master/docs/CH20.md
package jdbc;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBSource {
	private Properties props;
	private String url;
	private String user;
	private String passwd;
	private int poolmax; // 連接池中最大Connection數目
	private List<Connection> connectionsPool;

	public DBSource() throws IOException, ClassNotFoundException {
		this("jdbc_tw-patent.properties");
	}

	public DBSource(String configFile) throws IOException, ClassNotFoundException {
		props = new Properties();
		props.load(new FileInputStream(configFile));

		url = props.getProperty("jdbc.mysql.url");
		user = props.getProperty("jdbc.mysql.user");
		passwd = props.getProperty("jdbc.mysql.password");
		poolmax = Integer.parseInt(props.getProperty("jdbc.mysql.poolmax"));

		Class.forName(props.getProperty("jdbc.mysql.driver"));
		connectionsPool = new ArrayList<Connection>();
	}

	public synchronized Connection getConnection() throws SQLException {
		if (connectionsPool.size() == 0)
			return DriverManager.getConnection(url, user, passwd);
		else {
			int lastIndex = connectionsPool.size() - 1;
			return connectionsPool.remove(lastIndex);
		}
	}

	public synchronized void closeConnection(Connection conn) throws SQLException {
		if (connectionsPool.size() == poolmax) conn.close();
		else connectionsPool.add(conn);
	}
}