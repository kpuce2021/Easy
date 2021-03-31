package com.example.gps_vallocity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
                //LocationListener : 위치 정보를 위치 공급자로부터 지속적으로 받아옴
public class MainActivity extends AppCompatActivity implements LocationListener { // class


    private LocationManager locationManager;
    private Location mLastLocation = null;
    private TextView tvGetSpeed, tvCalSpeed , tvTime , tvLastTime , tvGpsEnable , tvTimeDif,tvDistDif;
    private double speed;

//=================================================================================================
    @Override // onCreate
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGetSpeed = (TextView)findViewById(R.id.tvGetSpeed);
        tvCalSpeed = (TextView)findViewById(R.id.tvCalSpeed);
        tvTime = (TextView)findViewById(R.id.tvTime);
        tvLastTime = (TextView)findViewById(R.id.tvLastTime);
        tvGpsEnable = (TextView)findViewById(R.id.tvGpsEnable);
        tvTimeDif = (TextView)findViewById(R.id.tvTimeDif);
        tvDistDif = (TextView)findViewById(R.id.tvDistDif);
//=================================================================================================

        /*퍼미션 체크 ActivityCompat.checkSelfPermission(Context, String)
        퍼미션 요청 ActivityCompat.requestPermissions(Activity, String[], int)
        퍼미션 요청 콜백함수 ActivityCompat.OnRequestPermissionsResultCallback */
        //1. 권한체크
  //  int LocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

    //2. 이미 퍼미션을 가지고 있다면
 /*   if(LocationPermission == PackageManager.PERMISSION_GRANTED){
        return;
    }
*/
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

      //boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

//=================================================================================================

        // LocationManager 가져옴
    locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

//=================================================================================================
        //tvTime

        //마지막위치 정보 locationManager의 getLastKnownLocation 메소드 사용 (gps_provider 값 넘겨줌)
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //마지막 위치 정보가 없다면 (시작시간값 찾기위해)
        if(lastKnownLocation != null){

            //데이터 형식지정 1
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

            //fromatDate 에 마지막위지 정보의 시간값을 저장 1
            String formatDate = sdf.format(new Date(lastKnownLocation.getTime()));
            //tvTime에 시간정보 띄움
            tvTime.setText(":" + formatDate); //시작 Time
        }

//=================================================================================================
        //tvGpsEnable

        //GPS 사용 가능 여부 확인
        boolean isEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        tvGpsEnable.setText(":" + isEnable); // Gps Enable
//=================================================================================================
        /*위치정보 업데이트 등록
        이동시 위치정보 업데이트를 위해 requestLocationUpdates함수 사용
        provider : 현재 위치 정보 제공자가 gps 임
        minTime : 몇 초마다 정보를 update할 것인지 설정  1000 millisecond = 1초 -> 0으로설정 or 1000으로 설정 저속에서 차이 시간 거리값 조정해서 테스트 필요
        minDistance : 거리 이동을 얼마나 할 때마다 update 하는지에 대한 값 / 단위 m
        listener : LocationListener*/
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);

    }//onCreate end

//=================================================================================================
    //LocationLisetner 에 대한 Override

    /*위치정보를 가져 올 수 있는 메소드
    위치 이동이나 시간 경과 등으로 인해 호출
    최신 위치는 location 파라메터가 가지고 있음
    최신 위치를 가져 오려면, location 파라메터 이용*/
    @Override
    public void onLocationChanged(@NonNull Location location) {
        //데이터 형식 지정 2
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        //deltaTime
        double deltaTime = 0;

        // getSpeed() 함수를 이용하여 속도를 계산
        double getSpeed = Double.parseDouble(String.format("%.1f", location.getSpeed()));
        tvGetSpeed.setText(": " + getSpeed); // Get Speed

        //fromatDate 에 현재location 정보의 시간값을 저장 2
        String formatDate = sdf.format(new Date(location.getTime()));
        //tvTime에 시간정보 띄움
        tvTime.setText(":" + formatDate); // 현재 count 시간 Time

        //위치 변경이 두번째로 변경된 경우 계산에 의해 속도 계산
        if (mLastLocation != null){
            //시간 간격
            //현재 위치 시간값 - 마지막 위치 시간 값 / 1000 = 이동시간 m/s
            deltaTime = (location.getTime() - mLastLocation.getTime()) / 1000.0;
            tvTimeDif.setText(":" + deltaTime + "sec");// Time Difference 시간간격
            tvDistDif.setText(": " + (int)mLastLocation.distanceTo(location)+" m"); // Time Difference 거리간격

            //거리간격 / 시간간격 = 속도계산
            //mLastLocation.distanceTo(location) -> 마지막 위치와 현재위치의 거리간격 구하는 메소드
            speed = mLastLocation.distanceTo(location) / deltaTime;

            String formatLastDate = sdf.format(new Date(mLastLocation.getTime()));
            tvLastTime .setText(": " + formatLastDate); //Last Time

            //speed 값을 format
            double calSpeed = Double.parseDouble(String.format("%.1f",speed));
            tvCalSpeed.setText(": " + calSpeed); // Cal Speed
        }
        //현재 위치를 마지막 위치로 변경
        mLastLocation = location;
    }
//=================================================================================================
    //LocationLisetner 에 대한 Override

    // 위치 공급자의 상태가 바뀔 때 호출
    // 단순 위치 계산 사용 x
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

//=================================================================================================
    //LocationLisetner 에 대한 Override

    //위치 공급자가 사용 가능해질 때 호출

    @Override
    public void onProviderEnabled(String provider) {
        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
     }
        // 위치정보 업데이트
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
    }
//=================================================================================================
    //LocationLisetner 에 대한 Override

    //위치공급자가 사용 불가능해질때 호출
    //단순 위치정보 구할때 사용 x
    @Override
    public void onProviderDisabled(String provider) {

    }
//=================================================================================================
// onCreate 다음 호출
@Override
protected void onStart() {
    super.onStart();
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //권한이 없을 경우 최초 권한 요청 또는 사용자에 의한 재요청 확인
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // 권한 재요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
    }

}
//=================================================================================================
//onStart 다음 호출

    @Override
    protected void onResume() {
        super.onResume();
        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         return;
        }
        // 위치정보 업데이트
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
    }
//=================================================================================================
   //앱 사용하지 않을 때 작동

    @Override
    protected void onPause() {
        super.onPause();
        // 위치정보 갱신 제거
        //locationManager.removeUpdates(this);
        stopLocationUpdates();
    }
    private void stopLocationUpdates(){
            locationManager.removeUpdates(this);
    }
//=================================================================================================

} // class end
























