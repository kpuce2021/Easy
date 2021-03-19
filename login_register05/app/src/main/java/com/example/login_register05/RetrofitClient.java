package com.example.login_register05;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/*서비스 객체 생성
 * ServiceAPI 인터페이스의 구현체를 만들기위해 Retrofit 클래스를 사용
 * 서비스 객체를 초기화 및 생성
 *
 * */


//레트로핏 클라이언트 선언
public class RetrofitClient {

    //https 통신시 handshake 오류 발생 >> http통신으로 변경
    private final static String BASE_URL = "http://ec2-13-124-56-124.ap-northeast-2.compute.amazonaws.com:3000";
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
