package com.hyphenate.easeim.section.group.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.utils.EaseUserUtils;

public class GroupMuteAdapter extends EaseBaseRecyclerViewAdapter<String>{
    private OnUnMuteClickListener listener;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupMuteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.em_mute_member_item, parent, false));
    }

    class GroupMuteViewHolder extends EaseBaseRecyclerViewAdapter.ViewHolder<String> {
        AppCompatImageView avatar;
        AppCompatTextView name;
        AppCompatButton btnUnMute;
        Context mContext;

        public GroupMuteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mContext = itemView.getContext();
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            btnUnMute = itemView.findViewById(R.id.btn_remove_mute);
        }

        @Override
        public void setData(String item, int position) {
            EaseUserUtils.setUserAvatar(mContext, item, avatar);
            EaseUserUtils.setUserNick(item, name);
            btnUnMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        listener.onUnMute(item);
                    }
                }
            });
        }
    }

    public void setOnUnMuteClickListener(OnUnMuteClickListener listener){
        this.listener = listener;
    }

    public interface OnUnMuteClickListener{
        void onUnMute(String username);
    }
}
