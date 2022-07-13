package com.hyphenate.easeim.section.conference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easecallkit.EaseCallKit;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.widget.SearchBar;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.conference.adapter.ConferenceInviteAdapter;
import com.hyphenate.easeim.section.conference.adapter.InviteSelectedAdapter;
import com.hyphenate.easeim.section.group.viewmodels.GroupDetailViewModel;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.List;

public class ConferenceInviteActivity extends BaseInitActivity implements View.OnClickListener, EaseTitleBar.OnBackPressListener, ConferenceInviteAdapter.OnItemCheckedListener {
    private static final String TAG = "ConferenceInvite";

    private EaseTitleBar mTitleBar;
    private RecyclerView selectedView;
    private InviteSelectedAdapter selectedAdapter;
    private SearchBar searchBar;
    private RecyclerView memberView;
    private static String groupId;
    private String[] exist_member;
    private GroupDetailViewModel viewModel;
    private ConferenceInviteAdapter inviteAdapter;
    private List<EaseUser> memberList;
    private List<EaseUser> selectedList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_conference_invite;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        String group = intent.getStringExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_ID);
        if(group != null){
            groupId = group;
            exist_member = intent.getStringArrayExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_EXIST_MEMBERS);
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mTitleBar = findViewById(R.id.title_bar);
        selectedView = findViewById(R.id.selected_view);
        selectedView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        selectedAdapter = new InviteSelectedAdapter();
        selectedView.setAdapter(selectedAdapter);

        searchBar = findViewById(R.id.search_bar);
        searchBar.init(false);

        memberView = findViewById(R.id.member_list);
        memberView.setLayoutManager(new LinearLayoutManager(this));
        inviteAdapter = new ConferenceInviteAdapter();
        inviteAdapter.setOnItemCheckedListener(this);
        memberView.setAdapter(inviteAdapter);
    }

    @Override
    protected void initListener() {
        super.initListener();

        mTitleBar.setOnBackPressListener(this);
        searchBar.setOnSearchBarListener(new SearchBar.OnSearchBarListener() {
            @Override
            public void onSearchContent(String text) {
                inviteAdapter.getFilter().filter(text);
            }
        });
        mTitleBar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getGroupMember().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            for(EaseUser user : data){
                                if(TextUtils.equals(user.getUsername(), EMClient.getInstance().getCurrentUser())){
                                    data.remove(user);
                                    break;
                                }
                            }
                            memberList = data;
                            inviteAdapter.setData(memberList);
                        }
                    });
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();

                }
            });
        });

        viewModel.getGroupAllMember(groupId);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
        EaseCallKit.getInstance().startInviteMultipleCall(null,null);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EaseCallKit.getInstance().startInviteMultipleCall(null,null);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onCheckedChanged(EaseUser user) {
        if(selectedList == null){
            selectedList = new ArrayList<>();
        }
        for(EaseUser item : memberList){
            if(TextUtils.equals(item.getUsername(), user.getUsername())){
                item.setChecked(user.isChecked());
                if(item.isChecked()){
                    selectedList.add(item);
                } else {
                    selectedList.remove(item);
                }
            }
        }
        if(selectedList.size() > 0){
            selectedView.setVisibility(View.VISIBLE);
            selectedAdapter.setData(selectedList);
            mTitleBar.setRightLayoutVisibility(View.VISIBLE);
        } else {
            selectedView.setVisibility(View.GONE);
            mTitleBar.setRightLayoutVisibility(View.GONE);
        }

    }
}
