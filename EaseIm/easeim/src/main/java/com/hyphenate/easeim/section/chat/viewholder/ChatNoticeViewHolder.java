package com.hyphenate.easeim.section.chat.viewholder;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyphenate.easeim.section.chat.views.ChatRowNotice;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;

public class ChatNoticeViewHolder extends EaseChatRowViewHolder {

    public ChatNoticeViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    public static ChatNoticeViewHolder create(ViewGroup parent, boolean isSender,
                                              MessageListItemClickListener listener) {
        return new ChatNoticeViewHolder(new ChatRowNotice(parent.getContext(), isSender), listener);
    }


}
