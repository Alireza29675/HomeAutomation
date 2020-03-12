package com.mhksoft.onoff;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("toggle/{tag}")
    Call<Void> toggle(@Path("tag") String tag);
}
