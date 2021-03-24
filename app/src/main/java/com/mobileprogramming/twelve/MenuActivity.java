/*
로그인 회원 가입 화면 다음 화면
1. camera로 이동 버튼
2. 저장된 영상으로 이동하기 위한 버튼
 */

package com.mobileprogramming.twelve;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MenuActivity extends AppCompatActivity {
//variable
    private Button btn_camera;  // MainActivity로 이동하기 위한 버튼
    private Button btn_gall;    // 저장된 영상리스트로 이동하기 위한 버튼
    private Intent intent_camera;   // MainActivity로 이동 Intent
    private Intent intent_gall; // VideoActivity로 이동 Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        // for delete
        /*
        Uri targetUri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String targetDir=Environment.getExternalStorageDirectory().getPath()+File.separator+"/녹화영상";
        targetUri=targetUri.buildUpon().appendQueryParameter("bucketId",String.valueOf(targetDir.toLowerCase().hashCode())).build();
 */
        File file=new File(Environment.getExternalStorageDirectory().getPath()+File.separator+"/녹화영상"); //영상 저장을 위한 폴더 경로
        if(!file.exists()){
            file.mkdirs();  // 영상 저장을 위한 폴더를 생성
        }


        Uri uri=Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/녹화영상/");


        intent_camera=new Intent(this, MainActivity.class);
        intent_gall=new Intent(this, VideoActivity.class);


        btn_camera=(Button)findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                startActivity(intent_camera);
            }   //MainActivity로 이동
        });

        btn_gall=(Button)findViewById(R.id.btn_gall);
        btn_gall.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                startActivity(intent_gall);
            }   //저장된 영상이 있는 VideoActivity로 이동
        });

    }
}