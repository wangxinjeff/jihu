package com.hyphenate.easeim.common.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 由于View.class源码第5497~5499行
 * 对background有过滤，所以为了保证ChatView中绘制的正常生效，
 * 在父类ChatLayout中进行手动添加background
 */
public class ChatLayout extends RelativeLayout {

    public ChatLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundColor(Color.TRANSPARENT);
    }
}
