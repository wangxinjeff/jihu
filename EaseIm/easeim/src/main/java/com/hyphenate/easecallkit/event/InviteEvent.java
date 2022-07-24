package com.hyphenate.easecallkit.event;

import com.hyphenate.easecallkit.base.EaseCallType;
import com.hyphenate.easecallkit.utils.EaseCallAction;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 01/16/2021
 */
public class InviteEvent extends BaseEvent {
    public InviteEvent(){
        callAction = EaseCallAction.CALL_INVITE;
    }
    public EaseCallType type;
}
