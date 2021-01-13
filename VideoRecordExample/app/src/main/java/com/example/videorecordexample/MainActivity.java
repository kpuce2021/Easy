package com.example.videorecordexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    //필요 변수 선언
    private Camera camera; // hardware camera import
    private MediaRecorder mediaRecorder; // 동영상 촬영시 사용하는 변수
    private Button btn_record;
    private SurfaceView surfaceView; // 동영상 같은 연산처리가 많이 필요한 뷰를 위해 사용
    private SurfaceHolder surfaceHolder;
    private boolean recording = false; // 녹화여부 검사 ,초기값 false

/*=================================================================================================================================================================
===================================================================================================================================================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TedPermission.with(this) // 권한 체크 >> Manifest에서 Tedpermission을 sync해준뒤 main에서 사용
                .setPermissionListener(permission)
                .setRationaleMessage("녹화를 위하여 권한을 허용해 주세요.")
                .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요")
                .setPermissions(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO) // Manifest에 사용할 권한
                .check(); // 최종check

/*------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        btn_record = (Button)findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() { // 녹화시작 버튼클릭 이벤트 처리
            @Override
            public void onClick(View view) {
                if(recording){  // 레코딩이 진행중이라면
                    mediaRecorder.stop(); // 녹화종료
                    mediaRecorder.release(); //녹화종료
                    camera.lock(); // 카메라 잠금
                    recording = false;
                }else{
                    runOnUiThread(new Runnable() { // 영상,애니매이션 처리는 백그라운드 스레드에서 처리
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "녹화가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                            try{
                                mediaRecorder = new MediaRecorder(); // 객체생성
                                camera.unlock(); //카메라 잠금해제
                                mediaRecorder.setCamera(camera); // 카메라 변수사용
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER); // 버튼클릭시 알림음
                                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 비디오 소스를 설정
                                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P)); // ============녹화화질 설정============
                                mediaRecorder.setOrientationHint(90); // 촬영 각도 설정
                                mediaRecorder.setOutputFile("/sdcard/test.mp4"); // 저장경로 설정 // ***확장자***
                                //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //동영상 출력 형식 설정
                                //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP); //비디오 인코더 설정
                                mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());// 영상 미리보기화면 세팅
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                                recording = true; // 녹화중
                                btn_record.setText("녹화종료");
                                Toast.makeText(MainActivity.this,"녹화가 종료되었습니다 ",Toast.LENGTH_SHORT).show();
                            }catch (Exception e){ // 오류발생시
                                e.printStackTrace();
                                mediaRecorder.release(); // 녹화종료
                            }
                        }
                    }); // 스레드 종료
                }
            }
        });// 녹화버튼 클릭 이벤트 처리

    }

/*=================================================================================================================================================================
===================================================================================================================================================================*/

    PermissionListener permission = new PermissionListener() { // line39 의 permission의 오류를 해결하기위한 객체생성
        @Override
        public void onPermissionGranted() { // permission 이 허용 되었을 경우
            Toast.makeText(MainActivity.this,"권한 허가",Toast.LENGTH_SHORT).show();

            //camera 권한을 허가한 뒤에 카메라를 사용해야 하기 때문에 권한 허가했을 경우 수행
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(MainActivity.this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) { // permission이 거부된것이 있을경우
            Toast.makeText(MainActivity.this,"권한 거부",Toast.LENGTH_SHORT).show();
        }
    };

/*=================================================================================================================================================================
===================================================================================================================================================================*/

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) { // surfaceview가 만들어졌을때 시점

    }

    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) { // 서피스뷰 변화할때 호출
        refreshCamera(camera); // 카메라 초기화 작업
    }

    private void refreshCamera(Camera camera) {
        if(surfaceHolder.getSurface() == null){ //surfaceholder 가 null일경우 예외처리
            return;
        }

        try { //카메라 초기화 작업
            camera.stopPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
        setCamera(camera);
    }

    private void setCamera(Camera cam) {

        camera = cam;
    }
    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}