package com.example.androiddemo.tools.scanner;

import android.graphics.Bitmap;

import com.example.androiddemo.tools.scanner.helpers.ImageUtils;
import com.example.androiddemo.tools.scanner.helpers.MathUtils;
import com.example.androiddemo.tools.scanner.helpers.PerspectiveTransformation;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class NativeClass {

    static {
        System.loadLibrary("opencv_java4");
    }

    private static final double AREA_LOWER_THRESHOLD = 0.03;
    private static final double AREA_UPPER_THRESHOLD = 0.95;
    private static final double DOWNSCALE_IMAGE_SIZE = 800f;
    private static final double MIN_RECTANGULARITY = 0.55;
    private static final double MAX_ANGLE_COSINE = 0.45;
    private static final double A4_ASPECT_RATIO = 1.414;
    private static final double MIN_DOCUMENT_SCORE = 0.010;
    private static final double MIN_OPPOSITE_SIDE_PARALLEL = 0.88;
    private static final double MAX_ADJACENT_SIDE_COSINE = 0.36;
    private static final double PREVIEW_EDGE_MARGIN_RATIO = 0.035;

    public static class DetectionResult {
        public final MatOfPoint2f points;
        public final double score;

        DetectionResult(MatOfPoint2f points, double score) {
            this.points = points;
            this.score = score;
        }
    }

    public Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        PerspectiveTransformation perspective = new PerspectiveTransformation();
        MatOfPoint2f rectangle = new MatOfPoint2f();
        rectangle.fromArray(new Point(x1, y1), new Point(x2, y2), new Point(x3, y3), new Point(x4, y4));
        Mat dstMat = perspective.transform(ImageUtils.bitmapToMat(bitmap), rectangle);
        return ImageUtils.matToBitmap(dstMat);
    }

    public MatOfPoint2f getPoint(Bitmap bitmap) {
        DetectionResult result = getScoredPoint(bitmap);
        return result != null ? result.points : null;
    }

    public DetectionResult getScoredPoint(Bitmap bitmap) {
        Mat rgba = ImageUtils.bitmapToMat(bitmap);
        Mat bgr = new Mat();
        Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR);
        rgba.release();
        DetectionResult result = getScoredPoint(bgr);
        bgr.release();
        return result;
    }

    public MatOfPoint2f getPoint(Mat src) {
        DetectionResult result = getScoredPoint(src);
        return result != null ? result.points : null;
    }

    public DetectionResult getScoredPoint(Mat src) {
        double ratio = Math.min(1.0, DOWNSCALE_IMAGE_SIZE / Math.max(src.width(), src.height()));
        Size downscaledSize = new Size(src.width() * ratio, src.height() * ratio);
        Mat downscaled = new Mat(downscaledSize, src.type());
        Imgproc.resize(src, downscaled, downscaledSize);

        List<MatOfPoint2f> rectangles = getPoints(downscaled);
        if (rectangles.isEmpty()) {
            downscaled.release();
            return null;
        }

        double imgW = src.width() * ratio;
        double imgH = src.height() * ratio;
        double cx = imgW / 2.0;
        double cy = imgH / 2.0;

        MatOfPoint2f best = null;
        double bestScore = -1;
        for (MatOfPoint2f rectangle : rectangles) {
            double score = scoreRectangle(rectangle, downscaled, imgW, imgH, cx, cy);
            if (score > bestScore) {
                bestScore = score;
                best = rectangle;
            }
        }

        downscaled.release();
        if (best == null || bestScore < MIN_DOCUMENT_SCORE) {
            return null;
        }

        MatOfPoint2f result = MathUtils.scaleRectangle(orderCorners(best), 1f / ratio);
        return new DetectionResult(result, bestScore);
    }

    public List<MatOfPoint2f> getPoints(Mat src) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Mat enhanced = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(gray, enhanced);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(enhanced, blurred, new Size(5, 5), 0);

        List<MatOfPoint2f> rectangles = new ArrayList<>();
        int srcArea = src.rows() * src.cols();
        Mat kernel3 = Mat.ones(new Size(3, 3), CvType.CV_8U);
        Mat kernel5 = Mat.ones(new Size(5, 5), CvType.CV_8U);

        addWhitePaperRegionPass(gray, rectangles, srcArea);
        addInferredA4LinePass(blurred, rectangles, srcArea);

        addEdgesPass(blurred, rectangles, srcArea, 20, 60, kernel3);
        addEdgesPass(blurred, rectangles, srcArea, 45, 135, kernel3);
        addEdgesPass(blurred, rectangles, srcArea, 70, 210, kernel3);

        Mat adaptive = new Mat();
        Imgproc.adaptiveThreshold(enhanced, adaptive, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 7);
        Imgproc.morphologyEx(adaptive, adaptive, Imgproc.MORPH_CLOSE, kernel5);
        findRectangles(adaptive, rectangles, srcArea);

        Mat otsu = new Mat();
        Imgproc.threshold(enhanced, otsu, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.morphologyEx(otsu, otsu, Imgproc.MORPH_CLOSE, kernel5);
        findRectangles(otsu, rectangles, srcArea);

        gray.release();
        enhanced.release();
        blurred.release();
        adaptive.release();
        otsu.release();
        kernel3.release();
        kernel5.release();

        return rectangles;
    }

    private void addInferredA4LinePass(Mat gray, List<MatOfPoint2f> rectangles, int srcArea) {
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 45, 140);
        clearPreviewEdges(edges);

        Mat lines = new Mat();
        int minImageSide = Math.min(gray.rows(), gray.cols());
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180.0, 55,
                minImageSide * 0.18, minImageSide * 0.035);

        List<LineSegment> segments = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            double[] values = lines.get(i, 0);
            if (values == null || values.length < 4) continue;

            LineSegment segment = new LineSegment(
                    new Point(values[0], values[1]),
                    new Point(values[2], values[3])
            );
            if (isPreviewEdgeSegment(segment, gray.cols(), gray.rows())) {
                continue;
            }
            if (segment.length >= minImageSide * 0.18) {
                segments.add(segment);
            }
        }

        segments.sort((a, b) -> Double.compare(b.length, a.length));
        int limit = Math.min(segments.size(), 32);
        double minArea = srcArea * AREA_LOWER_THRESHOLD;
        double maxArea = srcArea * AREA_UPPER_THRESHOLD;

        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                LineSegment first = segments.get(i);
                LineSegment second = segments.get(j);
                if (Math.abs(first.unitX * second.unitX + first.unitY * second.unitY) > 0.28) {
                    continue;
                }

                Point corner1 = distance(first.p1, second.p1) < distance(first.p1, second.p2) ? second.p1 : second.p2;
                Point corner2 = distance(first.p2, second.p1) < distance(first.p2, second.p2) ? second.p1 : second.p2;
                Point firstCorner = distance(first.p1, corner1) < distance(first.p2, corner1) ? first.p1 : first.p2;
                Point secondCorner = distance(second.p1, firstCorner) < distance(second.p2, firstCorner) ? second.p1 : second.p2;
                if (distance(firstCorner, secondCorner) > minImageSide * 0.08) {
                    continue;
                }
                Point corner = midpoint(firstCorner, secondCorner);

                Point firstFar = distance(first.p1, corner) > distance(first.p2, corner) ? first.p1 : first.p2;
                Point secondFar = distance(second.p1, corner) > distance(second.p2, corner) ? second.p1 : second.p2;
                double ux = firstFar.x - corner.x;
                double uy = firstFar.y - corner.y;
                double vx = secondFar.x - corner.x;
                double vy = secondFar.y - corner.y;
                double uLen = Math.hypot(ux, uy);
                double vLen = Math.hypot(vx, vy);
                if (uLen < minImageSide * 0.12 || vLen < minImageSide * 0.12) {
                    continue;
                }

                ux /= uLen;
                uy /= uLen;
                vx /= vLen;
                vy /= vLen;

                addInferredA4Candidates(rectangles, edges, corner, ux, uy, vx, vy, uLen, vLen, minArea, maxArea);
            }
        }

        edges.release();
        lines.release();
    }

    private void addInferredA4Candidates(List<MatOfPoint2f> rectangles, Mat edges, Point corner,
                                         double ux, double uy, double vx, double vy,
                                         double uLen, double vLen, double minArea, double maxArea) {
        double[][] sizes;
        if (uLen >= vLen) {
            double longSide = Math.max(uLen, vLen * A4_ASPECT_RATIO);
            sizes = new double[][]{
                    {longSide, longSide / A4_ASPECT_RATIO},
                    {vLen * A4_ASPECT_RATIO, vLen}
            };
        } else {
            double longSide = Math.max(vLen, uLen * A4_ASPECT_RATIO);
            sizes = new double[][]{
                    {longSide / A4_ASPECT_RATIO, longSide},
                    {uLen, uLen * A4_ASPECT_RATIO}
            };
        }

        for (double[] size : sizes) {
            MatOfPoint2f candidate = buildRectangle(corner, ux, uy, vx, vy, size[0], size[1]);
            if (candidate == null) continue;
            if (!isInsideImage(candidate, edges.cols(), edges.rows())) continue;

            double area = Math.abs(Imgproc.contourArea(candidate));
            if (area < minArea || area > maxArea) continue;
            if (!hasRegularDocumentShape(candidate)) continue;
            if (scoreEdgeSupport(candidate, edges) < 0.42) continue;

            MatOfPoint2f ordered = orderCorners(candidate);
            if (!isDuplicate(ordered, rectangles)) {
                rectangles.add(ordered);
            }
        }
    }

    private MatOfPoint2f buildRectangle(Point corner, double ux, double uy, double vx, double vy, double uLen, double vLen) {
        Point p0 = corner;
        Point p1 = new Point(corner.x + ux * uLen, corner.y + uy * uLen);
        Point p3 = new Point(corner.x + vx * vLen, corner.y + vy * vLen);
        Point p2 = new Point(p1.x + vx * vLen, p1.y + vy * vLen);

        MatOfPoint2f rectangle = new MatOfPoint2f();
        rectangle.fromArray(p0, p1, p2, p3);
        return rectangle;
    }

    private boolean isInsideImage(MatOfPoint2f rectangle, int width, int height) {
        double margin = Math.min(width, height) * PREVIEW_EDGE_MARGIN_RATIO;
        for (Point point : rectangle.toArray()) {
            if (point.x < margin || point.y < margin || point.x >= width - margin || point.y >= height - margin) {
                return false;
            }
        }
        return true;
    }

    private void clearPreviewEdges(Mat edges) {
        int margin = Math.max(8, (int) Math.round(Math.min(edges.rows(), edges.cols()) * PREVIEW_EDGE_MARGIN_RATIO));
        Imgproc.rectangle(edges, new Point(0, 0), new Point(edges.cols() - 1, margin), new Scalar(0), -1);
        Imgproc.rectangle(edges, new Point(0, edges.rows() - margin), new Point(edges.cols() - 1, edges.rows() - 1), new Scalar(0), -1);
        Imgproc.rectangle(edges, new Point(0, 0), new Point(margin, edges.rows() - 1), new Scalar(0), -1);
        Imgproc.rectangle(edges, new Point(edges.cols() - margin, 0), new Point(edges.cols() - 1, edges.rows() - 1), new Scalar(0), -1);
    }

    private boolean isPreviewEdgeSegment(LineSegment segment, int width, int height) {
        double margin = Math.max(8.0, Math.min(width, height) * PREVIEW_EDGE_MARGIN_RATIO);
        boolean nearLeft = segment.p1.x <= margin && segment.p2.x <= margin;
        boolean nearRight = segment.p1.x >= width - margin && segment.p2.x >= width - margin;
        boolean nearTop = segment.p1.y <= margin && segment.p2.y <= margin;
        boolean nearBottom = segment.p1.y >= height - margin && segment.p2.y >= height - margin;
        return nearLeft || nearRight || nearTop || nearBottom;
    }

    private double scoreEdgeSupport(MatOfPoint2f rectangle, Mat edges) {
        Point[] pts = orderCorners(rectangle).toArray();
        double total = 0.0;
        double strongTotal = 0.0;
        int strongEdges = 0;
        int usableEdges = 0;
        double weakest = 1.0;
        for (int i = 0; i < 4; i++) {
            double support = lineEdgeSupport(edges, pts[i], pts[(i + 1) % 4], 5);
            total += support;
            weakest = Math.min(weakest, support);
            if (support > 0.24) {
                usableEdges++;
            }
            if (support > 0.42) {
                strongEdges++;
                strongTotal += support;
            }
        }
        if (strongEdges < 2) {
            return 0.0;
        }
        double average = total / 4.0;
        if ((usableEdges < 2) || (usableEdges < 3 && weakest < 0.035) || average < 0.22) {
            return 0.0;
        }
        double strongAverage = strongTotal / strongEdges;
        return strongAverage * 0.70 + average * 0.30;
    }

    private double lineEdgeSupport(Mat edges, Point a, Point b, int thickness) {
        Mat lineMask = Mat.zeros(edges.rows(), edges.cols(), CvType.CV_8U);
        Imgproc.line(lineMask, a, b, new Scalar(255), thickness);
        Mat masked = new Mat();
        Core.bitwise_and(edges, lineMask, masked);

        double length = Math.max(1.0, Math.hypot(a.x - b.x, a.y - b.y));
        double support = Core.countNonZero(masked) / length;
        lineMask.release();
        masked.release();
        return clamp(support);
    }

    private void addWhitePaperRegionPass(Mat gray, List<MatOfPoint2f> rectangles, int srcArea) {
        int kernelSize = Math.max(17, Math.round(Math.min(gray.rows(), gray.cols()) * 0.045f));
        if (kernelSize % 2 == 0) {
            kernelSize += 1;
        }

        Mat fixedWhite = new Mat();
        Mat otsuWhite = new Mat();
        Mat paperMask = new Mat();
        Mat closeKernel = Mat.ones(new Size(kernelSize, kernelSize), CvType.CV_8U);
        Mat openKernel = Mat.ones(new Size(5, 5), CvType.CV_8U);

        Imgproc.threshold(gray, fixedWhite, 125, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(gray, otsuWhite, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Core.bitwise_and(fixedWhite, otsuWhite, paperMask);

        Imgproc.morphologyEx(paperMask, paperMask, Imgproc.MORPH_CLOSE, closeKernel);
        Imgproc.morphologyEx(paperMask, paperMask, Imgproc.MORPH_OPEN, openKernel);
        Imgproc.dilate(paperMask, paperMask, openKernel);

        findExternalRectangles(paperMask, rectangles, srcArea);

        fixedWhite.release();
        otsuWhite.release();
        paperMask.release();
        closeKernel.release();
        openKernel.release();
    }

    private void addEdgesPass(Mat src, List<MatOfPoint2f> rectangles, int srcArea,
                              double threshold1, double threshold2, Mat kernel) {
        Mat edges = new Mat();
        Imgproc.Canny(src, edges, threshold1, threshold2);
        clearPreviewEdges(edges);
        Imgproc.dilate(edges, edges, kernel);
        Imgproc.morphologyEx(edges, edges, Imgproc.MORPH_CLOSE, kernel);
        findRectangles(edges, rectangles, srcArea);
        edges.release();
    }

    private void findRectangles(Mat mask, List<MatOfPoint2f> rectangles, int srcArea) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat workingMask = mask.clone();
        Imgproc.findContours(workingMask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        addRectangles(contours, rectangles, srcArea);
        workingMask.release();
        hierarchy.release();
    }

    private void findExternalRectangles(Mat mask, List<MatOfPoint2f> rectangles, int srcArea) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat workingMask = mask.clone();
        Imgproc.findContours(workingMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        addRectangles(contours, rectangles, srcArea);
        workingMask.release();
        hierarchy.release();
    }

    private void addRectangles(List<MatOfPoint> contours, List<MatOfPoint2f> rectangles, int srcArea) {
        double minArea = srcArea * AREA_LOWER_THRESHOLD;
        double maxArea = srcArea * AREA_UPPER_THRESHOLD;
        double[] epsilons = {0.015, 0.02, 0.03, 0.045, 0.06, 0.08};

        for (MatOfPoint contour : contours) {
            MatOfPoint2f contourFloat = MathUtils.toMatOfPointFloat(contour);
            double arcLen = Imgproc.arcLength(contourFloat, true);
            if (arcLen < 80) {
                contourFloat.release();
                continue;
            }

            MatOfPoint2f bestApprox = null;
            double bestArea = 0;
            for (double epsilon : epsilons) {
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(contourFloat, approx, arcLen * epsilon, true);
                if (isRectangle(approx, minArea, maxArea)) {
                    double area = Math.abs(Imgproc.contourArea(approx));
                    if (area > bestArea) {
                        bestArea = area;
                        bestApprox = approx;
                    }
                }
            }

            if (bestApprox == null) {
                bestApprox = minAreaRectangleFallback(contourFloat, minArea, maxArea);
            }

            if (bestApprox != null) {
                MatOfPoint2f ordered = orderCorners(bestApprox);
                if (!isDuplicate(ordered, rectangles)) {
                    rectangles.add(ordered);
                }
            }
            contourFloat.release();
        }
    }

    private MatOfPoint2f minAreaRectangleFallback(MatOfPoint2f contour, double minArea, double maxArea) {
        RotatedRect rotatedRect = Imgproc.minAreaRect(contour);
        double boxArea = rotatedRect.size.width * rotatedRect.size.height;
        double contourArea = Math.abs(Imgproc.contourArea(contour));
        if (boxArea < minArea || boxArea > maxArea || contourArea / Math.max(1.0, boxArea) < MIN_RECTANGULARITY) {
            return null;
        }

        Point[] box = new Point[4];
        rotatedRect.points(box);
        MatOfPoint2f candidate = new MatOfPoint2f();
        candidate.fromArray(box);
        return candidate;
    }

    private boolean isRectangle(MatOfPoint2f polygon, double minArea, double maxArea) {
        if (polygon.rows() != 4) return false;
        if (!hasRegularDocumentShape(polygon)) return false;

        MatOfPoint polygonInt = MathUtils.toMatOfPointInt(polygon);
        boolean convex = Imgproc.isContourConvex(polygonInt);
        polygonInt.release();
        if (!convex) return false;

        double area = Math.abs(Imgproc.contourArea(polygon));
        if (area < minArea || area > maxArea) return false;

        RotatedRect minRect = Imgproc.minAreaRect(polygon);
        double rectArea = Math.max(1.0, minRect.size.width * minRect.size.height);
        if (area / rectArea < MIN_RECTANGULARITY) return false;

        Point[] pts = orderCorners(polygon).toArray();
        double maxCosine = 0;
        for (int i = 0; i < 4; i++) {
            Point previous = pts[(i + 3) % 4];
            Point current = pts[i];
            Point next = pts[(i + 1) % 4];
            maxCosine = Math.max(maxCosine, Math.abs(MathUtils.angle(previous, next, current)));
        }
        return maxCosine < MAX_ANGLE_COSINE;
    }

    private double scoreRectangle(MatOfPoint2f rectangle, Mat src, double imgW, double imgH, double cx, double cy) {
        if (!hasRegularDocumentShape(rectangle)) {
            return 0.0;
        }
        if (touchesPreviewEdge(rectangle, imgW, imgH)) {
            return 0.0;
        }

        double imageArea = imgW * imgH;
        double area = Math.abs(Imgproc.contourArea(rectangle));
        Point[] pts = rectangle.toArray();

        double rcx = 0;
        double rcy = 0;
        for (Point point : pts) {
            rcx += point.x;
            rcy += point.y;
        }
        rcx /= 4.0;
        rcy /= 4.0;

        double dist = Math.sqrt((rcx - cx) * (rcx - cx) + (rcy - cy) * (rcy - cy));
        double centerRatio = 1.0 - Math.min(1.0, dist / Math.hypot(cx, cy));

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for (Point point : pts) {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }

        double edgeMargin = Math.min(imgW, imgH) * 0.012;
        double edgePenalty = 1.0;
        if (minX <= edgeMargin || minY <= edgeMargin || maxX >= imgW - edgeMargin || maxY >= imgH - edgeMargin) {
            edgePenalty = 0.72;
        }

        RotatedRect minRect = Imgproc.minAreaRect(rectangle);
        double boxArea = Math.max(1.0, minRect.size.width * minRect.size.height);
        double rectangularity = Math.min(1.0, area / boxArea);
        double areaRatio = Math.min(1.0, area / imageArea);
        double aspect = Math.max(minRect.size.width, minRect.size.height)
                / Math.max(1.0, Math.min(minRect.size.width, minRect.size.height));
        if (aspect < 1.08 || aspect > 2.05) {
            return 0.0;
        }

        double a4Score = scoreA4Aspect(aspect);
        if (a4Score < 0.18) {
            return 0.0;
        }

        double areaScore = 1.0;
        if (areaRatio < 0.07) {
            areaScore = 0.22 + areaRatio / 0.07 * 0.38;
        } else if (areaRatio > 0.82) {
            areaScore = 0.25;
        } else if (areaRatio > 0.68) {
            areaScore = 1.0 - (areaRatio - 0.68) / 0.14 * 0.55;
        }

        PaperEvidence evidence = analyzePaperEvidence(rectangle, src, area);
        double paperScore = evidence.score;
        if (paperScore < 0.20 && areaRatio < 0.16) {
            paperScore *= 0.35;
        }
        if (evidence.paperWhiteScore < 0.42 || (evidence.reliableBoundaryCount < 2 && evidence.whiteMarginScore < 0.36)) {
            return 0.0;
        }
        if (paperScore < 0.050) {
            return 0.0;
        }

        return areaRatio * areaScore * (0.60 + centerRatio * 0.40)
                * (0.55 + rectangularity * 0.45)
                * Math.pow(a4Score, 1.8)
                * paperScore
                * edgePenalty;
    }

    private PaperEvidence analyzePaperEvidence(MatOfPoint2f rectangle, Mat src, double area) {
        Mat mask = Mat.zeros(src.rows(), src.cols(), CvType.CV_8U);
        MatOfPoint polygon = MathUtils.toMatOfPointInt(rectangle);
        Imgproc.fillConvexPoly(mask, polygon, new Scalar(255));

        Scalar meanBgr = Core.mean(src, mask);
        double brightness = (meanBgr.val[0] + meanBgr.val[1] + meanBgr.val[2]) / 3.0;
        double maxChannel = Math.max(meanBgr.val[0], Math.max(meanBgr.val[1], meanBgr.val[2]));
        double minChannel = Math.min(meanBgr.val[0], Math.min(meanBgr.val[1], meanBgr.val[2]));
        double chroma = maxChannel - minChannel;

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Scalar grayMean = Core.mean(gray, mask);

        Mat innerMask = new Mat();
        Mat ringMask = new Mat();
        Mat borderMask = new Mat();
        Mat whiteMarginMask = new Mat();
        Mat kernel = Mat.ones(new Size(17, 17), CvType.CV_8U);
        Mat marginKernel = Mat.ones(new Size(31, 31), CvType.CV_8U);
        Imgproc.erode(mask, innerMask, kernel);
        Imgproc.dilate(mask, ringMask, kernel);
        Core.subtract(ringMask, mask, ringMask);
        Core.subtract(mask, innerMask, borderMask);
        Imgproc.erode(mask, whiteMarginMask, marginKernel);
        Core.subtract(mask, whiteMarginMask, whiteMarginMask);

        Scalar innerMean = Core.mean(gray, innerMask);
        Scalar ringMean = Core.mean(gray, ringMask);
        Scalar borderMean = Core.mean(gray, borderMask);
        Scalar innerBgrMean = Core.mean(src, innerMask);
        Scalar ringBgrMean = Core.mean(src, ringMask);
        Scalar marginMeanBgr = Core.mean(src, whiteMarginMask);
        double edgeContrast = Math.max(0.0, innerMean.val[0] - ringMean.val[0]);
        double innerRingGap = Math.abs(innerMean.val[0] - ringMean.val[0]);
        double darkBorderGap = innerMean.val[0] - borderMean.val[0];
        double colorGap = colorDistance(innerBgrMean, ringBgrMean);

        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 60, 180);
        Mat innerEdges = new Mat();
        Mat marginEdges = new Mat();
        Core.bitwise_and(edges, innerMask, innerEdges);
        Core.bitwise_and(edges, whiteMarginMask, marginEdges);
        double edgeDensity = Core.countNonZero(innerEdges) / Math.max(1.0, Core.countNonZero(innerMask));
        double marginEdgeDensity = Core.countNonZero(marginEdges) / Math.max(1.0, Core.countNonZero(whiteMarginMask));
        double areaRatio = area / Math.max(1.0, src.rows() * src.cols());

        double whiteScore = clamp((brightness - 80.0) / 100.0);
        double pureWhiteScore = clamp((brightness - 165.0) / 70.0) * clamp((45.0 - chroma) / 45.0);
        double marginBrightness = (marginMeanBgr.val[0] + marginMeanBgr.val[1] + marginMeanBgr.val[2]) / 3.0;
        double marginMax = Math.max(marginMeanBgr.val[0], Math.max(marginMeanBgr.val[1], marginMeanBgr.val[2]));
        double marginMin = Math.min(marginMeanBgr.val[0], Math.min(marginMeanBgr.val[1], marginMeanBgr.val[2]));
        double marginChroma = marginMax - marginMin;
        double whiteMarginScore = clamp((marginBrightness - 170.0) / 70.0) * clamp((42.0 - marginChroma) / 42.0);
        if (marginEdgeDensity > 0.045) {
            whiteMarginScore *= 0.45;
        }
        double neutralScore = clamp((95.0 - chroma) / 95.0);
        double grayScore = clamp((grayMean.val[0] - 75.0) / 115.0);
        double contrastScore = clamp(edgeContrast / 22.0);
        double colorBoundaryScore = clamp((innerRingGap * 0.65 + colorGap * 0.35) / 22.0);
        BoundaryEvidence boundaryEvidence = analyzeBoundaryEvidence(rectangle, src, gray);
        double darkBorderPenalty = darkBorderGap > 24.0 && innerRingGap < 18.0 ? 0.04 : 1.0;
        double tableLinePenalty = edgeDensity > 0.055 && areaRatio < 0.50 ? 0.12 : 1.0;
        double weakOuterEdgePenalty = edgeContrast < 4.0 && colorGap < 7.0 ? 0.55 : 1.0;
        double hasInterior = Core.countNonZero(innerMask) > Math.max(100.0, area * 0.20) ? 1.0 : 0.55;

        mask.release();
        polygon.release();
        gray.release();
        innerMask.release();
        ringMask.release();
        borderMask.release();
        whiteMarginMask.release();
        kernel.release();
        marginKernel.release();
        edges.release();
        innerEdges.release();
        marginEdges.release();

        double paperWhiteScore = whiteScore * 0.45 + pureWhiteScore * 0.25 + neutralScore * 0.20 + grayScore * 0.10;
        double score = (whiteScore * 0.13 + pureWhiteScore * 0.12 + whiteMarginScore * 0.25
                + neutralScore * 0.12 + grayScore * 0.08
                + contrastScore * 0.08 + colorBoundaryScore * 0.08 + boundaryEvidence.score * 0.14)
                * hasInterior * darkBorderPenalty * tableLinePenalty * weakOuterEdgePenalty;
        return new PaperEvidence(score, paperWhiteScore, whiteMarginScore, boundaryEvidence.reliableCount);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double colorDistance(Scalar a, Scalar b) {
        double db = a.val[0] - b.val[0];
        double dg = a.val[1] - b.val[1];
        double dr = a.val[2] - b.val[2];
        return Math.sqrt(db * db + dg * dg + dr * dr) / Math.sqrt(3.0);
    }

    private BoundaryEvidence analyzeBoundaryEvidence(MatOfPoint2f rectangle, Mat src, Mat gray) {
        Point[] pts = orderCorners(rectangle).toArray();
        Point center = polygonCenter(pts);
        double total = 0.0;
        int reliableCount = 0;

        for (int i = 0; i < 4; i++) {
            Point a = pts[i];
            Point b = pts[(i + 1) % 4];
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double len = Math.hypot(dx, dy);
            if (len < 1.0) continue;

            double nx = -dy / len;
            double ny = dx / len;
            Point mid = midpoint(a, b);
            double towardCenter = (center.x - mid.x) * nx + (center.y - mid.y) * ny;
            if (towardCenter < 0) {
                nx = -nx;
                ny = -ny;
            }

            double offset = 8.0;
            Point innerA = new Point(a.x + nx * offset, a.y + ny * offset);
            Point innerB = new Point(b.x + nx * offset, b.y + ny * offset);
            Point outerA = new Point(a.x - nx * offset, a.y - ny * offset);
            Point outerB = new Point(b.x - nx * offset, b.y - ny * offset);

            Mat innerMask = Mat.zeros(src.rows(), src.cols(), CvType.CV_8U);
            Mat outerMask = Mat.zeros(src.rows(), src.cols(), CvType.CV_8U);
            Imgproc.line(innerMask, innerA, innerB, new Scalar(255), 9);
            Imgproc.line(outerMask, outerA, outerB, new Scalar(255), 9);

            Scalar innerBgr = Core.mean(src, innerMask);
            Scalar outerBgr = Core.mean(src, outerMask);
            Scalar innerGray = Core.mean(gray, innerMask);
            Scalar outerGray = Core.mean(gray, outerMask);

            double innerBrightness = (innerBgr.val[0] + innerBgr.val[1] + innerBgr.val[2]) / 3.0;
            double innerMax = Math.max(innerBgr.val[0], Math.max(innerBgr.val[1], innerBgr.val[2]));
            double innerMin = Math.min(innerBgr.val[0], Math.min(innerBgr.val[1], innerBgr.val[2]));
            double innerChroma = innerMax - innerMin;
            double insideWhite = clamp((innerBrightness - 120.0) / 105.0) * clamp((75.0 - innerChroma) / 75.0);
            double brightnessGap = Math.max(0.0, innerGray.val[0] - outerGray.val[0]);
            double colorGap = colorDistance(innerBgr, outerBgr);
            double sideScore = insideWhite * 0.58
                    + clamp(brightnessGap / 28.0) * 0.24
                    + clamp(colorGap / 26.0) * 0.18;

            if (insideWhite > 0.48 && (brightnessGap > 7.0 || colorGap > 9.0 || sideScore > 0.62)) {
                reliableCount++;
            }
            total += sideScore;

            innerMask.release();
            outerMask.release();
        }

        return new BoundaryEvidence(total / 4.0, reliableCount);
    }

    private Point polygonCenter(Point[] points) {
        double x = 0.0;
        double y = 0.0;
        for (Point point : points) {
            x += point.x;
            y += point.y;
        }
        return new Point(x / points.length, y / points.length);
    }

    private static class PaperEvidence {
        final double score;
        final double paperWhiteScore;
        final double whiteMarginScore;
        final int reliableBoundaryCount;

        PaperEvidence(double score, double paperWhiteScore, double whiteMarginScore, int reliableBoundaryCount) {
            this.score = score;
            this.paperWhiteScore = paperWhiteScore;
            this.whiteMarginScore = whiteMarginScore;
            this.reliableBoundaryCount = reliableBoundaryCount;
        }
    }

    private static class BoundaryEvidence {
        final double score;
        final int reliableCount;

        BoundaryEvidence(double score, int reliableCount) {
            this.score = score;
            this.reliableCount = reliableCount;
        }
    }

    private double scoreA4Aspect(double aspect) {
        double relativeError = Math.abs(aspect - A4_ASPECT_RATIO) / A4_ASPECT_RATIO;
        if (relativeError <= 0.08) {
            return 1.0;
        }
        if (relativeError <= 0.18) {
            return 1.0 - (relativeError - 0.08) / 0.10 * 0.35;
        }
        if (relativeError <= 0.35) {
            return 0.65 - (relativeError - 0.18) / 0.17 * 0.47;
        }
        return 0.0;
    }

    private boolean hasRegularDocumentShape(MatOfPoint2f rectangle) {
        if (rectangle == null || rectangle.rows() != 4) return false;

        Point[] pts = orderCorners(rectangle).toArray();
        double[] lengths = new double[4];
        double[][] vectors = new double[4][2];
        for (int i = 0; i < 4; i++) {
            Point a = pts[i];
            Point b = pts[(i + 1) % 4];
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double len = Math.hypot(dx, dy);
            if (len < 1.0) return false;

            lengths[i] = len;
            vectors[i][0] = dx / len;
            vectors[i][1] = dy / len;
        }

        double topBottomParallel = Math.abs(vectors[0][0] * vectors[2][0] + vectors[0][1] * vectors[2][1]);
        double rightLeftParallel = Math.abs(vectors[1][0] * vectors[3][0] + vectors[1][1] * vectors[3][1]);
        if (topBottomParallel < MIN_OPPOSITE_SIDE_PARALLEL || rightLeftParallel < MIN_OPPOSITE_SIDE_PARALLEL) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            double adjacentCos = Math.abs(vectors[i][0] * vectors[(i + 1) % 4][0]
                    + vectors[i][1] * vectors[(i + 1) % 4][1]);
            if (adjacentCos > MAX_ADJACENT_SIDE_COSINE) {
                return false;
            }
        }

        double widthRatio = Math.min(lengths[0], lengths[2]) / Math.max(lengths[0], lengths[2]);
        double heightRatio = Math.min(lengths[1], lengths[3]) / Math.max(lengths[1], lengths[3]);
        if (widthRatio < 0.62 || heightRatio < 0.62) {
            return false;
        }

        double diagonal1 = distance(pts[0], pts[2]);
        double diagonal2 = distance(pts[1], pts[3]);
        double diagonalRatio = Math.min(diagonal1, diagonal2) / Math.max(diagonal1, diagonal2);
        return diagonalRatio >= 0.72;
    }

    private boolean touchesPreviewEdge(MatOfPoint2f rectangle, double width, double height) {
        double margin = Math.min(width, height) * PREVIEW_EDGE_MARGIN_RATIO;
        for (Point point : rectangle.toArray()) {
            if (point.x <= margin || point.y <= margin || point.x >= width - margin || point.y >= height - margin) {
                return true;
            }
        }
        return false;
    }

    private MatOfPoint2f orderCorners(MatOfPoint2f rectangle) {
        Point[] pts = rectangle.toArray();
        Point[] sorted = new Point[4];

        double minSum = Double.MAX_VALUE;
        double maxSum = -Double.MAX_VALUE;
        double minDiff = Double.MAX_VALUE;
        double maxDiff = -Double.MAX_VALUE;
        for (Point point : pts) {
            double sum = point.x + point.y;
            double diff = point.y - point.x;
            if (sum < minSum) {
                minSum = sum;
                sorted[0] = point;
            }
            if (diff < minDiff) {
                minDiff = diff;
                sorted[1] = point;
            }
            if (sum > maxSum) {
                maxSum = sum;
                sorted[2] = point;
            }
            if (diff > maxDiff) {
                maxDiff = diff;
                sorted[3] = point;
            }
        }

        MatOfPoint2f result = new MatOfPoint2f();
        result.fromArray(sorted);
        return result;
    }

    private Point midpoint(Point a, Point b) {
        return new Point((a.x + b.x) / 2.0, (a.y + b.y) / 2.0);
    }

    private double distance(Point a, Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private boolean isDuplicate(MatOfPoint2f candidate, List<MatOfPoint2f> rectangles) {
        RotatedRect candidateRect = Imgproc.minAreaRect(candidate);
        double candidateArea = Math.max(1.0, candidateRect.size.width * candidateRect.size.height);
        for (MatOfPoint2f existing : rectangles) {
            RotatedRect existingRect = Imgproc.minAreaRect(existing);
            double existingArea = Math.max(1.0, existingRect.size.width * existingRect.size.height);
            double dx = candidateRect.center.x - existingRect.center.x;
            double dy = candidateRect.center.y - existingRect.center.y;
            double centerDistance = Math.sqrt(dx * dx + dy * dy);
            double areaRatio = Math.min(candidateArea, existingArea) / Math.max(candidateArea, existingArea);
            if (centerDistance < 16 && areaRatio > 0.86) {
                return true;
            }
        }
        return false;
    }

    private static class LineSegment {
        final Point p1;
        final Point p2;
        final double length;
        final double unitX;
        final double unitY;

        LineSegment(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
            double dx = p2.x - p1.x;
            double dy = p2.y - p1.y;
            this.length = Math.max(1.0, Math.hypot(dx, dy));
            this.unitX = dx / length;
            this.unitY = dy / length;
        }
    }
}
