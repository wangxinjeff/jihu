package com.hyphenate.easeim.section.group.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailMemberAdapter extends RecyclerView.Adapter<GroupDetailMemberAdapter.ViewHolder> {

    private List<EaseUser> userData = new ArrayList<>();
    private GroupMemberAddClickListener memberClickListener;
    private boolean isShowAll = false;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_detail_member_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EaseUser user = userData.get(position);
        if(TextUtils.equals(user.getUsername(), "em_editUser") && position == 0){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    memberClickListener.onAddClick();
                }
            });
//            holder.memberAvatar.setImageDrawable(ContextCompat.getDrawable(holder.mContext, R.drawable.icon_group_edit));
            Glide.with(holder.mContext).load(R.drawable.icon_group_edit).into(holder.memberAvatar);
            holder.memberNick.setText(user.getNickname());
        } else if(TextUtils.equals(user.getUsername(), "em_addUser") && position == 0){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    memberClickListener.onAddClick();
                }
            });
            if(EaseIMHelper.getInstance().isAdmin()){
//                holder.memberAvatar.setImageDrawable(ContextCompat.getDrawable(holder.mContext, R.drawable.icon_invite_admin));
                Glide.with(holder.mContext).load(R.drawable.icon_invite_admin).into(holder.memberAvatar);
            } else {
//                holder.memberAvatar.setImageDrawable(ContextCompat.getDrawable(holder.mContext, R.drawable.icon_group_invite));
                Glide.with(holder.mContext).load(R.drawable.icon_group_invite).into(holder.memberAvatar);
            }
            holder.memberNick.setText(user.getNickname());
        } else if(TextUtils.equals(user.getUsername(), "em_removeUser") && position == 1){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    memberClickListener.onRemoveClick();
                }
            });
            holder.memberAvatar.setImageDrawable(ContextCompat.getDrawable(holder.mContext, R.drawable.icon_remove_admin));
            holder.memberNick.setText(user.getNickname());
        } else {
            EaseUserProfileProvider provider = EaseIM.getInstance().getUserProvider();
            if(provider != null){
                EaseUser easeUser = provider.getUser(user.getUsername());
                if(easeUser != null){
                    user.setAvatar(easeUser.getAvatar());
                    user.setNickname(easeUser.getNickname());
                }
            }
            Glide.with(holder.mContext).load(user.getAvatar()).apply(RequestOptions.bitmapTransform(new CircleCrop())).error(R.drawable.ease_default_avatar).into(holder.memberAvatar);
            holder.memberNick.setText(user.getNickname());
        }
    }

    @Override
    public int getItemCount() {
        return userData.size();
    }

    public void setData(List<EaseUser> data){
        if(data != null){
            if(isShowAll){
                userData = data;
            } else {
                if(data.size() > 12){
                    userData = data.subList(0, 11);
                } else {
                    userData = data;
                }
            }

            notifyDataSetChanged();
        }
    }

    public void setShowAll(boolean isShowAll){
        this.isShowAll = isShowAll;
    }

    public void setOnAddClickListener(GroupMemberAddClickListener listener){
        memberClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        AppCompatImageView memberAvatar;
        AppCompatTextView memberNick;
        Context mContext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            memberAvatar = itemView.findViewById(R.id.member_avatar);
            memberNick = itemView.findViewById(R.id.member_nick);
        }
    }

    public interface GroupMemberAddClickListener{
        void onAddClick();
        void onRemoveClick();
    }
}
