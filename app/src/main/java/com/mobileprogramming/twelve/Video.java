/*
리스트뷰에 띄울 Video 객체
Adapter에 의해 listView에 연결
 */

package com.mobileprogramming.twelve;

import javax.xml.namespace.QName;

public class Video {
    private String title; // 영상 객체의 제목
    private String path;    // 영상 객체가 위치하고 있는 경로
    private long size_byte;
    private long size_Mbyte;
//==============================================================================================================
    public Video(String title, String path, long size){    //생성자 제목과, 경로 설정
        this.title=title;
        this.path=path;
        this.size_byte=size;
    }
    public String getTitle(){
        return this.title;
    }
    public String getPath(){
        return this.path;
    }
    public long getSizeToLong(){    //byte 크기
        return this.size_byte;
    }
    public String getSizeToString_Mbyte(){
        size_Mbyte=size_byte/(1024*1024);
        return Long.toString(size_Mbyte);
    }
    public String getSizeToString_byte(){
        return Long.toString(size_byte);

    }

}
