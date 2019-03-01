package com.hante.zhongliang.hantepaydemo_android;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HantePayOrder {

    private String hanteApiUrl;
    private String hanteApiToken;
    private String vendor;
    private String reference;
    private String amount;
    private String currency;
    private String ipnUrl;
    private String desc;//optional
    private String note;//optional


    public String getHanteApiUrl() {
        return hanteApiUrl;
    }

    public void setHanteApiUrl(String hanteApiUrl) {
        this.hanteApiUrl = hanteApiUrl;
    }

    public String getHanteApiToken() {
        return hanteApiToken;
    }

    public void setHanteApiToken(String hanteApiToken) {
        this.hanteApiToken = hanteApiToken;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


    public interface RequestCallBack{
        public void successCallBack(Response response) throws Exception;
        public void errorCallBack(String errorString);
    }



    public static HantePayOrder getInstance() {
        // if already inited, no need to get lock everytime



        return new HantePayOrder();
    }


    public void doPost( final HantePayOrder.RequestCallBack requestCallBack){

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request;

        JSONObject body = new JSONObject();

        body.put("amount",this.amount);
        body.put("currency",this.currency);
        body.put("ipn_url",this.ipnUrl);
        body.put("reference",this.reference);
        body.put("note",this.note);
        body.put("description",this.desc);
        body.put("vendor",this.vendor);

        Log.d("参数", "：" + body.toString());
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON,body.toString());

        request = new Request.Builder()
                .url(this.hanteApiUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer "+this.hanteApiToken)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                if (requestCallBack != null){
                    requestCallBack.errorCallBack(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (requestCallBack != null) {
                    try {
                        requestCallBack.successCallBack(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

}
