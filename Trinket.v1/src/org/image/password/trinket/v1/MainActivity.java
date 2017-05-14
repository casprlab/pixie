package org.image.password.trinket.v1;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	protected SharedPreferences sharedPreferences;
	protected boolean settingPassword = false, training = false;
	public static int refImageCaptureCounter = 0;
	public static final int numRefImages = 3;
	public static final int thresholdForNumKPs = 50;
	public static int ws, hs; 

	private ImageView nextTabBtn;
	private TextView skipTextView;
	private ImageView skipBtn;
	private LinearLayout mainLayout;
	private ViewPager viewPager;
	private TabPagerAdapter tabPagerAdapter;
	public static ArrayList<String> errorLog;
	public static ArrayList<Long> errorTimestamp;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		errorLog = new ArrayList<String>();
		errorTimestamp = new ArrayList<Long>();
		boolean exit = false;
		if (getIntent().getExtras() != null) {
			exit = getIntent().getExtras().getBoolean("EXIT");
		}

		if(exit){		
			finish();
		}

		setTitle("How to use Pixie");
		getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

		refImageCaptureCounter = 0;
		DisplayMetrics metrics = getBaseContext().getResources()
				.getDisplayMetrics();
		ws = metrics.widthPixels;
		hs = metrics.heightPixels;

		SharedMethods.createDirIfNotExists("Pixie");
		SharedMethods.createDirIfNotExists("Pixie/Candidate");
		SharedMethods.createDirIfNotExists("Pixie/Reference");
		SharedMethods.createDirIfNotExists("Pixie/Candidate/Cropped");
		SharedMethods.createDirIfNotExists("Pixie/Reference/Cropped");
	}

	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.imageViewNext){
				if(viewPager.getCurrentItem() < 3)
					viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
				else if(viewPager.getCurrentItem() == 3){
					settingPassword = true; 
					training = true;
					startCameraActivity();	
				}
			}
			else if(v.getId() == R.id.skipTextView || v.getId() == R.id.imageViewSkip){
				if(!PreferenceManager.getInstance().isPrefSet()){
					settingPassword = true; 
					training = true;	
				}
				else{
					settingPassword = false; 
					training = false;
				}
				startCameraActivity();
			}
		}

	};


	protected void startCameraActivity() {
		MainActivity.refImageCaptureCounter = 0;
		Intent intent = new Intent(MainActivity.this, CameraActivity.class);
		intent.putExtra("settingPw", settingPassword);
		intent.putExtra("trainingBool", training);
		startActivity(intent);
		//startActivityForResult(intent, 0);
	}

	protected void startLoginActivity(boolean reset) {
		MainActivity.refImageCaptureCounter = 0;
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		intent.putExtra("reset", reset);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		if(!PreferenceManager.getInstance().isFIUSet()){
			MenuItem item = menu.findItem(R.id.action_resetFiu);
			item.setVisible(false);
			this.invalidateOptionsMenu();
		}
		else{
			MenuItem item = menu.findItem(R.id.action_entertxtPw);
			item.setVisible(false);
			this.invalidateOptionsMenu();
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_resetTrinket:
			MainActivity.refImageCaptureCounter = 0;
			PreferenceManager.getInstance().setIsPrefSet(false);
			settingPassword = true; 
			training = false;
			startCameraActivity();
			return true;
		case R.id.action_entertxtPw:
			startLoginActivity(true);
			return true;
		case R.id.action_resetFiu:
			PreferenceManager.getInstance().logOut();
			Intent intent3 = new Intent(MainActivity.this, LoginActivity.class);
			intent3.putExtra("reset", true);
			startActivity(intent3);
			return true;
		case R.id.action_logout:
			finish();
			return true;
		case R.id.action_restoreTrinket:
			Intent intent4 = new Intent(MainActivity.this, RestoreRefSetActivity.class);
			startActivity(intent4);
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		initialization();
	}


	private class InilializeDatasets extends
	AsyncTask<Void, Void, Double> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Double doInBackground(Void... params) {
			if(!AutomaticPreFilter.instance.isInitialized())
				AutomaticPreFilter.instance.Initialization(MainActivity.this);

			if(!TestImageClassifier.instance.isInitialized())
				TestImageClassifier.instance.Initialization(MainActivity.this);

			double x = 0.002;
			return x;
		}

		@Override
		protected void onPostExecute(Double result) {
			super.onPostExecute(result);
		}

	}

	protected void initialization(){

		MainActivity.refImageCaptureCounter = 0;
		//Initializing PrefManager
		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(this); 
		}
		PreferenceManager.getInstance().initiate();
		//Initializing datasets
		if(!AutomaticPreFilter.instance.isInitialized() || !TestImageClassifier.instance.isInitialized()){
			InilializeDatasets initialization = new InilializeDatasets();
			initialization.execute();
		}

		training = true;
		settingPassword = true;

		//If this is not the first time the app is running and the trinket is set
		if(PreferenceManager.getInstance().getNumRun() != 1 && PreferenceManager.getInstance().isPrefSet()){
			training = false;
			settingPassword = false;
			startCameraActivity();
		}
		//if this is not the first time the app is running but the trinket is not set
		else if(PreferenceManager.getInstance().getNumRun() != 1 && !PreferenceManager.getInstance().isPrefSet() && PreferenceManager.getInstance().isFIUSet()){
			training = false;
			settingPassword = true;
			startCameraActivity();
		}
		else{

			skipTextView = (TextView) findViewById(R.id.skipTextView);
			skipBtn = (ImageView) findViewById(R.id.imageViewSkip);
			nextTabBtn = (ImageView) findViewById(R.id.imageViewNext);
			nextTabBtn.setImageDrawable(getResources().getDrawable(R.drawable.arrowrightgreen));
			nextTabBtn.setOnClickListener(clickListener);
			skipBtn.setOnClickListener(clickListener);
			skipTextView.setOnClickListener(clickListener);

			mainLayout = (LinearLayout) findViewById(R.id.trainingLayout);
			mainLayout.setBackgroundColor(getResources().getColor(R.color.greenm));

			viewPager = (ViewPager) findViewById(R.id.pager);
			tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
			viewPager.setAdapter(tabPagerAdapter);
			viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				// on swipe select the respective tab
				@Override
				public void onPageSelected(int position) {
					//actionBar.setSelectedNavigationItem(position);
					if(position == 0 ){
						nextTabBtn.setImageResource(R.drawable.arrowrightgreen);
						mainLayout.setBackgroundColor(getResources().getColor(R.color.greenm));
						skipTextView.setVisibility(View.VISIBLE);
						skipBtn.setVisibility(View.VISIBLE);
					}
					else if(position == 1){
						nextTabBtn.setImageResource(R.drawable.arrowrightred);
						mainLayout.setBackgroundColor(getResources().getColor(R.color.redm));
						skipTextView.setVisibility(View.INVISIBLE);
						skipBtn.setVisibility(View.INVISIBLE);
					}
					else if(position == 2){
						nextTabBtn.setImageResource(R.drawable.arrowrightpurple);
						mainLayout.setBackgroundColor(getResources().getColor(R.color.purplem));
						skipTextView.setVisibility(View.INVISIBLE);
						skipBtn.setVisibility(View.INVISIBLE);
					}
					else if(position == 3){
						nextTabBtn.setImageResource(R.drawable.arrowrightblue);
						mainLayout.setBackgroundColor(getResources().getColor(R.color.bluem));
						skipTextView.setVisibility(View.INVISIBLE);
						skipBtn.setVisibility(View.INVISIBLE);
					}
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) { }

				@Override
				public void onPageScrollStateChanged(int arg0) { }
			});
		}
	}


}
