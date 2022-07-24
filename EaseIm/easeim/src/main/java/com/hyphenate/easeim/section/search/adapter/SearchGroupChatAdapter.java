package com.hyphenate.easeim.section.search.adapter;

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
import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.model.SearchResult;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;

public class SearchGroupChatAdapter extends EaseBaseRecyclerViewAdapter<SearchResult> {

    private onItemClickListener listener;

    public void setItemClickListener(onItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public int getEmptyLayoutId() {
        return EaseIMHelper.getInstance().isAdmin() ? R.layout.ease_layout_default_no_search_result_admin : R.layout.ease_layout_default_no_search_result;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_group_chat_item, parent, false));
    }

    private class GroupChatViewHolder extends ViewHolder<SearchResult>{
        private AppCompatImageView avatar;
        private AppCompatTextView groupName;
        private AppCompatTextView joinText;
        private AppCompatImageView next;
        private Context context;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            avatar = itemView.findViewById(R.id.group_avatar);
            groupName = itemView.findViewById(R.id.group_name);
            joinText = itemView.findViewById(R.id.join_group_text);
            next = itemView.findViewById(R.id.icon_next);
            context = itemView.getContext();
        }

        @Override
        public void setData(SearchResult item, int position) {
            String avatarUrl = item.getAvatar();
            String groupId = item.getId();
            String name = item.getName();
            if(!TextUtils.isEmpty(avatarUrl)){
                Glide.with(context).load(avatarUrl).apply(RequestOptions.bitmapTransform(new CircleCrop())).error(R.drawable.ease_group_icon).into(avatar);
            }

            EMGroup group = EaseIMHelper.getInstance().getGroupManager().getGroup(groupId);
            if(group != null){
                next.setVisibility(View.VISIBLE);
                joinText.setVisibility(View.GONE);
            } else {
                next.setVisibility(View.GONE);
                joinText.setVisibility(View.VISIBLE);
            }
            groupName.setText(groupId);
            if(!TextUtils.isEmpty(name)){
                groupName.setText(name);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        if(next.getVisibility() == View.VISIBLE){
                            listener.onClick(groupId);
                        }
                    }
                }
            });
        }
    }

    public interface onItemClickListener{
        void onClick(String groupId);
    }
}
