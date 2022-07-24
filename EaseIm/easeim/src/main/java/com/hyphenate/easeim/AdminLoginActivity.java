package com.hyphenate.easeim;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.constants.EaseConstant;

public class AdminLoginActivity extends BaseInitActivity implements View.OnClickListener {

    private AppCompatEditText phoneView;
    private AppCompatEditText passwordView;
    private AppCompatButton btnLogin;
    private AppCompatImageView emptyIcon;
    private FrameLayout visibleView;
    private AppCompatImageView visibleIcon;
    private AppCompatImageView unVisibleIcon;

    @Override
    protected int getLayoutId() {
        return R.layout.em_activity_admin_login;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        phoneView = findViewById(R.id.phone);
        passwordView = findViewById(R.id.password);
        btnLogin = findViewById(R.id.login_btn);
        emptyIcon = findViewById(R.id.empty_phone);
        visibleView = findViewById(R.id.visible_view);
        visibleIcon = findViewById(R.id.visible);
        unVisibleIcon = findViewById(R.id.un_visible);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initListener() {
        super.initListener();
        phoneView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(phoneView.getText().toString().trim())){
                    emptyIcon.setVisibility(View.VISIBLE);
                    if(!TextUtils.isEmpty(passwordView.getText().toString().trim())){
                        btnLogin.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.login_btn_bg));
                        btnLogin.setEnabled(true);
                        btnLogin.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    } else {
                        btnLogin.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.login_btn_unable_bg));
                        btnLogin.setEnabled(false);
                        btnLogin.setTextColor(ContextCompat.getColor(mContext, R.color.con_content_time));
                    }
                } else {
                    emptyIcon.setVisibility(View.INVISIBLE);
                    btnLogin.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.login_btn_unable_bg));
                    btnLogin.setEnabled(false);
                    btnLogin.setTextColor(ContextCompat.getColor(mContext, R.color.con_content_time));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(passwordView.getText().toString().trim())){
                    if(!TextUtils.isEmpty(phoneView.getText().toString().trim())){
                        btnLogin.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.login_btn_bg));
                        btnLogin.setEnabled(true);
                        btnLogin.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    } else {
                        btnLogin.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.login_btn_unable_bg));
                        btnLogin.setEnabled(false);
                        btnLogin.setTextColor(ContextCompat.getColor(mContext, R.color.con_content_time));
                    }
                } else {
                    btnLogin.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.login_btn_unable_bg));
                    btnLogin.setEnabled(false);
                    btnLogin.setTextColor(ContextCompat.getColor(mContext, R.color.con_content_time));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emptyIcon.setOnClickListener(this);
        visibleView.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.empty_phone){
            phoneView.setText("");
        } else if(v.getId() == R.id.visible_view){
            if(visibleIcon.getVisibility() == View.VISIBLE){
                passwordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordView.setSelection(passwordView.getText().length());
                visibleIcon.setVisibility(View.GONE);
                unVisibleIcon.setVisibility(View.VISIBLE);
            } else {
                passwordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordView.setSelection(passwordView.getText().length());
                visibleIcon.setVisibility(View.VISIBLE);
                unVisibleIcon.setVisibility(View.GONE);
            }
        } else if(v.getId() == R.id.login_btn){
            showLoading("登录中");
            EaseIMHelper.getInstance().initSDK(mContext, true);
            EaseIMHelper.getInstance().loginChat(phoneView.getText().toString().trim(), passwordView.getText().toString().trim(), new EMCallBack() {
                @Override
                public void onSuccess() {
                    dismissLoading();
                    EaseIMHelper.getInstance().startChat(mContext, EaseConstant.CON_TYPE_ADMIN);
                    finish();
                }

                @Override
                public void onError(int i, String s) {
                    dismissLoading();
                    if(i == EMError.USER_AUTHENTICATION_FAILED) {
                        ToastUtils.showToast(R.string.demo_error_user_authentication_failed);
                    }else {
                        ToastUtils.showToast(s);
                    }
                }
            });
        }
    }
}
