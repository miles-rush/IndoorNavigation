package com.example.tool;

import com.example.bean.ResponseCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class GsonUtil {
    public static ResponseCode getResponseJson(String jsonData) {
        Gson gson = new Gson();
        ResponseCode responseCode = gson.fromJson(jsonData, new TypeToken<ResponseCode>(){}.getType());
        return responseCode;
    }
}
