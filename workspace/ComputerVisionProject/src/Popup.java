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
		setBounds(100, 100, 700, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		//dropdown menu
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(5, 5, 674, 20);
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Smoothing Filter", "Median Filter", "Sharpening Laplacian Filter", "High-boosting Filter", "Histogram Equalization", "FFT"}));
		
		//source image
		lblSourceImage = new JLabel();
		lblSourceImage.setBounds(33, 36, 347, 200);
		
		//changed image
		lblChangedImage = new JLabel();
		lblChangedImage.setBounds(356, 227, 303, 177);
		
		//perform the conversion
		btnConvert = new JButton("Convert");
		btnConvert.setBounds(5, 433, 674, 23);
		btnConvert.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e)
			  {
				  //do the conversion when button is pressed
				  convert();
			  }
			});
		contentPane.setLayout(null);
		
		//add to content pane
		contentPane.add(lblSourceImage);
		contentPane.add(lblChangedImage);
		contentPane.add(comboBox);
		contentPane.add(btnConvert);
		
	}
	
	//does the conversion
	public void convert(){
		String sourcePath = "C:\\Users\\parkin\\Documents\\GitHub\\Computer-Vision-Work\\workspace\\ComputerVisionProject\\src\\BigTree.jpg";
		Mat imageMat = getImage(sourcePath);
		
		//imageMat = getHE(imageMat, 0);
		imageMat = getSmoothing(imageMat, 3);
		
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


	public Mat getSmoothing(Mat input, int filterSize){
		Mat output = input.clone();
		int width = (int) (input.total());
		int height = (int) (input.channels());
		int totalSize = (int) (input.total() * input.channels());
		double[] pixels = new double[totalSize];
		double[] newPixels = new double[totalSize];
		input.convertTo(input, CvType.CV_64FC3);
		input.get(0, 0, pixels);
		
		//set default filter size
		if(filterSize < 3){
			filterSize = 3;
		}

		//traverse pixels
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				int position = (j * width) + i;
				int newValue = 0;
				
				//average the pixels
				for(int a = 0; a < filterSize; a++){
					for(int b = 0; b < filterSize; b++){
						int x = (a - filterSize / 2);
						int y = (b - filterSize / 2) * width;
						int newPosition = position + x + y;
						
						if(newPosition >= 0 && newPosition < totalSize){
							newValue += pixels[newPosition];
						}
					}
				}
				
				newPixels[position] = (newValue / ((filterSize) * (filterSize)));
				//newPixels[position] = (pixels[position] + 1);
			}
		}
		
		/*byte[][] filterArray = new byte[filterSize][filterSize];
		
		//build filter
		for(int i = 0; i < filterSize; i++){
			for(int j = 0; j < filterSize; j++){
				filterArray[i][j] = 1;
			}
		}
		
		//missing edge size
		int edgeSize = filterSize/2;
		
		//traverse each pixel
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				//create the array
				byte[][] pixelArray = new byte[filterSize][filterSize];
				
				//traversing new array
				for(int a = 0; a < filterSize; a++){
					for(int b = 0; b < filterSize; b++){
						int x = (i - filterSize / 2) - (a - filterSize / 2);
						int y = (j - filterSize / 2) - (b - filterSize / 2);
						
						if(x < 0 || y < 0){
							pixelArray[a][b] = 0;
						}
						else{
							pixelArray[a][b] = pixels[((width * y) + x)];
						}
					}
				}
				
				//set the new value
				newPixels[((width * j) + i)] = get2dMulti(pixelArray,filterArray);
			}
		}*/

		output.put(0, 0, newPixels);
		
		return output;
	}
	
	public Mat getMedian(Mat input, int filterSize){
		return null;
	}
	
	public Mat getSharpeningLaplacian(Mat input, int filterSize){
		return null;
	}
	
	public Mat getHighBoosting(Mat input, int filterSize){
		return null;
	}
	
	public Mat getFFT(Mat input){
		return null;
	}
	
	public byte get2dMulti(byte[][] pixelArray, byte[][] filter){
		int value = 0;
		int size = pixelArray.length; 
		
		for(int i = 0; i < size; i++){
			int total = 0;
			for(int j = 0; j < size; j++){
				total += pixelArray[j][i] * filter[i][j];
			}
			
			value += total;
		}
		
		return (byte)(value / size);
	}
	
	public byte[][] getPixelMatrix(byte[][] input, int currentX, 
									int currentY, int size){
		byte[][] output = new byte[size][size];
		
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
