/*
녹화된 비디오 영상을 리스트 뷰로 구성하기 위한 Acitivty
 */

package com.example.easydashcam;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity {

    public ArrayList<com.example.easydashcam.Video> array_video;   // 리스트 뷰로 구성하기 위해 Video객체를 참조할 배열 변수 선언
    private ListView mListView;     // .xml의 listView와 연결할 변수
    private File directory; // array_video에 저장할 동영상이 있는 폴더
    private File[] files;   // 폴더내 모든 파일을 저장할 배열 변수
    final private String path= Environment.getExternalStorageDirectory().getPath()+File.separator+"/녹화영상";  // 찾고자 하는 영상들이 위치해 있는 경로

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mListView = (ListView) findViewById(R.id.list_video);

        File directory=new File(path);
        files=directory.listFiles();    // path의 위치에 있는 파일들 files배열 변수에 저장


        array_video=new ArrayList<com.example.easydashcam.Video>();
        for(int i=0; i<files.length; i++){
            long size;
            size=files[i].length();
            String fileName = files[i].getName();
            int pos = fileName.lastIndexOf(".");
            String _fileName = fileName.substring(0, pos);  //확장자 제거

            array_video.add(new com.example.easydashcam.Video(_fileName, files[i].getPath(), size)); //array_video에 파일내 모든 영상 저장
        }


        com.example.easydashcam.VideoAdapter videoAdapter=new com.example.easydashcam.VideoAdapter(this, array_video);  //Adapter생성하여 현재 context와 array_video전달
        mListView.setAdapter(videoAdapter); // listView에 adapter 연결
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){ //리스트 뷰 클릭이벤트 선언
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Toast.makeText(getApplicationContext(), "pushed", Toast.LENGTH_SHORT).show();

            }
        });

    }
}