package com.example.easydashcam;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadViewModel extends ViewModel {
    MutableLiveData<ArrayList<TableResponse>> mutableLiveData = new MutableLiveData<ArrayList<TableResponse>>();
    private ServiceApi api;
    private Retrofit retrofit;

    public void accessDB(){
        api = new Retrofit.Builder().baseUrl("http://13.209.7.225:80").addConverterFactory(GsonConverterFactory.create()).build().create(com.example.easydashcam.ServiceApi.class);//save code
        Call<ArrayList<TableResponse>> call = api.accessSeverDB();

        call.enqueue(new Callback<ArrayList<TableResponse>>() {
            @Override
            public void onResponse(Call<ArrayList<TableResponse>> call, Response<ArrayList<TableResponse>> response) {
                Log.e("ACCESSDB", "onResponse Called");
                mutableLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ArrayList<TableResponse>> call, Throwable t) {
                Log.e("ACCESSDB", "onFailure Called");
            }
        });


    }






}
