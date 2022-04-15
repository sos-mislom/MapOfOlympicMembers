package com.example.mapyandex.Requests;

import org.json.JSONObject;

public class Response {
    private final String string;
    private final int statusCode;

    public Response(String string, int statusCode) {
        this.string = string;
        this.statusCode = statusCode;
    }

    public JSONObject json() {
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
