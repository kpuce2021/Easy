#include <opencv2/core/core.hpp>
#include <opencv2/opencv.hpp> // opencv 기본적인 API가 들어있는 헤더파일
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#define PI 3.141592

using namespace std;
using namespace cv;

int main(int argc, char** argv)
{
    //차선 검출
    VideoCapture capture;
    capture.open("highway.mp4");

    if (!capture.isOpened())
        return -1;

    cout << "Device opening..." << endl;
    namedWindow("File Play", WINDOW_AUTOSIZE);
    Mat frame;
    vector<Vec2f> lines;
    Point banishP;
    Point pt1, pt2;
    vector<Vec2f>linesR;
    float resultLine[2];

    for (;;) {
        //Mat frame;
        Mat thres, canny;
        Mat Roi1, Roi2, Roi;

        capture >> frame;

        imshow("File Play", frame);
        if (waitKey(30) >= 0)  //27 = Esc, 32 = Space key
            break;

        cvtColor(frame, thres, COLOR_BGR2GRAY);
        GaussianBlur(thres, thres, Size(3, 3), 0, 3);
        Canny(thres, canny, 85, 110, 3);
        imshow("canny", canny);
        printf("%d, %d\n", frame.rows, frame.cols);

        Roi1 = canny(Rect(0, 0, frame.cols / 2, frame.rows));
        Roi2 = canny(Rect(frame.cols / 2, 0, frame.cols / 2, frame.rows));
        Roi = canny(Rect(0, 3 * frame.rows / 5, frame.cols, 2 * frame.rows / 5));

        //imshow("Roi1", Roi1);
        //imshow("Roi2", Roi2);
        imshow("Roi", Roi);

        // Hughlines function for line detection
        HoughLines(Roi, lines, 1, PI / 180, 200);

        // draw lines
        Mat result(canny.rows, canny.cols, CV_8U, Scalar(255));
        cout << "Lines detected: " << lines.size() << endl;

        // draw lines repeatedly with line vectors
        vector<Vec2f>::const_iterator it = lines.begin();

        while (it != lines.end()) {
            // 1st factor = rho distance
            float rho = (*it)[0];
            // 2nd factor = delta degree
            float theta = (*it)[1];

            // vertical line
            if (theta < PI || theta > PI / 2) {
                // intersection of the line in the first row
                Point pt1(rho / cos(theta), 0);
                // intersection of the line in the last row
                Point pt2((rho - result.rows*sin(theta)) / cos(theta), result.rows);

                pt1.y = pt1.y + 3 * result.rows / 5;
                pt2.y = pt2.y + 3 * result.rows / 5;
                
                line(frame, pt1, pt2, Scalar(255, 0, 0), 5);
            }

            // horizontal line
            else {
                // intersection of the line in the first column
                Point pt1(0, rho / sin(theta));
                // intersection of the line in the last column
                Point pt2(result.cols, (rho - result.cols*cos(theta)) / sin(theta));

                pt1.y = pt1.y + 3 * result.rows / 5;
                pt2.y = pt2.y + 3 * result.rows / 5;
                
                line(frame, pt1, pt2, Scalar(0, 0, 0), 5);
            }

            cout << "line: (" << rho << "," << theta << ")\n";
            ++it;
        }
        imshow("Example", frame);
    }
}
