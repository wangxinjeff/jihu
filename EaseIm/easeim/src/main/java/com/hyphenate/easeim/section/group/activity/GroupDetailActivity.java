package com.hyphenate.easeim.section.group.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.widget.ArrowItemView;
import com.hyphenate.easeim.common.widget.SwitchItemView;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.dialog.DemoDialogFragment;
import com.hyphenate.easeim.section.dialog.SimpleDialogFragment;
import com.hyphenate.easeim.section.group.GroupHelper;
import com.hyphenate.easeim.section.group.adapter.GroupDetailMemberAdapter;
import com.hyphenate.easeim.section.group.fragment.GroupEditFragment;
import com.hyphenate.easeim.section.group.viewmodels.GroupDetailViewModel;
import com.hyphenate.easeim.section.search.SearchHistoryChatActivity;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.List;

public class GroupDetailActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener, SwitchItemView.OnCheckedChangeListener {
    private static final int REQUEST_CODE_GROUP_NAME = 0;
    private static final int REQUEST_CODE_GROUP_NOTICE = 1;
    private static final int REQUEST_CODE_GROUP_INTRO = 2;
    private static final int REQUEST_CODE_GROUP_MUTE = 3;
    private static final int REQUEST_CODE_GROUP_NOTE = 4;
    private EaseTitleBar titleBar;
    private View groupInfo;
    private EaseImageView ivGroupAvatar;
    private ImageView iconNext;
    private TextView tvGroupMemberTitle;
    private TextView tvGroupMemberNum;
    private ArrowItemView itemGroupName;
    private ArrowItemView itemGroupOwner;
    private ArrowItemView itemGroupNotice;
    private ArrowItemView itemGroupIntroduction;
    private ArrowItemView itemGroupMute;
    private ArrowItemView itemGroupNote;
    private ArrowItemView itemGroupHistory;
    private SwitchItemView itemGroupNotDisturb;
    private String groupId;
    private EMGroup group;
    private GroupDetailViewModel viewModel;
    private EMConversation conversation;
    private GroupDetailMemberAdapter memberAdapter;
    private RecyclerView memberList;
    private LinearLayout showMore;

    private String systemNote;
    private String serviceNote;

    public static void actionStart(Context context, String groupId) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.demo_activity_chat_group_detail;
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
        groupInfo = findViewById(R.id.cl_group_info);
        ivGroupAvatar = findViewById(R.id.iv_group_avatar);
        iconNext = findViewById(R.id.icon_next);
        tvGroupMemberTitle = findViewById(R.id.tv_group_member_title);
        tvGroupMemberNum = findViewById(R.id.tv_group_member_num);
        itemGroupName = findViewById(R.id.item_group_name);
        itemGroupOwner = findViewById(R.id.item_group_owner);
        itemGroupNotice = findViewById(R.id.item_group_notice);
        itemGroupIntroduction = findViewById(R.id.item_group_introduction);
        itemGroupMute = findViewById(R.id.item_group_mute);
        itemGroupNote = findViewById(R.id.item_group_note);
        itemGroupHistory = findViewById(R.id.item_group_history);
        itemGroupNotDisturb = findViewById(R.id.item_group_not_disturb);
        memberList = findViewById(R.id.rl_member_list);
        memberAdapter = new GroupDetailMemberAdapter();
        memberList.setLayoutManager(new GridLayoutManager(this, 6));
        memberList.setAdapter(memberAdapter);
        showMore = findViewById(R.id.show_more_member);

        group = EaseIMHelper.getInstance().getGroupManager().getGroup(groupId);

        if(EaseIMHelper.getInstance().isAdmin()){
            titleBar.setLeftImageResource(R.drawable.icon_back_admin);
            itemGroupNote.setVisibility(View.VISIBLE);
            if(isOwner()){
                iconNext.setVisibility(View.VISIBLE);
                itemGroupName.setItemShowArrow(true);
                itemGroupMute.setVisibility(View.VISIBLE);
            }
        }

        initGroupView();
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
        if(EaseIMHelper.getInstance().isAdmin() && isOwner()){
            groupInfo.setOnClickListener(this);
            itemGroupName.setOnClickListener(this);
        }
        tvGroupMemberTitle.setOnClickListener(this);
        tvGroupMemberNum.setOnClickListener(this);
        itemGroupNotice.setOnClickListener(this);
        itemGroupIntroduction.setOnClickListener(this);
        itemGroupMute.setOnClickListener(this);
        itemGroupNote.setOnClickListener(this);
        itemGroupHistory.setOnClickListener(this);
        itemGroupNotDisturb.setOnCheckedChangeListener(this);
        showMore.setOnClickListener(this);
        memberAdapter.setOnAddClickListener(new GroupDetailMemberAdapter.GroupMemberAddClickListener() {
            @Override
            public void onAddClick() {
                GroupPickContactsActivity.actionStart(mContext, groupId, false);
            }

            @Override
            public void onRemoveClick() {
                GroupRemoveMemberActivity.startAction(mContext, groupId);
            }
        });
    }

    private void initGroupView() {
        if(group == null) {
            finish();
            return;
        }
        itemGroupName.getTvContent().setText(group.getGroupName());
        itemGroupOwner.getTvContent().setText(group.getOwner());
        tvGroupMemberNum.setText(getString(R.string.em_chat_group_detail_member_num, group.getMemberCount()));
        conversation = EaseIMHelper.getInstance().getConversation(groupId, EMConversation.EMConversationType.GroupChat, true);

//        itemGroupIntroduction.getTvContent().setText(group.getDescription());

        makeTextSingleLine(itemGroupNotice.getTvContent());
        makeTextSingleLine(itemGroupIntroduction.getTvContent());

        List<String> disabledIds = EaseIMHelper.getInstance().getPushManager().getNoPushGroups();
        itemGroupNotDisturb.getSwitch().setChecked(disabledIds != null && disabledIds.contains(groupId));
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EMGroup>() {
                @Override
                public void onSuccess(EMGroup data) {
                    group = data;
                    initGroupView();
                }
            });
        });
        viewModel.getAnnouncementObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String data) {
//                    itemGroupNotice.getTvContent().setText(data);
                }
            });
        });
        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    loadGroup();
                }
            });
        });
        viewModel.getMessageChangeObservable().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event.isGroupLeave() && TextUtils.equals(groupId, event.message)) {
                finish();
                return;
            }
            if(event.isGroupChange()) {
                loadGroup();
            }
        });
        viewModel.getLeaveGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    finish();
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_LEAVE, EaseEvent.TYPE.GROUP, groupId));
                }
            });
        });
        viewModel.blockGroupMessageObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    //itemGroupNotDisturb.getSwitch().setChecked(true);
                }
            });
        });
        viewModel.unblockGroupMessage().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    //itemGroupNotDisturb.getSwitch().setChecked(false);
                }
            });
        });
        viewModel.offPushObservable().observe(this, response -> {
            if(response) {
                loadGroup();
            }
        });
        viewModel.getClearHistoryObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    LiveDataBus.get().with(DemoConstant.CONVERSATION_DELETE).postValue(new EaseEvent(DemoConstant.CONTACT_DECLINE, EaseEvent.TYPE.MESSAGE));
                }
            });
        });

        viewModel.getGroupMember().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if(EaseIMHelper.getInstance().isAdmin() && isOwner()){
                                EaseUser removeUser = new EaseUser("em_removeUser");
                                removeUser.setNickname(getString(R.string.action_delete));
                                data.add(0, removeUser);
                            }
                            EaseUser addUser = new EaseUser("em_addUser");
                            addUser.setNickname(getString(R.string.em_add_member));
                            data.add(0, addUser);
                            memberAdapter.setData(data);
                        }
                    });
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    dismissLoading();
                }

                @Override
                public void onLoading(@Nullable List<EaseUser> data) {
                    super.onLoading(data);
                    showLoading();
                }
            });
        });

        loadGroup();

        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                if(memberAdapter != null){
                    memberAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void loadGroup() {
        viewModel.getGroup(groupId);
        viewModel.getGroupAnnouncement(groupId);
        viewModel.getGroupAllMember(groupId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();// 群成员
        if (id == R.id.cl_group_info) {
        } else if (id == R.id.tv_group_member_title || id == R.id.show_more_member) {
            GroupMemberTypeActivity.actionStart(mContext, groupId, isOwner());
        } else if (id == R.id.item_group_name) {//群名称
//                showGroupNameDialog();
            GroupEditActivity.startEditForResult(mContext, getString(R.string.em_chat_group_detail_name),
                    group.getGroupName(),
                    getString(R.string.em_chat_group_detail_name_hint),
                    GroupHelper.isOwner(group), REQUEST_CODE_GROUP_NAME);
        } else if (id == R.id.item_group_notice) {//群公告
//                showAnnouncementDialog();
            GroupEditActivity.startEditForResult(mContext, getString(R.string.em_chat_group_detail_announcement),
                    group.getAnnouncement(),
                    EaseIMHelper.getInstance().isAdmin() ? getString(R.string.em_chat_group_detail_announcement_hint) : "",
                    GroupHelper.isOwner(group), REQUEST_CODE_GROUP_NOTICE);
        } else if (id == R.id.item_group_introduction) {//群介绍
//                showIntroductionDialog();
            GroupEditActivity.startEditForResult(mContext, getString(R.string.em_chat_group_detail_introduction),
                    group.getDescription(),
                    EaseIMHelper.getInstance().isAdmin() ? getString(R.string.em_chat_group_detail_introduction_hint) : "",
                    GroupHelper.isOwner(group), REQUEST_CODE_GROUP_NOTICE);
        } else if (id == R.id.item_group_mute) {
            GroupMuteActivity.startAction(mContext, groupId);
        } else if (id == R.id.item_group_note) {
            GroupEditActivity.startNoteForResult(mContext, getString(R.string.em_group_note),
                    "",
                    "",
                    getString(R.string.em_group_note_hint),
                    REQUEST_CODE_GROUP_NOTE
            );
        } else if (id == R.id.item_group_history) {//查找聊天记录
            SearchHistoryChatActivity.actionStart(mContext, groupId, EaseConstant.CHATTYPE_GROUP);
        }
    }

    private void showClearConfirmDialog() {
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(R.string.em_chat_group_detail_clear_history_warning)
                .setOnConfirmClickListener(new DemoDialogFragment.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.clearHistory(groupId);
                    }
                })
                .showCancelButton(true)
                .show();
    }

    private void showConfirmDialog() {
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(isOwner() ? R.string.em_chat_group_detail_dissolve : R.string.em_chat_group_detail_refund)
                .setOnConfirmClickListener(new DemoDialogFragment.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        if(isOwner()) {
                            viewModel.destroyGroup(groupId);
                        }else {
                            viewModel.leaveGroup(groupId);
                        }
                    }
                })
                .showCancelButton(true)
                .show();
    }

    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.item_group_not_disturb) {//消息免打扰
            viewModel.updatePushServiceForGroup(groupId, isChecked);
//                if(isChecked) {
//                    viewModel.blockGroupMessage(groupId);
//                }else {
//                    viewModel.unblockGroupMessage(groupId);
//                }
        }
    }

    private void showGroupNameDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.em_chat_group_detail_name),
                group.getGroupName(),
                getString(R.string.em_chat_group_detail_name_hint),
                GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        //修改群公告
                        viewModel.setGroupName(groupId, content);
                    }
                });
    }

    private void showAnnouncementDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.em_chat_group_detail_announcement),
                group.getAnnouncement(),
                getString(R.string.em_chat_group_detail_announcement_hint),
                GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        //修改群公告
                        viewModel.setGroupAnnouncement(groupId, content);
                    }
                });
    }

    private void showIntroductionDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.em_chat_group_detail_introduction),
                group.getDescription(),
                getString(R.string.em_chat_group_detail_introduction_hint),
                GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        //修改群介绍
                        viewModel.setGroupDescription(groupId, content);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            String content = data.getStringExtra("content");
            switch (requestCode) {
                case REQUEST_CODE_GROUP_NAME:
                    //修改群名称
                    viewModel.setGroupName(groupId, content);
                    break;
                case REQUEST_CODE_GROUP_NOTICE:
                    //修改群公告
                    viewModel.setGroupAnnouncement(groupId, content);
                    break;
                case REQUEST_CODE_GROUP_INTRO:
                    //修改群介绍
                    viewModel.setGroupDescription(groupId, content);
                    break;
                case REQUEST_CODE_GROUP_NOTE:
                    //修改运营备注
                    //todo：待实现  获取运营备注
                    break;

            }
        }
    }

    private void makeTextSingleLine(TextView tv) {
        tv.setMaxLines(1);
        tv.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    /**
     * 是否有邀请权限
     * @return
     */
    private boolean isCanInvite() {
        return GroupHelper.isCanInvite(group);
    }

    /**
     * 是否是管理员
     * @return
     */
    private boolean isAdmin() {
        return GroupHelper.isAdmin(group);
    }

    /**
     * 是否是群主
     * @return
     */
    private boolean isOwner() {
        return GroupHelper.isOwner(group);
    }
}
