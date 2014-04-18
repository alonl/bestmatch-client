package com.bestmatch.activities;


import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bestmatch.R;
import com.bestmatch.fragments.MainFragment;
import com.bestmatch.helpers.PostLoginData;
import com.facebook.Session;
import com.facebook.SessionState;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
//import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.ActionBar;
//import android.support.v4.app.Fragment;

public class Login extends FragmentActivity {
	
	private static final String SERVER_ADDRESS = "http://10.10.8.196:3001/";
	public static final String TAG = "Matcher-Main";
	protected ProgressDialog pd;
	Context context;
	JSONObject lastQueryResult;
	
	
	
	String currentUserID = "";
	String currentFBToken = "";

	private MainFragment mainFragment;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = Login.this;
        
        if (savedInstanceState == null) {
	        // Add the fragment on initial activity setup
	        mainFragment = new MainFragment();
	        mainFragment.setParent(this);
	        getSupportFragmentManager()
	        .beginTransaction()
	        .add(android.R.id.content, mainFragment)
	        .commit();
	    } else {
	        // Or set the fragment from restored state info
	        mainFragment = (MainFragment) getSupportFragmentManager()
	        .findFragmentById(android.R.id.content);
	    }

		Log.i(TAG, "On Create!");

        
    }
    
    

    public void handleFBLoggedIn(Session session, SessionState state) {
    	currentFBToken = session.getAccessToken();
    	sendFBTokenToServer(currentFBToken);
    }
    
    /**
     * 
     * @param matchID
     * @param matchScore - 0 skip,  1 - recommend, -1 for not recommend
     */
    public void sendFBTokenToServer(final String FBToken) {

        Log.i(TAG, "Logged In to FB. Uploading token. Token is:");
        Log.i(TAG, FBToken);


    	final String actionSuffix = "users";
    	
        /** According with the new StrictGuard policy,  running long tasks on the Main UI thread is not possible
         So creating new thread to create and execute http operations */
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {

                pd = new ProgressDialog(context);
//                pd.setTitle("logging in...");
                pd.setMessage("Logging In...");
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
	                    //postParameters.add(new BasicNameValuePair("token", FBToken));
                    lastQueryResult = PostLoginData.executeHttpPost(SERVER_ADDRESS + actionSuffix + "?token=" + FBToken, postParameters);
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
                Log.i(TAG, "onPostExecute - dismissing pd");

            	if (pd!=null) {
                    //Log.i(TAG, "onPostExecute - dismissing pd2");

                    //pd.dismiss();
                }
            	handleSuccessfulFBTokenSent();
            }
        };

        task.execute((Void[])null);
    }
    
    

    
    /**
     * Run after FB Token is sent to server
     */
    private void handleSuccessfulFBTokenSent() {
//    	String userId = lastQueryResult.ge 
    			
        // Allows us to change the UI when the request returns.
        try {
            int resCode = lastQueryResult.getInt(PostLoginData.RES_CODE);
            String resBody = lastQueryResult.getString(PostLoginData.RESPONSE_BODY);
            if(resCode == HttpStatus.SC_OK) {
                Log.i(TAG, "handleResponse - Login successful");
                Log.i(TAG, resBody);

                JSONObject resJson = new JSONObject(resBody);
            	currentUserID = resJson.getString("uid");
                


          // Shows a counter for 3 seconds, and then closes the activity.
          new CountDownTimer(3000, 1000) {
              public void onTick(long millisUntilFinished) {
                  //resultView.setText("Moving to matcher./nClosing in " + millisUntilFinished / 1000 + "...");
              }
              public void onFinish() {
                  moveToMatcher();
              }
          }.start();

            } else {
                Log.i(TAG, "handleResponse - Login failed. res code is: " + resCode + " and body is:" + resBody);
                //resultView.setText(resBody);
            }



        } catch (Exception e) {
            Log.e(TAG, "handleResponse - Error in handleResponse. error: " + e.getMessage());
        }
    			
    	
    }
    
    private void moveToMatcher() {
    	if (pd!=null) {
            Log.i(TAG, "onPostExecute - dismissing pd2");
            pd.dismiss();
        }
    	Intent intent = new Intent(this, MainActivity.class);
    	intent.putExtra("userId", currentUserID);
    	intent.putExtra("fbToken", currentFBToken);
    	startActivity(intent);
    }
    
}
