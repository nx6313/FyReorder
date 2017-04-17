package com.fy.niu.fyreorder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by 25596 on 2017/4/17.
 */

public class HasOrderFragment extends Fragment {
    private String mTitle;
    public static final String BUNDLE_TITLE = "title";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null){
            mTitle = bundle.getString(BUNDLE_TITLE);
        }

        TextView tv = new TextView(getActivity());
        tv.setText(mTitle);
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    public static HasOrderFragment newInstance(String title){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TITLE, title);

        HasOrderFragment fragment = new HasOrderFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
