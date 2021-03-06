package com.hyphenate.easeim;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.heytap.msp.push.HeytapPushManager;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMChatRoomManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMPushManager;
import com.hyphenate.chat.EMTranslateParams;
import com.hyphenate.cloud.EMHttpClient;
import com.hyphenate.easecallkit.EaseCallKit;
import com.hyphenate.easecallkit.base.EaseCallEndReason;
import com.hyphenate.easecallkit.base.EaseCallKitConfig;
import com.hyphenate.easecallkit.base.EaseCallKitListener;
import com.hyphenate.easecallkit.base.EaseCallKitTokenCallback;
import com.hyphenate.easecallkit.base.EaseCallType;
import com.hyphenate.easecallkit.base.EaseCallUserInfo;
import com.hyphenate.easecallkit.base.EaseGetUserAccountCallback;
import com.hyphenate.easecallkit.base.EaseUserAccount;
import com.hyphenate.easecallkit.event.CallCancelEvent;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.db.DemoDbHelper;
import com.hyphenate.easeim.common.interfaceOrImplement.UserActivityLifecycleCallbacks;
import com.hyphenate.easeim.common.manager.UserProfileManager;
import com.hyphenate.easeim.common.model.DemoModel;
import com.hyphenate.easeim.common.model.EmojiconExampleGroupData;
import com.hyphenate.easeim.common.receiver.HeadsetReceiver;
import com.hyphenate.easeim.common.repositories.EMClientRepository;
import com.hyphenate.easeim.common.utils.FetchUserInfoList;
import com.hyphenate.easeim.common.utils.FetchUserRunnable;
import com.hyphenate.easeim.common.utils.PreferenceManager;
import com.hyphenate.easeim.section.av.MultipleVideoActivity;
import com.hyphenate.easeim.section.av.VideoCallActivity;
import com.hyphenate.easeim.section.chat.ChatPresenter;
import com.hyphenate.easeim.section.chat.activity.ChatActivity;
import com.hyphenate.easeim.section.chat.delegates.ChatNoticeAdapterDelegate;
import com.hyphenate.easeim.section.conference.ConferenceInviteActivity;
import com.hyphenate.easeim.section.conversation.ConversationListActivity;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.delegate.EaseFileAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseImageAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseLocationAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseTextAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseVideoAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseVoiceAdapterDelegate;
import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseMessageTypeSetManager;
import com.hyphenate.easeui.model.EaseNotifier;
import com.hyphenate.easeui.provider.EaseEmojiconInfoProvider;
import com.hyphenate.easeui.provider.EaseSettingsProvider;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.push.EMPushConfig;
import com.hyphenate.push.EMPushHelper;
import com.hyphenate.push.EMPushType;
import com.hyphenate.push.PushListener;
import com.hyphenate.util.EMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * ??????hyphenate-sdk???????????????????????????sdk??????????????????????????????
 */
public class EaseIMHelper {
    private static final String TAG = EaseIMHelper.class.getSimpleName();

    public boolean isSDKInit;//SDK???????????????
    private static EaseIMHelper mInstance;
    private DemoModel demoModel = null;
    private Map<String, EaseUser> contactList;
    private UserProfileManager userProManager;

    private EaseCallKitListener callKitListener;
    private Context mainContext;

    private String tokenUrl = "http://a1.easemob.com/token/rtcToken/v1";
    private String uIdUrl = "http://a1.easemob.com/channel/mapper";
    
    private FetchUserRunnable fetchUserRunnable;
    private Thread fetchUserTread;
    private FetchUserInfoList fetchUserInfoList;
    private boolean isAdmin = false;
    private String chatPageConId = "";
    private Application application;
    private UserActivityLifecycleCallbacks mLifecycleCallbacks = new UserActivityLifecycleCallbacks();
    private EMClientRepository clientRepository;

    private EaseIMHelper() {}

    public static EaseIMHelper getInstance() {
        if(mInstance == null) {
            synchronized (EaseIMHelper.class) {
                if(mInstance == null) {
                    mInstance = new EaseIMHelper();
                }
            }
        }
        return mInstance;
    }

    public Application getApplication(){
        return application;
    }

    private void registerActivityLifecycleCallbacks() {
        application.registerActivityLifecycleCallbacks(mLifecycleCallbacks);
    }

    public UserActivityLifecycleCallbacks getLifecycleCallbacks() {
        return mLifecycleCallbacks;
    }

    public void init(Application application){
        this.application = application;
        // ?????????PreferenceManager
        PreferenceManager.init(application);
    }

    public void initSDK(Context context, boolean isAdmin) {
        this.isAdmin = isAdmin;
        demoModel = new DemoModel(context);
        clientRepository = new EMClientRepository();
        //?????????IM SDK
        if(initSDK(context)) {
            // debug mode, you'd better set it to false, if you want release your App officially.
            EMClient.getInstance().setDebugMode(true);
            // set Call options
            setCallOptions(context);
            //???????????????
            initPush(context);
            //??????call Receiver
            //initReceiver(context);
            //?????????ease ui??????
            initEaseUI(context);
            //??????????????????
            registerConversationType();

            //callKit?????????
            InitCallKit(context);

            //??????????????????????????????
            fetchUserInfoList = FetchUserInfoList.getInstance();
            fetchUserRunnable = new FetchUserRunnable();
            fetchUserTread = new Thread(fetchUserRunnable);
            fetchUserTread.start();

            registerActivityLifecycleCallbacks();
        }

    }


    /**
     * callKit?????????
     * @param context
     */
    private void InitCallKit(Context context){
        EaseCallKitConfig callKitConfig = new EaseCallKitConfig();
        //????????????????????????
        callKitConfig.setCallTimeOut(30 * 1000);
        //????????????AgoraAppId
        callKitConfig.setAgoraAppId("15cb0d28b87b425ea613fc46f7c9f974");
        callKitConfig.setEnableRTCToken(true);
        EaseCallKit.getInstance().init(context,callKitConfig);
        // Register the activities which you have registered in manifest
        EaseCallKit.getInstance().registerVideoCallClass(VideoCallActivity.class);
        EaseCallKit.getInstance().registerMultipleVideoClass(MultipleVideoActivity.class);
        addCallkitListener();
    }

    /**
     * ?????????SDK
     * @param context
     * @return
     */
    private boolean initSDK(Context context) {
        // ?????????????????????SDK????????????
        EMOptions options = initChatOptions(context);
        //??????????????????rest server???im server
//        options.setRestServer("a1-hsb.easemob.com");
//        options.setIMServer("106.75.100.247");
//        options.setImPort(6717);

//        options.setRestServer("a41.easemob.com");
//        options.setIMServer("msync-im-41-tls-test.easemob.com");
//        options.setImPort(6717);

        // ?????????SDK
        isSDKInit = EaseIM.getInstance().init(context, options);
        //??????????????????????????????????????????
        demoModel.setUserInfoTimeOut(30 * 60 * 1000);
        //??????????????????????????????
        updateTimeoutUsers();
        mainContext = context;
        return isSDKInit();
    }


    /**
     *??????????????????
     */
    private void registerConversationType() {
        EaseMessageTypeSetManager.getInstance()
                .addMessageType(EaseFileAdapterDelegate.class)             //??????
                .addMessageType(EaseImageAdapterDelegate.class)            //??????
                .addMessageType(EaseLocationAdapterDelegate.class)         //??????
                .addMessageType(EaseVideoAdapterDelegate.class)            //??????
                .addMessageType(EaseVoiceAdapterDelegate.class)            //??????
                .addMessageType(ChatNoticeAdapterDelegate.class)           //??????
                .setDefaultMessageType(EaseTextAdapterDelegate.class);       //??????
    }

    /**
     * ???????????????????????????
     * @return
     */
    public boolean isLoggedIn() {
        return getEMClient().isLoggedInBefore();
    }

    /**
     * ??????IM SDK????????????
     * @return
     */
    public EMClient getEMClient() {
        return EMClient.getInstance();
    }

    /**
     * ??????contact manager
     * @return
     */
    public EMContactManager getContactManager() {
        return getEMClient().contactManager();
    }

    /**
     * ??????group manager
     * @return
     */
    public EMGroupManager getGroupManager() {
        return getEMClient().groupManager();
    }

    /**
     * ??????chatroom manager
     * @return
     */
    public EMChatRoomManager getChatroomManager() {
        return getEMClient().chatroomManager();
    }


    /**
     * get EMChatManager
     * @return
     */
    public EMChatManager getChatManager() {
        return getEMClient().chatManager();
    }

    /**
     * get push manager
     * @return
     */
    public EMPushManager getPushManager() {
        return getEMClient().pushManager();
    }

    /**
     * get conversation
     * @param username
     * @param type
     * @param createIfNotExists
     * @return
     */
    public EMConversation getConversation(String username, EMConversation.EMConversationType type, boolean createIfNotExists) {
        return getChatManager().getConversation(username, type, createIfNotExists);
    }

    public String getCurrentUser() {
        return getEMClient().getCurrentUser();
    }

    /**
     * ChatPresenter????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param context
     */
    private void initEaseUI(Context context) {
        //??????ChatPresenter,ChatPresenter???????????????????????????????????????
        EaseIM.getInstance().addChatPresenter(ChatPresenter.getInstance());
        EaseIM.getInstance()
                .setSettingsProvider(new EaseSettingsProvider() {
                    @Override
                    public boolean isMsgNotifyAllowed(EMMessage message) {
                        if(message == null){
                            return demoModel.getSettingMsgNotification();
                        }
                        if(!demoModel.getSettingMsgNotification()){
                            return false;
                        }else{
                            String chatUsename = null;
                            List<String> notNotifyIds = null;
                            // get user or group id which was blocked to show message notifications
                            if (message.getChatType() == EMMessage.ChatType.Chat) {
                                chatUsename = message.getFrom();
                                notNotifyIds = demoModel.getDisabledIds();
                            } else {
                                chatUsename = message.getTo();
                                notNotifyIds = demoModel.getDisabledGroups();
                            }

                            if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }

                    @Override
                    public boolean isMsgSoundAllowed(EMMessage message) {
                        return demoModel.getSettingMsgSound();
                    }

                    @Override
                    public boolean isMsgVibrateAllowed(EMMessage message) {
                        return demoModel.getSettingMsgVibrate();
                    }

                    @Override
                    public boolean isSpeakerOpened() {
                        return demoModel.getSettingMsgSpeaker();
                    }
                })
                .setEmojiconInfoProvider(new EaseEmojiconInfoProvider() {
                    @Override
                    public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
                        EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
                        for(EaseEmojicon emojicon : data.getEmojiconList()){
                            if(emojicon.getIdentityCode().equals(emojiconIdentityCode)){
                                return emojicon;
                            }
                        }
                        return null;
                    }

                    @Override
                    public Map<String, Object> getTextEmojiconMapping() {
                        return null;
                    }
                })
                .setAvatarOptions(getAvatarOptions())
                .setUserProvider(new EaseUserProfileProvider() {
                    @Override
                    public EaseUser getUser(String username) {
                        return getUserInfo(username);
                    }

                });
    }

    //Translation Manager ?????????
    public void initTranslationManager() {
        EMTranslateParams params = new EMTranslateParams("46c34219512d4f09ae6f8e04c083b7a3", "https://api.cognitive.microsofttranslator.com", 500);

        EMClient.getInstance().translationManager().init(params);
    }

    /**
     * ??????????????????
     * @return
     */
    private EaseAvatarOptions getAvatarOptions() {
        EaseAvatarOptions avatarOptions = new EaseAvatarOptions();
        avatarOptions.setAvatarShape(1);
        return avatarOptions;
    }

    public EaseUser getUserInfo(String username) {
        // To get instance of EaseUser, here we get it from the user list in memory
        // You'd better cache it if you get it from your server
        EaseUser user = null;
        if(username.equals(EMClient.getInstance().getCurrentUser()))
            return getUserProfileManager().getCurrentUserInfo();
        user = getContactList().get(username);
        if(user == null){
            //??????????????????????????? ????????????
            updateContactList();
            user = getContactList().get(username);
            //?????????????????????????????????????????? ????????????UI????????????
            if(user == null){
                if(fetchUserInfoList != null){
                    fetchUserInfoList.addUserId(username);
                }
            }
        }
        return user;
    }


    /**
     * ?????????????????????????????????
     * @param context
     * @return
     */
    private EMOptions initChatOptions(Context context){
        Log.d(TAG, "init HuanXin Options");

        EMOptions options = new EMOptions();
        // ???????????????????????????????????????,?????????true
        options.setAcceptInvitationAlways(false);
        // ???????????????????????????????????????
        options.setRequireAck(true);
        // ???????????????????????????????????????,??????false
        options.setRequireDeliveryAck(false);
        //??????fpa???????????????false
        options.setFpaEnable(true);

        /**
         * NOTE:????????????????????????????????????????????????????????????????????????????????????
         */
        EMPushConfig.Builder builder = new EMPushConfig.Builder(context);

        builder.enableVivoPush() // ?????????AndroidManifest.xml?????????appId???appKey
                .enableMeiZuPush("134952", "f00e7e8499a549e09731a60a4da399e3")
                .enableMiPush("2882303761517426801", "5381742660801")
                .enableOppoPush("0bb597c5e9234f3ab9f821adbeceecdb",
                        "cd93056d03e1418eaa6c3faf10fd7537")
                .enableHWPush() // ?????????AndroidManifest.xml?????????appId
                .enableFCM("782795210914");
        options.setPushConfig(builder.build());

        //set custom servers, commonly used in private deployment
        if(demoModel.isCustomSetEnable()) {
            if(demoModel.isCustomServerEnable() && demoModel.getRestServer() != null && demoModel.getIMServer() != null) {
                // ??????rest server??????
                options.setRestServer(demoModel.getRestServer());
                // ??????im server??????
                options.setIMServer(demoModel.getIMServer());
                //??????im server????????????????????????
                if(demoModel.getIMServer().contains(":")) {
                    options.setIMServer(demoModel.getIMServer().split(":")[0]);
                    // ??????im server ??????????????????443
                    options.setImPort(Integer.valueOf(demoModel.getIMServer().split(":")[1]));
                }else {
                    //????????????????????????
                    if(demoModel.getIMServerPort() != 0) {
                        options.setImPort(demoModel.getIMServerPort());
                    }
                }
            }
        }
        if (demoModel.isCustomAppkeyEnabled() && !TextUtils.isEmpty(demoModel.getCutomAppkey())) {
            // ??????appkey
            options.setAppKey(demoModel.getCutomAppkey());
        }

        String imServer = options.getImServer();
        String restServer = options.getRestServer();

        // ???????????????????????????owner???????????????????????????????????????owner???????????????????????????
        options.allowChatroomOwnerLeave(demoModel.isChatroomOwnerLeaveAllowed());
        // ????????????(?????????????????????)?????????????????????????????????
        options.setDeleteMessagesAsExitGroup(demoModel.isDeleteMessagesAsExitGroup());
        // ????????????????????????????????????
        options.setAutoAcceptGroupInvitation(demoModel.isAutoAcceptGroupInvitation());
        // ???????????????????????????????????????????????????????????????True????????????????????????????????????
        options.setAutoTransferMessageAttachments(demoModel.isSetTransferFileByUser());
        // ???????????????????????????????????????true???????????????
        options.setAutoDownloadThumbnail(demoModel.isSetAutodownloadThumbnail());
        return options;
    }

    private void setCallOptions(Context context) {
        HeadsetReceiver headsetReceiver = new HeadsetReceiver();
        IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(headsetReceiver, headsetFilter);
    }

    public void initPush(Context context) {
        if(EaseIM.getInstance().isMainProcess(context)) {
            //OPPO SDK?????????2.1.0????????????????????????
            HeytapPushManager.init(context, true);
            //HMSPushHelper.getInstance().initHMSAgent(DemoApplication.getInstance());
            EMPushHelper.getInstance().setPushListener(new PushListener() {
                @Override
                public void onError(EMPushType pushType, long errorCode) {
                    // TODO: ?????????errorCode???9xx??????????????????????????????EMError?????????????????????????????????pushType???????????????????????????????????????
                    EMLog.e("PushClient", "Push client occur a error: " + pushType + " - " + errorCode);
                }

                @Override
                public boolean isSupportPush(EMPushType pushType, EMPushConfig pushConfig) {
                    // ?????????????????????????????????????????????FCM??????
                    if(pushType == EMPushType.FCM){
                        EMLog.d("FCM", "GooglePlayServiceCode:"+GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context));
                        return demoModel.isUseFCM() && GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
                    }
                    return super.isSupportPush(pushType, pushConfig);
                }
            });
        }
    }

    /**
     * logout
     *
     * @param unbindDeviceToken
     *            whether you need unbind your device token
     * @param callback
     *            callback
     */
    public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
        Log.d(TAG, "logout: " + unbindDeviceToken);
        if(fetchUserTread != null && fetchUserRunnable != null){
            fetchUserRunnable.setStop(true);
        }

        CallCancelEvent cancelEvent = new CallCancelEvent();
        EaseCallKit.getInstance().sendCmdMsg(cancelEvent, EaseCallKit.getInstance().getFromUserId(), new EMCallBack() {
            @Override
            public void onSuccess() {
                EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        logoutSuccess();
                        //reset();
                        if (callback != null) {
                            callback.onSuccess();
                        }

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                        if (callback != null) {
                            callback.onProgress(progress, status);
                        }
                    }

                    @Override
                    public void onError(int code, String error) {
                        Log.d(TAG, "logout: onSuccess");
                        //reset();
                        if (callback != null) {
                            callback.onError(code, error);
                        }
                    }
                });
            }

            @Override
            public void onError(int code, String error) {
                EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        logoutSuccess();
                        //reset();
                        if (callback != null) {
                            callback.onSuccess();
                        }

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                        if (callback != null) {
                            callback.onProgress(progress, status);
                        }
                    }

                    @Override
                    public void onError(int code, String error) {
                        Log.d(TAG, "logout: onSuccess");
                        //reset();
                        if (callback != null) {
                            callback.onError(code, error);
                        }
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    /**
     * ??????????????????
     */
    public void killApp() {
//        List<Activity> activities = DemoApplication.getInstance().getLifecycleCallbacks().getActivityList();
//        if(activities != null && !activities.isEmpty()) {
//            for(Activity activity : activities) {
//                activity.finish();
//            }
//        }
//        Process.killProcess(Process.myPid());
//        System.exit(0);
    }



    /**
     * ?????????????????????????????????????????????
     */
    public void logoutSuccess() {
        Log.d(TAG, "logout: onSuccess");
        setAutoLogin(false);
        DemoDbHelper.getInstance(application).closeDb();
        getUserProfileManager().reset();
        EMClient.getInstance().translationManager().logout();
    }

    public EaseAvatarOptions getEaseAvatarOptions() {
        return EaseIM.getInstance().getAvatarOptions();
    }

    public DemoModel getModel(){
        if(demoModel == null) {
            demoModel = new DemoModel(mainContext);
        }
        return demoModel;
    }

    public String getCurrentLoginUser() {
        return getModel().getCurrentUsername();
    }

    /**
     * get instance of EaseNotifier
     * @return
     */
    public EaseNotifier getNotifier(){
        return EaseIM.getInstance().getNotifier();
    }

    /**
     * ???????????????????????????????????????
     * @param autoLogin
     */
    public void setAutoLogin(boolean autoLogin) {
        PreferenceManager.getInstance().setAutoLogin(autoLogin);
    }

    /**
     * ???????????????????????????????????????
     * @return
     */
    public boolean getAutoLogin() {
        return PreferenceManager.getInstance().getAutoLogin();
    }

    /**
     * ??????SDK???????????????
     * @param init
     */
    public void setSDKInit(boolean init) {
        isSDKInit = init;
    }

    public boolean isSDKInit() {
        return isSDKInit;
    }

    /**
     * ???????????????????????????
     * @param object
     */
    public void insert(Object object) {
        demoModel.insert(object);
    }

    /**
     * update
     * @param object
     */
    public void update(Object object) {
        demoModel.update(object);
    }

    /**
     * update user list
     * @param users
     */
    public void updateUserList(List<EaseUser> users){
        demoModel.updateContactList(users);
    }

    /**
     * ?????????????????????????????????
     */
    public void updateTimeoutUsers() {
        List<String> userIds = demoModel.selectTimeOutUsers();
        if(userIds != null && userIds.size() > 0){
            if(fetchUserInfoList != null){
                for(int i = 0; i < userIds.size(); i++){
                    fetchUserInfoList.addUserId(userIds.get(i));
                }
            }
        }
    }

    /**
     * get contact list
     *
     * @return
     */
    public Map<String, EaseUser> getContactList() {
        if (isLoggedIn() && contactList == null) {
            updateTimeoutUsers();
            contactList = demoModel.getAllUserList();
        }

        // return a empty non-null object to avoid app crash
        if(contactList == null){
            return new Hashtable<String, EaseUser>();
        }
        return contactList;
    }

    /**
     * update contact list
     */
    public void updateContactList() {
        if(isLoggedIn()) {
            updateTimeoutUsers();
            contactList = demoModel.getContactList();
        }
    }

    public UserProfileManager getUserProfileManager() {
        if (userProManager == null) {
            userProManager = new UserProfileManager();
        }
        return userProManager;
    }

    /**
     * ????????????????????????
     */
    public void showNotificationPermissionDialog() {
        EMPushType pushType = EMPushHelper.getInstance().getPushType();
        // oppo
        if(pushType == EMPushType.OPPOPUSH && HeytapPushManager.isSupportPush(mainContext)) {
            HeytapPushManager.requestNotificationPermission();
        }
    }

    /**
     * ???????????????
     * @param username
     * @return
     */
    public synchronized int deleteContact(String username) {
        if(TextUtils.isEmpty(username)) {
            return 0;
        }
        DemoDbHelper helper = DemoDbHelper.getInstance(application);
        if(helper.getUserDao() == null) {
            return 0;
        }
        int num = helper.getUserDao().deleteUser(username);
        if(helper.getInviteMessageDao() != null) {
            helper.getInviteMessageDao().deleteByFrom(username);
        }
        EMClient.getInstance().chatManager().deleteConversation(username, false);
        getModel().deleteUsername(username, false);
        Log.e(TAG, "delete num = "+num);
        return num;
    }

    /**
     * ????????????????????????????????????
     * ????????????true, ????????????api???????????????????????????????????????false.
     * @return
     */
    public boolean isFirstInstall() {
        return getModel().isFirstInstall();
    }

    /**
     * ??????????????????????????????????????????????????????????????????api?????????
     * ?????????????????????????????????????????????true
     */
    public void makeNotFirstInstall() {
        getModel().makeNotFirstInstall();
    }

    /**
     * ????????????????????????????????????????????????
     * @return
     */
    public boolean isConComeFromServer() {
        return getModel().isConComeFromServer();
    }

    /**
     * Determine if it is from the current user account of another device
     * @param username
     * @return
     */
    public boolean isCurrentUserFromOtherDevice(String username) {
        if(TextUtils.isEmpty(username)) {
            return false;
        }
        if(username.contains("/") && username.contains(EMClient.getInstance().getCurrentUser())) {
            return true;
        }
        return false;
    }


    /**
     * ??????EaseCallkit??????
     *
     */
    public void addCallkitListener(){
        callKitListener = new EaseCallKitListener() {
            @Override
            public void onInviteUsers(Context context,String userId[],JSONObject ext) {
                Intent intent = new Intent(context, ConferenceInviteActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
                String groupId = null;
                if(ext != null && ext.length() > 0){
                    try {
                        groupId = ext.getString("groupId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                intent.putExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_ID, groupId);
                intent.putExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_EXIST_MEMBERS, userId);
                context.startActivity(intent);
            }

            @Override
            public void onEndCallWithReason(EaseCallType callType, String channelName, EaseCallEndReason reason, long callTime) {
                EMLog.d(TAG,"onEndCallWithReason" + (callType != null ? callType.name() : " callType is null ") + " reason:" + reason + " time:"+ callTime);
                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String callString = mainContext.getString(R.string.call_duration);
                callString += formatter.format(callTime);

                Toast.makeText(mainContext,callString,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGenerateToken(String userId, String channelName, String appKey, EaseCallKitTokenCallback callback){
                EMLog.d(TAG,"onGenerateToken userId:" + userId + " channelName:" + channelName + " appKey:"+ appKey);
                String url = tokenUrl;
                url += "?";
                url += "userAccount=";
                url += userId;
                url += "&channelName=";
                url += channelName;
                url += "&appkey=";
                url +=  appKey;

                //????????????Token
                getRtcToken(url, callback);
            }

            @Override
            public void onReceivedCall(EaseCallType callType, String fromUserId,JSONObject ext) {
                //??????????????????
                EMLog.d(TAG,"onRecivedCall" + callType.name() + " fromUserId:" + fromUserId);
            }
            @Override
            public  void onCallError(EaseCallKit.EaseCallError type, int errorCode, String description){

            }

            @Override
            public void onInViteCallMessageSent(){
//                LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));
            }

            @Override
            public void onRemoteUserJoinChannel(String channelName, String userName, int uid, EaseGetUserAccountCallback callback){
                if(userName == null || userName == ""){
                    String url = uIdUrl;
                    url += "?";
                    url += "channelName=";
                    url += channelName;
                    url += "&userAccount=";
                    url += EMClient.getInstance().getCurrentUser();
                    url += "&appkey=";
                    url +=  EMClient.getInstance().getOptions().getAppKey();
                    getUserIdAgoraUid(uid,url,callback);
                }else{
                    //?????????????????? ??????
                    setEaseCallKitUserInfo(userName);
                    EaseUserAccount account = new EaseUserAccount(uid,userName);
                    List<EaseUserAccount> accounts = new ArrayList<>();
                    accounts.add(account);
                    callback.onUserAccount(accounts);
                }
            }
        };
        EaseCallKit.getInstance().setCallKitListener(callKitListener);
    }


    /**
     * ????????????Token
     *
     */
    private void getRtcToken(String tokenUrl,EaseCallKitTokenCallback callback){
        new AsyncTask<String, Void, Pair<Integer, String>>(){
            @Override
            protected Pair<Integer, String> doInBackground(String... str) {
                try {
                    Pair<Integer, String> response = EMHttpClient.getInstance().sendRequestWithToken(tokenUrl, null,EMHttpClient.GET);
                    return response;
                }catch (HyphenateException exception) {
                    exception.printStackTrace();
                }
                return  null;
            }
            @Override
            protected void onPostExecute(Pair<Integer, String> response) {
                if(response != null) {
                    try {
                          int resCode = response.first;
                          if(resCode == 200){
                              String responseInfo = response.second;
                              if(responseInfo != null && responseInfo.length() > 0){
                                  try {
                                      JSONObject object = new JSONObject(responseInfo);
                                      String token = object.getString("accessToken");
                                      int uId = object.getInt("agoraUserId");

                                      //????????????????????????
                                      setEaseCallKitUserInfo(EMClient.getInstance().getCurrentUser());
                                      callback.onSetToken(token,uId);
                                  }catch (Exception e){
                                      e.getStackTrace();
                                  }
                              }else{
                                  callback.onGetTokenError(response.first,response.second);
                              }
                          }else{
                              callback.onGetTokenError(response.first,response.second);
                          }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    callback.onSetToken(null,0);
                }
            }
        }.execute(tokenUrl);
    }

    /**
     * ??????channelName?????????uId???????????????????????????UserId
     * @param uId
     * @param url
     * @param callback
     */
    private void getUserIdAgoraUid(int uId, String url, EaseGetUserAccountCallback callback){
        new AsyncTask<String, Void, Pair<Integer, String>>(){
            @Override
            protected Pair<Integer, String> doInBackground(String... str) {
                try {
                    Pair<Integer, String> response = EMHttpClient.getInstance().sendRequestWithToken(url, null,EMHttpClient.GET);
                    return response;
                }catch (HyphenateException exception) {
                    exception.printStackTrace();
                }
                return  null;
            }
            @Override
            protected void onPostExecute(Pair<Integer, String> response) {
                if(response != null) {
                    try {
                        int resCode = response.first;
                        if(resCode == 200){
                            String responseInfo = response.second;
                            List<EaseUserAccount> userAccounts = new ArrayList<>();
                            if(responseInfo != null && responseInfo.length() > 0){
                                try {
                                    JSONObject object = new JSONObject(responseInfo);
                                    JSONObject resToken = object.getJSONObject("result");
                                    Iterator it = resToken.keys();
                                    while(it.hasNext()) {
                                        String uIdStr = it.next().toString();
                                        int uid = 0;
                                        uid = Integer.valueOf(uIdStr).intValue();
                                        String username = resToken.optString(uIdStr);
                                        if(uid == uId){
                                            //????????????????????????userName ???????????????????????????
                                            setEaseCallKitUserInfo(username);
                                        }
                                        userAccounts.add(new EaseUserAccount(uid, username));
                                    }
                                    callback.onUserAccount(userAccounts);
                                }catch (Exception e){
                                    e.getStackTrace();
                                }
                            }else{
                                callback.onSetUserAccountError(response.first,response.second);
                            }
                        }else{
                            callback.onSetUserAccountError(response.first,response.second);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    callback.onSetUserAccountError(100,"response is null");
                }
            }
        }.execute(url);
    }


    /**
     * ??????callKit ??????????????????
     * @param userName
     */
    private void setEaseCallKitUserInfo(String userName){
        EaseUser user = getUserInfo(userName);
        EaseCallUserInfo userInfo = new EaseCallUserInfo();
        if(user != null){
            userInfo.setNickName(user.getNickname());
            userInfo.setHeadImage(user.getAvatar());
        }
        EaseCallKit.getInstance().getCallKitConfig().setUserInfo(userName,userInfo);
    }

    public String getChatPageConId() {
        return chatPageConId;
    }

    public void setChatPageConId(String chatPageConId) {
        this.chatPageConId = chatPageConId;
    }


    /**
     * data sync listener
     */
    public interface DataSyncListener {
        /**
         * sync complete
         * @param success true???data sync successful???false: failed to sync data
         */
        void onSyncComplete(boolean success);
    }

    public boolean isAdmin(){
        return isAdmin;
    }

    //??????
    public void loginChat(String username, String password, EMCallBack callBack){
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                EaseIMHelper.getInstance().getModel().setCurrentUserName(username);
                EaseIMHelper.getInstance().getModel().setCurrentUserPwd(password);
                clientRepository.loginSuccess();
                setAutoLogin(true);
                //todo:???????????????????????????????????????????????????????????????????????????????????????????????????



//        EMConversation conversation = EMClient.getInstance().chatManager().getConversation("186245684723713", EMConversation.EMConversationType.GroupChat, true);
//        String ext = conversation.getExtField();
//
//        try {
//            JSONObject extJson;
//            if(!ext.isEmpty()){
//                extJson = new JSONObject(ext);
//            } else {
//                extJson = new JSONObject();
//            }
//            extJson.put(EaseConstant.IS_EXCLUSIVE, 1);
//            conversation.setExtField(extJson.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

                callBack.onSuccess();
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(i, s);
            }
        });
    }

    // ???????????????????????????
    public int getExclusiveGroupsUnread(){
        int unread = 0;
        Map<String, EMConversation> map = EMClient.getInstance().chatManager().getAllConversations();
        for(EMConversation conversation : map.values()){
            if(isExclusiveGroup(conversation)){
                unread =+ conversation.getUnreadMsgCount();
            }
        }
        return unread;
    }

    // ??????????????????????????????
    public void getChatUnread(EMValueCallBack<Map<String, Integer>> callBack){
        int totalUnread = EMClient.getInstance().chatManager().getUnreadMessageCount();
        int exclusiveUnread = 0;
        Map<String, EMConversation> map = EMClient.getInstance().chatManager().getAllConversations();
        for(EMConversation conversation : map.values()){
            if(isExclusiveGroup(conversation)){
                exclusiveUnread =+ conversation.getUnreadMsgCount();
            }
        }
        int unread = totalUnread - exclusiveUnread;
        Map<String, Integer> result = new HashMap<>();
        result.put(EaseConstant.UNREAD_TOTAL, totalUnread);
        result.put(EaseConstant.UNREAD_EXCLUSIVE_GROUP, exclusiveUnread);
        result.put(EaseConstant.UNREAD_MY_CHAT, unread);
        callBack.onSuccess(result);
    }

    public void startChat(Context context, int conversationType){
        clientRepository.loginSuccess();
        if(isAdmin()){
            ConversationListActivity.actionStart(context, EaseConstant.CON_TYPE_ADMIN);
        } else {
            if(conversationType == EaseConstant.CON_TYPE_EXCLUSIVE){
                Map<String, EMConversation> map = EMClient.getInstance().chatManager().getAllConversations();
                List<EMConversation> list = new ArrayList<>();
                for(EMConversation conversation : map.values()){
                    if(isExclusiveGroup(conversation)){
                        list.add(conversation);
                    }
                }
                if(list.size() == 1){
                    ChatActivity.actionStart(context, list.get(0).conversationId(), EaseConstant.CHATTYPE_GROUP);
                } else {
                    ConversationListActivity.actionStart(context, conversationType);
                }
            } else {
                ConversationListActivity.actionStart(context, conversationType);
            }
        }
    }

    public boolean isExclusiveGroup(EMConversation conversation){
        String ext = conversation.getExtField();
        if(!ext.isEmpty() && conversation.getType() == EMConversation.EMConversationType.GroupChat){
            try {
                JSONObject extJson = new JSONObject(ext);
                int isExclusive = extJson.optInt(EaseConstant.IS_EXCLUSIVE);
                if(isExclusive == 1){
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
