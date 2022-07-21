package com.hyphenate.easeim.section.group.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.model.GroupApplyBean;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.group.adapter.GroupApplyAdapter;

import com.hyphenate.easeui.widget.EaseTitleBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupApplyActivity extends BaseInitActivity {

    private EaseTitleBar titleBar;
    private RecyclerView applyList;
    private GroupApplyAdapter applyAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_apply;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        applyList = findViewById(R.id.apple_list);
        applyList.setLayoutManager(new LinearLayoutManager(this));
        applyAdapter = new GroupApplyAdapter();
        applyList.setAdapter(applyAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        List<GroupApplyBean> list = new ArrayList<>();
        GroupApplyBean bean = new GroupApplyBean();
        bean.setCustomerName("123");
        bean.setGroupName("专属群");
        bean.setInviterName("111");
        bean.setOperated(false);
        bean.setTimeStamp(1658370875111L);
        GroupApplyBean bean1 = new GroupApplyBean();
        bean1.setCustomerName("123");
        bean1.setGroupName("专属群");
        bean1.setInviterName("111");
        bean1.setOperated(true);
        bean1.setTimeStamp(1658370875178L);
        GroupApplyBean bean2 = new GroupApplyBean();
        bean2.setCustomerName("123");
        bean2.setGroupName("专属群");
        bean2.setInviterName("111");
        bean2.setOperated(false);
        bean2.setTimeStamp(1658370875000L);
        GroupApplyBean bean3 = new GroupApplyBean();
        bean3.setCustomerName("123");
        bean3.setGroupName("专属群");
        bean3.setInviterName("111");
        bean3.setOperated(true);
        bean3.setTimeStamp(1658370875123L);
        list.add(bean);
        list.add(bean1);
        list.add(bean2);
        list.add(bean3);
        List<GroupApplyBean> operatedList = new ArrayList<>();
        List<GroupApplyBean> unOperatedList = new ArrayList<>();
        for(GroupApplyBean b : list){
            if(b.isOperated()){
                operatedList.add(b);
            } else {
                unOperatedList.add(b);
            }
        }
        sortByTimestamp(unOperatedList);
        sortByTimestamp(operatedList);
        operatedList.addAll(0, unOperatedList);
        applyAdapter.setData(operatedList);
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

        applyAdapter.setOnGroupApplyListener(new GroupApplyAdapter.OnGroupApplyListener() {
            @Override
            public void onRefused() {
                ToastUtils.showCenterToast("", "已拒绝", 0, Toast.LENGTH_SHORT);
            }

            @Override
            public void onAgreed() {
                ToastUtils.showCenterToast("", "已同意", 0, Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * 排序
     * @param list
     */
    private void sortByTimestamp(List<GroupApplyBean> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        Collections.sort(list, new Comparator<GroupApplyBean>() {
            @Override
            public int compare(GroupApplyBean o1, GroupApplyBean o2) {
                if(o2.getTimeStamp() > o1.getTimeStamp()) {
                    return 1;
                }else if(o2.getTimeStamp() == o1.getTimeStamp()) {
                    return 0;
                }else {
                    return -1;
                }
            }
        });
    }
}
