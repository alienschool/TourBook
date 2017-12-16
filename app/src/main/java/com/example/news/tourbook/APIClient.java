package com.example.news.tourbook;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private  static final String BaseUri="http://dibukhanmathematician.com/tourbook/API/";
    private static Retrofit retrofit;

    public static Retrofit getApiClient()
    {
        if(retrofit==null)
        {
            retrofit= new Retrofit.Builder().baseUrl(BaseUri).
                    addConverterFactory(GsonConverterFactory.create()).build();

        }
        return retrofit;
    }
}
