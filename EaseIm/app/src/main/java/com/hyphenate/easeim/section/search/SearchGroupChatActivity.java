package com.hyphenate.easeim.section.search;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.model.SearchResult;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.chat.activity.ChatActivity;
import com.hyphenate.easeim.section.search.adapter.SearchGroupChatAdapter;
import com.hyphenate.easeui.constants.EaseConstant;

import java.util.ArrayList;
import java.util.List;

public class SearchGroupChatActivity extends BaseInitActivity implements View.OnClickListener{

    private AppCompatImageView backIcon;
    private AppCompatTextView searchName;
    private AppCompatEditText searchContent;
    private AppCompatTextView searchStart;

    private View popView;
    private AppCompatTextView item1;
    private AppCompatTextView item2;
    private AppCompatTextView item3;
    private AppCompatTextView item4;
    private PopupWindow popupWindow;

    private RecyclerView recyclerView;
    private SearchGroupChatAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search_group_chat;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        backIcon = findViewById(R.id.icon_back);
        searchName = findViewById(R.id.search_name);
        searchContent = findViewById(R.id.search_content);
        searchStart = findViewById(R.id.search_start);

        popView = LayoutInflater.from(this).inflate(R.layout.pop_search_group_chat, null, false);
        item1 = popView.findViewById(R.id.item1);
        item2 = popView.findViewById(R.id.item2);
        item3 = popView.findViewById(R.id.item3);
        item4 = popView.findViewById(R.id.item4);
        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        // 这里单独写一篇文章来分析
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        recyclerView = findViewById(R.id.result_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchGroupChatAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initListener() {
        super.initListener();
        backIcon.setOnClickListener(this);
        searchName.setOnClickListener(this);
        searchStart.setOnClickListener(this);
        searchContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() &&
                                KeyEvent.ACTION_DOWN == event.getAction()){
                    searchGroupChat(searchName.getText().toString(), searchContent.getText().toString());
                    return true;
                } else {
                    return false;
                }
            }
        });

        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        item3.setOnClickListener(this);
        item4.setOnClickListener(this);

        adapter.setItemClickListener(new SearchGroupChatAdapter.onItemClickListener() {
            @Override
            public void onClick(String groupId) {
                ChatActivity.actionStart(SearchGroupChatActivity.this, groupId, EaseConstant.CHATTYPE_GROUP);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.icon_back:
                onBackPressed();
                break;
            case R.id.search_name:
                popupWindow.showAsDropDown(searchName);
                break;
            case R.id.search_start:
                searchGroupChat(searchName.getText().toString(), searchContent.getText().toString());
                break;
            case R.id.item1:
                searchName.setText(item1.getText().toString());
                popupWindow.dismiss();
                break;
            case R.id.item2:
                searchName.setText(item2.getText().toString());
                popupWindow.dismiss();
                break;
            case R.id.item3:
                searchName.setText(item3.getText().toString());
                popupWindow.dismiss();
                break;
            case R.id.item4:
                searchName.setText(item4.getText().toString());
                popupWindow.dismiss();
                break;
        }
    }

    private void searchGroupChat(String name, String content){
        List<SearchResult> result = new ArrayList<>();
        SearchResult searchResult = new SearchResult();
        searchResult.setAvatar("https://img2.baidu.com/it/u=4244269751,4000533845&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        searchResult.setId("186245684723713");
        searchResult.setName("专属群");
        result.add(searchResult);

        SearchResult searchResult1 = new SearchResult();
        searchResult1.setId("19023173891712");
        searchResult1.setName("测试");
        result.add(searchResult1);

        adapter.setData(result);
    }
}
