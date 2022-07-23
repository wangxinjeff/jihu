package com.hyphenate.easeim.section.chat.views;

import static com.hyphenate.easeui.constants.EaseConstant.MESSAGE_TYPE_RECALLER;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

public class ChatRowRecall extends EaseChatRow {
    private TextView name;
    private TextView content;

    public ChatRowRecall(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(R.layout.demo_row_recall_message, this);
    }

    @Override
    protected void onFindViewById() {
        name = (TextView) findViewById(R.id.user);
        content = findViewById(R.id.content);
    }

    @Override
    protected void onSetUpView() {
        if(message.getBooleanAttribute(DemoConstant.MESSAGE_TYPE_RECALL, false)){
            String user = message.getFrom();
            if(TextUtils.equals(user, EaseIMHelper.getInstance().getCurrentUser())){
                name.setVisibility(GONE);
                content.setText(R.string.msg_recall_by_self);
            } else {
                name.setVisibility(VISIBLE);
                EaseUserUtils.setUserNick(user, name);
                content.setText(R.string.msg_recall_by_user);
            }
        } else if(!message.getStringAttribute(EaseConstant.MESSAGE_ATTR_CALL_STATE, "").equals("")){
            String createCall = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_CALL_STATE, "");
            String user = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_CALL_USER, "");
            if(TextUtils.equals(createCall, EaseConstant.CONFERENCE_STATE_CREATE)){
                name.setVisibility(VISIBLE);
//            name.setText(user);
                content.setText(context.getString(R.string.em_initiated_call));
//            EaseUserProfileProvider profileProvider = EaseIM.getInstance().getUserProvider();
//            if(profileProvider != null){
//                EaseUser easeUser = profileProvider.getUser(user);
//                if(easeUser != null){
//                    name.setText(easeUser.getNickname());
//                }
//            }
                EaseUserUtils.setUserNick(user, name);
            } else if(TextUtils.equals(createCall, EaseConstant.CONFERENCE_STATE_END)){
                name.setVisibility(GONE);
                content.setText(context.getString(R.string.em_call_over));
            }
        } else if(message.getBooleanAttribute(DemoConstant.CREATE_GROUP_PROMPT, false)){
            name.setVisibility(GONE);
            String groupName = message.getStringAttribute(EaseConstant.CREATE_GROUP_NAME, "");
            content.setText(String.format(context.getString(R.string.em_group_create_success), groupName));
        }
    }
}
