package com.hyphenate.easeim.section.conference.adapter;


import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseImageView;

public class InviteSelectedAdapter extends EaseBaseRecyclerViewAdapter<EaseUser> {

    @Override
    public int getEmptyLayoutId() {
        return R.layout.ease_layout_default_no_search_result;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new InviteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.invite_selected_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

    }

    private class InviteViewHolder extends ViewHolder<EaseUser> {
        private AppCompatImageView mAvatar;
        private AppCompatTextView mName;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mAvatar = findViewById(R.id.item_avatar);
            mName = findViewById(R.id.item_name);
        }

        @Override
        public void setData(EaseUser item, int position) {
            String avatarUrl = item.getAvatar();
            String nickname = item.getNickname();
            if(!TextUtils.isEmpty(avatarUrl)){
                Glide.with(mContext).load(avatarUrl).apply(RequestOptions.bitmapTransform(new CircleCrop())).error(R.drawable.ease_default_avatar).into(mAvatar);
            }
            mName.setText(nickname);
        }
    }


    @Override
    public boolean filterToCompare(String filter, EaseUser data) {
        if(data.getNickname().contains(filter)){
            return true;
        }
        return super.filterToCompare(filter, data);
    }
}
