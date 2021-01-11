package com.example.localreader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public abstract class BaseFragment extends Fragment {

    private View rootView;
    private Unbinder mUnbinder;
    /**
     * 初始化布局
     */
    protected abstract int getLayoutRes();

    protected abstract void initData(View view);

    protected abstract void initListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        rootView = view;
        // 初始化View注入
        mUnbinder = ButterKnife.bind(this,view);
        initData(view);
        initListener();
        return view;
    }

    public View getRootView(){
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
