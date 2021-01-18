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
    private final static String TAG = "PageWidget";
    private int screenWidth = 0; // 屏幕宽
    private int screenHeight = 0; // 屏幕高
    private Context context;

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
    private Boolean isRunning =false;

    Bitmap curPageBitmap = null; // 当前页
    Bitmap nextPageBitmap = null;
    private BaseFlip baseFlip;

    Scroller scroller;
    private int bgColor = 0xFFCEC29C;
    private TouchListener mTouchListener;

    public PageWidget(Context context) {
        this(context,null);
    }

    public PageWidget(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PageWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initPage();
        scroller = new Scroller(getContext(),new LinearInterpolator());
        baseFlip = new CoverFlip(curPageBitmap, nextPageBitmap, screenWidth, screenHeight);
    }

    private void initPage(){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        screenWidth = metric.widthPixels;
        screenHeight = metric.heightPixels;
        curPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        nextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
    }

    public Bitmap getCurPage(){
        return curPageBitmap;
    }

    public Bitmap getNextPage(){
        return nextPageBitmap;
    }

    public void setBgColor(int color){
        bgColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(bgColor);
        if (isRunning) {
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
            isRunning = false;
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
                isRunning = true;
                this.postInvalidate();
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            if (!isMove){
                cancelPage = false;
                //是否点击了中间
                if (downX > screenWidth / 5 && downX < screenWidth * 4 / 5 && downY > screenHeight / 3 && downY < screenHeight * 2 / 3){
                    if (mTouchListener != null){
                        mTouchListener.center();
                    }
                    return true;
                }else if (x < screenWidth / 2){
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
                isRunning = true;
                baseFlip.startSliding(scroller);
                this.postInvalidate();
            }
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            float x = scroller.getCurrX();
            float y = scroller.getCurrY();
            baseFlip.setTouchPoint(x,y);
            if (scroller.getFinalX() == x && scroller.getFinalY() == y){
                isRunning = false;
            }
            postInvalidate();
        }
        super.computeScroll();
    }

    public void abortAnimation() {
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
            baseFlip.setTouchPoint(scroller.getFinalX(), scroller.getFinalY());
            postInvalidate();
        }
    }

    public boolean isRunning(){
        return isRunning;
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
