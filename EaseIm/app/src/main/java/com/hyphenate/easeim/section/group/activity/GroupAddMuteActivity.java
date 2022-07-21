package com.hyphenate.easeim.section.group.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.common.widget.SearchBar;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.group.adapter.GroupAddMuteAdapter;
import com.hyphenate.easeim.section.group.viewmodels.GroupMemberAuthorityViewModel;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.List;

public class GroupAddMuteActivity extends BaseInitActivity {

    private EaseTitleBar titleBar;
    private SearchBar searchBar;
    private RecyclerView memberList;
    private GroupMemberAuthorityViewModel viewModel;
    private List<String> mutedList;
    private String groupId;
    private GroupAddMuteAdapter addMuteAdapter;
    private List<String> selectedList;

    public static void startAction(Context context, String groupId, ArrayList<String> mutedList){
        Intent intent = new Intent(context, GroupAddMuteActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putStringArrayListExtra("mutedList", mutedList);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.em_activity_group_add_mute;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("groupId");
        mutedList = intent.getStringArrayListExtra("mutedList");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        searchBar = findViewById(R.id.search_bar);
        searchBar.init(false);
        memberList = findViewById(R.id.member_list);
        memberList.setLayoutManager(new LinearLayoutManager(this));
        titleBar.getRightText().setTextColor(ContextCompat.getColor(this, R.color.search_close));

        addMuteAdapter = new GroupAddMuteAdapter();
        memberList.setAdapter(addMuteAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupMemberAuthorityViewModel.class);
        viewModel.getGroupMember().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    for(EaseUser user : data){
                        if(user.isOwner()){
                            data.remove(user);
                            break;
                        }
                    }
                    for(String id : mutedList){
                        for(EaseUser item : data){
                            if(TextUtils.equals(id, item.getUsername())){
                                item.setMuted(true);
                            }
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addMuteAdapter.setData(data);
                        }
                    });
                }

                @Override
                public void onLoading(@Nullable List<EaseUser> data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    dismissLoading();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);

                }
            });
        });

        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    onBackPressed();
                }

                @Override
                public void onLoading(@Nullable String data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    dismissLoading();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    ToastUtils.showCenterToast("", getString(R.string.em_group_mute_failed), 0 ,Toast.LENGTH_SHORT);
                }
            });
        });

        viewModel.getGroupMembers(groupId);
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
        titleBar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.muteGroupMembers(groupId, selectedList, -1);
            }
        });
        searchBar.setOnSearchBarListener(new SearchBar.OnSearchBarListener() {
            @Override
            public void onSearchContent(String text) {
                addMuteAdapter.getFilter().filter(text);
            }
        });

        addMuteAdapter.setOnItemCheckedListener(new GroupAddMuteAdapter.OnItemCheckedListener(){
            @Override
            public void onCheckedChanged(EaseUser user) {
                if(selectedList == null){
                    selectedList = new ArrayList<>();
                }
                if(user.isChecked()){
                    selectedList.add(user.getUsername());
                } else {
                    selectedList.remove(user.getUsername());
                }
            }
        });
    }
}
