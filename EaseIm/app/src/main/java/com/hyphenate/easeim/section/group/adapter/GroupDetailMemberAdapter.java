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
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailMemberAdapter extends RecyclerView.Adapter<GroupDetailMemberAdapter.ViewHolder> {

    private List<EaseUser> userData = new ArrayList<>();
    private GroupMemberAddClickListener memberClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_detail_member_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EaseUser user = userData.get(position);
        if(TextUtils.equals(user.getUsername(), "addUser")){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    memberClickListener.onAddClick();
                }
            });
            holder.memberAvatar.setImageDrawable(ContextCompat.getDrawable(holder.mContext, R.drawable.ease_default_avatar));
            holder.memberNick.setText("");
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
            data.add(0, new EaseUser("addUser"));
            if(data.size() > 12){
                userData = data.subList(0, 11);
            } else {
                userData = data;
            }
            notifyDataSetChanged();
        }
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
    }
}
