package com.fy.niu.fyreorder.customView;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * Created by 18230 on 2017/4/17.
 */

public class ElasticScrollView extends ScrollView {
    private View inner;
    private float y;
    private Rect normal = new Rect();
    private boolean animationFinish = true;

    public ElasticScrollView(Context context) {
        super(context);
    }

    public ElasticScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElasticScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            inner = getChildAt(0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (inner == null) {
            return super.onTouchEvent(ev);
        } else {
            commOnTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    public void commOnTouchEvent(MotionEvent event) {
        if (animationFinish) {
            int action = event.getAction();
            switch(action) {
                case MotionEvent.ACTION_DOWN:
                    y = event.getY();
                    super.onTouchEvent(event);
                    break;
                case MotionEvent.ACTION_UP:
                    y = 0;
                    if (isNeedAnimation()) {
                        animation();
                    }
                    super.onTouchEvent(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    final float preY = y == 0 ? event.getY() : y;
                    float nowY = event.getY();
                    int deltaY = (int) (preY - nowY) / 3;

                    //滚动
                    //scrollBy(0, deltaY);

                    y = nowY;
                    //当滚动到最上或者最下时就不会再滚动，这时移动布局
                    if (isNeedMove()) {
                        if (normal.isEmpty()) {
                            //保持正常的布局位置
                            normal.set(inner.getLeft(), inner.getTop(),
                                    inner.getRight(), inner.getBottom());
                        }
                        //移动布局
                        inner.layout(inner.getLeft(), inner.getTop() - deltaY,
                                inner.getRight(), inner.getBottom() - deltaY);
                    }
                    break;
            }
        }
    }

    /**
     * 开启动画移动
     */
    public void animation() {
        //开启移动动画
        TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(), normal.top);
        ta.setDuration(200);
        inner.startAnimation(ta);
        //设置回到正常布局位置
        inner.layout(normal.left, normal.top, normal.right, normal.bottom);
        normal.setEmpty();
    }

    /**
     * 是否需要开启动画
     * @return
     */
    public boolean isNeedAnimation() {
        return !normal.isEmpty();
    }

    /**
     * 是否需要移动布局
     * @return
     */
    public boolean isNeedMove() {
        int offset = inner.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();
        if (scrollY == 0 || scrollY == offset) {
            return true;
        }
        return false;
    }

    /**
     * Runnable延迟执行的时间
     */
    private long delayMillis = 100;

    /**
     * 上次滑动的时间
     */
    private long lastScrollUpdate = -1;

    private OnScrollListener listener;

    /**
     * 设置滑动开始结束监听器
     * @param listener
     */
    public void setOnScrollListener(OnScrollListener listener){
        this.listener = listener;
    }

    // 滑动距离监听器
    public interface OnScrollListener{
        /**
         * 在滑动开始的时候调用
         */
        void onScrollStart();
        /**
         * 在滑动开始的时候调用
         */
        void onScrollEnd();
    }

    @Override
    public void computeScroll() {
        if(lastScrollUpdate == -1){
            if(listener != null){
                listener.onScrollStart();
            }
            postDelayed(scrollerTask, delayMillis);
        }
        // 更新ScrollView的滑动时间
        lastScrollUpdate = System.currentTimeMillis();

        super.computeScroll();
    }

    private Runnable scrollerTask = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastScrollUpdate) > 100) {
                lastScrollUpdate = -1;
                if(listener != null){
                    listener.onScrollEnd();
                }
            } else {
                postDelayed(this, delayMillis);
            }
        }
    };
}
