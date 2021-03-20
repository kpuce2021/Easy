/*
저장된 영상을 VideoView를 통해 시청하기 위한 Acitivty
 */

package com.mobileprogramming.twelve;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class playActivity extends AppCompatActivity {

    //final private String basePath= Environment.getExternalStorageDirectory().getPath()+ File.separator+"/녹화영상/";
    private String basePath=Environment.getExternalStorageDirectory().getAbsolutePath(); // 내부 저장소 경로
    private String videoTitle=null; // play할 비디오의 제목
    private Intent intent;  // Adapter로 부터 intent를 받기 위한 Intent
    private VideoView videoView;    // activity_play.xml 의 VideoView를 연결하기 위한 변수
    private MediaController mc; //동영상을 플레이 하기 위한 MediaControlloer
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mc=new MediaController(this);
        intent=getIntent(); //Adapter로 부터 intent 수신
        videoTitle=(String)intent.getSerializableExtra("videoTitle");   // intent에 포함된 내용 수신
        videoView=(VideoView)findViewById(R.id.videoView);
        videoView.setMediaController(mc);   // videoView 에 mediaController 연결
        videoView.setVideoPath(basePath+"/녹화영상/"+videoTitle);   // play할 영상이 있는 경로
        videoView.requestFocus();   // 영상 포커싱
        videoView.start();  //영상 재생 시작

    }
}