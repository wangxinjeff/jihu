package com.hyphenate.easeim.section.search.adapter;


import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseDateUtils;
import com.hyphenate.easeui.utils.EaseEditTextUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

import java.util.Date;

public class SearchAllAdapter extends EaseBaseRecyclerViewAdapter<EMMessage> {
    private String keyword;

    @Override
    public int getEmptyLayoutId() {
        return EaseIMHelper.getInstance().isAdmin() ? R.layout.ease_layout_default_no_search_result_admin : R.layout.ease_layout_default_no_search_result;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new SearchAllViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_item_row_chat_history, parent, false));
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private class SearchAllViewHolder extends ViewHolder<EMMessage>{
        private EaseImageView avatar;
        private TextView name;
        private TextView time;
        private ImageView msg_state;
        private TextView message;

        public SearchAllViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            avatar = findViewById(R.id.avatar);
            name = findViewById(R.id.name);
            time = findViewById(R.id.time);
            msg_state = findViewById(R.id.msg_state);
            message = findViewById(R.id.message);
        }

        @Override
        public void setData(EMMessage item, int position) {
            EMMessage.ChatType chatType = item.getChatType();
            time.setText(EaseDateUtils.getTimestampString(mContext, new Date(item.getMsgTime())));
            if(chatType == EMMessage.ChatType.GroupChat || chatType == EMMessage.ChatType.ChatRoom) {
                EMGroup group = EMClient.getInstance().groupManager().getGroup(item.getTo());
                if(group != null){
                    name.setText(group.getGroupName());
                } else {
                    name.setText(item.getTo());
                }
            }else {
//                EaseUserProfileProvider profileProvider = EaseIM.getInstance().getUserProvider();
//                if(profileProvider != null){
                    if(item.direct() == EMMessage.Direct.SEND) {
                        EaseUserUtils.setUserNick(item.getFrom(), name);
                        EaseUserUtils.setUserAvatar(mContext, item.getFrom(), avatar);
                    }else {
                        EaseUserUtils.setUserNick(item.getTo(), name);
                        EaseUserUtils.setUserAvatar(mContext, item.getTo(), avatar);
                    }
//                } else {
//                    if(item.direct() == EMMessage.Direct.SEND) {
//                        name.setText(item.getFrom());
//                    }else {
//                        name.setText(item.getTo());
//                    }
//                }
            }
            if (item.direct() == EMMessage.Direct.SEND && item.status() == EMMessage.Status.FAIL) {
                msg_state.setVisibility(View.VISIBLE);
            } else {
                msg_state.setVisibility(View.GONE);
            }
            String content = EaseSmileUtils.getSmiledText(mContext, EaseCommonUtils.getMessageDigest(item, mContext)).toString();
            if(!TextUtils.isEmpty(keyword)){
                message.post(()-> {
                    String subContent = EaseEditTextUtils.ellipsizeString(message, content, keyword, message.getWidth());
                    SpannableStringBuilder builder = EaseEditTextUtils.highLightKeyword(mContext, subContent, keyword);
                    if(builder != null) {
                        message.setText(builder);
                    }else {
                        message.setText(content);
                    }
                });
            } else {
                message.setText(content);
            }
        }
    }

    @Override
    public boolean filterToCompare(String filter, EMMessage data) {
        if(data.getType() == EMMessage.Type.FILE){
            if(((EMNormalFileMessageBody)data.getBody()).getFileName().contains(filter)){
                return true;
            }
        }

        return false;
    }
}
