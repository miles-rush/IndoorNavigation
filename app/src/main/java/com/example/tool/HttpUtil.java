package com.example.tool;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    public static String API_URL = "http://10.0.2.2:8080";//本机地址

    public static void sendOkHttpGetRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL + address).build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendOkHttpPostRequest(String address, RequestBody requestBody, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL + address).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
}
