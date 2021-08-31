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
    private static final String TAG = "#######################";
    Response<ResponseBody> requestResult; //다운로드 요청 결과 얻은 body저장
    String serverUrl;
    DownloadVideoFileTask downloadVideoFileTask;
    String targetFileName;
    ArrayList<DownloadVideoFileTask> taskArr=new ArrayList<DownloadVideoFileTask>();

    //하이라이트 영상을 저장할 안드로이드쪽 폴더
    String highlitePath= Environment.getExternalStorageDirectory().getPath()+ File.separator+"/하이라이트";


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

        //askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);

        int numberOfVideo=intent.getIntExtra("numberOfVideo", 0);  //default =0 아무값도 putExtra하지 않았거나 0을 보낸경우 destroy
        if(numberOfVideo==0){
            onDestroy();    //잘못된 호출로 판단 -> download를 수행하지 않고 서비스 종료
        }else{

            for(int i=0; i<numberOfVideo; i++){     //인자로 받은 비디오 개수 만큼 다운로드 요청

                serverUrl="uploads/RightIndicator"+i+".mp4";  // 서버쪽 파일명
                targetFileName="highlight"+Integer.toString(i+1)+".mp4";

                taskArr.add(new DownloadVideoFileTask(targetFileName));

                downloadVideoFile(i);    // callBack 함수 에서 download 실패 -> onDestroy()

            //    SystemClock.sleep(5000);//1초

            }


            onDestroy();    //영상 수신 모두 수행한 후 서비스 종료
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    private void downloadVideoFile(int i) {
        //클라이언트와 통신할 서버측 주소 입력
        ServiceApi downloadService = createService(ServiceApi.class, "http://ec2-13-124-56-124.ap-northeast-2.compute.amazonaws.com:3001");
        Call<ResponseBody> call = downloadService.downloadFileByUrl(serverUrl); // 다운받을파일의 서버측 경로

        //파일 송신부 RetrofitInterface에 선언해 놓은Streaming 방식으로 파일 송신
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {  //요청 성공시
                    Log.d(TAG, "Got the body for the file");
                     Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_SHORT).show();
                    //saveToDisk(response.body(), "Video.mp4"); // 다운받을 파일명 설정

                    taskArr.get(i).execute(response.body());
                    //downloadVideoFileTask.execute(response.body());

                } else {
                    Log.d(TAG, "Connection failed " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e(TAG, t.getMessage());
                requestResult=null; // download요청 결과 실패 -> requestResult=null
            }
        });

    }

    //==================================================================================================
    //retrofit 빌더 생성
    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder().build())
                .build();
        return retrofit.create(serviceClass);
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
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                }
                outputStream.flush();
                Log.d(TAG, destinationFile.getParent());
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Failed to save the file!");
                return;
            } finally {

                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to save the file!");
            return;
        }

    }

    //==================================================================================================
    /*
    //권한 허용 여부 확인
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "Permission was denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == 101)
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


     */

    private class DownloadVideoFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {
        String fileName;

        DownloadVideoFileTask(String fileName){
            this.fileName=fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(ResponseBody... urls) {
            saveToDisk(urls[0], fileName); // 다운받을 파일명 설정

            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {


        }

        public void doProgress(Pair<Integer, Long> progressDetails) {

        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
