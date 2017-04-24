package com.fy.niu.fyreorder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fy.niu.fyreorder.R;

/**
 * Created by 25596 on 2017/4/17.
 */
public class GuideFragment extends Fragment {
    private Integer guideImgId;
    public static final String BUNDLE_GUIDE_ID = "guideId";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null){
            guideImgId = bundle.getInt(BUNDLE_GUIDE_ID);
        }

        View view = inflater.inflate(R.layout.guide_fragment, null);
        view.setTag(guideImgId);

        ImageView guideImg = (ImageView) view.findViewById(R.id.guideImg);
        guideImg.setBackgroundResource(guideImgId);

        return view;
    }

    public static GuideFragment newInstance(Integer guideImgId){
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_GUIDE_ID, guideImgId);

        GuideFragment fragment = new GuideFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
