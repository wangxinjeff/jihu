package com.hyphenate.easeim.section.group.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.group.adapter.GroupMuteAdapter;
import com.hyphenate.easeim.section.group.viewmodels.GroupMemberAuthorityViewModel;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupMuteActivity extends BaseInitActivity {
    private String groupId;
    private EaseTitleBar titleBar;
    private LinearLayout addMute;
    private RecyclerView recyclerView;
    protected GroupMemberAuthorityViewModel viewModel;
    private GroupMuteAdapter adapter;
    private List<String> muteList;


    public static void startAction(Context context, String groupId){
        Intent intent = new Intent(context, GroupMuteActivity.class);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.em_activity_group_mute;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("groupId");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        addMute = findViewById(R.id.add_mute_text);
        recyclerView = findViewById(R.id.mute_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupMuteAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        super.initData();
        muteList = new ArrayList<>();
        viewModel = new ViewModelProvider(this).get(GroupMemberAuthorityViewModel.class);
        viewModel.getMuteMembersObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Map<String, Long>>() {
                @Override
                public void onSuccess(Map<String, Long> data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            muteList.clear();
                            muteList.addAll(data.keySet());
                            adapter.setData(muteList);
                        }
                    });
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    dismissLoading();
                }

                @Override
                public void onLoading(@Nullable Map<String, Long> data) {
                    super.onLoading(data);
                    showLoading();
                }
            });
        });

        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    viewModel.getMuteMembers(groupId);
                }
            });
        });

        viewModel.getMuteMembers(groupId);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        adapter.setOnUnMuteClickListener(new GroupMuteAdapter.OnUnMuteClickListener() {
            @Override
            public void onUnMute(String username) {
                List<String> unMuteList = new ArrayList<>();
                unMuteList.add(username);
                viewModel.unMuteGroupMembers(groupId, unMuteList);
            }
        });
        addMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupAddMuteActivity.startAction(mContext, groupId, (ArrayList<String>)muteList);
                finish();
            }
        });
    }
}
