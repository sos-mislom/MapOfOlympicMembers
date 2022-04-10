package com.example.mapyandex;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Map;

import okhttp3.*;

class Post {
    static final OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(chain -> {
                Request request = chain.request().newBuilder().addHeader("Connection", "close").build();
                return chain.proceed(request);
            })
            .build();

    public Response get(String url) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            assert response.body() != null;
            return new Response(response.body().string(), response.code());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Response post(String url, Map<String, String> json) {
        try {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder body = new FormBody.Builder();
            for (Map.Entry<String, String> e : json.entrySet()) {
                body.add(e.getKey(), e.getValue());
            }
            Request request = new Request.Builder()
                    .url(url)
                    .post(body.build())
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            assert response.body() != null;
            return new Response(response.body().string(), response.code());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
class Response {
    private final String string;
    private final int statusCode;

    public Response(String string, int statusCode) {
        this.string = string;
        this.statusCode = statusCode;
        //Log.e("REQ", string);
    }

    public JSONObject json(){
        return JSON.decode(string);
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return string;
    }
}