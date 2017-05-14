package org.image.password.trinket.v1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.image.password.trinket.v1.R.drawable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

public class CameraActivity extends Activity {
	AudioManager meng;
	private int numRun;
	TooltipWindow tipWindow;
	private TableLayout nextTableView;
	private TextView nextTblTxtView;
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;
	private ImageView capture, switchCamera, nextBtn, errorLogBtn;
	private Context myContext;
	private RelativeLayout cameraPreview;
	private boolean cameraFront = false;
	private ImageView box1, box2, box3;
	//---------------------------------------------
	protected boolean settingPassword = false, training = false, 
			settingPassDone = false, testingPassDone = false, settingPassTest = false
			,manualFilterPicked = false;
	protected String filePath, croppedPath;
	protected ProgressDialog mProgressDialog;
	protected AlertDialog.Builder badImageAlert;
	public static int numPassKeypoints = -1;
	public static boolean refSetAccepted = false;
	FeatureDetector detector;
	DescriptorExtractor descriptor;
	DescriptorMatcher matcher;
	public static boolean isWritingPassword = false;

	private ReferenceSet refSet, refSetForTestPurposeOnly;
	protected List<MatOfKeyPoint> temp_passKeypoints = new ArrayList<MatOfKeyPoint>();
	private List<Mat> temp_passMat = new ArrayList<Mat>(), temp_passDescriptor = new ArrayList<Mat>();
	private ArrayList<String> temp_passFilePath = new ArrayList<String>();
	public static boolean passImageFetched = false;
	public static int numMatch = -1;
	public static long end_time_image, start_time_image, takeImage_time;
	public String timingFile = "Timing_TestPass.txt";
	private String filterErrorMsg = null;

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
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.training = getIntent().getExtras().getBoolean("trainingBool");
			this.settingPassword = getIntent().getExtras().getBoolean("settingPw");
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
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


		SharedMethods.writeFileOnSDCard("----------------- New Session ---------------\n "
				,timingFile);
		cameraPreview = (RelativeLayout) findViewById(R.id.camera_preview);
		nextTblTxtView = (TextView) findViewById(R.id.nextTblMsg);
		nextTableView = (TableLayout) findViewById(R.id.nextTableLayout);
		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);
		nextBtn = (ImageView) findViewById(R.id.nextBtnCamera);
		nextBtn.setOnClickListener(nextButtonListener);
		capture = (ImageView) findViewById(R.id.button_capture);
		capture.setOnClickListener(captrureListener);
		nextTableView.setVisibility(View.GONE);

		errorLogBtn = (ImageView) findViewById(R.id.errorLogBtn);
		errorLogBtn.setOnClickListener(errorLogListener);
		errorLogBtn.setImageResource(drawable.ic_error_64);
		errorLogBtn.setVisibility(View.GONE);
		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(CameraActivity.this);
		}
		numRun = PreferenceManager.getInstance().getNumRun();

		//change icon
		if(settingPassword){
			changeIconOfCamptureBtn(1);
			//instructionTxtView.setText("Take a picture of your trinket");
			timingFile = "Timing_SetPass.txt";
		}
		else{
			//instructionTxtView.setText("Take a picture of your trinket");
		}

		// Drawing the rectangle on camera view
		DrawOnTop mDraw = new DrawOnTop(getApplicationContext());
		addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		if(!settingPassword){
			if(PreferenceManager.getInstance().isPrefSet()){
				FetchPasswordInfoInBackground fetchPass = new FetchPasswordInfoInBackground();
				fetchPass.execute();
			}
		}

		if(numRun == 1)
			showTooltip("Take a picture of your trinket");

		start_time_image = System.currentTimeMillis();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//when on Pause, release camera in order to be used from other applications
		releaseCamera();
		if(tipWindow != null && tipWindow.isTooltipShown())
			tipWindow.dismissTooltip();

		MainActivity.errorLog = new ArrayList<String>();
		MainActivity.errorTimestamp = new ArrayList<Long>();
		errorLogBtn.setVisibility(View.INVISIBLE);
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

		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(CameraActivity.this);
		}

		settingPassDone = false;
		testingPassDone = false;
		CameraActivity.numMatch = -1;
		CameraActivity.numPassKeypoints = -1;
		CameraActivity.refSetAccepted = false;
		CameraActivity.isWritingPassword = false;
		temp_passKeypoints = new ArrayList<MatOfKeyPoint>();
		temp_passMat = new ArrayList<Mat>();
		temp_passDescriptor = new ArrayList<Mat>();
		temp_passFilePath = new ArrayList<String>();

		if(capture != null){
			//change icon
			if(settingPassword){
				changeIconOfCamptureBtn(1);
				//instructionTxtView.setText("Take a picture of your trinket");
			}
			else
				changeIconOfCamptureBtn(0);	
		}

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
		new Handler().postDelayed(new TooltipRunnable(text), 1500L);
	}

	public class TooltipRunnable implements Runnable {
		private String data;

		public TooltipRunnable(String _data) {
			this.data = _data;
		}

		public void run() {
			if (!isFinishing()) {
				tipWindow = new TooltipWindow(CameraActivity.this, data);
				if (!tipWindow.isTooltipShown())
					tipWindow.showToolTip(capture);
			}
		}
	}


	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_entertxtPw) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

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

	/*OnClickListener switchCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//get the number of cameras
			int camerasNumber = Camera.getNumberOfCameras();
			if (camerasNumber > 1) {
				//release the old camera instance
				//switch camera, from the front and the back and vice versa

				releaseCamera();
				chooseCamera();
			} else {
				Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
				toast.show();
			}
		}
	};*/

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
					SharedMethods.writeFileOnSDCard("Training: " + training + ", Cropping Time: " +
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
		if (!this.settingPassword) { // testing password
			this.settingPassword = false;
			this.filePath = Environment.getExternalStorageDirectory().getPath()		
					+ "/Pixie/Candidate/" + currentDateandTime;
			this.croppedPath = Environment.getExternalStorageDirectory()
					.getPath() + "/Pixie/Candidate/Cropped/" + currentDateandTime;
		}
		else if(this.settingPassword){
			this.settingPassword = true;
			this.filePath = Environment.getExternalStorageDirectory().getPath()		
					+ "/Pixie/Reference/" + currentDateandTime;
			this.croppedPath = Environment.getExternalStorageDirectory()
					.getPath() + "/Pixie/Reference/Cropped/" + currentDateandTime;
		}
		if(training){
			this.filePath += "_training";
			this.croppedPath += "_training";
		}

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
			SharedMethods.writeFileOnSDCard("Training: " + training + ", Capture Time: "  + 
					CommonOperations.common.getCurrentDateAndTime() + ": " +
					String.valueOf(takeImage_time - start_time_image) + "\n",
					timingFile);
			//---------------------------------------
			shootSound();
			//---------------------------------------

		}
	};

	OnClickListener errorLogListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			alertScrollView(MainActivity.errorLog, MainActivity.errorTimestamp);
		}
	};


	// This will only available in reset trinket or training use case
	OnClickListener nextButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			CameraActivity.numPassKeypoints = -1;

			if(settingPassDone && !testingPassDone){
				settingPassword = false;
				changeIconOfCamptureBtn(0);
				nextTableView.setVisibility(View.GONE);
				capture.setVisibility(View.VISIBLE);
				//instructionTxtView.setText("Test your trinket to confirm");
				//instructionTxtView.setVisibility(View.VISIBLE);	
				//instructionFrame.setVisibility(View.VISIBLE);

				showTooltip("Take a picture of your trinket to login");
				settingPassTest = true;
			}
			else if(testingPassDone){
				if(training){
					training = false;
					startLoginActivity(true);
				}
				else{
					startLoginActivity(false);
				}
				//TODO: if training || fiu not set --> show next btn and go to loginActivity
				//		else if fiu is set --> goto webview
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

	//make picture and save to a folder
	/*private static File getOutputMediaFile() {
		//make a new file directory inside the "sdcard" folder
		File mediaStorageDir = new File("/sdcard/", "JCG Camera");

		//if this "JCGCamera folder does not exist
		if (!mediaStorageDir.exists()) {
			//if you cannot make this folder return
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}

		//take the current timeStamp
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		//and make a media file:
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

		return mediaFile;
	}
	 */

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
			mProgressDialog = ProgressDialog.show(CameraActivity.this, "Please wait",
					"Processing image");
		}

		@Override
		protected Double doInBackground(Void... params) {
			//-------------------  Finding Keypoints  --------------------------

			Mat descriptors1 = new Mat();
			MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
			long start = System.currentTimeMillis();
			detector.detect(croppedPhoto, keypoints1);
			descriptor.compute(croppedPhoto, keypoints1, descriptors1);			
			int temp_numKP = keypoints1.toList().size();
			long end = System.currentTimeMillis();
			SharedMethods.writeFileOnSDCard("Training: " + training + ", KP_Extraction Time (BG): " +
					String.valueOf(end - start) + "\n",
					timingFile);

			//-------------------  Setting Password  --------------------------
			if(settingPassword){
				//if numKP > min
				if(temp_numKP > MainActivity.thresholdForNumKPs){
					temp_passKeypoints.add(keypoints1);
					temp_passDescriptor.add(descriptors1);
					temp_passMat.add(croppedPhoto);
					temp_passFilePath.add(croppedPath);
				}

				// the last image is taken
				if(MainActivity.refImageCaptureCounter == 2){


					start = System.currentTimeMillis();
					refSet = new ReferenceSet(temp_passMat, new ArrayList<Integer>(Arrays.asList(0, 1, 2)), temp_passKeypoints, temp_passDescriptor);

					//manual filter
					filterErrorMsg = isRefSetValidBasedOnManualPrefilter();
					if (filterErrorMsg != null){
						manualFilterPicked = true;
						CameraActivity.numPassKeypoints = -1;
						refSetAccepted = false;
					}
					//classifier based filter
					else{
						manualFilterPicked = false;
						//Check if the prefiltering will filter the referenceSet
						if (!AutomaticPreFilter.instance.isInitialized()) {
							AutomaticPreFilter.instance.Initialization(CameraActivity.this);
						}

						//check refset
						if(AutomaticPreFilter.instance.testInstance(refSet).equals("0")){
							//Will result in putting  refset in the preferenceManager
							CameraActivity.refSetAccepted = true;
							
							//time to set the trinket without confirming it
							SharedMethods.writeFileOnSDCard( "Time to set the trinket: "  + 
									CommonOperations.common.getCurrentDateAndTime() + ": " 
							+ String.valueOf(System.currentTimeMillis() - start_time_image)+"\n",
									"SetTrinketTaskNotConfirmed.txt");

						}
					}
					end = System.currentTimeMillis();
					SharedMethods.writeFileOnSDCard("Training: " + training + ", RefSet Evaluation Total: " +
							String.valueOf(end - start) + "\n",
							timingFile);
				}
			}
			//-------------------  Testing Password  --------------------------
			else{
				start = System.currentTimeMillis();
				CameraActivity.refSetAccepted = false;
				//checking trinket
				if(!settingPassTest){
					//wait until the passImage is fetched
					while(!passImageFetched){	
					}
					InstanceVerification_MoreFeatures ver = new InstanceVerification_MoreFeatures(refSetForTestPurposeOnly, croppedPhoto, keypoints1, descriptors1 );
					String classificationResult = ver.testInstance(AutomaticPreFilter.instance.getContext());
					//if(classificationResult.equals("1"))
						numMatch = 1;
					//else numMatch = 0 ;
					end = System.currentTimeMillis();
					SharedMethods.writeFileOnSDCard("Training: " + training + ", Test Image Time Total: " +
							String.valueOf(end - start) + "\n",
							timingFile);
					SharedMethods.writeFileOnSDCard("Time to test Trinket: "  + 
							CommonOperations.common.getCurrentDateAndTime() + ": " 
							+ String.valueOf(end - start_time_image) + "\n",
							"TestTrinketTaskTime.txt");
				}
				else{
					//We can use the current refSet to test and don't need to wait for fetching
					InstanceVerification_MoreFeatures ver = new InstanceVerification_MoreFeatures(refSet, croppedPhoto, keypoints1, descriptors1 );
					String classificationResult = ver.testInstance(AutomaticPreFilter.instance.getContext());
					//if(classificationResult.equals("1"))
						numMatch = 1;
					//else numMatch = 0 ;
					end = System.currentTimeMillis();
					SharedMethods.writeFileOnSDCard("Training: " + training + ", Test Image Time Total: " +
							String.valueOf(end - start) + "\n",
							timingFile);
					SharedMethods.writeFileOnSDCard("Time to set Trinket: "  + 
							CommonOperations.common.getCurrentDateAndTime() + ": " 
							+ String.valueOf(end - start_time_image) + "\n",
							"SetTrinketTaskConfirmed.txt");
				}
				
				
			}

			CameraActivity.numPassKeypoints = temp_numKP;

			SharedMethods.writeFileOnSDCard("Training: " + training + ", NumKP: " +
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


			/*if(!isValidBasedOnManualPrefilter()){//filter based on num KPS	
				showAlert() ;
				CameraActivity.numPassKeypoints = -1;
			}*/

			//if we get here, it means the image has passed the manual filter
			//-------------------  Setting Password  --------------------------
			else if (settingPassword){

				//prepare for next refImage to be taken
				if(MainActivity.refImageCaptureCounter < MainActivity.numRefImages - 1){
					MainActivity.refImageCaptureCounter ++;
					changeIconOfCamptureBtn(MainActivity.refImageCaptureCounter + 1);
					CameraActivity.numPassKeypoints = -1;
					if(numRun == 1){
						if((MainActivity.refImageCaptureCounter + 1) == 2)
							showTooltip("Take another picture of your trinket to confirm");
						else 
							showTooltip("Take one last picture of your trinket to confirm");
					}

					/*if(MainActivity.refImageCaptureCounter + 1 == 2)
						instructionTxtView.setText("Take another picture of your trinket to confirm");
					else
						instructionTxtView.setText("Take one last picture of your trinket to confirm");
					 */
				}

				//setting is finished and refSet accepted
				else if((MainActivity.refImageCaptureCounter == MainActivity.numRefImages - 1) && refSetAccepted){
					settingPassDone = true;
					MainActivity.refImageCaptureCounter = 0;
					CameraActivity.numPassKeypoints = -1;

					//write the refset to preferences
					PutTheReferenceSetInPreferences put = new PutTheReferenceSetInPreferences(refSet);
					put.execute();
					CameraActivity.refSetAccepted = false;
					
					//showing next button
					//instructionTxtView.setVisibility(View.GONE);
					//instructionFrame.setVisibility(View.GONE);
					capture.setVisibility(View.GONE);
					nextTableView.setVisibility(View.VISIBLE);
					nextTblTxtView.setText("The trinket is set successfully");


					/*else{//show success message
						AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
						builder.setMessage("The pass image is set successfully!")
						.setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								//do things
								MainActivity.refImageCaptureCounter = 0;
								CameraActivity.numPassKeypoints = -1;
								CameraActivity.refSetAccepted = false;
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
					}*/
				}
				//setting is finished but refSet not accepted
				else if((MainActivity.refImageCaptureCounter == MainActivity.numRefImages - 1) && !refSetAccepted){
					if(filterErrorMsg != null){
						filterErrorMsg = "Please check out the following possible reasons and retry:\n\n" +
								"1. Insufficient lighting\n2. The trinket has plain texture\n" +
								"3. The photos you took (see below) are not from the same object\n";

					MainActivity.errorLog.add(filterErrorMsg);
					MainActivity.errorTimestamp.add(System.currentTimeMillis());
					}


					/*AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
					//
					//builder.setTitle("Error");
					builder.setMessage(filterErrorMsg).setTitle("Error")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});
					AlertDialog alert = builder.create();
					alert.show();
					 */

					alertRefSetError(temp_passFilePath);
					//do things after user presses ok
					MainActivity.refImageCaptureCounter = 0;
					CameraActivity.numPassKeypoints = -1;
					CameraActivity.refSetAccepted = false;

					temp_passKeypoints = new ArrayList<MatOfKeyPoint>();
					temp_passMat = new ArrayList<Mat>();
					temp_passDescriptor = new ArrayList<Mat>();
					temp_passFilePath = new ArrayList<String>();

					changeIconOfCamptureBtn(1);
					if(numRun == 1)
						showTooltip("Take a picture of your trinket");

					SharedMethods.writeFileOnSDCard("RefSet rejected by prefilter\n",
							timingFile);
					manualFilterPicked = false;
					filterErrorMsg = null;
				}
			}
			//-------------------  Testing Password  --------------------------
			else if(!settingPassword)
			{
				//instructionTxtView.setVisibility(View.INVISIBLE);
				//instructionFrame.setVisibility(View.INVISIBLE);
				if(numMatch == 1){
					if(!training){
						if(PreferenceManager.getInstance().isFIUSet())
							startLoginActivity(false);
						else
							startLoginActivity(true);
					}
					else if(training && settingPassDone){
						settingPassTest = false;
						testingPassDone = true;
						numMatch = -1;
						nextTableView.setVisibility(View.VISIBLE);
						capture.setVisibility(View.GONE);
						//instructionTxtView.setText("Test your trinket to confirm");
						//instructionTxtView.setVisibility(View.GONE);
						//instructionFrame.setVisibility(View.GONE);
						nextTblTxtView.setText("The trinket is confirmed");
						
						//time to set the trinket with confirming it
						//SharedMethods.writeFileOnSDCard( "Time to set and confirm the trinket: " + String.valueOf(System.currentTimeMillis() - start_time_image)+"\n",
						//		"SetTrinketWithConfirming.txt");
					}	
				}
				else{
					Toast toast = Toast.makeText(CameraActivity.this, "Password is incorrect.\nTry again!", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					numMatch = -1;
					CameraActivity.numPassKeypoints = -1;
				}

			}

			if(MainActivity.errorLog != null && MainActivity.errorLog.size() != 0)
				errorLogBtn.setVisibility(View.VISIBLE);

		}

		private class PutTheReferenceSetInPreferences extends
		AsyncTask<Void, Void, Double> {
			long start = System.currentTimeMillis();
			ReferenceSet refSet;
			public PutTheReferenceSetInPreferences(ReferenceSet refSet){
				this.refSet = refSet;

			}
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected Double doInBackground(Void... params) {
				CameraActivity.isWritingPassword = true;
				for(int coun = 0 ; coun < temp_passKeypoints.size(); coun++){
					if(coun == 0 ){
						PreferenceManager.getInstance().setPassFilePath(temp_passFilePath.get(0));
						PreferenceManager.getInstance().setPassMat(temp_passMat.get(0));
						PreferenceManager.getInstance().setPassDescriptors(temp_passDescriptor.get(0));
						PreferenceManager.getInstance().setPassKeypoints(temp_passKeypoints.get(0));
					}
					else if(coun == 1){
						PreferenceManager.getInstance().setPassFilePath1(temp_passFilePath.get(1));
						PreferenceManager.getInstance().setPassMat1(temp_passMat.get(1));
						PreferenceManager.getInstance().setPassDescriptors1(temp_passDescriptor.get(1));
						PreferenceManager.getInstance().setPassKeypoints1(temp_passKeypoints.get(1));	
					}
					else if(coun == 2){
						PreferenceManager.getInstance().setPassFilePath2(temp_passFilePath.get(2));
						PreferenceManager.getInstance().setPassMat2(temp_passMat.get(2));
						PreferenceManager.getInstance().setPassDescriptors2(temp_passDescriptor.get(2));
						PreferenceManager.getInstance().setPassKeypoints2(temp_passKeypoints.get(2));
					}
				}
				//calculate authentication features and store the reference set
				this.refSet.calculateAuthenticationFeatures();
				PreferenceManager.getInstance().setReferenceSetInPreferences(this.refSet);
				PreferenceManager.getInstance().setIsPrefSet(true);

				temp_passKeypoints = new ArrayList<MatOfKeyPoint>();
				temp_passMat = new ArrayList<Mat>();
				temp_passDescriptor = new ArrayList<Mat>();
				temp_passFilePath = new ArrayList<String>();


				double x = 0.002;
				return x;
			}

			@Override
			protected void onPostExecute(Double result) {
				CameraActivity.isWritingPassword = false;
				long end = System.currentTimeMillis();
				SharedMethods.writeFileOnSDCard("Training: " + training + ", Store RefSet Including Auth Feature cal: " +
						String.valueOf(end - start) + "\n",
						timingFile);
			}

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
			SharedMethods.writeFileOnSDCard("Training: " + training + ", Fetch RefSet (BG): " +
					String.valueOf(end - start) + "\n",
					timingFile);

		}

	}

	private void showAlert(String msg) {
		badImageAlert = new AlertDialog.Builder(this);
		final LinearLayout layout       = new LinearLayout(this);
		final TextView message        = new TextView(this);
		ImageView img    = new ImageView(this);    

		MainActivity.errorLog.add(msg);
		MainActivity.errorTimestamp.add(System.currentTimeMillis());

		layout.setPadding(10, 10, 10, 10);;
		//margine for message text
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(20, 20, 20, 0);
		message.setLayoutParams(lp);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		Bitmap bitmap = BitmapFactory.decodeFile(croppedPath, options);
		img.setImageBitmap(Bitmap.createScaledBitmap(
				SharedMethods.RotateBitmap(bitmap, 90), 400, 400, true));

		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(message);
		layout.addView(img);
		badImageAlert.setTitle("Low quality picture");
		badImageAlert.setView(layout);
		badImageAlert.setCancelable(false);
		message.setText(msg + "\n");
		badImageAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Delete the bad images 
				File file = new File(croppedPath);
				file.renameTo(new File(croppedPath + "_error.jpg"));
				//file.delete();
				file = new File(filePath);
				file.renameTo(new File(filePath + "_error.jpg"));
				//file.delete();

				//Clear the path from main activity to prevent accessing null file
				dialog.cancel();
				layout.removeAllViews();
				badImageAlert = null;

			}
		});

		//}
		badImageAlert.show();
		SharedMethods.writeFileOnSDCard("Image rejected by prefilter\n",
				timingFile);

	}

	private void changeIconOfCamptureBtn(int id){
		//if(trainig)
		//	capture.setImageResource(R.drawable.ic_navigate_next);
		//else{
		if(id == 0){
			capture.setImageResource(R.drawable.capturegreenbtn);
			/*box1.setVisibility(View.GONE);
			box2.setVisibility(View.GONE);
			box3.setVisibility(View.GONE);*/
			
		}
		else if (id == 3){
			capture.setImageResource(R.drawable.captureonebtn);
			
		}
		else if (id == 2){
			capture.setImageResource(R.drawable.capturetwobtn);
			//box1.setImageResource(R.drawable.checked_box);
		}
		else if (id == 1){
			capture.setImageResource(R.drawable.capturethreebtn);
			//box2.setImageResource(R.drawable.checked_box);
		}
		//}
	}

	private String isRefSetValidBasedOnManualPrefilter(){

		//if(refSet.getAvgDistRef() < 0.6)
			//return "Not identical trinkets!\nPlease try again.";
		//else if(refSet.getAvg_NumWhite() < 500)
		//	return "Blurry images!\nPlease try again.";
		//else
			return null;
	}

	private String isImageValidBasedOnNumKPs(){

		//if(CameraActivity.numPassKeypoints < MainActivity.thresholdForNumKPs)
		//	return "You might have selected a trinket with plain texture, if not please make sure there is enought lighting and retry.";
		
		//else
			return null;

	}


	protected void startLoginActivity(boolean reset) {
		MainActivity.refImageCaptureCounter = 0;
		Intent intent = new Intent(CameraActivity.this, LoginActivity.class);
		intent.putExtra("reset", reset);
		startActivity(intent);
	}

	public void gotoHome(boolean exit){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.putExtra("EXIT", exit);
		startActivity(intent);
	}


	public void alertRefSetError(ArrayList<String> croppedPath){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View myScrollView = inflater.inflate(R.layout.refset_dialog_error_layout, null, false);
		ImageView img1 = (ImageView) myScrollView.findViewById(R.id.image1);
		ImageView img2 = (ImageView) myScrollView.findViewById(R.id.image2);
		ImageView img3 = (ImageView) myScrollView.findViewById(R.id.image3);


		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		Bitmap bitmap = BitmapFactory.decodeFile(croppedPath.get(0), options);
		img1.setImageBitmap(Bitmap.createScaledBitmap(
				SharedMethods.RotateBitmap(bitmap, 90), 200, 200, true));

		bitmap = BitmapFactory.decodeFile(croppedPath.get(1), options);
		img2.setImageBitmap(Bitmap.createScaledBitmap(
				SharedMethods.RotateBitmap(bitmap, 90), 200, 200, true));

		bitmap = BitmapFactory.decodeFile(croppedPath.get(2), options);
		img3.setImageBitmap(Bitmap.createScaledBitmap(
				SharedMethods.RotateBitmap(bitmap, 90), 200, 200, true));

		new AlertDialog.Builder(CameraActivity.this).setView(myScrollView)
		.setTitle("Trinket setup error")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}

		}).show();

	}

	public void alertScrollView(ArrayList<String> errorLog , ArrayList<Long> timeStamp) {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View myScrollView = inflater.inflate(R.layout.scroll_text, null, false);

		LinearLayout linearLayout = (LinearLayout) myScrollView.findViewById(R.id.layoutscrollable);

		for (int x = errorLog.size() - 1 ; x >= 0; x--) {
			//conver to sec
			double time_sec = (System.currentTimeMillis() - timeStamp.get(x)) * 0.001; 
			String timeTxt = "";
			if(((int)time_sec / 60) > 0){
				long min = (int) time_sec /60;
				long sec = (int) time_sec % 60;
				if(sec != 0 )
					timeTxt = min + " minutes and " + sec + " seconds ago:";
				else
					timeTxt = min + " minutes ago:";
			}
			else
				timeTxt = (int) time_sec + " seconds ago:";

			if(x != errorLog.size() - 1){
				LinearLayout line = new LinearLayout(this);
		    	line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,2));
		    	line.setBackgroundColor(getResources().getColor(R.color.darkpurplem));
		    	linearLayout.addView(line);
			}
			TextView textView1 = new TextView(this);
			textView1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			textView1.setText(timeTxt + "\n" + errorLog.get(x));
			textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
			linearLayout.addView(textView1);

		}
		/*TextView tv = (TextView) myScrollView
	            .findViewById(R.id.textViewWithScroll);

	    tv.setText("");	 
	    for (int x = errorLog.size() - 1 ; x >= 0; x--) {
	    	//conver to sec
	    	double time = (System.currentTimeMillis() - timeStamp.get(x)) * 0.001; 
	    	String timeTxt = "";
	    	if((int)time / 60 > 0){
	    		long min = (int) time %60;
	    		long sec = (int) time / 60;
	    		if(sec != 0 )
	    			timeTxt = min + " minutes and " + sec + " seconds ago:";
	    		else
	    			timeTxt = min + " minutes ago:";
	    	}
	    	else
	    		timeTxt = (int) time + " seconds ago:";


	    	tv.append( timeTxt + "\n");
	        tv.append(errorLog.get(x) + "\n\n");

	    }*/

		new AlertDialog.Builder(CameraActivity.this).setView(myScrollView)
		.setTitle("Error Log")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}

		}).setNegativeButton("Dismiss All", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.errorLog = new ArrayList<String>();
				MainActivity.errorTimestamp = new ArrayList<Long>();
				errorLogBtn.setVisibility(View.INVISIBLE);
				dialog.cancel();
			}
		})
		.show();

	}
}