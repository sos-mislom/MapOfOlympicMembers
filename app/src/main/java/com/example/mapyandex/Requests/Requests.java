package com.example.mapyandex.Requests;

import java.io.IOException;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Requests {
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
