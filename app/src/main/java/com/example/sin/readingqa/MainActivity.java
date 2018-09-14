package com.example.sin.readingqa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private Button btnSubmit = null;
    private Spinner spinnerStories = null;


    private String chosenStoryName;


    private RequestQueue mQueue;
    private static final String url = "http://123";
    private JSONArray storiesInfoJsonArray;

    private String sid;
    private String storyUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQueue = Volley.newRequestQueue(this);
        jsonParse();

        initUI();


    }

    private void initUI() {
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReadingActivity();
            }
        });
    }

    private void jsonParse() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    storiesInfoJsonArray = response.getJSONArray("stories");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createSpinner();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    private void createSpinner() {
        spinnerStories = findViewById(R.id.spinner_stories);
        ArrayList<String> storyNameList = new ArrayList<>();

        try {
            for (int i = 0; i < storiesInfoJsonArray.length(); i++) {
                JSONObject jsonObject = storiesInfoJsonArray.getJSONObject(i);
                storyNameList.add(jsonObject.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ArrayAdapter<String> spinnerMenu = new ArrayAdapter<>(this, R.layout.spinner_style, storyNameList);


        spinnerStories.setAdapter(spinnerMenu);
        spinnerStories.setOnItemSelectedListener(spnOnItemSelected);
    }

    /*========================
     * Spinner Select Listener
     * =======================*/
    private AdapterView.OnItemSelectedListener spnOnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            chosenStoryName = spinnerStories.getSelectedItem().toString().trim();

            if (storiesInfoJsonArray != null) {
                for (int i = 0; i < storiesInfoJsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject = storiesInfoJsonArray.getJSONObject(i);
                        if (jsonObject.get("name").equals(chosenStoryName)) {
                            sid = jsonObject.getString("sid");
                            storyUrl = jsonObject.getString("URL");

                            Log.d("StoryInfo",sid+"   "+storyUrl);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e("JSON ARRAY", "NULL");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            chosenStoryName = null;
        }
    };


    /*======================
     * Go to ReadingActivity
     * =====================*/
    private void startReadingActivity() {
        Intent intent = new Intent(this, ReadingActivity.class);

        Bundle bundle = new Bundle();
        Log.d("put Info",sid+"  "+storyUrl);
        bundle.putString("sid", sid);
        bundle.putString("storyURL", storyUrl);

        intent.putExtras(bundle);

        /*---- lunch ReadingActivity ----*/
        startActivity(intent);
    }
}
