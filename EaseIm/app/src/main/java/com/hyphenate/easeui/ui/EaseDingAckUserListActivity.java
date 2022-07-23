package com.hyphenate.easeui.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.group.viewmodels.GroupDetailViewModel;
import com.hyphenate.easeim.section.search.adapter.SectionPagerAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseDingMessageHelper;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangsong on 18-1-23.
 */

public class EaseDingAckUserListActivity extends BaseInitActivity {
    private EaseTitleBar titleBar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private List<Fragment> fragmentList;
    private EMMessage message;
    private List<String> memberList = new ArrayList<>();
    private List<String> readList = new ArrayList<>();
    private List<String> unReadList = new ArrayList<>();
    private GroupReadAckListFragment readFragment;
    private GroupReadAckListFragment unReadFragment;
    private GroupDetailViewModel viewModel;

    public static void startAction(Context context, EMMessage message){
        Intent intent = new Intent(context, EaseDingAckUserListActivity.class);
        intent.putExtra("message", message);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ease_activity_ding_ack_user_list;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        message = intent.getParcelableExtra("message");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        EaseDingMessageHelper.get().setUserUpdateListener(message, userUpdateListener);
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel =  new ViewModelProvider(this).get(GroupDetailViewModel.class);
        EaseDingMessageHelper.get().fetchGroupReadAck(message);
        showLoading();
        viewModel.getGroupMember().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    memberList.clear();
                    unReadList.clear();
                    for(EaseUser user : data){
                        if(!TextUtils.equals(user.getUsername(), EaseIMHelper.getInstance().getCurrentUser())){
                            memberList.add(user.getUsername());
                        }
                    }
                    for(String s : memberList){
                        if(!readList.contains(s)){
                            unReadList.add(s);
                        }
                    }
                    runOnUiThread(() -> refreshView());
                }
            });
        });
    }

    private void refreshView(){
        fragmentList = new ArrayList<>();
        readFragment = new GroupReadAckListFragment(readList);
        unReadFragment = new GroupReadAckListFragment(unReadList);
        fragmentList.add(readFragment);
        fragmentList.add(unReadFragment);

        viewPager.setAdapter(new SectionPagerAdapter(this, fragmentList));

        List<String> titleList = new ArrayList<>();
        titleList.add(readList.size() + "人已读");
        titleList.add(unReadList.size() + "人未读");
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy(){
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titleList.get(position));
            }
        }).attach();
        dismissLoading();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        EaseDingMessageHelper.get().setUserUpdateListener(message, userUpdateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EaseDingMessageHelper.get().setUserUpdateListener(message, null);
    }

    private EaseDingMessageHelper.IAckUserUpdateListener userUpdateListener =
            new EaseDingMessageHelper.IAckUserUpdateListener() {
                @Override
                public void onUpdate(List<String> list) {
                    readList = list;
                    runOnUiThread(() -> viewModel.getGroupAllMember(message.getTo()));
                }
            };

}
