package data;

import com.google.gson.annotations.SerializedName;

//회원가입 요청에 대한 응답으로 돌아올 데이터
public class JoinResponse {
    /*
     * SerializedName은 gson에 포함된 어노테이션
     * 문자열로 넘겨주는 value는 해당 객체가 JSON으로 바뀔 때 이름으로 사용되는 값으로,
     * 변수 이름이 JSON 객체안에 들어있는 이름과 완전히 똑같으면 생략 가능.
     * */
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}
