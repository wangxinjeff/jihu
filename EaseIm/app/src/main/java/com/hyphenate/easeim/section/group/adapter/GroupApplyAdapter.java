package com.hyphenate.easeim.section.group.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.model.GroupApplyBean;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;


public class GroupApplyAdapter extends EaseBaseRecyclerViewAdapter<GroupApplyBean> {

    private OnGroupApplyListener listener;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupApplyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_apply_item, parent, false));
    }

    public void setOnGroupApplyListener(OnGroupApplyListener listener){
        this.listener = listener;
    }

    class GroupApplyViewHolder extends ViewHolder<GroupApplyBean>{
        AppCompatTextView customerName;
        AppCompatTextView groupName;
        AppCompatTextView inviterName;
        AppCompatButton btnRefused;
        AppCompatButton btnAgree;
        AppCompatButton btnOperated;

        public GroupApplyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            customerName = itemView.findViewById(R.id.customer_name);
            groupName = itemView.findViewById(R.id.group_name);
            inviterName = itemView.findViewById(R.id.inviter_name);
            btnRefused = itemView.findViewById(R.id.btn_refused);
            btnAgree = itemView.findViewById(R.id.btn_agree);
            btnOperated = itemView.findViewById(R.id.btn_operated);
        }

        @Override
        public void setData(GroupApplyBean item, int position) {
            customerName.setText(item.getCustomerName());
            groupName.setText(item.getGroupName());
            inviterName.setText(item.getInviterName());
            if(item.isOperated()){
                btnRefused.setVisibility(View.GONE);
                btnAgree.setVisibility(View.GONE);
                btnOperated.setVisibility(View.VISIBLE);
            } else {
                btnRefused.setVisibility(View.VISIBLE);
                btnAgree.setVisibility(View.VISIBLE);
                btnOperated.setVisibility(View.GONE);
            }

            btnRefused.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        listener.onRefused();
                    }
                }
            });
            btnAgree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        listener.onAgreed();
                    }
                }
            });
        }
    }

    public interface OnGroupApplyListener{
        void onRefused();
        void onAgreed();
    }
}
