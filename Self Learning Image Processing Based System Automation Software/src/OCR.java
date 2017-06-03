import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class OCR {
	/* Declaring variables for connecting to the database */
	
	static Statement stmt;
	static Connection conn;
	static Image img1, img, imgtemp;
	static ImageIcon imgIcon;
	
	public OCR() throws Exception {
		/* Make database connection */

		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/GESTURE_DATABASE?autoReconnect=true&useSSL=false";
		String user = "root";
		String pass = "sqlpass";

		Class.forName(driver);

		conn = DriverManager.getConnection(url, user, pass);

		stmt = conn.createStatement();


	}

	public static void detectText(String fileName) throws Exception{


		File imageFile = new File(fileName); // open the image that has gesture recorded in it


		ITesseract instance = new Tesseract(); // create object for Tesseract OCR 
		//	instance.setDatapath(".");

		try {
			char ch = 0;

			String result = instance.doOCR(imageFile); // do OCR on the image

			result = result.replace(" ", ""); // remove possible small noises from the result
			result = result.replace("\n", "");
			result = result.replace(".", "");
			result = result.replace(";", "");
			result = result.replace("'", "");
			result = result.replace("-", "");
			result = result.replace("_", "");
			result = result.replace("/", "");
			result = result.replace("|", "");
			result = result.replace("`", "");
			result = result.replace("~", "");
			result = result.replace("'", "");
			result = result.replace("\"", "");


			result = result.toUpperCase(); // convert the result to uppercase

			if(result.isEmpty()){ // if nothing is read

				JOptionPane.showMessageDialog(null, "I didn't get u, Tell me once more");
				return;
			}


			else// if some result is available take the first character from the result
				ch = result.charAt(0);

		
			checkDatabase(ch); // invoke method to check the database for the character



		}catch(Exception e){ // if any error occurs with Tesseract
	
			
		}


	}



	private static void checkDatabase(char capturedGesture) throws Exception { // method to check the database

		/* Querying the database to get all the data of capturedGesture in the CAPTURED_GESTURE table */
		ResultSet rs = stmt.executeQuery("SELECT * FROM RECORDED_GESTURES WHERE CAPTURED_GESTURE = '"+capturedGesture+"';");

		String code = "";

		while (rs.next()) { // iterate through the Resultset and find the code corresponding to it

			code = rs.getString("CODE_TO_EXE"); // set the code

		}
		if(code.equals("")) // if there is no entry in the database invoke newGesture() method
			newGesture(capturedGesture);
		else
			executeCommand(code); // if entry is found invoke executeCommand() to execute the corresponding code
	}

	private static void newGesture(char capturedGesture) throws Exception {

		char correct_Gesture = 0;

		JFrame questionFrame = new JFrame("New Gesture Captured"); // tell the user that new gesture found 

		int optionSelected = JOptionPane.showConfirmDialog( // read the user feedback either correct or wrong (yes / no)
				questionFrame, 
				"I detected "+capturedGesture+" am I correct.?" 

				);

		questionFrame.dispose(); 

		if(optionSelected == JOptionPane.NO_OPTION){ // if user presses no

			JFrame noFrame = new JFrame("Enter Gesture"); // read the correct gesture from the user

			String correctGesture = JOptionPane.showInputDialog(
					noFrame, 
					"Tell me what that was", 
					"Enter the gesture", 
					JOptionPane.WARNING_MESSAGE
					);

			
			if(correctGesture.equals("")){
				JOptionPane.showMessageDialog(null, "Gesture cant be empty");
				return;
			}
			else{

				correct_Gesture = correctGesture.charAt(0);

				String codeToExe = getCode(); // invoke getCode() method for getting the code
				if(codeToExe.equals("")){
					JOptionPane.showMessageDialog(null, "Code cant be empty");
					return;
				}
				else{ // if the code entered is not null record it to the database

					stmt.executeUpdate("INSERT INTO RECORDED_GESTURES"
							+ "(CAPTURED_GESTURE, CORRECT_GESTURE, CODE_TO_EXE)"
							+ "VALUES"
							+ "('"+capturedGesture+"', '"+correct_Gesture+"', '"+codeToExe+"');"
							);

					executeCommand(codeToExe);
				}
			}

		}

		else if(optionSelected == JOptionPane.YES_OPTION){ // if yes is selected

			correct_Gesture = capturedGesture; // set correct_Gesture

			String codeToExe = getCode();  // get the code to execute
			if(codeToExe.equals("")){
				JOptionPane.showMessageDialog(null, "Code cant be empty");
				return;
			}
			
			else{ // if the code is not null

				stmt.executeUpdate("INSERT INTO RECORDED_GESTURES"
						+ "(CAPTURED_GESTURE, CORRECT_GESTURE, CODE_TO_EXE)"
						+ "VALUES"
						+ "('"+capturedGesture+"', '"+correct_Gesture+"', '"+codeToExe+"');"
						);

				executeCommand(codeToExe); // invoke method to execute the code
			}

		}

	}

	public static void manageDatabase() throws Exception{ // method to manage the database

		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); // vector variables to maintain the table data
		Vector <String> column = new Vector<>(); 

		JFrame dFrame = new JFrame(""); // create frame
		dFrame.setSize(500, 500);
		dFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		 
		try {
		    img = ImageIO.read(HciMain.class.getResource("res/mainicon.jpg"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		 imgtemp = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
		dFrame.setIconImage(imgtemp);

		JPanel controlPane = new JPanel(); // create 2 panels. 1 for table and 1 for buttons
		JPanel buttonPane = new JPanel();

		controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.PAGE_AXIS)); // set layout for table panels
		controlPane.setPreferredSize(new Dimension(500, 400)); // set size



		ResultSet rs = stmt.executeQuery("SELECT * FROM RECORDED_GESTURES"); // query the database for getting all the data

		ResultSetMetaData rsmd = rs.getMetaData(); // get the metadata from the table data
		int count = rsmd.getColumnCount(); // find the coloumn count
		
		for (int i = 1; i <= count; i++) { // iterate and add the column names to column vector

			column.add(rsmd.getColumnName(i));
		}

		while (rs.next()) { //iterate and add each record to object vector

			Vector<Object> vector = new Vector<Object>();

			for (int columnIndex = 1; columnIndex <= count; columnIndex++) {
				vector.add(rs.getObject(columnIndex));
			}
			data.add(vector);
		}

		JTable dTable = new JTable(data, column); // create dTable
		dTable.setBackground(Color.LIGHT_GRAY); // set Background

		JScrollPane scrollPane = new JScrollPane(dTable); // make scrollpane to scrool the table
		dTable.setFillsViewportHeight(true); // make the table fill the frame

		controlPane.add(scrollPane); // add the scrollpane to table

		JButton edit = new JButton("EDIT"); // make edit button

		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER)); // add the button to jpane for button
		buttonPane.setPreferredSize(new Dimension(100,40)); // set size

		/* If edit button is clicked show new frame with fields and buttons to edit the database*/
		edit.addActionListener(new ActionListener(){ // set actionListener


			public void actionPerformed(ActionEvent ae) {

				JFrame editFrame = new JFrame(""); // create new frame
				editFrame.setSize(500, 500);
				
		
				 
				try {
				    img = ImageIO.read(HciMain.class.getResource("res/mainicon.jpg"));
				} catch (IOException e) {
				    e.printStackTrace();
				}
				 imgtemp = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
				 editFrame.setIconImage(imgtemp);

				editFrame.setLayout(new GridLayout(9, 2, 2, 2)); // insert layout
				JLabel rg = new JLabel("Enter the recorded gesture : "); // make label that ask the user for gesture to edit
				editFrame.add(rg); // add it to the frame.
				
				JTextField rrg = new JTextField(2); // make text filed for the user to enter the gesture to edit
				editFrame.add(rrg); // add it to the frame

				JTextField cg = new JTextField(2); // make text fiels for showing the gesture and code
				JTextField code = new JTextField(150);


				JButton validate = new JButton("VALIDATE"); // make a button to check the entered gesture in the database
				validate.addActionListener(new ActionListener(){ // addActionListener for the button

					public void actionPerformed(ActionEvent ae) {
						if(rrg.getText().equals("")) {
							JOptionPane.showMessageDialog(null, "Insufficient Data, Enter the gesture to check");
							cg.setText(""); code.setText(""); rrg.setText("");
							return;
						}
						try{ // Quey the database for the data of that specific gesture
							ResultSet rs = stmt.executeQuery("SELECT * FROM RECORDED_GESTURES WHERE CAPTURED_GESTURE = '"+rrg.getText()+"';");
							
							if(rs.next()) {// if found set the textfields with the data from the database
								cg.setText(""+rs.getString(2)); code.setText(""+rs.getString(3)); 
							}else{ // if not found display error
								JOptionPane.showMessageDialog(null, "Error.! Gesture not found in the database");
								cg.setText(""); code.setText(""); rrg.setText("");
							}

						}catch(SQLException e){ // else error
				
						} 
						
					}

				});
				editFrame.add(validate); //add button to frame
				JLabel labelCorrect = new JLabel("Correct Gesture : ");
				editFrame.add(labelCorrect);
				editFrame.add(cg); // add the textFields to the frame
				JLabel labelCode = new JLabel("Code to Execute : ");
				editFrame.add(labelCode);
				editFrame.add(code);

				JButton update = new JButton("SUBMIT"); // create button for submit
				try {
				    img = ImageIO.read(HciMain.class.getResource("res/submit.png"));
				} catch (IOException e) {
				    e.printStackTrace();
				}
				 imgtemp = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
				 imgIcon = new ImageIcon(imgtemp);
				update.setIcon(imgIcon);
				
				update.addActionListener(new ActionListener(){ // add actionListener

					public void actionPerformed(ActionEvent tae) {
						if(rrg.getText().equals("") || cg.getText().equals("") || code.getText().equals("")) {
							JOptionPane.showMessageDialog(null, "Insertion Failed, Insufficient Data");
							return;
						}
						try { // update the gesture based on the user entered data
							stmt.executeUpdate("UPDATE RECORDED_GESTURES " +
									"SET CORRECT_GESTURE = '"+cg.getText()+"', CODE_TO_EXE = '"+code.getText()+"' WHERE CAPTURED_GESTURE = '"+rrg.getText()+"';");
							JOptionPane.showMessageDialog(null, "Updated Successfully");
							cg.setText(""); code.setText(""); rrg.setText("");
						} catch (SQLException e) { // on error show error frame
							
							JOptionPane.showMessageDialog(null, "Update Failed");
						}

					}

				});

				editFrame.add(update); // add to the frame
				
				JButton delete = new JButton("DELETE"); // create delete button to deleter the records from the database
				try {
				    img = ImageIO.read(HciMain.class.getResource("res/delete.png"));
				} catch (IOException e) {
				    e.printStackTrace();
				}
				 imgtemp = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
				 imgIcon = new ImageIcon(imgtemp);
				delete.setIcon(imgIcon);
				
				delete.addActionListener(new ActionListener() { // add actionListener
					
					public void actionPerformed(ActionEvent ae) {
						
						if(rrg.getText().equals("")){
						
							JOptionPane.showMessageDialog(null, "Deletion Failed, Insufficient Data");
							return;
						}
						try { // delete the specified gesture
							
							stmt.executeUpdate("DELETE FROM RECORDED_GESTURES WHERE CORRECT_GESTURE = '"+cg.getText()+"';");
							cg.setText(""); code.setText(""); rrg.setText("");
							JOptionPane.showMessageDialog(null, "Deleted Succesfully");
						} catch (SQLException e) { // if gesture no found in the database show error
							
							JOptionPane.showMessageDialog(null, "Deletion Failed");
						}
					}
				});
				
				editFrame.add(delete); // add button to the frame
				editFrame.setVisible(true); // make the frame visible
			}

		});
		buttonPane.add(edit); // add to the btton jpanels
		

		dFrame.add(controlPane, BorderLayout.NORTH); // add the jpanels to jframe
		dFrame.add(buttonPane, BorderLayout.SOUTH);

		dFrame.pack(); // pack the jpanels

		dFrame.setVisible(true); // make dFrame visible

	}

	private static String getCode(){ // method to read the code from the user

		JFrame codeFrame = new JFrame("Code");

		String codeToExe = JOptionPane.showInputDialog(
				codeFrame, 
				"Enter the code to execute", 
				"Enter the code", 
				JOptionPane.QUESTION_MESSAGE
				);
		
		codeFrame.dispose();
		
		
			return codeToExe; // return the read code

	}

	private static void executeCommand(String code) throws Exception{

		Runtime rt = Runtime.getRuntime(); // create instance of runtime

		rt.exec(code); // execute the code


	}



}