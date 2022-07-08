package com.hyphenate.easeim;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.constants.EaseConstant;

import java.util.Map;

public class MainActivity extends BaseInitActivity implements View.OnClickListener {
    private AppCompatImageView groupIcon;
    private AppCompatTextView groupUnread;
    private AppCompatTextView chatText;
    private AppCompatTextView chatUnread;

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_main;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        groupIcon = findViewById(R.id.icon);
        groupUnread = findViewById(R.id.group_unread);
        chatText = findViewById(R.id.text);
        chatUnread = findViewById(R.id.chat_unread);
    }

    @Override
    protected void initData() {
        super.initData();
        EaseIMHelper.getInstance().getChatUnread(new EMValueCallBack<Map<String, Integer>>() {
            @Override
            public void onSuccess(Map<String, Integer> stringIntegerMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        groupUnread.setText(stringIntegerMap.get(EaseConstant.UNREAD_EXCLUSIVE_GROUP).toString());
                        chatUnread.setText(stringIntegerMap.get(EaseConstant.UNREAD_MY_CHAT)+"");
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        groupIcon.setOnClickListener(this);
        chatText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.icon:
                EaseIMHelper.getInstance().startChat(MainActivity.this, EaseConstant.CON_TYPE_EXCLUSIVE);
                break;
            case R.id.text:
                EaseIMHelper.getInstance().startChat(MainActivity.this, EaseConstant.CON_TYPE_MY_CHAT);
                break;
        }
    }
}
