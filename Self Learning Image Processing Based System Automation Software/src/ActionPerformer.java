import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import java.util.Arrays;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class ActionPerformer {

	static int X[] = new int[10000], Y[] =  new int[10000];
	static int i = 0, j =0;

	static CvScalar gminc = cvScalar(30,110,40,0); // color code for green
	static CvScalar gmaxc = cvScalar(80,255,255,0);

	static CvScalar bminc = cvScalar(95,150,75,0);// color code for blue
	static CvScalar bmaxc = cvScalar(145,255,255,0);

	static CvScalar rminc = cvScalar(160,150,75,0); // color code for red
	static CvScalar rmaxc = cvScalar(180,255,255,0);

	static double maxArea = 1500, area = 0;

	static CvMemStorage storage; // variables for contour manipulation
	static CvSeq contor1 = new CvSeq(), contor2;
	static CvMoments moments;

	static Object lock = new Object();
	static boolean locked;

	static OpenCVFrameGrabber grabber1 = new OpenCVFrameGrabber(0); // grabber instance to grab frames
	static OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage(); // converter to convert frame to image
	static IplImage imghsv, binimg, img; // image variables

	static int mouseMoveCordinates[] = new int[2], gestureCordinates[] = new int[2], mouseClickCordinates[] = new int[2], pos[] = new int[2];

	static Frame frame1; // frame variable

	public static  void performAction() throws Exception {


		grabber1.setImageWidth(680); // setting capture resoltuion 680x380
		grabber1.setImageHeight(380);

		grabber1.start(); // starting capture

		Thread mainCapturer = new Thread(){

			public void run(){

				try {

					while (true) {

						moments = new CvMoments(Loader.sizeof(CvMoments.class)); // initializing variables

						storage = CvMemStorage.create();

						frame1 = grabber1.grab(); // capturing frame

						img = converter.convert(frame1); // converting frame to image

						cvSmooth(img,img,CV_GAUSSIAN,9,9,2,2); // smoothening the image removing small noises

						imghsv = cvCreateImage(cvGetSize(img),8,3); // converting img to hsv image

						binimg = cvCreateImage(cvGetSize(img),8,1); // converting img to binary image

						/* Invoking method to calculate coordinates for gesture - green */
						gestureCordinates = ImgManip.coordinateCalculator(maxArea, area, gminc, gmaxc, storage, contor1, contor2, img, imghsv, binimg, moments,  1, 0, 0);

						if(gestureCordinates[0] > 0 && gestureCordinates[1] > 0){ // checking whether green is found

							doGesture(); // invoking doGesture() method to handle gestures

							synchronized (lock) { // locking the main thread

								if(locked == true){

									lock.wait();
								
								}
							}

						}


						moments = new CvMoments(Loader.sizeof(CvMoments.class));  // re-initializing the variables
						storage = CvMemStorage.create();
					
						img = converter.convert(frame1);

						cvSmooth(img,img,CV_GAUSSIAN,9,9,2,2);

						imghsv = cvCreateImage(cvGetSize(img),8,3);


						binimg = cvCreateImage(cvGetSize(img),8,1);
						/* Invoking method to calculate coordinates for moving mouse - blue */
						mouseMoveCordinates = ImgManip.coordinateCalculator(maxArea, area, bminc, bmaxc, storage, contor1, contor2, img, imghsv, binimg, moments,  0, 1, 0);

						if(mouseMoveCordinates[0] > 0 && mouseMoveCordinates[1] > 0) // if blue is found
	
							MouseActions.movePointer(mouseMoveCordinates);
					

						moments = new CvMoments(Loader.sizeof(CvMoments.class)); // re-initializing the variables
						storage = CvMemStorage.create();
					
						img = converter.convert(frame1);

						cvSmooth(img,img,CV_GAUSSIAN,9,9,2,2);

						imghsv = cvCreateImage(cvGetSize(img),8,3);

						binimg = cvCreateImage(cvGetSize(img),8,1);

						mouseClickCordinates = ImgManip.coordinateCalculator(maxArea, area, rminc, rmaxc, storage, contor1, contor2, img, imghsv, binimg, moments,  0, 0, 1);


						if(mouseClickCordinates[0] > 0 && mouseClickCordinates[1] > 0) // if red is found
							
								MouseActions.leftClick();

					}

				} catch (Exception e) { }

			} 

		};

		mainCapturer.start();

	}

	static GestureDrawer d = new GestureDrawer();
	static IplImage img2;

	static Frame frame2;

	private static void doGesture() throws Exception {

		locked = true; // lock the thread

		Thread gestureCapturer = new Thread() {

			public void run(){

				try {

					long startTime = System.currentTimeMillis(); // setting current time in ms


					while ((System.currentTimeMillis() - startTime) < 5000) { // run while loop for 5sec

						moments = new CvMoments(Loader.sizeof(CvMoments.class)); // re-initialize the variables
						storage = CvMemStorage.create();

						frame2 = grabber1.grab(); // grab frame

						img2 = converter.convert(frame2); // convert frame to image

						cvSmooth(img2,img2,CV_GAUSSIAN,9,9,2,2); // apply smoothening

						imghsv = cvCreateImage(cvGetSize(img2),8,3); // img to hsv image

						binimg = cvCreateImage(cvGetSize(img2),8,1); // img to binary image
						
						/* Reusing the method to get coordinates for gestures - green */
						pos = ImgManip.coordinateCalculator(maxArea, area, gminc, gmaxc, storage, contor1, contor2, img2, imghsv, binimg, moments, 1, 0, 0);
						X[i++] =  pos[0]; Y[j++] =  pos[1]; // assign the cordinates to X and Y array for drawing image later

					}


					d.draw(X, Y); // after 5sec invoke draw() to draw the gesture

					Arrays.fill(X, 0); Arrays.fill(Y, 0); // reset the arrays to 0

					i = j = 0;

					synchronized (lock) { // release lock
						locked = false;
						lock.notifyAll(); 
					}


				} catch (Exception e) { // if any occured in the framing
						
				}


			} 

		};

		gestureCapturer.start();

	}
}
