package com.bestmatch.activities;

import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.bestmatch.R;
import com.bestmatch.adapters.MatchAdapterBottom;
import com.bestmatch.adapters.MatchAdapterTop;
import com.bestmatch.fragments.MainFragment;
import com.bestmatch.helpers.Data;
import com.bestmatch.helpers.Match;
import com.bestmatch.helpers.PostLoginData;
import com.bestmatch.helpers.User;

public class MainActivity extends FragmentActivity {

	public static int PAGES = 8;
	// You can choose a bigger number for LOOPS, but you know, nobody will fling
	// more than 1000 times just in order to test your "infinite" ViewPager :D
	public final static int LOOPS = 100;
	public final static int FIRST_PAGE = 0;
	public final static float BIG_SCALE = 1.0f;
	public final static float SMALL_SCALE = 0.7f;
	public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;

	public MatchAdapterTop adapterTop;
	public MatchAdapterBottom adapterBottom;

	public static MainActivity thisRef = null;
	private ScaleGestureDetector SGD = null;

	public RelativeLayout top = null;
	public RelativeLayout bottom = null;
	public ViewPager topPageViewer = null;
	public ViewPager bottomPageViewer = null;

	public ImageView yes = null;
	public View whiteLine = null;
	public LinearLayout textContainer = null;

	public int imageHeight = 0;
	public int screenHeight = 0;
	public int startMargins = 0;

	private boolean firstTime = true;
	private boolean isAnimating = false;

	public static final String TAG = "Matcher";
	protected ProgressDialog pd;
	Context context;
	JSONObject lastQueryResult;

	ArrayList<Match> matches;

	String currentUserID = "";
	String currentFBToken = "";
	private MainFragment mainFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		thisRef = this;
		top = (RelativeLayout) findViewById(R.id.top);
		bottom = (RelativeLayout) findViewById(R.id.bottom);
		topPageViewer = (ViewPager) findViewById(R.id.topViewer);
		bottomPageViewer = (ViewPager) findViewById(R.id.bottomViewer);

		yes = (ImageView) findViewById(R.id.yes);
		textContainer = (LinearLayout) findViewById(R.id.textContainer);
		SGD = new ScaleGestureDetector(this, new ScaleListener());

		ImageButton resultsBtn = (ImageButton) findViewById(R.id.resultsButton);
		resultsBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, MyResults.class);
				MainActivity.this.startActivity(myIntent);
				MainActivity.this.overridePendingTransition(0, 0);
			}
		});

		try {
			currentUserID = getIntent().getExtras().getString("userId");
			currentFBToken = getIntent().getExtras().getString("fbToken");
			Data.currentUserID = currentUserID;
			Data.currentFBToken = currentFBToken;
		} catch (Exception e) {
			currentUserID = Data.currentUserID;
			currentFBToken = Data.currentFBToken;
		}

		context = MainActivity.this;
		matches = new ArrayList<Match>();

		if (Data.MainData == null) {
			getSuggestion(8, 0);
		} else {
			matches = Data.MainData;
			onWindowFocusChanged(true);
			showSuggestionOnScreen();
		}
	}

	/**
	 * 
	 * @param numberOfSuggestions
	 * @param friendID
	 *        - 0 for empty friendID
	 */
	public void getSuggestion(final int numberOfSuggestions, final int friendID) {

		final String methodSuffix = "suggest/";

		/**
		 * According with the new StrictGuard policy, running long tasks on the Main UI thread is not possible So
		 * creating new thread to create and execute http operations
		 */
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {

				pd = new ProgressDialog(context);
				// pd.setTitle("logging in...");
				pd.setMessage("Loading Matches...");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				// Execute HTTP Post Request
				Log.i(TAG, "Execute HTTP Post Request");

				try {

					String queryString = "?numOfSuggest=" + numberOfSuggestions;

					if (friendID != 0) {
						queryString += "&friendID=" + friendID;
					}

					// TODO - add the self signiture and use https. Don't use the general CA store, only our
					// certificate.
					// lastQueryResult = PostLoginData.executeHttpPost(SERVER_ADDRESS + "matches/00001111",
					// postParameters);

					lastQueryResult = PostLoginData.executeHttpGet(Data.SERVER_ADDRESS + methodSuffix + currentUserID
							+ queryString);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Error in HTTP Post Request. error: " + e.getMessage());
					try {
						lastQueryResult = new JSONObject();
						lastQueryResult.put(PostLoginData.RES_CODE, HttpStatus.SC_BAD_REQUEST);
						lastQueryResult.put(PostLoginData.RESPONSE_BODY, "Failed to connect server.");
					} catch (JSONException e1) {
						Log.e(TAG, "Error in creating fail message. error: " + e1.getMessage());
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					// pd.dismiss();
				}
				handleSuggetResponse();
			}
		};

		task.execute((Void[]) null);
	}

	private void handleSuggetResponse() {
		// Allows us to change the UI when the request returns.
		try {
			int resCode = lastQueryResult.getInt(PostLoginData.RES_CODE);
			String resBody = lastQueryResult.getString(PostLoginData.RESPONSE_BODY);
			if (resCode == HttpStatus.SC_OK) {
				Log.i(TAG, "handleResponse - Login successful");

				JSONArray resJson = new JSONArray(resBody);
				Log.i(TAG, "number of users: " + resJson.length());
				PAGES = resJson.length();
				for (int i = 0; i < resJson.length(); i++) {
					JSONObject jObj = resJson.getJSONObject(i);
					final String matchID = jObj.getString("_id");
					JSONObject jObjMale = jObj.getJSONObject("male");
					JSONObject jObjFemale = jObj.getJSONObject("female");

					String maleAge = (jObjMale.isNull("age")) ? "" : jObjMale.getString("age");
					String maleLocation = (jObjMale.isNull("location")) ? "" : jObjMale.getString("location");
					User male = new User(jObjMale.getString("uid"), jObjMale.getString("name"),
							jObjMale.getString("sex"), maleAge, maleLocation);

					String femaleAge = (jObjFemale.isNull("age")) ? "" : jObjFemale.getString("age");
					String femaleLocation = (jObjFemale.isNull("location")) ? "" : jObjFemale.getString("location");
					User female = new User(jObjFemale.getString("uid"), jObjFemale.getString("name"),
							jObjFemale.getString("sex"), femaleAge, femaleLocation);

					Match newSuggest = new Match(matchID, male, female);

					matches.add(newSuggest);
				}

				// Displaying suggestiong result to screen:
				// matcherTextView.setText(resJson.toString());

				// Load images for matches:
				loadImageForUsers();

			} else {
				Log.i(TAG, "handleResponse - Login failed. res code is: " + resCode + " and body is:" + resBody);
				// matcherTextView.setText(resBody);
			}

		} catch (Exception e) {
			Log.e(TAG, "handleResponse - Error in handleResponse. error: " + e.getMessage());
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
				Log.i(TAG, "Execute HTTP Post Request");
				for (int matchesIndex = 0; matchesIndex < matches.size(); matchesIndex++) {
					Bitmap user1Image = loadImageForUser(matches.get(matchesIndex).getUser1().getUid());
					Bitmap user2Image = loadImageForUser(matches.get(matchesIndex).getUser2().getUid());

					matches.get(matchesIndex).getUser1().setProfilePic(user1Image);
					matches.get(matchesIndex).getUser2().setProfilePic(user2Image);
				}
				return null;

			}

			@Override
			protected void onPostExecute(Void result) {

				// All Images loaded!
				Log.i(TAG, "All Images loaded!");
				if (pd != null) {
					pd.dismiss();
					onWindowFocusChanged(true);
				}
				Data.MainData = matches;
				showSuggestionOnScreen();
			}
		};

		task.execute((Void[]) null);

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

	private void showSuggestionOnScreen() {
		TextView text1 = (TextView) textContainer.getChildAt(0);
		TextView text2 = (TextView) textContainer.getChildAt(2);
		if (matches.size() <= 0) {
			text1.setText("No Suggested Matches");
			text2.setText("");
		} else {

			text1.setText(matches.get(0).getUser1().getName());
			text2.setText(matches.get(0).getUser2().getName());

			adapterTop = new MatchAdapterTop(this, this.getSupportFragmentManager(), matches);
			adapterBottom = new MatchAdapterBottom(this, this.getSupportFragmentManager(), matches);

			topPageViewer.setAdapter(adapterTop);
			topPageViewer.setOnPageChangeListener(adapterTop);

			bottomPageViewer.setAdapter(adapterBottom);
			bottomPageViewer.setOnPageChangeListener(adapterBottom);

			// Set current item to the middle page so we can fling to both
			// directions left and right
			topPageViewer.setCurrentItem(FIRST_PAGE);
			bottomPageViewer.setCurrentItem(FIRST_PAGE);

			// Necessary or the pager will only have one extra page to show
			// make this at least however many pages you can see
			topPageViewer.setOffscreenPageLimit(3);
			bottomPageViewer.setOffscreenPageLimit(3);

			// Set margin for pages as a negative number, so a part of next and
			// previous pages will be showed
			topPageViewer.setPageMargin(-200);
			bottomPageViewer.setPageMargin(-200);
		}
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (firstTime) {
			firstTime = false;
			LinearLayout main = (LinearLayout) findViewById(R.id.mainContainer);
			screenHeight = main.getMeasuredHeight();
			int width = main.getMeasuredWidth();
			if (screenHeight <= 0) {
				screenHeight = Data.heightBackUp;
				width = Data.widthBackUp;
			} else {
				Data.heightBackUp = screenHeight;
				Data.widthBackUp = width;
			}
				
			imageHeight = (int) (screenHeight * 0.32);
			startMargins = (int) (screenHeight * 0.042);

			RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
			container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			LayoutParams matchParams = new LayoutParams(imageHeight / 2, imageHeight / 2);
			matchParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			yes.setLayoutParams(matchParams);

			LinearLayout greenStrip = (LinearLayout) findViewById(R.id.greenStrip);
			LayoutParams greenStripParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int) (screenHeight * 0.273));
			greenStripParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			greenStrip.setLayoutParams(greenStripParams);

			whiteLine = (View) findViewById(R.id.middleLine);
			whiteLine.setLayoutParams(new LinearLayout.LayoutParams((int) (width * 0.5),(int) (screenHeight * 0.005)));

			LayoutParams paramsTop = new LayoutParams(LayoutParams.MATCH_PARENT, imageHeight);
			paramsTop.addRule(RelativeLayout.CENTER_HORIZONTAL);
			paramsTop.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			paramsTop.topMargin = startMargins;

			LayoutParams paramsBottom = new LayoutParams(LayoutParams.MATCH_PARENT, imageHeight);
			paramsBottom.addRule(RelativeLayout.CENTER_HORIZONTAL);
			paramsBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			paramsBottom.bottomMargin = startMargins;

			top.setLayoutParams(paramsTop);
			bottom.setLayoutParams(paramsBottom);
		}
	}

	public boolean onTouchEvent(MotionEvent ev) {
		SGD.onTouchEvent(ev);
		return true;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float scale = detector.getScaleFactor();
			Log.d("scale", "" + scale);
			if (scale <= 0 || scale == 1 || isAnimating) {
				return true;
			}
			if (scale < 1) {
				isAnimating = true;
				// ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(newBall, "alpha", 1f, 0f);
				ValueAnimator topGoDown = ObjectAnimator.ofFloat(top, "translationY", 0, (imageHeight * 0.725f));
				ValueAnimator bootompGoUp = ObjectAnimator.ofFloat(bottom, "translationY", 0, -(imageHeight * 0.725f));
				ValueAnimator showPinkTop = ObjectAnimator.ofFloat(top.getChildAt(2), "alpha", 0, 0.6f);
				ValueAnimator showPinkBottom = ObjectAnimator.ofFloat(bottom.getChildAt(2), "alpha", 0, 1f);

				AnimatorSet moveMatch = new AnimatorSet();
				moveMatch.setDuration(450);
				// match.setInterpolator(interpolator);
				moveMatch.play(topGoDown).with(bootompGoUp).with(showPinkBottom).with(showPinkTop);

				ValueAnimator fadeOutText = ObjectAnimator.ofFloat(textContainer, "alpha", 1f, 0f);
				ValueAnimator fadeinMatch = ObjectAnimator.ofFloat(yes, "alpha", 0f, 1f);
				fadeinMatch.setDuration(10);
				fadeinMatch.addListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						AnimationDrawable a = (AnimationDrawable) yes.getDrawable();
						a.stop();
						a.start();

						// TODO Put new DAta.

						ValueAnimator fadeOutMatch = ObjectAnimator.ofFloat(yes, "alpha", 1f, 0f);
						ValueAnimator fadeInText = ObjectAnimator.ofFloat(textContainer, "alpha", 0f, 1f);
						fadeInText.addListener(new AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {
							}

							@Override
							public void onAnimationRepeat(Animator animation) {
							}

							@Override
							public void onAnimationEnd(Animator animation) {
								isAnimating = false;
							}

							@Override
							public void onAnimationCancel(Animator animation) {
							}
						});

						ValueAnimator topGoUp = ObjectAnimator.ofFloat(top, "translationY", (imageHeight * 0.725f), 0);
						ValueAnimator bootompGoDown = ObjectAnimator.ofFloat(bottom, "translationY",
								-(imageHeight * 0.725f), 0);
						ValueAnimator hidePinkTop = ObjectAnimator.ofFloat(top.getChildAt(2), "alpha", 0.6f, 0);
						ValueAnimator hidePinkBottom = ObjectAnimator.ofFloat(bottom.getChildAt(2), "alpha", 1, 0);

						int next = (topPageViewer.getCurrentItem() + 1) % PAGES;
						topPageViewer.setAlpha(0);
						bottomPageViewer.setAlpha(0);
						topPageViewer.setCurrentItem(next);
						bottomPageViewer.setCurrentItem(next);
						TextView text1 = (TextView) textContainer.getChildAt(0);
						TextView text2 = (TextView) textContainer.getChildAt(2);
						text1.setText(matches.get(next).getUser1().getName());
						text2.setText(matches.get(next).getUser2().getName());
						topPageViewer.setAlpha(1);
						bottomPageViewer.setAlpha(1);

						AnimatorSet goBack = new AnimatorSet();
						goBack.setDuration(450);
						goBack.play(topGoUp).with(bootompGoDown).with(hidePinkTop).with(hidePinkBottom);

						AnimatorSet animationSet = new AnimatorSet();
						animationSet.setStartDelay(600);
						animationSet.play(fadeOutMatch).before(goBack).before(fadeInText);
						animationSet.start();

					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}
				});

				AnimatorSet showMatch = new AnimatorSet();
				showMatch.play(fadeOutText).before(fadeinMatch);

				AnimatorSet animatorSet = new AnimatorSet();
				animatorSet.play(moveMatch).before(showMatch);
				animatorSet.start();

			} else {
				isAnimating = true;
				// ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(newBall, "alpha", 1f, 0f);
				ValueAnimator topHide = ObjectAnimator.ofFloat(top, "alpha", 1, 0);
				ValueAnimator bootomHide = ObjectAnimator.ofFloat(bottom, "alpha", 1, 0);
				ValueAnimator topGoUp = ObjectAnimator.ofFloat(top, "translationY", 0, -(imageHeight));
				ValueAnimator bootompGoDown = ObjectAnimator.ofFloat(bottom, "translationY", 0, (imageHeight));
				ValueAnimator showBlueTop = ObjectAnimator.ofFloat(top.getChildAt(1), "alpha", 0, 1f);
				ValueAnimator showBlueBottom = ObjectAnimator.ofFloat(bottom.getChildAt(1), "alpha", 0, 1f);
				ValueAnimator fadeOutText = ObjectAnimator.ofFloat(textContainer, "alpha", 1f, 0f);

				AnimatorSet moveMatch = new AnimatorSet();
				moveMatch.setDuration(450);
				moveMatch.play(topGoUp).with(bootompGoDown).with(showBlueTop).with(showBlueBottom).with(fadeOutText)
						.with(topHide).with(bootomHide);

				ValueAnimator fadeinMatch = ObjectAnimator.ofFloat(yes, "alpha", 0f, 1f);
				fadeinMatch.addListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {

						int next = (topPageViewer.getCurrentItem() + 1) % PAGES;
						topPageViewer.setAlpha(0);
						bottomPageViewer.setAlpha(0);
						topPageViewer.setCurrentItem(next);
						bottomPageViewer.setCurrentItem(next);
						TextView text1 = (TextView) textContainer.getChildAt(0);
						TextView text2 = (TextView) textContainer.getChildAt(2);
						text1.setText(matches.get(next).getUser1().getName());
						text2.setText(matches.get(next).getUser2().getName());
						topPageViewer.setAlpha(1);
						bottomPageViewer.setAlpha(1);
						
						ValueAnimator topGoBack = ObjectAnimator.ofFloat(top, "translationY", -(imageHeight), 0);
						ValueAnimator bootompGoBack = ObjectAnimator.ofFloat(bottom, "translationY", (imageHeight), 0);

						ValueAnimator hideBlueTop = ObjectAnimator.ofFloat(top.getChildAt(1), "alpha", 0.6f, 0);
						ValueAnimator hideBlueBottom = ObjectAnimator.ofFloat(bottom.getChildAt(1), "alpha", 0.6f, 0);

						AnimatorSet reset = new AnimatorSet();
						reset.play(topGoBack).with(bootompGoBack).with(hideBlueTop).with(hideBlueBottom);
						reset.setDuration(10);

						ValueAnimator fadeOutMatch = ObjectAnimator.ofFloat(yes, "alpha", 1f, 0f);
						ValueAnimator fadeInText = ObjectAnimator.ofFloat(textContainer, "alpha", 0f, 1f);
						ValueAnimator topShow = ObjectAnimator.ofFloat(top, "alpha", 0, 1);
						ValueAnimator bootompShow = ObjectAnimator.ofFloat(bottom, "alpha", 0, 1);
						bootompShow.addListener(new AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
							}

							@Override
							public void onAnimationRepeat(Animator animation) {
							}

							@Override
							public void onAnimationEnd(Animator animation) {
								isAnimating = false;
							}

							@Override
							public void onAnimationCancel(Animator animation) {
							}
						});

						AnimatorSet show = new AnimatorSet();
						show.play(reset).before(fadeInText).with(topShow).with(bootompShow).with(fadeOutMatch);
						show.setDuration(250);
						show.start();

					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}
				});

				fadeinMatch.setDuration(10);

				AnimatorSet animatorSet = new AnimatorSet();
				animatorSet.play(moveMatch).before(fadeinMatch);
				animatorSet.start();

			}

			return true;
		}
	}

	/**
	 * 
	 * @param matchID
	 * @param matchScore
	 *        - 0 skip, 1 - recommend, -1 for not recommend
	 */
	public void sendMatcheToServer(final String matchID, final int matchScore) {

		final String actionSuffix = "matches/";

		/**
		 * According with the new StrictGuard policy, running long tasks on the Main UI thread is not possible So
		 * creating new thread to create and execute http operations
		 */
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				// matcherTextView.setText("logging in...");

				pd = new ProgressDialog(context);
				// pd.setTitle("logging in...");
				pd.setMessage("Loading Matches...");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				// Execute HTTP Post Request
				Log.i(TAG, "Execute HTTP Post Request");

				try {

					ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>(2);
					postParameters.add(new BasicNameValuePair("matchID", matchID));
					postParameters.add(new BasicNameValuePair("matchScore", String.valueOf(matchScore)));
					lastQueryResult = PostLoginData.executeHttpPost(Data.SERVER_ADDRESS + actionSuffix + currentUserID,
							postParameters);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Error in HTTP Post Request. error: " + e.getMessage());
					try {
						lastQueryResult = new JSONObject();
						lastQueryResult.put(PostLoginData.RES_CODE, HttpStatus.SC_BAD_REQUEST);
						lastQueryResult.put(PostLoginData.RESPONSE_BODY, "Failed to connect server.");
					} catch (JSONException e1) {
						Log.e(TAG, "Error in creating fail message. error: " + e1.getMessage());
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					pd.dismiss();
				}
				handleMatchSent();
			}
		};

		task.execute((Void[]) null);
	}

	/**
	 * Run after match is sent to server
	 */
	private void handleMatchSent() {

	}

	/**
	 * 
	 * @param matchID
	 * @param matchScore
	 *        - 0 skip, 1 - recommend, -1 for not recommend
	 */
	public void sendFBTokenToServer(final String FBToken) {

		final String actionSuffix = "users";

		/**
		 * According with the new StrictGuard policy, running long tasks on the Main UI thread is not possible So
		 * creating new thread to create and execute http operations
		 */
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				// matcherTextView.setText("logging in...");

				pd = new ProgressDialog(context);
				// pd.setTitle("logging in...");
				pd.setMessage("Loading Matches...");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				// Execute HTTP Post Request
				Log.i(TAG, "Execute HTTP Post Request");

				try {

					ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>(2);
					postParameters.add(new BasicNameValuePair("token", FBToken));
					lastQueryResult = PostLoginData.executeHttpPost(Data.SERVER_ADDRESS + actionSuffix, postParameters);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Error in HTTP Post Request. error: " + e.getMessage());
					try {
						lastQueryResult = new JSONObject();
						lastQueryResult.put(PostLoginData.RES_CODE, HttpStatus.SC_BAD_REQUEST);
						lastQueryResult.put(PostLoginData.RESPONSE_BODY, "Failed to connect server.");
					} catch (JSONException e1) {
						Log.e(TAG, "Error in creating fail message. error: " + e1.getMessage());
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					pd.dismiss();
				}
				handleFBTokenSent();
			}
		};

		task.execute((Void[]) null);
	}

	/**
	 * Run after FB Token is sent to server
	 */
	private void handleFBTokenSent() {

	}
}
