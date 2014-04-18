package com.bestmatch.viewelements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bestmatch.activities.MainActivity;
import com.bestmatch.helpers.ImageHelper;

public class MatchImage extends RelativeLayout {
	
	private float scale = MainActivity.BIG_SCALE;
	
	public MatchImage(Context context) {
		super(context);
		setWillNotDraw(false);
		// TODO Auto-generated constructor stub
	}
	
	public MatchImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		// TODO Auto-generated constructor stub
	}
	
	public MatchImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		// TODO Auto-generated constructor stub
	}

	int size = 0;
	
	public void setSize(int _size) {
		size = _size;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size,size);
		this.setLayoutParams(params);
	}
	
	public void setImage(Bitmap image) {
		ImageView face = (ImageView)this.getChildAt(0); 
		face .setImageBitmap(ImageHelper.getRoundedCornerBitmap(image, size));
	}
	
	public void setScaleBoth(float scale)
	{
		this.scale = scale;
		this.invalidate(); 	// If you want to see the scale every time you set
							// scale you need to have this line here, 
							// invalidate() function will call onDraw(Canvas)
							// to redraw the view for you
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// The main mechanism to display scale animation, you can customize it
		// as your needs
		int w = this.getWidth();
		int h = this.getHeight();
		canvas.scale(scale, scale, w/2, h/2);

		super.onDraw(canvas);
	}


}
