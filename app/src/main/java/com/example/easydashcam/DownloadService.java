package com.example.easydashcam;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


/*
전체 흐름은 onStartCommand에서 결정

전송 실패에 대한 내용은 따로 기록하거나 리턴하지 않고 Toast메시지를 띄우는 것으로 수행

 */

public class DownloadService extends Service {

    String highlitePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "/하이라이트";


    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String fileName = intent.getStringExtra("fileName");
        Log.e("DOWNLOADSERVICE", fileName.toString());
        String serverUrl = "ExtractedVideo/" + fileName; //서버에서 가져올 파일의 서버 경로

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.56.124:3001")
                .client(new OkHttpClient.Builder().build())
                .build();

        ServiceApi api = retrofit.create(ServiceApi.class);

        Call<ResponseBody> call = api.downloadFileByUrl(serverUrl); // 다운받을파일의 서버측 경로

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("DOWNLOAD", "onResponse Called");

                if(response.isSuccessful()){
                    new Thread() {
                        @Override
                        public void run() {
                            saveToDisk(response.body(), fileName);
                        }
                    }.start();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("DOWNLOAD", "onFailure Called");
                Log.e("DOWNLOAD", t.getMessage());
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //==================================================================================================
    // 다운받은 파일을 앱에 저장
    private void saveToDisk(ResponseBody body, String filename) {
        Log.d("Destination", "SaveToDistk start Point");
        try {
            //저장될 경로 지정

            File destinationFile = new File(highlitePath, filename);    //안드로이드에 들어갈 파일명

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                long fileSize = body.contentLength();

                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                }
                outputStream.flush();

                return;
            } catch (IOException e) {
                e.printStackTrace();

                return;
            } finally {

                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

    }


}