package com.hyphenate.easeim.section.group.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hyphenate.chat.EMGroup;

import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.widget.ArrowItemView;

import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.chat.activity.ChatActivity;

import com.hyphenate.easeim.section.dialog.SimpleDialogFragment;
import com.hyphenate.easeim.section.contact.viewmodels.NewGroupViewModel;

import com.hyphenate.easeim.section.group.adapter.GroupDetailMemberAdapter;
import com.hyphenate.easeim.section.group.fragment.GroupEditFragment;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.widget.EaseTitleBar;


import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.hyphenate.chat.EMGroupManager.EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;

public class NewGroupActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener{
    private static final int ADD_NEW_MEMBERS = 10;
    private EaseTitleBar titleBar;
    private TextView tvGroupMemberTitle;
    private TextView tvGroupMemberNum;
    private ArrowItemView itemGroupName;
    private ArrowItemView itemGroupIntroduction;

    private int maxUsers = 200;
    private static final int MAX_GROUP_USERS = 3000;
    private static final int MIN_GROUP_USERS = 3;
    private NewGroupViewModel viewModel;
    private List<EaseUser> members;

    private GroupDetailMemberAdapter memberAdapter;
    private RecyclerView memberList;
    private AppCompatButton confirmBtn;

    private String groupName = "";
    private String groupIntroduction = "";

    public static void actionStart(Context context,  ArrayList<EaseUser> members) {
        Intent intent = new Intent(context, NewGroupActivity.class);
        intent.putParcelableArrayListExtra("members", members);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_new_group;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        members = intent.getParcelableArrayListExtra("members");
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null) {
            initIntent(intent);
            initData();
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        tvGroupMemberTitle = findViewById(R.id.tv_group_member_title);
        tvGroupMemberNum = findViewById(R.id.tv_group_member_num);
        itemGroupName = findViewById(R.id.item_group_name);
        itemGroupIntroduction = findViewById(R.id.item_group_introduction);

        itemGroupName.getTvContent().setHint(getString(R.string.group_name));

        memberList = findViewById(R.id.rl_member_list);
        memberAdapter = new GroupDetailMemberAdapter();
        memberAdapter.setShowAll(true);
        memberList.setLayoutManager(new GridLayoutManager(this, 6));
        memberList.setAdapter(memberAdapter);
        confirmBtn = findViewById(R.id.done);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);

        itemGroupName.setOnClickListener(this);
        itemGroupIntroduction.setOnClickListener(this);

        memberAdapter.setOnAddClickListener(new GroupDetailMemberAdapter.GroupMemberAddClickListener() {
            @Override
            public void onAddClick() {
                GroupPickContactsActivity.actionStart(mContext, (ArrayList<EaseUser>) members);
            }

            @Override
            public void onRemoveClick() {

            }
        });

        confirmBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        List<EaseUser> data = new ArrayList<>();
        EaseUser user = new EaseUser("em_editUser");
        user.setNickname(getString(R.string.em_action_edit));
        data.add(user);
        data.addAll(members);
        memberAdapter.setData(data);

        if(members.size() <  2){
            confirmBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_gray_pressed));
            confirmBtn.setEnabled(false);
            confirmBtn.setText(getString(R.string.em_must_be_no_less_than_2_members));
        } else {
            confirmBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.search_close));
            confirmBtn.setEnabled(true);
            confirmBtn.setText(getString(R.string.em_group_new_save));
        }

        tvGroupMemberNum.setText(members.size() + "人");

        viewModel = new ViewModelProvider(this).get(NewGroupViewModel.class);
        viewModel.groupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EMGroup>() {
                @Override
                public void onSuccess(EMGroup data) {
//                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
//                    //跳转到群组聊天页面
//                    ChatActivity.actionStart(mContext, data.getGroupId(), DemoConstant.CHATTYPE_GROUP);
                    EMMessage message = EMMessage.createTextSendMessage("prompt", data.getGroupId());
                    message.setChatType(EMMessage.ChatType.GroupChat);
                    message.setAttribute(EaseConstant.CREATE_GROUP_PROMPT, true);
                    message.setAttribute(EaseConstant.CREATE_GROUP_NAME, data.getGroupName());
                    EaseIMHelper.getInstance().getChatManager().sendMessage(message);
                    finish();
                }

                @Override
                public void onLoading(EMGroup data) {
                    super.onLoading(data);
                    showLoading(getString(R.string.request));
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    dismissLoading();
                }
            });
        });

    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    private void checkGroupInfo() {
        String groupName = itemGroupName.getTvContent().getText().toString().trim();
        if(TextUtils.isEmpty(groupName)) {
            new SimpleDialogFragment.Builder(mContext)
                    .setTitle(R.string.em_group_new_name_cannot_be_empty)
                    .show();
            return;
        }
        if(maxUsers < MIN_GROUP_USERS || maxUsers > MAX_GROUP_USERS) {
            showToast(R.string.em_group_new_member_limit);
            return;
        }
//        viewModel.createGroup(groupName, desc, newmembers, reason, option);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_group_name ://群名称
                showGroupNameDialog();
                break;
            case R.id.item_group_introduction ://群介绍
                showIntroductionDialog();
                break;
            case R.id.done:

                EMGroupOptions option = new EMGroupOptions();
                option.style = EMGroupStylePrivateOnlyOwnerInvite;
                List<String> list = new ArrayList<>();
                for(EaseUser user : members){
                    list.add(user.getUsername());
                }
                viewModel.createGroup(groupName, groupIntroduction, list.toArray(new String[0]), "", option);
                break;
        }
    }

    private void showIntroductionDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.em_chat_group_detail_introduction),
                groupIntroduction,
                getString(R.string.em_chat_group_detail_introduction_hint),
                true,
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        groupIntroduction = content;
                    }
                });
    }

    private void showGroupNameDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.em_chat_group_detail_name),
                groupName,
                getString(R.string.em_chat_group_detail_name_hint),
                true,
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        groupName = content;
                        itemGroupName.getTvTitle().setText(groupName);
                    }
                });
    }
}
