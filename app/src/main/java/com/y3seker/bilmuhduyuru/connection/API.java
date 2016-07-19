package com.y3seker.bilmuhduyuru.connection;

import com.y3seker.bilmuhduyuru.models.Annc;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Yunus Emre Şeker on 04.03.2015.
 * -
 */

public interface API {

    @GET("/duyurular")
    void getAll(Callback<ArrayList<Annc>> cb);

    @GET("/duyurular/{count}")
    void getSizeOf(@Path("count") int count, Callback<ArrayList<Annc>> cb);

    @GET("/dahayeni/{index}")
    void getNewer(@Path("index") int last, Callback<ArrayList<Annc>> cb);

    @FormUrlEncoded
    @POST("/kayit")
    void register(@Field("reg_id") String reg_id, Callback<RegisterResponse> cb);

}