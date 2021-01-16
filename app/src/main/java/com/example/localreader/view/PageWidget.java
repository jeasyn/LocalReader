package com.example.localreader.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.example.localreader.util.PageFactory;

/**
 * @author xialijuan
 * @date 2021/1/9
 */
public class PageWidget extends View {
    private final static String TAG = "BookPageWidget";
    private int mScreenWidth = 0; // 屏幕宽
    private int mScreenHeight = 0; // 屏幕高
    private Context mContext;

    //是否移动了
    private Boolean isMove = false;
    //是否翻到下一页
    private Boolean isNext = false;
    //是否取消翻页
    private Boolean cancelPage = false;
    //是否没下一页或者上一页
    private Boolean noNext = false;
    private int downX = 0;
    private int downY = 0;

    private int moveX = 0;
    private int moveY = 0;
    //翻页动画是否在执行
    private Boolean isRuning =false;

    Bitmap mCurPageBitmap = null; // 当前页
    Bitmap mNextPageBitmap = null;
    private BaseFlip baseFlip;

    Scroller mScroller;
    private int mBgColor = 0xFFCEC29C;
    private TouchListener mTouchListener;

    public PageWidget(Context context) {
        this(context,null);
    }

    public PageWidget(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PageWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initPage();
        mScroller = new Scroller(getContext(),new LinearInterpolator());

        baseFlip = new CoverFlip(mCurPageBitmap,mNextPageBitmap,mScreenWidth,mScreenHeight);
    }

    private void initPage(){
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;
        mCurPageBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);      //android:LargeHeap=true  use in  manifest application
        mNextPageBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);
    }

    public Bitmap getCurPage(){
        return mCurPageBitmap;
    }

    public Bitmap getNextPage(){
        return mNextPageBitmap;
    }

    public void setBgColor(int color){
        mBgColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mBgColor);
        if (isRuning) {
            baseFlip.drawMove(canvas);
        } else {
            baseFlip.drawStatic(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (PageFactory.getStatus() == PageFactory.Status.OPENING){
            return true;
        }
        int x = (int)event.getX();
        int y = (int)event.getY();

        baseFlip.setTouchPoint(x,y);
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            downX = (int) event.getX();
            downY = (int) event.getY();
            moveX = 0;
            moveY = 0;
            isMove = false;
            noNext = false;
            isNext = false;
            isRuning = false;
            baseFlip.setStartPoint(downX,downY);
            abortAnimation();
        }else if (event.getAction() == MotionEvent.ACTION_MOVE){
            final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            //判断是否移动了
            if (!isMove) {
                isMove = Math.abs(downX - x) > slop || Math.abs(downY - y) > slop;
            }

            if (isMove){
                isMove = true;
                if (moveX == 0 && moveY ==0) {
                    //判断翻得是上一页还是下一页
                    if (x - downX >0){
                        isNext = false;
                    }else{
                        isNext = true;
                    }
                    cancelPage = false;
                    if (isNext) {
                        Boolean isNext = mTouchListener.nextPage();
                        baseFlip.setDirection(BaseFlip.Direction.next);
                        if (!isNext) {
                            noNext = true;
                            return true;
                        }
                    } else {
                        Boolean isPre = mTouchListener.prePage();
                        baseFlip.setDirection(BaseFlip.Direction.pre);
                        if (!isPre) {
                            noNext = true;
                            return true;
                        }
                    }
                }else{
                    //判断是否取消翻页
                    if (isNext){
                        if (x - moveX > 0){
                            cancelPage = true;
                            baseFlip.setCancel(true);
                        }else {
                            cancelPage = false;
                            baseFlip.setCancel(false);
                        }
                    }else{
                        if (x - moveX < 0){
                            baseFlip.setCancel(true);
                            cancelPage = true;
                        }else {
                            baseFlip.setCancel(false);
                            cancelPage = false;
                        }
                    }
                }
                moveX = x;
                moveY = y;
                isRuning = true;
                this.postInvalidate();
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            if (!isMove){
                cancelPage = false;
                //是否点击了中间
                if (downX > mScreenWidth / 5 && downX < mScreenWidth * 4 / 5 && downY > mScreenHeight / 3 && downY < mScreenHeight * 2 / 3){
                    if (mTouchListener != null){
                        mTouchListener.center();
                    }
                    return true;
                }else if (x < mScreenWidth / 2){
                    isNext = false;
                }else{
                    isNext = true;
                }
                if (isNext) {
                    Boolean isNext = mTouchListener.nextPage();
                    baseFlip.setDirection(BaseFlip.Direction.next);
                    if (!isNext) {
                        return true;
                    }
                } else {
                    Boolean isPre = mTouchListener.prePage();
                    baseFlip.setDirection(BaseFlip.Direction.pre);
                    if (!isPre) {
                        return true;
                    }
                }
            }
            if (cancelPage && mTouchListener != null){
                mTouchListener.cancel();
            }
            if (!noNext) {
                isRuning = true;
                baseFlip.startSliding(mScroller);
                this.postInvalidate();
            }
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            baseFlip.setTouchPoint(x,y);
            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y){
                isRuning = false;
            }
            postInvalidate();
        }
        super.computeScroll();
    }

    public void abortAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            baseFlip.setTouchPoint(mScroller.getFinalX(),mScroller.getFinalY());
            postInvalidate();
        }
    }

    public boolean isRunning(){
        return isRuning;
    }

    public void setTouchListener(TouchListener mTouchListener){
        this.mTouchListener = mTouchListener;
    }

    public interface TouchListener{
        void center();
        Boolean prePage();
        Boolean nextPage();
        void cancel();
    }
}
