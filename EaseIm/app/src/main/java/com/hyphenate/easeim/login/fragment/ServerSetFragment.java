package com.hyphenate.easeim.login.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.model.DemoServerSetBean;
import com.hyphenate.easeim.section.base.BaseInitFragment;
import com.hyphenate.easeim.section.dialog.DemoDialogFragment;
import com.hyphenate.easeim.section.dialog.SimpleDialogFragment;
import com.hyphenate.easeui.widget.EaseTitleBar;

public class ServerSetFragment extends BaseInitFragment implements EaseTitleBar.OnBackPressListener, CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener {
    private EaseTitleBar mToolbarServer;
    private Switch mSwitchServer;
    private TextView mEtServerHint;
    private EditText mEtAppkey;
    private Switch mSwitchSpecifyServer;
    private EditText mEtServerAddress;
    private EditText mEtServerPort;
    private EditText mEtServerRest;
    private Switch mSwitchHttpsSet;
    private Button mBtnReset;
    private Button mBtnServer;
    private Group mGroupServerSet;

    private String mServerAddress;
    private String mServerPort;
    private String mRestServerAddress;
    private String mAppkey;
    private boolean mCustomServerEnable;
    private boolean mCustomSetEnable;

    @Override
    protected int getLayoutId() {
        return R.layout.demo_fragment_server_set;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mToolbarServer = findViewById(R.id.toolbar_server);
        mSwitchServer = findViewById(R.id.switch_server);
        mEtServerHint = findViewById(R.id.et_server_hint);
        mEtAppkey = findViewById(R.id.et_appkey);
        mSwitchSpecifyServer = findViewById(R.id.switch_specify_server);
        mEtServerAddress = findViewById(R.id.et_server_address);
        mEtServerPort = findViewById(R.id.et_server_port);
        mEtServerRest = findViewById(R.id.et_server_rest);
        mSwitchHttpsSet = findViewById(R.id.switch_https_set);
        mBtnReset = findViewById(R.id.btn_reset);
        mBtnServer = findViewById(R.id.btn_server);
        mGroupServerSet = findViewById(R.id.group_server_set);

        checkServerSet();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mToolbarServer.setOnBackPressListener(this);
        mSwitchServer.setOnCheckedChangeListener(this);
        mSwitchSpecifyServer.setOnCheckedChangeListener(this);
        mEtAppkey.addTextChangedListener(this);
        mEtServerAddress.addTextChangedListener(this);
        mEtServerPort.addTextChangedListener(this);
        mEtServerRest.addTextChangedListener(this);
        mBtnReset.setOnClickListener(this);
        mBtnServer.setOnClickListener(this);
    }

    /**
     * ?????????????????????
     */
    private void checkServerSet() {
        boolean isInited = EaseIMHelper.getInstance().isSDKInit();

        //???????????????????????????????????????????????????????????????
        mCustomSetEnable = EaseIMHelper.getInstance().getModel().isCustomSetEnable();
        mCustomServerEnable = EaseIMHelper.getInstance().getModel().isCustomServerEnable();
        mSwitchServer.setChecked(mCustomSetEnable);
        mSwitchSpecifyServer.setChecked(mCustomServerEnable);
        mSwitchHttpsSet.setChecked(EaseIMHelper.getInstance().getModel().getUsingHttpsOnly());
        String appkey = EaseIMHelper.getInstance().getModel().getCutomAppkey();
        mEtAppkey.setText((!EaseIMHelper.getInstance().getModel().isCustomAppkeyEnabled() || TextUtils.isEmpty(appkey)) ? "":appkey);
        String imServer = EaseIMHelper.getInstance().getModel().getIMServer();
        mEtServerAddress.setText(TextUtils.isEmpty(imServer) ? "" : imServer);
        int imServerPort = EaseIMHelper.getInstance().getModel().getIMServerPort();
        mEtServerPort.setText(imServerPort == 0 ? "" : imServerPort+"");
        String restServer = EaseIMHelper.getInstance().getModel().getRestServer();
        mEtServerRest.setText(TextUtils.isEmpty(restServer) ? "" : restServer);
        mGroupServerSet.setVisibility(mSwitchServer.isChecked() ? View.VISIBLE : View.GONE);
        setResetButtonVisible(mSwitchServer.isChecked(), isInited);
        //??????????????????
        mEtServerHint.setVisibility(isInited ? View.VISIBLE : View.GONE);
        mEtAppkey.setEnabled(!isInited);
        mSwitchSpecifyServer.setEnabled(!isInited);
        mEtServerAddress.setEnabled(!isInited && mCustomServerEnable);
        mEtServerPort.setEnabled(!isInited && mCustomServerEnable);
        mEtServerRest.setEnabled(!isInited && mCustomServerEnable);
        mSwitchHttpsSet.setEnabled(!isInited && mCustomServerEnable);
        checkButtonEnable();
    }

    /**
     * ???????????????????????????button????????????
     * @param isChecked
     * @param isInited
     */
    private void setResetButtonVisible(boolean isChecked, boolean isInited) {
        mBtnReset.setVisibility(isChecked ? (isInited ? View.GONE : View.VISIBLE) : View.GONE);
    }

    @Override
    public void onBackPress(View view) {
        onBackPress();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_server :
                mCustomSetEnable = isChecked;
                EaseIMHelper.getInstance().getModel().enableCustomSet(isChecked);
                EaseIMHelper.getInstance().getModel().enableCustomAppkey(!TextUtils.isEmpty(mEtAppkey.getText().toString().trim()) && isChecked);
                mGroupServerSet.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                setResetButtonVisible(isChecked, EaseIMHelper.getInstance().isSDKInit());
                break;
            case R.id.switch_specify_server :
                EaseIMHelper.getInstance().getModel().enableCustomServer(isChecked);
                mCustomServerEnable = isChecked;
                mEtServerAddress.setEnabled(isChecked);
                mEtServerPort.setEnabled(isChecked);
                mEtServerRest.setEnabled(isChecked);
                mSwitchHttpsSet.setEnabled(isChecked);
                checkButtonEnable();
                break;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mAppkey = mEtAppkey.getText().toString().trim();
        EaseIMHelper.getInstance().getModel().enableCustomAppkey(!TextUtils.isEmpty(mAppkey) && mSwitchServer.isChecked());
        checkButtonEnable();

    }

    private void checkButtonEnable() {
        mAppkey = mEtAppkey.getText().toString().trim();
        if(mCustomServerEnable) {
            mServerAddress = mEtServerAddress.getText().toString().trim();
            mServerPort = mEtServerPort.getText().toString().trim();
            mRestServerAddress = mEtServerRest.getText().toString().trim();
            setButtonEnable(!TextUtils.isEmpty(mServerAddress)
                    && !TextUtils.isEmpty(mAppkey)
                    && !TextUtils.isEmpty(mServerPort)
                    && !TextUtils.isEmpty(mRestServerAddress));
        }else {
            setButtonEnable(!TextUtils.isEmpty(mAppkey));
        }
        boolean isInited = EaseIMHelper.getInstance().isSDKInit();
        //??????sdk??????????????????????????????????????????????????????????????????
        if(isInited) {
            mBtnServer.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_server) {
            saveServerSet();
        }else if(v.getId() == R.id.btn_reset) {
            new SimpleDialogFragment.Builder(mContext)
                    .setTitle(R.string.em_server_set_dialog_reset)
                    .setOnConfirmClickListener(new DemoDialogFragment.OnConfirmClickListener() {
                        @Override
                        public void onConfirmClick(View view) {
                            DemoServerSetBean set = EaseIMHelper.getInstance().getModel().getDefServerSet();
                            mEtAppkey.setText(set.getAppkey());
                            mEtServerAddress.setText(set.getImServer());
                            mEtServerPort.setText(set.getImPort()+"");
                            mEtServerRest.setText(set.getRestServer());
                        }
                    })
                    .showCancelButton(true)
                    .show();
        }
    }

    private void saveServerSet() {
        if(mCustomServerEnable) {
            //?????????????????????????????????????????????????????????????????????
            if(TextUtils.isEmpty(mAppkey)) {
                showToast(R.string.em_server_set_appkey_empty_hint);
                return;
            }
            if(TextUtils.isEmpty(mServerAddress)) {
                showToast(R.string.em_server_set_im_server_empty_hint);
                return;
            }
            if(TextUtils.isEmpty(mServerPort)) {
                showToast(R.string.em_server_set_im_port_empty_hint);
                return;
            }
            if(TextUtils.isEmpty(mRestServerAddress)) {
                showToast(R.string.em_server_set_rest_server_empty_hint);
                return;
            }
        }
        // ????????????
        if(!TextUtils.isEmpty(mAppkey)) {
            EaseIMHelper.getInstance().getModel().enableCustomAppkey(mSwitchServer.isChecked());
            EaseIMHelper.getInstance().getModel().setCustomAppkey(mAppkey);
        }
        if(!TextUtils.isEmpty(mServerAddress)) {
            EaseIMHelper.getInstance().getModel().setIMServer(mServerAddress);
        }
        if(!TextUtils.isEmpty(mServerPort)) {
            EaseIMHelper.getInstance().getModel().setIMServerPort(Integer.valueOf(mServerPort));
        }
        if(!TextUtils.isEmpty(mRestServerAddress)) {
            EaseIMHelper.getInstance().getModel().setRestServer(mRestServerAddress);
        }
        EaseIMHelper.getInstance().getModel().enableCustomServer(mCustomServerEnable);
        EaseIMHelper.getInstance().getModel().setUsingHttpsOnly(mSwitchHttpsSet.isChecked());

        //??????????????????????????????????????????
        onBackPress();
    }

    private void setButtonEnable(boolean enable) {
        Log.e("TAG", "setButtonEnable = "+enable);
        mBtnServer.setEnabled(enable);
        //????????????????????????drawalbeRight???????????????
//        Drawable rightDrawable;
//        if(enable) {
//            rightDrawable = ContextCompat.getDrawable(mContext, R.drawable.demo_login_btn_right_enable);
//        }else {
//            rightDrawable = ContextCompat.getDrawable(mContext, R.drawable.demo_login_btn_right_unable);
//        }
//        mBtnServer.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

}
