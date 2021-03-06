package com.example.tool;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.indoornavigation.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    //public static String API_URL = "http://10.0.2.2:8080";//本机地址
    public static String API_URL = "http://192.168.43.160:8080";//局域网地址
    public static String RESOURCE_URL = "http://192.168.43.160:8080/voice/";//局域网地址

    public static String adminToken = "";

    public static void setAdminToken(String token) {
        adminToken = token;
    }

    public static void sendOkHttpGetRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL + address).build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendOkHttpLoginPostRequest(String address, RequestBody requestBody, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL + address).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendOkHttpPostRequest(String address, RequestBody requestBody, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = null;
        if (adminToken.equals("")) {
            request = new Request.Builder().url(API_URL + address).post(requestBody).build();
        }else {
            request = new Request.Builder().url(API_URL + address).addHeader("admin-token",adminToken).post(requestBody).build();
        }
        client.newCall(request).enqueue(callback);
    }
}
