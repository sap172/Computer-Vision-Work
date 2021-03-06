import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

public class Popup extends JFrame {

	private JPanel contentPane;
	private JLabel lblSourceImage;
	private JLabel lblChangedImage;
	private JButton btnConvert;
	private JComboBox comboBox;
	private JTextField txtSize;
	private JCheckBox chckbxGlobal;
	private String sourcePath;
	
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
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Smoothing Filter", 
																 "Median Filter", 
																 "Sharpening Laplacian Filter", 
																 "High-boosting Filter", 
																 "Histogram Equalization", 
																 "FFT"}));
		
		//if a particular item is selected
		comboBox.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        JComboBox combo = (JComboBox)e.getSource();
                        String currentSelection = (String)combo.getSelectedItem();
                		
                        //text field
                        if(currentSelection.equals("Smoothing Filter") ||
                			currentSelection.equals("Median Filter") ||
                        	currentSelection.equals("Sharpening Laplacian Filter") ||
                        	currentSelection.equals("High-boosting Filter") ||
                        	currentSelection.equals("Histogram Equalization")){
                        	
                        	//display text field
                        	txtSize.setVisible(true);
                        }
                        else{
                        	//remove text field
                        	txtSize.setVisible(false);
                        }
                        
                        //checkbox
                        if(currentSelection.equals("Histogram Equalization")){
                        	chckbxGlobal.setVisible(true);
                        }else{
                        	chckbxGlobal.setVisible(false);
                        }
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
				  if(sourcePath != ""){
					  convert();
				  }
				  else{
					  System.out.println("Please input a file!");
				  }
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
		
		chckbxGlobal = new JCheckBox("Global");
		chckbxGlobal.setBounds(562, 95, 97, 23);
		chckbxGlobal.setVisible(false);
		contentPane.add(chckbxGlobal);
		
		
		//browse
		JButton btnBrowse = new JButton("Browse");
		final JFrame frame = this;
		sourcePath = "";
		btnBrowse.setBounds(5, 403, 674, 23);
		btnBrowse.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e)
			  {
				  	//get file
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
					int result = fileChooser.showOpenDialog(frame);
					if (result == JFileChooser.APPROVE_OPTION) {
					    File selectedFile = fileChooser.getSelectedFile();
					    sourcePath =  selectedFile.getAbsolutePath();
					}
			  }
			});
		contentPane.add(btnBrowse);
	}
	
	//does the conversion
	public void convert(){
		Mat imageMat = getImage(sourcePath);
		Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
		
		Mat outputMat = imageMat.clone();
		String input = txtSize.getText();
		
		if(!input.isEmpty()){
			try{		
				//check what is selected
				if(comboBox.getSelectedItem().toString().equals("Smoothing Filter")){
					int size = Integer.parseInt(input);
					outputMat = getSmoothing(imageMat, size);
				}else if(comboBox.getSelectedItem().toString().equals("Median Filter")){
					int size = Integer.parseInt(input);
					outputMat = getMedian(imageMat, size);
				}else if(comboBox.getSelectedItem().toString().equals("Sharpening Laplacian Filter")){
					outputMat = getSharpeningLaplacian(imageMat, 1);
				}else if(comboBox.getSelectedItem().toString().equals("High-boosting Filter")){
					int size = Integer.parseInt(input);
					outputMat = getHighBoosting(imageMat, size);
				}else if(comboBox.getSelectedItem().toString().equals("Histogram Equalization")){
					
					if(chckbxGlobal.isSelected()){
						//global HE
						outputMat = getHE(imageMat, 0);
					}else{
						int size = Integer.parseInt(input);
						//local HE
						outputMat = getHE(imageMat, size);		
					}	
				}else if(comboBox.getSelectedItem().toString().equals("FFT")){
					outputMat = getFFT(imageMat);
				}
				
				//displays the images
				lblSourceImage.setIcon(new ImageIcon(toBufferedImage(imageMat)));
				lblChangedImage.setIcon(new ImageIcon(toBufferedImage(outputMat)));
	
			}catch(Exception e){
				System.out.println("Please enter an Integer value!");
				e.printStackTrace();
			}
		}
		
	}
	
	//get local image using opencv
	public Mat getImage(String filename){

		Mat a = Highgui.imread(filename);
		
		return a;
	}
	
	public Mat getHE(Mat input, int filterSize){
		//variables
		Mat inMat = input.clone();
		Mat output = input.clone();
		int maxValue = 255;
		int size = (int) (input.total() * input.channels());
		double[] pixels = new double[size];
		double[] newPixels = new double[size];
		int[] newCounterArray = new int[maxValue];
		int[] counterArray = new int[maxValue];
		double[] probArray = new double[maxValue];
		
		inMat.convertTo(inMat, CvType.CV_64FC3);
		inMat.get(0, 0, pixels);
		
		//set arrays to zero
		for (int i = 0; i < maxValue; i++){
			counterArray[i] = 0;
			probArray[i] = 0;
		}

		//each pixel intensity
		for (int i = 0; i < size; i++){
			counterArray[(int)pixels[i]]++;
		}
		
		//calculate p(r)
		for (int i = 0; i < maxValue; i++){
			probArray[i] = ((double)counterArray[i] / (double)size);
		
		}
		
		//get new values
		double sum = 0;
		for (int i = 0; i < maxValue; i++){
			newCounterArray[i] = (int)(((maxValue - 1)/((double)size)) * sum);
			sum += counterArray[i];
		}

		//create newpixel array
		for (int i = 0; i < size; i++){
			newPixels[i] = (double)(newCounterArray[(int)pixels[i]]);
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
		
		Mat frequencyMat = getFrequency(input);
		
	    //high pass gaussian filter
		int row = input.rows();
		int col = input.cols();
	    Mat ghpf = createGHPF(input, new Size(row, col), 16.0) ;
	    
	    //get double arrays
	    double[] gaussianPixels = new double[row * col];
	    double[] resultPixels = new double[row * col];
	    double[] outputPixels = new double[row * col];
	    
	    //set array values
	    frequencyMat.convertTo(frequencyMat, CvType.CV_64FC1);
		ghpf.get(0, 0, gaussianPixels);
		frequencyMat.get(0, 0, resultPixels);
	    
	    //create new image
	    for(int i = 0; i < row; i++){
	    	for(int j = 0; j < col; j++){
	    		outputPixels[i*j + i] = gaussianPixels[i*j + i] * resultPixels[i*j + i];
	    	}
	    }

	    //final output mat
	    Mat output = input.clone();
		//output.put(0, 0, gaussianPixels);
	    output.put(0, 0, resultPixels);
	    
		return getSpacial(output);
	}
	
	public Mat createGHPF(Mat input, Size size, double cutoff){
		
		Mat ghpf = input.clone();
		ghpf.convertTo(ghpf, CvType.CV_64F);
				
		return ghpf;
	}
	
	public Mat getFrequency(Mat input){
		Mat output = input.clone();
		
		output.convertTo(output, CvType.CV_64FC1);
		
		//get the optimal size
		int m = Core.getOptimalDFTSize(input.rows());
		int n = Core.getOptimalDFTSize(input.cols());
		
		//create the padded image
		Mat padded = new Mat(new Size(n,m), CvType.CV_64FC1);
		Imgproc.copyMakeBorder(output, padded, 0, m - output.rows(), 0, n - output.cols(), Imgproc.BORDER_CONSTANT);
		
	    List<Mat> planes = new ArrayList<Mat>();
	    planes.add(padded);
	    planes.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

	    Mat complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

	    Mat complexI2 = Mat
	            .zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

	    Core.merge(planes, complexI); // Add to the expanded another plane with
	                                    // zeros

	    Core.dft(complexI, complexI2); // this way the result may fit in the
	                                    // source matrix

	    // compute the magnitude and switch to logarithmic scale
	    // => log(1 + sqrt(Re(DFT(I))^2 + Im(DFT(I))^2))
	    Core.split(complexI2, planes); // planes[0] = Re(DFT(I), planes[1] =
	                                    // Im(DFT(I))

	    Mat mag = new Mat(planes.get(0).size(), planes.get(0).type());

	    Core.magnitude(planes.get(0), planes.get(1), mag);// planes[0]
	                                                        // =
	                                                        // magnitude

	    Mat magI = mag;
	    Mat magI2 = new Mat(magI.size(), magI.type());
	    Mat magI3 = new Mat(magI.size(), magI.type());
	    Mat magI4 = new Mat(magI.size(), magI.type());
	    Mat magI5 = new Mat(magI.size(), magI.type());

	    Core.add(magI, Mat.ones(padded.rows(), padded.cols(), CvType.CV_64FC1),
	            magI2); // switch to logarithmic scale
	    Core.log(magI2, magI3);

	    Mat crop = new Mat(magI3, new Rect(0, 0, magI3.cols() & -2,
	            magI3.rows() & -2));

	    magI4 = crop.clone();

	    // rearrange the quadrants of Fourier image so that the origin is at the
	    // image center
	    int cx = magI4.cols() / 2;
	    int cy = magI4.rows() / 2;

	    Rect q0Rect = new Rect(0, 0, cx, cy);
	    Rect q1Rect = new Rect(cx, 0, cx, cy);
	    Rect q2Rect = new Rect(0, cy, cx, cy);
	    Rect q3Rect = new Rect(cx, cy, cx, cy);

	    Mat q0 = new Mat(magI4, q0Rect); // Top-Left - Create a ROI per quadrant
	    Mat q1 = new Mat(magI4, q1Rect); // Top-Right
	    Mat q2 = new Mat(magI4, q2Rect); // Bottom-Left
	    Mat q3 = new Mat(magI4, q3Rect); // Bottom-Right

	    Mat tmp = new Mat(); // swap quadrants (Top-Left with Bottom-Right)
	    q0.copyTo(tmp);
	    q3.copyTo(q0);
	    tmp.copyTo(q3);

	    q1.copyTo(tmp); // swap quadrant (Top-Right with Bottom-Left)
	    q2.copyTo(q1);
	    tmp.copyTo(q2);

	    Core.normalize(magI4, magI5, 0, 255, Core.NORM_MINMAX);

	    Mat realResult = new Mat(magI5.size(), CvType.CV_8UC1);

	    magI5.convertTo(realResult, CvType.CV_8UC1);
	    
	    return realResult;
	}
	
	public Mat getSpacial(Mat input){
		Mat inMat = input.clone();
		Mat convertedMat = input.clone();
		double[] pixels = new double[input.rows() * input.cols()];
		
		inMat.convertTo(inMat, CvType.CV_64FC1);
		convertedMat.convertTo(convertedMat, CvType.CV_64FC1);
		Core.idft(inMat, convertedMat);
		
		convertedMat.get(0, 0,pixels);
		
		Mat output = input.clone();
		output.put(0, 0, pixels);
		
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
