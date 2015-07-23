package com.example.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GifView extends View {

	/**
	 * 默认为1秒
	 */
	private static final int DEFAULT_MOVIE_DURATION = 1000;

	private int mMovieResourceId = -1;

	private Movie mMovie = null;

	private long mMovieStart;

	private int mCurrentAnimationTime = 0;

	private float mLeft;

	private float mTop;

	private float mScale;

	private int mMeasuredMovieWidth;

	private int mMeasuredMovieHeight;

	private boolean mVisible = true;

	private volatile boolean mPaused = false;

	private String TAG = "GifView";

	public GifView(Context context) {
		this(context, null);
	}

	public GifView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GifView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setViewAttributes(context, attrs, defStyle);
	}

	@SuppressLint("NewApi")
	private void setViewAttributes(Context context, AttributeSet attrs,
			int defStyle) {

		Log.d(TAG, "setViewAttributes");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

	}

	/**
	 * 设置gif图资源
	 * 
	 * @param movieResId
	 */
	public void setMovieResource(int movieResId) {
		Log.d(TAG, "setMovieResource");
		this.mMovieResourceId = movieResId;
		mMovie = Movie.decodeStream(getResources().openRawResource(
				mMovieResourceId));
		requestLayout();
	}

	public void setMovie(Movie movie) {
		Log.d(TAG, "setMovie");
		this.mMovie = movie;
		requestLayout();
	}

	public Movie getMovie() {

		return mMovie;
	}

	public void setMovieTime(int time) {
		Log.d(TAG, "setMovieTime");
		mCurrentAnimationTime = time;
		invalidate();
	}

	/**
	 * 设置暂停
	 * 
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		Log.d(TAG, "setPaused");
		this.mPaused = paused;
		if (!paused) {
			mMovieStart = android.os.SystemClock.uptimeMillis()
					- mCurrentAnimationTime;
		}
		invalidate();
	}

	/**
	 * 判断gif图是否停止了
	 * 
	 * @return
	 */
	public boolean isPaused() {
		return this.mPaused;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "onMeasure");
		if (mMovie != null) {
			int movieWidth = mMovie.width();
			int movieHeight = mMovie.height();

			/*
			 * Calculate horizontal scaling
			 */
			float scaleH = 1f;
			int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);

			if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
				int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
				if (movieWidth > maximumWidth) {
					scaleH = (float) movieWidth / (float) maximumWidth;
				}
			}

			/*
			 * calculate vertical scaling
			 */
			float scaleW = 1f;
			int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);

			if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
				int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
				if (movieHeight > maximumHeight) {
					scaleW = (float) movieHeight / (float) maximumHeight;
				}
			}

			/*
			 * calculate overall scale
			 */
			mScale = 1f / Math.max(scaleH, scaleW);

			mMeasuredMovieWidth = (int) (movieWidth * mScale);
			mMeasuredMovieHeight = (int) (movieHeight * mScale);

			setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);

		} else {
			/*
			 * No movie set, just set minimum available size.
			 */
			setMeasuredDimension(getSuggestedMinimumWidth(),
					getSuggestedMinimumHeight());
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout");
		super.onLayout(changed, l, t, r, b);
		System.out.println(getWidth());
		mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
		mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
		mVisible = getVisibility() == View.VISIBLE;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(TAG, "onDraw");
		if (mMovie != null) {
			if (!mPaused) {
				updateAnimationTime();
				drawMovieFrame(canvas);
				invalidateView();
			} else {
				drawMovieFrame(canvas);
			}
		}
	}

	@SuppressLint("NewApi")
	private void invalidateView() {
		Log.d(TAG, "invalidateView");
		if (mVisible) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				postInvalidateOnAnimation();
			} else {
				invalidate();
			}
		}
	}

	private void updateAnimationTime() {
		Log.d(TAG, "updateAnimationTime");
		long now = android.os.SystemClock.uptimeMillis();
		// 如果第一帧，记录起始时间
		if (mMovieStart == 0) {
			mMovieStart = now;
		}
		// 取出动画的时长
		int dur = mMovie.duration();
		if (dur == 0) {
			dur = DEFAULT_MOVIE_DURATION;
		}
		// 算出需要显示第几帧
		mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
	}

	private void drawMovieFrame(Canvas canvas) {
		Log.d(TAG, "drawMovieFrame");
		// 设置要显示的帧，绘制即可
		mMovie.setTime(mCurrentAnimationTime);
		Log.d(TAG, "s1");
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		Log.d(TAG, "s2");
		canvas.scale(mScale, mScale);
		Log.d(TAG, "s3");
		System.out.println(mLeft);
		mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
		Log.d(TAG, "s4");
		canvas.restore();

	}

	@SuppressLint("NewApi")
	@Override
	public void onScreenStateChanged(int screenState) {
		super.onScreenStateChanged(screenState);
		mVisible = screenState == SCREEN_STATE_ON;
		invalidateView();
	}

	@SuppressLint("NewApi")
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}

}
