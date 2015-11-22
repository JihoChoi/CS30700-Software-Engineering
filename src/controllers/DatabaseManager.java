package controllers;

import dataManagement.User;
import dataManagement.DataEntry;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cryptography.Crypto;

public class DatabaseManager {
	
	private static String database_name;
	
	public DatabaseManager(String database) {
		this.database_name = database;
	}

	/**
	 * Connects to the vault database and returns a Connection for two-way
	 * communication
	 * 
	 * @return active Connection to vault_database
	 */
	public static Connection connectToDatabase() {
		Connection connection = null;
		// Establish connection to the existing database
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + database_name);
			connection.setAutoCommit(false);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			// e.printStackTrace();
		}
		return connection;
	}
	
	/**
	 * Connects to a given database and returns a Connection for two-way
	 * communication
	 * 
	 * @param	database	name of database to connect to
	 * @return	active		Connection to vault_database
	 */
	public static Connection connectToDatabase(String database) {
		Connection connection = null;
		// Establish connection to the existing database
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + database);
			connection.setAutoCommit(false);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			// e.printStackTrace();
		}
		return connection;
	}
	
	/**
	 * Create the "users" table in a given database to set up for use with Sentinel Data Vault
	 * 
	 * @param database name of database to add a "users" table to
	 */
	public void createUsersTable(String database) {
		// Connect to the given database
		Connection DBconnection = connectToDatabase(database);
		try {
			// Construct SQLite statement
			Statement stmt = DBconnection.createStatement();
			String sql = "CREATE TABLE users ("
					+ "'user_email'			TEXT	NOT NULL	UNIQUE, "
					+ "'password_hash'		TEXT	NOT NULL, "
					+ "'password_salt'		TEXT, "
					+ "'data_key'			TEXT, "
					+ "'security_question'	TEXT, "
					+ "'security_answer'	TEXT, "
					+ "'last_login'			TEXT,"
					+ "'high_security'		INTEGER, "
					+ "'account_wipe_set'	INTEGER, "
					+ "'backup_frequency'	TEXT, "
					+ "'max_backup_size'	INTEGER, "
					+ "PRIMARY KEY(user_email))";
			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();
			// Disconnect from database
			stmt.close();
			DBconnection.close();
		}
		catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the "data_entries" table in a given database to set up for use with Sentinel Data Vault
	 * 
	 * @param database name of database to add a "data_entries" table to
	 */
	public void createDataEntriesTable(String database) {
		// Connect to the given database
		Connection DBconnection = connectToDatabase(database);
		try {
			// Construct SQLite statement
			Statement stmt = DBconnection.createStatement();
			String sql = "CREATE TABLE data_entries ("
					+ "'entry_name'		TEXT 	NOT NULL, "
					+ "'entry_type'		TEXT 	NOT NULL, "
					+ "'encryption_key'	TEXT, "
					+ "'owner'			TEXT 	NOT NULL, "
					+ "'valid_users'	TEXT, "
					+ "'secure_entry'	INTEGER, "
					+ "'last_modified'	TEXT, "
					+ "'data_field_1'	TEXT, "
					+ "'data_field_2'	TEXT, "
					+ "'data_field_3'	TEXT, "
					+ "'data_field_4'	TEXT, "
					+ "'data_field_5'	TEXT, "
					+ "'data_field_6'	TEXT, "
					+ "'data_field_7'	TEXT, "
					+ "'data_field_8'	TEXT, "
					+ "'data_field_9'	TEXT, "
					+ "'data_field_10'	TEXT)";
			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();
			// Disconnect from database
			stmt.close();
			DBconnection.close();
		}
		catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Adds a given user (account) to the vault database.
	 * 
	 * @param newUser
	 *            user object to add to the vault database
	 * @return positive integer if user successfully added; negative if user
	 *         already exists in the database
	 */
	public int addUserToDatabase(User newUser) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();

			// Check that user does not already exist
			ResultSet results = stmt.executeQuery(
					"SELECT count(*) FROM users WHERE user_email = " + "'" + newUser.getUsername() + "';");
			if (results.getInt(1) != 0) {
				// user exists, return failure value
				results.close();
				stmt.close();
				DBconnection.close();
				return -1;
			}

			// Construct the SQL INSERT statement
			String sql = "INSERT INTO users (user_email, password_hash, password_salt, data_key, security_question, security_answer, "
					+ "last_login, high_security, account_wipe_set, backup_frequency, max_backup_size) " + "VALUES ("
					+ "'" + newUser.getUsername() + "', " + "'" + newUser.getPasswordHash() + "', " + "'"
					+ newUser.getPasswordSalt() + "', " + "'" + newUser.getDataKey() + "', " + "'"
					+ newUser.getSecurityQuestion() + "', " + "'" + newUser.getSecurityAnswer() + "', " + "'"
					+ newUser.getLastLogin().toString() + "', " + newUser.isHighSecurity() + ", "
					+ newUser.isAccountWipeSet() + ", " + "'" + newUser.getBackupFrequency() + "', "
					+ newUser.getMaxBackupSize() + ");";

			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();

			// Disconnect from database
			stmt.close();
			DBconnection.close();

			// return a success value
			return 1;

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}
	}
	
	/**
	 * Deletes a given user from the vault database.
	 * 
	 * @param doomedUser
	 *            user object to delete from the vault database
	 * @return positive integer if user successfully deleted; negative if
	 *         unsuccessful
	 */
	public int deleteUserFromDatabase(User doomedUser) { // TODO Check for
															// success/failure
															// unnecessary
		// Connect to the database
		Connection DBconnection = connectToDatabase();

		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();
			// Construct the SQL DELETE statement
			String sql = "DELETE FROM users WHERE user_email = " + "'" + doomedUser.getUsername() + "';";

			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();

			// Disconnect from database
			stmt.close();
			DBconnection.close();

			// return a success value
			return 1;
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}
	}

	/**
	 * Retrieves a stored user from the vault database
	 * 
	 * @param userEmail
	 *            username (email) of user to be retrieved from the vault
	 *            database
	 * @return User object containing that user's stored data
	 */
	public User retrieveUserFromDatabase(String userEmail) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();

			// Construct the SQL DELETE statement
			String sql = "SELECT * FROM users WHERE user_email = " + "'" + userEmail + "';";

			// Execute the statement and commit database changes
			ResultSet userInfoSet = stmt.executeQuery(sql);
			if(userInfoSet.isClosed()) {
				userInfoSet.close();
				stmt.close();
				DBconnection.close();
				return null;
			}
			// DBconnection.commit();
			// while ( userInfoSet.next() ) {
			// String id = userInfoSet.getString("user_email");
			String passwordHash = userInfoSet.getString("password_hash");
			String salt = userInfoSet.getString("password_salt");
			String datakey = userInfoSet.getString("data_key");
			String question = userInfoSet.getString("security_question");
			String answer = userInfoSet.getString("security_answer");
			String lastLogin = userInfoSet.getString("last_login");
			LocalDateTime loginLDT = LocalDateTime.parse(lastLogin);

			int isHigh = userInfoSet.getInt("high_security");
			int wipeSet = userInfoSet.getInt("account_wipe_set");
			String backupFreq = userInfoSet.getString("backup_frequency");
			int size = userInfoSet.getInt("max_backup_size");

			// Reconstruct user
			User user = new User(userEmail, passwordHash, salt, datakey, question, answer, loginLDT);
			user.setDefaultHighSecurity(isHigh);
			user.setAccountWipe(wipeSet);
			user.setBackupFrequency(backupFreq);
			user.setMaxBackupSize(size);

			// Disconnect from database
			userInfoSet.close();
			stmt.close();
			DBconnection.close();

			// return a success value
			return user;
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			try {
				DBconnection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// return a failure value
			return null;
		}
	}

	/**
	 * Retrieve of List of all a user's data entry objects
	 * 
	 * @param userEmail	user to retrieve data entries from
	 * @param database	name of database to access
	 * @return			List<DataEntry> of all a user's data entries
	 */
	public List<DataEntry> retrieveUserDataEntries(String userEmail, String database) {
		// Connect to the database
		Connection DBconnection = connectToDatabase(database);
		// Create an empty List to populate with data entries
		List<DataEntry> dataEntryList = new ArrayList<DataEntry>();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();
			// Construct the SQL DELETE statement
			String sql = "SELECT * FROM data_entries WHERE owner = " + "'" + userEmail + "';";
			// Execute the statement and commit database changes
			ResultSet dataEntrySet = stmt.executeQuery(sql);
			while (dataEntrySet.next()) {
				// Retrieve all fields of the entry
				String entry_name = dataEntrySet.getString("entry_name");
				String entry_type = dataEntrySet.getString("entry_type");
				String encryption_key = dataEntrySet.getString("encryption_key");
				String owner = dataEntrySet.getString("owner");
				int secure_entry = dataEntrySet.getInt("secure_entry");
				LocalDateTime last_modified = LocalDateTime.parse(dataEntrySet.getString("last_modified"));
				
				// Parse the valid_users String and convert to List<String> to assign to validUsers field of DataEntry
				List<String> validUsers = new ArrayList<String>();
				String validUsersString = dataEntrySet.getString("valid_users");
				String[] parsedValidUsers = validUsersString.split(" ");
				for (int i = 0; i < parsedValidUsers.length; i++) {
					validUsers.add(parsedValidUsers[i]);
				}
				
				// Retrieve the data_fields; create a List of the fields
				List<String> data_field_list = new ArrayList<String>();
				String data_field_1 = dataEntrySet.getString("data_field_1");
				String data_field_2 = dataEntrySet.getString("data_field_2");
				String data_field_3 = dataEntrySet.getString("data_field_3");
				String data_field_4 = dataEntrySet.getString("data_field_4");
				String data_field_5 = dataEntrySet.getString("data_field_5");
				String data_field_6 = dataEntrySet.getString("data_field_6");
				String data_field_7 = dataEntrySet.getString("data_field_7");
				String data_field_8 = dataEntrySet.getString("data_field_8");
				String data_field_9 = dataEntrySet.getString("data_field_9");
				String data_field_10 = dataEntrySet.getString("data_field_10");
				data_field_list.add(data_field_1);
				data_field_list.add(data_field_2);
				data_field_list.add(data_field_3);
				data_field_list.add(data_field_4);
				data_field_list.add(data_field_5);
				data_field_list.add(data_field_6);
				data_field_list.add(data_field_7);
				data_field_list.add(data_field_8);
				data_field_list.add(data_field_9);
				data_field_list.add(data_field_10);
				
				// Create a data entry to encapsulate the information; add entry to List
				DataEntry entry = new DataEntry(entry_name, entry_type, encryption_key, owner, validUsers, secure_entry, last_modified, data_field_list);
				dataEntryList.add(entry);
			}

			// Disconnect from database
			dataEntrySet.close();
			stmt.close();
			DBconnection.close();

			return dataEntryList;
			
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return null;
		}
	}

	public List<String> retrieveDataEntryNameList(String user_email) {
		List<String> entryNameList = new ArrayList<String>();

		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();

			// Construct the SQL select statement
			String sql = "SELECT entry_name FROM data_entries WHERE owner = '" + user_email + "';";

			// Execute SQL statement and retrieve result set
			ResultSet entryNameSet = stmt.executeQuery(sql);

			// Construct list from result set
			while (entryNameSet.next()) {
				String entryName = entryNameSet.getString("entry_name");
				entryNameList.add(entryName);
			}

			// Disconnect and close database
			entryNameSet.close();
			stmt.close();
			DBconnection.close();

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}

		return entryNameList;
	}

	public List<String> retrieveDataEntryTypeList(String user_email) {
		List<String> entryTypeList = new ArrayList<String>();

		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();

			// Construct the SQL select statement
			String sql = "SELECT entry_type FROM data_entries WHERE owner = '" + user_email + "';";

			// Execute SQL statement and retrieve result set
			ResultSet entryTypeSet = stmt.executeQuery(sql);

			// Construct list from result set
			while (entryTypeSet.next()) {
				String entryType = entryTypeSet.getString("entry_type");
				entryTypeList.add(entryType);
			}

			// Disconnect and close database
			entryTypeSet.close();
			stmt.close();
			DBconnection.close();

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}

		return entryTypeList;
	}

	// TODO May need to be adjusted based on how shared data entries will be
	// retrieved from the database (name & owner?)
	/**
	 * Generates an alphabetical List of all a given user’s viewable shared data
	 * entries. NOTE: Requires that valid_users field in database contains a
	 * SORTED String of user_emails separated by " " (space)
	 * 
	 * @param user_email
	 *            User to find viewable shared entries for
	 * @return Alphabetically-sorted List of all shared data entries a user has
	 *         permission to view.
	 */
	public List<String> retrieveSharedEntryList(String user_email) {
		List<String> sharedEntryList = new ArrayList<String>();

		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();

			// Construct the SQL select statement (gets ALL data entries)
			String sql = "SELECT entry_name, valid_users FROM data_entries;";

			// Execute SQL statement and retrieve result set
			ResultSet allDataEntries = stmt.executeQuery(sql);

			// Construct list of available shared entries from result set of ALL
			// entries
			while (allDataEntries.next()) {
				// parse the valid_users STRING to get resulting LIST of valid
				// users. Delimiter = ' '
				String validUsers = allDataEntries.getString("valid_users");
				String[] parsedValidUsers = validUsers.split(" ");
				// search the String array for user_email; add to
				// SharedEntryList
				if (Arrays.binarySearch(parsedValidUsers, user_email) >= 0) {
					sharedEntryList.add(allDataEntries.getString("entry_name"));
				}
			}

			// Disconnect and close database
			allDataEntries.close();
			stmt.close();
			DBconnection.close();

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}

		Collections.sort(sharedEntryList);
		return sharedEntryList;
	}

	public DataEntry retrieveOneDataEntry(String entryname, String email, String type) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();
			// Construct the SQL select statement
			String sql = "SELECT * FROM data_entries WHERE owner = " + "'" + email + "'" + " AND " + "entry_name = "
					+ "'" + entryname + "'" + " AND " + "entry_type = " + "'" + type + "';";

			// Execute the statement and commit database changes
			ResultSet dataEntryInfoSet = stmt.executeQuery(sql);

			// reconstruct entry
			String entryName = dataEntryInfoSet.getString("entry_name");
			String entryType = dataEntryInfoSet.getString("entry_type");
			String encryptionKey = dataEntryInfoSet.getString("encryption_key");
			String owner = dataEntryInfoSet.getString("owner");

			// Parse the valid_users String and convert to List<String> to
			// assign to validUsers field of DataEntry
			List<String> validUsers = new ArrayList<String>();
			String validUsersString = dataEntryInfoSet.getString("valid_users");
			String[] parsedValidUsers = validUsersString.split(" ");
			for (int i = 0; i < parsedValidUsers.length; i++) {
				validUsers.add(parsedValidUsers[i]);
			}

			int highSecurity = dataEntryInfoSet.getInt("secure_entry");
			String lastModified = dataEntryInfoSet.getString("last_modified");
			LocalDateTime modifiedLDT = LocalDateTime.parse(lastModified);

			String datafield_1 = dataEntryInfoSet.getString("data_field_1");
			String datafield_2 = dataEntryInfoSet.getString("data_field_2");
			String datafield_3 = dataEntryInfoSet.getString("data_field_3");
			String datafield_4 = dataEntryInfoSet.getString("data_field_4");
			String datafield_5 = dataEntryInfoSet.getString("data_field_5");
			String datafield_6 = dataEntryInfoSet.getString("data_field_6");
			String datafield_7 = dataEntryInfoSet.getString("data_field_7");
			String datafield_8 = dataEntryInfoSet.getString("data_field_8");
			String datafield_9 = dataEntryInfoSet.getString("data_field_9");
			String datafield_10 = dataEntryInfoSet.getString("data_field_10");

			List<String> fields = new ArrayList<String>();
			fields.add(datafield_1);
			fields.add(datafield_2);
			fields.add(datafield_3);
			fields.add(datafield_4);
			fields.add(datafield_5);
			fields.add(datafield_6);
			fields.add(datafield_7);
			fields.add(datafield_8);
			fields.add(datafield_9);
			fields.add(datafield_10);

			// Reconstruct DataEntry
			DataEntry dataEntry = new DataEntry(entryName, entryType, encryptionKey, owner, highSecurity, modifiedLDT);
			dataEntry.setHighSecurity(highSecurity);
			dataEntry.setDataFields(fields);
			dataEntry.setValidUsers(validUsers);

			// Disconnect from database
			dataEntryInfoSet.close();
			stmt.close();
			DBconnection.close();

			// return a success value
			return dataEntry;
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return null;
		}
	}

	/**
	 * Modifies a TEXT (String) user field in the 'users' table of the vault
	 * database
	 * 
	 * @param user
	 *            user whose field is to be updated
	 * @param fieldName
	 *            name of user field to modify ('users' table column identifier)
	 * @param newTextData
	 *            new TEXT data to put into user field
	 */
	public void modifyUserField(User user, String fieldName, String newTextData) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();

		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();
			// Construct the SQL UPDATE statement

			String sql = "UPDATE users SET " + fieldName + " = '" + newTextData + "' " + "WHERE user_email = '"
					+ user.getUsername() + "';";

			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();

			// Disconnect from database
			stmt.close();
			DBconnection.close();
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Modifies a INTEGER (int) user field in the 'users' table of the vault
	 * database
	 * 
	 * @param user
	 *            user whose field is to be updated
	 * @param fieldName
	 *            name of user field to modify ('users' table column identifier)
	 * @param newIntData
	 *            new INTEGER data to put into user field
	 */
	public void modifyUserField(User user, String fieldName, int newIntData) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();

		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();
			// Construct the SQL UPDATE statement

			String sql = "UPDATE users SET " + fieldName + " = " + String.valueOf(newIntData) + " "
					+ "WHERE user_email = '" + user.getUsername() + "';";

			/*
			 * String sql = "UPDATE users" + "SET " + fieldName + " = '" +
			 * String.valueOf(newIntData) + "' " + "WHERE user_email = '" +
			 * user.getUsername() + "';";
			 * 
			 */

			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();

			// Disconnect from database
			stmt.close();
			DBconnection.close();
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	public int updateEntry(DataEntry oldEntry, DataEntry newEntry) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			if (!oldEntry.getEntryName().equals(newEntry.getEntryName())) {
				String count = "SELECT COUNT(*) FROM data_entries WHERE entry_name=? AND owner=?;";
				PreparedStatement preparedStatement = DBconnection.prepareStatement(count);
				preparedStatement.setString(1, newEntry.getEntryName());
				preparedStatement.setString(2, newEntry.getOwner());
				ResultSet results = preparedStatement.executeQuery();
				if (results.getInt(1) != 0) {
					// entry_name exists, return failure value
					System.out.println("Entry name already exists!");
					results.close();
					preparedStatement.close();
					DBconnection.close();
					return -1;
				}
			}
			// Initialize a statement to execute

			// Construct the SQL INSERT statement
			int size_of_datafield = newEntry.getFieldDataList().size();
			String sql = "UPDATE data_entries SET entry_name=?,";
			for (int i = 0; i < size_of_datafield; i++) {
				sql += "data_field_" + Integer.toString(i + 1) + "=?";
				if (i != size_of_datafield - 1)
					sql += ",";
			}
			sql += " WHERE entry_name=? AND owner=?;";


			PreparedStatement preparedStatement = DBconnection.prepareStatement(sql);
			int j = 2;
			preparedStatement.setString(1, newEntry.getEntryName());
			for (int i = 0; i < size_of_datafield; i++) {
				System.out.println(newEntry.getFieldDataList().get(i));
				preparedStatement.setString(2 + i, newEntry.getFieldDataList().get(i));
				j++;
			}
			System.out.println(newEntry.getEntryName() + oldEntry.getOwner());
			preparedStatement.setString(j , oldEntry.getEntryName());
			preparedStatement.setString(j + 1 , oldEntry.getOwner());

			//System.out.println(sql);
			
			/*String sql_test = "UPDATE data_entries SET entry_name=?,data_field_1=? WHERE ";
			
			preparedStatement = DBconnection.prepareStatement(sql_test);
			preparedStatement.setString(1, "myss");
			preparedStatement.setString(2, "default");
			preparedStatement.setString(3, "default");
			preparedStatement.setString(4, "myss");*/
			// Execute the statement and commit database changes
			preparedStatement.executeUpdate();
			DBconnection.commit();

			// Disconnect from database
			preparedStatement.close();
			DBconnection.close();

			// return a success value
			return 1;

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}

	}

	public int addEntryToDatabase(User user, DataEntry entry) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		Crypto c = new Crypto();
		try {
			entry.setEncryptionKey(c.randomDataKey(entry.isHighSecurity()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		entry = c.encrypt(user, entry);
		try {
			// Initialize a statement to execute

			String count = "SELECT COUNT(*) FROM data_entries WHERE entry_name=? AND owner=?;";
			PreparedStatement preparedStatement = DBconnection.prepareStatement(count);
			preparedStatement.setString(1, entry.getEntryName());
			preparedStatement.setString(2, entry.getOwner());
			// Check that entry_name does not already exist
			ResultSet results = preparedStatement.executeQuery();
			if (results.getInt(1) != 0) {
				// entry_name exists, return failure value
				System.out.println("Entry name already exists!");
				results.close();
				preparedStatement.close();
				DBconnection.close();
				return -1;
			}

			// Construct the SQL INSERT statement
			int field_number = entry.getFieldDataList().size();
			String sql = "INSERT INTO data_entries(entry_name, entry_type, encryption_key, owner, valid_users, secure_entry, last_modified";
			for (int i = 0; i < 10; i++) {
				sql = sql + ", ";
				sql = sql + "data_field_" + Integer.toString(i + 1);
			}
			sql = sql + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

			preparedStatement = DBconnection.prepareStatement(sql);

			preparedStatement.setString(1, entry.getEntryName());
			preparedStatement.setString(2, entry.getEntryType());
			preparedStatement.setString(3, entry.getEncryptionKey());
			preparedStatement.setString(4, entry.getOwner());
			preparedStatement.setString(5, entry.buildValidUsersString());
			preparedStatement.setInt(6, entry.isHighSecurity());
			preparedStatement.setString(7, entry.getLastModified().toString());
			
			int j = 0;
			for (int i = 0; i < field_number; i++) {
				preparedStatement.setString(8 + i, entry.getFieldDataList().get(i));
				j++;
			}
			for (int i = j; i < 10; i++) {
				preparedStatement.setString(8 + i, "null");
			}

			System.out.println(preparedStatement.toString());
			preparedStatement.executeUpdate();
			
			// Execute the statement and commit database changes
			// stmt.executeUpdate(sql);
			DBconnection.commit();

			// Disconnect from database
			preparedStatement.close();
			DBconnection.close();

			// return a success value
			return 1;

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}
	}

	public int deleteEntryFromDatabase(DataEntry entry) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();

			// Construct the SQL INSERT statement
			String sql = "DELETE FROM data_entries WHERE entry_name='" + entry.getEntryName() + "'" + " AND "
					+ "owner='" + entry.getOwner() + "';";
			System.out.println(sql);

			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();

			// Disconnect from database
			stmt.close();
			DBconnection.close();

			// return a success value
			return 1;

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}
	}

	// ******This method should always be called BEFORE we delete a user
	// account*****
	// TODO
	public int deleteAllEntriesFromDatabase(User doomeduser) {
		// Connect to the database
		Connection DBconnection = connectToDatabase();
		try {
			// Initialize a statement to execute
			Statement stmt = DBconnection.createStatement();

			// Construct the SQL INSERT statement
			String sql = "DELETE FROM data_entries WHERE owner='" + doomeduser.getUsername() + "';";
			System.out.println(sql);

			// Execute the statement and commit database changes
			stmt.executeUpdate(sql);
			DBconnection.commit();

			// Disconnect from database
			stmt.close();
			DBconnection.close();

			// return a success value
			return 1;

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			// return a failure value
			return -1;
		}
	}
}
