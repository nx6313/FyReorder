package com.fy.niu.fyreorder.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.fy.niu.fyreorder.R;

public class SlideMenu extends HorizontalScrollView {
    private LinearLayout mWapper;
    private ViewGroup mMenu;
    private ViewGroup mContent;
    private FrameLayout mContentMask;
    private int mScreenWidth;
    private int mMenuWidth;

    private int mMenuRightPadding = 50; // dp

    private boolean once = false;
    private boolean isOpen = false;

    public SlideMenu(Context context) {
        this(context, null);
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContentMask = new FrameLayout(context);
        mContentMask.setBackgroundColor(0x88000000);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlideMenu, defStyle, 0);
        int n = a.getIndexCount();
        for(int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SlideMenu_rightPadding:
                    mMenuRightPadding = a.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
                    break;
            }
        }
        a.recycle();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(!once){
            mWapper = (LinearLayout) getChildAt(0);
            mWapper.addView(mContentMask, 2);
            mContentMask.setAlpha(0);
            mMenu = (ViewGroup) mWapper.getChildAt(0);
            mContent = (ViewGroup) mWapper.getChildAt(1);
            mContentMask.measure(widthMeasureSpec, heightMeasureSpec);
            mMenuWidth = mMenu.getLayoutParams().width = mScreenWidth - mMenuRightPadding;
            mContent.getLayoutParams().width = mScreenWidth;
            once = true;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mContentMask.layout(l + mMenu.getMeasuredWidth(), 0, r + mMenu.getMeasuredWidth(), b);
        if(changed) {
            this.scrollTo(mMenuWidth, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int scrollX = getScrollX();
                if(scrollX >= mMenuWidth / 2){
                    this.smoothScrollTo(mMenuWidth, 0);
                    isOpen = false;
                } else {
                    this.smoothScrollTo(0, 0);
                    isOpen = true;
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float curX = Math.abs(l);
        float scale =  1f - curX / (float) mMenu.getMeasuredWidth();
        mContentMask.setAlpha(scale);
    }

    public void openMenu() {
        if(isOpen) return;
        this.smoothScrollTo(0, 0);
        isOpen = true;
    }

    public void closeMenu() {
        if(!isOpen) return;
        this.smoothScrollTo(mMenuWidth, 0);
        isOpen = false;
    }

    public void toggle() {
        if(isOpen) {
            closeMenu();
        }else{
            openMenu();
        }
    }
}
