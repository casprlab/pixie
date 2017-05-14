package org.image.password.trinket.v1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.View.OnClickListener;

public class LoginActivity extends Activity {
	protected boolean reset = false;
	protected ImageView submit, cancel;
	protected TextView userid, pass;
	long start_time, end_time;
	public static boolean login = false, error = false;
	public final static String EXTRA_MESSAGE = "org.opencv.image.pass.pilot.line.LoginActivity.MESSAGE";
	public final static String EXTRA_MESSAGE2 = "org.opencv.image.pass.pilot.line.LoginActivity.MESSAGE2";

	protected ButtonClickHandler clickHandler = new ButtonClickHandler();

	long passEntryStartTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setTitle("Login");
		getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		
		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(LoginActivity.this);
		}
		
		
		TextView errorMsg = (TextView) findViewById(R.id.errorMsg);
		errorMsg.setVisibility(View.GONE);
		if(getIntent().hasExtra("error")){
			errorMsg.setVisibility(View.VISIBLE);
		}
		Bundle extras = getIntent().getExtras();	
		if (extras != null) {
			this.reset = getIntent().getExtras().getBoolean("reset");
			if(getIntent().hasExtra("error")){
				errorMsg.setVisibility(View.VISIBLE);
			}
		}
		
		if(PreferenceManager.getInstance().isFIUSet() && !reset)
			loginToWebView(PreferenceManager.getInstance().getFiuUserName(), PreferenceManager.getInstance().getFiuPassword());
		
		start_time = System.currentTimeMillis();
		//timerTaskDealyLogin();
		
		submit = (ImageView) findViewById(R.id.logInBtn);
		submit.setOnClickListener(clickHandler);
		cancel = (ImageView) findViewById(R.id.cancelBtnLogIn);
		cancel.setOnClickListener(clickHandler);
		
		userid = (TextView) (findViewById(R.id.usernameTxt));
		pass = (TextView) (findViewById(R.id.passwordTxt));

		userid.setText(null);
		pass.setText(null);

		pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
					//if (event !=null && !event.isShiftPressed()){
						SharedMethods.writeFileOnSDCard("Time to type the password "  + 
								CommonOperations.common.getCurrentDateAndTime() + ": " +
								String.valueOf(System.currentTimeMillis() - passEntryStartTime) + "\n",
								"TextPassword_TimingPasswordOnly.txt");
					//}
				}
				return false;
			}
		});
		
		
		pass.setOnFocusChangeListener(new OnFocusChangeListener() {

		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		        passEntryStartTime = System.currentTimeMillis();
		        }
		    }
		});
		
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		userid.setText(null);
		pass.setText(null);
		
		if(!PreferenceManager.isInitialized())
			PreferenceManager.initializeInstance(LoginActivity.this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		
		if(!PreferenceManager.getInstance().isPrefSet()){
			MenuItem item = menu.findItem(R.id.action_testTrinket);
			item.setVisible(false);
			this.invalidateOptionsMenu();
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_resetTrinket:
			MainActivity.refImageCaptureCounter = 0;
			PreferenceManager.getInstance().setIsPrefSet(false);
			Intent intent = new Intent(LoginActivity.this, CameraActivity.class);
			intent.putExtra("settingPw", true);
			intent.putExtra("trainingBool", false);
			startActivity(intent);
			return true;
		case R.id.action_broom:
			userid.setText(null);
			pass.setText(null);
			return true;
		case R.id.action_logInTrinket:
			MainActivity.refImageCaptureCounter = 0;
			Intent intent3 = new Intent(LoginActivity.this, CameraActivity.class);
			intent3.putExtra("settingPw", false);
			intent3.putExtra("trainingBool", false);
			startActivity(intent3);
			return true;
		case R.id.action_logout:
			finish();
			gotoHome(true);
			return true;
		case R.id.action_testTrinket:
			MainActivity.refImageCaptureCounter = 0;
			Intent intent5 = new Intent(LoginActivity.this, TestTrinket.class);
			startActivity(intent5);
			return true;
		default:
			return super.onOptionsItemSelected(item);
				
		}
	}
	
	public class ButtonClickHandler implements View.OnClickListener {
		Context ctx = getApplication();

		public void onClick(View view) {
			if (view.getId() == R.id.logInBtn) {
				reset = true;
				end_time = System.currentTimeMillis();
				SharedMethods.writeFileOnSDCard("Time to type the password " + 
				CommonOperations.common.getCurrentDateAndTime() + ": " +
						String.valueOf(end_time - start_time) + "\n",
						"TextPassword_Timing.txt");
				loginToWebView(userid.getText().toString().trim(), pass.getText().toString().trim());
				
			}
			else if(view.getId() == R.id.cancelBtnLogIn){
				finish();
				gotoHome(true);
			}
		}

	}
	
	private void loginToWebView(String userId, String pass) {
		if(this.reset){
			PutMyFIUPreferences storeMyFiu = new PutMyFIUPreferences(userId, pass);
			storeMyFiu.execute();
		}
		Intent intent = new Intent(LoginActivity.this, WebActivity.class);
		String useridStr = userId;
		String passStr = pass;
		intent.putExtra(EXTRA_MESSAGE, useridStr);
		intent.putExtra(EXTRA_MESSAGE2, passStr);
		startActivity(intent);
	}
	
	private class PutMyFIUPreferences extends
	AsyncTask<Void, Void, Double> {
		String userName, pass;
		public PutMyFIUPreferences(String userName, String pass){
			this.userName = userName;
			this.pass = pass;
			
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Double doInBackground(Void... params) {

			PreferenceManager.getInstance().setFiuUserName(this.userName);
			PreferenceManager.getInstance().setFiuPassword(this.pass);
		
			double x = 0.002;
			return x;
		}

	}
	
	@Override
	public void onBackPressed() {
		finish();
		gotoHome(false);
	}
	
	public void gotoHome(boolean exit){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.putExtra("EXIT", exit);
		startActivity(intent);
	}
	
	@Override
	public void onDestroy(){
		super.onStop();
		//finish();
		//gotoHome(true);
	}
}
