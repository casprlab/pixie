package org.image.password.trinket.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import android.content.Context;
import android.os.Environment;

public enum TestImageClassifier {

	instance;
	private Context context;
	private Instances dataset;
	private MultilayerPerceptron mlp;
	private boolean initialized = false;
	public static final long serialVersionUID = 0L;
	
	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}

	public void Initialization(Context context){
		try {
			this.context = context;
			
			String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
			this.dataset = new Instances
					(new BufferedReader(new InputStreamReader(this.context.getAssets().open("Dataset.arff"))));
			this.dataset.setClassIndex(this.dataset.numAttributes() - 1);
			
			//ObjectInputStream ois = new ObjectInputStream(
            //        new FileInputStream(sdcard + "/Pixie/Models/MLP_ORB_PreFilter.model"));
			//ObjectInputStream ois = new ObjectInputStream(this.context.getAssets().open("MLP_ORB_Prefilter.model"));
			//this.mlp = (MultilayerPerceptron) ois.readObject();
			//ois.close();
			//this.context.getAssets().open("MLP_ORB_Prefilter.model")
			
			Vector vector = (Vector) SerializationHelper.read(this.context.getAssets().open("MLP_ORB_WholeData2.model"));
			this.mlp = (MultilayerPerceptron) vector.get(0);
			Instances header = (Instances) vector.get(1);
	
            this.initialized = true;
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void addDataRecordToDataset(double minSim, double maxSim, double tempSim, 
			double MeanMinSim, double MeanMaxSim, double MeanTempSim, int numKPQ,int numKPT,
			int numMatch, double maxDist, double minDist, double meanDist, double stdDist,   
			double meanObjSize, double stdObjSize, double meanSceSize, double stdSceSize, double meanObjRes,
			double stdObjResp, double meanSceResp, double stdSceResp, double meanObjAngle, double stdObjAngle,
			double meanSceAngle, double stdSceAngle, double h1, double h2, double h3, int numMatchHomo){
		Instance inst  = new DenseInstance(this.dataset.numAttributes());
		inst.setDataset(this.dataset); 
		inst.setValue(0, minSim); 
		inst.setValue(1, maxSim); 
		inst.setValue(2, tempSim); 
		inst.setValue(3, MeanMinSim); 
		inst.setValue(4, MeanMaxSim); 
		inst.setValue(5, MeanTempSim); 
		inst.setValue(6, numKPQ); 
		inst.setValue(7, numKPT); 
		inst.setValue(8, numMatch); 
		inst.setValue(9, maxDist); 
		inst.setValue(10, minDist); 
		inst.setValue(11, meanDist); 
		inst.setValue(12, stdDist); 
		inst.setValue(13, meanObjSize); 
		inst.setValue(14, stdObjSize); 
		inst.setValue(15, meanSceSize); 
		inst.setValue(16, stdSceSize); 
		inst.setValue(17, meanObjRes); 
		inst.setValue(18, stdObjResp); 
		inst.setValue(19, meanSceResp); 
		inst.setValue(20, stdSceResp); 
		inst.setValue(21, meanObjAngle); 
		inst.setValue(22, stdObjAngle); 
		inst.setValue(23, meanSceAngle); 
		inst.setValue(24, stdSceAngle); 
		inst.setValue(25, h1); 
		inst.setValue(26, h2); 
		inst.setValue(27, h3); 
		inst.setValue(28, numMatchHomo); 
		inst.setValue(29,"0"); 
		//add the new instance to the main dataset at the last position
		this.dataset.add(inst);
	}

	public String testInstance(){
		String pred = "";
		try {
            for (int i = 0; i < this.dataset.numInstances(); i++) {
            	double clsLabel = this.mlp.classifyInstance(this.dataset.instance(i));
            	pred = this.dataset.classAttribute().value((int) clsLabel);
            }
            writeDataRecordToFile(this.dataset);
        } catch (IOException ex) {
        	ex.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.dataset.remove(0);
		return pred;	
	}
	
	public void writeDataRecordToFile(Instances dataSet){
		StringBuilder strBuilder = new StringBuilder();
		for(int i = 0 ; i < dataSet.numAttributes() - 1; i++){
			strBuilder.append(String.valueOf(dataSet.get(0).value(i)) + ", ");
		}
		strBuilder.append(String.valueOf(dataSet.get(0).value(dataSet.numAttributes()-1)));
		SharedMethods.writeFileOnSDCard(strBuilder.toString() + "\n",
				"DataSetCollected.arff");
	}



}
