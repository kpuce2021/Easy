package com.mobileprogramming.videorecorderex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    String mPath=null;  //동영상 녹화 파일 저장공간
    MediaRecorder mRecorder=null;
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isRecording=false;  // 현재 녹화중인지 확인하기 위한 flag
    Button recordButton;
    int count=0;
    //===================================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkCameraHardware(this)){  // 디바이스 카메라 하드웨어 탑재 여부를 검사
            mCamera=getCameraInstance(); // getCameraInstance method 호츌
            mPreview=new CameraPreview(this, mCamera);
            FrameLayout preview=(FrameLayout)findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }

        recordButton=(Button)findViewById(R.id.button_capture);   // 버튼 클릭 리스너 정의
        recordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                    startVideoRecord(); // recorder method 시작



            }
        });

    }
    //===================================================================================================================
    private void startVideoRecord(){
        if(isRecording){
            mRecorder.stop();
            mRecorder.release();
            mRecorder=null;

            mCamera.lock();
            isRecording=false;

            recordButton.setText("녹화 시작");  // 버튼 텍스트 상태 변경

        }else{
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    mRecorder=new MediaRecorder();
                    mCamera.unlock();   // 카메라 잠금 해제
                    mRecorder.setCamera(mCamera);   // 카메라를 동영상 캡처에 사용하도록 설정
                    //mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);    //오디오 소스 설정
                    mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 동영상 소스 설정
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);   // 동영상 출력 형식 설정
                    mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);    //비디오 인코더 설정
                    //mRecorder.setOrientationHint(90); //촬영 각도 맞추기


                    mPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/record.mp4";

                    // 저장 경로 설정
                    mRecorder.setOutputFile(mPath);

                    mRecorder.setPreviewDisplay(mPreview.previewHolder.getSurface());
                    try{
                        mRecorder.prepare();    //제공된 구성 설정으로 MediaRecorder 준비
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    mRecorder.start();
                    isRecording=true;

                    recordButton.setText("녹화중지");


                }
            });   // multi thread 구현


        }

    }



    //===================================================================================================================
    private boolean checkCameraHardware(Context context){   // 해당 디바이스가 카메라 하드웨어를 포함하고 있는지 체크하는 메소드 선언
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){    //카메라 하드웨어 있을 경우 true 리턴하게됨
            return true;    // 카메라 하드웨어 탑재
        }else{
            return false;   // 카메라 하드웨어 미탑재
        }
    }
    //===================================================================================================================
    public static Camera getCameraInstance(){   // 카메라 객체 얻는 메소드 카메라의 경우 예외처리를 통해 객체를 얻어야 함으로 메소드를 사용하여 객체 얻음
        Camera c=null;
        try{
            c=Camera.open(); // 카메라 객체를 얻는 메소드 호출
        }catch(Exception e){
            // 예외처리 코드 작성
        }
        return c;
    }
//===================================================================================================================
    // CameraView 구현 메소드 View에 현재 카메라가 갖고 있는 이미지 데이터를 보여줌
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
        private SurfaceHolder previewHolder;
        private Camera previewCamera;

        public CameraPreview(Context context, Camera camera){   // 생성자
        super(context); // SurfaceView 에 기본 생성자가 없음 super을 통해서 부모 클래스 생성자에 parameter 전달
        previewCamera=camera;    // mCamera필드에 현재 camera 객체 저장

        previewHolder=getHolder();
        previewHolder.addCallback(this);
        //previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //android version 3.0이하 에는 있어야 함 이후 버전에서는 사라진 기능임
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try{
            previewCamera.setPreviewDisplay(holder);
            previewCamera.startPreview();
        }catch(IOException e){
            //Log.d(TAG, "Error setting camera preview"+e.getMessage());    // 주석 -> TAG오류 발생
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
        if(previewHolder.getSurface()==null){   // preview surface 가 존재 하지 않는다면 return
            return;
        }
        try{    // preview change하기 전에 stop 구현
            previewCamera.stopPreview();
        }catch(Exception e){
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or reformatting changes too .

        try{
            previewCamera.setPreviewDisplay(previewHolder);
            previewCamera.startPreview();
        }   catch(Exception e){
            //Log.d(TAG, "Error setting camera preview"+e.getMessage());    // 주석 -> TAG오류 발생
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}

}