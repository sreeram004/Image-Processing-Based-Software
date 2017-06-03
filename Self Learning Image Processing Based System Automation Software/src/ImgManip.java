import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacv.*;

public class ImgManip {

	static Image img1, img, imgtemp;
	static ImageIcon imgIcon;
	static CanvasFrame canvas = new CanvasFrame("Web Cam"); // To show feedback for gesture
	
	static OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage(); // converter to convert frame to image
	static int pos[] = new int[2]; // declaring necessary variables
	static double m10, m01, aream;
	static double areaLimit = 5000; // max contour area to check
	static boolean feedback = true; // to hide / show feedback screen

	public static void setIcon() {
		
		try {
		    img = ImageIO.read(HciMain.class.getResource("res/mainicon.jpg"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		 imgtemp = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
		canvas.setIconImage(imgtemp);
	}
	
	public static int[]  coordinateCalculator(double maxArea, double area, CvScalar minc, CvScalar maxc, CvMemStorage storage,  
			CvSeq contor1, CvSeq contor2, IplImage img, IplImage imghsv, IplImage binimg, CvMoments moments, int green, int blue, int red) throws Exception{
		
		maxArea = 2000;
		area = 0;

		cvCvtColor(img,imghsv,CV_BGR2HSV); // converting rgb to hsv
		cvInRangeS(imghsv, minc, maxc, binimg); // comparing the hsv image for the given color range and saving it to binary image
		opencv_core.cvReleaseImage(imghsv); // relased hsv image to avoid memory leak

		cvFindContours(binimg, storage, contor1, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_LINK_RUNS, 
				cvPoint(0,0)); // finding all the contours in the given image, contour will be in countour1 variable

		contor2 = contor1; // moving contour1 for future use

		while(contor1 != null && !contor1.isNull()){ // iterating throught the contours to find the best contour in the matching range

			area = cvContourArea(contor1, CV_WHOLE_SEQ, 1); // calculating the contour area

			if(area > maxArea && area < areaLimit) // checking the with the area range
				maxArea = area;

			contor1 = contor1.h_next(); // taking next contour 
		}

		while(contor2 != null && !contor2.isNull()){ // using contour2 set all other contours to black

			area = cvContourArea(contor2,  CV_WHOLE_SEQ, 1);

			if(area < maxArea  && area < areaLimit){
				cvDrawContours(binimg, contor2, CV_RGB(0,0,0), CV_RGB(0,0,0), 0, CV_FILLED, 8, cvPoint(0,0)); // coloring the unwanted contours to black
			}

			contor2 = contor2.h_next();
		}

		cvMoments(binimg, moments, 1); // calculating the central moments, will be stored in the moments variable

		

		m10 = cvGetSpatialMoment(moments, 1, 0); // finding the X coordinate of the central moment
		m01 = cvGetSpatialMoment(moments, 0, 1); // finding the Y coordinate of the central moment
		aream = cvGetCentralMoment(moments, 0, 0); // finding the area of the central moment
		
		moments.deallocate(); // deallocating used variables to prevent memory leak
		storage.deallocate();

		pos[0] = (int) (m10/aream); // calculating the X and Y coordinates
		pos[1] = (int) (m01/aream);

		if(feedback == true) { // if the "SHOW FEEDBACK" option is selected show the binary image after rotating it
			canvas.setVisible(true);
			cvFlip(binimg, binimg, 1); // rotating the image by 180 degrees
			canvas.showImage(converter.convert(binimg));   
		}else{
			canvas.setVisible(false); //  if the "HIDE FEEDBACK" option is selected, dont show the image
		}
		opencv_core.cvReleaseImage(binimg); // release bionary image after use to prevent memory leaks

		return pos; // return the calculated X and Y coordinates

	}
}
