package com.hyphenate.easeim.section.group.adapter;

import com.hyphenate.easeim.R;
import com.hyphenate.easeui.adapter.EaseBaseDelegateAdapter;
import com.hyphenate.easeui.domain.EaseUser;


public class GroupPickContactsAdapter extends EaseBaseDelegateAdapter<EaseUser> {
    private int emptyLayoutId;

    @Override
    public int getEmptyLayoutId() {
        return emptyLayoutId != 0 ? emptyLayoutId : R.layout.ease_layout_default_no_conversation_data;
    }

    /**
     * set empty layout
     * @param layoutId
     */
    public void setEmptyLayoutId(int layoutId) {
        this.emptyLayoutId = layoutId;
        notifyDataSetChanged();
    }

}
