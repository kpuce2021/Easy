package com.example.easydashcam;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.putText;


/*
 save code check
 delete code check

 충돌 감도 -> findEventFrame => if (crash.get(i) > avr * 3) {
 현재 속도 -> onLocationChange => speed_location = (int) (getDistance(lastLat, lastLng, newLat, newLng) * 60 * 60 / 1000);

 Tone소리 -> speed_location = (int) (getDistance(lastLat, lastLng, newLat, newLng) * 60 * 60 / 1000);

 녹화주기 Text -> case R.id.item_record_test:

 영상 업로드 및 다운로드 설정 -> startService 상단 intent.putExtra("mode", 1) save

============================================================================================================================

추가해야할 사항
 시속 60km이상에서만 동작 하도록 설정

 */


//videos class================================================================================================


class Videos {
    public String info_path;
    public String info_startingPoint;
    public String info_destination;
    public String info_speed;
    public String info_crash;

    public Videos(String path, String startingPoiunt, String destination, String speed, String crash) {
        info_path = path;
        info_startingPoint = startingPoiunt;
        info_destination = destination;
        info_speed = speed;
        info_crash = crash;
    }

    public String getPath() {
        return this.info_path;
    }
}

//videos class================================================================================================
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener, LocationListener {

    public void updateInfo(ArrayList<Videos> arr_videoInfo) {
        for (int i = 0; i < arr_videoInfo.size(); i++) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(arr_videoInfo.get(i).info_path, true));
                bw.write(arr_videoInfo.get(i).info_path + "\n" + arr_videoInfo.get(i).info_startingPoint + arr_videoInfo.get(i).info_destination + arr_videoInfo.get(i).info_speed + "\n" + arr_videoInfo.get(i).info_crash);
                bw.close();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }
    //==============================================================================


    final int INIT = 1;
    final int PROGRESS = 2;
    final int RESTART = 3;
    final int RELEASE = 4;
    final int OFF = 5;
    final int DEFAULT = 0;
    final int LANE = 1;
    final int DISTANCE = 2;


    //Controll Video Record
    private int recordController = OFF;   // record 컨트롤을 위한 변수
    private int driveMode = DEFAULT;

    //variable test crash
    private ArrayList<Integer> eventFrames; // 추출해야할 이벤트 Frame을 저장할 배열

    //variable gradient&intercept
    private double gradientLeft = 0;  // 0 -> init 수행 (onCameraFrame메서드내 findGradient 호출)
    private double interceptLeft;
    private double gradientRight;
    private double interceptRight;

    //variable view
    private FrameLayout baseLayout = null;
    private ImageButton contextBtn = null;

    //variable gps      //check !!
    private LocationListener locationListener;
    private LocationManager locationManager;
    private int speed_location = 0;
    private ArrayList<Integer> speedArray=null;

    private String address_startingPoint;
    private String address_destination;
    private boolean location_flag = false;
    private double lastLat = 0.0;
    private double lastLng = 0.0;
    private double newLat = 0.0;
    private double newLng = 0.0;

    //variable Video Info
    private ArrayList<Videos> arr_videoInfo = new ArrayList<Videos>();
    private String info_path;
    private String info_size;
    private String info_speed;
    private int info_crash = 0;

    //variable 가속도 센서
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private int COLLISION_THRESHOLD = 2500;   //충돌임계값 -> 값이 낮을 수록 작은 충돌에도 이벤트 발생


    //variable RingThone
    int durationOfAlarm = 100;    //ms단위
    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);


    //variable timer
    TextView txt_timer;
    Timer timer;
    //int periodOfTimer=60000*5; // 현재 타이머 간격  5분
    //int periodOfTimer=10000; // 현재 타이머 간격  10초
    int periodOfTimer = 60000; // 현재 타이머 간격 default = 60초

    //variable display
    private Button btn_changeDisplay1; // 해상도 변경을 위한 버튼 변수
    private Button btn_changeDisplay2;
    private Button btn_changeDisplay3;
    private Button btn_changeDisplay4;
    private Button btn_changeDisplay5;
    private Button btn_10sec;   // 10 초간격으로 record 테스트를 위한 버튼

    //variabe path
    String baseDir = null;    //path for car.xml
    String pathDir = null;
    String baseDir_Recorder = null;   // 내부 저장소 경로
    String pathDir_Recorder = null;   // 내부 저장소 중 녹화파일 을 저장할 경로

    //variable VideoWriter
    int frame_count = 0;
    int count = 0;
    private ImageButton btn_record;  // 비디오 record 시작 버튼
    boolean record_flag = false;  //recodring 시작 포인트를 위한 flag
    VideoWriter videoWriter = null;
    boolean onRecording = false;  // recording 중인지 확인하기 위한 flag
    private int recordCount = 0;

    //variable Date
    private long now;
    private Date date;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); //햔재 날짜, 시간 포맷 설정
    private String time;    // 녹화 시작 시점의 시간을 저장하기위한 변수

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    //variable lane&car detection
    public native int ConvertImage(long matAddrInput, long matAddrResult, int count);   // 차선 검출 수행

    public native void alarmImage(long matAddrInput, long matAddrResult);   // 이벤트 발생 시점 시각화

    public native long loadCascade(String cascadeFileName);     // 스마트폰 내부 저장소에 저장한 cars.xml 파일의 경로 리턴

    public native boolean detectCar(long cascadeClassifier_car, long matAddrInput, double gradientLeft, double interceptLeft, double gradientRight, double interceptRight);

    public native void detect(long cascadeClassifier_car, long matAddrInput, long matAddrResult);   // asset folder 내 cars.xml파일 기반 객체 검출 수행 및 frame위에 표시


    private static final String TAG = "opencv";
    private Mat matInput = null;
    private Mat matResult = null;

    private CameraBridgeViewBase mOpenCvCameraView;
    long cascadeClassifier_car = 0;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.e("BASELOADERCALLBACK", "SUCCESS");
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        //opencv Camera API
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0);

        contextBtn = (ImageButton) findViewById(R.id.btn_context);
        locationListener = this;


        //location permission - delete
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "NOT PERMITTED!!");
            return;
        }


        //location start
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //location end

        //가속도 센서 설정
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);



        //baseLayout btnlistener===============================================================

        registerForContextMenu(contextBtn);
        baseLayout = (FrameLayout) findViewById(R.id.main_frame);
        baseLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (contextBtn.getVisibility() == View.INVISIBLE) {
                    contextBtn.setVisibility(View.VISIBLE);
                } else {
                    contextBtn.setVisibility(View.INVISIBLE);
                }

            }
        });

        contextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContextMenu(contextBtn);
            }
        });

        //baseLayout btnlistener===============================================================

        //record 수행 버튼
        btn_record = (ImageButton) findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new Button.OnClickListener() {
            @SuppressLint("MissingPermission")
            public void onClick(View v) {
                if (recordController == OFF) { // 녹화 종료하기
                    recordController = INIT;

                    TimerTask timerTask = new TimerTask() {
                        public void run() {
                            if (recordController == PROGRESS) {
                                recordController = RESTART;
                            }

                        }
                    };
                    timer = new Timer();
                    timer.schedule(timerTask, 0, periodOfTimer);

                } else if (recordController == PROGRESS) {
                    recordController = RELEASE;
                }
            }
        });

    }// oncreate end point

    //=============================================================================================================
    public int findAverageSpeed(ArrayList<Integer> speedArray) {
        int sum=0;
        for(int i=0; i<speedArray.size(); i++){
            sum+=speedArray.get(i);
        }
        return sum/speedArray.size();
    }
    //=============================================================================================================
    public ArrayList<Integer> findEventFrame(ArrayList<Double> crash) {
        ArrayList<Integer> eventArray = new ArrayList<Integer>();
        double avr = 0.0;

        for (int i = 0; i < crash.size(); i++) {  //모든 충격 값 더하기
            avr += crash.get(i);
        }
        avr = avr / crash.size();   //모든 충격의 평균값 계산

        for (int i = 0; i < crash.size(); i++) {
            if (crash.get(i) > avr * 5) {
                eventArray.add(i);  //  충격의 평균치 보다 2배 이상 높은 경우 해당 index(frame)을 배열에 추가
            }
        }

        return eventArray;
    }

    //=============================================================================================================
    //find gradient
    public void findGradient(Mat frame, double gradientLeft, double interceptLeft, double gradientRight, double interceptRight) {
        Point ptCenter = new Point(frame.cols() / 2.0, frame.rows() / 2.0);  // 화면 중앙에 해당하는 포인트
        Point ptLeft = new Point((double) frame.cols() / 10.0, (double) frame.rows());    // 화면 좌측 하단에 해당하는 포인트
        Point ptRight = new Point(9 * (double) frame.cols() / 10.0, (double) frame.rows()); // 화면 우측 하단에 해당하는 포인트


        double leftGradient = (double) (ptCenter.y - ptLeft.y) / (double) (ptCenter.x - ptLeft.x);    // 좌측 직선의 기울기
        double leftIntercept = frame.rows() / 2.0 - leftGradient * frame.cols() / 2.0;    // 좌측 직선의 x절편 값

        this.gradientLeft = leftGradient;
        this.interceptLeft = leftGradient;

        double rightGradient = (double) (ptRight.y - ptCenter.y) / (double) (ptRight.x - ptCenter.x); //우측 직선의 기울기
        double rightIntercept = ptRight.y - rightGradient * ptRight.x;    //우측 직선의 x절편 값

        this.gradientRight = rightGradient;
        this.interceptRight = rightIntercept;

    }
    //=============================================================================================================

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getMenuInflater();
        if (v == contextBtn) {
            inflater.inflate(R.menu.menu_main, menu);   //contextBtn click -> menu_main 을 inflate
        }
    }

    //=============================================================================================================
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        super.onContextItemSelected(item);
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.item_320:
                mOpenCvCameraView.setMaxFrameSize(320, 280);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
                break;
            case R.id.item_480:
                mOpenCvCameraView.setMaxFrameSize(480, 320);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
                break;
            case R.id.item_640:
                mOpenCvCameraView.setMaxFrameSize(640, 480);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
                break;
            case R.id.item_800:
                mOpenCvCameraView.setMaxFrameSize(800, 600);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
                break;
            case R.id.item_1280:
                mOpenCvCameraView.setMaxFrameSize(1280, 780);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
                break;
            case R.id.item_record_test:
                periodOfTimer = 5000;   //녹화 주기 5초 테스트용
                break;
            case R.id.item_record_1:
                periodOfTimer = 60000 * 1;   //녹화 주기 1분 설정
                break;
            case R.id.item_record_3:
                periodOfTimer = 60000 * 3;   //녹화 주기 3분 설정
                break;
            case R.id.item_record_5:
                periodOfTimer = 60000 * 5;   //녹화 주기 5분 설정
                break;
            case R.id.item_drive_default:
                driveMode = DEFAULT;
                break;
            case R.id.item_drive_lane:
                driveMode = LANE;
                break;
            case R.id.item_drive_distance:
                driveMode = DISTANCE;
                break;
        }
        return false; //check
    }

    //=============================================================================================================
    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6372800.0; // inkillometer
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double dLat1 = Math.toRadians(lat1);
        double dLat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2.0) + Math.pow(Math.sin(dLon / 2), 2.0) * Math.cos(dLat1) * Math.cos(dLat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    //=============================================================================================================
    @Override
    public void onLocationChanged(@NonNull Location location) { // requestLocationUpdate 이후 CallBack
        if (location != null) {
            newLat = location.getLatitude();
            newLng = location.getLongitude();

            if (lastLat != 0.0 && lastLng != 0.0) {
                speed_location = (int) (getDistance(lastLat, lastLng, newLat, newLng) * 60 * 60 / 1000);
                // 현재 속도 시속
            }

            lastLat = newLat;
            lastLng = newLng;

        }
    }

    //=============================================================================================================
    @Override
    public void onProviderEnabled(String provider) {
        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    //=============================================================================================================
    @Override
    public void onProviderDisabled(String provider) {
    }

    //=============================================================================================================
    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    //=============================================================================================================
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    //=============================================================================================================
    private void copyFile(String filename) {    //car.xml 을 내부 저장소에 copy
        baseDir = Environment.getExternalStorageDirectory().getPath();   // 기본 저장 경로
        pathDir = baseDir + File.separator + filename;   // 기본 저장 경로 + cars.xml 파일의 경로

        AssetManager assetManager = this.getAssets();   // asset folder내 cars.xml 파일 접근 하기 위한 assetmanager

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d(TAG, "copyFile :: 다음 경로로 파일복사 " + pathDir);
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
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 " + e.toString()); //파일 복사 실패 시점에 유의
        }

    }

    //=============================================================================================================
    private void read_cascade_file() {   // native method loadCascade를 호출하여 cascadeClassifier_car 변수에 경로 저장

        copyFile("cars.xml");
        Log.d(TAG, "read_cascade_file");

        cascadeClassifier_car = loadCascade("cars.xml");  //loadCascade -> native-lib.cpp 구현
    }

    //=============================================================================================================


    public String getTime() {    //현재 시간을 문자열로 리턴하는 메서드
        String current = null;
        now = System.currentTimeMillis();
        date = new Date(now);
        current = sdf.format(date);
        return current;
    }
    //=============================================================================================================

    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        sensorManager.unregisterListener(this);     // sensorListener 해제
        stopLocationUpdates();

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
        mOpenCvCameraView.enableView();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @SuppressLint("MissingPermission")
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {   // 프레임 단위 영상처리 수행
        matInput = inputFrame.rgba();
        if (recordController != OFF) {
            //===============//===============//===============//===============//===============//===============//===============//===============
            if (recordController == INIT) {

                frame_count=0;
                eventFrames=new ArrayList<Integer>();

                speedArray=new ArrayList<Integer>();

                sensorManager.registerListener(MainActivity.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                findGradient(matInput, gradientLeft, interceptLeft, gradientRight, interceptRight); //선을 기울기를 찾기 위한 메서드 호출
                int w=matInput.cols();
                int h=matInput.rows();
                time=getTime();
                baseDir_Recorder = Environment.getExternalStorageDirectory().getPath();   // 기본 저장 경로
                pathDir_Recorder = baseDir_Recorder + File.separator+"/녹화영상/"+time+".avi";
                //information of VideoFile
                info_path=baseDir_Recorder + File.separator+"/녹화정보/"+time+".txt";

                address_startingPoint=getCurrentAddress(newLat, newLng);    // 녹화 시작시 시작 주소에 해당

                videoWriter=new VideoWriter(pathDir_Recorder, VideoWriter.fourcc('M','J','P','G'),20,new Size(w,h), true);
                videoWriter.open(pathDir_Recorder, VideoWriter.fourcc('M','J','P','G'),20,new Size(w,h), true);
                if(!videoWriter.isOpened()){    //video writer가 열리지 않았으면 INIT 상태를 유지
                    Log.d("VIDEOWRITER", "NOT OPENED!!!!!!!!!");
                }else{  //video writer가 열렸다면 -> PROGRESS로 상태 변경
                    recordController=PROGRESS;
                }
            }
            //===============//===============//===============//===============//===============//===============//===============//===============
            if(recordController==PROGRESS){ //frame카운트 고려 할것 - > 이벤트 위함

                frame_count++;

                if (matResult == null) {
                    matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type()); // 연산 최소화를 위한 영역 지정
                }

                // 1. 화면에 녹화 시간 , 속도 출력
                //현재 시간 화면에 출력
                putText(matInput, getTime(), new Point(5,40), FONT_HERSHEY_PLAIN, 2, new Scalar(255,255,255));
                //현재 속도 화면에 출력
                putText(matInput, Integer.toString(speed_location), new Point(5,80), FONT_HERSHEY_PLAIN, 2, new Scalar(255,255,255));

                // 2. 영상 저장 수행
                videoWriter.write(matInput); // video writer 수행

                // 3. 충격 정보 수집 & 속도 정보 수집
                speedArray.add(speed_location); //현재 프레임의 속도값 ArrayList에 저장

                // 4. 주행 보조 모드에 따른 영상처리 수행
                if(driveMode==LANE){
                    count = ConvertImage(matInput.getNativeObjAddr(), matResult.getNativeObjAddr(), count);   // 차선 검출 메서드 호출 native-lib 구현
                    if (count > 18) {   // 12
                        alarmImage(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());  // 이벤트 발생 시점->화면전환
                        //tone.startTone(ToneGenerator.TONE_CDMA_PIP, durationOfAlarm);   //차선 이탈 알림
                    }
                }
                if(driveMode==DISTANCE){

//                    detect(cascadeClassifier_car, matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

                    //차량 거리 측정 수행결과 true -> native code(화면색상변경) / activity(소리 알림)

                    if(detectCar(cascadeClassifier_car, matInput.getNativeObjAddr(), gradientLeft, interceptLeft, gradientRight, interceptRight)){
                        //tone.startTone(ToneGenerator.TONE_CDMA_PIP, durationOfAlarm);   //차선 이탈 알림
                        alarmImage(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());  // 이벤트 발생 시점->화면전환
                    }

                }
            }
            //===============//===============//===============//===============//===============//===============//===============//===============
            if(recordController==RESTART){  // Timer로 부터 상태 변경 release ->init 수행
                // 재시작 순서 1.카메라 릴리즈 2.VideoInfo 구성 및 저장 3.서비스 시작 4.Controller 상태 변경
                // 5. 가속도 센서 unregister

                frame_count=0;


                // 1.카메라 릴리즈
                videoWriter.release();
                videoWriter=null;

                //2. VideoInfo 구성 및 저장
                arr_videoInfo.clear();  //arrayVideoInfo 비우기
                address_destination=getCurrentAddress(newLat, newLng);  // 도착지 주소 찾기
                //더 구성해야할 정보 존재 ex) 충돌 횟수/ 속도 등
                arr_videoInfo.add(new Videos(info_path, address_startingPoint, address_destination, Integer.toString(findAverageSpeed(speedArray)), Integer.toString(eventFrames.size()))); //정보 구성하기
                updateInfo(arr_videoInfo);  //정보 저장 메서드 호출
                speedArray.clear();

                //3. 서비스 시작
                Intent intent=new Intent(getApplicationContext(), RetrofitService.class);
                //intent.putExtra("target", pathDir_Recorder);
                intent.putExtra("target", time);
                intent.putIntegerArrayListExtra("eventArray", eventFrames); //추출할 이벤트 프레임이 저장되어 있는 ArrayList전달

                startService(intent);

                // 4.Controller 상태 변경
                recordController=INIT;

                //5. sensorManager.unregisterListener(this);     // sensorListener 해제
                sensorManager.unregisterListener(this);     // sensorListener 해제
            }
            //===============//===============//===============//===============//===============//===============//===============//===============
            if(recordController==RELEASE){  //Button으로 부터 상태 변경 RELEASE -> OFF
                // 종료 순서 1.카메라 릴리즈 2.VideoInfo 구성 및 저장 3.타이머 끄기 4.서비스 시작 5.Controller 상태 변경
                // 6. 가속도 센서 unregister

                //1. 카메라 릴리즈
                videoWriter.release();
                videoWriter=null;

                //2. VideoInfo 구성 및 저장
                arr_videoInfo.clear();  //arrayVideoInfo 비우기
                address_destination=getCurrentAddress(newLat, newLng);  // 도착지 주소 찾기
                //더 구성해야할 정보 존재 ex) 충돌 횟수/ 속도 등
                arr_videoInfo.add(new Videos(info_path, address_startingPoint, address_destination, Integer.toString(findAverageSpeed(speedArray)), Integer.toString(eventFrames.size()))); //정보 구성하기
                updateInfo(arr_videoInfo);  //정보 저장 메서드 호출
                speedArray.clear();

                //3. 타이머 끄기
                timer.cancel();

                //4. 서비스 시작
                Intent intent=new Intent(getApplicationContext(), RetrofitService.class);
                intent.putExtra("target", time);
                intent.putIntegerArrayListExtra("eventArray", eventFrames); //추출할 이벤트 프레임이 저장되어 있는 ArrayList전달

                startService(intent);

                // 5.Controller 상태 변경
                recordController=OFF;

                //6. sensorManager.unregisterListener(this);     // sensorListener 해제
                sensorManager.unregisterListener(this);     // sensorListener 해제
            }
            //===============//===============//===============//===============//===============//===============//===============//===============
        }// off가 아닐때
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
                cameraBridgeViewBase.setCameraPermissionGranted();
                read_cascade_file();
            }
        }
    }
    public String getCurrentAddress( double latitude, double longitude) {

        if(latitude==0.0||longitude==0.0){
            return "No Latitude or Longitude"+"\n";
        }

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
            //Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가"+"\n";
        } catch (IllegalArgumentException illegalArgumentException) {
            //Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표"+"\n";
        }

        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견"+"\n";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

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
        super.onResume();
        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 위치정보 업데이트
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
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
    public void onSensorChanged(SensorEvent sensorEvent) {  //가속도 센서값 변화시 호출
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
                //충돌값 계산
                double collision_detect = Math.sqrt( Math.pow(z - last_z,2)*100 + Math.pow(x-last_x,2)*10+  Math.pow(y-last_y,2)*10)/ diffTime * 10000;
                //충격 값 저장 -> 녹화 진행중 프레임당 충격값을 저장

                if (collision_detect > COLLISION_THRESHOLD) {
                    //지정된 수치이상 흔들림이 있으면 실행
                    //tone.startTone(ToneGenerator.TONE_CDMA_PIP, durationOfAlarm);   //check 주석삭제
                    eventFrames.add(frame_count);   //0909
                    info_crash++; // save?? code
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