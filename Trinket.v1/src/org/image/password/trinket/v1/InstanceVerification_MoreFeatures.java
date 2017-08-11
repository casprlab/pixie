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
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;
import android.content.Context;

public class InstanceVerification_MoreFeatures {

	ReferenceSet refSet;
	Mat testImage;
	double[] similarityMatrix;
	Mat testDescriptor;
	MatOfKeyPoint testKeypoint;
	double similarityMin, similarityMax, similarityTemplate;
	String extraFeatures= "";
	int NNIndex;
	double [] extraFeaturess = new double[23];
	public InstanceVerification_MoreFeatures(ReferenceSet refSet, Mat testImage, MatOfKeyPoint testKeypoint, Mat testDescriptor) {
		CommonOperations instance = CommonOperations.common;
		this.refSet = refSet;
		this.testImage = testImage;
		this.similarityMatrix = new double[this.refSet.getReferenceSet().size()];
		this.testDescriptor = new Mat();
		this.testDescriptor = testDescriptor;
		this.testKeypoint = new MatOfKeyPoint();
		this.testKeypoint = testKeypoint;
		// findDescriptorsAndKepoints();
		this.similarityMatrix = findSimilarityToRefSet(this.testImage,this.refSet);

		/*
		if(dataSetCrossSimilarity != null){
			this.similarityMatrix = inferSimilarityToRefSet(dataSetCrossSimilarity,
					testImgIndex, refSetIndex);
		}
		else{
			for (int i = 0 ; i < refSet.getReferenceSet().size() ; i++ ){
				this.similarityMatrix [i] =  detect(testImage, refSet.getReferenceSet().get(i));
			}
		}*/
		double[] result = new double[4];
		result = instance.findMinMax_withIndex(similarityMatrix);
		this.similarityMin = result[0] / refSet.getMeanFNDist();
		int minIndex = (int) result[1];
		this.similarityMax = result[2] / refSet.getMeanNNDist();
		this.similarityTemplate = this.similarityMatrix[refSet.getTemplateID()]
				/ refSet.getMeanTempDist();

		this.NNIndex = minIndex;
		//----------- more features ------------
		detect(testImage, refSet.getReferenceSet().get(refSet.getTemplateID()));
	}

	public String getExtraFeatures() {
		return extraFeatures;
	}


	public void setExtraFeatures(String extraFeatures) {
		this.extraFeatures = extraFeatures;
	}


	/**
	 * This method computes the similarity of two images using cross-checking,
	 * homography and perspective transformation. puts the results into
	 * crosschecking.txt and create a dataset
	 * 
	 * @param img1
	 * @param img2
	 * @param classLabel1
	 *            : class label of first image
	 * @param classLabel2
	 *            : class label of second image
	 * @return
	 */
	public double detect(Mat img1, Mat img2) {
		String datasetRecord = "";
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		DescriptorExtractor descriptor = DescriptorExtractor
				.create(DescriptorExtractor.ORB);

		DescriptorMatcher matcher = DescriptorMatcher
				.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		Mat descriptors1 = new Mat();
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		detector.detect(img1, keypoints1);
		descriptor.compute(img1, keypoints1, descriptors1);

		Mat descriptors2 = new Mat();
		MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
		detector.detect(img2, keypoints2);
		descriptor.compute(img2, keypoints2, descriptors2);

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

			for (int i = 0; i < matchesList.size(); i++) {
				if (matchesList21.get(matchesList.get(i).trainIdx).trainIdx == matchesList
						.get(i).queryIdx)
					matches_final.add(matches.toList().get(i));
			}

			// --------------------- Homography
			LinkedList<Point> objList = new LinkedList<Point>();
			LinkedList<Point> sceneList = new LinkedList<Point>();

			List<KeyPoint> keypoints_objectList = keypoints1.toList();
			List<KeyPoint> keypoints_sceneList = keypoints2.toList();

			//double distToMedoid_Obj = FeaturesCalculator.instance.getDistanceToMedoid (keypoints1);
			//double distToMedoid_Sce = FeaturesCalculator.instance.getDistanceToMedoid (keypoints2);

			double maxDist = 0;
			double minDist = 0;
			if (matches_final.size() > 0)
				minDist = matches_final.get(0).distance;

			// These are auxilary lists for getting statistics
			ArrayList<Double> match_distances = new ArrayList<Double>();
			ArrayList<Double> match_keyPointSize_obj = new ArrayList<Double>();
			ArrayList<Double> match_keyPointSize_sce = new ArrayList<Double>();
			ArrayList<Double> match_response_obj = new ArrayList<Double>();
			ArrayList<Double> match_response_sce = new ArrayList<Double>();
			ArrayList<Double> match_angle_obj = new ArrayList<Double>();
			ArrayList<Double> match_angle_sce = new ArrayList<Double>();

			for (int i = 0; i < matches_final.size(); i++) {
				match_distances.add((double) matches_final.get(i).distance);

				objList.addLast(keypoints_objectList.get(matches_final.get(i).queryIdx).pt);
				sceneList
				.addLast(keypoints_sceneList.get(matches_final.get(i).trainIdx).pt);
				// finding min dist and max dist among matches
				if (matches_final.get(i).distance > maxDist)
					maxDist = matches_final.get(i).distance;
				if (matches_final.get(i).distance <= minDist)
					minDist = matches_final.get(i).distance;

				match_keyPointSize_obj.add((double) keypoints_objectList
						.get(matches_final.get(i).queryIdx).size);
				match_keyPointSize_sce.add((double) keypoints_sceneList
						.get(matches_final.get(i).trainIdx).size);
				match_response_obj.add((double) keypoints_objectList
						.get(matches_final.get(i).queryIdx).response);
				match_response_sce.add((double) keypoints_sceneList
						.get(matches_final.get(i).trainIdx).response);
				match_angle_obj.add((double) keypoints_objectList
						.get(matches_final.get(i).queryIdx).angle);
				match_angle_sce.add((double) keypoints_sceneList
						.get(matches_final.get(i).trainIdx).angle);
			}
			/*
			 * CommonFunctions.writeFileOnSDCard(
			 * String.valueOf(keypoints_objectList.size()) + "\n",
			 * "numKeypoints.txt");
			 */

			//datasetRecord += String.valueOf(keypoints_objectList.size()) + ", ";
			//datasetRecord += String.valueOf(keypoints_sceneList.size()) + ", ";
			//datasetRecord += String.valueOf(matches_final.size()) + ", ";
			//datasetRecord += String.valueOf(maxDist) + ", "
			//		+ String.valueOf(minDist) + ", "; 
			
			extraFeaturess[0] = (double) keypoints_objectList.size();
			extraFeaturess[1] = keypoints_sceneList.size();
			extraFeaturess[2] = matches_final.size();
			extraFeaturess[3] = maxDist;
			extraFeaturess[4] = minDist;
			
			
			// Add statistic parameters to dataset
			Statistics stat = new Statistics(match_distances);
			Statistics stat_size_obj = new Statistics(match_keyPointSize_obj);
			Statistics stat_size_sce = new Statistics(match_keyPointSize_sce);
			Statistics stat_res_obj = new Statistics(match_response_obj);
			Statistics stat_res_sce = new Statistics(match_response_sce);
			Statistics stat_angle_obj = new Statistics(match_angle_obj);
			Statistics stat_angle_sce = new Statistics(match_angle_sce);

			/*datasetRecord += String.valueOf(stat.getMean()) + ", ";
			datasetRecord += String.valueOf(stat.getStandardDeviation()) + ", ";
			datasetRecord += String.valueOf(stat_size_obj.getMean()) + ", ";
			datasetRecord += String.valueOf(stat_size_obj
					.getStandardDeviation()) + ", ";
			datasetRecord += String.valueOf(stat_size_sce.getMean()) + ", ";
			datasetRecord += String.valueOf(stat_size_sce
					.getStandardDeviation()) + ", ";
			datasetRecord += String.valueOf(stat_res_obj.getMean()) + ", ";
			datasetRecord += String
					.valueOf(stat_res_obj.getStandardDeviation()) + ", ";
			datasetRecord += String.valueOf(stat_res_sce.getMean()) + ", ";
			datasetRecord += String
					.valueOf(stat_res_sce.getStandardDeviation()) + ", ";
			datasetRecord += String.valueOf(stat_angle_obj.getMean()) + ", ";
			datasetRecord += String.valueOf(stat_angle_obj
					.getStandardDeviation()) + ", ";
			datasetRecord += String.valueOf(stat_angle_sce.getMean()) + ", ";
			datasetRecord += String.valueOf(stat_angle_sce
					.getStandardDeviation()) + ", ";
			*/
			
			
			extraFeaturess [5] = stat.getMean() ;
			extraFeaturess [6] = stat.getStandardDeviation();
			extraFeaturess [7] = stat_size_obj.getMean();
			extraFeaturess [8] = stat_size_obj
					.getStandardDeviation();
			extraFeaturess [9] = stat_size_sce.getMean();
			extraFeaturess [10] = stat_size_sce.getStandardDeviation();
			extraFeaturess [11] = stat_res_obj.getMean();
			extraFeaturess [12] = stat_res_obj.getStandardDeviation();
			extraFeaturess [13] = stat_res_sce.getMean();
			extraFeaturess [14] = stat_res_sce.getStandardDeviation();
			extraFeaturess [15] = stat_angle_obj.getMean();
			extraFeaturess [16] = stat_angle_obj.getStandardDeviation();
			extraFeaturess [17] = stat_angle_sce.getMean();
			extraFeaturess [18] = stat_angle_sce.getStandardDeviation();
			
			stat = null;
			stat_size_obj = null;
			stat_size_sce = null;

			// /----------------- Homography ------------------------
			MatOfPoint2f obj = new MatOfPoint2f();
			obj.fromList(objList);

			MatOfPoint2f scene = new MatOfPoint2f();
			scene.fromList(sceneList);

			double counter = 0;

			if (matches_final.size() > 4) {
				Mat mask = new Mat();
				Mat H = Calib3d.findHomography(obj, scene, 8, 10, mask);

				for (int j = 0; j < H.size().height; j++) {
					double[] h = H.get(0, j);
					for (int x = 0; x < h.length; x++) {

						 System.out.print(h[x] + " *  ");
						//datasetRecord += String.valueOf(h[x]) + ", ";
						extraFeaturess[19+x] = h[x];
					}

				}

				mask.convertTo(mask, CvType.CV_64FC3); // New line added.
				int size = (int) (mask.total() * mask.channels());
				double[] temp = new double[size]; // use double[] instead of
				// byte[]
				mask.get(0, 0, temp);

				for (int i = 0; i < size; i++) {
					if (temp[i] == 1.0)
						counter++;
				}
				//datasetRecord += String.valueOf(counter) + ", ";
				extraFeaturess[22] = counter;
				temp = null;

				Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
				Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

				obj_corners.put(0, 0, new double[] { 0, 0 });
				obj_corners.put(1, 0, new double[] { img2.cols(), 0 });
				obj_corners
				.put(2, 0, new double[] { img2.cols(), img2.rows() });
				obj_corners.put(3, 0, new double[] { 0, img2.rows() });

				Core.perspectiveTransform(obj_corners, scene_corners, H);
				/*for (int j = 0; j < obj_corners.size().height; j++) {
					double[] s = obj_corners.get(j, 0);
					for (int x = 0; x < s.length; x++) {
						datasetRecord += String.valueOf(s[x]) + ", ";

					}
				}*/

				// ----------------------------------------------------------------------------------------

			} else {

				MatOfDMatch matches_final_mat = new MatOfDMatch();
				matches_final_mat.fromList(matches_final);

				double mTotalMatches = matches_final.size();
				counter = mTotalMatches;

				//datasetRecord += "?, ? ,?, 0, ? , ? , ? , ? , ? , ? , ? , ?, ";
				datasetRecord += "?, ? ,?, 0,  ";
				extraFeaturess[19] = 0;
				extraFeaturess[20] = 0;
				extraFeaturess[21] = 0;
				extraFeaturess[22] = 0;

			}

			//datasetRecord += String.valueOf(distToMedoid_Obj) + ", " + String.valueOf(distToMedoid_Sce) + ", ";

			// --------- do cleaning
			double res = counter / (objList.size());
			objList = null;
			sceneList = null;
			keypoints_objectList = null;
			keypoints_sceneList = null;
			obj.release();
			scene.release();
			System.gc();
			//datasetRecord += "\n";
			this.extraFeatures = datasetRecord;
			datasetRecord = "";

			stat = null;
			stat_size_obj = null;
			stat_size_sce = null;
			stat_res_obj = null;
			stat_res_sce = null;
			stat_angle_obj = null;
			stat_angle_sce = null;
			// ------------------
			return res;
		}
		// return mTotalMatches;
		else {
			System.out.println("error:");
			return 0;
		}
	}// end detect


	//canny edge detection + average distance of edges pixel positions to center of these pixels
	public String cannyForObjAndSce(Mat img1, Mat img2, int lowTh, int highTh ){
		String datasetRecord = "";
		Mat gray = new Mat();
		Imgproc.cvtColor(img1, gray, Imgproc.COLOR_BGR2GRAY);
		// detect the edges
		Mat edges = new Mat();
		int lowThreshold = lowTh;
		int highThreshold = highTh;
		Imgproc.Canny(img1, edges, lowThreshold, highThreshold);//lowThreshold * ratio);
		Imgproc.threshold(edges, edges, 240, 255, Imgproc.THRESH_BINARY);
		int numWhite = Core.countNonZero(edges);
		datasetRecord += String.valueOf(numWhite) + ", ";
		double val = FeaturesCalculator.instance.getAvgDistanceToCenterForEdges(edges);
		String param = String.valueOf(val);
		// generate gray scale and blur
		gray = new Mat();
		Imgproc.cvtColor(img2, gray, Imgproc.COLOR_BGR2GRAY);
		// detect the edges
		edges = new Mat();
		Imgproc.Canny(img2, edges, lowThreshold, highThreshold);//lowThreshold * ratio);
		Imgproc.threshold(edges, edges, 240, 255, Imgproc.THRESH_BINARY);
		numWhite = Core.countNonZero(edges);
		datasetRecord += String.valueOf(numWhite) + ", ";

		val = FeaturesCalculator.instance.getAvgDistanceToCenterForEdges(edges);
		String param2 = String.valueOf(val);

		datasetRecord += param + ", " + param2 + ", ";
		return datasetRecord;
	}

	public double getSimilarityMin() {
		return similarityMin;
	}

	public void setSimilarityMin(double similarityMin) {
		this.similarityMin = similarityMin;
	}

	public double getSimilarityMax() {
		return similarityMax;
	}

	public void setSimilarityMax(double similarityMax) {
		this.similarityMax = similarityMax;
	}

	public double getSimilarityTemplate() {
		return similarityTemplate;
	}

	public void setSimilarityTemplate(double similarityTemplate) {
		this.similarityTemplate = similarityTemplate;
	}
	
	private double[] findSimilarityToRefSet(Mat image, ReferenceSet refSet) {
		double[] similarity = new double[refSet.getReferenceSet().size()];

		for (int i = 0; i < similarity.length; i++) {
			similarity[i] = calculateSimilarity(this.testImage, this.refSet
					.getReferenceSet().get(i), this.testDescriptor, this.refSet
					.getDescriptors().get(i), this.testKeypoint, this.refSet
					.getKeypoints().get(i));
		}
		return similarity;

	}
	 
	public double calculateSimilarity(Mat img1, Mat img2, Mat descriptors1,
			Mat descriptors2, MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2) {
		DescriptorMatcher matcher = DescriptorMatcher
				.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

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
				objList.addLast(keypoints_objectList.get(matches_final.get(k).queryIdx).pt);
				sceneList
				.addLast(keypoints_sceneList.get(matches_final.get(k).trainIdx).pt);
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
				obj_corners.put(1, 0, new double[] { img2.cols(), 0 });
				obj_corners
				.put(2, 0, new double[] { img2.cols(), img2.rows() });
				obj_corners.put(3, 0, new double[] { 0, img2.rows() });

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
			return res;
		} else {
			System.out.println("error.");
			return 0;
		}

	}

	public int getNNIndex() {
		return NNIndex;
	}
	public void setNNIndex(int nNIndex) {
		NNIndex = nNIndex;
	}
	
	/*public Instances createDatasetRecord(Context context){
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		//this.preFilterDataset = new Instances(new BufferedReader(new FileReader(sdcard + "/Pixie/Models/PreFilterDataset.arff")));
		Instances Dataset;
		try {
			Dataset = new Instances
					(new BufferedReader(new InputStreamReader(context.getAssets().open("Dataset.arff"))));

			Dataset.setClassIndex(Dataset.numAttributes() - 1);
			Instance inst  = new DenseInstance(Dataset.numAttributes());
			inst.setDataset(Dataset); 
			inst.setValue(0, this.similarityMin); 
			inst.setValue(1, this.similarityMax); 
			inst.setValue(2, this.similarityTemplate); 
			inst.setValue(3, this.refSet.getMeanNNDist()); 
			inst.setValue(4, this.refSet.getMeanFNDist()); 
			inst.setValue(5, this.refSet.getMeanTempDist()); 
			//extra features
			for (int i = 0 ; i < 23; i++){
				inst.setValue(6+i,extraFeaturess[i]); 
			}
			inst.setValue(29,"0"); 
			//add the new instance to the main dataset at the last position
			Dataset.add(inst);
			return Dataset;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}*/
	
	public String testInstance(Context context){
		String pred = "";
		if(!TestImageClassifier.instance.isInitialized())
			TestImageClassifier.instance.Initialization(context);
		//create the preflter record for the referenceSet
		TestImageClassifier.instance.addDataRecordToDataset(this.similarityMin, this.similarityMax, this.similarityTemplate, 
				this.refSet.getMeanNNDist(), this.refSet.getMeanFNDist(), this.refSet.getMeanTempDist(), (int) extraFeaturess[0], (int) extraFeaturess[1],
				(int) extraFeaturess[2], extraFeaturess[3], extraFeaturess[4], extraFeaturess[5], extraFeaturess[6],   
				extraFeaturess[7], extraFeaturess[8], extraFeaturess[9], extraFeaturess[10], extraFeaturess[11],
				extraFeaturess[12], extraFeaturess[13], extraFeaturess[14], extraFeaturess[15], extraFeaturess[16],
				extraFeaturess[17], extraFeaturess[18], extraFeaturess[19], extraFeaturess[20], extraFeaturess[21], (int) extraFeaturess[22]);

		pred = TestImageClassifier.instance.testInstance();
		return pred;
	}
	
	
}
