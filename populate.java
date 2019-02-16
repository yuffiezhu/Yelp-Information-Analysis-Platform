package yelp_dataAnalysis;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;
import org.json.*;

public class populate {
	
	public static void readFile(String path, Connection conn) {
		File file = new File(path);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while((tempString = reader.readLine()) != null) {
				JSONObject json = new JSONObject(tempString);
				insertQuery(json, conn);
			}
			reader.close();
			conn.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException el) {
					el.printStackTrace();
				}
			}
		}
	}
	
	public static void insertQuery(JSONObject json, Connection conn) throws SQLException, Exception {
		String insertString = "";
		PreparedStatement pstm = null;
		if (json.getString("type").equals("business")) {
			String[] mainArray = {"Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Convenience Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"};
			List<String> mainList = Arrays.asList(mainArray);
			insertString = "INSERT INTO business VALUES (?, ?, ?, ?, ?)";
			String insertCategory = "INSERT INTO business_category VALUES (?, ?, ?)";
			String business_id = json.getString("business_id");
			String name = json.getString("name");
			String city = json.getString("city");
			String state = json.getString("state");
			float stars = json.getFloat("stars");
			pstm = conn.prepareStatement(insertString);			
			pstm.setString(1, business_id);
			pstm.setString(2, name);
			pstm.setString(3, city);
			pstm.setString(4, state);
			pstm.setFloat(5, stars);
			pstm.executeUpdate();
			conn.commit();
			pstm.close();

			pstm = conn.prepareStatement(insertCategory);
			JSONArray arr_category = json.getJSONArray("categories");
			int length = arr_category.length();
			Set<String> maincategory = new HashSet<>();
			Set<String> subcategory = new HashSet<>();
			for(int i = 0; i < length; i++) {
				if(mainList.contains(arr_category.getString(i))) {
					maincategory.add(arr_category.getString(i));
				} else {
					subcategory.add(arr_category.getString(i));
				}
			}
			if (maincategory.size() != 0) {
				for (String main : maincategory) {
					for (String sub : subcategory) {
						pstm.setString(1, business_id);
						pstm.setString(2, main);
						pstm.setString(3, sub);
						pstm.executeUpdate();
						conn.commit();
					}
				}
			} else {
				for (String sub : subcategory) {
					pstm.setString(1, business_id);
					pstm.setString(2, null);
					pstm.setString(3, sub);
				}
			}
			pstm.close();

		} else if (json.getString("type").equals("review")) {
			insertString = "INSERT INTO reviews VALUES (?, ?, ?, ?, ?, ?)";
			pstm = conn.prepareStatement(insertString);
			String review_id = json.getString("review_id");
			int stars = json.getInt("stars");
			String author = json.getString("user_id");
			String business_id = json.getString("business_id");
			String publish_date = json.getString("date"); 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(publish_date);
			java.sql.Date p_date = new java.sql.Date(date.getTime());
			JSONObject votes = json.getJSONObject("votes");
			int funny = votes.getInt("funny");
			int useful = votes.getInt("useful");
			int cool = votes.getInt("cool");
			int voteNum = funny + useful + cool;
			pstm.setString(1, review_id);
			pstm.setInt(2, stars);
			pstm.setString(3, author);
			pstm.setDate(4, p_date);
			pstm.setString(5, business_id);
			pstm.setInt(6, voteNum);
			pstm.executeUpdate();
			conn.commit();
			pstm.close();
		} else if (json.getString("type").equals("checkin")) {
			insertString = "INSERT INTO check_in VALUES (?, ?, ?, ?)";
			pstm = conn.prepareStatement(insertString);
			String business_id = json.getString("business_id");
			JSONObject info = json.getJSONObject("checkin_info");
			for (String key : info.keySet()) {			
				int num = info.getInt(key);
				String[] keySplit = key.split("-");
				int hour = Integer.parseInt(keySplit[0]);
				int day = Integer.parseInt(keySplit[1]);
				/*if (keySplit[1].equals("0")) {day = 0;}
				else if (keySplit[1].equals("1")) {day = 1;}
				else if (keySplit[1].equals("2")) {day = "Tuesdays";}
				else if (keySplit[1].equals("3")) {day = "Wednesdays";}
				else if (keySplit[1].equals("4")) {day = "Thursdays";}
				else if (keySplit[1].equals("5")) {day = "Fridays";}
				else if (keySplit[1].equals("6")) {day = "Saturdays";} */
				//insertString = "INSERT INTO check_in VALUES('" + business_id + "', '" + day + "', " + Integer.toString(hour) + ", " + Integer.toString(num) + ")";
				//statement.executeUpdate(insertString);
				pstm.setString(1, business_id);
				pstm.setInt(2, day);
				pstm.setInt(3, hour);
				pstm.setInt(4, num);
				pstm.executeUpdate();
			}
			conn.commit();
			pstm.close();
			
		} else if (json.getString("type").equals("user")) {
			insertString = "INSERT INTO y_user VALUES (?, ?, ?, ?, ?, ?)";
			pstm = conn.prepareStatement(insertString);
			String yelp_id = json.getString("user_id");
			String name = json.getString("name");
			int review_count = json.getInt("review_count");
			String sinceT = json.getString("yelping_since");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			Date date = sdf.parse(sinceT);
			java.sql.Date since = new java.sql.Date(date.getTime());
			float avg_stars = json.getFloat("average_stars");
			JSONArray friends = json.getJSONArray("friends");
			int num_friends = friends.length();
			//insertString = "INSERT INTO y_user VALUES('" + yelp_id + "', '" + name + "', " + Integer.toString(review_count) + ", '" + String.valueOf(since) + "', " + String.valueOf(avg_stars) + ", " + Integer.toString(num_friends) + ")";			
			//statement.executeUpdate(insertString);
			pstm.setString(1, yelp_id);
			pstm.setString(2, name);
			pstm.setInt(3, review_count);
			pstm.setDate(4, since);
			pstm.setFloat(5, avg_stars);
			pstm.setInt(6, num_friends);
			pstm.executeUpdate();
			conn.commit();
			pstm.close();
		}	
	}
	
	
	public static Connection connectDB() {
	Connection connection = null;
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl","scott","1qaz");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	public static void main(String[] args) {
		Connection conn = connectDB();
		Statement statement;
		try {
			statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String findTable = "select table_name from user_tables";
			ResultSet tableName = statement.executeQuery(findTable);
			int rowCount = 0;
			if(tableName != null) {
				tableName.last();
				rowCount = tableName.getRow();
			}
			String[] tables = new String[rowCount];
			tableName.beforeFirst();
			int i = 0;
			while (tableName.next()) {
				tables[i] = tableName.getString(1);
			}
			
			String dropTable = "DROP TABLE ?";
			PreparedStatement pstm = conn.prepareStatement(dropTable);
			for (int j = 0; j < tables.length; j++) {
				pstm.setString(1, tables[j]);
				pstm.executeQuery();
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		for (int i = 0; i < args.length; i++) {
			readFile(args[i], conn);
		}
		System.out.println("all inserted"); 
	}
}
