package com.example.easydashcam;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UploadData {

    @SerializedName("VideoTitle")
    String videoTitle;

    @SerializedName("CrashEventArray")
    ArrayList<Integer> crashEventArray;

    public UploadData(String videoTitle ,ArrayList<Integer> inputArr){
        this.videoTitle=videoTitle;
        this.crashEventArray=inputArr;
    }
}
