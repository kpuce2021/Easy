package com.mobileprogramming.twelve;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaMuxer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobileprogramming.twelve.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.HoughLines;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.putText;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener {

    //variable GPS
    private GpsTracker gpsTracker;  //현재 위치 정보를 저장할 객체
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private String address; // 현재 주소 -> 배열에 저장 및 서버로 전송
    private float speed=0;  //현재 속도 -> 화면에 띄워 녹화영상에 포함

    //variable 가속도 센서
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate=0;
    private float last_x, last_y, last_z;
    private int COLLISION_THRESHOLD=5000;   //충돌 임계값 -> 값이 낮을 수록 작은 충돌에도 이벤트 발생


    //variable RingThone
    int durationOfAlarm=100;    //알림 소리 길이 -> 현재 100ms
    ToneGenerator tone=new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);


    //variable timer
    TextView txt_timer;
    Timer timer;
    int periodOfTimer=20000; // 타이머 간격 ms단위 -> 현재 타이머 간격 5 초


    //variable display
    private Button btn_changeDisplay1; // 해상도 변경을 위한 버튼 변수
    private Button btn_changeDisplay2;
    private Button btn_changeDisplay3;
    private Button btn_changeDisplay4;
    private Button btn_changeDisplay5;
    private Button btn_10sec;   // 10 초간격으로 record 테스트를 위한 버튼

    //variabe path
    String baseDir=null;    //path for car.xml
    String pathDir=null;
    String baseDir_Recorder=null;   // 내부 저장소 경로
    String pathDir_Recorder=null;   // 내부 저장소 중 녹화파일 을 저장할 경로

    //variable VideoWriter
    int count=0;
    private Button btn_record;  // 비디오 record 시작 버튼
    boolean record_flag=false;  //recodring 시작 포인트를 위한 flag
    VideoWriter videoWriter;
    boolean onRecording=false;  // recording 중인지 확인하기 위한 flag
    private int recordCount=0;

    //variable Date
    private long now;
    private Date date;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); //햔재 날짜, 시간 포맷 설정
    private String time;    // 녹화 시작 시점의 시간을 저장하기위한 변수

    //variable lane&car detection
    public native int ConvertImage(long matAddrInput, long matAddrResult, int count);   // 차선 검출 수행
    public native void alarmImage(long matAddrInput, long matAddrResult);   // 이벤트 발생 시점 시각화
    public native long loadCascade(String cascadeFileName);     // 스마트폰 내부 저장소에 저장한 cars.xml 파일의 경로 리턴
    public native void detect(long cascadeClassifier_car, long matAddrInput, long matAddrResult);   // asset folder 내 cars.xml파일 기반 객체 검출 수행 및 frame위에 표시

    private static final String TAG = "opencv";
    private Mat matInput;
    private Mat matResult;
    private CameraBridgeViewBase mOpenCvCameraView;
    long cascadeClassifier_car=0;

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    //=============================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //permission - gps
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        // 가속도 센서 설정
        sensorManager =(SensorManager)this.getSystemService(SENSOR_SERVICE);
        senAccelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //sensorManager.registerListener(MainActivity.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //카메라 설정
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)

        //타이머 설정
        txt_timer=(TextView)findViewById(R.id.txt_timer);

        //화질 선택
        btn_changeDisplay1=(Button)findViewById(R.id.btn_changeDisplay1);
        btn_changeDisplay1.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                mOpenCvCameraView.setMaxFrameSize(320,280);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();

                //tone.startTone(ToneGenerator.TONE_CDMA_PIP, durationOfAlarm);   //버튼 클릭시 알림
            }
        });
        btn_changeDisplay2=(Button)findViewById(R.id.btn_changeDisplay2);
        btn_changeDisplay2.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                mOpenCvCameraView.setMaxFrameSize(480,320);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
            }
        });
        btn_changeDisplay3=(Button)findViewById(R.id.btn_changeDisplay3);
        btn_changeDisplay3.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                mOpenCvCameraView.setMaxFrameSize(640,480);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
            }
        });
        btn_changeDisplay4=(Button)findViewById(R.id.btn_changeDisplay4);
        btn_changeDisplay4.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                mOpenCvCameraView.setMaxFrameSize(800,600);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
            }
        });
        btn_changeDisplay5=(Button)findViewById(R.id.btn_changeDisplay5);
        btn_changeDisplay5.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                mOpenCvCameraView.setMaxFrameSize(1280,780);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
            }
        });


        // record 수행
        btn_record=(Button)findViewById(R.id.btnRecord);
        btn_record.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){

                if(record_flag==true){ // 녹화 종료하기
                    //녹화 메서드
                    btn_record.setText("녹화시작");
                    videoWriter.release();  //video write종료 -> 녹화파일에 .avi영상 저장
                    record_flag=false;
                    timer.cancel();
                }else{   //녹화 시작하기
                    //녹화 메서드
                    gpsTracker=new GpsTracker(MainActivity.this);   //gpsTracker 객체 생성
                    double latitude = gpsTracker.getLatitude();     //위도 값 저장
                    double longitude = gpsTracker.getLongitude();   //경도 값 저장
                    address = getCurrentAddress(latitude, longitude);   //현재 주소를 address에 저장

                    sensorManager.registerListener(MainActivity.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    btn_record.setText("녹화종료");
                    record_flag=true;
                    onRecording=false;

                    TimerTask timerTask=new TimerTask(){
                        public void run(){

                            if(onRecording==true){
                            videoWriter.release();
                            onRecording=false;
                            }
                        }
                    };
                    timer=new Timer();
                    timer.schedule(timerTask,0,periodOfTimer);
                }
            }
        });

    }
    //=============================================================================================================
    private void showDialogForLocationServiceSetting() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    //=============================================================================================================
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    //=============================================================================================================
    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }
    //=============================================================================================================
    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }
    //=============================================================================================================
    private void read_cascade_file(){   // native method loadCascade를 호출하여 cascadeClassifier_car 변수에 경로 저장

        copyFile("cars.xml");
        Log.d(TAG, "read_cascade_file");

        cascadeClassifier_car=loadCascade("cars.xml");  //loadCascade -> native-lib.cpp 구현

    }

    //=============================================================================================================
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //=============================================================================================================
    private void copyFile(String filename) {    //car.xml 을 내부 저장소에 copy
        baseDir = Environment.getExternalStorageDirectory().getPath();   // 기본 저장 경로
        pathDir = baseDir + File.separator + filename;   // 기본 저장 경로 + cars.xml 파일의 경로

        AssetManager assetManager = this.getAssets();   // asset folder내 cars.xml 파일 접근 하기 위한 assetmanager

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() ); //파일 복사 실패 시점에 유의
        }

    }



    //=============================================================================================================

    public String getTime(){    //현재 시간을 문자열로 리턴하는 메서드
        String current=null;
        now=System.currentTimeMillis();
        date=new Date(now);
        current=sdf.format(date);
        return current;
    }
    //=============================================================================================================

    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    //=============================================================================================================
    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    //=============================================================================================================
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }
    //=============================================================================================================
    @Override
    public void onCameraViewStarted(int width, int height) {

    }
    @Override
    public void onCameraViewStopped() {

    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {   // 프레임 단위 영상처리 수행
        matInput = inputFrame.rgba();

        if(record_flag==true) { //record 시작 포인트 및 녹화중 상태


            int w=matInput.cols();
            int h=matInput.rows();
            if(onRecording==false){ //녹화 시작 포인트에만 해당
                time=getTime();
                baseDir_Recorder = Environment.getExternalStorageDirectory().getPath();   // 기본 저장 경로
                pathDir_Recorder = baseDir_Recorder + File.separator+"/녹화영상/"+getTime()+ ".avi";
                /*
                File file=new File(Environment.getExternalStorageDirectory().getPath()+File.separator+"/녹화영상");
                file.mkdirs();
                 */
                //voideoWriter를 통해 avi영상을 저장하기 위해 writer set
                // android opencv 의 경우 avi외에 포맷을 지원하지 않
                videoWriter=new VideoWriter(pathDir_Recorder, VideoWriter.fourcc('M','J','P','G'),20,new Size(w,h), true);
                videoWriter.open(pathDir_Recorder, VideoWriter.fourcc('M','J','P','G'),20,new Size(w,h), true);
                onRecording=true;
            }
            if(!videoWriter.isOpened()){
                finish();
            }
            speed=gpsTracker.getSpeedOfTracker();   // gpsTracker객체로 부터 현재 속도를 리턴 받음

            //현재 속도 화면에 출력
            putText(matInput, Float.toString(speed), new Point(5,80), FONT_HERSHEY_PLAIN, 3, new Scalar(255,255,255));
            //현재 시간 화면에 출력
            putText(matInput, getTime(), new Point(5,40), FONT_HERSHEY_PLAIN, 3, new Scalar(255,255,255));
            videoWriter.write(matInput); // video writer 수행

            Mat canny = new Mat(); // 원본 Mat canny;
            Mat Roi1, Roi2, Roi;

            if (matResult == null) {
                matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type()); // 연산 최소화를 위한 영역 지정
            }
            count = ConvertImage(matInput.getNativeObjAddr(), matResult.getNativeObjAddr(), count);   // 차선 검출 메서드 호출 native-lib 구현

            if (count > 18) {
                tone.startTone(ToneGenerator.TONE_CDMA_PIP, durationOfAlarm);   //차선 이탈 알림
                //alarmImage(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());  // 이벤트 발생 시점->화면전환
            }
            detect(cascadeClassifier_car, matInput.getNativeObjAddr(), matResult.getNativeObjAddr()); // cars.xml 기반 차량 검출 메서드 호출
        }

        return matInput;
    }
    //=============================================================================================================
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;


    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase: cameraViews) {
            if (cameraBridgeViewBase != null) {
                //cameraBridgeViewBase.setCameraPermissionGranted();
                read_cascade_file();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean havePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED||checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= getPackageManager().PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
                havePermission = false;
            }
        }
        if (havePermission) {
            onCameraPermissionGranted();
        }
    }
    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted();
        }else{
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if(mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis(); // 현재시간

            // 0.1초 간격으로 가속도값을 업데이트
            if((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                // 내가 마음대로 정한 충돌량
                double collision_detect = Math.sqrt( Math.pow(z - last_z,2)*100 + Math.pow(x-last_x,2)*10+  Math.pow(y-last_y,2)*10)/ diffTime * 10000;
                if (collision_detect > COLLISION_THRESHOLD) {
                    //지정된 수치이상 흔들림이 있으면 실행
                    tone.startTone(ToneGenerator.TONE_CDMA_PIP, durationOfAlarm);   //버튼 클릭시 알림

                } else {

                }
                //갱신
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

