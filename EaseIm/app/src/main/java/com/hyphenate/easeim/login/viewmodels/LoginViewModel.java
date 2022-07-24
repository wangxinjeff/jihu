package com.hyphenate.easeim.login.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.hyphenate.easeim.common.net.Resource;
import com.hyphenate.easeim.common.livedatas.SingleSourceLiveData;
import com.hyphenate.easeim.common.repositories.EMClientRepository;


public class LoginViewModel extends AndroidViewModel {
    private EMClientRepository mRepository;
    private SingleSourceLiveData<Resource<String>> registerObservable;
    private SingleSourceLiveData<Integer> pageObservable;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        mRepository = new EMClientRepository();
        registerObservable = new SingleSourceLiveData<>();
        pageObservable = new SingleSourceLiveData<>();
    }

    /**
     * 获取页面跳转的observable
     * @return
     */
    public LiveData<Integer> getPageSelect() {
        return pageObservable;
    }

    /**
     * 设置跳转的页面
     * @param page
     */
    public void setPageSelect(int page) {
        pageObservable.setValue(page);
    }

    /**
     * 清理注册信息
     */
    public void clearRegisterInfo() {
        registerObservable.setValue(null);
    }

}
