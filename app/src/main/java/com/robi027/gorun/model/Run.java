package com.robi027.gorun.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by robi on 4/30/2019.
 */

public class Run extends BaseResponse{

    @SerializedName("data")
    private ArrayList<RunData> runDatas;

    @SerializedName("detail")
    private RunData runData;

    public Run(boolean error, String message) {
        super(error, message);
    }

    public ArrayList<RunData> getRunDatas() {
        return runDatas;
    }

    public void setRunDatas(ArrayList<RunData> runDatas) {
        this.runDatas = runDatas;
    }

    public RunData getRunData() {
        return runData;
    }

    public void setRunData(RunData runData) {
        this.runData = runData;
    }
}
