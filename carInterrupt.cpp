#include <opencv2/core/core.hpp>
#include <opencv2/opencv.hpp> // opencv 기본적인 API가 들어있는 헤더파일
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/core/utility.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/tracking.hpp>
#include <opencv2/videoio.hpp>
#include <cstring>
#include <iostream>
#include <time.h>
#include<stdlib.h>
#include<cmath>
#include<vector>

#define PI 3.141592

using namespace std;
using namespace cv;


class VideoEvent{
public:
    string path;    //동영상 파일 경로
    string title;   // 생성될 영상 이름
    int startFrame; // 시작 프레임 시작
    int endFrame;   //  종료 프레임
    
    VideoEvent(string _path, string _title, int _startFrame, int _endFrame){
        path=_path;
        title=_title;
        startFrame=_startFrame;
        endFrame=_endFrame;
        
    }
    string getPath(void){
        return path;
    }
    int getStartFrame(void){
        return startFrame;
    }
    string getTitle(void){
        return title;
    }
    int getEndFrame(void){
        return endFrame;
    }
};

bool findIndicator(Mat target, int direction);
double Distance(Point p1, Point p2);
Rect detectCars(Mat *detectFrame, Mat * baseFrame, CascadeClassifier classifier, bool *track_flag);    // classifier 는 한번 호출하는것으로 수정할것
Rect trackCars(Mat *detectFrame,Mat *baseFrame,bool *init_flag, bool *track_flag,Rect target, Ptr<Tracker> tracker, int *process_size, CascadeClassifier classifier, vector<int> *vector_x, bool *indicatorFlag); //vectorsave

void crashToEvent(string path, int crashArr[], int sizeOfArr);

void extractVideo(vector<VideoEvent> events);

int frameCount=0;
int startFrame=0;
int endFrame=0;

string targetVideo;     // 영상처리 작업과 영상 추출작업을 할 비디오 파일
vector<VideoEvent> videoEvents;

int main(int argc, char* argv[]){

    // event input 상황 start   1. 영상의 경로 2. 이벤트발생 프레임이 담겨 있는 배열
    
    // 요구사항!
    //1. 들어온 영상의 경로가 "change_test.mp4" 파일 이라고 가정
    string inputPath=argv[1]; // argv[1] == 인자로 받은 파일명
    targetVideo=inputPath;
    // 2.Event 배열이 들어왔다고 가정
    int crashFrame[7]={10,11,22,33,44,55,500};   // frame의 차이가 얼마 안나거나 frame이 영상의 범위를 벗어날 경우에 대한 예외처리 주의
    
    crashToEvent(targetVideo, crashFrame, sizeof(crashFrame)/sizeof(int));
    
    // event input 상황 end
    
    int target_size;
    int process_size=0;
    Mat roi_left;
    Mat roi_right;
    bool track_flag=false;
    Rect target_car;
    Rect process_car;
    bool init_flag;
    bool indicatorFlag;
    
    //targetVideo="change_test.mp4";   //영상 처리 작업을 수행할 비디오 파일의 경로
    
    vector<int> vector_x;
    
    VideoCapture capture;
    //capture.open("change_test.mp4");
    capture.open(targetVideo);
    
    CascadeClassifier classifier("cars.xml");
    
    Mat base_frame; //기본 프레임
    Mat frame;
    Ptr<Tracker> tracker=TrackerCSRT::create(); //tracker 객체
    
    if(classifier.empty()){
        cout<<"xml load fail"<<endl;
        return -1;
    }
    if (!capture.isOpened())
        return -1;
    //namedWindow("File Play", WINDOW_AUTOSIZE);

    while(1){
        
        capture>>frame;
        if(frame.empty()){
            extractVideo(videoEvents);
            return 0;
        }
        base_frame=frame;

        frameCount++;
        
        if (waitKey(30) >= 0){
           // extractVideo(videoEvents);
            break;
            
        }  //27 = Esc, 32 = Space key
            
        roi_left=frame(Rect(0, 2*frame.rows/5, 7*frame.cols/10, 3*frame.rows/5));
        
        roi_right=frame(Rect(3*frame.cols/10, 2*frame.rows/5, 7*frame.cols/10, 2*frame.rows/5));
        
        // detecting 은 항상 수행
        
        target_car=detectCars(&roi_left, &frame, classifier, &track_flag);
        
        
        if(track_flag==true){// track_flag가 true면 일단 track 수행 이후 init update 결정
            
            target_size=target_car.width*target_car.height;
            
            if(process_size==0){
                //check
                
                if(target_car.y<10){
                    continue;
                }
                
                init_flag=true;
                process_car=trackCars(&roi_left, &frame, &init_flag, &track_flag, target_car, tracker, &process_size, classifier,&vector_x, &indicatorFlag);
                
            }else{
                //location 비교 코드가 들어갈 곳
                Point standard_Point=Point(0, process_car.height);
                Point process_point=Point(process_car.x/2, process_car.y/2);
                Point target_point=Point(target_car.x/2, target_car.y/2);
                
                double distanceOfProcess=Distance(standard_Point, Point(process_car.x,process_car.y));
                double distanceOfNewTarget=Distance(standard_Point, Point(target_car.x, target_car.y));
                double distanceOfcars=Distance(process_point, target_point);
                
                if((distanceOfProcess>distanceOfNewTarget)&&(distanceOfcars>150)){//location 비교하여 init여부를 결정 하도록 만들것//비교한 값들의 차이가 적으면 같은것으로 간주
                    //check02
                    if(target_car.y<10){
                        continue;
                    }
                    
                    
                    string str_distanceOfPro = to_string(distanceOfProcess);
                    string str_distanceOfNew=to_string(distanceOfNewTarget);
                    cout<<"the distance of the two cars : "<<distanceOfcars<<endl;
                    
                    init_flag=true;
                    process_car=trackCars(&roi_left, &frame, &init_flag, &track_flag, target_car, tracker, &process_size, classifier,&vector_x,&indicatorFlag);
                    
                    
                }else{// update
                    init_flag=false;
                    
                    process_car=trackCars(&roi_left, &frame, &init_flag, &track_flag, process_car, tracker, &process_size, classifier,&vector_x,&indicatorFlag);
                }
            
            }
            
        }
        putText(base_frame, "frame count :"+to_string(frameCount), Point(100,100), FONT_HERSHEY_DUPLEX, 1, Scalar(255,255,255));
    
        //check check
        
        imshow("base_frame", base_frame);
        //imshow("base_frame", frame);
        
    }
    return 0;
}

//=============//=============//=============//=============//=============//=============//=============//=============

void crashToEvent(string path, int crashArr[], int sizeOfArr){
    int targetIndex;
    vector<int> v;
    
    for(int i=0; i<sizeOfArr; i++){
        targetIndex=i;
        for(int j=i; j<sizeOfArr-1; j++){
            if(crashArr[j+1]-crashArr[j]>20){
                i=j;
                break;
            }
        }
        v.push_back(crashArr[targetIndex]);
    }
    
    for(int i=0; i<v.size(); i++){
        videoEvents.push_back(VideoEvent(path, "crash", v.at(i)-50,v.at(i)+50));
    }
}

//=============//=============//=============//=============//=============//=============//=============//=============
void extractVideo(vector<VideoEvent> events){   // 이벤트가 영상의 범위를 벗어나거나, 두 이벤트가 겹치는 경우에 대한 예외처리 수행

    for(int i=0; i<events.size(); i++){
        VideoCapture videoCapture(events.at(i).getPath());  //check
        if (!videoCapture.isOpened()) {
            cout << "Can't open video !!!" << std::endl;
            return;
        }
        videoCapture.set(10, 0);
        
        VideoWriter videoWriter;
        int current_frame=0;
        
        int startFrame=events.at(i).getStartFrame();
        int endFrame=events.at(i).getEndFrame();
        
        if(startFrame<0){   //영상의 범위를 벗어났을 경우 예외처리
            startFrame=0;
            endFrame=100;
        
        }
        if(endFrame>frameCount){
            endFrame=frameCount;
            startFrame=endFrame-100;
        }
        
        Mat videoFrame;
        
        float videoFPS = videoCapture.get(CAP_PROP_FPS);
        int videoWidth = videoCapture.get(CAP_PROP_FRAME_WIDTH);
        int videoHeight = videoCapture.get(CAP_PROP_FRAME_HEIGHT);

        cout << "Video Info" << endl;
        cout << "video FPS : " << videoFPS << endl;
        cout << "video width : " << videoWidth << endl;
        cout << "video height : " << videoHeight << endl;
            
        //event 이름으로 파일 출력
        //videoWriter.open(events.at(i).getTitle()+to_string(i)+".avi", VideoWriter::fourcc('M', 'J', 'P', 'G'),
          //  videoFPS , cv::Size(videoWidth, videoHeight), true);
        
        //요구사항!
        videoWriter.open("./ExtractVideo"+events.at(i).getPath()+"-"+events.at(i).getTitle()+to_string(i)+".avi", VideoWriter::fourcc('h','2','6','4'),
            videoFPS , cv::Size(videoWidth, videoHeight), true);
        //영상 저장 셋팅 실패시
        if (!videoWriter.isOpened())
        {
            cout << "Can't write video !!! check setting" << endl;
            return;
        }
 

        namedWindow("video");
        videoCapture.set(CAP_PROP_POS_FRAMES, startFrame);
        
        while (true) {
            
            current_frame++;
            
            if(current_frame>(endFrame-startFrame)){
                break;  // 사용자가 지정한 endFrame까지 영상이 진행 되었을 경우 종료
            }
            
            //VideoCapture로 부터 프래임을 받아온다
            videoCapture >> videoFrame;

            //캡쳐 화면이 없는 경우는 Video의 끝인 경우
            if (videoFrame.empty()) {
                cout << "Video END" << std::endl;
                break;
            }

        
            //받아온 Frame을 저장한다.
            videoWriter << videoFrame;
                
            //window에 frame 출력.
            
            //check check
            
            imshow("video", videoFrame);

            //'ESC'키를 누르면 종료된다.
            //FPS를 이용하여 영상 재생 속도를 조절하여준다.
            if (waitKey(1000 / videoFPS) == 27) {
                cout << "Stop video record" << endl;
                break;
            }
        }

        //생성하였던 윈도우를 제거합니다.
        destroyWindow("video");
        
    }
}

//=========================================================================================




Rect detectCars(Mat *detectFrame, Mat *baseFrame ,CascadeClassifier classifier, bool *track_flag){
    
    int tempIndex;  //cars배열의 값을 임시로 저장할 index
    int targetIndex=0;    //cars배열중 target이 되는 index
    Rect resultCars;
    vector<Rect> cars;
    int tempSize;
    int targetSize=0;
    Mat tempFrame=*baseFrame;
    Mat grayFrame;
    

    cvtColor(*detectFrame, grayFrame, COLOR_BGR2GRAY);
    
    classifier.detectMultiScale(grayFrame, cars);
    if(cars.empty()){
        cout<<"fail to find cars"<<endl;
    }else{
        
    for(int i=0; i<cars.size(); i++){
        tempIndex=i;
        int width=cars[tempIndex].width;
        int height=cars[tempIndex].height;
        tempSize=width*height;
        
        if(tempSize>50000&&targetSize<tempSize){
            targetSize=tempSize;
            targetIndex=tempIndex;
            *track_flag=true;
            resultCars=cars[targetIndex];
        }else{
            continue;
        }
    }
        
        
        Rect drawCars=resultCars;
        drawCars.y=resultCars.y+2*tempFrame.rows/5;
        
        rectangle(*baseFrame,drawCars, Scalar( 0, 0, 255 ), 4, 1 );
        putText(*baseFrame, "Detecting car", Point(drawCars.x,drawCars.y+30), FONT_HERSHEY_DUPLEX, 1, Scalar(0,0,255));
    }
    
    return resultCars;
}


// tracker check

Rect trackCars(Mat *detectFrame, Mat *baseFrame,bool *init_flag, bool *track_flag,Rect target, Ptr<Tracker> tracker, int *process_size, CascadeClassifier classifier, vector<int> *vector_x, bool *indicatorFlag){  //vectorsave
    
    vector<Rect> cars;
    
    Mat frame=*detectFrame;
    Mat tempFrame=*baseFrame;
    Mat grayFrame;
    
    int direction;
    cvtColor(*detectFrame, grayFrame, COLOR_BGR2GRAY);
    
    if(target.x/2<tempFrame.cols/2){
        direction=0;
    }else{
        direction=1;
    }
    
    if(*init_flag==true){
        
        startFrame=frameCount;  // 영상 편집에 사용될 start Frmae -> event 객체 생성하여 저장
        *indicatorFlag=false;
    
        vector_x->clear(); //delete
        //vector_mat->clear();    //vectorsave
        
        tracker->init(grayFrame, target); //parameter flag 가 false 이면 tracker 초기화 detection결과를 전달하고 이를 트래킹 하도록 초기화
        vector_x->push_back(target.x/2);   //delete
        
        
       // tracker->init(*baseFrame, target); //parameter flag 가 false 이면 tracker 초기화 detection결과를 전달하고 이를 트래킹 하도록 초기화
        Rect drawCars=target;
        drawCars.y=target.y+2*tempFrame.rows/5;
        rectangle(*baseFrame, drawCars, Scalar( 0, 255, 0 ), 2, 1 );
        
        Mat temp=frame(target);
        //cvtColor(temp, temp, COLOR_BGR2GRAY);
        //classifier.detectMultiScale(temp, cars);
    }else{
        tracker->update(grayFrame, target);   //flag param true 이면 트래킹 이어서 수행 -> 어떤 객체를 트래킹 해야할지 init되었으므로 트래킹 이어서 수행
        
        //tracker->update(grayFrame, target);   //flag param true 이면 트래킹 이어서 수행 -> 어떤 객체를 트래킹 해야할지 init되었으므로 트래킹 이어서 수행
        vector_x->push_back(target.x/2);   //delete
        //vector_mat->push_back(frame); vectorsave
        
        Rect drawCars=target;
        drawCars.y=target.y+2*tempFrame.rows/5;
        

        rectangle(*baseFrame, drawCars, Scalar( 255, 0, 0 ), 2, 1 );
        
        putText(*baseFrame, "Tracking car", Point(drawCars.x,drawCars.y+30), FONT_HERSHEY_DUPLEX, 1, Scalar(255,255,255));
        
        //delete start
        
        string str_locationX = to_string(drawCars.x/2);
        putText(*baseFrame, "location x : "+str_locationX, Point(drawCars.x,drawCars.y+60), FONT_HERSHEY_DUPLEX, 1, Scalar(255,255,255));
        
        Mat temp=tempFrame(drawCars);
        
        if(*indicatorFlag==false){
       *indicatorFlag=findIndicator(temp, direction);  //findIndicator는 indicatorflag가 false일때만 true를 검사 -> threshold 값 체크할것
        }
        
        
        //true -> false로 변경 해야함 !!! && 해당 조건식 충족한 상태로 반복되는 문제 해결해야함 딱 한번만 수행하도록 설정 해야함
        if(vector_x->back()>250&&vector_x->front()<100&&*indicatorFlag==true){    //내 차선으로 진입할 경우 그리고 indicator 가 false일 경우 Event 객체를 생성하여 videowrite method 에 전달
            endFrame=frameCount;
            cout<<"start frame : "<<startFrame<<endl;
            cout<<"end Frame: "<<endFrame<<endl;
            
            //check
            videoEvents.push_back(VideoEvent(targetVideo, "RightIndicator", startFrame-20, endFrame+20));
           // extractVideo(videoEvents);
            
                                        
            *track_flag=false;
            
        }
        
    
        //delete end
        //cvtColor(temp, temp, COLOR_BGR2GRAY);
        
        imshow("tracking cars", temp);
        
    }
    
    *process_size=target.width*target.height;
    int tempSize=*process_size;
    
    //check point
    if(*process_size<90000||vector_x->back()>250){//if(cars.empty()||*process_size<100000){
        *process_size=0;
        *track_flag=false;
    }else{
        *track_flag=true;
    }
    
    return target;
}

//=========================================================================================

double Distance(Point p1, Point p2){
    double distance=sqrt(pow(p1.x-p2.x,2)+pow(p1.y-p2.y,2));
    return distance;
}
 
bool findIndicator(Mat target, int direction){
    double percent;
    double count=0;
    Mat img_hsv;
    bool resultFlag=false;
    double threshold=0.1;
    
    //target=target(Rect(target.rows/2,target.cols/4,target.rows/2, 2*target.cols/4));
    
    
    if(direction==0){   // tracking중인 차량이 좌측-> 우측 방향인 경우
        target=target(Rect(target.rows/2, target.cols/4, target.rows/2, 2*target.cols/4));
    }else{ //tracking 중인 차량이 우측->좌측인 경우
        target=target(Rect(0, target.cols/4, target.rows/2, 2*target.cols/4));
    }
    
    double width=target.cols;
    double height=target.rows;
    
    int bpp=target.channels();
    double imageSize=width*height*bpp;
    
    cvtColor(target, img_hsv, COLOR_BGR2HSV);
    
    Mat yellow_mask;
    Mat yellow_image;
    
    Scalar lower_yellow=Scalar(20,20,100);
    Scalar upper_yellow=Scalar(32,255,255);
    
    inRange(img_hsv, lower_yellow, upper_yellow, yellow_mask);
    bitwise_and(target, target, yellow_image, yellow_mask);
    
    for(int i=0; i<height; i++){
        for(int j=0; j<width*3; j++){
            if(yellow_image.at<uchar>(i, j)<200){
                yellow_image.at<uchar>(i, j)=0;
            }else{
                count++;
            }
        }
    }
    
    percent=(count/imageSize)*100;
  
    imshow("yellow_image", yellow_image);
    
    imshow("img_hsv", img_hsv);
    
    
    if(percent>threshold){
        resultFlag=true;
    }
    
    //cout<<"result is : "<<resultFlag<<"  ";
    //cout<<percent<<"%"<<endl;
    return resultFlag;

}

 
//end of change line
