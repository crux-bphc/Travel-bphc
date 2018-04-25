package com.crux.pratd.travelbphc;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pratd on 29-03-2018.
 */

public class ApiClient {
    public static final String baseurl = "https://us-central1-travelbphc.cloudfunctions.net/app/";
    public static Retrofit retrofit = null;

    public static Retrofit getClient()
    {
        if(retrofit==null)
        {
            retrofit = new Retrofit.Builder().baseUrl(baseurl).
                    addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
