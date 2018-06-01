package com.lp.pickerview.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.Gravity;
import android.view.WindowManager;

import com.lp.pickerview.R;

public abstract class AbsBottomDialog extends AppCompatDialog {
    public AbsBottomDialog(Context context) {
        super(context, R.style.dialog);
        setContentView(getLayoutId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(layoutParams);
        initView();
        initData();
    }


    public abstract int getLayoutId();

    public abstract void initView();

    public abstract void initData();
}
