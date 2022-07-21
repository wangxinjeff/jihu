package com.hyphenate.easeim.section.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.search.adapter.SectionPagerAdapter;
import com.hyphenate.easeim.section.search.fragment.SearchAllFragment;
import com.hyphenate.easeim.section.search.fragment.SearchFileFragment;
import com.hyphenate.easeim.section.search.fragment.SearchMultiMediaFragment;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryChatActivity extends BaseInitActivity {
    private String toUsername;
    private EaseTitleBar titleBar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private List<Fragment> fragmentList;
    private EMConversation conversation;
    private int chatType;

    public static void actionStart(Context context, String toUsername, int chatType) {
        Intent intent = new Intent(context, SearchHistoryChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, toUsername);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search_history;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        toUsername = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID);
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
        conversation = EMClient.getInstance().chatManager().getConversation(toUsername);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        if(EaseIMHelper.getInstance().isAdmin()){
            titleBar.setLeftImageResource(R.drawable.icon_back_admin);
        }

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        fragmentList = new ArrayList<>();
        fragmentList.add(new SearchAllFragment(toUsername, chatType));
        if(conversation.getType() == EMConversation.EMConversationType.GroupChat){
            fragmentList.add(new SearchFileFragment(toUsername));
        }
        fragmentList.add(new SearchMultiMediaFragment(toUsername));

        List<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.em_search_message));
        if(conversation.getType() == EMConversation.EMConversationType.GroupChat) {
            titleList.add(getString(R.string.em_search_file));
        }
        titleList.add(getString(R.string.em_image_and_video));

        viewPager.setAdapter(new SectionPagerAdapter(this, fragmentList));
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy(){
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titleList.get(position));
            }
        }).attach();

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
    protected void initData() {
        super.initData();
    }
}
