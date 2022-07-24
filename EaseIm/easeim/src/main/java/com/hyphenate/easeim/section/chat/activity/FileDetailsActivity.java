package com.hyphenate.easeim.section.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseCompat;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.TextFormater;

public class FileDetailsActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener{

    private EaseTitleBar titleBar;
    private AppCompatImageView fileTypeImg;
    private AppCompatTextView fileNameView;
    private AppCompatTextView fileSizeView;
    private ProgressBar progressBar;
    private AppCompatButton startBtn;
    private EMMessage message;
    private EMNormalFileMessageBody fileMessageBody;

    public static void actionStart(Context context, EMMessage message){
        Intent intent = new Intent(context, FileDetailsActivity.class);
        intent.putExtra("message", message);
        context.startActivity(intent);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_file_details;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        message = intent.getParcelableExtra("message");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        if(EaseIMHelper.getInstance().isAdmin()){
            titleBar.setLeftImageResource(R.drawable.icon_back_admin);
        }
        fileTypeImg = findViewById(R.id.file_type);
        fileNameView = findViewById(R.id.file_name);
        fileSizeView = findViewById(R.id.file_size);
        progressBar = findViewById(R.id.progress_bar);
        startBtn = findViewById(R.id.start_btn);
    }

    @Override
    protected void initData() {
        super.initData();
        if(message != null){
            fileMessageBody = (EMNormalFileMessageBody)message.getBody();
            String fileName = fileMessageBody.getFileName();
            Uri filePath = fileMessageBody.getLocalUri();
            fileNameView.setText(fileName);
            if(fileName.contains(".jpg") || fileName.contains(".JPG")
                    || fileName.contains(".png") || fileName.contains(".PNG")
                    || fileName.contains(".jpeg") || fileName.contains(".JPEG")){
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_img));
            } else if (fileName.contains(".doc")){
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_doc));
            } else if (fileName.contains(".exel") || fileName.contains(".xlsx")){
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_exel));
            } else if (fileName.contains(".pdf")){
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_pdf));
            } else if (fileName.contains(".txt")){
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_text));
            } else if (fileName.contains(".docx")){
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_docx));
            } else if (fileName.contains(".ppt")){
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_ppt));
            } else {
                fileTypeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_file_other));
            }

            fileSizeView.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
            if (EaseFileUtils.isFileExistByUri(this, filePath)) {
                startBtn.setText(getString(R.string.em_open_file));
            } else {
                startBtn.setText(getString(R.string.em_start_download));
            }
        }

    }

    @Override
    protected void initListener() {
        super.initListener();
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        EaseCompat.openFile(FileDetailsActivity.this, fileMessageBody.getLocalUri());
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        ToastUtils.showCenterToast("", getString(R.string.em_try_again_later), 0, Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(i);
                    }
                });
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.equals(getString(R.string.em_start_download), startBtn.getText().toString())){
                    if(EaseCommonUtils.isNetConnection(FileDetailsActivity.this)){
                        EMClient.getInstance().chatManager().downloadAttachment(message);
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        ToastUtils.showCenterToast("", getString(R.string.em_try_again_later), 0, Toast.LENGTH_SHORT);
                    }
                } else {
                    EaseCompat.openFile(FileDetailsActivity.this, fileMessageBody.getLocalUri());
                }
            }
        });
        titleBar.setOnBackPressListener(this);
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }
}
