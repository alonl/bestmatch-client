package com.bestmatch.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bestmatch.R;
import com.bestmatch.activities.MainActivity;
import com.bestmatch.helpers.Data;
import com.bestmatch.helpers.Match;
import com.bestmatch.viewelements.MatchImage;

public class MatchImageFragment extends Fragment {

	public MatchImage root = null;
	public Match data = null;
	public boolean isTop = false;

	public static Fragment newInstance(MainActivity context, int pos, float scale) {
		Bundle b = new Bundle();
		b.putInt("pos", pos);
		b.putFloat("scale", scale);
		return Fragment.instantiate(context, MatchImageFragment.class.getName(), b);
	}

	public void setData(Match data) {
		this.data = data;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		LinearLayout l = (LinearLayout) inflater.inflate(R.layout.match_images_container, container, false);
		// ViewPager.LayoutParams params = new ViewPager.LayoutParams()
		// l.setLayoutParams(params);

		root = (MatchImage) l.findViewById(R.id.root);
		root.setSize(MainActivity.thisRef.imageHeight);

		if (isTop) {
			root.setImage(data.getUser1().getProfilePic());
		} else {
			root.setImage(data.getUser2().getProfilePic());
		}

		float scale = this.getArguments().getFloat("scale");
		root.setScaleBoth(scale);

		return l;
	}

}
