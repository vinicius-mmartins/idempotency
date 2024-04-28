package com.github.viniciusmartins;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScriptByIdempotencyKey {

    private static final ConcurrentMap<String, String> responsePerThread = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        doSyncAndConcurrently(
                4,
                c -> postAndSaveResponses(client, buildRequest())
        );
        System.out.println(responsePerThread);
    }

    private static Request buildRequest() {
        String json = """
                {
                    "uniqueNumber":"123"
                }
                """;
        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
        return new Request.Builder()
                .post(requestBody)
                .url("http://localhost:8080/idempotency/key")
                .header("Idempotency-Key", "4a2eecc7-d553-4fed-81c9-dcfd1d60ef2b")
                .build();
    }

    private static void postAndSaveResponses(OkHttpClient client, Request request) {
        try (Response response = client.newCall(request).execute()) {
            //save
            String currentThread = Thread.currentThread().getName();
            String resp = response.body().string();
            responsePerThread.put(currentThread, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doSyncAndConcurrently(int threadCount, Consumer<String> operation) throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    operation.accept("");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        startLatch.countDown();
        endLatch.await();
    }

}