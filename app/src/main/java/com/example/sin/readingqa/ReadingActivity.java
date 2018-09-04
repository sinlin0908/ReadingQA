package com.example.sin.readingqa;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;

import android.content.Intent;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class ReadingActivity extends YouTubeBaseActivity {
    private static String TAG = "ReadingActivity";

    private static final int RECOVERY_DIALOG_REQUEST = 10;

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer mYouTubePlayer;
    private Button btn_go_qa_false, btn_go_qa_true;
    private TextView txt_go_to_qa;
    LinearLayout linearLayout;

    boolean isAsked = false;

    private String storyName;
    private String storyURL;
    private String mVideoID;

    private static final int PORTRAIT_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
    private static final int LANDSCAPE_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

    private boolean mAutoRotation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        initUI();
        getMessage();
    }


    private void getMessage() {
        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            storyName = bundle.getString("storyName");
            storyURL = bundle.getString("storyURL");
            if (storyURL != null) {
                mVideoID = getVideoID(storyURL);

                if (mVideoID == null) {
                    Log.d(TAG, "Not catch video ID");
                    Toast.makeText(this,"No video id",Toast.LENGTH_LONG).show();
                    finishReadingActivity();
                }
            }
        }
    }

    private String getVideoID(String URL) {
        int pos = URL.indexOf("?v=");
        if (pos != -1) {
            String videoID = URL.substring(pos + "?v=".length());
            Log.d(TAG, "getVideoId: " + videoID);
            return videoID;
        }
        return null;
    }

    private void finishReadingActivity() {
        finish();
    }

    /*------- 返回鍵 -----*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            finishReadingActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initUI() {
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.view_youtube);
        youTubePlayerView.initialize(YouTubeConfig.getApiKey(), onInitializedListener);
        linearLayout = (LinearLayout) findViewById(R.id.layout);
        mAutoRotation = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
    }


    /*------------ Screen 旋轉 --------------*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mYouTubePlayer != null) {
                mYouTubePlayer.setFullscreen(true);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mYouTubePlayer != null) {
                mYouTubePlayer.setFullscreen(false);
            }
        }
    }



    /*============================
     *          YouTube
     * ===========================*/

    /*------------ Youtube Initialize Listener ------*/
    private YouTubePlayer.OnInitializedListener onInitializedListener = new YouTubePlayer.OnInitializedListener() {
        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
            Log.d(TAG, "onClick:Done initializing");
            mYouTubePlayer = youTubePlayer;

            youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
            youTubePlayer.setPlaybackEventListener(playbackEventListener);


            youTubePlayer.setOnFullscreenListener(onFullscreenListener);
            if (mAutoRotation) {
                youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                        | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                        | YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
                        | YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
            }else {
                youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                        | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                        | YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
            }

            if (!b) {
                youTubePlayer.cueVideo(mVideoID);
            }
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
            Log.d(TAG, "onClick: Failed to initialize");

            if (youTubeInitializationResult.isUserRecoverableError()) {
                youTubeInitializationResult.getErrorDialog(ReadingActivity.this, RECOVERY_DIALOG_REQUEST).show();
            } else {
                String errorMessage = "Youtube Error: " + youTubeInitializationResult.toString();
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                finishReadingActivity();
            }
        }
    };

    /*------------ YouTube Player state listener -------------*/
    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            if (!isAsked) {
                isAsked = true;

                mYouTubePlayer.setFullscreen(false);
                txt_go_to_qa = new TextView(getApplicationContext());
                txt_go_to_qa.setText("要不要做閱讀測驗?");
                txt_go_to_qa.setTextColor(Color.WHITE);
                linearLayout.addView(txt_go_to_qa);

                btn_go_qa_false = new Button(getApplicationContext());
                btn_go_qa_false.setText("不要");
                btn_go_qa_false.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finishReadingActivity();
                    }
                });
                linearLayout.addView(btn_go_qa_false);

                btn_go_qa_true = new Button(getApplicationContext());
                btn_go_qa_true.setText("要");
                btn_go_qa_true.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);

                        intent.putExtra("storyName", storyName);

                        startActivity(intent);
                    }
                });
                linearLayout.addView(btn_go_qa_true);
            }
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    };


    /*------------ YouTube Back Play Listener --------------*/
    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    /*------------ YouTube Full Screen Listener ------------*/
    private YouTubePlayer.OnFullscreenListener onFullscreenListener = new YouTubePlayer.OnFullscreenListener() {
        @Override
        public void onFullscreen(boolean b) {
            if (b){
                setRequestedOrientation(LANDSCAPE_ORIENTATION);
            }else {
                setRequestedOrientation(PORTRAIT_ORIENTATION);
            }
        }
    };

}
