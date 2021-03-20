//3월 2일 native

#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include<opencv2/core/core.hpp>
#include<opencv2/imgproc/imgproc.hpp>
#include<iostream>

#define PI 3.141592

using namespace std;
using namespace cv;

bool find_left(Mat *frame, vector<Vec2f> lines);
bool find_right(Mat *frame, vector<Vec2f> lines);
void drawLines(Mat *frame, float rho, float theta);



extern "C"
JNIEXPORT int JNICALL
Java_com_mobileprogramming_twelve_MainActivity_ConvertImage(JNIEnv *env, jobject thiz,
                                                            jlong mat_addr_input,
                                                            jlong mat_addr_result, jint count) {
    Mat &matInput=*(Mat *)mat_addr_input;
    Mat &matResult=*(Mat *)mat_addr_result;
//==================================================================================================
    Mat canny;
    Mat frame;

    vector<Vec2f> lines_left;
    vector<Vec2f> lines_right;

    bool left_flag;
    bool right_flag;
    Mat roi_left;
    Mat roi_right;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
    GaussianBlur(matResult, matResult, Size(3, 3), 0, 3);
    Canny(matResult, canny, 85, 110, 3);
    frame=canny;

    roi_left=canny(Rect(2*frame.cols/10, 3*frame.rows/5, 3*frame.cols/10, 2*frame.rows/5));
    roi_right=canny(Rect(5*frame.cols/10, 3*frame.rows/5, 3*frame.cols/10, 2*frame.rows/5));

    HoughLines(roi_left, lines_left, 3, PI / 180, 200);
    HoughLines(roi_right,lines_right, 3, PI / 180, 200);

    left_flag=find_left(&matInput, lines_left);
    right_flag=find_right(&matInput, lines_right);

    if(left_flag==false&&right_flag==false){
        count++;
    }else{
        count=0;
    }
    return count;
}
//====================================================================================
bool find_left(Mat* frame, vector<Vec2f> lines){  //Min
    bool flag=false;
    int minIndex=0;    // min theta

    if(lines.empty()!=1){
        for(int i=0; i<lines.size(); i++){
            if(lines[minIndex][1]>lines[i][1]&&lines[i][1]<1.1&&lines[i][1]>0.8) {
                minIndex = i; //최소 세타 인덱스
            }
        }
        if(lines[minIndex][1]<1.1&&lines[minIndex][1]>0.8){
            drawLines(frame, lines[minIndex][0], lines[minIndex][1]);
            flag=true;  //flag true->1 라인 찾기 성공
        }
    }
    return flag;
}
//========================================================================================
bool find_right(Mat *frame, vector<Vec2f> lines){ //Max
    bool flag=false;
    int maxIndex=0;
    if(lines.empty()!=1){
        for(int i=0; i<lines.size(); i++){

            if(lines[maxIndex][1]<lines[i][1]&&lines[i][1]>2.1&&lines[i][1]<2.5){
                maxIndex=i; //최대 세타 인덱스
            }
        }

        if(lines[maxIndex][1]>2.1&&lines[maxIndex][1]<2.5){
            drawLines(frame, lines[maxIndex][0], lines[maxIndex][1]);
            flag=true;  //flag true->1 라인 찾기 성공
        }
    }
    return flag;
}
//====================================================================================
void drawLines(Mat* frame, float rho, float theta){
    Mat temp=*frame;
    if(theta>0.8&&theta<1.1){   //left Line
        Point pt1(rho / cos(theta), 0);
        Point pt2((rho - temp.rows*sin(theta)) / cos(theta), temp.rows);
        pt1.x=pt1.x+2*temp.cols/10;
        pt1.y = pt1.y + 3 * temp.rows / 5;
        pt2.x=pt2.x+2*temp.cols/10;
        pt2.y = pt2.y + 3 * temp.rows / 5;
        line(*frame, pt1, pt2, Scalar(255, 0, 0), 5);
    }else if(theta>2.0&&theta<2.5){
        Point pt1(rho / cos(theta), 0);
        Point pt2((rho - temp.rows*sin(theta)) / cos(theta), temp.rows);
        pt1.x=pt1.x+5*temp.cols/10;
        pt1.y = pt1.y + 3 * temp.rows / 5;
        pt2.x=pt2.x+5*temp.cols/10;
        pt2.y = pt2.y + 3 * temp.rows / 5;
        line(*frame, pt1, pt2, Scalar(255, 0, 0), 5);
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_mobileprogramming_twelve_MainActivity_alarmImage(JNIEnv *env, jobject thiz,
                                                          jlong mat_addr_input,
                                                          jlong mat_addr_result) {

    Mat &matInput=*(Mat *)mat_addr_input;
    cvtColor(matInput, matInput, COLOR_BGR2HLS);

    // TODO: implement alarmImage()
}
//JNIEXPORT jlong JNICALL

extern "C"
JNIEXPORT jlong JNICALL
Java_com_mobileprogramming_twelve_MainActivity_loadCascade(JNIEnv *env, jobject thiz,
                                                           jstring cascade_file_name) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascade_file_name, 0);

    string baseDir("/storage/emulated/0/"); // cars.xml 파일 경로
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    jlong ret = 0;
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
        //___android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
          //                  "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
        //__android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            //"CascadeClassifier로 로딩 성공 %s", nativeFileNameString);
    env->ReleaseStringUTFChars(cascade_file_name, nativeFileNameString);
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mobileprogramming_twelve_MainActivity_detect(JNIEnv *env, jobject thiz,
                                                      jlong cascade_classifier_car,
                                                      jlong mat_addr_input, jlong mat_addr_result) {

    Mat &img_input = *(Mat *) mat_addr_input;   //input frame
    Mat &img_result = *(Mat *) mat_addr_result; //output frame

    std::vector<Rect> cars;     //검출된 차량의 정보를 저장할 벡터 자료형

    //((CascadeClassifier *) cascade_classifier_car)->detectMultiScale( img_resize, cars, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );
    ((CascadeClassifier *) cascade_classifier_car)->detectMultiScale(img_input, cars);  // 차량 검출 수행 -> 결과 cars벡터에 저장

    if(!cars.empty()){
        for(int i=0; i<cars.size(); i++){
            rectangle(img_input, cars[i], Scalar(255,0,255),2);
        }
    }
}


//========================================================================================================
