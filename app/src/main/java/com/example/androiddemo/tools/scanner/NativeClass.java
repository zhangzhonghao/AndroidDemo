package com.example.androiddemo.tools.scanner;

import android.graphics.Bitmap;

import com.example.androiddemo.tools.scanner.helpers.ImageUtils;
import com.example.androiddemo.tools.scanner.helpers.MathUtils;
import com.example.androiddemo.tools.scanner.helpers.PerspectiveTransformation;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NativeClass {

    static {
        System.loadLibrary("opencv_java4");
    }

    private static final double AREA_LOWER_THRESHOLD = 0.03;
    private static final double AREA_UPPER_THRESHOLD = 0.95;
    private static final double DOWNSCALE_IMAGE_SIZE = 800f;

    public Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        PerspectiveTransformation perspective = new PerspectiveTransformation();
        MatOfPoint2f rectangle = new MatOfPoint2f();
        rectangle.fromArray(new Point(x1, y1), new Point(x2, y2), new Point(x3, y3), new Point(x4, y4));
        Mat dstMat = perspective.transform(ImageUtils.bitmapToMat(bitmap), rectangle);
        return ImageUtils.matToBitmap(dstMat);
    }

    private static Comparator<MatOfPoint2f> AreaDescendingComparator = new Comparator<MatOfPoint2f>() {
        public int compare(MatOfPoint2f m1, MatOfPoint2f m2) {
            double area1 = Imgproc.contourArea(m1);
            double area2 = Imgproc.contourArea(m2);
            return Double.compare(area2, area1);
        }
    };

    public MatOfPoint2f getPoint(Bitmap bitmap) {
        Mat rgba = ImageUtils.bitmapToMat(bitmap);
        Mat bgr = new Mat();
        Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR);
        rgba.release();
        MatOfPoint2f result = getPoint(bgr);
        bgr.release();
        return result;
    }

    public MatOfPoint2f getPoint(Mat src) {
        double ratio = DOWNSCALE_IMAGE_SIZE / Math.max(src.width(), src.height());
        Size downscaledSize = new Size(src.width() * ratio, src.height() * ratio);
        Mat downscaled = new Mat(downscaledSize, src.type());
        Imgproc.resize(src, downscaled, downscaledSize);

        List<MatOfPoint2f> rectangles = getPoints(downscaled);
        downscaled.release();
        if (rectangles.isEmpty()) {
            return null;
        }

        // Pick best rectangle: large area + near center
        double imgW = src.width() * ratio;
        double imgH = src.height() * ratio;
        double cx = imgW / 2.0, cy = imgH / 2.0;

        MatOfPoint2f best = null;
        double bestScore = -1;
        for (MatOfPoint2f r : rectangles) {
            double area = Imgproc.contourArea(r);
            Point[] pts = r.toArray();
            double rcx = 0, rcy = 0;
            for (Point p : pts) { rcx += p.x; rcy += p.y; }
            rcx /= 4; rcy /= 4;
            double dist = Math.sqrt((rcx - cx) * (rcx - cx) + (rcy - cy) * (rcy - cy));
            // Strong center bias: penalize off-center rectangles, prefer ones near image center
            double centerRatio = 1.0 - Math.min(1.0, dist / Math.max(imgW, imgH));
            double score = area * centerRatio * centerRatio;
            if (score > bestScore) {
                bestScore = score;
                best = r;
            }
        }

        MatOfPoint2f result = MathUtils.scaleRectangle(best, 1f / ratio);
        return result;
    }

    public List<MatOfPoint2f> getPoints(Mat src) {
        // Convert to grayscale — handles the full luminance range
        Mat gray0 = new Mat();
        Imgproc.cvtColor(src, gray0, Imgproc.COLOR_BGR2GRAY);

        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint2f> rectangles = new ArrayList<>();
        int srcArea = src.rows() * src.cols();

        Mat edgeImg = new Mat();
        Mat hierarchy = new Mat();
        Mat kernel = Mat.ones(new Size(3, 3), CvType.CV_8U);

        // Pass 1: Canny low thresholds
        Imgproc.GaussianBlur(gray0, edgeImg, new Size(3, 3), 0);
        Imgproc.Canny(edgeImg, edgeImg, 5, 15);
        Imgproc.dilate(edgeImg, edgeImg, kernel);
        Imgproc.findContours(edgeImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        addRectangles(contours, rectangles, srcArea);

        // Pass 2: Canny medium thresholds
        Imgproc.GaussianBlur(gray0, edgeImg, new Size(3, 3), 0);
        Imgproc.Canny(edgeImg, edgeImg, 15, 45);
        Imgproc.dilate(edgeImg, edgeImg, kernel);
        Imgproc.findContours(edgeImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        addRectangles(contours, rectangles, srcArea);

        // Pass 3: Canny higher thresholds
        Imgproc.GaussianBlur(gray0, edgeImg, new Size(3, 3), 0);
        Imgproc.Canny(edgeImg, edgeImg, 30, 90);
        Imgproc.dilate(edgeImg, edgeImg, kernel);
        Imgproc.findContours(edgeImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        addRectangles(contours, rectangles, srcArea);

        // Pass 4: Adaptive threshold (handles uneven lighting)
        Imgproc.adaptiveThreshold(gray0, edgeImg, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Imgproc.findContours(edgeImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        addRectangles(contours, rectangles, srcArea);

        gray0.release();
        edgeImg.release();
        hierarchy.release();
        kernel.release();

        return rectangles;
    }

    private void addRectangles(List<MatOfPoint> contours, List<MatOfPoint2f> rectangles, int srcArea) {
        int rectCount = 0;
        int fourV = 0;
        int areaFail = 0;
        double minArea = srcArea * AREA_LOWER_THRESHOLD;

        // Try multiple epsilon values to catch the document contour
        double[] epsilons = {0.01, 0.02, 0.04, 0.06};
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contourFloat = MathUtils.toMatOfPointFloat(contour);
            double arcLen = Imgproc.arcLength(contourFloat, true);

            MatOfPoint2f bestApprox = null;
            for (double eps : epsilons) {
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(contourFloat, approx, arcLen * eps, true);
                if (approx.rows() == 4) {
                    double area = Math.abs(Imgproc.contourArea(approx));
                    if (area >= minArea && (bestApprox == null || area > Math.abs(Imgproc.contourArea(bestApprox)))) {
                        bestApprox = approx;
                    }
                }
            }

            if (bestApprox != null) {
                rectangles.add(bestApprox);
                rectCount++;
            } else {
                // Check if any approx has 4 vertices
                MatOfPoint2f approx02 = new MatOfPoint2f();
                Imgproc.approxPolyDP(contourFloat, approx02, arcLen * 0.02, true);
                if (approx02.rows() == 4) {
                    fourV++;
                    if (Math.abs(Imgproc.contourArea(approx02)) < minArea) areaFail++;
                }
            }
        }
        android.util.Log.d("NativeClass", "pass: contours=" + contours.size() + " rects=" + rectCount + " fourV@02=" + fourV + " areaFail=" + areaFail);
    }

    private boolean isRectangle(MatOfPoint2f polygon, int srcArea) {
        if (polygon.rows() != 4) return false;
        double area = Math.abs(Imgproc.contourArea(polygon));
        return area >= srcArea * AREA_LOWER_THRESHOLD;
    }
}
