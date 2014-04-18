package com.bestmatch.adapters;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.bestmatch.R;
import com.bestmatch.activities.MyResults;
import com.bestmatch.fragments.ResultImageFragment;
import com.bestmatch.helpers.Data;
import com.bestmatch.helpers.Match;
import com.bestmatch.viewelements.MatchImage;

public class ResultsAdapter extends FragmentPagerAdapter implements
		ViewPager.OnPageChangeListener {

	private MatchImage cur = null;
	private MatchImage next = null;
	private MyResults context;
	private FragmentManager fm;
	private float scale;
	private TextView name;
	private ArrayList<Match> data = null;

	public ResultsAdapter(MyResults context, FragmentManager fm, ArrayList<Match> data, TextView name) {
		super(fm);
		this.fm = fm;
		this.context = context;
		this.data = data;
		this.name = name;
	}

	@Override
	public Fragment getItem(int position) 
	{
        // make the first pager bigger than others
        if (position == MyResults.FIRST_PAGE)
        	scale = MyResults.BIG_SCALE;     	
        else
        	scale = MyResults.SMALL_SCALE;
        
        position = position % MyResults.PAGES;
        ResultImageFragment Item = (ResultImageFragment) ResultImageFragment.newInstance(context, position, scale);
        Item.setData(data.get(position));
        return Item;
	}

	@Override
	public int getCount()
	{		
		return MyResults.PAGES * MyResults.LOOPS;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) 
	{	
		if (positionOffset >= 0f && positionOffset <= 1f)
		{
			int pos = position % MyResults.PAGES;  
			if (data.get(pos).getUser1().getUid().equals(Data.currentUserID)) {
				name.setText(data.get(pos).getUser2().getName());	
			} else {
				name.setText(data.get(pos).getUser1().getName());
			}
			
			cur = getRootView(position);
			next = getRootView(position +1);

			cur.setScaleBoth(MyResults.BIG_SCALE 
					- MyResults.DIFF_SCALE * positionOffset);
			next.setScaleBoth(MyResults.SMALL_SCALE 
					+ MyResults.DIFF_SCALE * positionOffset);
		}
	}

	@Override
	public void onPageSelected(int position) {}

	@Override
	public void onPageScrollStateChanged(int state) {}

	private MatchImage getRootView(int position)
	{
		//return (MatchImage) fm.findFragmentByTag(this.getFragmentTag(position)).getView().findViewById(R.id.root);
		return (MatchImage) fm.findFragmentByTag(this.getFragmentTag(position)).getView().findViewById(R.id.root);
	}

	private String getFragmentTag(int position)
	{
	    return "android:switcher:" + context.pager.getId() + ":" + position;
	}
}