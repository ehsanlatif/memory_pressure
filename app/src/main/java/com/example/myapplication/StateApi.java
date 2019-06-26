package com.example.myapplication;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StateApi {
    /*
    Get request to fetch city weather.Takes in two parameter-city name and API key.
    */
    @GET("/api/vmstate")
    Call <String> sendState(@Query("state") String state);
}