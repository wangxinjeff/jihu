package com.hyphenate.easeui.modules.chat;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeim.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.manager.EaseDingMessageHelper;
import com.hyphenate.easeui.modules.chat.interfaces.OnAddMsgAttrsBeforeSendEvent;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatLayoutListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatRecordTouchListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnMenuChangeListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnTranslateMessageListener;
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper;
import com.hyphenate.easeui.modules.menu.MenuItemBean;
import com.hyphenate.easeui.ui.EaseBaiduMapActivity;
import com.hyphenate.easeui.ui.base.EaseBaseFragment;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseCompat;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.utils.Glide4Engine;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;
import com.hyphenate.util.VersionUtils;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.hyphenate.mediapicker.SmartMediaPicker;
import com.hyphenate.mediapicker.config.Constant;
import com.hyphenate.mediapicker.config.MediaPickerEnum;

public class EaseChatFragment extends EaseBaseFragment implements OnChatLayoutListener, OnMenuChangeListener, OnAddMsgAttrsBeforeSendEvent, OnChatRecordTouchListener, OnTranslateMessageListener {
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected static final int REQUEST_CODE_DING_MSG = 4;
    protected static final int REQUEST_CODE_SELECT_VIDEO = 11;
    protected static final int REQUEST_CODE_SELECT_FILE = 12;
    private static final String TAG = EaseChatFragment.class.getSimpleName();
    public EaseChatLayout chatLayout;
    public String conversationId;
    public int chatType;
    public String historyMsgId;
    public boolean isRoam;
    public boolean isMessageInit;
    private OnChatLayoutListener listener;

    protected File cameraFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initArguments();
        return inflater.inflate(getLayoutId(), null);
    }

    private int getLayoutId() {
        return R.layout.ease_fragment_chat_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    public void initArguments() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            conversationId = bundle.getString(EaseConstant.EXTRA_CONVERSATION_ID);
            chatType = bundle.getInt(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
            historyMsgId = bundle.getString(EaseConstant.HISTORY_MSG_ID);
            isRoam = bundle.getBoolean(EaseConstant.EXTRA_IS_ROAM, false);
        }
    }

    public void initView() {
        chatLayout = findViewById(R.id.layout_chat);
        chatLayout.getChatMessageListLayout().setItemShowType(EaseChatMessageListLayout.ShowType.NORMAL);
        chatLayout.getChatMessageListLayout().setBackgroundColor(ContextCompat.getColor(mContext, R.color.gray));
    }

    public void initListener() {
        chatLayout.setOnChatLayoutListener(this);
        chatLayout.setOnPopupWindowItemClickListener(this);
        chatLayout.setOnAddMsgAttrsBeforeSendEvent(this);
        chatLayout.setOnChatRecordTouchListener(this);
        chatLayout.setOnTranslateListener(this);
    }

    public void initData() {
        if(!TextUtils.isEmpty(historyMsgId)) {
            chatLayout.init(EaseChatMessageListLayout.LoadDataType.HISTORY, conversationId, chatType);
            chatLayout.loadData(historyMsgId);
        }else {
            if(isRoam) {
                chatLayout.init(EaseChatMessageListLayout.LoadDataType.ROAM, conversationId, chatType);
            }else {
                chatLayout.init(conversationId, chatType);
            }
            chatLayout.loadDefaultData();
        }
        isMessageInit = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isMessageInit) {
            chatLayout.getChatMessageListLayout().refreshMessages();
        }
    }

    public void setOnChatLayoutListener(OnChatLayoutListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onBubbleClick(EMMessage message) {
        if(listener != null) {
            return listener.onBubbleClick(message);
        }
        return false;
    }

    @Override
    public boolean onBubbleLongClick(View v, EMMessage message) {
        if(listener != null) {
            return listener.onBubbleLongClick(v, message);
        }
        return false;
    }

    @Override
    public void onUserAvatarClick(String username) {
        if(listener != null) {
            listener.onUserAvatarClick(username);
        }
    }

    @Override
    public void onUserAvatarLongClick(String username) {
        if(listener != null) {
            listener.onUserAvatarLongClick(username);
        }
    }

    @Override
    public void onChatExtendMenuItemClick(View view, int itemId) {
        if(itemId == R.id.extend_item_take_picture) {
            selectPicFromCamera();
        }else if(itemId == R.id.extend_item_picture) {
            selectPicFromLocal();
        }else if(itemId == R.id.extend_item_location) {
            startMapLocation(REQUEST_CODE_MAP);
        }else if(itemId == R.id.extend_item_video) {
            selectVideoFromLocal();
        }else if(itemId == R.id.extend_item_file) {
            selectFileFromLocal();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void onChatSuccess(EMMessage message) {
        // you can do something after sending a successful message
    }

    @Override
    public void onChatError(int code, String errorMsg) {
        if(listener != null) {
            listener.onChatError(code, errorMsg);
        }
    }

    @Override
    public void onReadNumClick(EMMessage message) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            chatLayout.getChatInputMenu().hideExtendContainer();
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                onActivityResultForCamera(data);
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                onActivityResultForLocalPhotos(data);
            } else if (requestCode == REQUEST_CODE_MAP) { // location
                onActivityResultForMapLocation(data);
            } else if (requestCode == REQUEST_CODE_DING_MSG) { // To send the ding-type msg.
                onActivityResultForDingMsg(data);
            }else if(requestCode == REQUEST_CODE_SELECT_FILE) {
                onActivityResultForLocalFiles(data);
            }else if(requestCode == REQUEST_CODE_SELECT_VIDEO) {
                onActivityResultForLocalVideos(data);
            } else if(requestCode == Constant.REQUEST_CODE_CHOOSE){
                chatLayout.sendImageMessage(Matisse.obtainResult(data).get(0), true);
            } else if(requestCode == Constant.CAMERA_RESULT_CODE){
                List<String> resultData = SmartMediaPicker.getResultData(getContext(), requestCode, resultCode, data);
                if (resultData != null && resultData.size() > 0) {
                    if(resultData.get(0).contains(".jpg")){
                        chatLayout.sendImageMessage(Uri.parse(resultData.get(0)), true);
                    } else if(resultData.get(0).contains(".mp4")){
                        chatLayout.sendVideoMessage(Uri.parse(resultData.get(0)), SmartMediaPicker.getVideoDuration(getContext(), EaseCompat.getUriForFile(getContext(), new File(resultData.get(0)))));
                    }
                }
            }
        }
    }

    private void onActivityResultForLocalVideos(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mContext,uri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int duration = mediaPlayer.getDuration();
            EMLog.d(TAG, "path = "+uri.getPath()+",duration="+duration );
            chatLayout.sendVideoMessage(uri, duration);
        }
    }

    /**
     * select picture from camera
     */
    protected void selectPicFromCamera() {
        if(!checkSdCardExist()) {
            return;
        }
//        cameraFile = new File(PathUtil.getInstance().getImagePath(), EMClient.getInstance().getCurrentUser()
//                + System.currentTimeMillis() + ".jpg");
//        //noinspection ResultOfMethodCallIgnored
//        cameraFile.getParentFile().mkdirs();
//        startActivityForResult(
//                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, EaseCompat.getUriForFile(getContext(), cameraFile)),
//                REQUEST_CODE_CAMERA);

        SmartMediaPicker.builder(this)
                //最大图片选择数目 如果不需要图片 将数目设置为0
                .withMaxImageSelectable(1)
                //最大视频选择数目 如果不需要视频 将数目设置为0
                .withMaxVideoSelectable(1)
                //图片选择器是否显示数字
                .withCountable(false)
                //最大视频长度
                .withMaxVideoLength(15 * 1000)
                //最大视频文件大小 单位MB
                .withMaxVideoSize(10)
                //最大图片高度 默认1920
                .withMaxHeight(4032)
                //最大图片宽度 默认1920
                .withMaxWidth(4032)
                //最大图片大小 单位MB
                .withMaxImageSize(10)
                //设置图片加载引擎
                .withImageEngine(new Glide4Engine())
                //前置摄像头拍摄是否镜像翻转图像
                .withIsMirror(true)
                //弹出类别，默认弹出底部选择栏，也可以选择单独跳转
                .withMediaPickerType(MediaPickerEnum.CAMERA)
                .build(getContext())
                .show();
    }

    /**
     * select local image
     */
    protected void selectPicFromLocal() {
        SmartMediaPicker.builder(this)
                //最大图片选择数目 如果不需要图片 将数目设置为0
                .withMaxImageSelectable(1)
                //最大视频选择数目 如果不需要视频 将数目设置为0
                .withMaxVideoSelectable(0)
                //图片选择器是否显示数字
                .withCountable(false)
                //最大视频长度
                .withMaxVideoLength(15 * 1000)
                //最大视频文件大小 单位MB
                .withMaxVideoSize(10)
                //最大图片高度 默认1920
                .withMaxHeight(4032)
                //最大图片宽度 默认1920
                .withMaxWidth(4032)
                //最大图片大小 单位MB
                .withMaxImageSize(10)
                //设置图片加载引擎
                .withImageEngine(new Glide4Engine())
                //前置摄像头拍摄是否镜像翻转图像
                .withIsMirror(true)
                //弹出类别，默认弹出底部选择栏，也可以选择单独跳转
                .withMediaPickerType(MediaPickerEnum.PHOTO_PICKER)
                .build(getContext())
                .show();
    }

    /**
     * 启动定位
     * @param requestCode
     */
    protected void startMapLocation(int requestCode) {
        EaseBaiduMapActivity.actionStartForResult(this, requestCode);
    }

    /**
     * select local video
     */
    protected void selectVideoFromLocal() {
        Intent intent = new Intent();
        if(VersionUtils.isTargetQ(getActivity())) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);

    }

    /**
     * select local file
     */
    protected void selectFileFromLocal() {
        Intent intent = new Intent();
        if(VersionUtils.isTargetQ(getActivity())) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    /**
     * 相机返回处理结果
     * @param data
     */
    protected void onActivityResultForCamera(Intent data) {
        if (cameraFile != null && cameraFile.exists()) {
            chatLayout.sendImageMessage(Uri.parse(cameraFile.getAbsolutePath()));
        }
    }

    /**
     * 选择本地图片处理结果
     * @param data
     */
    protected void onActivityResultForLocalPhotos(@Nullable Intent data) {
        if (data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                String filePath = EaseFileUtils.getFilePath(mContext, selectedImage);
                if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    chatLayout.sendImageMessage(Uri.parse(filePath));
                }else {
                    EaseFileUtils.saveUriPermission(mContext, selectedImage, data);
                    chatLayout.sendImageMessage(selectedImage);
                }
            }
        }
    }

    /**
     * 地图定位结果处理
     * @param data
     */
    protected void onActivityResultForMapLocation(@Nullable Intent data) {
        if(data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String locationAddress = data.getStringExtra("address");
            String buildingName = data.getStringExtra("buildingName");
            if (locationAddress != null && !locationAddress.equals("")) {
                chatLayout.sendLocationMessage(latitude, longitude, locationAddress, buildingName);
            } else {
                if(listener != null) {
                    listener.onChatError(-1, getResources().getString(R.string.unable_to_get_loaction));
                }
            }
        }
    }

    protected void onActivityResultForDingMsg(@Nullable Intent data) {
        if(data != null) {
            String msgContent = data.getStringExtra("msg");
            EMLog.i(TAG, "To send the ding-type msg, content: " + msgContent);
            // Send the ding-type msg.
            EMMessage dingMsg = EaseDingMessageHelper.get().createDingMessage(conversationId, msgContent);
            chatLayout.sendMessage(dingMsg);
        }
    }

    /**
     * 本地文件选择结果处理
     * @param data
     */
    protected void onActivityResultForLocalFiles(@Nullable Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = EaseFileUtils.getFilePath(mContext, uri);
                if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    chatLayout.sendFileMessage(Uri.parse(filePath));
                }else {
                    EaseFileUtils.saveUriPermission(mContext, uri, data);
                    chatLayout.sendFileMessage(uri);
                }
            }
        }
    }

    /**
     * 检查sd卡是否挂载
     * @return
     */
    protected boolean checkSdCardExist() {
        return EaseCommonUtils.isSdcardExist();
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, EMMessage message, View v) {

    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, EMMessage message) {
        return false;
    }

    @Override
    public void addMsgAttrsBeforeSend(EMMessage message) {

    }

    /**
     * Set whether can touch voice button
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onRecordTouch(View v, MotionEvent event) {
        return true;
    }

    @Override
    public void translateMessageSuccess(EMMessage message) {

    }

    @Override
    public void translateMessageFail(EMMessage message, int code, String error) {

    }
}

