package com.hyphenate.easeui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.hyphenate.EMError;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.manager.EaseVoiceRecorder;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVoicePlayer;
import com.hyphenate.util.EMLog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Voice recorder view
 *
 */
public class EaseVoiceRecorderView extends RelativeLayout {
    protected Context context;
    protected LayoutInflater inflater;
    protected Drawable[] micImages;
    protected EaseVoiceRecorder voiceRecorder;

    protected PowerManager.WakeLock wakeLock;
    protected ImageView ivIcon;
    protected ImageView micImage;
    protected TextView recordingHint;
    private TextView countdownHint;

    private Timer timer;
    private TimerTask timerTask;
    private int totalTime = 21;
    private EaseVoiceRecorderCallback recorderCallback;
    private View view;
    private int ENABLE_VIEW = 10001;

    protected Handler micImageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            // change image
            int index = msg.what;
            if(index == ENABLE_VIEW){
                view.setEnabled(true);
            } else {
                if (index < 0 || index > micImages.length - 1) {
                    return;
                }
                micImage.setImageDrawable(micImages[index]);
            }
        }
    };

    public EaseVoiceRecorderView(Context context) {
        super(context);
        init(context);
    }

    public EaseVoiceRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EaseVoiceRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_voice_recorder, this);

        ivIcon = findViewById(R.id.iv_icon);
        micImage = (ImageView) findViewById(R.id.mic_image);
        recordingHint = (TextView) findViewById(R.id.recording_hint);
        countdownHint = findViewById(R.id.countdown_hint);

        voiceRecorder = new EaseVoiceRecorder(micImageHandler);

        // animation resources, used for recording
        micImages = new Drawable[] {ContextCompat.getDrawable(context, R.drawable.ease_record_animate_01),
                ContextCompat.getDrawable(context, R.drawable.ease_record_animate_02),
                ContextCompat.getDrawable(context, R.drawable.ease_record_animate_03),
                ContextCompat.getDrawable(context, R.drawable.ease_record_animate_04),
                ContextCompat.getDrawable(context, R.drawable.ease_record_animate_05),
                ContextCompat.getDrawable(context, R.drawable.ease_record_animate_06),
                ContextCompat.getDrawable(context, R.drawable.ease_record_animate_07)};

        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
    }

    private void countdownHint(){
//        ToastUtils.showCenterToast("", "还有" + totalTime + " s 自动发送", 0, Toast.LENGTH_SHORT);
        EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                countdownHint.setVisibility(VISIBLE);
                countdownHint.setText(String.format(context.getString(R.string.countdown_send), totalTime + ""));
            }
        });
    }

    /**
     * on speak button touched
     * 
     * @param v
     * @param event
     */
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event, EaseVoiceRecorderCallback recorderCallback) {
        this.recorderCallback = recorderCallback;
        view = v;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            try {
                EaseChatRowVoicePlayer voicePlayer = EaseChatRowVoicePlayer.getInstance(context);
                if (voicePlayer.isPlaying())
                    voicePlayer.stop();
                v.setPressed(true);
                setTextContent(v, true);
                startRecording();
            } catch (Exception e) {
                v.setPressed(false);
            }
            return true;
        case MotionEvent.ACTION_MOVE:
            if (event.getY() < dip2px(getContext(), 10)) {
                setTextContent(v, false);
                showReleaseToCancelHint();
            } else {
                setTextContent(v, true);
                showMoveUpToCancelHint();
            }
            return true;
        case MotionEvent.ACTION_UP:
            v.setPressed(false);
            setTextContent(v, false);
            if (event.getY() < 0) {
                // discard the recorded audio.
                discardRecording();
            } else {
                // stop recording and send voice file
                endRecording();
            }
            return true;
        default:
            discardRecording();
            return false;
        }
    }

    private void endRecording(){
        // stop recording and send voice file
        view.setPressed(false);
        setTextContent(view, false);
        view.setEnabled(false);
        micImageHandler.sendEmptyMessageDelayed(ENABLE_VIEW, 1000);
        resetTimerTask();
        try {
            int length = stopRecoding();
            if (length > 0) {
                if (recorderCallback != null) {
                    recorderCallback.onVoiceRecordComplete(getVoiceFilePath(), length);
                }
            } else if (length == EMError.FILE_INVALID) {
                Toast.makeText(context, R.string.Recording_without_permission, Toast.LENGTH_SHORT).show();
            }
//            else {
//                Toast.makeText(context, R.string.The_recording_time_is_too_short, Toast.LENGTH_SHORT).show();
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.send_failure_please, Toast.LENGTH_SHORT).show();
        }
    }

    private void setTextContent(View view, boolean pressed) {
        if(view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 0) {
            View child = ((ViewGroup) view).getChildAt(0);
            if(child instanceof TextView) {
                ((TextView) child).setText(getContext().getString(pressed ? R.string.button_pushtotalk_pressed : R.string.button_pushtotalk));
            }
        }
    }

    public interface EaseVoiceRecorderCallback {
        /**
         * on voice record complete
         * 
         * @param voiceFilePath
         *            录音完毕后的文件路径
         * @param voiceTimeLength
         *            录音时长
         */
        void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength);
    }

    public void startRecording() {
        if (!EaseCommonUtils.isSdcardExist()) {
            Toast.makeText(context, R.string.Send_voice_need_sdcard_support, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            createTimerTask();
            wakeLock.acquire();
            this.setVisibility(View.VISIBLE);
            recordingHint.setText(context.getString(R.string.move_up_to_cancel));
            recordingHint.setBackgroundColor(Color.TRANSPARENT);
            ivIcon.setImageResource(R.drawable.ease_record_icon);
            micImage.setVisibility(VISIBLE);
            voiceRecorder.startRecording(context);
        } catch (Exception e) {
            resetTimerTask();
            e.printStackTrace();
            if (wakeLock.isHeld())
                wakeLock.release();
            if (voiceRecorder != null)
                voiceRecorder.discardRecording();
            this.setVisibility(View.INVISIBLE);
            Toast.makeText(context, R.string.recoding_fail, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void showReleaseToCancelHint() {
        recordingHint.setText(context.getString(R.string.release_to_cancel));
        //recordingHint.setBackgroundResource(R.drawable.ease_recording_text_hint_bg);
        ivIcon.setImageResource(R.drawable.ease_record_cancel);
        micImage.setVisibility(GONE);
    }

    public void showMoveUpToCancelHint() {
        recordingHint.setText(context.getString(R.string.move_up_to_cancel));
        recordingHint.setBackgroundColor(Color.TRANSPARENT);
        ivIcon.setImageResource(R.drawable.ease_record_icon);
        micImage.setVisibility(VISIBLE);
    }

    public void discardRecording() {
        resetTimerTask();
        if (wakeLock.isHeld())
            wakeLock.release();
        try {
            // stop recording
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                this.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
    }

    public int stopRecoding() {
        this.setVisibility(View.INVISIBLE);
        if (wakeLock.isHeld())
            wakeLock.release();
        return voiceRecorder.stopRecoding();
    }

    public String getVoiceFilePath() {
        return voiceRecorder.getVoiceFilePath();
    }

    public String getVoiceFileName() {
        return voiceRecorder.getVoiceFileName();
    }

    public boolean isRecording() {
        return voiceRecorder.isRecording();
    }

    /**
     * dip to px
     * @param context
     * @param value
     * @return
     */
    public static float dip2px(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    private void resetTimerTask(){
        timer.cancel();
        totalTime = 21;
        countdownHint.setVisibility(INVISIBLE);
    }

    private void createTimerTask(){
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                totalTime --;
                EMLog.e("time:", totalTime + "");
                if(totalTime < 10){
                    if(totalTime == 0 ){
                        endRecording();
                    } else {
                        countdownHint();
                    }
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }
}
