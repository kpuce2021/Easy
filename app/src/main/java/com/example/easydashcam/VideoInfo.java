package com.example.easydashcam;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileReader;

public class VideoInfo extends AppCompatActivity {
    private String basePath= Environment.getExternalStorageDirectory().getAbsolutePath(); // 내부 저장소 경로
    private String videoTitle=null; // play할 비디오의 제목
    private Intent intent;  // Adapter로 부터 intent를 받기 위한 Intent

    //xml variable
    private TextView info_title;
    private TextView info_startingPoint;
    private TextView info_destination;
    private TextView info_speed;
    private TextView info_crash;
    private Button info_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_info);


        info_title=(TextView)findViewById(R.id.info_title);
        info_startingPoint=(TextView)findViewById(R.id.info_size);
        info_destination=(TextView)findViewById(R.id.info_location);
        info_speed=(TextView)findViewById(R.id.info_speed);
        info_crash=(TextView)findViewById(R.id.info_crash);
        info_delete=(Button)findViewById(R.id.info_delete);


        intent=getIntent(); //Adapter로 부터 intent 수신
        videoTitle=(String)intent.getSerializableExtra("videoTitle");   // intent에 포함된 내용 수신

        try{
            BufferedReader br=new BufferedReader(new FileReader(basePath+"/녹화정보/"+videoTitle));
            String readStr="";
            String str=null;
            /*
            while((str=br.readLine())!=null){
                readStr+=str+"\n";
            }
            */
            str=br.readLine();
            info_title.setText("경로 : "+str);
            str=br.readLine();
            info_startingPoint.setText("출발 지점 : "+str);
            str=br.readLine();
            info_destination.setText("도착 지점 : "+str);
            str=br.readLine();
            info_speed.setText("평균 속도 : "+str);
            str=br.readLine();
            info_crash.setText("충돌 횟수 : "+str);
            br.close();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "fileReadFail", Toast.LENGTH_SHORT).show();

        }

    }
}