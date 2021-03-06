package com.example.bean;

//服务器响应对应类
public class ResponseCode {
    private String code;
    private String info;

    private Integer additionalId;
    private String additionalToken;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Integer getAdditionalId() {
        return additionalId;
    }

    public void setAdditionalId(Integer additionalId) {
        this.additionalId = additionalId;
    }

    public String getAdditionalToken() {
        return additionalToken;
    }

    public void setAdditionalToken(String additionalToken) {
        this.additionalToken = additionalToken;
    }
}
