package jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Mysql_Select {
	void select(ResultSet rs) throws SQLException;
}
