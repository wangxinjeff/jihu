package com.hyphenate.easeim.section.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.widget.SearchBar;
import com.hyphenate.easeim.section.chat.adapter.PickAllUserAdapter;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.chat.adapter.PickUserAdapter;
import com.hyphenate.easeim.section.group.viewmodels.GroupMemberAuthorityViewModel;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseRecyclerView;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

public class PickAtUserActivity extends BaseInitActivity implements OnItemClickListener, EaseTitleBar.OnBackPressListener{
    private EaseTitleBar mTitleBarPick;
    private EaseRecyclerView mRvPickUserList;
    private String mGroupId;
    private GroupMemberAuthorityViewModel mViewModel;
    protected PickUserAdapter mAdapter;
    private ConcatAdapter baseAdapter;
    private PickAllUserAdapter headerAdapter;

    private SearchBar searchBar;

    public static void actionStartForResult(Fragment fragment, String groupId, int requestCode) {
        Intent starter = new Intent(fragment.getContext(), PickAtUserActivity.class);
        starter.putExtra("groupId", groupId);
        fragment.startActivityForResult(starter, requestCode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_chat_pick_at_user;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        mGroupId = getIntent().getStringExtra("groupId");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mTitleBarPick = findViewById(R.id.title_bar_pick);
        mRvPickUserList = findViewById(R.id.rv_pick_user_list);

        mRvPickUserList.setLayoutManager(new LinearLayoutManager(mContext));
        baseAdapter = new ConcatAdapter();
        mAdapter = new PickUserAdapter();
        baseAdapter.addAdapter(mAdapter);
        mRvPickUserList.setAdapter(baseAdapter);

        searchBar = findViewById(R.id.search_bar);
        searchBar.init(false);
    }

    @Override
    protected void initListener() {
        super.initListener();
        mAdapter.setOnItemClickListener(this);
        mTitleBarPick.setOnBackPressListener(this);

        searchBar.setOnSearchBarListener(new SearchBar.OnSearchBarListener() {
            @Override
            public void onSearchContent(String text) {
                mAdapter.getFilter().filter(text);
            }
        });

//
    }

    @Override
    protected void initData() {
        super.initData();
        mViewModel = new ViewModelProvider(this).get(GroupMemberAuthorityViewModel.class);
        mViewModel.getGroupMember().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    checkIfAddHeader();
                    removeSelf(data);
                    mAdapter.setData(data);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                }
            });
        });

        mViewModel.getGroupMembers(mGroupId);

        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                if(mAdapter != null){
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void removeSelf(List<EaseUser> data) {
        if(data == null || data.isEmpty()) {
            return;
        }
        Iterator<EaseUser> iterator = data.iterator();
        while (iterator.hasNext()) {
            EaseUser user = iterator.next();
            if(TextUtils.equals(user.getUsername(), EaseIMHelper.getInstance().getCurrentUser())) {
                iterator.remove();
            }
        }
    }

    private void checkIfAddHeader() {
        EMGroup group = EaseIMHelper.getInstance().getGroupManager().getGroup(mGroupId);
        if(group != null) {
            String owner = group.getOwner();
            if(TextUtils.equals(owner, EaseIMHelper.getInstance().getCurrentUser())) {
                AddHeader();
            }
        }

    }

    private void AddHeader() {
        if( headerAdapter == null) {
            headerAdapter = new PickAllUserAdapter();
            EaseUser user = new EaseUser(getString(R.string.all_members));
            user.setAvatar(R.drawable.ease_group_icon+"");
            List<EaseUser> users = new ArrayList<>();
            users.add(user);
            headerAdapter.setData(users);
        }
        if(!baseAdapter.getAdapters().contains(headerAdapter)) {
            baseAdapter.addAdapter(0, headerAdapter);

            headerAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    setResult(RESULT_OK, new Intent().putExtra("username", headerAdapter.getItem(position).getUsername()));
                    finish();
                }
            });
        }

    }

    @Override
    public void onItemClick(View view, int position) {
        EaseUser user = mAdapter.getData().get(position);
        if(TextUtils.equals(user.getUsername(), EaseIMHelper.getInstance().getCurrentUser())) {
            return;
        }
        Intent intent = getIntent();
        intent.putExtra("username", user.getUsername());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void moveToRecyclerItem(String pointer) {
        List<EaseUser> data = mAdapter.getData();
        if(data == null || data.isEmpty()) {
            return;
        }
        for(int i = 0; i < data.size(); i++) {
            if(TextUtils.equals(EaseCommonUtils.getLetter(data.get(i).getNickname()), pointer)) {
                LinearLayoutManager manager = (LinearLayoutManager) mRvPickUserList.getLayoutManager();
                if(manager != null) {
                    manager.scrollToPositionWithOffset(i, 0);
                }
            }
        }
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }
}
