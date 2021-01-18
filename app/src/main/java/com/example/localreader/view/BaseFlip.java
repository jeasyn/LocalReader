package com.example.localreader.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.widget.Scroller;

/**
 * @author xialijuan
 * @date 2021/1/9
 */
public abstract class BaseFlip {

    public enum Direction {
        none(true), next(true), pre(true);

        public final boolean IsHorizontal;

        Direction(boolean isHorizontal) {
            IsHorizontal = isHorizontal;
        }
    }

    protected Bitmap curPageBitmap, nextPageBitmap;
    protected float startX;
    protected float startY;
    protected int screenWidth;
    protected int screenHeight;

    protected PointF mTouch = new PointF(); // 拖拽点
    private Direction direction = Direction.none;
    private boolean isCancel = false;

    public BaseFlip(Bitmap mCurrentBitmap, Bitmap mNextBitmap, int width, int height) {
        this.curPageBitmap = mCurrentBitmap;
        this.nextPageBitmap = mNextBitmap;
        this.screenWidth = width;
        this.screenHeight = height;
    }

    //绘制滑动页面
    public abstract void drawMove(Canvas canvas);

    //绘制不滑动页面
    public abstract void drawStatic(Canvas canvas);

    //设置开始拖拽点
    public void setStartPoint(float x, float y) {
        startX = x;
        startY = y;
    }

    //设置拖拽点
    public void setTouchPoint(float x, float y) {
        mTouch.x = x;
        mTouch.y = y;
    }

    //设置方向
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    public abstract void startSliding(Scroller scroller);

    public boolean getCancel() {
        return isCancel;
    }

}
