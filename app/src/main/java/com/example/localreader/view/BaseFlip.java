package com.example.localreader.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.widget.Scroller;

/**
 * @author xialijuan
 * @date 2020/12/18
 */
public abstract class BaseFlip {

    public enum Direction {
        /**
         * 不滑动
         */
        none(true),
        /**
         * 从右往左滑
         */
        next(true),
        /**
         * 从左往右滑
         */
        pre(true);

        public final boolean horizontal;

        Direction(boolean horizontal) {
            this.horizontal = horizontal;
        }
    }

    protected Bitmap curPageBitmap, nextPageBitmap;
    protected float startX;
    protected float startY;
    protected int screenWidth;
    protected int screenHeight;

    /**
     * 拖拽点
     */
    protected PointF mTouch = new PointF();
    /**
     * 滑动方向
     */
    private Direction direction = Direction.none;
    private boolean isCancel = false;

    public BaseFlip(Bitmap mCurrentBitmap, Bitmap mNextBitmap, int width, int height) {
        this.curPageBitmap = mCurrentBitmap;
        this.nextPageBitmap = mNextBitmap;
        this.screenWidth = width;
        this.screenHeight = height;
    }

    /**
     * 绘制滑动页面
     *
     * @param canvas
     */
    public abstract void drawMove(Canvas canvas);

    /**
     * 绘制不滑动页面
     *
     * @param canvas
     */
    public abstract void drawStatic(Canvas canvas);

    /**
     * 设置开始拖拽点
     *
     * @param x
     * @param y
     */
    public void setStartPoint(float x, float y) {
        startX = x;
        startY = y;
    }

    public void setTouchPoint(float x, float y) {
        mTouch.x = x;
        mTouch.y = y;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    /**
     * 开始滑动
     *
     * @param scroller
     */
    public abstract void startSliding(Scroller scroller);

    public boolean getCancel() {
        return isCancel;
    }
}
