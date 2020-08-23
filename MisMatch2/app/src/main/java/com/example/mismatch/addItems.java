package com.example.mismatch;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static okhttp3.RequestBody.create;

public class addItems extends AppCompatActivity {
    static String gothram;
    public final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    EditText surname_input, gothram_input, shaakha_input, pravara_input;
    Button add_new_items;
    String json;
    String surname;
    String shaakha;
    String pravara;
    String baseUrl = "https://script.google.com/macros/s/AKfycbxWVi8kzV6IltPuD-BWGKcWjlzQTmJFbGcD-iRxMl6eA6D6lVMr/exec";
    private String ReturnResponse;

    public static String toJson(String surname, String gothram, String shaakha, String pravara) {
        return "{ 'action' : 'postdata', " +
                "'surname' : '" + surname + "'," +
                " 'gothram' : '" + gothram + "'," +
                " 'shaakha' : '" + shaakha + "'," +
                " 'pravara' : '" + pravara + "'" +
                "}";

    }

    public static String toUrl(String surname, String gothram, String shaakha, String pravara) {
        return "?action=postdata&surname=" + surname + "&gothram=" + gothram + "&shaakha=" + shaakha + "&pravara=" + pravara;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_items);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        surname_input = findViewById(R.id.surname_input);
        gothram_input = findViewById(R.id.gothram_input);
        shaakha_input = findViewById(R.id.shaakha_input);
        pravara_input = findViewById(R.id.pravara_input);
        add_new_items = findViewById(R.id.add_items_to_sheet);
        add_new_items.setOnClickListener(view -> {
            if (isConnected(this)) {
                Toast.makeText(this, "sending......", Toast.LENGTH_LONG).show();
                surname = surname_input.getText().toString().toUpperCase();
                gothram = gothram_input.getText().toString().toUpperCase();
                shaakha = shaakha_input.getText().toString().toUpperCase();
                pravara = pravara_input.getText().toString().toUpperCase();
                if ((surname.isEmpty() && gothram.isEmpty()) && (shaakha.isEmpty() && pravara.isEmpty())) {
                    Toast.makeText(this, "Please enter valid inputs", Toast.LENGTH_SHORT).show();

                } else {

                    json = toJson(surname, gothram, shaakha, pravara);
                    String url = baseUrl + toUrl(surname, gothram, shaakha, pravara);
                    String response = post(url, json);
                    Log.d("postResponse", "onResponse: " + response);

                }
            } else {
                Toast.makeText(this, "please check your inernet connection", Toast.LENGTH_SHORT).show();
            }
        });


    }

    String post(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Log.d("postBody", "postBody" + body);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ReturnResponse = "Adding data failed";

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    ReturnResponse = Objects.requireNonNull(response.body()).string();
                    addItems.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(addItems.this, ReturnResponse, Toast.LENGTH_LONG).show();
                            UIUtil.hideKeyboard(addItems.this);
                            surname_input.setText("");
                            gothram_input.setText("");
                            shaakha_input.setText("");
                            pravara_input.setText("");


                        }
                    });
                }

            }
        });
        return ReturnResponse;
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
        Toast.makeText(addItems.this, "NO INTERNET CONNECTIVITY", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to wifi or mobile data");
        builder.setCancelable(false);

        builder.setPositiveButton("Connect to WIFI", (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        builder.setNeutralButton("turn on mobile data", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)));
        builder.setNegativeButton("Quit APP", (dialog, id) -> addItems.this.finish());
        AlertDialog alert = builder.create();
        alert.show();
    }
}