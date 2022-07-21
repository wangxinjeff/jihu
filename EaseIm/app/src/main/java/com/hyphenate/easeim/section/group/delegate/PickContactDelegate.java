package com.hyphenate.easeim.section.group.delegate;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;


import com.hyphenate.easeim.R;
import com.hyphenate.easeui.adapter.EaseAdapterDelegate;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseUser;

public class PickContactDelegate extends EaseAdapterDelegate<EaseUser, PickContactDelegate.ViewHolder>{

    private onCloseClickListener listener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, String tag) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, EaseUser item) {
        super.onBindViewHolder(holder, position, item);
        holder.nickView.setText(item.getNickname());
        holder.closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onMemberRemove(item);
                }
            }
        });
    }

    public interface onCloseClickListener{
        void onMemberRemove(EaseUser name);
    }

    public void setCloseClickListener(onCloseClickListener listener){
        this.listener = listener;
    }

    static class ViewHolder extends EaseBaseRecyclerViewAdapter.ViewHolder<EaseUser>{
        public Context mContext;
        public AppCompatImageView closeView;
        public AppCompatTextView nickView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            closeView = itemView.findViewById(R.id.iv_close);
            nickView = itemView.findViewById(R.id.tv_name);
        }

        @Override
        public void initView(View itemView) {

        }

        @Override
        public void setData(EaseUser name, int position) {

        }
    }
}

