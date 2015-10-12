import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class Popup extends JFrame {

	private JPanel contentPane;
	private JLabel lblSourceImage;
	private JLabel lblChangedImage;
	private JButton btnConvert;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		//required for imread
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Popup frame = new Popup();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Popup() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 622, 482);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		//dropdown menu
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Smoothing Filter", "Median Filter", "Sharpening Laplacian Filter", "High-boosting Filter", "Histogram Equalization", "FFT"}));
		
		//source image
		lblSourceImage = new JLabel();
		lblSourceImage.setSize(234, 137);
		
		//changed image
		lblChangedImage = new JLabel();
		lblChangedImage.setSize(234, 137);
		
		//perform the conversion
		btnConvert = new JButton("Convert");
		btnConvert.setSize(100,20);
		btnConvert.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e)
			  {
				  //do the conversion when button is pressed
				  convert();
			  }
			});
		
		//add to content pane
		contentPane.add(lblSourceImage, BorderLayout.WEST);
		contentPane.add(lblChangedImage, BorderLayout.EAST);
		contentPane.add(comboBox,BorderLayout.NORTH);
		contentPane.add(btnConvert,BorderLayout.SOUTH);
		
	}
	
	//does the conversion
	public void convert(){
		String sourcePath = "C:\\Users\\parkin\\workspace\\ComputerVisionProject\\src\\BigTree.jpg";
		Mat imageMat = getImage(sourcePath);
		
		imageMat = getHE(imageMat, 0);
		
		
		//displays the images
		lblSourceImage.setIcon(new ImageIcon(sourcePath));
		lblChangedImage.setIcon(new ImageIcon(toBufferedImage(imageMat)));
		
	}
	
	//get local image using opencv
	public Mat getImage(String filename){

		Mat a = Imgcodecs.imread(filename);
		
		return a;
	}
	
	public Mat getHE(Mat input, int filterSize){
		//variables
		Mat output = input.clone();
		int maxValue = 256;
		int size = (int) (input.total() * input.channels());
		byte[] pixels = new byte[size];
		byte[] newPixels = new byte[size];
		int[] newCounterArray = new int[maxValue];
		int[] counterArray = new int[maxValue];
		double[] probArray = new double[maxValue];
		
		input.get(0, 0, pixels);
		
		//set arrays to zero
		for (int i = 0; i < maxValue; i++){
			counterArray[i] = 0;
			probArray[i] = 0;
		}

		//each pixel intensity
		for (int i = 0; i < size; i++){
			counterArray[(int)(pixels[i] + maxValue/2)]++;
		}
		
		//calculate p(r)
		for (int i = 0; i < maxValue; i++){
			probArray[i] = (counterArray[i] / (double)size);
		
		}
		
		//get new values
		double sum = 0;
		for (int i = 0; i < maxValue; i++){
			sum += counterArray[i];
			newCounterArray[i] = (int)(((maxValue - 1)/((double)size)) * sum);
		}

		//create newpixel array
		for (int i = 0; i < size; i++){
			newPixels[i] = (byte)(newCounterArray[pixels[i] + maxValue/2] - maxValue/2);
		}
		
		output.put(0, 0, newPixels);
		
		return output;
	}


	//returns an Image to display
    public Image toBufferedImage(Mat m){
      int type = BufferedImage.TYPE_BYTE_GRAY;
      if ( m.channels() > 1 ) {
          type = BufferedImage.TYPE_3BYTE_BGR;
      }
      int bufferSize = m.channels()*m.cols()*m.rows();
      byte [] b = new byte[bufferSize];
      m.get(0,0,b); // get all the pixels
      BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
      System.arraycopy(b, 0, targetPixels, 0, b.length);  
      return image;

  }


}