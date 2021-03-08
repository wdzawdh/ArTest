package com.cw.artest.video;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cw.artest.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import androidx.core.content.ContextCompat;

/**
 * 无缝切换视频的DEMO
 * 这里是切换清晰度，稍微修改下也可以作为切换下一集等
 */
public class SmartPickVideo extends StandardGSYVideoPlayer {

    public boolean isMute;
    private ImageView mVolume;

    public SmartPickVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SmartPickVideo(Context context) {
        super(context);
    }

    public SmartPickVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        if (isMute) {
            mute();
        } else {
            unMute();
        }
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initView();
    }

    @Override
    public void release() {
        if (getCurrentState() != -1 && getCurrentState() != CURRENT_STATE_NORMAL) {
            //释放焦点(全屏播放返回时)
            abandonAudioFocus();
            //取消定时器任务，防止内存泄漏
            cancelProgressTimer();
            cancelDismissControlViewTimer();
            super.release();
        }
    }

    @Override
    public void onVideoPause() {
        if (getCurrentState() != -1 && getCurrentState() != CURRENT_STATE_NORMAL) {
            super.onVideoPause();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        startProgressTimer();
        startDismissControlViewTimer();
    }

    @Override
    public void setDisplay(Surface surface) {
        super.setDisplay(surface);
    }

    private void initView() {
        mVolume = (ImageView) findViewById(R.id.ivVolume);
        if (mVolume != null) {
            mVolume.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchMute(!isMute);
                }
            });
        }
    }

    public void switchMute(boolean isMute) {
        this.isMute = isMute;
        if (isMute) {
            mute();
        } else {
            unMute();
        }
    }

    public String getUrl() {
        return mUrl;
    }

    /**
     * 静音
     */
    private void mute() {
        if (null != getGSYVideoManager() && getGSYVideoManager().getPlayer() != null) {
            getGSYVideoManager().getPlayer().setNeedMute(true);
            mVolume.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_novolume));
        }
    }

    /**
     * 取消静音
     */
    private void unMute() {
        if (null != getGSYVideoManager() && getGSYVideoManager().getPlayer() != null) {
            getGSYVideoManager().getPlayer().setNeedMute(false);
            mVolume.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_volume));
        }
    }

    public void requestAudioFocus() {
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    public void abandonAudioFocus() {
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    public void clearFullscreenLayout() {
        super.clearFullscreenLayout();
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_sample_video_pick;
    }

    /**
     * 全屏时将对应处理参数逻辑赋给全屏播放器
     *
     * @param context   context
     * @param actionBar actionBar
     * @param statusBar statusBar
     */
    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        SmartPickVideo sampleVideo = (SmartPickVideo) super.startWindowFullscreen(context, actionBar, statusBar);
        sampleVideo.switchMute(isMute);
        requestAudioFocus();
        return sampleVideo;
    }

    /**
     * 推出全屏时将对应处理参数逻辑返回给非播放器
     *
     * @param oldF           oldF
     * @param vp             vp
     * @param gsyVideoPlayer gsyVideoPlayer
     */
    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer != null) {
            setUp(mOriginUrl, mCache, mCachePath, mTitle);
        }
    }
}
