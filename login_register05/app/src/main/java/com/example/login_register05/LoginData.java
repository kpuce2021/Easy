package com.example.login_register05;

import com.google.gson.annotations.SerializedName;


// 로그인 요청시 보낼 데이터
public class LoginData {
    @SerializedName("userEmail")
    String userEmail;

    @SerializedName("userPwd")
    String userPwd;

    public LoginData(String userEmail, String userPwd){
        this.userEmail = userEmail;
        this.userPwd = userPwd;
    }
}
