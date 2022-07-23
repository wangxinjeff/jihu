package com.hyphenate.easeui.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.section.base.BaseInitFragment;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.util.List;

public class GroupReadAckListFragment extends BaseInitFragment{
    private RecyclerView ackList;
    private GroupAckListAdapter ackListAdapter;
    private List<String> data;

    public GroupReadAckListFragment(List<String> data) {
        this.data = data;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.em_fragment_group_ack_list;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        ackList = findViewById(R.id.ack_list);
        ackList.setLayoutManager(new LinearLayoutManager(getContext()));
        ackListAdapter = new GroupAckListAdapter();
        ackList.setAdapter(ackListAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        ackListAdapter.setData(data);

        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                if(ackListAdapter != null){
                    ackListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    class GroupAckListAdapter extends EaseBaseRecyclerViewAdapter<String> {


        @Override
        public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
            return new AckViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list_item, parent, false));
        }

        class AckViewHolder extends EaseBaseRecyclerViewAdapter.ViewHolder<String>{

            AppCompatImageView avatar;
            AppCompatTextView name;
            Context context;

            public AckViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            public void initView(View itemView) {
                context = itemView.getContext();
                avatar = findViewById(R.id.member_item_avatar);
                name = findViewById(R.id.member_item_nick);
            }

            @Override
            public void setData(String item, int position) {
                EaseUserUtils.setUserAvatar(context, item, avatar);
                EaseUserUtils.setUserNick(item, name);
            }
        }
    }

}
