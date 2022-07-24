package com.hyphenate.easeim.section.search.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.section.base.BaseInitFragment;
import com.hyphenate.easeim.section.search.adapter.MultiMediaListAdapter;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import com.hyphenate.easeui.ui.EaseShowVideoActivity;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

public class SearchMultiMediaFragment extends BaseInitFragment {

    private RecyclerView mediaList;
    private MultiMediaListAdapter adapter;
    private EMConversation conversation;
    private String conversationId;

    public SearchMultiMediaFragment(String conversationId) {
        this.conversationId = conversationId;
        conversation = EMClient.getInstance().chatManager().getConversation(conversationId);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_multimedia;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mediaList = findViewById(R.id.rl_media_list);
        mediaList.setLayoutManager(new GridLayoutManager(getContext(), 4));

        adapter = new MultiMediaListAdapter();
        mediaList.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        super.initData();
        refreshData();
    }

    @Override
    protected void initListener() {
        super.initListener();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EMMessage message = adapter.getData().get(position);
                if(message.getType() == EMMessage.Type.IMAGE){
                    EMImageMessageBody imgBody = (EMImageMessageBody) message.getBody();
                    Intent intent = new Intent(getContext(), EaseShowBigImageActivity.class);
                    Uri imgUri = imgBody.getLocalUri();
                    //检查Uri读权限
                    EaseFileUtils.takePersistableUriPermission(getContext(), imgUri);
                    EMLog.e("Tag", "big image uri: " + imgUri + "  exist: "+EaseFileUtils.isFileExistByUri(getContext(), imgUri));
                    if(EaseFileUtils.isFileExistByUri(getContext(), imgUri)) {
                        intent.putExtra("uri", imgUri);
                    } else{
                        // The local full size pic does not exist yet.
                        // ShowBigImage needs to download it from the server
                        // first
                        String msgId = message.getMsgId();
                        intent.putExtra("messageId", msgId);
                        intent.putExtra("filename", imgBody.getFileName());
                    }
                    getContext().startActivity(intent);
                } else if(message.getType() == EMMessage.Type.VIDEO){
                    Intent intent = new Intent(getContext(), EaseShowVideoActivity.class);
                    intent.putExtra("msg", message);
                    getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData(){
        if(conversation != null){
            List<EMMessage> list =  conversation.searchMsgFromDB(System.currentTimeMillis(), 1000, EMConversation.EMSearchDirection.UP);
            List<EMMessage> data = new ArrayList<>();
            for(EMMessage message : list){
                if(message.getType() == EMMessage.Type.IMAGE || message.getType() == EMMessage.Type.VIDEO){
                    data.add(message);
                }
            }
            adapter.setData(data);
        }
    }
}
