package com.hyphenate.easeim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hyphenate.easeim.login.activity.LoginActivity;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.constants.EaseConstant;

public class ChooseActivity extends BaseInitActivity {

    Button customer;
    Button admin;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        if(EaseIMHelper.getInstance().getAutoLogin()){
            if(EaseIMHelper.getInstance().getModel().getAppMode()){
                EaseIMHelper.getInstance().startChat(mContext, EaseConstant.CON_TYPE_ADMIN);
            } else {
                startActivity(new Intent(mContext, MainActivity.class));
            }
            finish();
        }

        customer = findViewById(R.id.customer);
        admin = findViewById(R.id.admin);
    }

    @Override
    protected void initListener() {
        super.initListener();
        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EaseIMHelper.getInstance().getModel().setAppMode(false);
                LoginActivity.startAction(ChooseActivity.this);
                finish();
            }
        });
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EaseIMHelper.getInstance().getModel().setAppMode(true);
                startActivity(new Intent(ChooseActivity.this, AdminLoginActivity.class));
                finish();
            }
        });
    }
}
