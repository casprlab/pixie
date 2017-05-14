package org.image.password.trinket.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

		webView = (WebView) findViewById(R.id.webView);
		webView.clearHistory();
		webView.loadUrl("about:blank");
		webView.setWebViewClient(new WebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);

		String username = null, pass = null;
		Intent intent = getIntent();
		if (intent.getStringExtra(LoginActivity.EXTRA_MESSAGE) != null) {
			username = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
			pass = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE2);
		}

		else {
			String line;
			BufferedReader in = null;
			String fullPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			try {
				in = new BufferedReader(new FileReader(new File(fullPath
						+ "/ImagePassword/" + "credientials.txt")));
				if ((line = in.readLine()) != null)
					username = line;
				if ((line = in.readLine()) != null)
					pass = line;
				in.close();

			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}


		webView.setWebViewClient(new WebViewClient() {

			   public void onPageFinished(WebView view, String url) {
			        // do your stuff here
				   if(url.contains("errorCode=999")){
					   Intent intent2 = new Intent(WebActivity.this, LoginActivity.class);
					   intent2.putExtra("reset", true);
					   intent2.putExtra("error", true);
					   startActivity(intent2);
						
				   }
				   
			    }
			});
		
		//webView.getSettings().setUserAgentString(System.getProperty("http.agent"));
		String query = "userid=" + username + "&pwd=" + pass;
		webView.postUrl(
				"https://myportal.fiu.edu/psp/psepprd/EMPLOYEE/EMPL/?cmd=login&amp;languageCd=ENG",
				query.getBytes());
		

		/* String query = "userid" + username + "&password" + pass;
		 * webView.postUrl(
				"https://myaccount.nytimes.com/auth/login?URI=http%3A%2F%2Fwww.nytimes.com%2F",
				query.getBytes());
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web, menu);
		if(!PreferenceManager.isInitialized()){
			PreferenceManager.initializeInstance(WebActivity.this);
		}
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
			Intent intent = new Intent(WebActivity.this, CameraActivity.class);
			intent.putExtra("settingPw", true);
			intent.putExtra("trainingBool", false);
			startActivity(intent);
			return true;
		case R.id.action_entertxtPw:			
			Intent intent2 = new Intent(WebActivity.this, LoginActivity.class);
			intent2.putExtra("reset", true);
			startActivity(intent2);
			return true;
		case R.id.action_logout:
			finish();
			gotoHome(true);
			return true;
		case R.id.action_resetFiu:
			PreferenceManager.getInstance().logOut();
			Intent intent3 = new Intent(WebActivity.this, LoginActivity.class);
			intent3.putExtra("reset", true);
			startActivity(intent3);
			return true;
		case R.id.action_testTrinket:
			MainActivity.refImageCaptureCounter = 0;
			Intent intent4 = new Intent(WebActivity.this, TestTrinket.class);
			startActivity(intent4);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	public void onBackPressed() {
		loggingout();
		finish();
		gotoHome(false);
	}

	@Override
	public void onDestroy(){
		super.onStop();
		//finish();
		//gotoHome(true);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		loggingout();
	}

	public void gotoHome(boolean exit){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.putExtra("EXIT", exit);
		startActivity(intent);
	}

	private void loggingout(){
		String query = "";
		webView.postUrl(
				"https://myportal.fiu.edu/psp/psepprd/EMPLOYEE/EMPL/?cmd=logout", query.getBytes());
		
	}

}
