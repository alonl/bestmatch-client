package com.bestmatch.adapters;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.bestmatch.R;
import com.bestmatch.activities.MainActivity;
import com.bestmatch.activities.MyResults;
import com.bestmatch.fragments.MatchImageFragment;
import com.bestmatch.helpers.Match;
import com.bestmatch.viewelements.MatchImage;

public class MatchAdapterTop extends FragmentPagerAdapter implements
		ViewPager.OnPageChangeListener {

	private MatchImage cur = null;
	private MatchImage next = null;
	private MainActivity context;
	private FragmentManager fm;
	private float scale;
	private ArrayList<Match> data = null;

	public MatchAdapterTop(MainActivity context, FragmentManager fm, ArrayList<Match> data) {
		super(fm);
		this.fm = fm;
		this.context = context;
		this.data = data;
	}

	@Override
	public Fragment getItem(int position) 
	{
        // make the first pager bigger than others
        if (position == MainActivity.FIRST_PAGE)
        	scale = MainActivity.BIG_SCALE;     	
        else
        	scale = MainActivity.SMALL_SCALE;
        
        position = position % MainActivity.PAGES;
        MatchImageFragment Item = (MatchImageFragment) MatchImageFragment.newInstance(context, position, scale);
        Item.setData(data.get(position));
        Item.isTop = true;
        return Item;
	}

	@Override
	public int getCount()
	{		
		return MainActivity.PAGES * MainActivity.LOOPS;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) 
	{	
		if (positionOffset >= 0f && positionOffset <= 1f)
		{
			cur = getRootView(position);
			next = getRootView(position +1);

			cur.setScaleBoth(MainActivity.BIG_SCALE 
					- MainActivity.DIFF_SCALE * positionOffset);
			next.setScaleBoth(MainActivity.SMALL_SCALE 
					+ MainActivity.DIFF_SCALE * positionOffset);
		}
	}

	@Override
	public void onPageSelected(int position) {}

	@Override
	public void onPageScrollStateChanged(int state) {}

	private MatchImage getRootView(int position)
	{
		return (MatchImage) fm.findFragmentByTag(this.getFragmentTag(position)).getView().findViewById(R.id.root);
	}

	private String getFragmentTag(int position)
	{
	    return "android:switcher:" + context.topPageViewer.getId() + ":" + position;
	}
}