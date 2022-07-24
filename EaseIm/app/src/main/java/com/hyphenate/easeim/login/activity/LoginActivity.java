package com.hyphenate.easeim.login.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.MainActivity;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.repositories.EMClientRepository;
import com.hyphenate.easeim.section.base.BaseFragment;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.login.fragment.LoginFragment;
import com.hyphenate.easeim.login.fragment.ServerSetFragment;
import com.hyphenate.easeim.login.viewmodels.LoginViewModel;

public class LoginActivity extends BaseInitActivity {

    public static void startAction(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_login;
    }

    @Override
    protected void initSystemFit() {
        setFitSystemForTheme(false, R.color.transparent);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        if(EaseIMHelper.getInstance().getAutoLogin()){
            new EMClientRepository().loginSuccess();
            startActivity(new Intent(mContext, MainActivity.class));
            finish();
        }
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.fl_fragment, new LoginFragment()).
                commit();
    }

    @Override
    protected void initData() {
        super.initData();
        LoginViewModel viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.getPageSelect().observe(this, page -> {
            if(page == 0) {
                return;
            }
            if(page == 2) {
                replace(new ServerSetFragment());
            }

        });
    }

    private void replace(BaseFragment fragment) {
        getSupportFragmentManager().
                beginTransaction().
                setCustomAnimations(
                        R.anim.slide_in_from_right,
                        R.anim.slide_out_to_left,
                        R.anim.slide_in_from_left,
                        R.anim.slide_out_to_right
                ).
                replace(R.id.fl_fragment, fragment).
                addToBackStack(null).
                commit();
    }
}
