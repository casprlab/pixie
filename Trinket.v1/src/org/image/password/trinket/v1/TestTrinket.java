package org.image.password.trinket.v1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;

public class TestTrinket extends Activity {
	AudioManager meng;
	private int numRun;
	TooltipWindow tipWindow;
	private TableLayout nextTableView;
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;
	private ImageView capture, switchCamera, retryBtn, exitBtn;
	private Context myContext;
	private RelativeLayout cameraPreview;
	private boolean cameraFront = false;
	//---------------------------------------------
	protected String filePath, croppedPath;
	protected ProgressDialog mProgressDialog;
	protected AlertDialog.Builder badImageAlert;
	public static int numPassKeypoints = -1;
	public static boolean refSetAccepted = false;
	FeatureDetector detector;
	DescriptorExtractor descriptor;
	DescriptorMatcher matcher;
	private ReferenceSet refSetForTestPurposeOnly;
	public static boolean passImageFetched = false;
	public static int numMatch = -1;
	public static long end_time_image, start_time_image, takeImage_time;
	public String timingFile = "Timing_TestPass.txt";

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("OpenCVLoader", "OpenCV loaded successfully");
				detector = FeatureDetector.create(FeatureDetector.ORB);
				descriptor = DescriptorExtractor
						.create(DescriptorExtractor.ORB);
				matcher = DescriptorMatcher
						.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_trinket);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;
		initialize();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	public void initialize() {
		if(meng == null)
			meng = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);

		start_time_image = System.currentTimeMillis();
		SharedMethods.writeFileOnSDCard("----------------- New Session ---------------\n "
				,timingFile);
		cameraPreview = (RelativeLayout) findViewById(R.id.camera_preview);
		nextTableView = (TableLayout) findViewById(R.id.nextTableLayout);
		nextTableView.setVisibility(View.GONE);
		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);
		retryBtn = (ImageView) findViewById(R.id.retryBtnCamera);
		retryBtn.setOnClickListener(nextButtonListener);
		exitBtn = (ImageView) findViewById(R.id.exitBtnCamera);
		exitBtn.setOnClickListener(nextButtonListener);
		capture = (ImageView) findViewById(R.id.button_capture);
		capture.setOnClickListener(captrureListener);


		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(this);
		}
		numRun = PreferenceManager.getInstance().getNumRun();

		// Drawing the rectangle on camera view
		DrawOnTop mDraw = new DrawOnTop(getApplicationContext());
		addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));


		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(TestTrinket.this);
		}
		FetchPasswordInfoInBackground fetchPass = new FetchPasswordInfoInBackground();
		fetchPass.execute();
		//if(numRun == 1)
		showTooltip("Take a picture of your trinket");
	}

	@Override
	protected void onPause() {
		super.onPause();
		//when on Pause, release camera in order to be used from other applications
		releaseCamera();
		if(tipWindow != null && tipWindow.isTooltipShown())
			tipWindow.dismissTooltip();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(tipWindow != null && tipWindow.isTooltipShown())
			tipWindow.dismissTooltip();
	}

	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

		CameraActivity.numMatch = -1;
		CameraActivity.numPassKeypoints = -1;
		CameraActivity.refSetAccepted = false;

		if (!hasCamera(myContext)) {
			Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		if (mCamera == null) {
			//if the front facing camera does not exist
			if (findFrontFacingCamera() < 0) {
				Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
				switchCamera.setVisibility(View.GONE);
			}			 
			mCamera = Camera.open(findBackFacingCamera());

			mPicture = getPictureCallback();
			//-------------------------------------
			mCamera.setDisplayOrientation(90);

			//-------------------------------------
			mPreview.refreshCamera(mCamera);
		}
	}

	@Override
	public void onBackPressed() {
		//finish();
		gotoHome(false);
	}

	private void showTooltip(String text) {
		//new Handler().postDelayed(new TooltipRunnable(text), 1500L);
	}

	public class TooltipRunnable implements Runnable {
		private String data;

		public TooltipRunnable(String _data) {
			this.data = _data;
		}

		public void run() {
			if (!isFinishing()) {
				tipWindow = new TooltipWindow(TestTrinket.this, data);
				if (!tipWindow.isTooltipShown())
					tipWindow.showToolTip(capture);
			}
		}
	}



	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				cameraFront = true;
				break;
			}
		}
		return cameraId;
	}

	private int findBackFacingCamera() {
		int cameraId = -1;
		//Search for the back facing camera
		//get the number of cameras
		int numberOfCameras = Camera.getNumberOfCameras();
		//for every camera check
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				cameraFront = false;
				break;
			}
		}
		return cameraId;
	}
	public void chooseCamera() {
		//if the camera preview is the front
		if (cameraFront) {
			int cameraId = findBackFacingCamera();
			if (cameraId >= 0) {
				//open the backFacingCamera
				//set a picture callback
				//refresh the preview

				mCamera = Camera.open(cameraId);				
				mPicture = getPictureCallback();			
				mPreview.refreshCamera(mCamera);
			}
		} else {
			int cameraId = findFrontFacingCamera();
			if (cameraId >= 0) {
				//open the backFacingCamera
				//set a picture callback
				//refresh the preview

				mCamera = Camera.open(cameraId);
				mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		}
	}

	private boolean hasCamera(Context context) {
		//check if the device has camera
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	private PictureCallback getPictureCallback() {
		PictureCallback picture = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

				//defining the filePath and croppedPath string here and in main activity
				String [] fileNames = new String [2];
				fileNames = defineFilePathForTakenImage();
				String pictureFile = fileNames[0];
				String croppedPictureFile = fileNames[1];
				//------------------------------------------------
				if (pictureFile == null) {
					return;
				}
				try {
					//write the file
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
					//------------------------------------------------
					//crop photo
					Mat croppedPhoto = cropPicture();

					end_time_image = System.currentTimeMillis();
					SharedMethods.writeFileOnSDCard("Training: NoN"  + ", Cropping Time: " +
							String.valueOf(end_time_image - takeImage_time) + "\n",
							timingFile);

					ComputeNumKeypointsInBackground computeSimilarity = new ComputeNumKeypointsInBackground(croppedPictureFile, croppedPhoto);
					computeSimilarity.execute();
					//------------------------------------------------

				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}

				//refresh camera to continue preview
				mPreview.refreshCamera(mCamera);
			}
		};
		return picture;
	}

	private String [] defineFilePathForTakenImage(){
		String[] results = new String[2];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());

		this.filePath = Environment.getExternalStorageDirectory().getPath()		
				+ "/Pixie/Candidate/" + currentDateandTime;
		this.croppedPath = Environment.getExternalStorageDirectory()
				.getPath() + "/Pixie/Candidate/Cropped/" + currentDateandTime;

		this.filePath += "_testingTrinket";
		this.croppedPath += "_testingTrinket";


		this.filePath += ".jpg";
		this.croppedPath += ".jpg";
		results[0] = this.filePath;
		results[1] = this.croppedPath;
		return results;
	}

	AutoFocusCallback myAutoFocus = new AutoFocusCallback(){
		@Override
		public void onAutoFocus(boolean arg, Camera arg1){

		}
	};

	OnClickListener captrureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mCamera.autoFocus(myAutoFocus);
			mCamera.takePicture(null, null, mPicture);
			takeImage_time = System.currentTimeMillis();
			SharedMethods.writeFileOnSDCard("Training: NoN" + ", Capture Time: " +
					String.valueOf(takeImage_time - start_time_image) + "\n",
					timingFile);
			//---------------------------------------
			shootSound();
			//---------------------------------------

		}
	};


	// This will only available in reset trinket or training use case
	OnClickListener nextButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			CameraActivity.numPassKeypoints = -1;

			if(v.getId() == R.id.exitBtnCamera){
				
				gotoHome(false);
			}
			else if(v.getId() == R.id.retryBtnCamera){
				numMatch = -1;
				changeIconOfCamptureBtn(0);
				nextTableView.setVisibility(View.GONE);
				capture.setVisibility(View.VISIBLE);
			}

		}
	};

	private Mat cropPicture(){
		// ---------------------- Cropping the taken image ----------------
		String mPictureFileName = this.filePath;
		Mat image = Highgui.imread(mPictureFileName);		
		int width = (int) image.size().width;// display.getWidth();
		int height = (int) image.size().height;// display.getHeight();
		float radius = (float) 350.00;
		float a = (float) Math.sqrt(Math.pow(radius, 2)/2);
		int screenW =  MainActivity.ws;
		int screenH =  MainActivity.hs;
		int x1 = (int) (( a * height)/screenW) ;
		int y1 = (int) ((a * width)/screenH);
		org.opencv.core.Rect roi = new org.opencv.core.Rect((width / 2) - (y1-2), (height / 2) - (x1-2), (2 * (y1+2)),
				(2 * (x1+2)));
		Mat crop = new Mat(image, roi);
		/*String croppedPath = mPictureFileName.substring(0, mPictureFileName.lastIndexOf("/")) +  "/Cropped/" +
		mPictureFileName.substring(mPictureFileName.lastIndexOf("/")+1, mPictureFileName.length());
		 */
		Mat output = crop.clone();
		Highgui.imwrite(croppedPath, output);
		return output;
	}

	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void shootSound(){
		MediaPlayer _shootMP=null;

		int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);

		if (volume != 0)
		{
			if (_shootMP == null)
				_shootMP = MediaPlayer.create(getBaseContext(), Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
			if (_shootMP != null)
				_shootMP.start();
		}
	}

	@SuppressLint("DrawAllocation")
	class DrawOnTop extends View {
		public float recW = 0f;
		public float recH = 0f;

		public DrawOnTop(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub

			Paint paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);

			int h = canvas.getHeight();
			int w = canvas.getWidth();

			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(6);
			canvas.drawCircle(w / 2, h / 2, 350, paint);


			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(7);
			paint.setStyle(Style.STROKE);
			paint.setPathEffect(new DashPathEffect(new float[] {30,10}, 0));
			canvas.drawCircle(w / 2, h / 2, 50, paint);
			float radius = (float) 350.00;
			float a = (float) Math.sqrt(Math.pow(radius, 2)/2);
			//float x = (float) Math.sqrt((Math.pow(radius, 2)) - (Math.pow(a/2, 2)));
			paint.setColor(Color.GRAY);
			paint.setStrokeWidth(5);
			paint.setStyle(Style.STROKE);
			paint.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));
			Rect roi = new Rect( (int)(w/2 - a), (int) ((h / 2) - a), (int)(w/2 + a),
					(int) ((h / 2) + a));
			canvas.drawPoint(w, h, paint);
			canvas.drawRect(roi, paint);
			super.onDraw(canvas);

		}
	}

	private class ComputeNumKeypointsInBackground extends
	AsyncTask<Void, Void, Double> {

		String croppedPath;
		Mat croppedPhoto;

		public ComputeNumKeypointsInBackground(String croppedPath, Mat croppedPhoto){
			this.croppedPath = croppedPath;
			this.croppedPhoto = croppedPhoto;
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			if (mProgressDialog != null) { 
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss(); 
				} 
			}
			mProgressDialog = ProgressDialog.show(TestTrinket.this, "Please wait",
					"Processing image");
		}

		@Override
		protected Double doInBackground(Void... params) {
			//-------------------  Finding Keypoints  --------------------------
			long start = System.currentTimeMillis();
			Mat descriptors1 = new Mat();
			MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
			detector.detect(croppedPhoto, keypoints1);
			descriptor.compute(croppedPhoto, keypoints1, descriptors1);			
			int temp_numKP = keypoints1.toList().size();
			long end = System.currentTimeMillis();
			SharedMethods.writeFileOnSDCard("Training: NoN" + ", KP_Extraction Time (BG): " +
					String.valueOf(end - start) + "\n",
					timingFile);
			//-------------------  Testing Password  --------------------------

			start = System.currentTimeMillis();
			CameraActivity.refSetAccepted = false;

			while(!passImageFetched){	
			}
			InstanceVerification_MoreFeatures ver = new InstanceVerification_MoreFeatures(refSetForTestPurposeOnly, croppedPhoto, keypoints1, descriptors1 );
			String classificationResult = ver.testInstance(AutomaticPreFilter.instance.getContext());
			if(classificationResult.equals("1"))
				numMatch = 1;
			else numMatch = 0 ;
			end = System.currentTimeMillis();
			SharedMethods.writeFileOnSDCard("Training: NoN" +  ", Test Image Time Total: " +
					String.valueOf(end - start) + "\n",
					timingFile);

			CameraActivity.numPassKeypoints = temp_numKP;

			SharedMethods.writeFileOnSDCard("Training: NoN" + ", NumKP: " +
					String.valueOf(temp_numKP) + "\n",
					timingFile);

			double x = 0.002;
			return x;
		}

		@Override
		protected void onPostExecute(Double result) {

			if (mProgressDialog != null) { 
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss(); 
				} 
			}


			String msg = isImageValidBasedOnNumKPs();
			if (msg != null){
				showAlert(msg);
				CameraActivity.numPassKeypoints = -1;
			}


			//-------------------  Testing Password  --------------------------

			//instructionTxtView.setVisibility(View.INVISIBLE);
			//instructionFrame.setVisibility(View.INVISIBLE);
			if(numMatch == 1){
				Toast.makeText(TestTrinket.this, "Correct Trinket", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(TestTrinket.this, "Incorrect Trinket", Toast.LENGTH_SHORT).show();

			}
			numMatch = -1;
			CameraActivity.numPassKeypoints = -1;
			capture.setVisibility(View.GONE);
			nextTableView.setVisibility(View.VISIBLE);

		}



	}

	private class FetchPasswordInfoInBackground extends
	AsyncTask<Void, Void, Double> {
		long start = System.currentTimeMillis();
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected Double doInBackground(Void... params) {
			passImageFetched = false;
			//danger of infinte loop!!! :D 
			//(currently I cannot think of any situation that can result in infinite loop!)
			while(!PreferenceManager.getInstance().isPrefSet()){

			}

			//creating referenceset
			refSetForTestPurposeOnly = new ReferenceSet();
			refSetForTestPurposeOnly = PreferenceManager.getInstance().getReferenceSetInPreferences();

			//detector.detect(passMat, passKeypoints);
			passImageFetched = true;
			double x = 0.002;
			return x;
		}

		@Override
		protected void onPostExecute(Double result) {
			passImageFetched = true;
			long end = System.currentTimeMillis();
			SharedMethods.writeFileOnSDCard("Training: NoN"+ ", Fetch RefSet (BG): " +
					String.valueOf(end - start) + "\n",
					timingFile);

		}

	}

	private void showAlert(String msg) {
		badImageAlert = new AlertDialog.Builder(this);
		final LinearLayout layout       = new LinearLayout(this);
		final TextView message        = new TextView(this);
		ImageView img    = new ImageView(this);    
		//margine for message text
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(10, 10, 10, 10);
		message.setLayoutParams(lp);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		Bitmap bitmap = BitmapFactory.decodeFile(croppedPath, options);
		img.setImageBitmap(Bitmap.createScaledBitmap(
				SharedMethods.RotateBitmap(bitmap, 90), 400, 400, true));

		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(message);
		layout.addView(img);
		//badImageAlert.setTitle("Low quality photo!");
		badImageAlert.setTitle(msg);
		badImageAlert.setView(layout);
		badImageAlert.setCancelable(false);

		/*if(!settingPassword){
			message.setText(" Are you sure you want to keep this image? \n");
			badImageAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					badImageAlert = null;
				}
			});

			badImageAlert.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Delete the bad images 
					File file = new File(croppedPath);
					file.delete();
					file = new File(filePath);
					file.delete();
					//Clear the path from main activity to prevent accessing null file
					MainActivity.filePath = null;
					MainActivity.croppedPath = null;
					Toast.makeText(getBaseContext(), "Please retry!", Toast.LENGTH_SHORT).show();
					dialog.cancel();
					layout.removeAllViews();
					badImageAlert = null;

				}
			});

		}
		else{*/
		message.setText("Please retry! \n" /*+ "Number of keypoints: " + CameraActivity.numPassKeypoints + "\n"*/ );
		badImageAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Delete the bad images 
				File file = new File(croppedPath);
				file.delete();
				file = new File(filePath);
				file.delete();
				//Clear the path from main activity to prevent accessing null file

				dialog.cancel();
				layout.removeAllViews();
				badImageAlert = null;

			}
		});

		//}
		badImageAlert.show();

	}


	private void changeIconOfCamptureBtn(int id){
		//if(trainig)
		//	capture.setImageResource(R.drawable.ic_navigate_next);
		//else{
		if(id == 0)
			capture.setImageResource(R.drawable.capturegreenbtn);
		else if (id == 3)
			capture.setImageResource(R.drawable.captureonebtn);
		else if (id == 2)
			capture.setImageResource(R.drawable.capturetwobtn);
		else if (id == 1)
			capture.setImageResource(R.drawable.capturethreebtn);
		//}
	}

	private String isImageValidBasedOnNumKPs(){

		if(CameraActivity.numPassKeypoints < MainActivity.thresholdForNumKPs)
			return "Low quality photo!";
		else return null;

	}

	protected void startLoginActivity(boolean reset) {
		MainActivity.refImageCaptureCounter = 0;
		Intent intent = new Intent(TestTrinket.this, LoginActivity.class);
		intent.putExtra("reset", reset);
		startActivity(intent);
	}

	public void gotoHome(boolean exit){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.putExtra("EXIT", exit);
		startActivity(intent);
	}
}