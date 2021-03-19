package com.example.login_register05;

import com.google.gson.annotations.SerializedName;

// 회원가입 요청시 보낼 데이터
public class JoinData {
    @SerializedName("userName")
    private String userName;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("userPwd")
    private String userPwd;

    public JoinData(String userName, String userEmail, String userPwd) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPwd = userPwd;
    }

}
