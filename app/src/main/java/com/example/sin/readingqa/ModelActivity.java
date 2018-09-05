package com.example.sin.readingqa;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;



import java.net.URISyntaxException;
import java.util.ArrayList;

import java.util.Locale;

public class ModelActivity extends AppCompatActivity {

    private String storyName;
    private Socket clientSocket;
    private static final String URL = "http://140.123.97.126:995";
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private TextView txtIsConnected;
    private TextView txtQuery;
    private TextView txtOneMoreTime;
    private ImageView imgPeople;
    private Button btnVoice;
    private Button btnYes;
    private Button btnNo;
    private LinearLayout layoutModel;


    private TextView txtReadyAsk;
    private Button btnAskY,btnAskN;

    private TextToSpeech textToSpeech = null;
    private Context thisContext = this;

    private boolean isAsked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);

        getMessage();
        initUI();
        createClientSocket();
        createTextToSpeech();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyClientSocket();
        destroyTextToSpeech();
    }

    private void initUI() {
        txtIsConnected = (TextView) findViewById(R.id.txt_is_connected);
        imgPeople = (ImageView) findViewById(R.id.img_model);
        layoutModel = (LinearLayout) findViewById(R.id.layout_model);
    }

    private void finishModelActivity() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finishModelActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getMessage() {
        Intent intent = getIntent();
        storyName = intent.getStringExtra("storyName");
    }

    private void showOneMoreTime() {
        if (!isAsked) {
            removeQuestionUI();

            isAsked = true;
            txtOneMoreTime = new TextView(thisContext);
            txtOneMoreTime.setText("要不要再來一次?");
//            Bundle param = new Bundle();
//            param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"uid");
//            textToSpeech.speak("小朋友要不要再來一次?",TextToSpeech.QUEUE_FLUSH,param,"uid");

            layoutModel.addView(txtOneMoreTime);

            btnYes = new Button(thisContext);
            btnYes.setText("要");
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isAsked = false;
                    refresh();
                }
            });
            layoutModel.addView(btnYes);

            btnNo = new Button(thisContext);
            btnNo.setText("不要");
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToMainActivity();
                }
            });
            layoutModel.addView(btnNo);
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(ModelActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void refresh() {
        layoutModel.removeView(txtOneMoreTime);
        layoutModel.removeView(btnYes);
        layoutModel.removeView(btnNo);

        addQuestionUI();
        productQuestion();
    }

    private void addReadyUI(){
        txtReadyAsk = new TextView(thisContext);
        txtReadyAsk.setText("小朋友準備好要問問題了嗎？");
        layoutModel.addView(txtReadyAsk);

        Bundle param = new Bundle();
        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"uid");
        textToSpeech.speak("小朋友準備好要問問題了嗎？",TextToSpeech.QUEUE_FLUSH,param,"uid");

        btnAskY = new Button(thisContext);
        btnAskY.setText("是,我準備好了");
        btnAskY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAskUI();
                addQuestionUI();
                productQuestion();
            }
        });

        btnAskN = new Button(thisContext);
        btnAskN.setText("還沒準備好");
        btnAskN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishModelActivity();
            }
        });

        layoutModel.addView(btnAskY);
        layoutModel.addView(btnAskN);
    }

    private void removeAskUI(){
        layoutModel.removeView(txtReadyAsk);
        layoutModel.removeView(btnAskY);
        layoutModel.removeView(btnAskN);
    }

    private void addQuestionUI() {
        btnVoice = new Button(thisContext);
        btnVoice.setText("語音");
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        txtQuery = new TextView(thisContext);

        layoutModel.addView(txtQuery);
        layoutModel.addView(btnVoice);
    }

    private void removeQuestionUI(){
        layoutModel.removeView(txtQuery);
        layoutModel.removeView(btnVoice);
    }

    private void productQuestion(){
        clientSocket.emit("question", storyName);
    }

    /*====================
     *      TextToSpeech
     * ===================*/
    private void createTextToSpeech() {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(thisContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    if (i == TextToSpeech.SUCCESS) {
                        Locale lang = Locale.CHINESE;

                        if (textToSpeech.isLanguageAvailable(lang) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            textToSpeech.setLanguage(lang);
                        }

                        textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                    }
                }
            });
        }
    }

    private void destroyTextToSpeech() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }

    /*------ GIF Animation  ------*/
    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(thisContext)
                            .asGif()
                            .load(R.drawable.people_gif)
                            .into(imgPeople);
                }
            });
        }

        @Override
        public void onDone(String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgPeople.setImageResource(R.drawable.people);
                }
            });
        }

        @Override
        public void onError(String s) {
        }
    };


    /*====================
     *      SpeechToText
     * ===================*/
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請開始說話！");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userInput = result.get(0);
                    clientSocket.emit("answer",userInput);
                }
                break;
        }
    }

    /*=====================
     *     Client Socket
     * =====================*/
    private void createClientSocket() {

        try {
            clientSocket = IO.socket(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        createSocketEvent();

        clientSocket.connect();

    }

    private void createSocketEvent() {
        clientSocket.on(Socket.EVENT_CONNECT, onConnect)
                .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                .on(Socket.EVENT_DISCONNECT, onDisconnect)
                .on("question", onGetQuestion)
                .on("result", onGetResult);
    }

    private void destroyClientSocket() {
        clientSocket.disconnect();

        clientSocket.off(Socket.EVENT_CONNECT, onConnect)
                .off(Socket.EVENT_CONNECT_ERROR, onConnectError)
                .off(Socket.EVENT_DISCONNECT, onDisconnect)
                .off("question", onGetQuestion)
                .off("result", onGetResult);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        /*
         * @ Send Story Name to Server
         * */
        @Override
        public void call(Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtIsConnected.setText("連線成功");
                    addReadyUI();
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtIsConnected.setText("連線失敗");
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(thisContext, "連線失敗", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onGetQuestion = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String question;

                    question = (String) args[0];

                    if (!question.equals("null") && !question.equals("")) {

                        txtQuery.setText(question);
                        Bundle param = new Bundle();
                        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"");

                        textToSpeech.speak(question, TextToSpeech.QUEUE_FLUSH, param,"uid");
                    }
                }
            });
        }
    };

    private Emitter.Listener onGetResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String result;

                    result = (String) args[0];

                    if (result.equals("yes")) {
                        Bundle param = new Bundle();
                        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"");
                        textToSpeech.speak("恭喜你答對了", TextToSpeech.QUEUE_FLUSH, param,"uid");
                        showOneMoreTime();
                    } else {
                        Bundle param = new Bundle();
                        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"");
                        textToSpeech.speak("不對喔，再試一次", TextToSpeech.QUEUE_FLUSH, param,"uid");
                    }
                }
            });
        }
    };
}
