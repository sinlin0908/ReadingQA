package com.example.sin.readingqa;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Button btnSubmit = null;
    private Spinner spinnerStories = null;

    private String[] storyNames;
    private String chosenStoryName;
    private HashMap<String, String> storiesToURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = MainActivity.this.getResources();
        storyNames = resources.getStringArray(R.array.stories);

        initMap();
        initUI();
    }

    private void initMap() {

        storiesToURL = new HashMap<String, String>();

        final String threePigURL = "https://www.youtube.com/watch?v=a6HyQ7m_sUU";
        final String smallRedURL = "https://www.youtube.com/watch?v=USJyn_EkI7A";
        final String sevenSheepURL = "https://www.youtube.com/watch?v=fW5l9vbpXlc";


        storiesToURL.put(storyNames[0], threePigURL);
        storiesToURL.put(storyNames[1], smallRedURL);
        storiesToURL.put(storyNames[2], sevenSheepURL);
    }
    private void initUI() {
        spinnerStories = (Spinner) findViewById(R.id.spinner_stories);

        final ArrayAdapter<CharSequence> stories = ArrayAdapter.createFromResource(getApplicationContext(), R.array.stories, R.layout.spinner_style);
        stories.setDropDownViewResource(R.layout.spinner_style);

        spinnerStories.setAdapter(stories);
        spinnerStories.setOnItemSelectedListener(spnOnItemSelected);

        btnSubmit = (Button) findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(btnOnclickListener);
    }

    /*========================
     * Spinner Select Listener
     * =======================*/
    private AdapterView.OnItemSelectedListener spnOnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            chosenStoryName = spinnerStories.getSelectedItem().toString().trim();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            chosenStoryName = null;
        }
    };


    /*============================
     *  Button Click Listener
     *============================*/
    private Button.OnClickListener btnOnclickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            startReadingActivity();
        }
    };

    /*======================
     * Go to ReadingActivity
     * =====================*/
    private void startReadingActivity() {
        Intent intent = new Intent(this, ReadingActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("storyName", chosenStoryName);
        bundle.putString("storyURL", (String) storiesToURL.get(chosenStoryName));

        intent.putExtras(bundle);

        /*---- lunch ReadingActivity ----*/
        startActivity(intent);
    }
}
