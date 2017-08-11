package org.image.password.trinket.v1;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

public enum FeaturesCalculator {

	instance;
	
	public double[] canny_NumWhite_DTCPixel(Mat img1, int lowTh, int highTh, int ID){
		double[] res = new double[2];
		
		Mat gray = new Mat();
		Imgproc.cvtColor(img1, gray, Imgproc.COLOR_BGR2GRAY);
		Mat edges = new Mat();
		Imgproc.Canny(img1, edges, lowTh, highTh);
		Imgproc.threshold(edges, edges, 240, 255, Imgproc.THRESH_BINARY);
		int numWhite = Core.countNonZero(edges);
		//DTC_PIXEL
		double val = getAvgDistanceToCenterForEdges(edges);
		res[0] = numWhite;
		res[1] = val;
		return res;
	}
	
	//DTC_PIXEL
	public double getAvgDistanceToCenterForEdges(Mat img){
		double sumX = 0;
		double sumY = 0;
		double disToCentroid = 0;
		img.convertTo(img, CvType.CV_64FC3); // New line added. 
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		for (int i = 0; i < img.rows(); i++) {
			for (int j = 0; j < img.cols(); j++) {
				double[] data = new double[3];
				img.get(i, j, data);
				if (data[0] != 0.0) {
					x.add((double) i);
					sumX += (double) i;
					y.add((double) j);
					sumY += (double) j;
				}
			}
		}

		if(x.size() == 0 ){
			return 0;                    
		}


		Point medoid = new Point(sumX/x.size() , sumY/x.size());
		for(int i = 0 ; i < x.size(); i++){
			disToCentroid += EucleadianDistance (medoid, new Point(x.get(i) , y.get(i)));
		}
		double val = disToCentroid/x.size();

		if(val <= 0 || val != val){				
			System.out.println("Error");
		}
		return disToCentroid/x.size();
	}
	
	public double EucleadianDistance (Point point1, Point point2) {
		double deltaX = point2.x - point1.x;
		double deltaY = point2.y - point1.y;
		double result = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		return result; 
	}
	
	
	public double getDistanceToMedoid (MatOfKeyPoint keypoints){
		List<KeyPoint> keypoints_List = keypoints.toList();
		List<Double> xes = new ArrayList<Double>();
		List<Double> ys = new ArrayList<Double>();
		double sumX = 0;
		double sumY = 0;
		double disToCentroid = 0;
		for(int i = 0 ; i < keypoints_List.size(); i++){
			xes.add(keypoints_List.get(i).pt.x);
			sumX += xes.get(i);
			ys.add(keypoints_List.get(i).pt.y);	
			sumY +=ys.get(i);
		}
		Point medoid = new Point(sumX/keypoints_List.size() , sumY/keypoints_List.size());
		for(int i = 0 ; i < keypoints_List.size(); i++){
			disToCentroid += EucleadianDistance (medoid, new Point(keypoints_List.get(i).pt.x , keypoints_List.get(i).pt.y));

		}
		return disToCentroid;

	}
}
