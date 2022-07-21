package com.hyphenate.easeim.section.group.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.widget.EaseTitleBar;

public class GroupEditActivity extends BaseInitActivity {

    private EaseTitleBar titleBar;
    private AppCompatEditText editContent;
    private LinearLayout noteView;
    private AppCompatTextView systemNote;
    private AppCompatEditText serviceNote;

    private String title;
    private String systemContent;
    private String content;
    private String hint;
    private boolean canEdit;
    private boolean isShowNote;
    private int requestCode;

    public static void startEditForResult(Activity context, String title, String content, String hint, boolean canEdit, int requestCode){
        Intent intent = new Intent(context, GroupEditActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("hint", hint);
        intent.putExtra("canEdit", canEdit);
        intent.putExtra("content", content);
        intent.putExtra("isShowNote", false);
        context.startActivityForResult(intent, requestCode);
    }

    public static void startNoteForResult(Activity context, String title, String systemContent, String content, String hint, int requestCode){
        Intent intent = new Intent(context, GroupEditActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("hint", hint);
        intent.putExtra("canEdit", true);
        intent.putExtra("systemContent", systemContent);
        intent.putExtra("content", content);
        intent.putExtra("isShowNote", true);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.em_activity_group_edit;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        title = intent.getStringExtra("title");
        hint = intent.getStringExtra("hint");
        canEdit = intent.getBooleanExtra("canEdit", false);
        systemContent = intent.getStringExtra("systemContent");
        content = intent.getStringExtra("content");
        isShowNote = intent.getBooleanExtra("isShowNote", false);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        editContent = findViewById(R.id.et_content);
        noteView = findViewById(R.id.group_note_view);
        systemNote = findViewById(R.id.system_note);
        serviceNote = findViewById(R.id.service_note);

        if(isShowNote){
            noteView.setVisibility(View.VISIBLE);
            editContent.setVisibility(View.GONE);
            systemNote.setText(systemContent);
            if(TextUtils.isEmpty(content)){
                serviceNote.setHint(hint);
            } else {
                serviceNote.setText(content);
            }
            serviceNote.setEnabled(canEdit);
        } else {
            noteView.setVisibility(View.GONE);
            editContent.setVisibility(View.VISIBLE);
            if(TextUtils.isEmpty(content)){
                editContent.setHint(hint);
            } else {
                editContent.setText(content);
            }
            editContent.setEnabled(canEdit);
        }

        titleBar.setTitle(title);
        titleBar.getRightLayout().setVisibility(canEdit ? View.VISIBLE : View.GONE);
        if(canEdit){
            titleBar.getRightText().setTextColor(ContextCompat.getColor(this, R.color.search_close));
        }
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        if(canEdit){
            titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
                @Override
                public void onRightClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("content", content);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }

}
