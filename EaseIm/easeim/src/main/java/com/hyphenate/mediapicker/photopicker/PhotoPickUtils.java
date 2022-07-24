package com.hyphenate.mediapicker.photopicker;

import android.Manifest;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.hyphenate.mediapicker.config.Constant;
import com.hyphenate.mediapicker.config.MediaPickerConfig;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.filter.Filter;

import com.hyphenate.easeim.R;


/**
 * 照片选择器
 *
 * @author : BaoZhou
 * @date : 2018/7/12 10:02
 */
public class PhotoPickUtils {
    static public void getAllSelector(final Fragment fragment, final MediaPickerConfig config) {

        /**
         * RxPermissions 参考：https://blog.csdn.net/qq_43546258/article/details/107712028
         */
        RxPermissions rxPermissions = new RxPermissions(fragment);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(granted ->{
                    if(granted){
                        startMatisse(true, config, Matisse.from(fragment));
                    } else {
                        Toast.makeText(fragment.getContext(), R.string.em_permission_request_denied, Toast.LENGTH_LONG)
                        .show();
                    }
            }
        );
    }


    static public void getAllSelector(final FragmentActivity fragmentActivity, final MediaPickerConfig config) {

        startMatisse(true, config, Matisse.from(fragmentActivity));

        RxPermissions rxPermissions = new RxPermissions(fragmentActivity);
        rxPermissions.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(granted ->{
                    if(granted){
                        startMatisse(true, config, Matisse.from(fragmentActivity));
                    } else {
                        Toast.makeText(fragmentActivity, R.string.em_permission_request_denied, Toast.LENGTH_LONG)
                        .show();
                    }
                }

        );
    }

    private static void startMatisse(Boolean aBoolean, MediaPickerConfig config, Matisse matisse) {
        if (aBoolean) {
            if (config.getImageEngine() == null) {
                throw new IllegalArgumentException("ImageEngine cannot be null");
            }
            matisse.choose(config.getPhotoPickerMediaType())
                    .showSingleMediaType(config.getMaxVideoSelectable() == 0 || config.getMaxImageSelectable() == 0)
                    .theme(R.style.Matisse_Dracula)
                    .countable(config.isCountable())
                    .addFilter(new FileSizeFilter(config.getMaxWidth(), config.getMaxHeight(), config.getMaxVideoSize() * Filter.K * Filter.K, config.getMaxImageSize() * Filter.K * Filter.K, config.getMaxVideoLength()))
                    .maxSelectablePerMediaType(config.getMaxImageSelectable() == 0 ? 1 : config.getMaxImageSelectable()
                            , config.getMaxVideoSelectable() == 0 ? 1 : config.getMaxVideoSelectable())
                    .originalEnable(config.isOriginalEnable())
                    .maxOriginalSize(config.getMaxOriginalSize())
                    .imageEngine(config.getImageEngine())
                    .forResult(Constant.REQUEST_CODE_CHOOSE);

        }
    }

}


