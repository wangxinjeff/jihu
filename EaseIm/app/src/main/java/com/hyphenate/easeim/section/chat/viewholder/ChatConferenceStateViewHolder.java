package com.hyphenate.easeim.section.chat.viewholder;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.section.chat.views.ChatRowConferenceInvite;
import com.hyphenate.easeim.section.chat.views.ChatRowConferenceState;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;

public class ChatConferenceStateViewHolder extends EaseChatRowViewHolder {

    public ChatConferenceStateViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    public static ChatConferenceStateViewHolder create(ViewGroup parent, boolean isSender,
                                                       MessageListItemClickListener itemClickListener) {
        return new ChatConferenceStateViewHolder(new ChatRowConferenceState(parent.getContext(), isSender), itemClickListener);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        super.onBubbleClick(message);
    }
}
