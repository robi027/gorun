package com.robi027.gorun;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.robi027.gorun.adapter.RunAdapter;
import com.robi027.gorun.model.Run;
import com.robi027.gorun.model.RunData;
import com.robi027.gorun.service.RunService;
import com.robi027.gorun.util.PrefUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = HistoryActivity.class.getName();
    private ArrayList<RunData> runData = new ArrayList<>();
    private RunAdapter adapter;
    private RecyclerView rvRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvRun = findViewById(R.id.rvRunning);

        read();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void read(){

        rvRun.setLayoutManager(new LinearLayoutManager(this));

        String device = PrefUtil.getString(this, "id");

        RunService runService = new RunService(this);
        runService.read(device, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Run run = (Run) response.body();
                if (run != null){
                    if (!run.isError()){
                        runData = run.getRunDatas();
                        adapter = new RunAdapter(HistoryActivity.this, runData);
                        rvRun.setHasFixedSize(true);
                        rvRun.setItemViewCacheSize(20);
                        rvRun.setDrawingCacheEnabled(true);
                        rvRun.setAdapter(adapter);
                    }else {
                        Toast.makeText(HistoryActivity.this, run.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(HistoryActivity.this, R.string.failureMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(HistoryActivity.this, R.string.failureMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
