package com.example.easydashcam;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/*
Source
// 하이라이트 영상을 저장할 폴더 경로
//Environment.getExternalStorageDirectory().getPath()+File.separator+"/하이라이트"

---------------------------------------------------------------------------------------------------

Info

전송 후 결과 코드에 따른 onDestroy 작성

전송 후 결과 코드에 따른 download service 수행

intent.getIntExtra(Mode, 0)
-> mode 0 uplaod만 수행
-> mode 1 download service 호출

 ---------------------------------------------------------------------------------------------------

추가 해야할 사항

요청 결과로 Failure가 호출될 경우
 -> startService를 호출한 시점으로 ResultCode 전송 -> 다시 service를 호출 하도록 코드 작성

 */


public class RetrofitService extends Service {

    //하이라이트 영상을 저장할 안드로이드쪽 폴더
    String highlitePath= Environment.getExternalStorageDirectory().getPath()+ File.separator+"/하이라이트";

    com.example.easydashcam.ServiceApi apiService=null;
    String targetPath=null;
    ArrayList<Integer> eventArray;

    Context context=this;



    public RetrofitService() {
    } //constructor

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OkHttpClient client = new OkHttpClient.Builder().build();//save code
        apiService = new Retrofit.Builder().baseUrl("http://ec2-13-124-56-124.ap-northeast-2.compute.amazonaws.com:3001").client(client).build().create(com.example.easydashcam.ServiceApi.class);//save code
        targetPath=intent.getStringExtra("target"); // 전송할 영상이 저장되어 있는 경로 및 파일명 저장
        //추가된 내용 => 충격 감지 하이라이트 추출을 위한 Frame정보가 담겨있는 ArrayList 서버로 전송

        eventArray=intent.getIntegerArrayListExtra("eventArray");

        multipartVideoUploads();

        return super.onStartCommand(intent, flags, startId);
    }


    private void multipartVideoUploads() {
        //전송할 파일의 경로 저장 = mediaPath1


        File targetFile = new File(targetPath);  // 전송 할 target path
        // 파일전송을 위한 파싱
        RequestBody reqFile = RequestBody.create(MediaType.parse("*/*"), targetFile); // 파일 형식
        // 파일을 part 형식으로 body에 담음
        MultipartBody.Part body = MultipartBody.Part.createFormData("upload", targetFile.getName(), reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload");

        //전송부분 apiService 인터페이스에 선언해놓은 post 방식으로 이미지를 보냄
        Call<ResponseBody> req = apiService.postVideo(body, name);

        //요청에 대한 콜백 함수 호출
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {   //

                    Log.d("CALL_DOWNLOAD", "CALL_DOWNLOAD !!!!!!!!!!!");
                    Intent downloadIntent=new Intent(context, DownloadService.class);
                    //downloadIntent.putExtra("numberOfVideo", downloadParam);  //save
                    downloadIntent.putExtra("numberOfVideo", 2);    //delete
                    startService(downloadIntent);
                    Toast.makeText(getApplicationContext(),"Uploaded Successfully!",Toast.LENGTH_SHORT).show();
                    onDestroy();    // 모든 수행이끝난 후 onDestroy 요청코드를 받을 수 있다면 요청코드 받은후에 onDestroy 수행

                }else{
                    Toast.makeText(getApplicationContext(),"check request",Toast.LENGTH_SHORT).show();
                    Log.d("CALL_DOWNLOAD", "CALL_DOWNLOAD !!!!!!!!!!!");
                    Intent downloadIntent=new Intent(context, DownloadService.class);
                    //downloadIntent.putExtra("numberOfVideo", downloadParam);  //save
                    downloadIntent.putExtra("numberOfVideo", response.code());      //delete
                    startService(downloadIntent);
                    Toast.makeText(getApplicationContext(),"upload fail!",Toast.LENGTH_SHORT).show();
                    onDestroy();    // 모든 수행이끝난 후 onDestroy 요청코드를 받을 수 있다면 요청코드 받은후에 onDestroy 수행
                }
                //Toast.makeText(getApplicationContext(), response.code() + " ", Toast.LENGTH_SHORT).show();
            }
            // 전송 실패시 처리
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
                onDestroy();
                t.printStackTrace();
            }
        });
    }



    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}