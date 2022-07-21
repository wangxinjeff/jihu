package com.hyphenate.easeim.section.chat.views;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

public class ChatRowConferenceState extends EaseChatRow {

    private TextView name;
    private TextView content;

    public ChatRowConferenceState(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate( R.layout.demo_row_conference_state, this);
    }

    @Override
    protected void onFindViewById() {
        name = (TextView) findViewById(R.id.user);
        content = findViewById(R.id.content);
    }

    @Override
    protected void onSetUpView() {
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

    }
}
