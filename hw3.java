package yelp_dataAnalysis;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.BoxLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextArea;

public class hw3 {
	private JFrame frame;
	private JTextField textField_numCheckin;
	private JTextField textField_reviewstarvalue;
	private JTextField textField_reviewvotesnum;
	private JTextField textField_reviewCountNum;
	private JTextField textField_friendNun;
	private JTextField textField_avgStar;
	private JTextArea textArea_query;
	private JComboBox<String> comboBox_since;
	private JComboBox<String> comboBox_userReviewCount;
	private JComboBox<String> comboBox_numFriend;
	private JComboBox<String> comboBox_AvgStars;
	private JComboBox<String> comboBox_userSelect;
	private JScrollPane scrollPane_result; 
	private JTable resultTable; 
	private JPanel panel_result;
	private JPanel panel_select;
	
	private static Set<String> mainCategory = new HashSet<>();
	private static Set<String> subCategory = new HashSet<>();
	private JComboBox<String> checkin_fromday;
	private JComboBox<String> checkin_today;
	private JComboBox<String> checkin_fromhour;
	private JComboBox<String> checkin_tohour;
	private JComboBox<String> checkinOpr;
	private JComboBox<String> comboBox_reviewfrom;
	private JComboBox<String> comboBox_reviewto;
	private JComboBox<String> comboBox_reviewstarOpr;
	private JComboBox<String> comboBox_reviewvotesOpr;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					hw3 window = new hw3();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public hw3() {
		try {
			initialize();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String convertString(Set<String> data) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String item : data) {
			builder.append("'").append(item).append("'");
			if (i != data.size() - 1) {
				builder.append(", ");
			}
			i++;
		}
		return builder.toString();
	}
	
	private String businessQuery() throws ParseException {
		//build selected day map to integer
		Map<String, Integer> dayMap = new HashMap<>();
		dayMap.put("Sunday", 0);
		dayMap.put("Monday", 1);
		dayMap.put("Tuesday", 2);
		dayMap.put("Wednesday", 3);
		dayMap.put("Thursday", 4);
		dayMap.put("Friday", 5);
		dayMap.put("Saturday", 6);
				
		String mainString = convertString(mainCategory);
		String subString = convertString(subCategory);
		
		String fromTemp = (String) checkin_fromday.getSelectedItem();
		int fromDay = dayMap.get(fromTemp);
		String toTemp = (String) checkin_today.getSelectedItem();
		int toDay = dayMap.get(toTemp);
		String fromHour = (String) checkin_fromhour.getSelectedItem();
		String toHour = (String) checkin_tohour.getSelectedItem();
		String compareCheckin = (String) checkinOpr.getSelectedItem();
		String numCheckin = textField_numCheckin.getText();				
			
		String reviewfrom = (String) comboBox_reviewfrom.getSelectedItem();
		String reviewto = (String) comboBox_reviewto.getSelectedItem();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date fromDate = sdf.parse(reviewfrom);
		Date toDate = sdf.parse(reviewto);
		java.sql.Date fromDateSql = new java.sql.Date(fromDate.getTime());
		java.sql.Date toDateSql = new java.sql.Date(toDate.getTime());
		
		String reviewStarOpr = (String) comboBox_reviewstarOpr.getSelectedItem();
		String reviewVotesOpr = (String) comboBox_reviewvotesOpr.getSelectedItem();
		
		String numReviewStar = textField_reviewstarvalue.getText();
		String numReviewVotes = textField_reviewvotesnum.getText();	
		
		String selectCategory = "";
		String selectCheckin = "";
		String selectReview = "";
		
		if (!mainString.isEmpty()) {
			selectCategory = "SELECT business_id FROM business_category WHERE main_category = ANY (" + mainString + ")";
			if (!subString.isEmpty()) {
				selectCategory += " AND sub_category = ANY (" + subString + ")";
			}
			selectCategory += " INTERSECT ";
		}	
		
		if (!numCheckin.isEmpty() ) {
			selectCheckin = "SELECT business_id FROM check_in WHERE (day > " + fromDay + " AND day < " + toDay + ") OR (day = " + fromDay +
							" AND hour >=" + fromHour + ") OR (day = " + toDay +  " AND hour < " + toHour + ") GROUP BY business_id HAVING SUM(num) " 
							+ compareCheckin + numCheckin + " INTERSECT ";
		}

		if (!numReviewStar.isEmpty() || !numReviewVotes.isEmpty()) {
			selectReview = "SELECT business_id FROM reviews WHERE publish_date BETWEEN Date '" + fromDateSql + "' AND Date '" + toDateSql + 
							"' GROUP BY business_id HAVING ";
			String op1 = "";
			String op2 = "";
			if (!numReviewStar.isEmpty()) {
				op1 += "AVG(stars) " + reviewStarOpr + numReviewStar;
			}
			if (!numReviewVotes.isEmpty()) {
				op2 += "AVG(votes) " + reviewVotesOpr + numReviewVotes;
			}
			if (!numReviewVotes.isEmpty() && !numReviewStar.isEmpty()) {
				selectReview = selectReview + op1 + " AND " + op2;
			} else {
				selectReview = selectReview + op1 + op2;
			}
			selectReview += " INTERSECT ";
		}
	
		String selectColumn = "SELECT name, city, state, stars FROM business WHERE business_id IN (";
		String intersect = " INTERSECT ";
		String queryBusiness = "";
		if (selectCategory.isEmpty() && selectCheckin.isEmpty() && selectReview.isEmpty()) {
			queryBusiness = "SELECT name, city, state, stars FROM business";
		} else {
			queryBusiness = selectColumn + selectCategory + selectCheckin  + selectReview;
			int length = queryBusiness.length() - intersect.length();
			queryBusiness = queryBusiness.substring(0, length) + ")";
		}
		return queryBusiness;
	}
	
	private String usersQuery() throws ParseException {
		String queryUsers = "";
		
		String since = (String) comboBox_since.getSelectedItem();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date sinceDate = sdf.parse(since);
		java.sql.Date sinceDateSql = new java.sql.Date(sinceDate.getTime());
		
		String reviewCountOpr = (String) comboBox_userReviewCount.getSelectedItem();
		String numFriendOpr = (String) comboBox_numFriend.getSelectedItem();
		String avgStarOpr = (String) comboBox_AvgStars.getSelectedItem();
		String opr = (String) comboBox_userSelect.getSelectedItem();
		
		String reviewCount = textField_reviewCountNum.getText();
		String friendNum = textField_friendNun.getText();
		String avgStar = textField_avgStar.getText();
		
		String selectFrom = "SELECT name, since AS Yelp_Since, avg_stars AS \"Avg. Star\" FROM y_user ";
		String whereString = "WHERE ";
		if (!since.isEmpty()) {
			whereString = whereString + "since > Date '" + sinceDateSql + "' " + opr + " ";
		} 
		if (!reviewCountOpr.isEmpty() && !reviewCount.isEmpty()) {
			whereString = whereString + "review_count " + reviewCountOpr + " " + reviewCount + " " + opr + " ";
		}
		if (!numFriendOpr.isEmpty() && !friendNum.isEmpty()) {
			whereString = whereString + "num_friends " + numFriendOpr + " " + friendNum + " " + opr + " ";
		}
		if (!avgStarOpr.isEmpty() && !avgStar.isEmpty()) {
			whereString = whereString + "avg_stars" + avgStarOpr + " " + avgStar + " " + opr + " ";
		}
		
		
		int length = whereString.length() - opr.length() - 2;
		whereString = whereString.substring(0, length);	
		queryUsers = selectFrom + whereString;

		return queryUsers;		
	}
	
	private void selectedQuery(String identify, String name, Connection conn) throws SQLException {
		String query = "";
		if (identify.equals("user")) {
			query = "SELECT review_id, stars, business_id FROM reviews r, y_user u WHERE r.author = u.yelp_id AND u.name = ?";
		}
		if (identify.equals("business")) {
			query = "SELECT review_id, r.stars, u.name\r\n" + 
					"FROM reviews r, business b, y_user u  \r\n" + 
					"WHERE r.business_id = b.business_id AND r.author = u.yelp_id AND b.name = ?";
		}
		PreparedStatement pstm = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		pstm.setString(1, name);
		ResultSet result = pstm.executeQuery();
		JTable resultTable = new JTable();
		resultTable = showResult(result);
		panel_select.removeAll();
		panel_select.add(resultTable.getTableHeader(),BorderLayout.NORTH);
		panel_select.add(resultTable, BorderLayout.CENTER);
		panel_select.revalidate();
		pstm.close();
	}
	
	private JTable showResult(ResultSet result) throws SQLException {
		// Get Column Header
		ResultSetMetaData resultMeta = result.getMetaData();
		int columnCount = resultMeta.getColumnCount();
		int rowCount = 0;
		if(result != null) {
			result.last();
			rowCount = result.getRow();
		}

		String[] columnNames = new String[columnCount];
		for (int i = 1; i <= columnCount; i++) {
            columnNames[i-1] =  resultMeta.getColumnName(i);
        }
		
		Object[][] data = new Object[rowCount][columnCount];
		result.beforeFirst();
		int i = 0;
		while(result.next()) {
			for (int j = 0; j < columnCount; j++) {
				data[i][j] = result.getObject(j+1);
			}
			i++;
		}
		
		JTable resultTable = new JTable(data, columnNames);
		
		return resultTable;
	}
	
	
	

	/**
	 * Initialize the contents of the frame.
	 * @throws SQLException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initialize() throws SQLException {
		Connection conn = connectDB();
		Statement statement = conn.createStatement();
		frame = new JFrame();
		frame.setBounds(100, 100, 1059, 453);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel_query = new JPanel();
		panel_query.setBorder(new MatteBorder(1, 1, 1, 1, (Color) Color.LIGHT_GRAY));
		panel_query.setBounds(301, 249, 290, 153);
		frame.getContentPane().add(panel_query);
		panel_query.setLayout(null);
		
		JButton btnGoBusiness = new JButton("Go Business");
		btnGoBusiness.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String button = arg0.getActionCommand();
				if (button.equals("Go Business")) {
					String queryCreated;
					panel_result.removeAll();
					panel_select.removeAll();
					try {
						queryCreated = businessQuery();				
						textArea_query.setText(queryCreated);						
						Statement businessQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						ResultSet result = businessQuery.executeQuery(queryCreated);
						resultTable = showResult(result);
						panel_result.add(resultTable.getTableHeader(),BorderLayout.NORTH);
						panel_result.add(resultTable, BorderLayout.CENTER);
						resultTable.setRowSelectionAllowed(true);
						resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						ListSelectionModel rowSelectionModel = resultTable.getSelectionModel();
						rowSelectionModel.addListSelectionListener(new ListSelectionListener() {
							@Override
							public void valueChanged(ListSelectionEvent arg0) {
								int row = resultTable.getSelectedRow();
								if (row != -1) {
									String name = (String) resultTable.getValueAt(row, 0);
									String identify = "business";
									try {
										selectedQuery(identify, name, conn);
									} catch (SQLException e) {
										e.printStackTrace();
									}
									
								}
							}
							
						});
						panel_result.revalidate();
						scrollPane_result.setViewportView(panel_result);				
						businessQuery.close();
					} catch (SQLException | ParseException e) {
						e.printStackTrace();
					}				
					
				}
			}
		});
		btnGoBusiness.setFont(new Font("Arial", Font.PLAIN, 11));
		btnGoBusiness.setBounds(33, 121, 95, 23);
		panel_query.add(btnGoBusiness);
		
		JButton btnGoUsers = new JButton("Go Users");
		btnGoUsers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String button = arg0.getActionCommand();
				if (button.equals("Go Users")) {
					String queryCreated;
					panel_result.removeAll();
					panel_select.removeAll();
					try {
						queryCreated = usersQuery();				
						textArea_query.setText(queryCreated);						
						Statement usersQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						ResultSet result = usersQuery.executeQuery(queryCreated);
						
						resultTable = showResult(result);						
						panel_result.add(resultTable.getTableHeader(),BorderLayout.NORTH);
						panel_result.add(resultTable, BorderLayout.CENTER);
						resultTable.setRowSelectionAllowed(true);
						resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						ListSelectionModel rowSelectionModel = resultTable.getSelectionModel();
						rowSelectionModel.addListSelectionListener(new ListSelectionListener() {
							@Override
							public void valueChanged(ListSelectionEvent arg0) {
								int row = resultTable.getSelectedRow();
								if (row != -1) {
									String name = (String) resultTable.getValueAt(row, 0);
									String identify = "user";
									try {
										selectedQuery(identify, name, conn);
									} catch (SQLException e) {
										e.printStackTrace();
									}
									
								}
							}
							
						});
						panel_result.revalidate();
						scrollPane_result.setViewportView(panel_result);				
						usersQuery.close();
						
					} catch (ParseException e1) {
						e1.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}				
					
				}
			}
		});
		btnGoUsers.setFont(new Font("Arial", Font.PLAIN, 11));
		btnGoUsers.setBounds(170, 121, 95, 23);
		panel_query.add(btnGoUsers);
		
		JScrollPane scrollPane_query = new JScrollPane();
		scrollPane_query.setBounds(10, 10, 270, 101);
		panel_query.add(scrollPane_query);
		
		textArea_query = new JTextArea();
		textArea_query.setWrapStyleWord(true);
		textArea_query.setLineWrap(true);
		scrollPane_query.setViewportView(textArea_query);
		
		JPanel panel_business = new JPanel();
		panel_business.setBorder(new TitledBorder(null, "Business", TitledBorder.CENTER, TitledBorder.TOP, null, Color.BLUE));
		panel_business.setBounds(0, 0, 591, 247);
		frame.getContentPane().add(panel_business);
		panel_business.setLayout(null);
		
		JPanel panel_checkin = new JPanel();
		panel_checkin.setBounds(300, 21, 148, 216);
		panel_checkin.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "checkin", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_business.add(panel_checkin);
		panel_checkin.setLayout(null);
		
		JLabel lblFrom = new JLabel("from");
		lblFrom.setFont(new Font("Arial", Font.PLAIN, 9));
		lblFrom.setBounds(10, 22, 24, 15);
		panel_checkin.add(lblFrom);
		
		JLabel lblTo = new JLabel("to");
		lblTo.setFont(new Font("Arial", Font.PLAIN, 9));
		lblTo.setBounds(10, 66, 30, 15);
		panel_checkin.add(lblTo);
		
		JLabel lblNoOfCheckins = new JLabel("No. of Checkins:");
		lblNoOfCheckins.setFont(new Font("Arial", Font.PLAIN, 9));
		lblNoOfCheckins.setBounds(10, 117, 89, 15);
		panel_checkin.add(lblNoOfCheckins);
		
		JLabel lblValue = new JLabel("Value:");
		lblValue.setFont(new Font("Arial", Font.PLAIN, 9));
		lblValue.setBounds(10, 166, 54, 15);
		panel_checkin.add(lblValue);
		
		checkin_fromday = new JComboBox<>();
		checkin_fromday.setModel(new DefaultComboBoxModel(new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}));
		/*
		 checkin_fromday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JComboBox temp = (JComboBox) arg0.getSource();
				String daySelected = (String) temp.getSelectedItem();
				fromDay = dayMap.get(daySelected);
			}		
		});
		*/
		checkin_fromday.setFont(new Font("Arial", Font.PLAIN, 10));
		checkin_fromday.setBounds(10, 35, 78, 19);
		panel_checkin.add(checkin_fromday);
		
		checkin_fromhour = new JComboBox<>();
		checkin_fromhour.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"}));
		/*checkin_fromhour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox temp = (JComboBox) e.getSource();
				String hourSelected = (String) temp.getSelectedItem();
				fromHour = Integer.parseInt(hourSelected);
			}
			
		});*/
		checkin_fromhour.setFont(new Font("Arial", Font.PLAIN, 10));
		checkin_fromhour.setBounds(98, 34, 38, 21);
		panel_checkin.add(checkin_fromhour);
		
		checkin_today = new JComboBox<>();
		checkin_today.setModel(new DefaultComboBoxModel(new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}));
		/*checkin_today.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JComboBox temp = (JComboBox) arg0.getSource();
				String daySelected = (String) temp.getSelectedItem();
				toDay = dayMap.get(daySelected);
			}		
		});*/
		checkin_today.setFont(new Font("Arial", Font.PLAIN, 10));
		checkin_today.setBounds(10, 80, 78, 19);
		panel_checkin.add(checkin_today);
		
		checkin_tohour = new JComboBox<>();
		checkin_tohour.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"}));
		/*checkin_tohour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox temp = (JComboBox) e.getSource();
				String hourSelected = (String) temp.getSelectedItem();
				toHour = Integer.parseInt(hourSelected);
			}
			
		});*/
		checkin_tohour.setFont(new Font("Arial", Font.PLAIN, 10));
		checkin_tohour.setBounds(98, 79, 38, 21);
		panel_checkin.add(checkin_tohour);
		
		checkinOpr = new JComboBox<>();
		checkinOpr.setFont(new Font("Arial", Font.PLAIN, 12));
		checkinOpr.setModel(new DefaultComboBoxModel(new String[] {"=", ">", "<"}));
		/*selectCheckin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox temp = (JComboBox) e.getSource();
				compareCheckin = (String) temp.getSelectedItem();
			}			
		});*/
		checkinOpr.setBounds(47, 135, 32, 21);
		panel_checkin.add(checkinOpr);
		
		textField_numCheckin = new JTextField();
		textField_numCheckin.setBounds(47, 166, 48, 21);
		panel_checkin.add(textField_numCheckin);
		textField_numCheckin.setColumns(10);
		
		JPanel panel_reviews = new JPanel();
		panel_reviews.setBounds(448, 21, 133, 216);
		panel_reviews.setBorder(new TitledBorder(null, "Review", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_business.add(panel_reviews);
		panel_reviews.setLayout(null);
		
		JLabel lblFrom_1 = new JLabel("from");
		lblFrom_1.setFont(new Font("Arial", Font.PLAIN, 9));
		lblFrom_1.setBounds(10, 24, 54, 15);
		panel_reviews.add(lblFrom_1);
		
		JLabel lblTo_1 = new JLabel("to");
		lblTo_1.setFont(new Font("Arial", Font.PLAIN, 10));
		lblTo_1.setBounds(10, 58, 54, 15);
		panel_reviews.add(lblTo_1);
		
		JLabel lblStar = new JLabel("star:");
		lblStar.setFont(new Font("Arial", Font.PLAIN, 10));
		lblStar.setBounds(10, 94, 19, 12);
		panel_reviews.add(lblStar);
		
		JLabel lblValue_1 = new JLabel("value:");
		lblValue_1.setFont(new Font("Arial", Font.PLAIN, 10));
		lblValue_1.setBounds(10, 119, 29, 13);
		panel_reviews.add(lblValue_1);
		
		JLabel lblVotes = new JLabel("votes:");
		lblVotes.setFont(new Font("Arial", Font.PLAIN, 10));
		lblVotes.setBounds(10, 163, 36, 15);
		panel_reviews.add(lblVotes);
		
		JLabel lblValue_2 = new JLabel("value:");
		lblValue_2.setFont(new Font("Arial", Font.PLAIN, 10));
		lblValue_2.setBounds(10, 188, 36, 15);
		panel_reviews.add(lblValue_2);
		
		//query all the available publish date from DB
		String queryReviewDate = "SELECT DISTINCT publish_date FROM reviews ORDER BY publish_date ASC";
		ResultSet resultReviewDate = statement.executeQuery(queryReviewDate);
		ArrayList<String> reviewDate = new ArrayList<>();
		while (resultReviewDate.next()) {
			String date = resultReviewDate.getString(1);
			reviewDate.add(date);
		}		
		
		comboBox_reviewfrom = new JComboBox<>();
		for (String date : reviewDate) {
			comboBox_reviewfrom.addItem(date);
		}
		comboBox_reviewfrom.setFont(new Font("Arial", Font.PLAIN, 10));
		comboBox_reviewfrom.setBounds(47, 24, 76, 19);
		panel_reviews.add(comboBox_reviewfrom);
		
		comboBox_reviewto = new JComboBox<>();
		comboBox_reviewto.setFont(new Font("Arial", Font.PLAIN, 10));
		for (String date : reviewDate) {
			comboBox_reviewto.addItem(date);
		}
		/*comboBox_reviewfrom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox temp = (JComboBox) e.getSource();
				String toString = (String) temp.getSelectedItem();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					toDate = sdf.parse(toString);
				} catch (ParseException e1) {
					// 
					e1.printStackTrace();
				}				
			}
			
		});*/
		comboBox_reviewto.setBounds(47, 54, 76, 19);
		panel_reviews.add(comboBox_reviewto);
		
		comboBox_reviewstarOpr = new JComboBox<>();
		comboBox_reviewstarOpr.setModel(new DefaultComboBoxModel(new String[] {"=", ">", "<"}));
		/*comboBox_reviewstar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox temp = (JComboBox) e.getSource();
				reviewStarOpr = (String) temp.getSelectedItem();
			}			
		});*/
		comboBox_reviewstarOpr.setBounds(47, 91, 32, 21);
		panel_reviews.add(comboBox_reviewstarOpr);
		
		textField_reviewstarvalue = new JTextField();
		textField_reviewstarvalue.setBounds(48, 122, 66, 21);
		panel_reviews.add(textField_reviewstarvalue);
		textField_reviewstarvalue.setColumns(10);
		
		comboBox_reviewvotesOpr = new JComboBox<>();
		comboBox_reviewvotesOpr.setFont(new Font("Arial", Font.PLAIN, 12));
		comboBox_reviewvotesOpr.setModel(new DefaultComboBoxModel(new String[] {"=", ">", "<"}));
		/*comboBox_reviewvotes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox temp = (JComboBox) e.getSource();
				reviewVotesOpr = (String) temp.getSelectedItem();
			}			
		});*/
		comboBox_reviewvotesOpr.setBounds(47, 163, 33, 21);
		panel_reviews.add(comboBox_reviewvotesOpr);
		
		textField_reviewvotesnum = new JTextField();
		textField_reviewvotesnum.setFont(new Font("Arial", Font.PLAIN, 10));
		textField_reviewvotesnum.setBounds(47, 186, 66, 21);
		panel_reviews.add(textField_reviewvotesnum);
		textField_reviewvotesnum.setColumns(10);
		
		JScrollPane scrollPane_main = new JScrollPane();
		scrollPane_main.setViewportBorder(new TitledBorder(null, "Category", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		scrollPane_main.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane_main.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_main.setBounds(10, 21, 143, 216);
		panel_business.add(scrollPane_main);
		
		JPanel panel_main = new JPanel();
		scrollPane_main.setViewportView(panel_main);
		panel_main.setLayout(new BoxLayout(panel_main, BoxLayout.Y_AXIS));
		
		//sub-category scroll panel
		JScrollPane scrollPane_sub = new JScrollPane();
		scrollPane_sub.setViewportBorder(new TitledBorder(null, "Sub-Category", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		scrollPane_sub.setBounds(155, 21, 143, 216);
		panel_business.add(scrollPane_sub);		
						
		JPanel panel_sub = new JPanel();
		scrollPane_sub.setViewportView(panel_sub);
		panel_sub.setLayout(new BoxLayout(panel_sub, BoxLayout.Y_AXIS));
		
		//define main category listener
		ActionListener actionListener_main = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox current = (JCheckBox) arg0.getSource();
				String main = current.getText();
				if (current.isSelected()) {
					mainCategory.add(main);
				} else {
					if(mainCategory.contains(main)) {
						mainCategory.remove(main);
					}
				}
				try {
					subPanelUpdate(mainCategory, conn, panel_sub);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		};
		
		
		//Add main category check boxes
		JCheckBox activeLife = new JCheckBox("Active Life");
		//activeLife.addItemListener(itemListener_main);
		activeLife.addActionListener(actionListener_main);
		panel_main.add(activeLife);
		
		JCheckBox arts = new JCheckBox("Arts & Entertainment");
		//arts.addItemListener(itemListener_main);
		arts.addActionListener(actionListener_main);
		panel_main.add(arts);
		
		JCheckBox automotive = new JCheckBox("Automotive");
		//automotive.addItemListener(itemListener_main);
		automotive.addActionListener(actionListener_main);
		panel_main.add(automotive);
		
		JCheckBox carRental = new JCheckBox("Car Rental");
		//carRental.addItemListener(itemListener_main);
		carRental.addActionListener(actionListener_main);
		panel_main.add(carRental);
		
		JCheckBox cafes = new JCheckBox("Cafes");
		//cafes.addItemListener(itemListener_main);
		cafes.addActionListener(actionListener_main);
		panel_main.add(cafes);
		
		JCheckBox beautySpas = new JCheckBox("Beauty & Spas");
		//beautySpas.addItemListener(itemListener_main);
		beautySpas.addActionListener(actionListener_main);
		panel_main.add(beautySpas);
		
		JCheckBox convenienceStores = new JCheckBox("Convenience Stores");
		//convenienceStores.addItemListener(itemListener_main);
		convenienceStores.addActionListener(actionListener_main);
		panel_main.add(convenienceStores);
		
		JCheckBox dentists = new JCheckBox("Dentists");
		//dentists.addItemListener(itemListener_main);
		dentists.addActionListener(actionListener_main);
		panel_main.add(dentists);
		
		JCheckBox doctors = new JCheckBox("Doctors");
		//doctors.addItemListener(itemListener_main);
		doctors.addActionListener(actionListener_main);
		panel_main.add(doctors);
		
		JCheckBox durgstores = new JCheckBox("Drugstores");
		//durgstores.addItemListener(itemListener_main);
		durgstores.addActionListener(actionListener_main);
		panel_main.add(durgstores);
		
		JCheckBox departmentStores = new JCheckBox("Department Stores");
		//departmentStores.addItemListener(itemListener_main);
		departmentStores.addActionListener(actionListener_main);
		panel_main.add(departmentStores);
		
		JCheckBox education = new JCheckBox("Education");
		//education.addItemListener(itemListener_main);
		education.addActionListener(actionListener_main);
		panel_main.add(education);
		
		JCheckBox eventPlanning = new JCheckBox("Event Planning & Services");
		//eventPlanning.addItemListener(itemListener_main);
		eventPlanning.addActionListener(actionListener_main);
		panel_main.add(eventPlanning);
		
		JCheckBox flowers = new JCheckBox("Flowers & Gifts");
		//flowers.addItemListener(itemListener_main);
		flowers.addActionListener(actionListener_main);
		panel_main.add(flowers);
		
		JCheckBox food = new JCheckBox("Food");
		//Food.addItemListener(itemListener_main);
		food.addActionListener(actionListener_main);
		panel_main.add(food);
		
		JCheckBox health = new JCheckBox("Health & Medical");
		//health.addItemListener(itemListener_main);
		health.addActionListener(actionListener_main);
		panel_main.add(health);
		
		JCheckBox homeServices = new JCheckBox("Home Services");
		//homeServices.addItemListener(itemListener_main);
		homeServices.addActionListener(actionListener_main);
		panel_main.add(homeServices);
		
		JCheckBox homeGarden = new JCheckBox("Home & Garden");
		//homeGarden.addItemListener(itemListener_main);
		homeGarden.addActionListener(actionListener_main);
		panel_main.add(homeGarden);
		
		JCheckBox hospitals = new JCheckBox("Hospitals");
		//hospitals.addItemListener(itemListener_main);
		hospitals.addActionListener(actionListener_main);
		panel_main.add(hospitals);
		
		JCheckBox hotels = new JCheckBox("Hotels & Travel");
		//hotels.addItemListener(itemListener_main);
		hotels.addActionListener(actionListener_main);
		panel_main.add(hotels);
		
		JCheckBox hardwareStores = new JCheckBox("Hardware Stores");
		//hardwareStores.addItemListener(itemListener_main);
		hardwareStores.addActionListener(actionListener_main);
		panel_main.add(hardwareStores);
		
		JCheckBox grocery = new JCheckBox("Grocery");
		grocery.addActionListener(actionListener_main);
		//grocery.addItemListener(itemListener_main);
		panel_main.add(grocery);
		
		JCheckBox medicalCenters = new JCheckBox("Medical Centers");
		//medicalCenters.addItemListener(itemListener_main);
		medicalCenters.addActionListener(actionListener_main);
		panel_main.add(medicalCenters);
		
		JCheckBox nurseries = new JCheckBox("Nurseries & Gardening");
		//nurseries.addItemListener(itemListener_main);
		nurseries.addActionListener(actionListener_main);
		panel_main.add(nurseries);
		
		JCheckBox nightlife = new JCheckBox("Nightlife");
		//nightlife.addItemListener(itemListener_main);
		nightlife.addActionListener(actionListener_main);
		panel_main.add(nightlife);
		
		JCheckBox restaurants = new JCheckBox("Restaurants");
		//restaurants.addItemListener(itemListener_main);
		restaurants.addActionListener(actionListener_main);
		panel_main.add(restaurants);
		
		JCheckBox shopping = new JCheckBox("Shopping");
		//shopping.addItemListener(itemListener_main);
		shopping.addActionListener(actionListener_main);
		panel_main.add(shopping);
		
		JCheckBox transportation = new JCheckBox("Transportation");
		//transportation.addItemListener(itemListener_main);
		transportation.addActionListener(actionListener_main);
		panel_main.add(transportation);
		
		
		JPanel panel_user = new JPanel();
		panel_user.setBorder(new TitledBorder(null, "Users", TitledBorder.CENTER, TitledBorder.TOP, null, Color.BLUE));
		panel_user.setBounds(0, 249, 291, 153);
		frame.getContentPane().add(panel_user);
		panel_user.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Member since:");
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 10));
		lblNewLabel.setBounds(7, 30, 70, 13);
		panel_user.add(lblNewLabel);
		
		JLabel lblReviewCount = new JLabel("Review count:");
		lblReviewCount.setFont(new Font("Arial", Font.PLAIN, 10));
		lblReviewCount.setBounds(7, 57, 67, 13);
		panel_user.add(lblReviewCount);
		
		JLabel lblNumOfFriends = new JLabel("Num. of friends:");
		lblNumOfFriends.setFont(new Font("Arial", Font.PLAIN, 10));
		lblNumOfFriends.setBounds(10, 80, 74, 13);
		panel_user.add(lblNumOfFriends);
		
		JLabel lblAvgStars = new JLabel("Avg. stars:");
		lblAvgStars.setFont(new Font("Arial", Font.PLAIN, 10));
		lblAvgStars.setBounds(10, 103, 54, 15);
		panel_user.add(lblAvgStars);
		
		JLabel lblSelect = new JLabel("Select:");
		lblSelect.setFont(new Font("Arial", Font.PLAIN, 10));
		lblSelect.setBounds(10, 128, 54, 15);
		panel_user.add(lblSelect);
		
		//query all the available publish date from DB
		String querySince = "SELECT DISTINCT since FROM y_user ORDER BY since ASC";
		ResultSet resultSince = statement.executeQuery(querySince);
		ArrayList<String> sinceDate = new ArrayList<>();
		while (resultSince.next()) {
			String date = resultSince.getString(1);
			sinceDate.add(date);
		}
		
		comboBox_since = new JComboBox<>();
		comboBox_since.setFont(new Font("Arial", Font.PLAIN, 10));
		for (String date : sinceDate) {
			comboBox_since.addItem(date);
		}
		comboBox_since.setBounds(87, 26, 150, 18);
		panel_user.add(comboBox_since);
		
		comboBox_AvgStars = new JComboBox<>();
		comboBox_AvgStars.setModel(new DefaultComboBoxModel(new String[] {"=", ">", "<"}));
		comboBox_AvgStars.setBounds(87, 100, 32, 21);
		panel_user.add(comboBox_AvgStars);
		
		comboBox_numFriend = new JComboBox<>();
		comboBox_numFriend.setModel(new DefaultComboBoxModel(new String[] {"=", ">", "<"}));
		comboBox_numFriend.setBounds(87, 75, 32, 21);
		panel_user.add(comboBox_numFriend);
		
		comboBox_userReviewCount = new JComboBox<>();
		comboBox_userReviewCount.setModel(new DefaultComboBoxModel(new String[] {"=", ">", "<"}));
		comboBox_userReviewCount.setBounds(87, 53, 32, 21);
		panel_user.add(comboBox_userReviewCount);
		
		JLabel lblNewLabel_1 = new JLabel("value:");
		lblNewLabel_1.setFont(new Font("Arial", Font.PLAIN, 10));
		lblNewLabel_1.setBounds(156, 57, 29, 13);
		panel_user.add(lblNewLabel_1);
		
		JLabel label = new JLabel("value:");
		label.setFont(new Font("Arial", Font.PLAIN, 10));
		label.setBounds(156, 80, 29, 13);
		panel_user.add(label);
		
		JLabel label_1 = new JLabel("value:");
		label_1.setFont(new Font("Arial", Font.PLAIN, 10));
		label_1.setBounds(156, 104, 29, 12);
		panel_user.add(label_1);
		
		textField_reviewCountNum = new JTextField();
		textField_reviewCountNum.setBounds(194, 52, 43, 18);
		panel_user.add(textField_reviewCountNum);
		textField_reviewCountNum.setColumns(10);
		
		textField_friendNun = new JTextField();
		textField_friendNun.setColumns(10);
		textField_friendNun.setBounds(195, 75, 43, 18);
		panel_user.add(textField_friendNun);
		
		textField_avgStar = new JTextField();
		textField_avgStar.setColumns(10);
		textField_avgStar.setBounds(195, 99, 43, 18);
		panel_user.add(textField_avgStar);
		
		comboBox_userSelect = new JComboBox<>();
		comboBox_userSelect.setFont(new Font("Arial", Font.PLAIN, 12));
		comboBox_userSelect.setModel(new DefaultComboBoxModel(new String[] {"AND", "OR"}));
		comboBox_userSelect.setBounds(87, 125, 51, 21);
		panel_user.add(comboBox_userSelect);	
		
		scrollPane_result = new JScrollPane();
		scrollPane_result.setViewportBorder(new TitledBorder(null, "Result", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		scrollPane_result.setBounds(593, 10, 215, 392);
		frame.getContentPane().add(scrollPane_result, BorderLayout.CENTER);
		
		panel_result = new JPanel();
		scrollPane_result.setViewportView(panel_result);
		panel_result.setLayout(new BoxLayout(panel_result, BoxLayout.Y_AXIS));
		
		JScrollPane scrollPane_select = new JScrollPane();
		scrollPane_select.setViewportBorder(new TitledBorder(null, "Selected Information ", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		scrollPane_select.setBounds(818, 10, 202, 392);
		frame.getContentPane().add(scrollPane_select);
		
		
		panel_select = new JPanel();
		scrollPane_select.setViewportView(panel_select);
		panel_select.setLayout(new BoxLayout(panel_select, BoxLayout.Y_AXIS));
		
	}

	
	private void subPanelUpdate(Set<String> mainCategory, Connection conn, JPanel subPanel ) throws SQLException {			
		subPanel.removeAll();
		subPanel.updateUI();
		Set<String> subCategoryList = new HashSet<>();
		ActionListener actionListener_sub = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox current = (JCheckBox) arg0.getSource();
				if (current.isSelected()) {
					subCategory.add(current.getText());
				} else {
					subCategory.remove(current.getText());
				}
			}
			
		};
		PreparedStatement pstm = null;
		String querySubCategory = "SELECT DISTINCT sub_category FROM business_category WHERE main_category = ? ORDER BY sub_category ASC";
		pstm = conn.prepareStatement(querySubCategory);
		for (String temp : mainCategory) {		
			pstm.setString(1, temp);			
			ResultSet subResult = pstm.executeQuery();
			ArrayList<String> subArray = new ArrayList<>();
			while (subResult.next()) {
				String sub = subResult.getString(1);
				subArray.add(sub);
			}
			for (String sub : subArray) {
				if (!subCategoryList.contains(sub)) {
					subCategoryList.add(sub);
					JCheckBox subCategory = new JCheckBox(sub);
					subPanel.add(subCategory);
					subPanel.revalidate();
					subCategory.addActionListener(actionListener_sub);
				}
			}
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
	
	
}

