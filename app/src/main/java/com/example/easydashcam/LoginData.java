package com.example.easydashcam;

import com.google.gson.annotations.SerializedName;

public class LoginData {

    //constructor
    public LoginData(String userEmail, String userPwd){
        this.userEmail = userEmail;
        this.userPwd = userPwd;
    }

    @SerializedName("userEmail")
    String userEmail;

    @SerializedName("userPwd")
    String userPwd;



}
