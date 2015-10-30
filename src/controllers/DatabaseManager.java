package controllers;

import dataManagement.User;
import java.sql.*;

public class DatabaseManager {
	
	private String username = "cs307@purdue.edu";
	private String userPassword = "12345678";
	
	private static Connection connectToDatabase() {
		Connection connection = null;
		// Establish connection to the existing database
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:vault_database");
			connection.setAutoCommit(false);
		} catch (Exception e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
			//e.printStackTrace();
		}
		return connection;
	}
	
	public int addUserToDatabase(User newUser) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();
			
			// Check that user does not already exist
			ResultSet results = stmt.executeQuery("SELECT count(*) FROM users WHERE user_email = "
					+ "'" + newUser.getUsername() + "';");
			if(results.getInt(1) != 0) {
				// user exists, return failure value
				return -1;
			}

			// Construct the SQL INSERT statement
			String sql =
		    		"INSERT INTO users (user_email, password_hash, password_salt, data_key, security_question, security_answer, "
		    		+ "last_login, high_security, account_wipe_set, backup_frequency, max_backup_size) "
		    		+ "VALUES ("
		    			+ "'" + newUser.getUsername() + "', "
		    			+ "'" + newUser.getPasswordHash() + "', "
		    			+ "'" + newUser.getPasswordSalt() + "', "
		    			+ "'" + newUser.getDataKey() + "', "
		    			+ "'" + newUser.getSecurityQuestion() + "', "
		    			+ "'" + newUser.getSecurityAnswer() + "', "
		    			+ "'" + newUser.getLastLogin().toString() + "', "
		    			+ newUser.isHighSecurity() + ", "
		    			+ newUser.isAccountWipeSet() + ", "
		    			+ "'" + newUser.getBackupFrequency() + "', "
		    			+ newUser.getMaxBackupSize()
		    		+ ");"
		    		;
			
			// Execute the statement and commit database changes
		    stmt.executeUpdate(sql);
		    DBconnection.commit();
		    
		    // Disconnect from database
		    stmt.close();
		    DBconnection.close();
		    
		    // return a success value
		    return 1;
		    
		} catch (SQLException e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}
	}
	
	public int deleteUserFromDatabase(User doomedUser) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();
			// Construct the SQL DELETE statement
			String sql = "DELETE FROM users WHERE user_email = "
					+ "'" + doomedUser.getUsername() + "';"
					;
			
			// Execute the statement and commit database changes
		    stmt.executeUpdate(sql);
		    DBconnection.commit();
		    
		    // Disconnect from database
		    stmt.close();
		    DBconnection.close();
			
			// return a success value
			return 1;
		} catch (SQLException e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}
	}
	
	public String retrievePassword(String userEmail) {
		if (userEmail.equals(username)) {
			return userPassword;
		}
		else {
			return "FAILED";
		}
	}
	
	public String retrieveUsername() {		
		return username;
	}

}
