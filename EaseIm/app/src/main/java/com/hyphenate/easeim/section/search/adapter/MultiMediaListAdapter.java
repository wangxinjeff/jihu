package com.hyphenate.easeim.section.search.adapter;


import android.content.Context;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseDateUtils;
import com.hyphenate.easeui.utils.EaseEditTextUtils;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.util.EMLog;

import java.util.Date;

public class MultiMediaListAdapter extends EaseBaseRecyclerViewAdapter<EMMessage> {
    @Override
    public int getEmptyLayoutId() {
        return R.layout.empty_layout;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new SearchAllViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.multimedia_item, parent, false));
    }

    private class SearchAllViewHolder extends ViewHolder<EMMessage>{
        private AppCompatImageView image;
        private Context context;

        public SearchAllViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
        }

        @Override
        public void initView(View itemView) {
            image = findViewById(R.id.image);
        }

        @Override
        public void setData(EMMessage item, int position) {
            if(item.getType() == EMMessage.Type.IMAGE){
                EMImageMessageBody body = (EMImageMessageBody) item.getBody();
                Uri imageUri = body.getLocalUri();
                // 获取Uri的读权限
                EaseFileUtils.takePersistableUriPermission(context, imageUri);
                if(!EaseFileUtils.isFileExistByUri(context, imageUri)) {
                    imageUri = ((EMImageMessageBody) body).thumbnailLocalUri();
                    EaseFileUtils.takePersistableUriPermission(context, imageUri);
                    if(!EaseFileUtils.isFileExistByUri(context, imageUri)) {
                        imageUri = null;
                    }
                }
                //获取图片服务器地址
                String thumbnailUrl = null;
                thumbnailUrl = ((EMImageMessageBody) body).getThumbnailUrl();
                if(TextUtils.isEmpty(thumbnailUrl)) {
                    thumbnailUrl = ((EMImageMessageBody) body).getRemoteUrl();
                }
                Glide.with(context)
                        .load(imageUri == null ? thumbnailUrl : imageUri)
                        .error(R.drawable.ease_default_image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(image);
            } else if(item.getType() == EMMessage.Type.VIDEO){
                EMVideoMessageBody body = (EMVideoMessageBody) item.getBody();
                //获取视频封面本地资源路径
                Uri localThumbUri = ((EMVideoMessageBody) body).getLocalThumbUri();
                //检查Uri读权限
                EaseFileUtils.takePersistableUriPermission(context, localThumbUri);
                //获取视频封面服务器地址
                String thumbnailUrl = ((EMVideoMessageBody) body).getThumbnailUrl();
                if(!EaseFileUtils.isFileExistByUri(context, localThumbUri)) {
                    localThumbUri = null;
                }
                Glide.with(context).load(localThumbUri == null ? thumbnailUrl : localThumbUri).error(R.drawable.ease_default_image).diskCacheStrategy(DiskCacheStrategy.ALL).into(image);
            }
        }
    }
}
