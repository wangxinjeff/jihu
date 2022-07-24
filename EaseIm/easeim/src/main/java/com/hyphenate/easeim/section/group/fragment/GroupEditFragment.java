package com.hyphenate.easeim.section.group.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.hyphenate.easeim.EaseIMHelper;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.section.base.BaseActivity;
import com.hyphenate.easeim.section.base.BaseDialogFragment;
import com.hyphenate.easeui.utils.StatusBarCompat;
import com.hyphenate.easeui.widget.EaseTitleBar;

public class GroupEditFragment extends BaseDialogFragment implements EaseTitleBar.OnRightClickListener, EaseTitleBar.OnBackPressListener {
    private EaseTitleBar titleBar;
    private EditText etContent;
    private String content;
    private String hint;
    private OnSaveClickListener listener;
    private String title;
    private boolean canEdit;
    private LinearLayout editRoot;



    public static void showDialog(BaseActivity activity, String title, String content, String hint, OnSaveClickListener listener) {
        showDialog(activity, title, content, hint, true, listener);
    }

    public static void showDialog(BaseActivity activity, String title, String content, String hint, boolean canEdit, OnSaveClickListener listener) {
        GroupEditFragment fragment = new GroupEditFragment();
        fragment.setOnSaveClickListener(listener);
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("content", content);
        bundle.putString("hint", hint);
        bundle.putBoolean("canEdit", canEdit);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragment.show(transaction, null);
    }

    @Override
    public int getLayoutId() {
        return R.layout.demo_fragment_group_edit;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.CustomerTheme);
        StatusBarCompat.setLightStatusBar(mContext, true);
    }

    @Override
    public void onStart() {
        super.onStart();
        setDialogFullParams();
    }

    @Override
    public void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            title = bundle.getString("title");
            content = bundle.getString("content");
            hint = bundle.getString("hint");
            canEdit = bundle.getBoolean("canEdit");
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        etContent = findViewById(R.id.et_content);
        editRoot = findViewById(R.id.edit_root);
        if(EaseIMHelper.getInstance().isAdmin()){
            editRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_float_bg));
            titleBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_bg));
            titleBar.getTitle().setTextColor(ContextCompat.getColor(getContext(), R.color.normal_text));
            titleBar.getRightText().setTextColor(ContextCompat.getColor(getContext(), R.color.normal_text));
            titleBar.setLeftImageResource(R.drawable.icon_back_admin);
            etContent.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_float_bg));
            etContent.setTextColor(ContextCompat.getColor(getContext(), R.color.normal_text));
        } else {
            editRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_float_bg_color));
            titleBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_bg_color));
            titleBar.getTitle().setTextColor(ContextCompat.getColor(getContext(), R.color.normal_text_color));
            titleBar.getRightText().setTextColor(ContextCompat.getColor(getContext(), R.color.normal_text_color));
            etContent.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_float_bg_color));
            etContent.setTextColor(ContextCompat.getColor(getContext(), R.color.normal_text_color));
        }


        if(TextUtils.isEmpty(content)) {
            etContent.setHint(hint);
        }else {
            etContent.setText(content);
        }

        etContent.setEnabled(canEdit);
        titleBar.setRightLayoutVisibility(canEdit ? View.VISIBLE : View.GONE);

        titleBar.setTitle(title);
    }

    @Override
    public void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
        titleBar.setOnRightClickListener(this);
    }

    public void setOnSaveClickListener(OnSaveClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onRightClick(View view) {
        String content = etContent.getText().toString().trim();
        if(listener != null) {
            listener.onSaveClick(view, content);
        }
        dismiss();
    }

    @Override
    public void onBackPress(View view) {
        dismiss();
    }

    public interface OnSaveClickListener {
        void onSaveClick(View view, String content);
    }

}
