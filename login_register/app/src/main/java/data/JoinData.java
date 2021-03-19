package data;

import com.google.gson.annotations.SerializedName;

// 회원가입 요청시 보낼 데이터
public class JoinData {
    /*
    * SerializedName은 gson에 포함된 어노테이션
    * 문자열로 넘겨주는 value는 해당 객체가 JSON으로 바뀔 때 이름으로 사용되는 값으로,
    * 변수 이름이 JSON 객체안에 들어있는 이름과 완전히 똑같으면 생략 가능.
    * */
    @SerializedName("userName")
    private String userName;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("userPwd")
    private String userPwd;

    public JoinData(String userName,String userEmail, String userPwd){
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPwd = userPwd;
    }
}
