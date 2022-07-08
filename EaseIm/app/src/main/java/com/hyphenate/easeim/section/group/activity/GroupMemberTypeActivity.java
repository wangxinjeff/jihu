package com.hyphenate.easeim.section.group.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.group.adapter.GroupMemberListAdapter;
import com.hyphenate.easeim.section.group.delegate.GroupMemberDelegate;
import com.hyphenate.easeim.section.group.viewmodels.GroupMemberAuthorityViewModel;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseRecyclerView;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.List;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

public class GroupMemberTypeActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener {
    private EaseTitleBar titleBar;
    private String groupId;
    private EMGroup group;
    private boolean isOwner;
    private AppCompatEditText searchView;
    private AppCompatImageView searchEmpty;
    private AppCompatTextView searchClose;
    private AppCompatTextView searchTextView;
    private LinearLayout searchIconView;
    private ConcatAdapter adapter;
    private GroupMemberListAdapter listAdapter;
    private EaseRecyclerView memberListView;

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_chat_group_member_type;
    }

    public static void actionStart(Context context, String groupId, boolean owner) {
        Intent starter = new Intent(context, GroupMemberTypeActivity.class);
        starter.putExtra("groupId", groupId);
        starter.putExtra("isOwner", owner);
        context.startActivity(starter);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("groupId");
        isOwner = intent.getBooleanExtra("isOwner", false);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);

        titleBar = findViewById(R.id.title_bar);
        searchView = findViewById(R.id.search_et_view);
        searchEmpty = findViewById(R.id.search_empty);
        searchClose = findViewById(R.id.search_close);
        searchTextView = findViewById(R.id.search_tv_view);
        searchIconView = findViewById(R.id.search_icon_view);
        memberListView = findViewById(R.id.rv_member_list);
        memberListView.setLayoutManager(new LinearLayoutManager(this));

        ConcatAdapter.Config config = new ConcatAdapter.Config.Builder()
                .setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
                .build();
        adapter = new ConcatAdapter(config);

        listAdapter = new GroupMemberListAdapter();
        listAdapter.setHasStableIds(true);
        adapter.addAdapter(listAdapter);
        listAdapter.addDelegate(new GroupMemberDelegate());
        memberListView.setAdapter(adapter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
        searchEmpty.setOnClickListener(this);
        searchClose.setOnClickListener(this);
        searchTextView.setOnClickListener(this);
        searchIconView.setOnClickListener(this);
        titleBar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        GroupMemberAuthorityViewModel viewModel = new ViewModelProvider(this).get(GroupMemberAuthorityViewModel.class);

        viewModel.getGroupMember().observe(this, response -> {

            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.setData(data);
                        }
                    });
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();

                }
            });
        });

        viewModel.getGroupMembers(groupId);

        viewModel.getMessageChangeObservable().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event.isGroupChange()) {
                viewModel.getGroupMembers(groupId);
            }else if(event.isGroupLeave() && TextUtils.equals(groupId, event.message)) {
                finish();
            }
        });
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_tv_view :
            case R.id.search_icon_view :
                searchTextView.setVisibility(View.GONE);
                searchIconView.setVisibility(View.GONE);
                EaseCommonUtils.showSoftKeyBoard(searchView);
                break;
            case R.id.search_empty:
                searchView.setText("");
                break;
            case R.id.search_close:
                searchView.setText("");
                searchTextView.setVisibility(View.VISIBLE);
                searchIconView.setVisibility(View.VISIBLE);
                EaseCommonUtils.hideSoftKeyBoard(searchView);
                break;
        }
    }
}
