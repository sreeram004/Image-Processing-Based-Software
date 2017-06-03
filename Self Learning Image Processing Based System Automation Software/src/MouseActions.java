import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

public class MouseActions {

	
	static int displayWidth, displayHeight, xAdjust, yAdjust; // variable for finding adjustements required
	static int oldX, oldY;
	static Robot rbt;
	
	public MouseActions() throws AWTException { // constructor to find the adjustements required

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // get the screen resolution
		displayWidth = screenSize.width; // get Width
		displayHeight = screenSize.width; // get Height

		xAdjust = (int) (displayWidth / 680); // find adjustment factors based on the frame size (680x380)
		yAdjust = (int) (displayHeight / 380);

		rbt = new Robot(); // instantiate the robot class
	}
	public static void movePointer(int pos[]) throws Exception { // method to mouse pointer


		int X = (int) pos[0] * xAdjust; int Y = (int) pos[1] * yAdjust; // applying adjustments based on screen


		if(Y < 500)
			Y -= 34;
		else
			Y += 80;

		if(X < 800)
			X -= 44;
		else
			X += 134;
		
		/* Code to reduce flickering*/
		if(Math.abs(oldX - X) > 10 || Math.abs(oldY - Y) > 10) {
			oldX = X; oldY = Y;
			
		}else{
			X = oldX; Y = oldY;
		}

		rbt.mouseMove(displayWidth - X, Y); // move the mouse applying rotation required
	}

	public static void leftClick() throws Exception { // method to perform left click
	
		rbt.mousePress(InputEvent.BUTTON1_MASK); // perform left click
		rbt.delay(500); // give a delay of provided ms
		rbt.mouseRelease(InputEvent.BUTTON1_MASK); // release the click
	}
}
