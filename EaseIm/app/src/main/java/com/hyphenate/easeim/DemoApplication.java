package com.hyphenate.easeim;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.hyphenate.easeim.common.interfaceOrImplement.UserActivityLifecycleCallbacks;
import com.hyphenate.easeim.common.utils.PreferenceManager;
import com.hyphenate.util.EMLog;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DemoApplication extends Application implements Thread.UncaughtExceptionHandler {
    private static DemoApplication instance;
    private UserActivityLifecycleCallbacks mLifecycleCallbacks = new UserActivityLifecycleCallbacks();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initThrowableHandler();
        initHx();
        registerActivityLifecycleCallbacks();
        closeAndroidPDialog();
    }

    private void initThrowableHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void initHx() {
        EaseIMHelper.getInstance().init(this);
        // init hx sdk
        if(EaseIMHelper.getInstance().getAutoLogin()) {
            EMLog.i("DemoApplication", "application initHx");
            EaseIMHelper.getInstance().initSDK(this, EaseIMHelper.getInstance().getModel().getAppMode());
        }

    }

    private void registerActivityLifecycleCallbacks() {
        this.registerActivityLifecycleCallbacks(mLifecycleCallbacks);
    }

    public static DemoApplication getInstance() {
        return instance;
    }

    public UserActivityLifecycleCallbacks getLifecycleCallbacks() {
        return mLifecycleCallbacks;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    static {
        //???????????????Header?????????
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                ClassicsHeader.REFRESH_HEADER_LASTTIME = context.getString(R.string.last_update);
                ClassicsHeader.REFRESH_HEADER_PULLDOWN = context.getString(R.string.pull_down);
                ClassicsHeader.REFRESH_HEADER_REFRESHING = context.getString(R.string.refreshing);
                ClassicsHeader.REFRESH_HEADER_RELEASE = context.getString(R.string.release_refresh);
                ClassicsHeader.REFRESH_HEADER_FINISH = context.getString(R.string.refresh_finish);
                ClassicsHeader.REFRESH_HEADER_FAILED = context.getString(R.string.refresh_failed);
                return new ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate);//???????????????Header???????????? ???????????????Header
            }
        });
        //???????????????Footer?????????
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                ClassicsFooter.REFRESH_FOOTER_LOADING = context.getString(R.string.be_loading);
                ClassicsFooter.REFRESH_FOOTER_FINISH = context.getString(R.string.loaded);
                ClassicsFooter.REFRESH_FOOTER_FAILED = context.getString(R.string.load_failed);
                //???????????????Footer???????????? BallPulseFooter
                return new ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate);
            }
        });
    }

    /**
     * ????????????5.0????????????vector??????
     */
    static {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        EMLog.e("demoApp", e.getMessage());

    }

    /**
     * ??????androidP ???????????????????????????????????????
     * ???????????????detected problems with api ???
     */
    private void closeAndroidPDialog(){
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            try {
                Class aClass = Class.forName("android.content.pm.PackageParser$Package");
                Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
                declaredConstructor.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Class cls = Class.forName("android.app.ActivityThread");
                Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
                declaredMethod.setAccessible(true);
                Object activityThread = declaredMethod.invoke(null);
                Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
                mHiddenApiWarningShown.setAccessible(true);
                mHiddenApiWarningShown.setBoolean(activityThread, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
