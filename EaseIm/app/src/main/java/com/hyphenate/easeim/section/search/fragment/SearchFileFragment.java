package com.hyphenate.easeim.section.search.fragment;

import android.os.Bundle;
import android.os.Message;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.widget.SearchBar;
import com.hyphenate.easeim.section.base.BaseInitFragment;
import com.hyphenate.easeim.section.chat.activity.FileDetailsActivity;
import com.hyphenate.easeim.section.search.adapter.SearchAllAdapter;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

public class SearchFileFragment extends BaseInitFragment {
    private SearchBar searchBar;
    private RecyclerView recyclerView;
    private SearchAllAdapter allAdapter;
    private EMConversation conversation;
    private String conversationId;

    public SearchFileFragment(String conversationId) {
        this.conversationId = conversationId;
        conversation = EMClient.getInstance().chatManager().getConversation(conversationId);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_file;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        searchBar = findViewById(R.id.search_bar);
        searchBar.init(true);
        recyclerView = findViewById(R.id.rl_msg_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allAdapter = new SearchAllAdapter();
        recyclerView.setAdapter(allAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        if(conversation != null){
            List<EMMessage> mData = conversation.searchMsgFromDB(EMMessage.Type.FILE, System.currentTimeMillis(), 100, null, EMConversation.EMSearchDirection.UP);
            allAdapter.setKeyword("");
            allAdapter.setData(mData);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        searchBar.setOnSearchBarListener(new SearchBar.OnSearchBarListener() {
            @Override
            public void onSearchContent(String text) {
                allAdapter.setKeyword(text);
                allAdapter.getFilter().filter(text);
            }
        });

        allAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FileDetailsActivity.actionStart(getContext(), allAdapter.getData().get(position));
            }
        });
    }
}
