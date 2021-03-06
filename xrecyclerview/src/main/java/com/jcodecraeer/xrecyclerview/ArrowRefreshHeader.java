package com.jcodecraeer.xrecyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.jcodecraeer.xrecyclerview.progressindicator.AVLoadingIndicatorView;
import java.util.Date;

public class ArrowRefreshHeader extends LinearLayout implements BaseRefreshHeader {
  private LinearLayout mContainer;
  private ImageView mArrowImageView;
  private SimpleViewSwitcher mProgressBar;
  private int mState = STATE_NORMAL;
  private Context mContext;
  private ImageView img_loading;
  private Animation mRotateUpAnim;
  private Animation mRotateDownAnim;
  private AnimationDrawable animationDrawable;

  private final int ROTATE_ANIM_DURATION = 180;
  private int width;
  public int mMeasuredHeight;
  private int maxHeight;
  private int rawX;
  private boolean isRefresh;

  public ArrowRefreshHeader(Context context) {
    super(context);
    initView(context);
  }

  /**
   * @param context
   * @param attrs
   */
  public ArrowRefreshHeader(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    mContext = context;
    width = getScreenWidth(mContext);
    maxHeight = dip2px(mContext, 74);
    // 初始情况，设置下拉刷新view高度为0
    mContainer =
        (LinearLayout) LayoutInflater.from(context).inflate(R.layout.listview_header, null);
    LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    lp.setMargins(0, 0, 0, 0);
    this.setLayoutParams(lp);
    this.setPadding(0, 0, 0, 0);

    addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
    setGravity(Gravity.BOTTOM);
    img_loading = (ImageView) findViewById(R.id.img_loading);
    animationDrawable = (AnimationDrawable) img_loading.getDrawable();
    mArrowImageView = (ImageView) findViewById(R.id.listview_header_arrow);
    //init the progress view
    mProgressBar = (SimpleViewSwitcher) findViewById(R.id.listview_header_progressbar);
    AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(context);
    progressView.setIndicatorColor(0xffB5B5B5);
    progressView.setIndicatorId(ProgressStyle.BallSpinFadeLoader);
    mProgressBar.setView(progressView);

    mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
    mRotateUpAnim.setFillAfter(true);
    mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
    mRotateDownAnim.setFillAfter(true);

    measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    mMeasuredHeight = getMeasuredHeight();
  }

  public void setProgressStyle(int style) {
    if (style == ProgressStyle.SysProgress) {
      mProgressBar.setView(new ProgressBar(mContext, null, android.R.attr.progressBarStyle));
    } else {
      AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(this.getContext());
      progressView.setIndicatorColor(0xffB5B5B5);
      progressView.setIndicatorId(style);
      mProgressBar.setView(progressView);
    }
  }

  public void setArrowImageView(int resid) {
    //mArrowImageView.setImageResource(resid);
  }

  public void setState(int state) {
    if (!animationDrawable.isRunning()) {
      animationDrawable.start();
    }
    if (state == mState) return;
    switch (state) {
      case STATE_NORMAL:
        if (mState == STATE_NORMAL) {
        }
        if (mState == STATE_DONE) {
          animationDrawable.stop();
        }
        break;
      case STATE_RELEASE_TO_REFRESH:
        if (mState != STATE_RELEASE_TO_REFRESH) {
        }
        break;
      case STATE_REFRESHING:
        break;
      case STATE_DONE:
        break;
      default:
    }
    mState = state;
  }

  public int getState() {
    return mState;
  }

  @Override public void refreshComplete() {
    setState(STATE_DONE);
    new Handler().postDelayed(new Runnable() {
      public void run() {
        reset();
      }
    }, 500);
  }

  public void setVisiableHeight(int height) {
    if (height < 0) height = 0;
    LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
    lp.height = height;
    mContainer.setLayoutParams(lp);
    if (height <= maxHeight) {
      rawX = width / 2 * height / maxHeight;
      RelativeLayout.LayoutParams params =
          (RelativeLayout.LayoutParams) mArrowImageView.getLayoutParams();
      if (!isRefresh) {
        params.leftMargin = width - rawX - mArrowImageView.getWidth() / 2;
      } else {
        params.leftMargin = rawX - mArrowImageView.getWidth() / 2;
        if (height == 0) {
          isRefresh = false;
        }
      }
      mArrowImageView.setLayoutParams(params);
    }
  }

  public int getScreenWidth(Context context) {
    Display display =
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    return display.getWidth();
  }

  public int dip2px(Context context, float value) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return (int) (value * metrics.density);
  }

  public int getVisiableHeight() {
    int height = 0;
    LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
    height = lp.height;
    return height;
  }

  @Override public void onMove(float delta) {
    if (getVisiableHeight() > 0 || delta > 0) {
      setVisiableHeight((int) delta + getVisiableHeight());
      if (mState <= STATE_RELEASE_TO_REFRESH) { // 未处于刷新状态，更新箭头
        if (getVisiableHeight() > mMeasuredHeight) {
          setState(STATE_RELEASE_TO_REFRESH);
        } else {
          setState(STATE_NORMAL);
        }
      }
    }
  }

  @Override public boolean releaseAction() {
    boolean isOnRefresh = false;
    int height = getVisiableHeight();
    if (height == 0) // not visible.
    {
      isOnRefresh = false;
    }

    if (getVisiableHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
      setState(STATE_REFRESHING);
      isOnRefresh = true;
      isRefresh = true;
    }
    // refreshing and header isn't shown fully. do nothing.
    if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
      //return;
    }
    int destHeight = 0; // default: scroll back to dismiss header.
    // is refreshing, just scroll back to show all the header.
    if (mState == STATE_REFRESHING) {
      destHeight = mMeasuredHeight;
    }
    smoothScrollTo(destHeight);

    return isOnRefresh;
  }

  public void reset() {
    smoothScrollTo(0);
    setState(STATE_NORMAL);
  }

  private void smoothScrollTo(int destHeight) {
    ValueAnimator animator = ValueAnimator.ofInt(getVisiableHeight(), destHeight);
    animator.setDuration(300).start();
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        setVisiableHeight((int) animation.getAnimatedValue());
      }
    });
    animator.start();
  }

  public static String friendlyTime(Date time) {
    //获取time距离当前的秒数
    int ct = (int) ((System.currentTimeMillis() - time.getTime()) / 1000);

    if (ct == 0) {
      return "刚刚";
    }

    if (ct > 0 && ct < 60) {
      return ct + "秒前";
    }

    if (ct >= 60 && ct < 3600) {
      return Math.max(ct / 60, 1) + "分钟前";
    }
    if (ct >= 3600 && ct < 86400) return ct / 3600 + "小时前";
    if (ct >= 86400 && ct < 2592000) { //86400 * 30
      int day = ct / 86400;
      return day + "天前";
    }
    if (ct >= 2592000 && ct < 31104000) { //86400 * 30
      return ct / 2592000 + "月前";
    }
    return ct / 31104000 + "年前";
  }
}
