package com.hyphenate.easeim.section.conversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMUserInfo;
import com.hyphenate.easecallkit.base.EaseCallType;

import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.HMSPushHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.utils.PreferenceManager;
import com.hyphenate.easeim.common.utils.PushUtils;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.av.MultipleVideoActivity;
import com.hyphenate.easeim.section.av.VideoCallActivity;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.chat.ChatPresenter;
import com.hyphenate.easeim.section.group.activity.GroupApplyActivity;
import com.hyphenate.easeim.section.group.activity.GroupPickContactsActivity;
import com.hyphenate.easeim.section.search.SearchGroupChatActivity;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.ui.base.EaseBaseFragment;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.EMLog;
import com.hyphenate.chat.EMUserInfo.*;

import java.util.Map;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class ConversationListActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener{
    private EaseTitleBar mTitleBar;
    private EaseBaseFragment mConversationListFragment;
    private boolean showMenu = true;//是否显示菜单项
    private int conversationsType = EaseConstant.CON_TYPE_EXCLUSIVE;

    private View popView;
    private LinearLayout notifyView;
    private LinearLayout searchView;
    private LinearLayout createView;
    private LinearLayout applyView;
    private PopupWindow popupWindow;
    private AppCompatImageView notifyIcon;
    private AppCompatImageView applyUnread;
    private int xoff;

    private boolean isNotify;

    public static void actionStart(Context context, int conversationsType){
        Intent intent = new Intent(context, ConversationListActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATIONS_TYPE, conversationsType);
        context.startActivity(intent);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        conversationsType = intent.getIntExtra(EaseConstant.EXTRA_CONVERSATIONS_TYPE, EaseConstant.CON_TYPE_EXCLUSIVE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_conversation_list;
    }

    @Override
    protected void initSystemFit() {
        setFitSystemForTheme(false);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mTitleBar = findViewById(R.id.title_bar_main);
        if(conversationsType == EaseConstant.CON_TYPE_EXCLUSIVE){
            mTitleBar.setTitle(getString(R.string.my_exclusive_service));
        } else if(conversationsType == EaseConstant.CON_TYPE_MY_CHAT){
            mTitleBar.setTitle(getString(R.string.my_chat));
        } else if(conversationsType == EaseConstant.CON_TYPE_ADMIN){
            mTitleBar.setTitle(getString(R.string.my_conversations));
            mTitleBar.setLeftImageResource(R.drawable.icon_back_admin);
            mTitleBar.setRightImageResource(R.drawable.em_home_menu_add);
        }
        switchToHome();

        popView = LayoutInflater.from(this).inflate(R.layout.pop_conversation_list, null, false);
        notifyView = popView.findViewById(R.id.notify_view);
        searchView = popView.findViewById(R.id.search_view);
        createView = popView.findViewById(R.id.create_view);
        applyView = popView.findViewById(R.id.apply_view);
        notifyIcon = popView.findViewById(R.id.notify_icon);
        applyUnread = popView.findViewById(R.id.apply_unread);

        showNotifyIcon();

        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        // 这里单独写一篇文章来分析
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        int popupWidth = popView.getMeasuredWidth();
        int mScreenWidth  = this.getResources().getDisplayMetrics().widthPixels;
        xoff = mScreenWidth - popupWidth;

    }

    @Override
    protected void initListener() {
        super.initListener();
        mTitleBar.setOnBackPressListener(this);
        mTitleBar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(mTitleBar, xoff, 0);
            }
        });
        notifyView.setOnClickListener(this);
        searchView.setOnClickListener(this);
        createView.setOnClickListener(this);
        applyView.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        initViewModel();
        ChatPresenter.getInstance().init();
        // 获取华为 HMS 推送 token
        HMSPushHelper.getInstance().getHMSToken(this);

        //判断是否为来电推送
        if(PushUtils.isRtcCall){
            if (EaseCallType.getfrom(PushUtils.type) != EaseCallType.CONFERENCE_CALL) {
                    Intent intent = new Intent(getApplicationContext(), VideoCallActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplication().getApplicationContext(), MultipleVideoActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
            PushUtils.isRtcCall  = false;
        }

        if(EaseIMHelper.getInstance().getModel().isUseFCM() && GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS){
            // 启用 FCM 自动初始化
            if(!FirebaseMessaging.getInstance().isAutoInitEnabled()){
                FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
            }
            // 获取FCM 推送 token 并上传
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        EMLog.d("FCM", "Fetching FCM registration token failed:"+task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    EMLog.d("FCM", token);
                    EMClient.getInstance().sendFCMTokenToServer(token);
                }
            });
        }
    }

    private void initViewModel() {

    }

    private void switchToHome() {
        if(mConversationListFragment == null) {
            mConversationListFragment = new ConversationListFragment(conversationsType);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_fragment, mConversationListFragment, "conversation").commit();
    }

    private void fetchSelfInfo(){
        String[] userId = new String[1];
        userId[0] = EMClient.getInstance().getCurrentUser();
        EMUserInfoType[] userInfoTypes = new EMUserInfoType[2];
        userInfoTypes[0] = EMUserInfoType.NICKNAME;
        userInfoTypes[1] = EMUserInfoType.AVATAR_URL;
        EMClient.getInstance().userInfoManager().fetchUserInfoByAttribute(userId, userInfoTypes,new EMValueCallBack<Map<String, EMUserInfo>>() {
            @Override
            public void onSuccess(Map<String, EMUserInfo> userInfos) {
                runOnUiThread(new Runnable() {
                    public void run() {
                       EMUserInfo userInfo = userInfos.get(EMClient.getInstance().getCurrentUser());
                        //昵称
                        if(userInfo != null && userInfo.getNickName() != null &&
                                userInfo.getNickName().length() > 0){
                            EaseEvent event = EaseEvent.create(DemoConstant.NICK_NAME_CHANGE, EaseEvent.TYPE.CONTACT);
                            event.message = userInfo.getNickName();
                            LiveDataBus.get().with(DemoConstant.NICK_NAME_CHANGE).postValue(event);
                            PreferenceManager.getInstance().setCurrentUserNick(userInfo.getNickName());
                        }
                        //头像
                        if(userInfo != null && userInfo.getAvatarUrl() != null && userInfo.getAvatarUrl().length() > 0){

                            EaseEvent event = EaseEvent.create(DemoConstant.AVATAR_CHANGE, EaseEvent.TYPE.CONTACT);
                            event.message = userInfo.getAvatarUrl();
                            LiveDataBus.get().with(DemoConstant.AVATAR_CHANGE).postValue(event);
                            PreferenceManager.getInstance().setCurrentUserAvatar(userInfo.getAvatarUrl());
                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e("MainActivity","fetchUserInfoByIds error:" + error + " errorMsg:" + errorMsg);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EaseIMHelper.getInstance().showNotificationPermissionDialog();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.notify_view:
                EaseIMHelper.getInstance().getModel().setConversationNotify(!isNotify);
                showNotifyIcon();
                if(isNotify){
                    ToastUtils.showCenterToast("", getString(R.string.em_open_notify), 0, Toast.LENGTH_SHORT);
                } else {
                    ToastUtils.showCenterToast("", getString(R.string.em_close_notify), 0, Toast.LENGTH_SHORT);
                }
                break;
            case R.id.search_view:
                startActivity(new Intent(this, SearchGroupChatActivity.class));
                break;
            case R.id.create_view:
                GroupPickContactsActivity.actionStartForResult(mContext, "", true);
                break;
            case R.id.apply_view:
                startActivity(new Intent(ConversationListActivity.this, GroupApplyActivity.class));
                break;
        }
        popupWindow.dismiss();
    }

    private void showNotifyIcon(){
        isNotify = EaseIMHelper.getInstance().getModel().getConversationNotify();
        if(isNotify){
            notifyIcon.setImageResource(R.drawable.icon_message_notify);
        } else {
            notifyIcon.setImageResource(R.drawable.icon_message_unnotify);
        }
    }
}
