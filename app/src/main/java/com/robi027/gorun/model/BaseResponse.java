package com.robi027.gorun.model;

/**
 * Created by robi on 4/30/2019.
 */

public class BaseResponse {
    private boolean error;
    private String message;

    public BaseResponse(boolean error, String message) {
        this.error = error;
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
