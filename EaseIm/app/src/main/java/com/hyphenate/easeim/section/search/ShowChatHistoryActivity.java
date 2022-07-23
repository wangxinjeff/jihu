package com.hyphenate.easeim.section.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.modules.chat.EaseChatMessageListLayout;
import com.hyphenate.easeui.widget.EaseTitleBar;

public class ShowChatHistoryActivity extends BaseInitActivity implements EaseChatMessageListLayout.OnMessageTouchListener
        , MessageListItemClickListener, EaseChatMessageListLayout.OnChatErrorListener{

    private EaseChatMessageListLayout messageListLayout;
    private EaseTitleBar titleBar;
    private String conversationId;
    private int chatType;
    private String historyMsgId;

    public static void actionStart(Context context, String conversationId, int chatType, String historyMsgId){
        Intent intent = new Intent(context, ShowChatHistoryActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType);
        intent.putExtra(EaseConstant.HISTORY_MSG_ID, historyMsgId);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_history;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        conversationId = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID);
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
        historyMsgId = intent.getStringExtra(EaseConstant.HISTORY_MSG_ID);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        if(EaseIMHelper.getInstance().isAdmin()){
            titleBar.setLeftImageResource(R.drawable.icon_back_admin);
        }
        messageListLayout = findViewById(R.id.layout_chat_message);
        messageListLayout.init(EaseChatMessageListLayout.LoadDataType.HISTORY, conversationId, chatType);
    }

    @Override
    protected void initData() {
        super.initData();
        messageListLayout.loadData(historyMsgId);
    }

    @Override
    protected void initListener() {
        super.initListener();
        messageListLayout.setOnMessageTouchListener(this);
        messageListLayout.setMessageListItemClickListener(this);
        messageListLayout.setOnChatErrorListener(this);
        titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onBubbleClick(EMMessage message) {
        return false;
    }

    @Override
    public boolean onResendClick(EMMessage message) {
        return false;
    }

    @Override
    public boolean onBubbleLongClick(View v, EMMessage message) {
        return false;
    }

    @Override
    public void onUserAvatarClick(String username) {

    }

    @Override
    public void onUserAvatarLongClick(String username) {

    }

    @Override
    public void onMessageCreate(EMMessage message) {

    }

    @Override
    public void onMessageSuccess(EMMessage message) {

    }

    @Override
    public void onMessageError(EMMessage message, int code, String error) {

    }

    @Override
    public void onMessageInProgress(EMMessage message, int progress) {

    }

    @Override
    public void onReadNumClick(EMMessage message) {

    }

    @Override
    public void onTouchItemOutside(View v, int position) {

    }

    @Override
    public void onViewDragging() {

    }

    @Override
    public void onChatError(int code, String errorMsg) {

    }
}
