package com.shuttleroid.vehicle.network.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shuttleroid.vehicle.app.AppConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitProvider {
    private static volatile Retrofit retrofit;
    private RetrofitProvider() {}

    public static Retrofit get() {
        if (retrofit == null) {
            synchronized (RetrofitProvider.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(log)
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    Gson gson = new GsonBuilder().setLenient().create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(AppConfig.baseUrl())
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofit;
    }
}
