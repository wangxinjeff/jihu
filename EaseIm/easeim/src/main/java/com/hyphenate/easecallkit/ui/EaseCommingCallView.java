package com.hyphenate.easecallkit.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.util.EMLog;

import com.hyphenate.easecallkit.utils.EaseCallKitUtils;
import com.hyphenate.easecallkit.widget.EaseImageView;


/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 01/15/2021
 */
public class EaseCommingCallView extends FrameLayout {

    private static final String TAG = EaseVideoCallActivity.class.getSimpleName();

    private ImageButton mBtnReject;
    private ImageButton mBtnPickup;
    private TextView mInviterName;
    private OnActionListener mOnActionListener;
    private EaseImageView avatar_view;
    private Bitmap headBitMap;
    private String headUrl;
    private String username;

    public EaseCommingCallView(@NonNull Context context) {
        this(context, null);
    }

    public EaseCommingCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseCommingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.activity_comming_call, this);
        mBtnReject = findViewById(R.id.btn_reject);
        mBtnPickup = findViewById(R.id.btn_pickup);
        mInviterName = findViewById(R.id.tv_nick);
        avatar_view = findViewById(R.id.iv_avatar);
        mBtnReject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnActionListener != null) {
                    mOnActionListener.onRejectClick(v);
                }
            }
        });

        mBtnPickup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnActionListener != null) {
                    mOnActionListener.onPickupClick(v);
                }
            }
        });
    }

    public void setInviteInfo(String username){
        this.username = username;
        mInviterName.setText(EaseCallKitUtils.getUserNickName(username));
        headUrl = EaseCallKitUtils.getUserHeadImage(username);

        //加载头像图片
        loadHeadImage();
    }

    /**
     * 加载用户配置头像
     * @return
     */
    private void loadHeadImage() {
//        EaseUserProfileProvider profileProvider = EaseIM.getInstance().getUserProvider();
//        if(profileProvider != null){
//            EaseUserUtils.setUserAvatar(getContext(), username, avatar_view);
//            EaseUserUtils.setUserNick(username, mInviterName);
//        }

        EaseUserUtils.setUserAvatar(getContext(), username, avatar_view);
        EaseUserUtils.setUserNick(username, mInviterName);
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }



    public void setOnActionListener(OnActionListener listener) {
        this.mOnActionListener = listener;
    }

    public interface OnActionListener {
        void onRejectClick(View v);
        void onPickupClick(View v);
    }


    float[] getScreenInfo(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        float[] info = new float[5];
        if(manager != null) {
            DisplayMetrics dm = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(dm);
            info[0] = dm.widthPixels;
            info[1] = dm.heightPixels;
            info[2] = dm.densityDpi;
            info[3] = dm.density;
            info[4] = dm.scaledDensity;
        }
        return info;
    }
}

