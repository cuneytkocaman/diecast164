package com.cuneyt.diecast164.entities;

public class ErrorLogModel {
    private String deviceName;
    private String activityName;
    private String error;
    private String dateTime;

    public ErrorLogModel() {
    }

    public ErrorLogModel(String deviceName, String activityName, String error, String dateTime) {
        this.deviceName = deviceName;
        this.activityName = activityName;
        this.error = error;
        this.dateTime = dateTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        activityName = activityName;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
