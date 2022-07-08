package com.hyphenate.easeim.section.group.delegate;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.adapter.EaseAdapterDelegate;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseUser;

public class GroupMemberDelegate extends EaseAdapterDelegate<EaseUser, GroupMemberDelegate.ViewHolder>{


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, String tag) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, EaseUser item) {
        super.onBindViewHolder(holder, position, item);
        holder.ownerView.setVisibility(View.GONE);
        if(item.isOwner()){
            holder.ownerView.setVisibility(View.VISIBLE);
        }
        String avatarUrl = item.getAvatar();
        String nickname = item.getNickname();
        if(!TextUtils.isEmpty(avatarUrl)){
            Glide.with(holder.mContext).load(avatarUrl).apply(RequestOptions.bitmapTransform(new CircleCrop())).error(R.drawable.ease_group_icon).into(holder.avatarView);
        }
        holder.nickView.setText(nickname);
    }

    class ViewHolder extends EaseBaseRecyclerViewAdapter.ViewHolder<EaseUser>{
        public Context mContext;
        public AppCompatImageView avatarView;
        public AppCompatImageView ownerView;
        public AppCompatTextView nickView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            avatarView = itemView.findViewById(R.id.member_item_avatar);
            nickView = itemView.findViewById(R.id.member_item_nick);
            ownerView = itemView.findViewById(R.id.member_item_owner);
        }

        @Override
        public void initView(View itemView) {

        }

        @Override
        public void setData(EaseUser item, int position) {

        }
    }
}

