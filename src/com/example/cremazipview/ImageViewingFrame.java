package com.example.cremazipview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/*
public class ImageViewingFrame extends ImageView{
	
	private final int NONE= 0;
	private final int DRAG= 1;
	
	private Context mContext;
	
	private Matrix matrix;
	private float[] m;
	
	private int mode= NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 3f;


    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;

	public ImageViewingFrame(Context context){ 
		super(context);
		mContext= context;
		sharedConstructing();
	}
	public ImageViewingFrame(Context context, AttributeSet attr){
		super(context, attr);
		mContext= context;
		sharedConstructing();
	}
	public ImageViewingFrame(Context context, AttributeSet attr, int defstyle){
		super(context, attr, defstyle);
		mContext= context;
		sharedConstructing();
	}
	
	private void sharedConstructing() {
        super.setClickable(true);
       // mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
             //   mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    	last.set(curr);
                        start.set(last);
                        mode = DRAG;
                        break;
                        
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff < CLICK && yDiff < CLICK)
                            performClick();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }
                
                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }

        });
    }
	
	void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        
        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }
    
    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        if (saveScale == 1) {
            //Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bviewWidth = drawable.getIntrinsicWidth();
            int bviewHeight = drawable.getIntrinsicHeight();
            
            Log.d("bmSize", "bviewWidth: " + bviewWidth + " bviewHeight : " + bviewHeight);

            float scaleX = (float) viewWidth / (float) bviewWidth;
            float scaleY = (float) viewHeight / (float) bviewHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bviewHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bviewWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }
	
	/*public void setBitmap(String filepath){
		System.out.println("get file\n"+filepath);
		mBitmap= BitmapFactory.decodeFile(filepath);
		mPaint= new Paint();
		invalidate();
	}
	
	public void onDraw(Canvas canvas){
		System.out.println("create canvas");
		mPaint.setColor(Color.WHITE);
		canvas.drawBitmap(mBitmap, new Matrix(), mPaint);
		//canvas.drawBitmap(mBitmap, 0, 0, mPaint);
		System.out.println("draw bitmap on canvas");
	}*
}*/
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class ImageViewingFrame extends ImageView {
	
	private Paint mPaint;

    Matrix matrix;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    
    private final int OFF = 0;
	private final int ON = 1;
	
	private final int RIGHT = 0;
	private final int LEFT = 1;
    
    static final int UP = 1;
    static final int DOWN = -1;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1.0f;
    float maxScale = 10.0f;
    float[] m;
    
    float startX, endX;


    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;


    ScaleGestureDetector mScaleDetector;

    Context mContext;

    public ImageViewingFrame(Context context) {
        super(context);
        mContext= context;
        sharedConstructing(context);
    }

    public ImageViewingFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext= context;
        sharedConstructing(context);
    }
    
    public ImageViewingFrame(Context context, AttributeSet attrs, int defstyle){
    	super(context, attrs, defstyle);
    	mContext= context;
    }
    
    private void sharedConstructing(Context context) {
        super.setClickable(true);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        this.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	System.out.println("touch event happened");
                PointF curr = new PointF(event.getX(), event.getY());
                
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                	last.set(curr);
                    start.set(last);
                    mode = DRAG;
                    break;
                    
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
                        float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
                        matrix.postTranslate(fixTransX, fixTransY);
                        fixTrans();
                        last.set(curr.x, curr.y);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK && yDiff < CLICK){
                    	if(start.x > ((CremaZipViewActivity)mContext).setWidth()/3*2){
                    		((CremaZipViewActivity)mContext).ViewingImageAll(RIGHT);
							if (((CremaZipViewActivity)mContext).setMenumode() == ON) {
								((CremaZipViewActivity)mContext).hideButtons();
								((CremaZipViewActivity)mContext).getMenumode(OFF);
							}
                    	}
                    	else if(start.x <  ((CremaZipViewActivity)mContext).setWidth()/3){
                    		((CremaZipViewActivity)mContext).ViewingImageAll(LEFT);
							if (((CremaZipViewActivity)mContext).setMenumode() == ON) {
								((CremaZipViewActivity)mContext).hideButtons();
								((CremaZipViewActivity)mContext).getMenumode(OFF);
							}
                    	}
                    	else{
                    		switch (((CremaZipViewActivity)mContext).setMenumode()) {
    						case OFF:
    							((CremaZipViewActivity)mContext).showButtons();
    							((CremaZipViewActivity)mContext).getMenumode(ON);
    							break;
    						case ON:
    							((CremaZipViewActivity)mContext).hideButtons();
    							((CremaZipViewActivity)mContext).getMenumode(OFF);
    							break;
    						}
                    	}
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }
            
            setImageMatrix(matrix);
            invalidate();

                /*if(saveScale == 1.0f){
                	switch (event.getAction()) {
    				case MotionEvent.ACTION_DOWN:
    					startX = event.getX();
    					System.out.println("start: " + startX);
    					break;
    				case MotionEvent.ACTION_MOVE:
    					break;
    				case MotionEvent.ACTION_CANCEL:
    				case MotionEvent.ACTION_UP:
    					endX = event.getX();
    					System.out.println("end: " + endX);
    					System.out.println("TOUCH DOWN");
    					if (saveScale == 1.0f) {
    						if (Math.abs(startX - endX) > 5) {

    							if (((CremaZipViewActivity)mContext).setMenumode() == ON) {
    								((CremaZipViewActivity)mContext).hideButtons();
    								((CremaZipViewActivity)mContext).getMenumode(OFF);
    							}
    							if (startX > endX) // right
    								((CremaZipViewActivity)mContext).ViewingImage(RIGHT);
    							else
    								// left
    								((CremaZipViewActivity)mContext).ViewingImage(LEFT);
    							startX = 0;
    							endX = 0;

    						} else {
    							if (endX > ((CremaZipViewActivity)mContext).setWidth() / 3 * 2) {
    								((CremaZipViewActivity)mContext).ViewingImage(RIGHT);
    								if (((CremaZipViewActivity)mContext).setMenumode() == ON) {
    									((CremaZipViewActivity)mContext).hideButtons();
        								((CremaZipViewActivity)mContext).getMenumode(OFF);
    								}
    							} else if (endX < ((CremaZipViewActivity)mContext).setWidth() / 3) {
    								((CremaZipViewActivity)mContext).ViewingImage(LEFT);
    								if (((CremaZipViewActivity)mContext).setMenumode() == ON) {
    									((CremaZipViewActivity)mContext).hideButtons();
        								((CremaZipViewActivity)mContext).getMenumode(OFF);
    								}
    							} else {
    								switch (((CremaZipViewActivity)mContext).setMenumode()) {
    								case OFF:
    									((CremaZipViewActivity)mContext).showButtons();
        								((CremaZipViewActivity)mContext).getMenumode(ON);
    									break;
    								case ON:
    									((CremaZipViewActivity)mContext).hideButtons();
        								((CremaZipViewActivity)mContext).getMenumode(OFF);
    									break;
    								}
    							}
    						}
    					}
    					break;
    				}
	            }
	            else{
	            	switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN:
	                   	last.set(curr);
	                    start.set(last);
	                    mode = DRAG;
	                    break;
	                      
	                case MotionEvent.ACTION_MOVE:
	                    if (mode == DRAG) {
	                        float deltaX = curr.x - last.x;
	                        float deltaY = curr.y - last.y;
	                        float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
	                        float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
	                        matrix.postTranslate(fixTransX, fixTransY);
	                        fixTrans();
	                        last.set(curr.x, curr.y);
	                    }
	                    break;
	
	                case MotionEvent.ACTION_UP:
	                    mode = NONE;
	                    int xDiff = (int) Math.abs(curr.x - start.x);
	                    int yDiff = (int) Math.abs(curr.y - start.y);
	                    if (xDiff < CLICK && yDiff < CLICK){
	                    	switch (((CremaZipViewActivity)mContext).setMenumode()) {
							case OFF:
								((CremaZipViewActivity)mContext).showButtons();
								((CremaZipViewActivity)mContext).getMenumode(ON);
								break;
							case ON:
								((CremaZipViewActivity)mContext).hideButtons();
								((CremaZipViewActivity)mContext).getMenumode(OFF);
								break;
							}
	                    }
	                    break;
		            }
		                
		            setImageMatrix(matrix);
		            invalidate();
	            }*/
            return true; // indicate event was handled
            }

        });
    }

    public void setMaxZoom(float x) {
        maxScale = x;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
            else
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }
    
    public boolean ImageScale( float scalemode){
    	float mScaleFactor; // = detector.getScaleFactor();
    	System.out.println("enter image scale");
    	
        float origScale = saveScale;
        saveScale += scalemode*0.5f;
        System.out.println("save scale: "+saveScale);
        if (saveScale > maxScale) {
            saveScale = maxScale;
           // mScaleFactor = maxScale / origScale;
        } else if (saveScale < minScale) {
            saveScale = minScale;
            //mScaleFactor= 1.0f;
        }
        else
        	//mScaleFactor= saveScale / origScale;
        
        //System.out.println("mScaleFactor: "+mScaleFactor);

        if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight){
            matrix.postScale(saveScale, saveScale, viewWidth / 2, viewHeight / 2);
            System.out.println("matrix post scale");
        }
        else{
            matrix.postScale(saveScale, saveScale, 0, 0);
            System.out.println("matrix post scale");
        }

        fixTrans();
        System.out.println("fix trans complete");

        invalidate();
        return true;
    }

    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        
        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }
    
    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        if (saveScale == 1) {
            //Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();
            
            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }
}
