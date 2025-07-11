package com.ultimafurniture.lynx.models;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BaseModel implements Serializable {

    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("statusCode")
    @Expose
    private int statusCode;
    private boolean updating;
    private int position;
    private boolean isChildOpen = false;
    private JsonObject jsonObject;
    private boolean selected;

    public boolean isSuccess() {
        Log.d("isSuccess: ", "" + success);
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isChildOpen() {
        return isChildOpen;
    }

    public void setChildOpen(boolean childOpen) {
        isChildOpen = childOpen;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static BaseModel parse(JsonObject jsonSting) {
        Gson gson = new Gson();
        return gson.fromJson(jsonSting, BaseModel.class);
    }

    public static Object parse(String jsonSting, Class<?> cls) {
        Gson gson = new Gson();
        return gson.fromJson(jsonSting, cls);
    }

    public static Object parse(JsonObject jsonSting, Class<?> cls) {
        Gson gson = new Gson();
        return gson.fromJson(jsonSting, cls);
    }

    public JsonObject toJsonObject() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(this, this.getClass());
        return jsonElement.getAsJsonObject();
    }
}