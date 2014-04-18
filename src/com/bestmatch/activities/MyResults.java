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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bestmatch.R;
import com.bestmatch.adapters.ResultsAdapter;
import com.bestmatch.helpers.Data;
import com.bestmatch.helpers.ImageHelper;
import com.bestmatch.helpers.Match;
import com.bestmatch.helpers.PostLoginData;
import com.bestmatch.helpers.User;

public class MyResults extends FragmentActivity {
	public static int PAGES = 5;
	// You can choose a bigger number for LOOPS, but you know, nobody will fling
	// more than 1000 times just in order to test your "infinite" ViewPager :D
	public final static int LOOPS = 100;
	public static int FIRST_PAGE = 0;
	public final static float BIG_SCALE = 1.0f;
	public final static float SMALL_SCALE = 0.65f;
	public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;

	public ResultsAdapter adapter;
	public ViewPager pager;
	private TextView name = null;
	private ArrayList<Match> results = null;
	protected ProgressDialog pd;
	Context context;
	JSONObject lastQueryResult;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		context = this;
		results = new ArrayList<Match>();
		name = (TextView) findViewById(R.id.name);
		pager = (ViewPager) findViewById(R.id.myviewpager);

		ImageButton mainBtn = (ImageButton) findViewById(R.id.mainButton);
		mainBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MyResults.this, MainActivity.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				MyResults.this.startActivity(myIntent);
				MyResults.this.overridePendingTransition(0, 0);
			}
		});
		
		if (Data.ResultsData == null) {
			getMatches(5);
		} else {
			results = Data.ResultsData;
			showMyPic();
			showResults();
		}
	}

	/**
	 * 
	 * @param numberOfSuggestions
	 */
	public void getMatches(final int numberOfSuggestions) {

		final String actionSuffix = "matches/";

		/**
		 * According with the new StrictGuard policy, running long tasks on the Main UI thread is not possible So
		 * creating new thread to create and execute http operations
		 */
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(context);
				// pd.setTitle("logging in...");
				pd.setMessage("Loading Results..");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				// Execute HTTP Post Request
				Log.i("s", "Execute HTTP Post Request");

				try {

					// TODO - add the self signiture and use https. Don't use the general CA store, only our
					// certificate.
					// lastQueryResult = PostLoginData.executeHttpPost(SERVER_ADDRESS + "matches/00001111",
					// postParameters);

					lastQueryResult = PostLoginData.executeHttpGet(Data.SERVER_ADDRESS + actionSuffix
							+ Data.currentUserID);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("", "Error in HTTP Post Request. error: " + e.getMessage());
					try {
						lastQueryResult = new JSONObject();
						lastQueryResult.put(PostLoginData.RES_CODE, HttpStatus.SC_BAD_REQUEST);
						lastQueryResult.put(PostLoginData.RESPONSE_BODY, "Failed to connect server.");
					} catch (JSONException e1) {
						Log.e("", "Error in creating fail message. error: " + e1.getMessage());
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					//pd.dismiss();
				}
				handleMatchResponse();
			}
		};

		task.execute((Void[]) null);
	}

	private void handleMatchResponse() {
		// Allows us to change the UI when the request returns.
		try {
			int resCode = lastQueryResult.getInt(PostLoginData.RES_CODE);
			String resBody = lastQueryResult.getString(PostLoginData.RESPONSE_BODY);
			if (resCode == HttpStatus.SC_OK) {
				Log.i("", "handleResponse - Login successful");

				JSONArray resJson = new JSONArray(resBody);
				PAGES = resJson.length();
				for (int i = 0; i < resJson.length(); i++) {
					JSONObject jObj = resJson.getJSONObject(i);
					final String matchID = jObj.getString("_id");
					JSONObject jObjMale = jObj.getJSONObject("male");
					JSONObject jObjFemale = jObj.getJSONObject("female");

					User male = new User(jObjMale.getString("uid"), jObjMale.getString("name"),
							jObjMale.getString("sex"), jObjMale.getString("age"), jObjMale.getString("location"));
					User female = new User(jObjFemale.getString("uid"), jObjFemale.getString("name"),
							jObjFemale.getString("sex"), jObjFemale.getString("age"), jObjFemale.getString("location"));

					Match newMatch = new Match(matchID, male, female);

					newMatch.setMatchRating(jObj.getString("matchRating").toString());

					ArrayList<User> recommenders = new ArrayList<User>();
					JSONArray resJsonRecommenders = jObj.getJSONArray("votedYes");
					for (int j = 0; j < resJsonRecommenders.length(); j++) {
						String jRecommenderID = resJsonRecommenders.getString(j);
						User recommender = new User(jRecommenderID, "", "", "", "");
						recommenders.add(recommender);
					}

					newMatch.setMatchRating(jObj.getString("matchRating").toString());
					newMatch.setRecommenders(recommenders);
					results.add(newMatch);
				}

				
				loadImageForUsers();

			} else {
				Log.i("", "handleResponse - Login failed. res code is: " + resCode + " and body is:" + resBody);
				// matcherTextView.setText(resBody);
			}

		} catch (Exception e) {
			Log.e("", "handleResponse - Error in handleResponse. error: " + e.getMessage());
		}
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
				for (int matchesIndex = 0; matchesIndex < results.size(); matchesIndex++) {
					Bitmap user1Image = loadImageForUser(results.get(matchesIndex).getUser1().getUid());
					Bitmap user2Image = loadImageForUser(results.get(matchesIndex).getUser2().getUid());

					results.get(matchesIndex).getUser1().setProfilePic(user1Image);
					results.get(matchesIndex).getUser2().setProfilePic(user2Image);
				}
				Data.myPic = loadImageForUser(Data.currentUserID);
				return null;

			}

			@Override
			protected void onPostExecute(Void result) {

				// All Images loaded!
				Log.i("", "All Images loaded!");
				if (pd != null) {
					pd.dismiss();
				}
				showMyPic();
				Data.ResultsData = results;
				showResults();
			}
		};

		task.execute((Void[]) null);

	}

	private void showMyPic() {
		LinearLayout parent = (LinearLayout)findViewById(R.id.mainContainer);
		int w = parent.getMeasuredHeight();
		if (w <= 0) {
			w = Data.widthRsultBackUp;
		} else {
			Data.widthRsultBackUp = w;
		}
		int size = (int)(w * 0.25);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		ImageView myp = (ImageView)findViewById(R.id.myPic);
		myp.setLayoutParams(params);
		myp.setImageBitmap(ImageHelper.getRoundedCornerBitmap(Data.myPic , size));
	}
	
	private Bitmap loadImageForUser(String uid) {
		Bitmap resultImg = null;
		try {
			HttpGet httpRequest = new HttpGet(URI.create("http://graph.facebook.com/" + uid + "/picture?type=large"));
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

	private void showResults() {
		if (results.get(FIRST_PAGE).getUser1().getUid().equals(Data.currentUserID)) {
			name.setText(results.get(FIRST_PAGE).getUser2().getName());	
		} else {
			name.setText(results.get(FIRST_PAGE).getUser1().getName());
		}
		adapter = new ResultsAdapter(this, this.getSupportFragmentManager(), results, name);
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(adapter);

		// Set current item to the middle page so we can fling to both
		// directions left and right
		pager.setCurrentItem(FIRST_PAGE);

		// Necessary or the pager will only have one extra page to show
		// make this at least however many pages you can see
		pager.setOffscreenPageLimit(3);

		// Set margin for pages as a negative number, so a part of next and
		// previous pages will be showed
		pager.setPageMargin(-500);
	}

}