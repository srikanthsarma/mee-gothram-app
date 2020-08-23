package com.example.mismatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    TextView output_txt1;
    EditText search_bar_input;
    String jsonOutput;
    Button change_page, search_button;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        output_txt1 = findViewById(R.id.output_txt1);
        output_txt1.setMovementMethod(new ScrollingMovementMethod());
        search_bar_input = findViewById(R.id.search_bar);
        change_page = findViewById(R.id.change_page);
        search_button = findViewById(R.id.search_button);

        search_bar_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                output_txt1.setText("");
            }
        });
        change_page.setOnClickListener(view -> {
            Intent intent = new Intent(this, addItems.class);
            startActivity(intent);
        });
        search_button.setOnClickListener(this::onClick);


    }


    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        } else {
            showDialog();
            return false;
        }
    }

    private void showDialog() {
        Toast.makeText(MainActivity.this, "NO INTERNET CONNECTIVITY", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to wifi or mobile data");
        builder.setCancelable(false);

        builder.setPositiveButton("Connect to WIFI", (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        builder.setNeutralButton("turn on mobile data", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)));
        builder.setNegativeButton("Quit APP", (dialog, id) -> MainActivity.this.finish());
        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressLint("SetTextI18n")
    private void onClick(View view) {
        if (MainActivity.this.isConnected(MainActivity.this)) {
            String surname = search_bar_input.getText().toString().toUpperCase();
            output_txt1.setText("LOADING.......");
            UIUtil.hideKeyboard(MainActivity.this);
            search_bar_input.setPressed(false);
            if (surname.isEmpty()) {
                output_txt1.setText("ENTER A SURNAME");
            } else {
                OkHttpClient client;
                String url = "https://script.google.com/macros/s/AKfycbxWVi8kzV6IltPuD-BWGKcWjlzQTmJFbGcD-iRxMl6eA6D6lVMr/exec?action=getdata&surname=";
                Request request = new Request.Builder()
                        .url(url + surname)
                        .build();

                         client = new OkHttpClient.Builder()
                                 .connectTimeout(10, TimeUnit.SECONDS)
                                 .writeTimeout(10, TimeUnit.SECONDS)
                                 .readTimeout(30, TimeUnit.SECONDS)
                                 .build();

                  


                OkHttpClient finalClient = client;
                finalClient.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        String eMessage = e.getMessage();
                        String eCause= String.valueOf(e.getCause());
                        assert eMessage != null;
                        Log.d("errMsg",eMessage+", "+eCause);
                        MainActivity.this.runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "coneection Failed", Toast.LENGTH_LONG).show();
                            search_bar_input.setText("");
                            output_txt1.setText("Could not connect to server :(\n\n"+eMessage+"\n\nPlease Try Again");
                        });


                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            jsonOutput = response.body().string();
                            MainActivity.this.runOnUiThread(() -> {

                                JSONArray reader = null;
                                try {
                                    reader = new JSONArray(jsonOutput);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                output_txt1.setText("");
                                assert reader != null;
                                for (int i = 0; i < reader.length(); i++) {
                                    String JsonString = null;
                                    try {
                                        JsonString = reader.getString(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d("testTxt", "test: " + reader);
                                    JSONObject data = null;
                                    try {
                                        assert JsonString != null;
                                        data = new JSONObject(JsonString);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    String status = null;
                                    try {
                                        assert data != null;
                                        status = data.getString("status");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    String result;
                                    assert status != null;
                                    if (status.equals("INVALID")) {
                                        result = "NOT A VALID SURNAME";

                                    } else {
                                        String Gothram = null;
                                        try {
                                            Gothram = data.getString("gothram");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        String Shaakha = null;
                                        try {
                                            Shaakha = data.getString("shaakha");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        String pravara = null;
                                        try {
                                            pravara = data.getString("pravara");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        result = "surname: " + surname + "\n" +
                                                "gothram: " + Gothram + "\n" +
                                                "shaakha: " + Shaakha + "\n" +
                                                "pravara: " + pravara + "\n\n";


                                        Log.d("string printing", "onSearch: " + Gothram + ", " + Shaakha + ", " + reader.length());
                                    }
                                    output_txt1.append(result);
                                }
                                search_bar_input.setText("");
                            });
                        }
                    }
                });


            }
        } else {
            Toast.makeText(MainActivity.this, "PLEASE CHECK YOUR CONNECTIVITY", Toast.LENGTH_LONG).show();

        }
    }
}


