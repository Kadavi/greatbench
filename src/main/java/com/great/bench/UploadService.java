package com.great.bench;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class UploadService
{
    static { System.load("C:/Users/Grant Dawson/Documents/opencv/build/java/x64/opencv_java246.dll"); }

    @Async
    public Future<String> processImage(String path)
    {

        return new AsyncResult<String>(DetectCircle(path));
    }

    public String DetectCircle(String path) {

        List<Point> totalCircles = new ArrayList(),
                totalTriangles = new ArrayList();

        Mat src = Highgui.imread(path, CvType.CV_32FC4);

        Mat circles = new Mat();

        Mat dst = Mat.zeros(src.size(), CvType.CV_32FC4);

        Mat triangleResult = Mat.zeros(src.size(), CvType.CV_32FC4),
                circleResult = Mat.zeros(src.size(), CvType.CV_32FC4);

        Core.inRange(src, new Scalar(205, 205, 205), new Scalar(245, 245, 245), circleResult); // White circles

        Imgproc.GaussianBlur(circleResult, circleResult, new Size(7, 7), 7);

        //Highgui.imwrite("C:/Users/Grant Dawson/IdeaProjects/greatbench/src/test/java/com/great/bench/circleResultOne.jpg", circleResult);

        Core.inRange(src, new Scalar(210,210,230), new Scalar(235,235,240), triangleResult); // White circles

        Imgproc.GaussianBlur(triangleResult, triangleResult, new Size(19, 19), 19);

        Core.addWeighted(circleResult, 1.0, triangleResult, 1.0, 50.0, circleResult);

        //Highgui.imwrite("C:/Users/Grant Dawson/IdeaProjects/greatbench/src/test/java/com/great/bench/circleResultTwo.jpg", circleResult);

        Core.inRange(src, new Scalar(15,25,150), new Scalar(65,80,240), triangleResult); // Red masses

        //Highgui.imwrite("C:/Users/Grant Dawson/IdeaProjects/greatbench/src/test/java/com/great/bench/triangleResult.jpg", triangleResult);

        int iCannyUpperThreshold = 20;
        int iMinDistance = 20;
        int iMinRadius = 5;
        int iMaxRadius = 100;
        int iAccumulator = 50;

        Imgproc.HoughCircles(circleResult, circles, Imgproc.CV_HOUGH_GRADIENT,
                1, iMinDistance, iCannyUpperThreshold, iAccumulator,
                iMinRadius, iMaxRadius);

        if (circles.cols() > 0) {
            for (int x = 0; x < circles.cols(); x++)
            {
                double vCircle[] = circles.get(0,x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                int radius = (int)Math.round(vCircle[2]);

                Core.circle(dst, pt, radius, new Scalar(255, 255, 255), 1);
                totalCircles.add(pt);
                //System.out.println("Circle: " + pt);

            }
        }

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint2f approx = new MatOfPoint2f();

        Imgproc.findContours(triangleResult, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);

        Imgproc.drawContours(dst, contours, 0, new Scalar(255,0,0), 2);

        for (int i = 0 ; i < contours.size() ; i++) {

            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx, 0, true);

            if (approx.toList().size() > 1)  {

                int averageCenterX = 0, averageCenterY = 0;

                for (int j = 0 ; j < approx.toList().size() ; j++) {

                    averageCenterX += approx.toList().get(j).x;
                    averageCenterY += approx.toList().get(j).y;

                }

                totalTriangles.add(new Point(averageCenterX / approx.toList().size(),
                        averageCenterY / approx.toList().size()));

                //System.out.println("Triangle: " + approx.toList());

            }

        }

        String result = null;

        for (int i = 0; i < totalCircles.size(); i++) {

            for (int j = 0; j < totalTriangles.size(); j++) {

                double distanceX = totalCircles.get(i).x - totalTriangles.get(j).x;
                double distanceY = totalCircles.get(i).y - totalTriangles.get(j).y;

                if (Math.abs(distanceX) < 10 && Math.abs(distanceY) < 10) {

                    //Highgui.imwrite("C:/Users/Grant Dawson/IdeaProjects/greatbench/src/test/java/com/great/bench/WEESALTS.jpg", dst);

                    /*
                    result = "\nMatch!\n" +
                             "(Circle) " + totalCircles.get(i).x + " " + totalCircles.get(i).y + "\n" +
                             "(Triangle) " + totalTriangles.get(j).x + " " + totalTriangles.get(j).y;
                    */

                    result = totalCircles.get(i).x + "," + totalCircles.get(i).y;

                    break;

                }

            }

        }

        return result;
    }
}