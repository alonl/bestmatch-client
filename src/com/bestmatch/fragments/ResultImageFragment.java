package com.bestmatch.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bestmatch.R;
import com.bestmatch.activities.Details;
import com.bestmatch.activities.MainActivity;
import com.bestmatch.activities.MyResults;
import com.bestmatch.helpers.Data;
import com.bestmatch.helpers.Match;
import com.bestmatch.viewelements.MatchImage;

public class ResultImageFragment extends Fragment {

	public MatchImage root = null;
	public Bitmap image = null;
	private Match data;
	private static MyResults thisContext;
	

	public static Fragment newInstance(MyResults context, int pos, float scale) {
		Bundle b = new Bundle();
		b.putInt("pos", pos);
		b.putFloat("scale", scale);
		thisContext = context;
		return Fragment.instantiate(context, ResultImageFragment.class.getName(), b);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		LinearLayout l = (LinearLayout) inflater.inflate(R.layout.results_images_container, container, false);
		// ViewPager.LayoutParams params = new ViewPager.LayoutParams()
		// l.setLayoutParams(params);

		int pos = this.getArguments().getInt("pos");

		root = (MatchImage) l.findViewById(R.id.root);
		root.setSize(MainActivity.thisRef.imageHeight);

		if (data.getUser1().getUid().equals(Data.currentUserID)) {
			root.setImage(data.getUser2().getProfilePic());
		} else {
			root.setImage(data.getUser1().getProfilePic());
		}

		float scale = this.getArguments().getFloat("scale");
		root.setScaleBoth(scale);

		root.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(thisContext, Details.class);
				Data.currentMatch = data;
				startActivity(myIntent);
				
			}
		});
		
		return l;
	}

	public void setData(Match data) {
		this.data = data;
	}

}
