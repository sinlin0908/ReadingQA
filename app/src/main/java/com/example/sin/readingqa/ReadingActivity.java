/*
 * @ author: Sin Lin
 * @ From CCU DM+ lab
 * */
package com.example.sin.readingqa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

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


    private boolean isAsked = false;

    private String sid;
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

    @Override
    protected void onResume() {
        super.onResume();
        isAsked = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mYouTubePlayer.release();
    }

    private void getMessage() {
        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            sid = bundle.getString("sid");
            storyURL = bundle.getString("storyURL");
            Log.d("YT get info", sid + " " + storyURL);
            if (storyURL != null) {
                mVideoID = getVideoID(storyURL);

                if (mVideoID == null) {
                    Log.d(TAG, "Not catch video ID");
                    Toast.makeText(this, "No video id", Toast.LENGTH_LONG).show();
                    finishReadingActivity();
                }
            }
        }
    }

    private String getVideoID(String URL) {
        String pattern = "youtu.be/";
        int pos = URL.indexOf(pattern);
        if (pos != -1) {
            String videoID = URL.substring(pos + pattern.length());
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishReadingActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initUI() {
        youTubePlayerView = findViewById(R.id.view_youtube);
        youTubePlayerView.initialize(YouTubeConfig.getApiKey(), onInitializedListener);

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

    /*
     * @ author: Sin Lin
     * @ From CCU DM+ lab
     * */

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
            } else {
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

                askGoToQA();

                mYouTubePlayer.seekToMillis(0);
                mYouTubePlayer.pause();



            }
        }

        private void askGoToQA() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(ReadingActivity.this);
            dialog.setTitle("訊息");
            dialog.setMessage("要做閱讀測驗嗎?");

            dialog.setPositiveButton("要", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(getApplicationContext(), ModelActivity.class);

                    intent.putExtra("sid", sid);

                    startActivity(intent);
                }
            });

            dialog.setNegativeButton("不要", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishReadingActivity();
                }
            });

            dialog.setNeutralButton("再看一次故事", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isAsked = false;
                }
            });

            dialog.show();
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
            if (b) {
                setRequestedOrientation(LANDSCAPE_ORIENTATION);
            } else {
                setRequestedOrientation(PORTRAIT_ORIENTATION);
            }
        }
    };

}
