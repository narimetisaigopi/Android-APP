package com.lalamove;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;

import Adapters.HomeAdapter;
import DBFlow.LocalModel;
import DBFlow.Utilities;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    ProgressDialog progressDialog;

    Utilities utilities;


    RecyclerView recyclerView;

    HomeAdapter homeAdapter;

    TextView textView;
    AlertDialog.Builder builder = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        if (utilities.getAllData().size() == 0 && !utilities.isNetworkConnected()) {
            builder = utilities.alertDialog("offline data not available please connect to internet", "try again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } else if (utilities.getAllData().size() == 0) {
            new FetchData(this).execute(utilities.URL);
        } else {
            showRecyclerView();
        }


    }

    void initialize() {
        progressDialog = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerView);
        utilities = new Utilities(this);
        textView = findViewById(R.id.notDataTV);

        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCanceledOnTouchOutside(false);

        getSupportActionBar().setTitle("Things to Deliver");

    }

    void showRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        homeAdapter = new HomeAdapter(this, utilities.getAllData());
        recyclerView.setAdapter(homeAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (builder != null) {
            if (builder.create().isShowing()) {
                builder.create().dismiss();
            }
        }
    }


}

class FetchData extends AsyncTask<String, Integer, Integer> {
    private static final String TAG = "FetchData";
    private Context context;
    private ProgressDialog progressDialog;
    private Utilities utilities;
    private WeakReference<Activity> weakReference;

    public FetchData(Context context) {
        this.context = context;
        weakReference = new WeakReference<Activity>((Activity) context);
        utilities = new Utilities(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }


    @Override
    protected Integer doInBackground(String... strings) {
        OkHttpClient client = new OkHttpClient();
        String URL = strings[0];
        final Request request = new Request.Builder()
                .url(URL)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();

            String resp = null;
            try {
                resp = response.body().string();
                Log.d(TAG, "resp : " + resp);
                Log.d(TAG, "resp : " + response);

                if (response.code() == 200) {
                    try {
                        JSONArray jsonArray = new JSONArray(resp);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            LocalModel localModel = new LocalModel();
                            localModel.setItemID(jsonArray.getJSONObject(i).getString("id"));
                            localModel.setDescription(jsonArray.getJSONObject(i).getString("description"));
                            localModel.setImageUrl(jsonArray.getJSONObject(i).getString("imageUrl"));
                            localModel.setLat(jsonArray.getJSONObject(i).getJSONObject("location").getString("lat"));
                            localModel.setLng(jsonArray.getJSONObject(i).getJSONObject("location").getString("lng"));
                            localModel.setAddress(jsonArray.getJSONObject(i).getJSONObject("location").getString("address"));
                            Log.d(TAG, "localModel : " + localModel.toString());
                            localModel.save();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(context, "failed try again : " + response.message(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }


            } catch (IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

            progressDialog.dismiss();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.code();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        Log.d(TAG, "resp : " + integer);
        if (integer == 200) {
            Activity activity = weakReference.get();
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new HomeAdapter(context, utilities.getAllData()));
        } else {
            Toast.makeText(context, "failed to get data or server error : " + integer, Toast.LENGTH_SHORT).show();

            if (utilities.getAllData().size() == 0 && !utilities.isNetworkConnected()) {
                utilities.alertDialog("offline data not available please connect to internet", "try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    }
                });
            } else if (utilities.getAllData().size() == 0) {
                new FetchData(context).execute(utilities.URL);
            }
        }

    }


}





