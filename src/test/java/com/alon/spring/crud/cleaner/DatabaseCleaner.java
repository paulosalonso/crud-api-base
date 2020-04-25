package com.alon.spring.crud.cleaner;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private DataSource dataSource;

	private Connection connection;

	public void clearTables() {
		try (Connection connection = dataSource.getConnection()) {
			this.connection = connection;
			checkAndClear();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			this.connection = null;
		}
	}

	private void checkAndClear() throws SQLException {
		checkTestDatabase();
		
		Statement statement = buildSqlStatement();

		LOGGER.debug("Executing SQL");
		
		statement.executeBatch();
	}
	
	private void checkTestDatabase() throws SQLException {
		String catalog = connection.getCatalog();

		if (catalog == null || !catalog.equals("TESTDB")) {
			throw new RuntimeException("Cannot clear database tables because '" 
					+ catalog + "' is not a test database (called TESTDB).");
		}
	}

	private Statement buildSqlStatement() {
		try {
			List<String> tablesNames = getTablesNames();
			
			Statement statement = connection.createStatement();
	
			statement.addBatch(logSql("SET FOREIGN_KEY_CHECKS = 0"));
			addTruncateSatements(statement, tablesNames);
			statement.addBatch(logSql("SET FOREIGN_KEY_CHECKS = 1"));
	
			return statement;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> getTablesNames() throws SQLException {
		List<String> tableNames = new ArrayList<>();

		DatabaseMetaData metaData = connection.getMetaData();
		ResultSet rs = metaData.getTables(
				connection.getCatalog(), null, null, new String[] { "TABLE" });

		while (rs.next()) 
			tableNames.add(rs.getString("TABLE_NAME"));

		tableNames.remove("flyway_schema_history");

		return tableNames;
	}
	
	private void addTruncateSatements(Statement statement, List<String> tableNames) {
		tableNames.forEach(tableName -> 
				addBatchCommand(statement, "TRUNCATE TABLE " + tableName));
	}
	
	private Statement addBatchCommand(Statement statement, String command) {
		try {	
			statement.addBatch(logSql(command));
			return statement;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private String logSql(String sql) {
		LOGGER.debug("Adding SQL: {}", sql);
		return sql;
	}
	
}