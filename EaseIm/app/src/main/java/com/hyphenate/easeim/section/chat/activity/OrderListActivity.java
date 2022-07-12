package com.hyphenate.easeim.section.chat.activity;

import android.content.Context;
import android.content.Intent;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.section.base.BaseInitActivity;

public class OrderListActivity extends BaseInitActivity {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, OrderListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order_list;
    }
}
