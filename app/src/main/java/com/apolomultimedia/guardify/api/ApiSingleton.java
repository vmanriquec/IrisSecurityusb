package com.apolomultimedia.guardify.api;

import com.apolomultimedia.guardify.util.Constantes;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by developer on 20/06/2016.
 */
public class ApiSingleton {

    private static ApiService apiService;

    public static ApiService getApiService() {
        if (apiService == null) {

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constantes.API_PATH)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            apiService = retrofit.create(ApiService.class);

        }
        return apiService;
    }

}
