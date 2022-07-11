package com.hyphenate.easeim.section.group.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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

public class GroupPickContactsActivity extends BaseInitActivity implements EaseTitleBar.OnRightClickListener, EaseTitleBar.OnBackPressListener, View.OnClickListener, PickContactDelegate.onCloseClickListener {
    private EaseTitleBar titleBar;
    private RecyclerView rvList;
    protected GroupPickContactsAdapter adapter;
    private GroupPickContactsViewModel viewModel;
    private String groupId;
    private boolean isOwner;
    private String[] newmembers;
    private FrameLayout searchBar;
    private AppCompatImageView userAvatar;
    private AppCompatTextView userName;
    private RelativeLayout resultView;
    private RadioGroup radioGroup;
    private String result = "";

    private AppCompatEditText searchView;
    private AppCompatImageView searchEmpty;
    private AppCompatTextView searchClose;
    private AppCompatTextView searchStart;
    private AppCompatTextView searchTextView;
    private LinearLayout searchIconView;

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
        searchBar = findViewById(R.id.search_bar);
        rvList = findViewById(R.id.rl_user_list);
        userAvatar = findViewById(R.id.user_avatar);
        userName = findViewById(R.id.user_name);
        resultView = findViewById(R.id.result_view);
        radioGroup = findViewById(R.id.rb_group);

        titleBar.getRightText().setTextColor(ContextCompat.getColor(mContext, R.color.em_color_brand));

        searchView = searchBar.findViewById(R.id.search_et_view);
        searchEmpty = searchBar.findViewById(R.id.search_empty);
        searchClose = searchBar.findViewById(R.id.search_close);
        searchStart = searchBar.findViewById(R.id.search_start);
        searchTextView = searchBar.findViewById(R.id.search_tv_view);
        searchIconView = searchBar.findViewById(R.id.search_icon_view);

        selectedTitle = findViewById(R.id.selected_title);
        resultTitle = findViewById(R.id.result_title);

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

        searchEmpty.setOnClickListener(this);
        searchClose.setOnClickListener(this);
        searchStart.setOnClickListener(this);
        searchTextView.setOnClickListener(this);
        searchIconView.setOnClickListener(this);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    searchClose.setVisibility(View.VISIBLE);
                    searchStart.setVisibility(View.GONE);
                } else {
                    searchClose.setVisibility(View.GONE);
                    searchStart.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
            case R.id.search_start:
                viewModel.getSearchContacts(searchView.getText().toString());
                break;
            case R.id.search_close:
                searchView.setText("");
                searchTextView.setVisibility(View.VISIBLE);
                searchIconView.setVisibility(View.VISIBLE);
                EaseCommonUtils.hideSoftKeyBoard(searchView);
                searchClose.setVisibility(View.VISIBLE);
                searchStart.setVisibility(View.GONE);
                break;
        }
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
