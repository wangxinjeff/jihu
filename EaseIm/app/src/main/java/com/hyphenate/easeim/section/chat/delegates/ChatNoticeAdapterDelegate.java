package com.hyphenate.easeim.section.chat.delegates;

import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.section.chat.viewholder.ChatNoticeViewHolder;
import com.hyphenate.easeim.section.chat.views.ChatRowNotice;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

import static com.hyphenate.chat.EMMessage.Type.TXT;

public class ChatNoticeAdapterDelegate extends EaseMessageAdapterDelegate<EMMessage, EaseChatRowViewHolder> {

    @Override
    public boolean isForViewType(EMMessage item, int position) {
        return item.getType() == TXT && (item.getBooleanAttribute(DemoConstant.MESSAGE_TYPE_RECALL, false)
                || !item.getStringAttribute(EaseConstant.MESSAGE_ATTR_CALL_STATE, "").equals("")
                || item.getBooleanAttribute(DemoConstant.CREATE_GROUP_PROMPT, false));
    }

    @Override
    protected EaseChatRow getEaseChatRow(ViewGroup parent, boolean isSender) {
        return new ChatRowNotice(parent.getContext(), isSender);
    }

    @Override
    protected EaseChatRowViewHolder createViewHolder(View view, MessageListItemClickListener itemClickListener) {
        return new ChatNoticeViewHolder(view, itemClickListener);
    }
}
