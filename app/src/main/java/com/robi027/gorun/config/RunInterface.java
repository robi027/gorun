package com.robi027.gorun.config;

import com.robi027.gorun.model.Run;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by robi on 4/30/2019.
 */

public interface RunInterface {

    @GET(WebService.API_READ)
    Call<Run> read(
            @Query("device") String device
    );

    @FormUrlEncoded
    @POST(WebService.API_CREATE)
    Call<Run> create(
            @Field("device") String device,
            @Field("timer") String timer,
            @Field("lat_start") String lat_start,
            @Field("lon_start") String lon_start,
            @Field("lat_end") String lat_end,
            @Field("lon_end") String lon_end
    );
}
