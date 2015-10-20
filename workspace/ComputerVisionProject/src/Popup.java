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
import javax.swing.JTextField;

public class Popup extends JFrame {

	private JPanel contentPane;
	private JLabel lblSourceImage;
	private JLabel lblChangedImage;
	private JButton btnConvert;
	private JComboBox comboBox;
	private JTextField txtSize;

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
		comboBox = new JComboBox();
		comboBox.setBounds(5, 5, 674, 20);
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Smoothing Filter", "Median Filter", "Sharpening Laplacian Filter", "High-boosting Filter", "Histogram Equalization", "FFT"}));
		
		//if a particular item is selected
		comboBox.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        JComboBox combo = (JComboBox)e.getSource();
                        String currentQuantity = (String)combo.getSelectedItem();
                        System.out.println(currentQuantity);
                    }
                }            
        );
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
		
		//input box of size
		txtSize = new JTextField();
		txtSize.setBounds(559, 55, 86, 20);
		contentPane.add(txtSize);
		txtSize.setColumns(10);
		
	}
	
	//does the conversion
	public void convert(){
		String sourcePath = "C:\\Users\\parkin\\Documents\\GitHub\\Computer-Vision-Work\\workspace\\ComputerVisionProject\\src\\BigTree.jpg";
		Mat imageMat = getImage(sourcePath);
		Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
		
		Mat outputMat = imageMat.clone();
		
		//check what is selected
		if(comboBox.getSelectedItem().toString().equals("Smoothing Filter")){
			outputMat = getSmoothing(imageMat, 3);
		}else if(comboBox.getSelectedItem().toString().equals("Median Filter")){
			outputMat = getMedian(imageMat, 3);
		}else if(comboBox.getSelectedItem().toString().equals("Sharpening Laplacian Filter")){
			outputMat = getSharpeningLaplacian(imageMat, 1);
		}else if(comboBox.getSelectedItem().toString().equals("High-boosting Filter")){
			outputMat = getHighBoosting(imageMat, 7);
		}else if(comboBox.getSelectedItem().toString().equals("Histogram Equalization")){
			outputMat = getHE(imageMat, 0);			
		}else if(comboBox.getSelectedItem().toString().equals("FFT")){
			
		}
		
		//displays the images
		lblSourceImage.setIcon(new ImageIcon(toBufferedImage(imageMat)));
		lblChangedImage.setIcon(new ImageIcon(toBufferedImage(outputMat)));
		
	}
	
	//get local image using opencv
	public Mat getImage(String filename){

		Mat a = Imgcodecs.imread(filename);
		
		return a;
	}
	
	public Mat getHE(Mat input, int filterSize){
		//variables
		Mat inMat = input.clone();
		Mat output = input.clone();
		int maxValue = 511;
		int size = (int) (input.total() * input.channels());
		double[] pixels = new double[size];
		double[] newPixels = new double[size];
		int[] newCounterArray = new int[maxValue];
		int[] counterArray = new int[maxValue];
		double[] probArray = new double[maxValue];
		//Imgproc.equalizeHist(inMat, output);
		inMat.convertTo(inMat, CvType.CV_64FC3);
		inMat.get(0, 0, pixels);
		
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
			newPixels[i] = (double)(newCounterArray[(int)pixels[i] + maxValue/2] - maxValue/2);
		}
		
		output.put(0, 0, newPixels);
		
		return output;
	}

	public Mat getSmoothing(Mat input, int filterSize){
		Mat inMat = input.clone();
		Mat output = input.clone();
		int height = (int) (input.total());
		int width = (int) (input.channels());
		int totalSize = width * height;
		double[] pixels = new double[totalSize];
		double[] newPixels = new double[totalSize];
		inMat.convertTo(inMat, CvType.CV_64FC3);
		inMat.get(0, 0, pixels);
		
		//make sure it is odd
		if(filterSize % 2 == 0){
			filterSize += 1;
		}
		
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
			}
		}
		

		output.put(0, 0, newPixels);
		
		return output;
	}
	
	public Mat getMedian(Mat input, int filterSize){
		Mat inMat = input.clone();
		Mat output = input.clone();
		int height = (int) (input.total());
		int width = (int) (input.channels());
		int totalSize = width * height;
		double[] pixels = new double[totalSize];
		double[] newPixels = new double[totalSize];
		inMat.convertTo(inMat, CvType.CV_64FC3);
		inMat.get(0, 0, pixels);
		
		//make sure it is odd
		if(filterSize % 2 == 0){
			filterSize += 1;
		}
		
		//set default filter size
		if(filterSize < 3){
			filterSize = 3;
		}

		//traverse pixels
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				int position = (j * width) + i;
				int max = Integer.MIN_VALUE;
				int min = Integer.MAX_VALUE;
				int newValue = 0;
				
				//average the pixels
				for(int a = 0; a < filterSize; a++){
					for(int b = 0; b < filterSize; b++){
						int x = (a - filterSize / 2);
						int y = (b - filterSize / 2) * width;
						int newPosition = position + x + y;
						
						if(newPosition >= 0 && newPosition < totalSize){
							if(pixels[newPosition] > max){
								max = (int)pixels[newPosition];
							}
							else if(pixels[newPosition] < min){
								min = (int)pixels[newPosition];
							}
						}
					}
				}
				
				newValue = (max + min) / 2;
				
				newPixels[position] = newValue;
			}
		}
		
		output.put(0, 0, newPixels);
		
		return output;
	}
	
	public Mat getSharpeningLaplacian(Mat input, int cValue){
		Mat inMat = input.clone();
		Mat output = input.clone();
		int height = (int) (input.total());
		int width = (int) (input.channels());
		int totalSize = width * height;
		int filterSize = 3;
		double[] pixels = new double[totalSize];
		double[] newPixels = new double[totalSize];
		inMat.convertTo(inMat, CvType.CV_64FC3);
		inMat.get(0, 0, pixels);
		
		
		//using
		//| -1  -1  -1 |
		//| -1   8  -1 |
		//| -1  -1  -1 |
		
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

						if(a == (filterSize / 2) && b == (filterSize / 2)){
							if(cValue == 1){
								newValue += (8 * pixels[newPosition]);
							}else{
								newValue += ((8 * cValue + 1) * pixels[newPosition]);
							}
						}
						else if(newPosition >= 0 && newPosition < totalSize){
							newValue += (-1 * cValue * pixels[newPosition]);
						}
					}
				}
				
				newPixels[position] = newValue;
			}
		}
		output.put(0, 0, newPixels);
		
		return output;
	}
	
	public Mat getHighBoosting(Mat input, int cValue){
		return getSharpeningLaplacian(input, cValue);
	}
	
	public Mat getFFT(Mat input){
		return null;
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
