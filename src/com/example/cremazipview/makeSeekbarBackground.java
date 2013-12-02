package com.example.cremazipview;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

public class makeSeekbarBackground {
	private Context mContext = null;
	private Bitmap mSeekBarBg = null;
	private static makeSeekbarBackground mInstance = null;
	
	private int mCurrentConfig= 0;
	
	private makeSeekbarBackground(Context context) {
		mContext = context;
		mInstance = this;
	}
	
	public static makeSeekbarBackground makeInstance(Context context) {
		if (mInstance == null) {
			mInstance = new makeSeekbarBackground(context);
		}
		return mInstance;
	}
	
	public static makeSeekbarBackground makeInstance(Context context, int config) {
		if (mInstance == null) {
			mInstance = new makeSeekbarBackground(context);
		}
		return mInstance;
	}
	
	public static makeSeekbarBackground getInstance() {
		return mInstance;
	}
	
	
	public BitmapDrawable getSeekBarBg(int config) {
		BitmapDrawable d = makeSeekBarBg(config);
		
		if (d != null) {
			return d;
		}
		return null;
	}
	
	public void getConfig(int config){
		mCurrentConfig= config;
	}
	
	private BitmapDrawable makeSeekBarBg(int config) {
		System.out.println("make seekbar background");
		final int pW = 636;
		final int pH = 21;
		final int lW = 1037;
		final int lH = 21;
		
		final int dotMargin = 15;
		final int dotSize = 5;
		
		Configuration conf = mContext.getResources().getConfiguration();
		int width = 0;
		int height = 0;
		if (config == Configuration.ORIENTATION_PORTRAIT) {
			width = pW;
			height = pH;
			System.out.println("width: "+pW);
		}
		else if(config == Configuration.ORIENTATION_LANDSCAPE){
			width = lW;
			height = lH;System.out.println("width: "+lW);
		}
		
		if (mSeekBarBg != null) {
			mSeekBarBg = null;
		}
		mSeekBarBg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mSeekBarBg);
		Bitmap dot = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bar_bottom_dot);
		
		int cnt = (width / (dotMargin+dotSize));
		System.out.println("dot count: "+cnt);
		int leftMargin = 0;
		for (int i = 0 ; i < cnt ; i++) {
			canvas.drawBitmap(dot, leftMargin, 7, null);
			leftMargin += (dotMargin+dotSize);
		}
		
		return new BitmapDrawable(mContext.getResources(), mSeekBarBg);
	}
}
