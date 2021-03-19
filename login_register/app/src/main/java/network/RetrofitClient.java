package network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/*서비스 객체 생성
* ServiceAPI 인터페이스의 구현체를 만들기위해 Retrofit 클래스를 사용해야함
* 서비스 객체를 초기화 및 생성
*
* */

//base_url 이 ec2 주소가아니라 내가만든 node.js 웹서버 주소를 써봐야함
public class RetrofitClient {
    private final static String BASE_URL = "https://ec2-13-124-56-124.ap-northeast-2.compute.amazonaws.com:3000";
    private static Retrofit retrofit = null;

    private RetrofitClient(){
    }

    public static Retrofit getRetrofit(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()   // Retrofit 객체 초기화
                    .baseUrl(BASE_URL) // 요청을 보낼 base_url 설정
                    .addConverterFactory(GsonConverterFactory.create()) // JSON 파싱을 위한 CsonConverterFactory메서드 추가
                    .build();
        }
        return retrofit;
    }



}
