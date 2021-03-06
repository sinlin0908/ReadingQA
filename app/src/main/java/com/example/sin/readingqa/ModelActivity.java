package com.example.sin.readingqa;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
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


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;


public class ModelActivity extends AppCompatActivity {

    private String sid;
    private Socket clientSocket;
    private static final String URL = "http://140.123.97.121:8001";
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


    private TextToSpeech textToSpeech = null;
    private Context thisContext = this;

    private boolean isAsked = false;


    private String qid;

    private boolean isAskQuestion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         * @ author: Sin Lin
         * @ From CCU DM+ lab
         * */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        createTextToSpeech();

        getMessage();
        initUI();
        createClientSocket();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyClientSocket();
        destroyTextToSpeech();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initUI() {
        txtIsConnected = findViewById(R.id.txt_is_connected);
        imgPeople = findViewById(R.id.img_model);
        layoutModel = findViewById(R.id.layout_model);
    }

    private void finishModelActivity() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishModelActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getMessage() {
        Intent intent = getIntent();
        sid = intent.getStringExtra("sid");
    }

    private void showOneMoreTime() {
        if (!isAsked) {
            removeQuestionUI();

            isAsked = true;
            txtOneMoreTime = new TextView(thisContext);
            txtOneMoreTime.setText("要不要再來一次?");
            txtOneMoreTime.setTextSize(20);
            txtOneMoreTime.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            layoutModel.addView(txtOneMoreTime);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

            btnYes = new Button(thisContext);

            btnYes.setLayoutParams(params);
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

            btnNo.setLayoutParams(params);
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
        Intent intent = new Intent(ModelActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void refresh() {
        layoutModel.removeView(txtOneMoreTime);
        txtOneMoreTime = null;
        layoutModel.removeView(btnYes);
        btnYes = null;
        layoutModel.removeView(btnNo);
        btnNo = null;

        addQuestionUI();
        productQuestion();
    }

    private void addReadyUI() {

        txtReadyAsk = new TextView(thisContext);
        txtReadyAsk.setTextSize(20);
        txtReadyAsk.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        txtReadyAsk.setText("小朋友準備好要問問題了嗎？");
        layoutModel.addView(txtReadyAsk);

        Bundle param = new Bundle();
        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "uid");
        textToSpeech.speak("小朋友準備好要問問題了嗎？", TextToSpeech.QUEUE_FLUSH, param, "uid");

        btnVoice = new Button(thisContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        btnVoice.setLayoutParams(params);
        btnVoice.setText("語音");

        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceInput();
            }
        });

        layoutModel.addView(btnVoice);

    }

    private void removeAskUI() {

        layoutModel.removeView(txtReadyAsk);
        txtReadyAsk = null;


        layoutModel.removeView(btnVoice);
        btnVoice = null;


    }

    private void addQuestionUI() {
        btnVoice = new Button(thisContext);
        btnVoice.setText("語音");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        btnVoice.setLayoutParams(params);
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

    private void removeQuestionUI() {
        if (txtQuery != null && btnVoice != null) {
            layoutModel.removeView(txtQuery);
            txtQuery = null;
            layoutModel.removeView(btnVoice);
            btnVoice = null;
        }
    }

    private void productQuestion() {
        clientSocket.emit("question", sid);
    }

    /*====================
     *      TextToSpeech
     * ===================*/
    /*
     * @ author: Sin Lin
     * @ From CCU DM+ lab
     * */
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
                            .load(R.drawable.girl_gif)
                            .into(imgPeople);
                }
            });
        }

        @Override
        public void onDone(String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgPeople.setImageResource(R.drawable.girl);
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
    /*
     * @ author: Sin Lin
     * @ From CCU DM+ lab
     * */
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

    /*------- Voice Result -----*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userInput = result.get(0);

                    switch (userInput) {
                        case "我準備好了": case "準備好了":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    removeAskUI();
                                    addQuestionUI();
                                    productQuestion();
                                }
                            });
                            break;

                        default:

                            if (isAskQuestion){
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("qid", qid);
                                    jsonObject.put("userInput", userInput);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                clientSocket.emit("answer", jsonObject);
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        askFinishModelActivity();
                                    }

                                    private void askFinishModelActivity() {
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(ModelActivity.this);
                                        dialog.setTitle("訊息");
                                        dialog.setMessage("確定要離開問答嗎？");

                                        dialog.setPositiveButton("要", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                goToMainActivity();
                                            }
                                        });

                                        dialog.setNegativeButton("不要", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                isAskQuestion = false;
                                            }
                                        });

                                        dialog.show();
                                    }
                                });
                            }
                            break;
                    }
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
                .on("result", onGetResult)
                .on("response", onOpenResponse);
    }

    private void destroyClientSocket() {
        clientSocket.disconnect();

        clientSocket.off();
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
                    removeAskUI();
                    removeQuestionUI();
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
                    isAskQuestion = true;

                    String data = (String) args[0];

                    String[] temp = data.split(",");

                    String question = temp[0];
                    qid = temp[1];

                    txtQuery.setTextSize(20);
                    txtQuery.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    if (!question.equals("null") && !question.equals("")) {
                        if (question.contains("什麼什麼")) {
                            question = question + "\n請問小朋友空格要填入什麼答案呢？";
                            txtQuery.setText(updateString(question));

                        } else {
                            txtQuery.setText(question);
                        }

                        Bundle param = new Bundle();
                        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");

                        textToSpeech.speak("叮叮！ 題目來囉！\n"+question, TextToSpeech.QUEUE_FLUSH, param, "uid");
                    }
                }

                @NonNull
                private String updateString(String question) {   // split ','
                    String pattern = "什麼什麼";

                    for(int index = question.indexOf(pattern);index!=-1;index = question.indexOf(pattern))
                    {
                        question = question.substring(0, index - 1) + "___ ___" + question.substring(index + 5);
                    }
                    return question;
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

                    Bundle param = new Bundle();
                    param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "uid");

                    if (result.equals("yes")) {
                        textToSpeech.speak("恭喜你答對了", TextToSpeech.QUEUE_FLUSH, param, "uid");
                        showOneMoreTime();
                    } else {
                        textToSpeech.speak("不對喔，再試一次", TextToSpeech.QUEUE_FLUSH, param, "uid");
                    }
                }
            });
        }
    };

    private Emitter.Listener onOpenResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = args[0].toString();
                    String response;
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        response = jsonObject.getString("a_content");

                        Bundle param = new Bundle();
                        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "uid");
                        textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, param, "uid");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    showOneMoreTime();
                }
            });
        }
    };
}
