package com.hyphenate.easeim.section.group.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.section.conference.adapter.ConferenceInviteAdapter;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

public class GroupAddMuteAdapter extends EaseBaseRecyclerViewAdapter<EaseUser>{
    private OnItemCheckedListener listener;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupMuteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_widget_contact_item, parent, false));
    }

    class GroupMuteViewHolder extends ViewHolder<EaseUser> {
        private EaseImageView mAvatar;
        private AppCompatImageView muteView;
        private TextView mName;
        private CheckBox checkBox;
        private Context mContext;

        public GroupMuteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mContext = itemView.getContext();
            mAvatar = findViewById(R.id.avatar);
            muteView = findViewById(R.id.icon_mute);
            mName = findViewById(R.id.name);
            checkBox = findViewById(R.id.checkbox);
        }

        @Override
        public void setData(EaseUser item, int position) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(item.isChecked());

            if(item.isMuted()){
                muteView.setVisibility(View.VISIBLE);
                checkBox.setChecked(true);
                checkBox.setEnabled(false);
            } else {
                muteView.setVisibility(View.GONE);
                checkBox.setEnabled(true);
            }

            EaseUserProfileProvider provider = EaseIM.getInstance().getUserProvider();
            if(provider != null){
                EaseUser easeUser = provider.getUser(item.getUsername());
                if(easeUser != null){
                    item.setNickname(easeUser.getNickname());
                    item.setAvatar(easeUser.getAvatar());
                }
            }
            String avatarUrl = item.getAvatar();
            String nickname = item.getNickname();
            if(!TextUtils.isEmpty(avatarUrl)){
                Glide.with(mContext).load(avatarUrl).apply(RequestOptions.bitmapTransform(new CircleCrop())).error(R.drawable.ease_default_avatar).into(mAvatar);
            }
            mName.setText(nickname);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.setChecked(isChecked);
                    if(listener != null){
                        listener.onCheckedChanged(item);
                    }
                }
            });
        }
    }

    @Override
    public boolean filterToCompare(String filter, EaseUser data) {
        if(data.getNickname().contains(filter)){
            return true;
        }
        return false;
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener){
        this.listener = listener;
    }

    public interface OnItemCheckedListener{
        void onCheckedChanged(EaseUser user);
    }
}
