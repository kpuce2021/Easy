package com.example.easydashcam;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    //https 통신시 handshake 오류 발생 >> http통신으로 변경
    private final static String BASE_URL = "http://ec2-13-124-56-124.ap-northeast-2.compute.amazonaws.com:3001";  // aws ip
    private static Retrofit retrofit = null;

    private RetrofitClient(){
    }
    public static Retrofit getClient(){
        if(retrofit == null){
            // Retrofit 객체 초기화
            retrofit = new Retrofit.Builder()
                    // 요청을 보낼 bae_url설정
                    .baseUrl(BASE_URL)
                    // Gson컨버터 사용
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
