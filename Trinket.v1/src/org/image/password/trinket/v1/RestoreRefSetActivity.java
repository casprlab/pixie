package org.image.password.trinket.v1;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RestoreRefSetActivity extends ListActivity {
	private List<String> item = null;
	private List<String> path = null;
	private String root;
	private TextView myPath;
	private int counter = 0;
	private List<Mat> refSetImages;
	private List<MatOfKeyPoint> keypoints;
	private List<Mat> descriptors;
	FeatureDetector detector;
	DescriptorExtractor descriptor;
	protected ProgressDialog mProgressDialog;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore_ref_set);
		myPath = (TextView)findViewById(R.id.path);

		root = Environment.getExternalStorageDirectory().getPath() + "/Pixie/Reference/Cropped";

		getDir(root);
	}
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("OpenCVLoader", "OpenCV loaded successfully");
				detector = FeatureDetector.create(FeatureDetector.ORB);
				descriptor = DescriptorExtractor
						.create(DescriptorExtractor.ORB);
			}
			break;
			default: {
				super.onManagerConnected(status);
			}
			break;
			}
		}
	};

	
	@Override
	public void onResume(){
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(RestoreRefSetActivity.this);
		}
		counter = 0;
		refSetImages = new ArrayList<Mat>();
		keypoints = new ArrayList<MatOfKeyPoint>();
		descriptors = new ArrayList<Mat>();
	}

	private void getDir(String dirPath)
	{
		myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();

		if(!dirPath.equals(root))
		{
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent()); 
		}

		for(int i=0; i < files.length; i++)
		{
			File file = files[i];

			if(!file.isHidden() && file.canRead()){
				path.add(file.getPath());
				if(file.isDirectory()){
					item.add(file.getName() + "/");
				}else{
					item.add(file.getName());
				}
			} 
		}

		ArrayAdapter<String> fileList =
				new ArrayAdapter<String>(this, R.layout.row, item);
		setListAdapter(fileList); 
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		File file = new File(path.get(position));

		if (file.isDirectory())
		{
			if(file.canRead()){
				getDir(path.get(position));
			}else{
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", null).show(); 
			} 
		}else {
			/*new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle("[" + file.getName() + "]")
			.setPositiveButton("OK", null).show();
			*/
			counter ++;
			Mat image = Highgui.imread(file.getAbsolutePath());
			this.refSetImages.add(image);
			Mat descriptors1 = new Mat();
			MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
			detector.detect(image, keypoints1);
			descriptor.compute(image, keypoints1, descriptors1);
			keypoints.add(keypoints1);
			descriptors.add(descriptors1);
			
			if(counter ==1 ){
				PreferenceManager.getInstance().setPassFilePath(file.getName());
				PreferenceManager.getInstance().setPassMat(image);
				PreferenceManager.getInstance().setPassDescriptors(descriptors1);
				PreferenceManager.getInstance().setPassKeypoints(keypoints1);
			}
			else if(counter == 2){
				PreferenceManager.getInstance().setPassFilePath1(file.getName());
				PreferenceManager.getInstance().setPassMat1(image);
				PreferenceManager.getInstance().setPassDescriptors1(descriptors1);
				PreferenceManager.getInstance().setPassKeypoints1(keypoints1);	
			}
			else if(counter == 3){
				PreferenceManager.getInstance().setPassFilePath2(file.getName());
				PreferenceManager.getInstance().setPassMat2(image);
				PreferenceManager.getInstance().setPassDescriptors2(descriptors1);
				PreferenceManager.getInstance().setPassKeypoints2(keypoints1);
				
				PutTheReferenceSetInPreferences put = new PutTheReferenceSetInPreferences();
				put.execute();
			}

		}
	}

	
	private class PutTheReferenceSetInPreferences extends
	AsyncTask<Void, Void, Double> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			if (mProgressDialog != null) { 
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss(); 
				} 
			}
			mProgressDialog = ProgressDialog.show(RestoreRefSetActivity.this, "Please wait",
					"Restoring Session");
		}

		@Override
		protected Double doInBackground(Void... params) {
			CameraActivity.isWritingPassword = true;
			ReferenceSet refSet = new ReferenceSet(refSetImages, new ArrayList<Integer>(Arrays.asList(0, 1, 2)), keypoints, descriptors);
			//calculate authentication features and store the reference set
			refSet.calculateAuthenticationFeatures();
			PreferenceManager.getInstance().setReferenceSetInPreferences(refSet);
			PreferenceManager.getInstance().setIsPrefSet(true);

			double x = 0.002;
			return x;
		}

		@Override
		protected void onPostExecute(Double result) {
			CameraActivity.isWritingPassword = false;
			if (mProgressDialog != null) { 
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss(); 
				} 
			}
		}

	}
}