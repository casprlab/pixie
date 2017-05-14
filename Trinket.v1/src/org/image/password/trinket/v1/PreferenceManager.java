package org.image.password.trinket.v1;

import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import com.google.gson.Gson;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
	
	
	private static final String PREF_NAME = "org.image.pass.pixie.preferences";
	private static final String IS_PREF_SET  = "org.image.pass.pixie.preferences.isPrefSet";
	private static final String NUMRUN = "org.image.pass.pixie.preferences.numRun";
	
	//FIU credientials
	private static final String FIU_USERNAME = "org.image.pass.pixie.fiuUserName";
	private static final String FIU_PASS = "org.image.pass.pixie.fiuPassword";
	
	
	// reference image 0
	private static final String PASS_FILE_PATH = "org.image.pass.pixie.passFilePath";
	private static final String PASS_Mat = "org.image.pass.pixie.passMat";
	private static final String PASS_KP = "org.image.pass.pixie.passKP";
	private static final String PASS_Descriptors = "org.image.pass.pixie.passDes";

	// reference image 1
	private static final String PASS_FILE_PATH1 = "org.image.pass.pixie.passFilePath1";
	private static final String PASS_Mat1 = "org.image.pass.pixie.passMat1";
	private static final String PASS_KP1 = "org.image.pass.pixie.passKP1";
	private static final String PASS_Descriptors1 = "org.image.pass.pixie.passDes1";

	// reference image 2
	private static final String PASS_FILE_PATH2 = "org.image.pass.pixie.passFilePath2";
	private static final String PASS_Mat2 = "org.image.pass.pixie.passMat2";
	private static final String PASS_KP2 = "org.image.pass.pixie.passKP2";
	private static final String PASS_Descriptors2 = "org.image.pass.pixie.passDes2";

	//ReferenceSet Info
	private static final String RefSet_TemplateID = "org.image.pass.pixie.refSet.templateID";
	private static final String RefSet_NearesNeighborDist = "org.image.pass.pixie.refSet.nearesNeighborDist";
	private static final String RefSet_FarthestNeighborDist = "org.image.pass.pixie.refSet.farthestNeighborDist";
	private static final String RefSet_ToTemplateDist = "org.image.pass.pixie.refSet.toTemplateDist";
	private static final String RefSet_CrossSimilarity = "org.image.pass.pixie.refSet.crossSimilarity";
	private static final String RefSet_NearestNeighbor = "org.image.pass.pixie.refSet.nearestNeighbor";
	private static final String RefSet_FarthestNeighbor = "org.image.pass.pixie.refSet.farthestNeighbor";
	private static final String RefSet_MeanNNDist = "org.image.pass.pixie.refSet.meanNNDist";
	private static final String RefSet_MeanFNDist = "org.image.pass.pixie.refSet.meanFNDist";
	private static final String RefSet_MeanTempDist = "org.image.pass.pixie.refSet.meanTempDist";

	private static final String RefSet_numWhitePixels = "org.image.pass.pixie.refSet.numWhitePixels";
	private static final String RefSet_DTCPixels = "org.image.pass.pixie.refSet.DTCPixels";
	private static final String RefSet_DTCKPs = "org.image.pass.pixie.refSet.DTCKPs";
	private static final String RefSet_NumKP_Temp = "org.image.pass.pixie.refSet.NumKP_Temp";
	private static final String RefSet_Avg_NumKP = "org.image.pass.pixie.refSet.Avg_NumKP";
	private static final String RefSet_Min_NumKP = "org.image.pass.pixie.refSet.Min_NumKP";
	private static final String RefSet_Max_NumKP = "org.image.pass.pixie.refSet.Max_NumKP";
	private static final String RefSet_DTC_KP_Temp = "org.image.pass.pixie.refSet.DTC_KP_Temp";
	private static final String RefSet_Avg_DTC_KP = "org.image.pass.pixie.refSet.Avg_DTC_KP";
	private static final String RefSet_Min_DTC_KP = "org.image.pass.pixie.refSet.Min_DTC_KP";
	private static final String RefSet_Max_DTC_KP = "org.image.pass.pixie.refSet.Max_DTC_KP";
	private static final String RefSet_NumWhite_Temp = "org.image.pass.pixie.refSet.NumWhite_Temp";
	private static final String RefSet_Avg_NumWhite = "org.image.pass.pixie.refSet.Avg_NumWhite";
	private static final String RefSet_Min_NumWhite = "org.image.pass.pixie.refSet.Min_NumWhite";
	private static final String RefSet_Max_NumWhite = "org.image.pass.pixie.refSet.Max_NumWhite";
	private static final String RefSet_DTC_Pixel_Template = "org.image.pass.pixie.refSet.DTC_Pixel_Template";
	private static final String RefSet_Avg_DTC_Pixel = "org.image.pass.pixie.refSet.Avg_DTC_Pixel";
	private static final String RefSet_Min_DTC_Pixel = "org.image.pass.pixie.refSet.Min_DTC_Pixel";
	private static final String RefSet_Max_DTC_Pixel = "org.image.pass.pixie.refSet.Max_DTC_Pixel";
	private static final String RefSet_avgDistRef = "org.image.pass.pixie.refSet.avgDistRef";
	private static final String RefSet_minDistRef = "org.image.pass.pixie.refSet.minDistRef"; 
	private static final String RefSet_maxDistRef = "org.image.pass.pixie.refSet.maxDistRef";

	private static PreferenceManager sInstance;
	private final SharedPreferences pref;

	private PreferenceManager (Context context){
		pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public static synchronized void initializeInstance (Context context){
		if(sInstance == null)
			sInstance = new PreferenceManager(context);
	}

	public static synchronized PreferenceManager getInstance(){
		if(sInstance == null){
			throw new IllegalStateException(PreferenceManager.class.getSimpleName() +
					"is not initialized, call initializeInstance(..) method first.");

		}
		return sInstance;
	}

	
	public static synchronized boolean isInitialized(){
		if(sInstance == null){
			return false;
		}
		else
			return true;
	}
	
	public void remove(String key){
		if(pref.contains(key))
			pref.edit().remove(key).commit();
	}

	public boolean clear(){
		return pref.edit().clear().commit();
	}

	public void initiate(){
		if(!pref.contains(IS_PREF_SET))
			PreferenceManager.getInstance().setIsPrefSet(false);
		if(!pref.contains(NUMRUN))
			PreferenceManager.getInstance().setNumRun(1);
		else
			PreferenceManager.getInstance().setNumRun(PreferenceManager.getInstance().getNumRun() + 1);
	}
	
	public boolean isPrefSet(){
		//if the refset set
		if(pref.getString(IS_PREF_SET, null).equals("True"))
			return true;
		else
			return false;
	}
	
	public boolean isFIUSet(){
		if(!pref.contains(FIU_USERNAME) || !pref.contains(FIU_USERNAME))
			return false;
		else
			return true;
	}
	
	public void setIsPrefSet(boolean value) {
		if(value)
			pref.edit().putString(IS_PREF_SET, "True").commit();
		else
			pref.edit().putString(IS_PREF_SET, "False").commit();
	}
	
	public void setFiuUserName(String value) {
		if(value.length() > 0 && value != null)
			pref.edit().putString(FIU_USERNAME, value.trim()).commit();
	}
	
	public String getFiuUserName() {
		return pref.getString(FIU_USERNAME, null);
	}
	
	public void setFiuPassword(String value) {
		if(value.length() > 0 && value != null)
			pref.edit().putString(FIU_PASS, value.trim()).commit();
	}
	
	public String getFiuPassword() {
		return pref.getString(FIU_PASS, null);
	}
	
	public int getNumRun(){
		String str = pref.getString(NUMRUN, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}
	
	
	public void setNumRun(int value) {
		String json =  String.valueOf(value);  //gson.toJson(des);
		pref.edit().putString(NUMRUN, json).commit();
	}
	
	public String getIsPrefSet() {
		return pref.getString(IS_PREF_SET, null);
	}

	
	//filePath name
	public void setPassFilePath (String value){
		pref.edit().putString(PASS_FILE_PATH, value).commit();
	}

	public String getPassFilePath(){
		return pref.getString(PASS_FILE_PATH, null);
	}

	// Image mat file
	public void setPassMat(Mat mat){
		String json = OpenCVObjectsToJSON.matToJson(mat); //= gson.toJson(mat);
		pref.edit().putString(PASS_Mat, json).commit();
	}

	public Mat getPassMat(){
		String str = pref.getString(PASS_Mat, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.matFromJson(str);
		else
			return null;
	}

	//Image Keypoints
	public void setPassKeypoints(MatOfKeyPoint keypoint){
		String json =  OpenCVObjectsToJSON.keypointsToJson(keypoint);  //gson.toJson(des);
		pref.edit().putString(PASS_KP, json).commit();
	}

	public MatOfKeyPoint getPassKeypoints(){
		String str = pref.getString(PASS_KP, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.keypointsFromJson(str);
		else 
			return null;
	}

	//Image Descriptor
	public void setPassDescriptors(Mat des){
		String json =  OpenCVObjectsToJSON.matToJson(des);  //gson.toJson(des);
		pref.edit().putString(PASS_Descriptors, json).commit();
	}

	public Mat getPassDescriptors(){
		String str = pref.getString(PASS_Descriptors, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.matFromJson(str);
		else 
			return null;
	}

	//filePath name
	public void setPassFilePath1 (String value){
		pref.edit().putString(PASS_FILE_PATH1, value).commit();
	}

	public String getPassFilePath1(){
		return pref.getString(PASS_FILE_PATH1, null);
	}

	// Image mat file
	public void setPassMat1(Mat mat){
		String json = OpenCVObjectsToJSON.matToJson(mat); //= gson.toJson(mat);
		pref.edit().putString(PASS_Mat1, json).commit();
	}

	public Mat getPassMat1(){
		String str = pref.getString(PASS_Mat1, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.matFromJson(str);
		else
			return null;
	}

	//Image Keypoints
	public void setPassKeypoints1(MatOfKeyPoint keypoint){
		String json =  OpenCVObjectsToJSON.keypointsToJson(keypoint);  //gson.toJson(des);
		pref.edit().putString(PASS_KP1, json).commit();
	}

	public MatOfKeyPoint getPassKeypoints1(){
		String str = pref.getString(PASS_KP1, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.keypointsFromJson(str);
		else 
			return null;
	}

	//Image Descriptor
	public void setPassDescriptors1(Mat des){
		String json =  OpenCVObjectsToJSON.matToJson(des);  //gson.toJson(des);
		pref.edit().putString(PASS_Descriptors1, json).commit();
	}

	public Mat getPassDescriptors1(){
		String str = pref.getString(PASS_Descriptors1, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.matFromJson(str);
		else 
			return null;
	}

	//filePath name
	public void setPassFilePath2 (String value){
		pref.edit().putString(PASS_FILE_PATH2, value).commit();
	}

	public String getPassFilePath2(){
		return pref.getString(PASS_FILE_PATH2, null);
	}

	// Image mat file
	public void setPassMat2(Mat mat){
		String json = OpenCVObjectsToJSON.matToJson(mat); //= gson.toJson(mat);
		pref.edit().putString(PASS_Mat2, json).commit();
	}

	public Mat getPassMat2(){
		String str = pref.getString(PASS_Mat2, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.matFromJson(str);
		else
			return null;
	}

	//Image Keypoints
	public void setPassKeypoints2(MatOfKeyPoint keypoint){
		String json =  OpenCVObjectsToJSON.keypointsToJson(keypoint);  //gson.toJson(des);
		pref.edit().putString(PASS_KP2, json).commit();
	}

	public MatOfKeyPoint getPassKeypoints2(){
		String str = pref.getString(PASS_KP2, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.keypointsFromJson(str);
		else 
			return null;
	}

	//Image Descriptor
	public void setPassDescriptors2(Mat des){
		String json =  OpenCVObjectsToJSON.matToJson(des);  //gson.toJson(des);
		pref.edit().putString(PASS_Descriptors2, json).commit();
	}

	public Mat getPassDescriptors2(){
		String str = pref.getString(PASS_Descriptors2, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.matFromJson(str);
		else 
			return null;
	}

	public void setRefSetTemplateID(int id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_TemplateID, json).commit();
	}

	public int getRefSetTemplateID(){
		String str = pref.getString(RefSet_TemplateID, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}

	public void setRefSetMeanNNDist(double meanNNDist){
		String json =  String.valueOf(meanNNDist);  //gson.toJson(des);
		pref.edit().putString(RefSet_MeanNNDist, json).commit();
	}

	public double getRefSetMeanNNDist(){
		String str = pref.getString(RefSet_MeanNNDist, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetMeanFNDist(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_MeanFNDist, json).commit();
	}

	public double getRefSetMeanFNDist(){
		String str = pref.getString(RefSet_MeanFNDist, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetMeanTempDist(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_MeanTempDist, json).commit();
	}

	public double getRefSetMeanTempDist(){
		String str = pref.getString(RefSet_MeanTempDist, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetCrossSimilarity(double[][] data){
		String json =  OpenCVObjectsToJSON.double2DArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_CrossSimilarity, json).commit();
	}

	public double[][] getRefSetCrossSimilarity(){
		String str = pref.getString(RefSet_CrossSimilarity, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToDouble2DArray(str, 3);
		else 
			return null;
	}

	public void setRefSetNearesNeighborDist(double[] data){
		String json =  OpenCVObjectsToJSON.doubleArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_NearesNeighborDist, json).commit();
	}

	public double[] getRefSetNearesNeighborDist(){
		String str = pref.getString(RefSet_NearesNeighborDist, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToDoubleArray(str, 3);
		else 
			return null;
	}

	public void setRefSetFarthestNeighborDist(double[] data){
		String json =  OpenCVObjectsToJSON.doubleArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_FarthestNeighborDist, json).commit();
	}

	public double[] getRefSetFarthestNeighborDist(){
		String str = pref.getString(RefSet_FarthestNeighborDist, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToDoubleArray(str, 3);
		else 
			return null;
	}

	public void setRefSetToTemplateDist(double[] data){
		String json =  OpenCVObjectsToJSON.doubleArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_ToTemplateDist, json).commit();
	}

	public double[] getRefSetToTemplateDist(){
		String str = pref.getString(RefSet_ToTemplateDist, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToDoubleArray(str, 3);
		else 
			return null;
	}

	public void setRefSetNearestNeighbor(int[] data){
		String json =  OpenCVObjectsToJSON.intArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_NearestNeighbor, json).commit();
	}

	public int[] getRefSetNearestNeighbor(){
		String str = pref.getString(RefSet_NearestNeighbor, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToIntArray(str, 3);
		else 
			return null;
	}

	public void setRefSetFarthestNeighbor(int[] data){
		String json =  OpenCVObjectsToJSON.intArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_FarthestNeighbor, json).commit();
	}

	public int[] getRefSetFarthestNeighbor(){
		String str = pref.getString(RefSet_FarthestNeighbor, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToIntArray(str, 3);
		else 
			return null;
	}

//------------------
	public void setRefSetNumKP_Temp(int id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_NumKP_Temp, json).commit();
	}

	public int getRefSetNumKP_Temp(){
		String str = pref.getString(RefSet_NumKP_Temp, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}

	public void setRefSetAvg_NumKP(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Avg_NumKP, json).commit();
	}

	public double getRefSetAvg_NumKP(){
		String str = pref.getString(RefSet_Avg_NumKP, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetMin_NumKP(int id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Min_NumKP, json).commit();
	}

	public int getRefSetMin_NumKP(){
		String str = pref.getString(RefSet_Min_NumKP, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}

	public void setRefSetMax_NumKP(int id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Max_NumKP, json).commit();
	}

	public int getRefSetMax_NumKP(){
		String str = pref.getString(RefSet_Max_NumKP, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}


	public void setRefSetDTC_KP_Temp(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_DTC_KP_Temp, json).commit();
	}

	public double getRefSetDTC_KP_Temp(){
		String str = pref.getString(RefSet_DTC_KP_Temp, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetAvg_DTC_KP(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Avg_DTC_KP, json).commit();
	}

	public double getRefSetAvg_DTC_KP(){
		String str = pref.getString(RefSet_Avg_DTC_KP, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetMin_DTC_KP(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Min_DTC_KP, json).commit();
	}

	public double getRefSetMin_DTC_KP(){
		String str = pref.getString(RefSet_Min_DTC_KP, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetMax_DTC_KP(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Max_DTC_KP, json).commit();
	}

	public double getRefSetMax_DTC_KP(){
		String str = pref.getString(RefSet_Max_DTC_KP, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}	


	public void setRefSetNumWhite_Temp(int id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_NumWhite_Temp, json).commit();
	}

	public int getRefSetNumWhite_Temp(){
		String str = pref.getString(RefSet_NumWhite_Temp, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}

	public void setRefSetAvg_NumWhite(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Avg_NumWhite, json).commit();
	}

	public double getRefSetAvg_NumWhite(){
		String str = pref.getString(RefSet_Avg_NumWhite, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}	


	public void setRefSetMin_NumWhite(int id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Min_NumWhite, json).commit();
	}

	public int getRefSetMin_NumWhite(){
		String str = pref.getString(RefSet_Min_NumWhite, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}

	public void setRefSetMax_NumWhite(int id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Max_NumWhite, json).commit();
	}

	public int getRefSetMax_NumWhite(){
		String str = pref.getString(RefSet_Max_NumWhite, null);
		if(str!=null && !str.equals(""))
			return Integer.parseInt(str);
		else 
			return -1;
	}


	public void setRefSetDTC_Pixel_Template(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_DTC_Pixel_Template, json).commit();
	}

	public double getRefSetDTC_Pixel_Template(){
		String str = pref.getString(RefSet_DTC_Pixel_Template, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}	

	public void setRefSetAvg_DTC_Pixel(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Avg_DTC_Pixel, json).commit();
	}

	public double getRefSetAvg_DTC_Pixel(){
		String str = pref.getString(RefSet_Avg_DTC_Pixel, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}
	public void setRefSetMin_DTC_Pixel(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Min_DTC_Pixel, json).commit();
	}

	public double getRefSetMin_DTC_Pixel(){
		String str = pref.getString(RefSet_Min_DTC_Pixel, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}	
	public void setRefSetMax_DTC_Pixel(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_Max_DTC_Pixel, json).commit();
	}

	public double getRefSetMax_DTC_Pixel(){
		String str = pref.getString(RefSet_Max_DTC_Pixel, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}	

	public void setRefSetAvgDistRef(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_avgDistRef, json).commit();
	}

	public double getRefSetAvgDistRef(){
		String str = pref.getString(RefSet_avgDistRef, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}	

	public void setRefSetMinDistRef(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_minDistRef, json).commit();
	}

	public double getRefSetMinDistRef(){
		String str = pref.getString(RefSet_minDistRef, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetMaxDistRef(double id){
		String json =  String.valueOf(id);  //gson.toJson(des);
		pref.edit().putString(RefSet_maxDistRef, json).commit();
	}

	public double getRefSetMaxDistRef(){
		String str = pref.getString(RefSet_maxDistRef, null);
		if(str!=null && !str.equals(""))
			return Double.parseDouble(str);
		else 
			return -1;
	}

	public void setRefSetNumWhitePixels(int[] data){
		String json =  OpenCVObjectsToJSON.intArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_numWhitePixels, json).commit();
	}

	public int[] getRefSetNumWhitePixels(){
		String str = pref.getString(RefSet_numWhitePixels, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToIntArray(str, 3);
		else 
			return null;
	}

	public void setRefSetDTCPixels(double[] data){
		String json =  OpenCVObjectsToJSON.doubleArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_DTCPixels, json).commit();
	}

	public double[] getRefSetDTCPixels(){
		String str = pref.getString(RefSet_DTCPixels, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToDoubleArray(str, 3);
		else 
			return null;
	}

	public void setRefSetDTCKPs(double[] data){
		String json =  OpenCVObjectsToJSON.doubleArrayToString(data);  //gson.toJson(des);
		pref.edit().putString(RefSet_DTCKPs, json).commit();
	}

	public double[] getRefSetDTCKPs(){
		String str = pref.getString(RefSet_DTCKPs, null);
		if(str!=null && !str.equals(""))
			return OpenCVObjectsToJSON.stringToDoubleArray(str, 3);
		else 
			return null;
	}
	
	public void setReferenceSetInPreferences(ReferenceSet refSet){
		setRefSetCrossSimilarity(refSet.getCrossSimilarity());
		setRefSetFarthestNeighbor(refSet.getFarthestNeighbor());
		setRefSetFarthestNeighborDist(refSet.getFarthestNeighborDist());
		setRefSetMeanFNDist(refSet.getMeanFNDist());
		setRefSetMeanNNDist(refSet.getMeanNNDist());
		setRefSetMeanTempDist(refSet.getMeanTempDist());
		setRefSetNearesNeighborDist(refSet.getNearesNeighborDist());
		setRefSetNearestNeighbor(refSet.getNearestNeighbor());
		setRefSetTemplateID(refSet.getTemplateID());
		setRefSetToTemplateDist(refSet.getToTemplateDist());
		//------------
		setRefSetNumKP_Temp(refSet.getNumKP_Temp());
		setRefSetAvg_NumKP(refSet.getAvg_NumKP());
		setRefSetMin_NumKP(refSet.getMin_NumKP());
		setRefSetMax_NumKP(refSet.getMax_NumKP());
		setRefSetDTC_KP_Temp(refSet.getDTC_KP_Temp());
		setRefSetAvg_DTC_KP(refSet.getAvg_DTC_KP());
		setRefSetMin_DTC_KP(refSet.getMin_DTC_KP());
		setRefSetMax_DTC_KP(refSet.getMax_DTC_KP());
		setRefSetNumWhite_Temp(refSet.getNumWhite_Temp());
		setRefSetAvg_NumWhite(refSet.getAvg_NumWhite());
		setRefSetMin_NumWhite(refSet.getMin_NumWhite());
		setRefSetMax_NumWhite(refSet.getMax_NumWhite());
		setRefSetDTC_Pixel_Template(refSet.getDTC_Pixel_Template());
		setRefSetAvg_DTC_Pixel(refSet.getAvg_DTC_Pixel());
		setRefSetMin_DTC_Pixel(refSet.getMin_DTC_Pixel());	
		setRefSetMax_DTC_Pixel(refSet.getMax_DTC_Pixel());
		setRefSetAvgDistRef(refSet.getAvgDistRef());		
		setRefSetMinDistRef(refSet.getMinDistRef());
		setRefSetMaxDistRef(refSet.getMaxDistRef());
		setRefSetNumWhitePixels(arrayListOfIntegerToArray(refSet.getNumWhitePixels()));
		setRefSetDTCPixels(arrayListOfDoubleToArray(refSet.getDTCPixels()));
		setRefSetDTCKPs(arrayListOfDoubleToArray(refSet.getDTCKPs()));
		
	}
	
	public ReferenceSet getReferenceSetInPreferences(){
		ReferenceSet refSet = new ReferenceSet();
		//refSetIndex
		for(int i = 0 ; i < MainActivity.numRefImages; i++){
			refSet.getRefSetIndex().add(i);
		}
		//RefSet Keypoints and Descriptors
		refSet.getReferenceSet().add(this.getPassMat());
		refSet.getDescriptors().add(this.getPassDescriptors());
		refSet.getKeypoints().add(this.getPassKeypoints());
		
		refSet.getReferenceSet().add(this.getPassMat1());
		refSet.getDescriptors().add(this.getPassDescriptors1());
		refSet.getKeypoints().add(this.getPassKeypoints1());
		
		refSet.getReferenceSet().add(this.getPassMat2());
		refSet.getDescriptors().add(this.getPassDescriptors2());
		refSet.getKeypoints().add(this.getPassKeypoints2());
		
		refSet.setCrossSimilarity(this.getRefSetCrossSimilarity());
		refSet.setFarthestNeighbor(this.getRefSetFarthestNeighbor());
		refSet.setFarthestNeighborDist(this.getRefSetFarthestNeighborDist());
		refSet.setMeanFNDist(this.getRefSetMeanFNDist());
		refSet.setMeanNNDist(this.getRefSetMeanNNDist());
		refSet.setMeanTempDist(this.getRefSetMeanTempDist());
		refSet.setNearesNeighborDist(this.getRefSetNearesNeighborDist());
		refSet.setNearestNeighbor(this.getRefSetNearestNeighbor());
		refSet.setTemplateID(this.getRefSetTemplateID());
		refSet.setToTemplateDist(this.getRefSetToTemplateDist());
		//---------------------------
		refSet.setNumKP_Temp(getRefSetNumKP_Temp());
		refSet.setAvg_NumKP(getRefSetAvg_NumKP());
		refSet.setMin_NumKP(getRefSetMin_NumKP());
		refSet.setMax_NumKP(getRefSetMax_NumKP());
		refSet.setDTC_KP_Temp(getRefSetDTC_KP_Temp());
		refSet.setAvg_DTC_KP(getRefSetAvg_DTC_KP());
		refSet.setMin_DTC_KP(getRefSetMin_DTC_KP());
		refSet.setMax_DTC_KP(getRefSetMax_DTC_KP());
		refSet.setNumWhite_Temp(getRefSetNumWhite_Temp());
		refSet.setAvg_NumWhite(getRefSetAvg_NumWhite());
		refSet.setMin_NumWhite(getRefSetMin_NumWhite());
		refSet.setMax_NumWhite(getRefSetMax_NumWhite());
		refSet.setDTC_Pixel_Template(getRefSetDTC_Pixel_Template());
		refSet.setAvg_DTC_Pixel(getRefSetAvg_DTC_Pixel());
		refSet.setMin_DTC_Pixel(getRefSetMin_DTC_Pixel());	
		refSet.setMax_DTC_Pixel(getRefSetMax_DTC_Pixel());
		refSet.setAvgDistRef(getRefSetAvgDistRef());		
		refSet.setMinDistRef(getRefSetMinDistRef());
		refSet.setMaxDistRef(getRefSetMaxDistRef());
		refSet.setNumWhitePixels(arrayOfIntegerToArrayList(this.getRefSetNumWhitePixels()));
		refSet.setDTCPixels( arrayOfDoubleToArrayList(this.getRefSetDTCPixels()));
		refSet.setDTCKPs( arrayOfDoubleToArrayList(this.getRefSetDTCKPs()));
		
		return refSet;
	}

	
	public double [] arrayListOfDoubleToArray(ArrayList<Double> data){
		double [] new_data = new double [data.size()];
		for(int i = 0 ; i < data.size(); i++)
			new_data[i] = data.get(i);
		return new_data;
			
	}

	public  int [] arrayListOfIntegerToArray(ArrayList<Integer> data){
		int [] new_data = new int [data.size()];
		for(int i = 0 ; i < data.size(); i++)
			new_data[i] = data.get(i);
		return new_data;
	}


	public ArrayList<Double> arrayOfDoubleToArrayList(double [] data){
		ArrayList<Double> new_data = new  ArrayList<Double>();
		for(int i = 0 ; i < data.length; i++)
			new_data.add(data[i]);
		return new_data;
			
	}

	public ArrayList<Integer>  arrayOfIntegerToArrayList(int [] data){
		 ArrayList<Integer> new_data = new  ArrayList<Integer>();
		for(int i = 0 ; i < data.length; i++)
			new_data.add(data[i]);
		return new_data;
	}

	public void logOut(){
		remove(FIU_USERNAME);
		remove(FIU_USERNAME);
	}
}
