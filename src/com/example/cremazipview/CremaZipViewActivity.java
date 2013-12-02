package com.example.cremazipview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.cremazipview.makeSeekbarBackground;
import com.example.cremazipview.DecompressFile;
import com.example.cremazipview.GetFile;
import com.example.cremazipview.R.id;
import com.shou.ui.*;

public class CremaZipViewActivity extends Activity {
	
	private Context mContext= null;

	// code constants
	private final int OFF = 0;
	private final int ON = 1;
	private final int RIGHT = 0;
	private final int LEFT = 1;

	private final int FRONT = 0;
	private final int BACK = 1;
	private final int NULL = 2;
	
	private final int CHANGED= 1;

	private final int NONE = 0;
	private final int DRAG = 1;

	private final int ONEPAGE = 1;
	private final int TWOPAGE = 2;

	private final int TAP_DIRECTION = 0;
	private final int TAP_RIGHT = 1;
	private final int TAP_LEFT = 2;
	
	private final int EDGE_NONE= -1;
	private final int EDGE_LEFT= 0;
	private final int EDGE_RIGHT= 1;
	
	private final int MOVE_TO_RIGHT= 0;
	private final int MOVE_TO_LEFT= 1;
	
	// files exist?
	private int isAlready= OFF;

	// top/bottom bar show ? on / off
	private int mMenuMode = OFF;

	// setting bar show ? on / off
	private int mSettingMode = OFF;

	// viewing direction ? left / right
	private int mReadingMode = RIGHT;

	// paging number ? one page / two page
	public int mPageViewMode = TWOPAGE;
	
	// tapping direction ? view / right / left
	private int mTapDirection = TAP_DIRECTION;

	// dragging mode ? none / drag
	private int mode = NONE;
	
	// current viewing page ? front / back / null
	private int mCurrentPage= NULL;

	// for viewer
	private ViewAnimator mTopBar = null;
	private ViewAnimator mBottomBar = null;
	private ViewAnimator mSettingBar = null;

	private CTextView mFileName = null;
	private ImageView mZoomOut = null;
	private ImageView mZoomIn = null;

	private Switch mAutoRotateSwitch = null;
	private Switch mViewPage = null;
	private Switch mViewDirection = null;

	// for view frame
	private ImageView mImageFrame = null;
	private Bitmap mBitmap;

	private PhotoViewAttacher mAttacher;

	private SeekBar mProgressBarPortrait = null;
	private SeekBar mProgressBarLandscape= null;
	private SeekBar mBrightness = null;
	private makeSeekbarBackground mSeekbarBg= null;

	private CTextView mPage = null;

	// tap direction
	private ImageView mAutoRotateOff = null;
	private ImageView mAutoRotateOn = null;
	private CTextView mAutoRotateOffText= null;
	private CTextView mAutoRotateOnText= null;
	
	private ImageView mPageViewOne= null;
	private ImageView mPageViewTwo= null;
	private CTextView mPageViewOneText= null;
	private CTextView mPageViewTwoText= null;
	
	private ImageView mPageDirectionLeft= null;
	private ImageView mPageDirectionRight= null;
	private CTextView mPageDirectionLeftText= null;
	private CTextView mPageDirectionRightText= null;

	private ImageView mTapViewDirection = null;
	private ImageView mTapRightDirection = null;
	private ImageView mTapLeftDirection = null;
	private CTextView mTapViewDirectionText = null;
	private CTextView mTapRightDirectionText = null;
	private CTextView mTapLeftDirectionText = null;

	// etc
	private Animation anim;
	private int mWidth, mHeight;
//	private float startX, endX;
//	private float mZoomState = 1.0f;

	// file
	// private String mPath = "/sdcard/Download/image/";
	private String filePath = "/sdcard/Download/104970.epub";
	private String location;
	private File[] mFiles;
	private int mPos = 0;

	private Matrix matrix;
	private Matrix savedMatrix;

	private DecompressFile mDec;
	private GetFile mGetFile;

	// current image size
	public int mCurrentImageWidth;
	public int mCurrentImageHeight;

	//public int mPageInImage = ONEPAGE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_crema_zip_view);
		
		mContext= this;
		
		init();
		getInformation();
		CreateUI();
		
		loadPreference();
		imageSetting();
	}
	
	private void init(){
	}

	public void getInformation() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		mWidth = displaymetrics.widthPixels;
		mHeight = displaymetrics.heightPixels;
		System.out.println("width: " + mWidth);
	}
	
	public void loadPreference(){
		SharedPreferences pref= mContext.getSharedPreferences(filePath.substring(filePath.lastIndexOf('/')+1, filePath.indexOf('.')), Context.MODE_PRIVATE);
		
		// files is already exist?
//		isAlready= pref.getInt("FileExist", OFF);
		//isAlready= OFF;

		// view page num
		mPageViewMode= pref.getInt("PageViewMode", TWOPAGE);
		OnPageViewSet(mPageViewMode);
						
		// page view direction
		mReadingMode= pref.getInt("PageViewDirection", RIGHT);
		OnPageDirectionSet(mReadingMode);
						
		// page tap direction
		mTapDirection= pref.getInt("PageTapDirection", TAP_DIRECTION);
		OnTapDirectionSet(mTapDirection);
			
		// current page ? front / back
		mCurrentPage= pref.getInt("CurrentPage", NULL);
		System.out.println("Current Page" + (mCurrentPage == FRONT ? "front": "back"));
		
		mPos= pref.getInt("CurrentViewingPage", 0);
			
//		if(isAlready == ON){
//			// current page
//			mPos= pref.getInt("CurrentViewingPage", 0);
//			location = filePath.substring(0, filePath.indexOf('.')) + '/';
//		}
//		else if(isAlready == OFF){
//			fileUnzip();
//		}
//		FileSetting();
	}
	
	public void imageSetting(){
		mGetFile= new GetFile();
		location = filePath.substring(0, filePath.indexOf('.')) + '/';
		mGetFile.getFileInformation(filePath, location);
		try{
			mGetFile.getFileEntry(mPos);
		}
		catch(IOException e){
		}
//		mDec = new DecompressFile();
//		mDec.getFile(filePath);
//		location = filePath.substring(0, filePath.indexOf('.')) + '/';
//		System.out.println("LOCATION: "+location);
//		mDec.getLocation(location);
//		System.out.println("START UNZIP");
//		
//		if(isAlready == OFF){
//			System.out.println("is already: "+(isAlready==OFF ? "off" : "on"));
//			mDec.decompressInputFile();
//			isAlready= ON;
//			System.out.println("is already: "+(isAlready==OFF ? "off" : "on"));
//		}
//		System.out.println("UNZIP FINISHED");
	}
	
	public void savePreference(){
		SharedPreferences pref= getSharedPreferences(filePath.substring(filePath.lastIndexOf('/')+1, filePath.indexOf('.')), Context.MODE_PRIVATE);
		Editor editor= pref.edit();
		editor.putInt("FileExist", isAlready);
		System.out.println(isAlready);
		editor.putInt("CurrentViewingPage", mPos);
		System.out.println(mPos);
		editor.putInt("CurrentPage", mCurrentPage);
		System.out.println("Current Page" + (mCurrentPage == FRONT ? "front": "back"));
		editor.putInt("PageViewMode", mPageViewMode);
		System.out.println(mPageViewMode);
		editor.putInt("PageViewDirection", mReadingMode);
		System.out.println(mReadingMode);
		editor.putInt("PageTapDirection", mTapDirection);
		System.out.println(mTapDirection);
		editor.commit();
	}
	
	public void fileUnzip() {
		mDec = new DecompressFile();
		mDec.getFile(filePath);
		location = filePath.substring(0, filePath.indexOf('.')) + '/';
		System.out.println("LOCATION: "+location);
		mDec.getLocation(location);
		System.out.println("START UNZIP");
		
		if(isAlready == OFF){
			System.out.println("is already: "+(isAlready==OFF ? "off" : "on"));
			mDec.decompressInputFile();
			isAlready= ON;
			System.out.println("is already: "+(isAlready==OFF ? "off" : "on"));
		}
		System.out.println("UNZIP FINISHED");
			
		//isAlready= ON;
	}

	// Image Viewer set on

	public void FileSetting() {
		mFiles = new File(location).listFiles();
		System.out.println("location: "+location);
		mFileName.setText(filePath.substring(filePath.lastIndexOf('/') + 1));
		mProgressBarPortrait.setMax(mFiles.length);
		mProgressBarLandscape.setMax(mFiles.length);
		mProgressBarPortrait.setProgress(mPos);
		mProgressBarLandscape.setProgress(mPos);
		
		viewImage(NULL, mCurrentPage);
	}

	public void CreateUI() {
		mImageFrame = (ImageView) findViewById(R.id.ImageViewFrame);
		mAttacher = new PhotoViewAttacher(mImageFrame);
		mAttacher.setOnViewTapListener(new ViewTapListener());
		//mAttacher.setContext(this);

		mFileName = (CTextView) findViewById(R.id.FileName);
		mZoomOut = (ImageView) findViewById(R.id.ZoomOut);
		mZoomIn = (ImageView) findViewById(R.id.ZoomIn);

		mTopBar = (ViewAnimator) findViewById(R.id.TopBarSwitcher);
		mTopBar.setVisibility(View.INVISIBLE);
		mBottomBar = (ViewAnimator) findViewById(R.id.BottomBarSwitcher);
		mBottomBar.setVisibility(View.INVISIBLE);
		mSettingBar = (ViewAnimator) findViewById(R.id.SettingSwitcher);
		mSettingBar.setVisibility(View.INVISIBLE);
		
		mProgressBarPortrait = (SeekBar) findViewById(R.id.ContentsSeekBarPortrait);
		mProgressBarPortrait.setOnSeekBarChangeListener(progressListener);
		mProgressBarLandscape= (SeekBar) findViewById(R.id.ContentsSeekBarLandscape);
		mProgressBarLandscape.setOnSeekBarChangeListener(progressListener);
		
		makeSeekbarBackground.makeInstance(CremaZipViewActivity.this.getApplicationContext());
		mSeekbarBg = makeSeekbarBackground.getInstance();
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			System.out.println("configuration: LANDSCAPE");
			mProgressBarLandscape.setBackgroundDrawable(mSeekbarBg.getSeekBarBg(Configuration.ORIENTATION_LANDSCAPE));
			mProgressBarLandscape.setVisibility(View.VISIBLE);
			mProgressBarPortrait.setVisibility(View.INVISIBLE);
		}
		else{
			System.out.println("configuration: PORTRAIT");
			mProgressBarPortrait.setBackgroundDrawable(mSeekbarBg.getSeekBarBg(Configuration.ORIENTATION_PORTRAIT));
			mProgressBarPortrait.setVisibility(View.VISIBLE);
			mProgressBarLandscape.setVisibility(View.INVISIBLE);
		}
		

		mBrightness = (SeekBar) findViewById(R.id.brightnessSetting);
		mBrightness
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar seekBar) {
						System.out.println(seekBar.getProgress());
						SettingBrightness(seekBar.getProgress());
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						SettingBrightness(seekBar.getProgress());
					}
				});
		
		try{
			mBrightness.setProgress(android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS));
		}
		catch(SettingNotFoundException e){
			System.out.println("Can't get value");
		}
		
		mAutoRotateOff = (ImageView) findViewById(R.id.AutoRotateOffButton);
		mAutoRotateOff.setVisibility(View.VISIBLE);
		mAutoRotateOn = (ImageView) findViewById(R.id.AutoRotateOnButton);
		mAutoRotateOn.setVisibility(View.INVISIBLE);
		mAutoRotateOffText= (CTextView)findViewById(R.id.AutoRotateOffText);
		mAutoRotateOnText= (CTextView)findViewById(R.id.AutoRotateOnText);
		mAutoRotateOnText.setAlpha(0.5f);
		
		mPageViewOne= (ImageView)findViewById(R.id.PageViewOneButton);
		mPageViewOne.setVisibility(View.INVISIBLE);
		mPageViewTwo= (ImageView)findViewById(R.id.PageViewTwoButton);
		mPageViewTwo.setVisibility(View.VISIBLE);
		mPageViewOneText= (CTextView)findViewById(R.id.PageViewOneText);
		mPageViewTwoText= (CTextView)findViewById(R.id.PageViewTwoText);
		mPageViewOneText.setAlpha(0.5f);
		
		mPageDirectionLeft= (ImageView)findViewById(R.id.ViewDirectionLeftButton);
		mPageDirectionLeft.setVisibility(View.INVISIBLE);
		mPageDirectionRight= (ImageView)findViewById(R.id.ViewDirectionRightButton);
		mPageDirectionRight.setVisibility(View.VISIBLE);
		mPageDirectionLeftText= (CTextView)findViewById(R.id.ViewDirectionLeftText);
		mPageDirectionRightText= (CTextView)findViewById(R.id.ViewDirectionRightText);
		mPageDirectionLeftText.setAlpha(0.5f);

		mTapViewDirection = (ImageView) findViewById(R.id.ToViewDirectionButton);
		mTapViewDirection.setVisibility(View.VISIBLE);
		mTapRightDirection = (ImageView) findViewById(R.id.ToRightDirectionButton);
		mTapRightDirection.setVisibility(View.INVISIBLE);
		mTapLeftDirection = (ImageView) findViewById(R.id.ToLeftDirectionButton);
		mTapLeftDirection.setVisibility(View.INVISIBLE);
		mTapViewDirectionText= (CTextView)findViewById(R.id.ToViewDirectionText);
		mTapRightDirectionText= (CTextView)findViewById(R.id.ToRightDirectionText);
		mTapLeftDirectionText= (CTextView)findViewById(R.id.ToLeftDirectionText);
		mTapRightDirectionText.setAlpha(0.5f);
		mTapLeftDirectionText.setAlpha(0.5f);

		mPage = (CTextView) findViewById(R.id.progressPercentage);
		System.out.println("create ui");
	}
	
	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {

	    if (newConfig.orientation ==
	        android.content.res.Configuration.ORIENTATION_LANDSCAPE)
	        Log.i("ZIPVIEW ACTIVITY", "orientation: landscape");
	    else if (newConfig.orientation == 
	        android.content.res.Configuration.ORIENTATION_PORTRAIT)
	    	Log.i("ZIPVIEW ACTIVITY", "orientation: portrait");

	    super.onConfigurationChanged(newConfig);
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i("ZIPVIEW ACTIVITY", "onSaveInstanceState");
	}
	
	protected void onRestoreInstanceState(Bundle savedState) {		
		Log.i("ZIPVIEW ACTIVITY", "onRestoreInstanceState");
	}

	private class ViewTapListener implements OnViewTapListener {
		public void onViewTap(View view, float x, float y) {
			System.out.println("x: " + x);
			if (x > mWidth / 3 * 2) {
				if(mPageViewMode == ONEPAGE && mCurrentImageWidth>mCurrentImageHeight)
					ViewingImageHalf(RIGHT);
				else
					ViewingImageAll(RIGHT);
				if (mMenuMode == ON) {
					hideButtons();
					mMenuMode = OFF;
				}
			} else if (x < mWidth / 3) {
				if(mPageViewMode == ONEPAGE && mCurrentImageWidth>mCurrentImageHeight)
					ViewingImageHalf(LEFT);
				else
					ViewingImageAll(LEFT);
				if (mMenuMode == ON) {
					hideButtons();
					mMenuMode = OFF;
				}
			} else {
				switch (mMenuMode) {
				case OFF:
					showButtons();
					mMenuMode = ON;
					break;
				case ON:
					hideButtons();
					mMenuMode = OFF;
					break;
				}
			}
		}
		
		public void likeScroll(int direction){
//			int a= mAttacher.MOVE_TO_LEFT;
//			System.out.println("FLING direction - ON LIBRARY: " +(direction == RIGHT? "right":"left"));
			
			if (direction == MOVE_TO_LEFT) {
				if(mPageViewMode == ONEPAGE && mCurrentImageWidth>mCurrentImageHeight)
					ViewingImageHalf(RIGHT);
				else// if(mAttacher.getScale() == 1.0f)
					ViewingImageAll(RIGHT);
				
				if (mMenuMode == ON) {
					hideButtons();
					mMenuMode = OFF;
				}
			} else if (direction == MOVE_TO_RIGHT) {
				if(mPageViewMode == ONEPAGE && mCurrentImageWidth>mCurrentImageHeight)
					ViewingImageHalf(LEFT);
				else// if(mAttacher.getScale() == 1.0f)
					ViewingImageAll(LEFT);
				
				if (mMenuMode == ON) {
					hideButtons();
					mMenuMode = OFF;
				}
			}
		}
	}
	
	
	
	@Override
	public void onResume() {
		loadPreference();
		super.onResume();
	}

	@Override
	public void onPause() {
		savePreference();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		savePreference();
		super.onDestroy();
	}
	
	public void OnPageDirectionSelected(View v){
		switch(v.getId()){
		case R.id.ViewDirectionLeft:
			OnPageDirectionSet(LEFT);
			break;
		case R.id.ViewDirectionRight:
			OnPageDirectionSet(RIGHT);
			break;
		}
	}
	
	public void OnPageDirectionSet(int getNumber){
		switch(getNumber){
		case LEFT:
			mReadingMode= LEFT;
			mPageDirectionLeft.setVisibility(View.VISIBLE);
			mPageDirectionRight.setVisibility(View.INVISIBLE);
			mPageDirectionLeftText.setAlpha(1.0f);
			mPageDirectionRightText.setAlpha(0.5f);
//			Toast.makeText(CremaZipViewActivity.this.getApplicationContext(), "view direction to left", 500).show();
			break;
		case RIGHT:
			mReadingMode= RIGHT;
			mPageDirectionLeft.setVisibility(View.INVISIBLE);
			mPageDirectionRight.setVisibility(View.VISIBLE);
			mPageDirectionLeftText.setAlpha(0.5f);
			mPageDirectionRightText.setAlpha(1.0f);
//			Toast.makeText(CremaZipViewActivity.this.getApplicationContext(), "view direction to right", 500).show();
			break;
		}
	}
	
	public void OnPageViewSelected(View v){
		switch(v.getId()){
		case R.id.PageViewOne:
			OnPageViewSet(ONEPAGE);
			System.out.println("ONE PAGE CLICKED");
			break;
		case R.id.PageViewTwo:
			OnPageViewSet(TWOPAGE);
			break;
		}
	}
	
	public void OnPageViewSet(int getNumber){
		switch(getNumber){
		case ONEPAGE:
			//mAttacher.setPageMode(ONEPAGE);
			mPageViewMode= ONEPAGE;
			if(mCurrentImageWidth>mCurrentImageHeight){
				mPageViewOne.setVisibility(View.VISIBLE);
				mPageViewTwo.setVisibility(View.INVISIBLE);
				mPageViewOneText.setAlpha(1.0f);
				mPageViewTwoText.setAlpha(0.5f);
				Toast.makeText(CremaZipViewActivity.this.getApplicationContext(), "page viewing one page", Toast.LENGTH_SHORT).show();
				switch(mReadingMode){
				case LEFT:
					System.out.println("reading mode: left");
					//mAttacher.setMode(mAttacher.CLICKED);
					mAttacher.setScale(2.0f, 0.0f, 0.0f, true);
					//mAttacher.setScale(2.0f, true);
					//mAttacher.setMode(mAttacher.ZOOM);
					mCurrentPage= FRONT;
					break;
				case RIGHT:
					System.out.println("reading mode: right");
					//mAttacher.setMode(mAttacher.CLICKED);
					mAttacher.setScale(2.0f, (float)mCurrentImageWidth, 0.0f, true);
//					mAttacher.setScale(2.0f, true);
					//mAttacher.setMode(mAttacher.ZOOM);
					mCurrentPage= BACK;
					break;
				}
			}
			else{
				
			}
//				Toast.makeText(CremaZipViewActivity.this.getApplicationContext(), "page viewing one page", Toast.LENGTH_SHORT).show();
			break;
		case TWOPAGE:
			//mAttacher.setPageMode(TWOPAGE);
			if(mCurrentImageWidth>=mCurrentImageHeight){
				mPageViewMode= TWOPAGE;
				mPageViewOne.setVisibility(View.INVISIBLE);
				mPageViewTwo.setVisibility(View.VISIBLE);
				mPageViewOneText.setAlpha(0.5f);
				mPageViewTwoText.setAlpha(1.0f);
				mCurrentPage= NULL;
//				Toast.makeText(CremaZipViewActivity.this.getApplicationContext(), "page viewing two page", Toast.LENGTH_SHORT).show();
				mAttacher.setScale(1.0f, true);
			}
			else{
				
			}
			break;
		}
	}

	public void OnAutoRotateClicked(View v) {
		switch (v.getId()) {
		case R.id.AutoRotateOff:
			android.provider.Settings.System.putInt(getContentResolver(),
					android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
			Toast.makeText(CremaZipViewActivity.this.getApplicationContext(),
					"AUTO ROTATION OFF", Toast.LENGTH_SHORT).show();

			mAutoRotateOff.setVisibility(View.VISIBLE);
			mAutoRotateOn.setVisibility(View.INVISIBLE);
			mAutoRotateOffText.setAlpha(1.0f);
			mAutoRotateOnText.setAlpha(0.5f);
			break;
		case R.id.AutoRotateOn:
			android.provider.Settings.System.putInt(getContentResolver(),
					android.provider.Settings.System.ACCELEROMETER_ROTATION, 1);
			Toast.makeText(CremaZipViewActivity.this.getApplicationContext(),
					"AUTO ROTATION ON", Toast.LENGTH_SHORT).show();
			mAutoRotateOff.setVisibility(View.INVISIBLE);
			mAutoRotateOn.setVisibility(View.VISIBLE);
			mAutoRotateOffText.setAlpha(0.5f);
			mAutoRotateOnText.setAlpha(1.0f);
			break;
		}
	}

	public void getPosition(int position) {
		if (position > mWidth / 3 * 2) {
			if(mPageViewMode == ONEPAGE && mCurrentImageWidth > mCurrentImageHeight)
				ViewingImageHalf(RIGHT);
			else
				ViewingImageAll(RIGHT);
			if (mMenuMode == ON) {
				hideButtons();
				mMenuMode = OFF;
			}
		} else if (position < mWidth / 3) {
			if(mPageViewMode == ONEPAGE && mCurrentImageWidth > mCurrentImageHeight)
				ViewingImageHalf(LEFT);
			else
				ViewingImageAll(LEFT);
			if (mMenuMode == ON) {
				hideButtons();
				mMenuMode = OFF;
			}
		} else {
			switch (mMenuMode) {
			case OFF:
				showButtons();
				mMenuMode = ON;
				break;
			case ON:
				hideButtons();
				mMenuMode = OFF;
				break;
			}
		}
	}
	
	public void ViewingImageAll(int direction){
		switch (mReadingMode) {
		case LEFT:
			switch (mTapDirection) {
			case TAP_LEFT:
				if (direction == RIGHT) {
					if (mPos > 0) {
						mPos--;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					if (mPos < mFiles.length - 1) {
						mPos++;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			case TAP_DIRECTION:
			case TAP_RIGHT:
				if (direction == LEFT) {
					if (mPos > 0) {
						mPos--;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					if (mPos < mFiles.length - 1) {
						mPos++;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			break;
		case RIGHT:
			switch (mTapDirection) {
			case TAP_RIGHT:
				if (direction == LEFT) {
					if (mPos > 0) {
						mPos--;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					if (mPos < mFiles.length - 1) {
						mPos++;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			case TAP_DIRECTION:
			case TAP_LEFT:
				if (direction == RIGHT) {
					if (mPos > 0) {
						mPos--;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					if (mPos < mFiles.length - 1) {
						mPos++;
						setPage(mPos);
						viewImage(NULL, NULL);
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			break;
		}
	}
	
	// INSERTED
	// mAttacher.mFlingMode -> edge definition inserted
	public void ViewingImageHalf(int direction){
		System.out.println("view half");
		switch (mReadingMode) {
		case LEFT:
			switch (mTapDirection) {
			case TAP_DIRECTION:
			case TAP_RIGHT:
				if (direction == LEFT) {
					System.out.println("to back");
					// view: left
					// tap: view direction OR right
					// touch position: left
					// motion: to back
					if (mPos > 0) {
						if(mCurrentPage == FRONT){
							
							if(mAttacher.mFlingMode == EDGE_RIGHT)
								viewImage(NULL, FRONT);
							else{
								mPos--;
								setPage(mPos);
								mCurrentPage= BACK;
								viewImage(CHANGED, BACK);
							}
						}
						else{
							if(mAttacher.mFlingMode == EDGE_LEFT){
								mPos--;
								setPage(mPos);
								viewImage(CHANGED, BACK);
							}
							else{
								mCurrentPage= FRONT;
								viewImage(NULL, FRONT);
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					// view: left
					// tap: view direction OR right
					// touch position: right
					// motion: to front
					System.out.println("to front");
					if (mPos < mFiles.length - 1) {
						if(mCurrentPage == BACK){
							if(mAttacher.mFlingMode == EDGE_LEFT)
								viewImage(NULL, BACK);
							else{
								mPos++;
								setPage(mPos);
								mCurrentPage= FRONT;
								viewImage(CHANGED, FRONT);
							}
						}
						else{
							if(mAttacher.mFlingMode == EDGE_RIGHT){
								mPos++;
								setPage(mPos);
								viewImage(CHANGED, FRONT);
							}
							else{
								mCurrentPage= BACK;
								viewImage(NULL, BACK);
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			case TAP_LEFT:
				if (direction == RIGHT) {
					// view: left
					// tap: left
					// touch position: right
					// motion: to back
					System.out.println("to back");
					if (mPos > 0) {
						if(mCurrentPage == FRONT){
							
							if(mAttacher.mFlingMode == EDGE_RIGHT)
								viewImage(NULL, FRONT);
							else{
								mPos--;
								setPage(mPos);
								mCurrentPage= BACK;
								viewImage(CHANGED, BACK);	
							}
							
						}
						else{
							if(mAttacher.mFlingMode == EDGE_LEFT){
								mPos--;
								setPage(mPos);
								viewImage(CHANGED, BACK);
							}
							else{
								mCurrentPage= FRONT;
								viewImage(NULL, FRONT);
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					// view: left
					// tap: left
					// touch position: left
					// motion: to front
					System.out.println("to front");
					if (mPos < mFiles.length - 1) {
						if(mCurrentPage == BACK){
							if(mAttacher.mFlingMode == EDGE_LEFT)
								viewImage(NULL, BACK);
							else{
								mPos++;
								setPage(mPos);
								mCurrentPage= FRONT;
								viewImage(CHANGED, FRONT);
							}
						}
						else{
							if(mAttacher.mFlingMode == EDGE_RIGHT){
								mPos++;
								setPage(mPos);
								viewImage(CHANGED, FRONT);
							}
							else{
								mCurrentPage= BACK;
								viewImage(NULL, BACK);
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			break;
		case RIGHT:
			switch (mTapDirection) {
			case TAP_DIRECTION:
			case TAP_LEFT:
				if (direction == RIGHT) {
					// view: right
					// tap: view direction OR left
					// touch position: right
					// motion: to back
					System.out.println("to back");
					if (mPos > 0) {
						if(mCurrentPage == BACK){
							if(mAttacher.mFlingMode == EDGE_LEFT)
								viewImage(NULL, BACK);
							else{
								mPos--;
								setPage(mPos);
								mCurrentPage= FRONT;
								viewImage(CHANGED, FRONT);
							}
						}
						else{
							if(mAttacher.mFlingMode == EDGE_RIGHT){
								mPos--;
								setPage(mPos);
								viewImage(CHANGED, FRONT);
							}
							else{
								mCurrentPage= BACK;
								viewImage(NULL, BACK);
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					// view: right
					// tap: view direction OR left
					// touch position: right
					// motion: to front
					System.out.println("to front");
					if (mPos < mFiles.length - 1) {
						if(mCurrentPage == FRONT){
							
							if(mAttacher.mFlingMode == EDGE_RIGHT)
								viewImage(NULL, FRONT);
							else{
								mPos++;
								setPage(mPos);
								mCurrentPage= BACK;
								viewImage(CHANGED, BACK);	
							}
						}
						else{
							if(mAttacher.mFlingMode == EDGE_LEFT){
								mPos++;
								setPage(mPos);
								viewImage(CHANGED, BACK);
							}
							else{
								mCurrentPage= FRONT;
								viewImage(NULL, FRONT);
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			case TAP_RIGHT:
				if (direction == LEFT) {
					// view: right
					// tap: right
					// touch position: left
					// motion: to back
					System.out.println("to back");
					if (mPos > 0) {
						if(mCurrentPage == BACK){
							if(mAttacher.mFlingMode == EDGE_LEFT)
								viewImage(NULL, BACK);
							else{
								mPos--;
								setPage(mPos);
								mCurrentPage= FRONT;
								viewImage(CHANGED, FRONT);	
							}
						}
						else{
							if(mAttacher.mFlingMode == EDGE_RIGHT){
								mPos--;
								setPage(mPos);
								viewImage(CHANGED, FRONT);
							}
							else{
								mCurrentPage= BACK;
								viewImage(NULL, BACK);	
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is first file", Toast.LENGTH_SHORT)
								.show();
				} else {
					// view: right
					// tap: right
					// touch position: right
					// motion: to front
					System.out.println("to front");
					if (mPos < mFiles.length - 1) {
						if(mCurrentPage == FRONT){
							if(mAttacher.mFlingMode == EDGE_RIGHT)
								viewImage(NULL, FRONT);
							else{
								mPos++;
								setPage(mPos);
								mCurrentPage= BACK;
								viewImage(CHANGED, BACK);								
							}
						}
						else{
							if(mAttacher.mFlingMode == EDGE_LEFT){
								mPos++;
								setPage(mPos);
								viewImage(CHANGED, BACK);
							}
							else{
								mCurrentPage= FRONT;
								viewImage(NULL, FRONT);
							}
						}
					} else
						Toast.makeText(
								CremaZipViewActivity.this
										.getApplicationContext(),
								"this is last file", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			break;
		}
		
		mAttacher.mFlingMode = EDGE_NONE;
	}
	
	public void viewImage(String path, int changed, int viewMode){
			if(mPageViewMode == ONEPAGE && mCurrentImageWidth > mCurrentImageHeight){
				mCurrentPage= viewMode;
				if(changed == CHANGED){
					System.out.println("path: "+path);
					mBitmap = BitmapFactory.decodeFile(path);

					mCurrentImageWidth = mBitmap.getWidth();
					mCurrentImageHeight = mBitmap.getHeight();

					mImageFrame.setImageBitmap(mBitmap);

					System.out.println("mCurrentImageWidth: " + mCurrentImageWidth
							+ "\nmCurrentImageHeight: " + mCurrentImageHeight);
					mAttacher.update();
					switch(viewMode){
					case FRONT:
						mAttacher.setScale(2.0f, 0.0f, 0.0f, false);
						break;
					case BACK:
						mAttacher.setScale(2.0f, (float)mCurrentImageWidth, 0.0f, false);
						break;
					}
				}
				else{
					switch(viewMode){
					case FRONT:
						mAttacher.setScale(1.0f, 0.0f, 0.0f, false);
						mAttacher.setScale(2.0f, 0.0f, 0.0f, false);
						break;
					case BACK:
						mAttacher.setScale(1.0f, (float)mCurrentImageWidth, 0.0f, false);
						mAttacher.setScale(2.0f, (float)mCurrentImageWidth, 0.0f, false);
						break;
					}
				}
			}
			else{
//				while(true){
//					System.out.println(mFiles[mPos].getName());
//					if(!mFiles[mPos].getName().contains("thum.jpg")  && !mFiles[mPos].getName().contains("mimetype"))
//						break;
//					mPos++;
//				}
					mBitmap = BitmapFactory.decodeFile("mnt" + location
							+ mFiles[mPos].getName());

					mCurrentImageWidth = mBitmap.getWidth();
					mCurrentImageHeight = mBitmap.getHeight();
					
//					if(mCurrentImageWidth <= mCurrentImageHeight){
//						OnPageViewSet(ONEPAGE);
//					}

					mImageFrame.setImageBitmap(mBitmap);

					System.out.println("mCurrentImageWidth: " + mCurrentImageWidth
							+ "\nmCurrentImageHeight: " + mCurrentImageHeight);
					mAttacher.update();
					mAttacher.setScale(1.0f, false);
			}
	}

	public void viewImage(int changed, int viewMode) {
		if(mPageViewMode == ONEPAGE && mCurrentImageWidth > mCurrentImageHeight){
			mCurrentPage= viewMode;
			if(changed == CHANGED){
//				while(true){
//					System.out.println(mFiles[mPos].getName());
//					if(!mFiles[mPos].getName().contains("thum.jpg") && !mFiles[mPos].getName().contains("mimetype"))
//						break;
//					mPos++;
//				}
				mBitmap = BitmapFactory.decodeFile("mnt" + location
						+ mFiles[mPos].getName());

				mCurrentImageWidth = mBitmap.getWidth();
				mCurrentImageHeight = mBitmap.getHeight();

				mImageFrame.setImageBitmap(mBitmap);

				System.out.println("mCurrentImageWidth: " + mCurrentImageWidth
						+ "\nmCurrentImageHeight: " + mCurrentImageHeight);
				mAttacher.update();
				switch(viewMode){
				case FRONT:
					mAttacher.setScale(2.0f, 0.0f, 0.0f, false);
					break;
				case BACK:
					mAttacher.setScale(2.0f, (float)mCurrentImageWidth, 0.0f, false);
					break;
				}
			}
			else{
				switch(viewMode){
				case FRONT:
					mAttacher.setScale(1.0f, 0.0f, 0.0f, false);
					mAttacher.setScale(2.0f, 0.0f, 0.0f, false);
					break;
				case BACK:
					mAttacher.setScale(1.0f, (float)mCurrentImageWidth, 0.0f, false);
					mAttacher.setScale(2.0f, (float)mCurrentImageWidth, 0.0f, false);
					break;
				}
			}
		}
		else{
//			while(true){
//				System.out.println(mFiles[mPos].getName());
//				if(!mFiles[mPos].getName().contains("thum.jpg")  && !mFiles[mPos].getName().contains("mimetype"))
//					break;
//				mPos++;
//			}
				mBitmap = BitmapFactory.decodeFile("mnt" + location
						+ mFiles[mPos].getName());

				mCurrentImageWidth = mBitmap.getWidth();
				mCurrentImageHeight = mBitmap.getHeight();
				
//				if(mCurrentImageWidth <= mCurrentImageHeight){
//					OnPageViewSet(ONEPAGE);
//				}

				mImageFrame.setImageBitmap(mBitmap);

				System.out.println("mCurrentImageWidth: " + mCurrentImageWidth
						+ "\nmCurrentImageHeight: " + mCurrentImageHeight);
				mAttacher.update();
				mAttacher.setScale(1.0f, false);
		}
	}

	
	public void setPage(int position) {
		mPage.setText(Integer.toString(position + 1) + "/"
				+ Integer.toString(mFiles.length));
		mProgressBarPortrait.setProgress(position);
		mProgressBarLandscape.setProgress(position);
	}

	public SeekBar.OnSeekBarChangeListener progressListener = new SeekBar.OnSeekBarChangeListener() {
		public void onStopTrackingTouch(SeekBar seekBar) {
			mPos = seekBar.getProgress();
			if(mPageViewMode == ONEPAGE && mCurrentImageHeight < mCurrentImageWidth){
				if(mReadingMode == RIGHT){
					viewImage(NULL, FRONT);
				}
				else if(mReadingMode == LEFT){
					viewImage(NULL, BACK);
				}
			}
			else
				viewImage(NULL, NULL);
			hideButtons();
			mMenuMode = OFF;
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			setPage(progress);
		}
	};

	// setting viewing

	public void showButtons() {
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mTopBar.bringToFront();
		mBottomBar.bringToFront();

		anim = new TranslateAnimation(0, 0, -mTopBar.getHeight(), 0);
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
				System.out.println("topbar visible");
				mTopBar.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
			}
		});
		mTopBar.startAnimation(anim);

		anim = new TranslateAnimation(0, 0, mBottomBar.getHeight(), 0);
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
				mBottomBar.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
			}
		});
		mBottomBar.startAnimation(anim);
	}

	public int setMenumode() {
		return mMenuMode;
	}

	public void getMenumode(int m) {
		mMenuMode = m;
	}

	public int setWidth() {
		return mWidth;
	}

	public void hideButtons() {
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		anim = new TranslateAnimation(0, 0, 0, -mTopBar.getHeight());
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				mTopBar.setVisibility(View.INVISIBLE);
			}
		});
		mTopBar.startAnimation(anim);

		anim = new TranslateAnimation(0, 0, 0, mBottomBar.getHeight());
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mBottomBar.setVisibility(View.INVISIBLE);
			}
		});
		mBottomBar.startAnimation(anim);

		if (mSettingMode == ON) {
			anim = new TranslateAnimation(0, 0, 0, -mSettingBar.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mSettingBar.setVisibility(View.INVISIBLE);
				}
			});
			mSettingBar.startAnimation(anim);
			mSettingMode = OFF;
		}
	}

	public void OnSettingClicked(View v) {
		mTopBar.bringToFront();

		switch (mSettingMode) {
		case OFF:

			System.out.println("setting width: " + mSettingBar.getWidth()
					+ "\n setting height: " + mSettingBar.getHeight());
			TextView mt = (TextView) findViewById(R.id.tapDirection);
			System.out.println("text height: " + mt.getHeight());
			anim = new TranslateAnimation(0, 0, -mSettingBar.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					System.out.println("topbar visible");
					mSettingBar.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
				}
			});
			mSettingBar.startAnimation(anim);
			mSettingMode = ON;
			break;
		case ON:
			anim = new TranslateAnimation(0, 0, 0, -mSettingBar.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mSettingBar.setVisibility(View.INVISIBLE);
				}
			});
			mSettingBar.startAnimation(anim);
			mSettingMode = OFF;
			break;
		}
	}

	public void OnZoomClicked(View v) {
		switch (v.getId()) {
		case R.id.ZoomIn:
			mAttacher.setScaleZoomInClicked(1.0f, true);
			break;
		case R.id.ZoomOut:
			mAttacher.setScaleZoomOutClicked(1.0f, true);
			break;
		}
		//mImageFrame.invalidate();
	}
	
	public void OnTapDirectionClicked(View v){
		switch(v.getId()){
		case R.id.ToViewDirection:
			OnTapDirectionSet(TAP_DIRECTION);
			break;
		case R.id.ToLeftDirection:
			OnTapDirectionSet(TAP_LEFT);
			break;
		case R.id.ToRightDirection:
			OnTapDirectionSet(TAP_RIGHT);
			break;
		}
	}

	public void OnTapDirectionSet(int getNumber) {
		switch (getNumber) {
		case TAP_DIRECTION:
			mTapDirection = TAP_DIRECTION;
			mTapViewDirection.setVisibility(View.VISIBLE);
			mTapLeftDirection.setVisibility(View.INVISIBLE);
			mTapRightDirection.setVisibility(View.INVISIBLE);
			mTapViewDirectionText.setAlpha(1.0f);
			mTapLeftDirectionText.setAlpha(0.5f);
			mTapRightDirectionText.setAlpha(0.5f);
			break;
		case TAP_LEFT:
			mTapDirection = TAP_LEFT;
			mTapViewDirection.setVisibility(View.INVISIBLE);
			mTapLeftDirection.setVisibility(View.VISIBLE);
			mTapRightDirection.setVisibility(View.INVISIBLE);
			mTapViewDirectionText.setAlpha(0.5f);
			mTapLeftDirectionText.setAlpha(1.0f);
			mTapRightDirectionText.setAlpha(0.5f);
			break;
		case TAP_RIGHT:
			mTapDirection = TAP_RIGHT;
			mTapViewDirection.setVisibility(View.INVISIBLE);
			mTapLeftDirection.setVisibility(View.INVISIBLE);
			mTapRightDirection.setVisibility(View.VISIBLE);
			mTapViewDirectionText.setAlpha(0.5f);
			mTapLeftDirectionText.setAlpha(0.5f);
			mTapRightDirectionText.setAlpha(1.0f);
			break;
		}
	}

	public void SettingBrightness(int index) {
		android.provider.Settings.System.putInt(getContentResolver(),
				"screen_brightness", index);
	}

	public void onNothing(View v) {
	}

}
