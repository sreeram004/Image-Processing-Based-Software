import java.awt.*;
import java.awt.event.*;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;


public class HciMain {

	static int fbclick = 0;
	static Image img1, img, imgtemp;
	static ImageIcon imgIcon;
	public static void main(String[] args) throws Exception {

		@SuppressWarnings("unused")
		OCR t = new OCR();	// To invoke constructor that makes database connection
		@SuppressWarnings("unused")
		MouseActions m = new MouseActions(); // To invoke mouse constructor for finding screen resolution
		
		
	
		JFrame mFrame = new JFrame(""); // create main frame
		mFrame.setSize(400, 500); // set size and layout
		mFrame.setLayout(new GridLayout(4, 2, 2, 2));
	
		try {
		    img = ImageIO.read(HciMain.class.getResource("res/mainicon.jpg"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		 imgtemp = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
		 mFrame.setIconImage(imgtemp);
			ImgManip.setIcon();
		JButton start = new JButton("START"); // create start button
		start.setFocusable(false);
	
		try {
		    img = ImageIO.read(HciMain.class.getResource("res/start.png"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		 imgtemp = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
		 imgIcon = new ImageIcon(imgtemp);
		start.setIcon(imgIcon);
		
		
		start.addActionListener(new ActionListener(){ // add the actionListener

			public void actionPerformed(ActionEvent ae){

				try {

					ActionPerformer.performAction(); // invoke the static performAction() method 
				} catch (Exception e) { // on any error show error message

					JOptionPane.showMessageDialog(mFrame, "Error.! Can't open the camera");
				}

				mFrame.setState(Frame.ICONIFIED); // to minimize the jframe
			}
		});

		mFrame.add(start); // add to the frame
	
		JButton manage = new JButton("MANAGE"); // create button to manage the database
		manage.setFocusable(false);
		try {
		    img = ImageIO.read(HciMain.class.getResource("res/manage.png"));
		} catch (IOException e) { }
		
		imgtemp = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		 imgIcon = new ImageIcon(imgtemp);
		manage.setIcon(imgIcon);
		
		manage.addActionListener(new ActionListener(){ // add the actionListener

			public void actionPerformed(ActionEvent ae){

				try { // invoke the static method manageDatabase()
					OCR.manageDatabase();
				} catch (Exception e) { // on error display the error message

					JOptionPane.showMessageDialog(mFrame, "Error.! Database unavailable");
				}
			}
		});
		
	
		
		mFrame.add(manage); // add the button to the frame
	
		JButton feedback = new JButton("HIDE FEEDBACK"); // make the button to toggle the feedback on / off
		feedback.setFocusable(false);
		
		try {
		    img = ImageIO.read(HciMain.class.getResource("res/hide.png"));
		} catch (IOException e) { }
		
		imgtemp = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		 imgIcon = new ImageIcon(imgtemp);
		feedback.setIcon(imgIcon);
		
		
		feedback.addActionListener(new ActionListener() { // add actionListener

			public void actionPerformed(ActionEvent ae) {

				fbclick++; // increment the fbclick variable

				if(fbclick % 2 == 0){ // if even number of clicks is done

					feedback.setText("HIDE FEEDBACK"); // change the name of the button
					ImgManip.feedback = true; // set the value so that other methods can know that feedback is on
					try {
					    img = ImageIO.read(HciMain.class.getResource("res/hide.png"));
					} catch (IOException e) { }
					
				    imgtemp = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
					imgIcon = new ImageIcon(imgtemp);
					feedback.setIcon(imgIcon);
				}
				else{ // if odd number of clicks is done

					ImgManip.feedback = false; // change the name of the button
					feedback.setText("SHOW FEEDBACK"); // set the value so that other methods can know that feedback is off
					try {
					    img = ImageIO.read(HciMain.class.getResource("res/show.png"));
					} catch (IOException e) { }
					
					imgtemp = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
					imgIcon = new ImageIcon(imgtemp);
					feedback.setIcon(imgIcon);
				}

			}
		});

		mFrame.add(feedback); // add to the frame
		
		JButton exit = new JButton("EXIT"); // create exit button
		exit.setFocusable(false);
		
		try {
		    img = ImageIO.read(HciMain.class.getResource("res/switch.png"));
		} catch (IOException e) { }
		
		imgtemp = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		imgIcon = new ImageIcon(imgtemp);
		exit.setIcon(imgIcon);
	
		exit.addActionListener(new ActionListener(){ // add actionListener 

			public void actionPerformed(ActionEvent ae){


				try {
					OCR.stmt.close(); // close the database connection
					mFrame.dispose(); // dispose the frame
				} catch (Exception e) { // on any error
					mFrame.dispose();// dispose the frame
					java.lang.System.exit(0); // stop the pgm
				}
				java.lang.System.exit(0); // stop the pgm
			}
		});

		mFrame.add(exit); // add to the frame
	
		mFrame.setVisible(true); // make frame visible
	}
}
