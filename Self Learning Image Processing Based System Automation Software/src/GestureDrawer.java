import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

@SuppressWarnings("serial")

public class GestureDrawer extends JPanel{

	public void draw(int x[], int y[]) throws Exception{

		BufferedImage bimage;
		bimage = ImageIO.read(getClass().getResource("res/whiteImage.jpg")); // open the white image to draw

		Graphics2D g2d = bimage.createGraphics(); // initialize the grapics2d object to draw rectangles
		g2d.setColor(Color.black); // set color to draw to black

		for(int i = 0; i < x.length; i++){

			g2d.fill(new Rectangle.Float(x[i], y[i], 30, 30)); // for all the found coordinates draw rectangle of 30x30
		}

		g2d.dispose(); // release the graphics object

		ImageIO.write(bimage, "JPG", new File("recordedGesture.jpg")); // save the drawn image as recordedGesture
		doFlip(); // rotate the image 

		bimage.flush(); // clear the memory usage for images

		OCR.detectText("recordedGesture.jpg"); // pass the image to OCR API to find the recorded text

	}

	private static void doFlip(){

		IplImage img = cvLoadImage("recordedGesture.jpg"); // open the image
		cvFlip(img, img, 1); // rotate the image

		cvSaveImage("recordedGesture.jpg",img); // save the image

		cvReleaseImage(img); // release the variable used
	}

}
