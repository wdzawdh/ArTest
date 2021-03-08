package com.cw.artest.video;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cw.artest.R;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;


/**
 * 单独的视频播放页面
 */
public class PlayPickActivity extends AppCompatActivity {

    public final static String VIDEO_URL = "video_url";
    public final static String VIDEO_TITLE = "video_title";
    public final static String VIDEO_COVER = "video_cover";
    public final static String VIDEO_AUTO_PLAY = "video_auto_play";
    public final static String VIDEO_CACHE_WITH_PLAY = "video_cache_with_play";
    public final static String IMG_TRANSITION = "img_transition";

    SmartPickVideo videoPlayer;
    OrientationUtils orientationUtils;
    private String mVideoUrl;
    private String mTitle;
    private String mCover;
    private boolean mAutoPlay;
    private boolean mCacheWithPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity_play_pick);
        videoPlayer = findViewById(R.id.video_player);
        mVideoUrl = getIntent().getStringExtra(VIDEO_URL);
        mTitle = getIntent().getStringExtra(VIDEO_TITLE);
        mCover = getIntent().getStringExtra(VIDEO_COVER);
        mAutoPlay = getIntent().getBooleanExtra(VIDEO_AUTO_PLAY, false);
        mCacheWithPlay = getIntent().getBooleanExtra(VIDEO_CACHE_WITH_PLAY, true);
        init();
    }

    private void init() {
        videoPlayer.setUp(mVideoUrl, mCacheWithPlay, mTitle);

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(mCover).into(imageView);
        videoPlayer.setThumbImageView(imageView);

        //增加title
        videoPlayer.getTitleTextView().setVisibility(View.VISIBLE);

        //设置返回键
        videoPlayer.getBackButton().setVisibility(View.VISIBLE);

        //设置旋转
        orientationUtils = new OrientationUtils(this, videoPlayer);

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(true);

        //拖动进度条时，是否在 seekbar 开始部位显示拖动进度
        videoPlayer.setShowDragProgressTextOnSeekBar(true);

        //循环播放
        videoPlayer.setLooping(true);

        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
            }
        });

        //设置返回按键功能
        videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //过渡动画
        initTransition();

        if (mAutoPlay) {
            videoPlayer.startPlayLogic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoPlayer.getFullscreenButton().performClick();
            return;
        }
        //释放所有
        videoPlayer.setVideoAllCallBack(null);
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void initTransition() {
        postponeEnterTransition();
        ViewCompat.setTransitionName(videoPlayer, IMG_TRANSITION);
        startPostponedEnterTransition();
    }
}
