package com.hyphenate.easeim.section.group.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.model.SelectedUser;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.common.widget.SearchBar;
import com.hyphenate.easeim.section.group.delegate.PickContactDelegate;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.group.adapter.GroupPickContactsAdapter;
import com.hyphenate.easeim.section.group.viewmodels.GroupPickContactsViewModel;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

public class GroupPickContactsActivity extends BaseInitActivity implements EaseTitleBar.OnRightClickListener, EaseTitleBar.OnBackPressListener, PickContactDelegate.onCloseClickListener {
    private EaseTitleBar titleBar;
    private RecyclerView rvList;
    protected GroupPickContactsAdapter adapter;
    private GroupPickContactsViewModel viewModel;
    private String groupId;
    private boolean isOwner;
    private String[] newmembers;
    private AppCompatImageView userAvatar;
    private AppCompatTextView userName;
    private RelativeLayout resultView;
    private RadioGroup radioGroup;
    private String result = "";

    private SearchBar searchBar;

    private AppCompatTextView selectedTitle;
    private AppCompatTextView resultTitle;
    private List<SelectedUser> selectedList = new ArrayList<>();

    public static void actionStartForResult(Activity context, String[] newmembers, int requestCode) {
        Intent starter = new Intent(context, GroupPickContactsActivity.class);
        starter.putExtra("newmembers", newmembers);
        context.startActivityForResult(starter, requestCode);
    }

    public static void actionStartForResult(Activity context, String groupId, boolean owner, int requestCode) {
        Intent starter = new Intent(context, GroupPickContactsActivity.class);
        starter.putExtra("groupId", groupId);
        starter.putExtra("isOwner", owner);
        context.startActivityForResult(starter, requestCode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_chat_group_pick_contacts;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("groupId");
        isOwner = intent.getBooleanExtra("isOwner", false);
        newmembers = intent.getStringArrayExtra("newmembers");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        rvList = findViewById(R.id.rl_user_list);
        userAvatar = findViewById(R.id.user_avatar);
        userName = findViewById(R.id.user_name);
        resultView = findViewById(R.id.result_view);
        radioGroup = findViewById(R.id.rb_group);

        titleBar.getRightText().setTextColor(ContextCompat.getColor(mContext, R.color.em_color_brand));

        searchBar = findViewById(R.id.search_bar);
        searchBar.init(true);

        selectedTitle = findViewById(R.id.selected_title);
        resultTitle = findViewById(R.id.result_title);

        /**
         * FlexboxLayout参考：https://blog.csdn.net/weixin_39397471/article/details/90212231
         */
        FlexboxLayoutManager manager = new FlexboxLayoutManager(this);
        manager.setFlexDirection(FlexDirection.ROW);
        manager.setFlexWrap(FlexWrap.WRAP);
        manager.setAlignItems(AlignItems.CENTER);
        manager.setJustifyContent(JustifyContent.FLEX_START);
        rvList.setLayoutManager(manager);
        adapter = new GroupPickContactsAdapter();
        PickContactDelegate delegate = new PickContactDelegate();
        delegate.setCloseClickListener(this);
        adapter.addDelegate(delegate);
        rvList.setAdapter(adapter);

        refreshSelectedView();
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
        titleBar.setOnRightClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SelectedUser user = new SelectedUser(result);

                RadioButton radioButton = findViewById(checkedId);
                if(TextUtils.equals(radioButton.getText(), getString(R.string.service_personnel))){
                    user.setCustomer(false);
                } else {
                    user.setCustomer(true);
                }
                for(SelectedUser item : selectedList){
                    if(TextUtils.equals(item.getName(), user.getName())){
                        ToastUtils.showCenterToast("", getString(R.string.can_not_select_again), 0, Toast.LENGTH_SHORT);
                        return;
                    }
                }
                selectedList.add(user);
                adapter.setData(selectedList);
                refreshSelectedView();
            }
        });

        searchBar.setOnSearchBarListener(new SearchBar.OnSearchBarListener() {
            @Override
            public void onSearchContent(String text) {
                viewModel.getSearchContacts(text);
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupPickContactsViewModel.class);

        viewModel.getAddMembersObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    setResult(RESULT_OK);
                    finish();
                }
            });
        });
        viewModel.getSearchContactsObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(data.size() > 0){
                                result = data.get(0);
                                resultView.setVisibility(View.VISIBLE);
                                userName.setText(result);
                            } else {
                                resultView.setVisibility(View.GONE);
                                result = "";
                            }
                        }
                    });
                }
            });
        });
    }

    @Override
    public void onRightClick(View view) {
        ToastUtils.showCenterToast("", getString(R.string.invite_user_toast), 0, Toast.LENGTH_SHORT);
        onBackPressed();
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onMemberRemove(SelectedUser item) {
        selectedList.remove(item);
        refreshSelectedView();
        adapter.setData(selectedList);
    }

    private void refreshSelectedView(){
        if(selectedList.size() == 0){
            selectedTitle.setVisibility(View.GONE);
            rvList.setVisibility(View.GONE);
        } else {
            selectedTitle.setVisibility(View.VISIBLE);
            rvList.setVisibility(View.VISIBLE);
        }
    }

}
