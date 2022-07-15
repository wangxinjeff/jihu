package com.hyphenate.easeim.section.search.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.widget.SearchBar;
import com.hyphenate.easeim.section.base.BaseInitFragment;
import com.hyphenate.easeim.section.search.ShowChatHistoryActivity;
import com.hyphenate.easeim.section.search.adapter.SearchAllAdapter;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.interfaces.OnItemClickListener;

import java.util.List;

public class SearchAllFragment extends BaseInitFragment {

    private SearchBar searchBar;
    private RecyclerView recyclerView;
    private SearchAllAdapter allAdapter;
    private EMConversation conversation;
    private String conversationId;
    private int chatType;

    public SearchAllFragment(String conversationId, int chatType) {
        this.conversationId = conversationId;
        this.chatType = chatType;
        conversation = EMClient.getInstance().chatManager().getConversation(conversationId);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_all;
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
    }

    @Override
    protected void initListener() {
        super.initListener();
        searchBar.setOnSearchBarListener(new SearchBar.OnSearchBarListener() {
            @Override
            public void onSearchContent(String text) {
                searchMessages(text);
            }
        });

        allAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ShowChatHistoryActivity.actionStart(getContext(), conversationId, chatType, allAdapter.getData().get(position).getMsgId());
            }
        });
    }

    private void searchMessages(String key){
        if(conversation != null){
            List<EMMessage> mData = conversation.searchMsgFromDB(key, System.currentTimeMillis(), 100, null, EMConversation.EMSearchDirection.UP);
            if(mData.size() > 0){
                allAdapter.setKeyword(key);
                allAdapter.setData(mData);
            }
        }
    }
}
