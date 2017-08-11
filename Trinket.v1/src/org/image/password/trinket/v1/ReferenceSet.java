package org.image.password.trinket.v1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.KeyPoint;

public class ReferenceSet {
	private int numRefereceImages = 3;
	private List<Mat> referenceSet;
	private int templateID;
	private double[] nearesNeighborDist;
	private double[] farthestNeighborDist;
	private double[] toTemplateDist;
	private double[][] crossSimilarity;
	private int[] nearestNeighbor;
	private int[] farthestNeighbor;
	private List<Mat> descriptors;
	private List<MatOfKeyPoint> keypoints;
	private double meanNNDist, meanFNDist, meanTempDist;
	private List<Integer> refSetIndex; 	
	
	
	private ArrayList<Integer> numWhitePixels;
	private ArrayList<Double> DTCPixels;
	private ArrayList<Double> DTCKPs;
	
	
	//------------------------ pre filter features
	private int NumKP_Temp ;
	private double Avg_NumKP;
	private int Min_NumKP ;
	private int Max_NumKP ;
	private double DTC_KP_Temp;
	private double Avg_DTC_KP;
	private double Min_DTC_KP;
	private double Max_DTC_KP;
	private int NumWhite_Temp;
	private double Avg_NumWhite;
	private int Min_NumWhite;
	private int Max_NumWhite;
	private double DTC_Pixel_Template;
	private double Avg_DTC_Pixel;
	private double Min_DTC_Pixel;
	private double Max_DTC_Pixel;
	private double avgDistRef;
	private double minDistRef; 
	private double maxDistRef;
	//--------------------------------------
	public ReferenceSet(){
		this.nearesNeighborDist = new double[numRefereceImages];
		this.farthestNeighborDist = new double[numRefereceImages];
		this.toTemplateDist = new double[numRefereceImages];
		this.crossSimilarity = new double[numRefereceImages][numRefereceImages];
		this.nearestNeighbor = new int[numRefereceImages];
		this.nearesNeighborDist = new double[numRefereceImages];
		this.farthestNeighbor = new int[numRefereceImages];
		this.farthestNeighborDist = new double[numRefereceImages];
		this.keypoints = new ArrayList<MatOfKeyPoint>();
		this.descriptors = new ArrayList<Mat>();
		this.refSetIndex = new ArrayList<Integer>();
		this.referenceSet = new ArrayList<Mat>();
		this.numWhitePixels = new ArrayList<Integer>();
		this.DTCPixels = new ArrayList<Double>();
		this.DTCKPs = new ArrayList<Double>();
	}
	
	public ReferenceSet(List<Mat> referenceSet, List<Integer> refSetIndex, List<MatOfKeyPoint> keypointsList, List<Mat> descriptorsList) {
		long start = System.currentTimeMillis();
		this.referenceSet = referenceSet;
		this.refSetIndex = refSetIndex;
		this.descriptors = new ArrayList<Mat>();
		this.descriptors = descriptorsList;
		this.keypoints = new ArrayList<MatOfKeyPoint>();
		this.keypoints = keypointsList;
		this.crossSimilarity = new double[referenceSet.size()][referenceSet.size()];
		findCrossSimilarity();
		findTemplateImage();
		calculatePreFilterFeatures();
		long end = System.currentTimeMillis();
		SharedMethods.writeFileOnSDCard("Initialization and Prefilter Features Extraction: " +
				String.valueOf(end - start) + "\n",
				"Timing_SetPass.txt");

	}
	
	public void calculateAuthenticationFeatures(){
		long start = System.currentTimeMillis();
		CommonOperations instance = CommonOperations.common;
		this.nearesNeighborDist = new double[this.referenceSet.size()];
		this.farthestNeighborDist = new double[this.referenceSet.size()];
		this.toTemplateDist = new double[this.referenceSet.size()];
		this.nearestNeighbor = new int[this.referenceSet.size()];
		this.nearesNeighborDist = new double[this.referenceSet.size()];
		this.farthestNeighbor = new int[this.referenceSet.size()];
		this.farthestNeighborDist = new double[this.referenceSet.size()];
		
		findNearestNeighbors();
		findFarthestNeighbors();
		for(int i = 0 ; i < this.referenceSet.size(); i++){
			this.toTemplateDist[i] = this.crossSimilarity[i][this.templateID];
		}
		this.meanNNDist = instance.calculateMean(this.nearesNeighborDist);
		this.meanFNDist = instance.calculateMean(this.farthestNeighborDist);
		this.meanTempDist = instance.calculateMean(this.toTemplateDist);
		long end = System.currentTimeMillis();
		SharedMethods.writeFileOnSDCard("Authentication Feature Computation: " +
				String.valueOf(end - start) + "\n",
				"Timing_SetPass.txt");
	}

	public void calculatePreFilterFeatures(){
		this.numWhitePixels = new ArrayList<Integer>();
		this.DTCPixels = new ArrayList<Double>();
		this.DTCKPs = new ArrayList<Double>();
		//------------------------------- prefilter info
		ArrayList<Double> temp = new ArrayList<Double>();
		for(int i = 0 ; i < this.referenceSet.size(); i++){
			temp.add( (double) this.keypoints.get(i).toList().size() );
		}
		//keypoints
		Statistics stat = new Statistics(temp);
		this.NumKP_Temp = this.keypoints.get(this.templateID).toList().size();
		this.Avg_NumKP = stat.getMean();
		this.Min_NumKP = (int) stat.getMin();
		this.Max_NumKP = (int) stat.getMax();
		
		
		temp = new ArrayList<Double>();
		//num pixels and DTC pixel
		for(int i = 0 ; i < this.referenceSet.size(); i++){
			double [] temp_res = new double [2];
			temp_res = FeaturesCalculator.instance.canny_NumWhite_DTCPixel(this.referenceSet.get(i), 100, 200, i);
			temp.add(temp_res[0]);
			this.numWhitePixels.add((int)temp_res[0]);
			this.DTCPixels.add(temp_res[1]);
		}
		
		stat = new Statistics(temp);
		this.NumWhite_Temp = this.numWhitePixels.get(this.templateID);
		this.Avg_NumWhite = stat.getMean();
		this.Min_NumWhite = (int) stat.getMin();
		this.Max_NumWhite = (int) stat.getMax();
		
		stat = new Statistics(new ArrayList<Double>(this.DTCPixels));
		this.DTC_Pixel_Template = this.DTCPixels.get(this.templateID);
		this.Avg_DTC_Pixel = stat.getMean();
		this.Min_DTC_Pixel = stat.getMin();
		this.Max_DTC_Pixel = stat.getMax();
		
		//DTC KPs
		for(int i = 0 ; i < this.referenceSet.size(); i++){
			this.DTCKPs.add(FeaturesCalculator.instance.getDistanceToMedoid(this.keypoints.get(i)));
		}
		stat = new Statistics(new ArrayList<Double>(this.DTCKPs));
		this.DTC_KP_Temp = this.DTCKPs.get(this.templateID);
		this.Avg_DTC_KP = stat.getMean();
		this.Min_DTC_KP = stat.getMin();
		this.Max_DTC_KP = stat.getMax();
		
		
		double [] statRefSet = findStatisticsOfReferenceSet();
		this.avgDistRef = statRefSet[0];
		this.minDistRef = statRefSet[1];
		this.maxDistRef = statRefSet[2];
		
	}
	
	public double [] findStatisticsOfReferenceSet(){
		double min = this.crossSimilarity[0][0];
		double max = 0;
		double sum = 0;
		double [] result = new double [3];
		for (int i = 0 ; i < crossSimilarity.length; i++){
			for(int j = 0 ; j < crossSimilarity.length ; j++){
				sum += crossSimilarity[i][j];
				if(crossSimilarity[i][j] < min)
					min = crossSimilarity[i][j];
				if(crossSimilarity[i][j] > max)
					max = crossSimilarity[i][j];
			}
		}
		result[0] = sum / (crossSimilarity.length * crossSimilarity.length);
		result[1] = min;
		result [2] = max;
		return result;
	}

	private void findCrossSimilarity() {
		DescriptorMatcher matcher = DescriptorMatcher
				.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		Mat descriptors1 = new Mat();
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		Mat descriptors2 = new Mat();
		MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

		for (int i = 0; i < this.referenceSet.size(); i++) {
			descriptors1 = this.descriptors.get(i);
			keypoints1 = this.keypoints.get(i);

			for (int j = 0; j < referenceSet.size(); j++) {
				descriptors2 = this.descriptors.get(j);
				keypoints2 = this.keypoints.get(j);

				MatOfDMatch matches = new MatOfDMatch();
				// object, scene
				if (descriptors1.type() == descriptors2.type()
						&& descriptors1.cols() == descriptors2.cols()) {

					matcher.match(descriptors1, descriptors2, matches);

					List<DMatch> matchesList = matches.toList();
					List<DMatch> matches_final = new ArrayList<DMatch>();
					// ------------------ Cross check
					MatOfDMatch matches21 = new MatOfDMatch();
					matcher.match(descriptors2, descriptors1, matches21);
					List<DMatch> matchesList21 = matches21.toList();

					for (int k = 0; k < matchesList.size(); k++) {
						if (matchesList21.get(matchesList.get(k).trainIdx).trainIdx == matchesList
								.get(k).queryIdx)
							matches_final.add(matches.toList().get(k));
					}

					// --------------------- Ratio
					// .match(descriptors1, descriptors2, matches);

					// --------------------- Homography
					LinkedList<Point> objList = new LinkedList<Point>();
					LinkedList<Point> sceneList = new LinkedList<Point>();

					List<KeyPoint> keypoints_objectList = keypoints1.toList();
					List<KeyPoint> keypoints_sceneList = keypoints2.toList();

					for (int k = 0; k < matches_final.size(); k++) {
						objList.addLast(keypoints_objectList.get(matches_final
								.get(k).queryIdx).pt);
						sceneList.addLast(keypoints_sceneList.get(matches_final
								.get(k).trainIdx).pt);
					}
					// /----------------- Homography ------------------------
					MatOfPoint2f obj = new MatOfPoint2f();
					obj.fromList(objList);

					MatOfPoint2f scene = new MatOfPoint2f();
					scene.fromList(sceneList);

					double counter = 0;

					if (matches_final.size() > 4) {
						Mat mask = new Mat();
						Mat H = Calib3d.findHomography(obj, scene, 8, 10, mask);
						mask.convertTo(mask, CvType.CV_64FC3);

						int size = (int) (mask.total() * mask.channels());
						double[] temp = new double[size];
						mask.get(0, 0, temp);

						for (int k = 0; k < size; k++) {
							if (temp[k] == 1.0)
								counter++;
						}
						temp = null;

						Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
						Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

						obj_corners.put(0, 0, new double[] { 0, 0 });
						obj_corners.put(1, 0, new double[] {
								referenceSet.get(j).cols(), 0 });
						obj_corners.put(2, 0, new double[] {
								referenceSet.get(j).cols(),
								referenceSet.get(j).rows() });
						obj_corners.put(3, 0, new double[] { 0,
								referenceSet.get(j).rows() });

						Core.perspectiveTransform(obj_corners, scene_corners, H);

						// ----------------------------------------------------------------------------------------

					} else {

						MatOfDMatch matches_final_mat = new MatOfDMatch();
						matches_final_mat.fromList(matches_final);

						double mTotalMatches = matches_final.size();
						counter = mTotalMatches;

					}

					// --------- do cleaning
					double res = counter / (objList.size());
					objList = null;
					sceneList = null;
					keypoints_objectList = null;
					keypoints_sceneList = null;
					obj.release();
					scene.release();
					System.gc();
					// ------------------
					this.crossSimilarity[i][j] = res;
				} else {
					System.out.println("error: " + i + ", " + j);
					this.crossSimilarity[i][j] = 0;
				}

			}

		}
	}

	private void findTemplateImage() {
		//CommonOperations instance = CommonOperations.getInstance();
		CommonOperations instance = CommonOperations.common;
		double[][] transposeSimilarity = instance.transposeMatrix(this.crossSimilarity);
		double[] mean = new double[this.referenceSet.size()];
		for (int i = 0; i < this.referenceSet.size(); i++) {
			mean[i] = instance.calculateMean(transposeSimilarity[i]);
		}
		int maxIndex = instance.findMaxIndex(mean);
		if (maxIndex == -1)
			// System.out.println("Error: ");
			throw new RuntimeException("Cannot find maxIndex!");
		else
			this.templateID = maxIndex;
	}

	private void findNearestNeighbors() {

		//CommonOperations instance = CommonOperations.getInstance();
		CommonOperations instance = CommonOperations.common;    
		double[][] similarity = new double[1][this.referenceSet.size()];
		for (int i = 0; i < this.referenceSet.size(); i++) {
			System.arraycopy(this.crossSimilarity[i], 0, similarity[0], 0,
					this.referenceSet.size());
			double[] result = instance.maxIndexExceptSelfIndex(similarity[0], i);
			this.nearestNeighbor[i] = (int) result[0];
			this.nearesNeighborDist[i] = result[1];
		}
	}

	private void findFarthestNeighbors() {
		//CommonOperations instance = CommonOperations.getInstance();
		CommonOperations instance = CommonOperations.common;
		double[][] similarity = new double[1][this.referenceSet.size()];
		for (int i = 0; i < this.referenceSet.size(); i++) {
			System.arraycopy(this.crossSimilarity[i], 0, similarity[0], 0,
					this.referenceSet.size());
			double[] result = instance.minIndexExceptSelfIndex(similarity[0], i);
			this.farthestNeighbor[i] = (int) result[0];
			this.farthestNeighborDist[i] = result[1];
		}
	}
	

	
	public double[][] getCrossSimilarity() {
		return crossSimilarity;
	}

	public List<Integer> getRefSetIndex() {
		return refSetIndex;
	}

	public void setRefSetIndex(List<Integer> refSetIndex) {
		this.refSetIndex = refSetIndex;
	}

	public int getTemplateID() {
		return templateID;
	}

	public void setTemplateID(int templateID) {
		this.templateID = templateID;
	}

	public List<Mat> getDescriptors() {
		return descriptors;
	}

	public void setDescriptors(List<Mat> descriptors) {
		this.descriptors = descriptors;
	}

	public List<MatOfKeyPoint> getKeypoints() {
		return keypoints;
	}

	public void setKeypoints(List<MatOfKeyPoint> keypoints) {
		this.keypoints = keypoints;
	}

	public double getMeanNNDist() {
		return meanNNDist;
	}

	public void setMeanNNDist(double meanNNDist) {
		this.meanNNDist = meanNNDist;
	}

	public double getMeanFNDist() {
		return meanFNDist;
	}

	public void setMeanFNDist(double meanFNDist) {
		this.meanFNDist = meanFNDist;
	}

	public double getMeanTempDist() {
		return meanTempDist;
	}

	public void setMeanTempDist(double meanTempDist) {
		this.meanTempDist = meanTempDist;
	}

	public List<Mat> getReferenceSet() {
		return referenceSet;
	}
	
	public double[] getNearesNeighborDist() {
		return nearesNeighborDist;
	}

	public void setNearesNeighborDist(double[] nearesNeighborDist) {
		this.nearesNeighborDist = nearesNeighborDist;
	}

	public double[] getFarthestNeighborDist() {
		return farthestNeighborDist;
	}

	public void setFarthestNeighborDist(double[] farthestNeighborDist) {
		this.farthestNeighborDist = farthestNeighborDist;
	}

	public double[] getToTemplateDist() {
		return toTemplateDist;
	}

	public void setToTemplateDist(double[] toTemplateDist) {
		this.toTemplateDist = toTemplateDist;
	}

	public int[] getNearestNeighbor() {
		return nearestNeighbor;
	}

	public void setNearestNeighbor(int[] nearestNeighbor) {
		this.nearestNeighbor = nearestNeighbor;
	}

	public int[] getFarthestNeighbor() {
		return farthestNeighbor;
	}

	public void setFarthestNeighbor(int[] farthestNeighbor) {
		this.farthestNeighbor = farthestNeighbor;
	}

	public void setCrossSimilarity(double[][] crossSimilarity) {
		this.crossSimilarity = crossSimilarity;
	}

	public void setReferenceSet(List<Mat> referenceSet) {
		this.referenceSet = referenceSet;
	}

	public int getNumRefereceImages() {
		return numRefereceImages;
	}

	public void setNumRefereceImages(int numRefereceImages) {
		this.numRefereceImages = numRefereceImages;
	}

	public ArrayList<Integer> getNumWhitePixels() {
		return numWhitePixels;
	}

	public void setNumWhitePixels(ArrayList<Integer> numWhitePixels) {
		this.numWhitePixels = numWhitePixels;
	}

	public ArrayList<Double> getDTCPixels() {
		return DTCPixels;
	}

	public void setDTCPixels(ArrayList<Double> avgDTCPixels) {
		this.DTCPixels = avgDTCPixels;
	}

	public ArrayList<Double> getDTCKPs() {
		return DTCKPs;
	}

	public void setDTCKPs(ArrayList<Double> dTCKPs) {
		DTCKPs = dTCKPs;
	}

	public int getNumKP_Temp() {
		return NumKP_Temp;
	}

	public void setNumKP_Temp(int numKP_Temp) {
		NumKP_Temp = numKP_Temp;
	}

	public double getAvg_NumKP() {
		return Avg_NumKP;
	}

	public void setAvg_NumKP(double avg_NumKP) {
		Avg_NumKP = avg_NumKP;
	}

	public int getMin_NumKP() {
		return Min_NumKP;
	}

	public void setMin_NumKP(int min_NumKP) {
		Min_NumKP = min_NumKP;
	}

	public int getMax_NumKP() {
		return Max_NumKP;
	}

	public void setMax_NumKP(int max_NumKP) {
		Max_NumKP = max_NumKP;
	}

	public double getDTC_KP_Temp() {
		return DTC_KP_Temp;
	}

	public void setDTC_KP_Temp(double dTC_KP_Temp) {
		DTC_KP_Temp = dTC_KP_Temp;
	}

	public double getAvg_DTC_KP() {
		return Avg_DTC_KP;
	}

	public void setAvg_DTC_KP(double avg_DTC_KP) {
		Avg_DTC_KP = avg_DTC_KP;
	}

	public double getMin_DTC_KP() {
		return Min_DTC_KP;
	}

	public void setMin_DTC_KP(double min_DTC_KP) {
		Min_DTC_KP = min_DTC_KP;
	}

	public double getMax_DTC_KP() {
		return Max_DTC_KP;
	}

	public void setMax_DTC_KP(double max_DTC_KP) {
		Max_DTC_KP = max_DTC_KP;
	}

	public int getNumWhite_Temp() {
		return NumWhite_Temp;
	}

	public void setNumWhite_Temp(int numWhite_Temp) {
		NumWhite_Temp = numWhite_Temp;
	}

	public double getAvg_NumWhite() {
		return Avg_NumWhite;
	}

	public void setAvg_NumWhite(double avg_NumWhite) {
		Avg_NumWhite = avg_NumWhite;
	}

	public int getMin_NumWhite() {
		return Min_NumWhite;
	}

	public void setMin_NumWhite(int min_NumWhite) {
		Min_NumWhite = min_NumWhite;
	}

	public int getMax_NumWhite() {
		return Max_NumWhite;
	}

	public void setMax_NumWhite(int max_NumWhite) {
		Max_NumWhite = max_NumWhite;
	}

	public double getDTC_Pixel_Template() {
		return DTC_Pixel_Template;
	}

	public void setDTC_Pixel_Template(double dTC_Pixel_Template) {
		DTC_Pixel_Template = dTC_Pixel_Template;
	}

	public double getAvg_DTC_Pixel() {
		return Avg_DTC_Pixel;
	}

	public void setAvg_DTC_Pixel(double avg_DTC_Pixel) {
		Avg_DTC_Pixel = avg_DTC_Pixel;
	}

	public double getMin_DTC_Pixel() {
		return Min_DTC_Pixel;
	}

	public void setMin_DTC_Pixel(double min_DTC_Pixel) {
		Min_DTC_Pixel = min_DTC_Pixel;
	}

	public double getMax_DTC_Pixel() {
		return Max_DTC_Pixel;
	}

	public void setMax_DTC_Pixel(double max_DTC_Pixel) {
		Max_DTC_Pixel = max_DTC_Pixel;
	}

	public double getAvgDistRef() {
		return avgDistRef;
	}

	public void setAvgDistRef(double avgDistRef) {
		this.avgDistRef = avgDistRef;
	}

	public double getMinDistRef() {
		return minDistRef;
	}

	public void setMinDistRef(double minDistRef) {
		this.minDistRef = minDistRef;
	}

	public double getMaxDistRef() {
		return maxDistRef;
	}

	public void setMaxDistRef(double maxDistRef) {
		this.maxDistRef = maxDistRef;
	}

}
