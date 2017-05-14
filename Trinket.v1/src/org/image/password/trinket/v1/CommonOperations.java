package org.image.password.trinket.v1;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;

public enum CommonOperations {
	common;
	public double[] maxIndexExceptSelfIndex(double[] similarity, int selfID) {
		double max = 0;
		int maxIndex = -1;
		for (int i = 0; i < similarity.length; i++) {
			if (similarity[i] >= max && i != selfID) {
				max = similarity[i];
				maxIndex = i;
			}
		}
		double[] result = new double[2];
		result[0] = maxIndex;
		result[1] = max;
		return result;
	}

	public double[] minIndexExceptSelfIndex(double[] similarity, int selfID) {
		double min;
		int minIndex;
		if (selfID != 0){
			min = similarity[0];
			minIndex = 0;
		}
		else{
			min = similarity[1];
			minIndex = 1;
		}
		for (int i = 0; i < similarity.length; i++) {
			if (similarity[i] <= min && i != selfID) {
				min = similarity[i];
				minIndex = i;
			}
		}
		double[] result = new double[2];
		result[0] = minIndex;
		result[1] = min;
		return result;
	}

	public double calculateMean(double[] data, int selfID) {

		double sum = 0;
		for (int i = 0; i < data.length; i++) {
				sum += data[i];
		}
		return (sum - data[selfID]) / (data.length - 1);

	}
	
	public double calculateMean(double[] data) {

		double sum = 0;
		for (int i = 0; i < data.length; i++) {
				sum += data[i];
		}
		return (sum ) / (data.length);

	}


	public int findMaxIndex(double[] data) {
		double max = 0;
		int maxIndex = -1;
		for (int i = 0; i < data.length; i++) {
			if (data[i] >= max) {
				max = data[i];
				maxIndex = i;
			}
		}
		return maxIndex;

	}
	
	public double[][] transposeMatrix(double[][] m) {
		double[][] temp = new double[m[0].length][m.length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				temp[j][i] = m[i][j];
		return temp;
	}
	
	public double [] findMinMax(double [] data){
		double min = data[0];
		double max = 0;
		
		double [] result = new double [2];
		for (int i = 0 ; i < data.length; i++){
			if(data[i] < min)
				min = data[i];
			if(data[i] > max)
				max = data[i];
		}
		result[0] = min;
		result [1] = max;
		return result;
	}
	public double [] findMinMax_withIndex(double [] data){
		double min = data[0];
		double max = 0;
		double min_index = 0;
		double max_index = -1;
		
		double [] result = new double [4];
		for (int i = 0 ; i < data.length; i++){
			if(data[i] < min){
				min = data[i];
				min_index = i;
			}
			if(data[i] > max){
				max = data[i];
				max_index = i;
			}
		}
		result[0] = min;
		result[1] = min_index;
		result [2] = max;
		result[3] = max_index;
		return result;
	}

	public double [] findStatisticsOfReferenceSet(double [][] crossSimilarity){
		double min = crossSimilarity[0][0];
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
	
	public String getCurrentDateAndTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());
		return currentDateandTime;
	}

}

