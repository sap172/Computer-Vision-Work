import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.bytedeco.javacpp.opencv_video.BackgroundSubtractorMOG2;
import org.bytedeco.javacpp.opencv_videoio.CvCapture;
import org.bytedeco.javacpp.indexer.ByteIndexer;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_videoio.*;
import static org.bytedeco.javacpp.opencv_video.*;

public class FinalProject extends JFrame {

	private JPanel contentPane;
	private JLabel lblSourceImage;
	private JLabel lblChangedImage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FinalProject frame = new FinalProject();
					frame.setVisible(true);
	
					/*//test the image
					Mat img = frame.getImage("C:/Users/parkin/Documents/GitHub/Computer-Vision-Work/workspace/ComputerVisionProject/src/BigTree.jpg");
					
					//frame
					OpenCVFrameConverter.ToIplImage opencvConverter = new OpenCVFrameConverter.ToIplImage();
					Frame imageFrame = opencvConverter.convert(img);
					Java2DFrameConverter frameConverter = new Java2DFrameConverter();
					
					//displays the images
					frame.lblSourceImage.setIcon(new ImageIcon(frameConverter.getBufferedImage(imageFrame)));
					//frame.lblChangedImage.setIcon(new ImageIcon(frame.toBufferedImage(img)));*/

					String filename = "C:/Users/parkin/Documents/GitHub/Computer-Vision-Work/workspace/ComputerVisionFinalProject/data/bike.avi";
					CvCapture capture = cvCreateFileCapture(filename);
					FrameGrabber grabber = new OpenCVFrameGrabber(filename);
					//CanvasFrame canvasFrame = new CanvasFrame("Extracted Foreground");
					int width = (int)cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH);
					int height = (int)cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT);
					
					//canvasFrame.setCanvasSize(width, height);
					//canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					long delay = Math.round((double)1000.0 / (double)cvGetCaptureProperty(capture, CV_CAP_PROP_FPS));
					
					BackgroundSubtractorMOG2 bsmog = new BackgroundSubtractorMOG2();
					IplImage image = null;
					image = cvRetrieveFrame(capture);
					
					while(cvGrabFrame(capture) != 0 && image != null){
						System.out.println("IT GOT HERE!!");
						
						Mat foreground = new Mat();
						bsmog.apply(new Mat(image), foreground, 0.01);
						
						threshold(foreground, foreground, 128, 255, CV_THRESH_BINARY_INV);
						
						//canvasFrame.showImage(toBufferedImage(foreground));
						
						//delay
						Thread.sleep(delay);
						
						image = cvRetrieveFrame(capture);
						frame.lblSourceImage.setIcon(new ImageIcon(toBufferedImage(new Mat(image))));
					}
					
					cvReleaseCapture(capture);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Create the frame.
	 */
	public FinalProject() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		//source image
		lblSourceImage = new JLabel();
		lblSourceImage.setBounds(33, 36, 347, 200);
		
		//changed image
		lblChangedImage = new JLabel();
		lblChangedImage.setBounds(356, 227, 303, 177);

		contentPane.add(lblSourceImage);
		contentPane.add(lblChangedImage);
	}
	
	//get local image using javacv
	public Mat getImage(String filename){

		Mat a = imread(filename);
		
		return a;
	}
	
	//returns an Image to display
    public static Image toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        ByteIndexer idx = m.createIndexer();
        idx.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
        return image;
  }
}
