package com.robi027.gorun.service;

import android.content.Context;

import com.robi027.gorun.util.RetrofitBuilder;
import com.robi027.gorun.config.RunInterface;

import retrofit2.Callback;

/**
 * Created by robi on 4/30/2019.
 */

public class RunService {
    private RunInterface runInterface;

    public RunService(Context context) {
        runInterface = RetrofitBuilder.builder(context)
                .create(RunInterface.class);
    }

    public void read(String device, Callback callback){
        runInterface.read(device).enqueue(callback);
    }

    public void create(String device, String timer,
                       String lat_start, String lon_start,
                       String lat_end, String lon_end, Callback callback){
        runInterface.create(device, timer, lat_start, lon_start, lat_end, lon_end).enqueue(callback);
    }
}
