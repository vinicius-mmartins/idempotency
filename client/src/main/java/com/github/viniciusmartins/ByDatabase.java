package com.github.viniciusmartins;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ByDatabase {

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }



    private static Request buildRequest() {
        String json = """
                {
                    "uniqueNumber":"123"
                }
                """;
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
        return new Request.Builder()
                .post(requestBody)
                .url("localhost:8080/idempotency/database")
                .build();
    }

}