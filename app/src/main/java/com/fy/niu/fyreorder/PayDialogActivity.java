package com.fy.niu.fyreorder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ant.liao.GifView;
import com.bakerj.infinitecards.AnimationTransformer;
import com.bakerj.infinitecards.CardItem;
import com.bakerj.infinitecards.InfiniteCardView;
import com.bakerj.infinitecards.ZIndexTransformer;
import com.bakerj.infinitecards.transformer.DefaultCommonTransformer;
import com.bakerj.infinitecards.transformer.DefaultTransformerToBack;
import com.bakerj.infinitecards.transformer.DefaultTransformerToFront;
import com.bakerj.infinitecards.transformer.DefaultZIndexTransformerCommon;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.DBOpenHelper;
import com.fy.niu.fyreorder.util.DBUtil;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PayDialogActivity extends Activity {
    private InfiniteCardView payCardView;
    private BaseAdapter mAdapter;

    private GifView payCardLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_dialog);

        ComFun.addToActiveActivityList(PayDialogActivity.this);

        initView();
    }

    private void initView() {
        payCardLoading = (GifView) findViewById(R.id.payCardLoading);
        payCardLoading.setGifImage(R.drawable.loading_girl);
        payCardLoading.setShowDimension(99, 184);
        payCardLoading.setGifImageType(GifView.GifImageType.COVER);
        // 设置弹框Activity页面大小
        WindowManager.LayoutParams p = getWindow().getAttributes();
        //p.height = 1000;
        //p.width = 1400;
        p.alpha = 1.0f; //设置本身透明度
        p.dimAmount = 0.8f; //设置黑暗度
        p.gravity = Gravity.CENTER;
        getWindow().setAttributes(p); //设置生效
        getWindow().setGravity(Gravity.CENTER);

        payCardView = (InfiniteCardView) findViewById(R.id.payCardView);

        setStyle3();

        JSONObject userInfo = DBUtil.find(new DBOpenHelper(PayDialogActivity.this), "userInfo", new String[]{"zhi", "wei"},
                null, null, null, null, null, null);
        List<String> payImgUriList = new ArrayList<>();
        try {
            payImgUriList.add(userInfo.getString("zhi"));
            payImgUriList.add(userInfo.getString("wei"));
        } catch (JSONException e) {
        }
        mAdapter = new PayCardAdapter(payImgUriList);
        payCardView.setAdapter(mAdapter);
    }

    class PayCardAdapter extends BaseAdapter {
        private List<String> payCardImgUris;

        public PayCardAdapter(List<String> payImgUris) {
            this.payCardImgUris = payImgUris;
        }

        @Override
        public int getCount() {
            return payCardImgUris.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView payImgView = new ImageView(PayDialogActivity.this);
            payImgView.setFocusable(true);
            Log.d("加载支付二维码图片", "图片地址：" + payCardImgUris.get(position));
            Picasso.with(PayDialogActivity.this).load(payCardImgUris.get(position)).placeholder(R.drawable.pay_default).error(R.drawable.pay_default).into(payImgView);
            payImgView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        payCardView.bringCardToFront(mAdapter.getCount() - 1);
                    }
                    return false;
                }
            });
            return payImgView;
        }
    }

    private void setStyle1() {
        payCardView.setClickable(true);
        payCardView.setAnimType(InfiniteCardView.ANIM_TYPE_FRONT);
        payCardView.setAnimInterpolator(new LinearInterpolator());
        payCardView.setTransformerToFront(new DefaultTransformerToFront());
        payCardView.setTransformerToBack(new DefaultTransformerToBack());
        payCardView.setZIndexTransformerToBack(new DefaultZIndexTransformerCommon());
    }

    private void setStyle3() {
        payCardView.setClickable(false);
        payCardView.setAnimType(InfiniteCardView.ANIM_TYPE_FRONT_TO_LAST);
        payCardView.setAnimInterpolator(new OvershootInterpolator(-8));
        payCardView.setTransformerToFront(new DefaultCommonTransformer());
        payCardView.setTransformerToBack(new AnimationTransformer() {
            @Override
            public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
                int positionCount = fromPosition - toPosition;
                float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
                ViewHelper.setScaleX(view, scale);
                ViewHelper.setScaleY(view, scale);
                if (fraction < 0.5) {
                    ViewCompat.setTranslationX(view, cardWidth * fraction * 1.5f);
                    ViewCompat.setRotationY(view, -45 * fraction);
                } else {
                    ViewCompat.setTranslationX(view, cardWidth * 1.5f * (1f - fraction));
                    ViewCompat.setRotationY(view, -45 * (1 - fraction));
                }
            }

            @Override
            public void transformInterpolatedAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
                int positionCount = fromPosition - toPosition;
                float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
                ViewHelper.setTranslationY(view, -cardHeight * (0.8f - scale) * 0.5f - cardWidth * (0.02f *
                        fromPosition - 0.02f * fraction * positionCount));
            }
        });
        payCardView.setZIndexTransformerToBack(new ZIndexTransformer() {
            @Override
            public void transformAnimation(CardItem card, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
                if (fraction < 0.5f) {
                    card.zIndex = 1f + 0.01f * fromPosition;
                } else {
                    card.zIndex = 1f + 0.01f * toPosition;
                }
            }

            @Override
            public void transformInterpolatedAnimation(CardItem card, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {

            }
        });
    }
}
