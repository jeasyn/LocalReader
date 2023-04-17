package com.example.localreader.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.widget.Scroller;

/**
 * Created by xialijuan on 27/12/2020.
 */
public class CoverFlip extends BaseFlip {

    private Rect srcRect, destRect;
    private GradientDrawable drawable;

    @Override
    public void drawMove(Canvas canvas) {
        if (getDirection().equals(BaseFlip.Direction.next)){
            int dis = (int) (screenWidth - startX + mTouch.x);
            if (dis > screenWidth){
                dis = screenWidth;
            }
            // 计算bitmap截取的区域
            srcRect.left = screenWidth - dis;
            // 计算bitmap在canvas显示的区域
            destRect.right = dis;
            canvas.drawBitmap(nextPageBitmap,0,0,null);
            canvas.drawBitmap(curPageBitmap, srcRect, destRect,null);
            addShadow(dis,canvas);
        }else{
            srcRect.left = (int) (screenWidth - mTouch.x);
            destRect.right = (int) mTouch.x;
            canvas.drawBitmap(curPageBitmap,0,0,null);
            canvas.drawBitmap(nextPageBitmap, srcRect, destRect,null);
            addShadow((int) mTouch.x,canvas);
        }
    }

    @Override
    public void drawStatic(Canvas canvas) {
        if (getCancel()){
            canvas.drawBitmap(curPageBitmap, 0, 0, null);
        }else {
            canvas.drawBitmap(nextPageBitmap, 0, 0, null);
        }
    }

    /**
     * 添加阴影
     * @param left 矩形左侧的X坐标
     * @param canvas
     */
    public void addShadow(int left,Canvas canvas) {
        drawable.setBounds(left, 0, left + 30 , screenHeight);
        drawable.draw(canvas);
    }

    @Override
    public void startSliding(Scroller scroller) {
        int dx;
        if (getDirection().equals(Direction.next)){
            if (getCancel()){
                int dis = (int) ((screenWidth - startX) + mTouch.x);
                if (dis > screenWidth){
                    dis = screenWidth;
                }
                dx = screenWidth - dis;
            }else{
                dx = (int) - (mTouch.x + (screenWidth - startX));
            }
        }else{
            if (getCancel()){
                dx = (int) - mTouch.x;
            }else{
                dx = (int) (screenWidth - mTouch.x);
            }
        }
        // 滑动速度保持一致
        int duration =  (400 * Math.abs(dx)) / screenWidth;
        scroller.startScroll((int) mTouch.x, 0, dx, 0, duration);
    }

    public CoverFlip(Bitmap currentBitmap, Bitmap nextBitmap, int width, int height) {
        super(currentBitmap, nextBitmap, width, height);
        srcRect = new Rect(0, 0, screenWidth, screenHeight);
        destRect = new Rect(0, 0, screenWidth, screenHeight);
        int[] mBackShadowColors = new int[] { 0x66000000,0x00000000};
        drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }
}
