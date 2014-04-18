package com.bestmatch.activities;

import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bestmatch.R;
import com.bestmatch.adapters.ResultsAdapter;
import com.bestmatch.helpers.Data;
import com.bestmatch.helpers.Match;
import com.bestmatch.helpers.PostLoginData;
import com.bestmatch.helpers.User;

public class Details extends FragmentActivity {
	public static int PAGES = 5;
	// You can choose a bigger number for LOOPS, but you know, nobody will fling
	// more than 1000 times just in order to test your "infinite" ViewPager :D
	public final static int LOOPS = 100;
	public static int FIRST_PAGE = PAGES * LOOPS / 2;
	public final static float BIG_SCALE = 1.0f;
	public final static float SMALL_SCALE = 0.65f;
	public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;

	public ResultsAdapter adapter;
	public ViewPager pager;

	private ArrayList<User> recommenders = null;
	protected ProgressDialog pd;
	Context context;
	JSONObject lastQueryResult;
	public Match currentMatch;
	
	TextView matchName, matchAge, numOfRecommenders, cityView;
	RelativeLayout matchImageView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		context = this;
		recommenders = new ArrayList<User>();
		pager = (ViewPager) findViewById(R.id.myviewpager);

		matchName = (TextView)findViewById(R.id.name);
		matchAge = (TextView)findViewById(R.id.ageText);
		numOfRecommenders = (TextView)findViewById(R.id.recomendationsCountText);
		cityView = (TextView)findViewById(R.id.cityText);
		matchImageView = (RelativeLayout)findViewById(R.id.matchImageView);
		
		currentMatch = Data.currentMatch;
		
		Bitmap matchImage = null;
		String matchName, matchAge, city;
		if (Data.currentUserID.equals(currentMatch.getUser1().getUid())) {
			matchImage = currentMatch.getUser2().getProfilePic();
			matchName = currentMatch.getUser2().getName();
			matchAge = currentMatch.getUser2().getAge();
			city = currentMatch.getUser2().getLocation();
		} else {
			matchImage = currentMatch.getUser1().getProfilePic();
			matchName = currentMatch.getUser1().getName();
			matchAge = currentMatch.getUser1().getAge();
			city = currentMatch.getUser1().getLocation();
		}
		try {
		matchImageView.setBackground(new BitmapDrawable(getResources(),matchImage));
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		this.matchName.setText(matchName);
		this.matchAge.setText(matchAge);
		this.cityView.setText(city);
		numOfRecommenders.setText(""+currentMatch.getRecommenders().size());
		
		
		loadImageForUsers();
	}
	

	private void showRecomendersOnScreen() {
		
		
	}
	
	
	/**
	 * Load the facebook profile picture from facebook
	 * 
	 * @param uid
	 */
	public void loadImageForUsers() {

		/**
		 * According with the new StrictGuard policy, running long tasks on the Main UI thread is not possible So
		 * creating new thread to create and execute http operations
		 */
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				// Execute HTTP Post Request
				Log.i("", "Execute HTTP Post Request");
				for (int matchesIndex = 0; matchesIndex < currentMatch.getRecommenders().size(); matchesIndex++) {
					Bitmap userImage = null;
					if (Data.currentUserID.equals(currentMatch.getUser1().getUid())) {
						userImage = loadImageForUser(currentMatch.getUser2().getUid());
					} else {
						userImage = loadImageForUser(currentMatch.getUser1().getUid());
					}
					User user = new User("", "", "", "", ""); // TODO: add details of recomender
					user.setProfilePic(userImage);
					recommenders.add(user);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {

				// All Images loaded!
				Log.i("", "All Images loaded!");
				if (pd != null) {
					pd.dismiss();
					onWindowFocusChanged(true);
				}
				showRecomendersOnScreen();
			}
		};

		task.execute((Void[]) null);

	}

	private Bitmap loadImageForUser(String uid) {
		Bitmap resultImg = null;
		try {
			HttpGet httpRequest = new HttpGet(URI.create("http://graph.facebook.com/" + uid + "/picture?type=normal"));
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			resultImg = BitmapFactory.decodeStream(bufHttpEntity.getContent());
			httpRequest.abort();
		} catch (Exception e) {
			e.toString();
		}
		return resultImg;
	}
	
	@Override
	public void onBackPressed() {
	   Log.d("CDA", "onBackPressed Called");
		Intent myIntent = new Intent(Details.this, MyResults.class);
		startActivity(myIntent);
		overridePendingTransition(0, 0);
	}

}