package org.image.password.trinket.v1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Vector;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Environment;

public enum AutomaticPreFilter {
	instance;
	private Context context;
	private Instances preFilterDataset;
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
			this.preFilterDataset = new Instances
					(new BufferedReader(new InputStreamReader(this.context.getAssets().open("PreFilterDataset.arff"))));
			this.preFilterDataset.setClassIndex(this.preFilterDataset.numAttributes() - 1);			
			Vector vector = (Vector) SerializationHelper.read(this.context.getAssets().open("MLP_ORB_Prefilter.model"));
			this.mlp = (MultilayerPerceptron) vector.get(0);
			Instances header = (Instances) vector.get(1);
	
            this.initialized = true;
		}catch (Exception e) {
			e.printStackTrace();
		}	
	}

	
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * This method removes the extra features from a prefilter dataset
	 * @param data
	 * @return
	 */
	public Instances removeExtraFeaturesFromPrefilter(Instances data){
		Instances temp = new Instances(data);
		// ----------- Removing the first and last 3 features
		int[] indicesOfColumnsToRemove = new int[] {0, 1, 2, 19, 20, 21};
		//ignoring the distRef elements
		//int[] indicesOfColumnsToRemove = new int[] {0, 1, 2, 19, 20, 21, 22, 23, 24};
		Remove remove = new Remove();
		remove.setAttributeIndicesArray(indicesOfColumnsToRemove);
		//remove.setInvertSelection(true);
		try {
			remove.setInputFormat(temp);
			temp = Filter.useFilter(temp, remove);
			temp.setClassIndex(temp.numAttributes() - 1);
			return temp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void addPrefilterDataToDataset(int NumKP_Temp, double Avg_NumKP_set, int Min_NumKP_set, 
			int Max_NumKP_set, double DTC_Temp, double Avg_DTC_set, double Min_DTC_set,double Max_DTC_set,
			int NumWhite_Temp, double Avg_NumWhite_set, int Min_NumWhite_set, int Max_NumWhite_set, double DTC_Pixel_Temp,   
			double Avg_DTC_Pixel_set, double Min_DTC_Pixel_set, double Max_DTC_Pixel_set, double avgDistRef, double minDistRef,
			double maxDistRef, String classificationRes){

		Instance inst  = new DenseInstance(this.preFilterDataset.numAttributes());
		inst.setDataset(this.preFilterDataset); 
		inst.setValue(0, NumKP_Temp); 
		inst.setValue(1, Avg_NumKP_set); 
		inst.setValue(2, Min_NumKP_set); 
		inst.setValue(3, Max_NumKP_set); 
		inst.setValue(4, DTC_Temp); 
		inst.setValue(5, Avg_DTC_set); 
		inst.setValue(6, Min_DTC_set); 
		inst.setValue(7, Max_DTC_set); 
		inst.setValue(8, NumWhite_Temp); 
		inst.setValue(9, Avg_NumWhite_set); 
		inst.setValue(10, Min_NumWhite_set); 
		inst.setValue(11, Max_NumWhite_set); 
		inst.setValue(12, DTC_Pixel_Temp); 
		inst.setValue(13, Avg_DTC_Pixel_set); 
		inst.setValue(14, Min_DTC_Pixel_set); 
		inst.setValue(15, Max_DTC_Pixel_set); 
		inst.setValue(16, avgDistRef); 
		inst.setValue(17, minDistRef); 
		inst.setValue(18, maxDistRef); 
		inst.setValue(19,"1"); 
		//add the new instance to the main dataset at the last position
		this.preFilterDataset.add(inst);
	}

	public String testInstance(ReferenceSet refSet){
		//create the prefilter record for the referenceSet
		AutomaticPreFilter.instance.addPrefilterDataToDataset(
				refSet.getNumKP_Temp(), refSet.getAvg_NumKP(),
				refSet.getMin_NumKP(), refSet.getMax_NumKP(),
				refSet.getDTC_KP_Temp(),
				refSet.getAvg_DTC_KP(), refSet.getMin_DTC_KP(),
				refSet.getMax_DTC_KP(),
				refSet.getNumWhite_Temp(),
				refSet.getAvg_NumWhite(),
				refSet.getMin_NumWhite(),
				refSet.getMax_NumWhite(),
				refSet.getDTC_Pixel_Template(),
				refSet.getAvg_DTC_Pixel(),
				refSet.getMin_DTC_Pixel(),
				refSet.getMax_DTC_Pixel(),
				refSet.getAvgDistRef(), refSet.getMinDistRef(),
				refSet.getMaxDistRef(), "1");
		
		String pred = "";
		try {
            for (int i = 0; i < this.preFilterDataset.numInstances(); i++) {
            	double clsLabel = this.mlp.classifyInstance(this.preFilterDataset.instance(i));
            	pred = this.preFilterDataset.classAttribute().value((int) clsLabel);
            }
            writeDataRecordToFile(this.preFilterDataset);
        } catch (IOException ex) {
        	ex.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.preFilterDataset.remove(0);
		return pred;	
	}
	
	public void writeDataRecordToFile(Instances dataSet){
		StringBuilder strBuilder = new StringBuilder();
		for(int i = 0 ; i < dataSet.numAttributes() - 1; i++){
			strBuilder.append(String.valueOf(dataSet.get(0).value(i)) + ", ");
		}
		strBuilder.append(String.valueOf(dataSet.get(0).value(dataSet.numAttributes()-1)));
		SharedMethods.writeFileOnSDCard(strBuilder.toString() + "\n",
				"PreFilterDataSetCollected.arff");
	}


}
