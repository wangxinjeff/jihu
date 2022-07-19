package com.hyphenate.easeim.common.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.hyphenate.easeim.R;
import com.hyphenate.easeui.utils.EaseCommonUtils;

public class SearchBar extends LinearLayout implements View.OnClickListener {

    private Context mContext;

    private AppCompatEditText searchView;
    private AppCompatImageView searchEmpty;
    private AppCompatTextView searchClose;
    private AppCompatTextView searchStart;
    private AppCompatTextView searchTextView;
    private LinearLayout searchIconView;

    private OnSearchBarListener listener;

    private boolean showSearchText;

    public SearchBar(Context context) {
        super(context);
        mContext = context;
    }

    public SearchBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public SearchBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;

    }

    public void init(boolean showSearchText){
        this.showSearchText = showSearchText;
        setLayout();
        initView();
        initListener();
    }

    private void setLayout(){
        LayoutInflater.from(mContext).inflate(R.layout.ease_layout_search, this);
    }

    private void initView(){
        searchView = findViewById(R.id.search_et_view);
        searchEmpty = findViewById(R.id.search_empty);
        searchClose = findViewById(R.id.search_close);
        searchStart = findViewById(R.id.search_start);
        searchTextView = findViewById(R.id.search_tv_view);
        searchIconView = findViewById(R.id.search_icon_view);
    }

    private void initListener(){
        searchEmpty.setOnClickListener(this);
        searchClose.setOnClickListener(this);
        searchStart.setOnClickListener(this);
        searchTextView.setOnClickListener(this);
        searchIconView.setOnClickListener(this);

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() &&
                                KeyEvent.ACTION_DOWN == event.getAction()){
                    if(listener != null){
                        listener.onSearchContent(searchView.getText().toString());
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(showSearchText){
                    if(TextUtils.isEmpty(s.toString())){
                        searchStart.setVisibility(GONE);
                        searchClose.setVisibility(VISIBLE);
                    }else {
                        searchStart.setVisibility(VISIBLE);
                        searchClose.setVisibility(GONE);
                    }
                } else {
                    searchClose.setVisibility(VISIBLE);
                    searchStart.setVisibility(GONE);
                    if(listener != null){
                        listener.onSearchContent(s.toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_tv_view :
            case R.id.search_icon_view :
                searchTextView.setVisibility(View.GONE);
                searchIconView.setVisibility(View.GONE);
                EaseCommonUtils.showSoftKeyBoard(searchView);
                break;
            case R.id.search_empty:
                searchView.setText("");
                break;
            case R.id.search_start:
                if(showSearchText){
                    if(listener != null){
                        listener.onSearchContent(searchView.getText().toString());
                    }
                }
                break;
            case R.id.search_close:
                searchView.setText("");
                searchTextView.setVisibility(View.VISIBLE);
                searchIconView.setVisibility(View.VISIBLE);
                EaseCommonUtils.hideSoftKeyBoard(searchView);
                searchClose.setVisibility(View.VISIBLE);
                searchStart.setVisibility(View.GONE);
                break;
        }
    }

    public void setOnSearchBarListener(OnSearchBarListener listener){
        this.listener = listener;
    }

    public interface OnSearchBarListener{
        void onSearchContent(String text);
    }
}
