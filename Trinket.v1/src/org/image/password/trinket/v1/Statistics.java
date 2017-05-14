package org.image.password.trinket.v1;
import java.util.ArrayList;

public class Statistics {


	private ArrayList<Double> data;
	double mean;
	double variance;
	double standardDeviation;
	double min, max;
	

	public ArrayList<Double> getData() {
		return data;
	}

	public void setData(ArrayList<Double> data) {
		this.data = data;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public Statistics(ArrayList<Double> data) {
		super();
		this.data = new ArrayList<Double>(data);
		this.mean = calculateMean(this.data);
		//this.variance = calculateVariance(this.data);
		//this.standardDeviation = calculateSD(this.data);
		calculateMinMax(this.data);
	}
	

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	private double calculateSD(ArrayList<Double> data2) {
		return Math.sqrt(getVariance());
	}

	private double calculateVariance(ArrayList<Double> data2) {
		double mean = calculateMean(data);
		double temp = 0;
		
		for (double a : data){	
			temp += (mean - a) * (mean - a);
		}
		return temp / data.size();

	}

	private double calculateMean(ArrayList<Double> data2) {
		
		double sum = 0;
		for (int i = 0; i < data.size(); i++) {;
			sum += data.get(i);
		}
		return sum / data.size();
		
	}

	private double [] calculateMinMax(ArrayList<Double> data){
		double [] res = new double[2];
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		for(int i = 0 ; i < data.size(); i++){
			if(data.get(i) < min)
				min = data.get(i);
			if(data.get(i) > max)
				max = data.get(i);
		}
		this.min = min;
		this.max = max;
		res[0] = min;
		res[1] = max;
		return res;
	}
}
