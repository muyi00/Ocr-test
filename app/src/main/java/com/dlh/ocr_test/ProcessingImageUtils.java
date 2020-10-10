package com.dlh.ocr_test;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * desc   : 处理图片工具
 * 参考：https://www.cnblogs.com/xunzhi/p/9131962.html
 * 参考：https://github.com/kmin0579/CharDetect/blob/master/CharDetect/src/com/kmin/CharDetect/DetectChar.java
 * https://github.com/gloomyfish1998/opencv4android/tree/master/samples/OpencvDemo/app/src/main/java/com/book/chapter
 * https://blog.csdn.net/poorkick/article/details/103881153#comments_13229179
 * https://blog.csdn.net/Kazichs/article/details/52914406
 * https://blog.csdn.net/bigFatCat_Tom/article/details/90753227
 * <p>
 * tesseract ocr -训练样本库  https://www.jianshu.com/p/55d2d26fa2ff
 * <p>
 * <p>
 * https://blog.csdn.net/qq_20158897/category_9325824.html
 * author : YJ
 * time   : 2020/9/28 11:44
 */
public class ProcessingImageUtils {
    private static final String TAG = "ProcessingImageUtils";

    /**
     * 图片二值化，斜体纠正
     *
     * @param textImage
     */
    public static Mat deSkewText(Mat textImage) {
        // 二值化图像
        Mat gray = new Mat();
        Mat binary = new Mat();
        Imgproc.cvtColor(textImage, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        // 寻找文本区域最新外接矩形
        int w = binary.cols();
        int h = binary.rows();
        List<Point> points = new ArrayList<>();
        int p = 0;
        byte[] data = new byte[w * h];
        binary.get(0, 0, data);
        int index = 0;
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                index = row * w + col;
                p = data[index] & 0xff;
                if (p == 255) {
                    points.add(new Point(col, row));
                }
            }
        }
        RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(points.toArray(new Point[0])));
        double angle = box.angle;
        if (angle < -45)
            angle += 90;

        Point[] vertices = new Point[4];
        box.points(vertices);
        // de-skew 偏斜校正
        Log.i(TAG, "angle=" + angle);
        Mat rot_mat = Imgproc.getRotationMatrix2D(box.center, angle, 1);

        Mat dst = new Mat();
        Imgproc.warpAffine(binary, dst, rot_mat, binary.size(), Imgproc.INTER_CUBIC);
        Core.bitwise_not(dst, dst);

        gray.release();
        binary.release();
        rot_mat.release();

        return dst;
    }

    /**
     * 逆时针旋转,但是图片宽高不用变
     *
     * @param src
     * @param angele
     * @return
     */
    public static Mat rotate(Mat src, double angele) {
        Mat dst = src.clone();
        Point center = new Point(src.width() / 2.0, src.height() / 2.0);
        Mat affineTrans = Imgproc.getRotationMatrix2D(center, angele, 1.0);
        Imgproc.warpAffine(src, dst, affineTrans, dst.size(), Imgproc.INTER_NEAREST);
        return dst;
    }

    /**
     * 图像整体向左旋转90度
     *
     * @param src Mat
     * @return 旋转后的Mat
     */
    public static Mat rotateLeft(Mat src) {
        Mat tmp = new Mat();
        // 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
        Core.transpose(src, tmp);
        Mat result = new Mat();
        // flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
        // flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
        // flipCode = -1 此函数关于原点对称
        Core.flip(tmp, result, 0);
        return result;
    }

    /**
     * 图像整体向右旋转90度
     *
     * @param src Mat
     * @return 旋转后的Mat
     */
    public static Mat rotateRight(Mat src) {
        Mat tmp = new Mat();
        // 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
        Core.transpose(src, tmp);
        Mat result = new Mat();
        // flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
        // flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
        // flipCode = -1 此函数关于原点对称
        Core.flip(tmp, result, 1);
        return result;
    }


    /***
     * 偏斜校正
     * @param src
     * @return
     */
    public static Mat deSkew(Mat src) {

        // 寻找文本区域最新外接矩形
        int w = src.cols();
        int h = src.rows();
        List<Point> points = new ArrayList<>();
        int p = 0;
        byte[] data = new byte[w * h];
        src.get(0, 0, data);
        int index = 0;
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                index = row * w + col;
                p = data[index] & 0xff;
                if (p == 255) {
                    points.add(new Point(col, row));
                }
            }
        }
        RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(points.toArray(new Point[0])));
        double angle = box.angle;
        if (angle < -45)
            angle += 90;

        Point[] vertices = new Point[4];
        box.points(vertices);
        // de-skew 偏斜校正
        Log.i(TAG, "angle=" + angle);
        Mat rot_mat = Imgproc.getRotationMatrix2D(box.center, angle, 1);

        Mat deSkewMat = new Mat();
        Imgproc.warpAffine(src, deSkewMat, rot_mat, src.size(), Imgproc.INTER_CUBIC);
        rot_mat.release();

        return deSkewMat;
    }

    public static Mat resize(Mat src) {
        Mat resizeMat = new Mat();
        Imgproc.resize(src, resizeMat, new Size(src.width() / 2, src.height() / 2));
        return resizeMat;
    }


    /**
     * 灰度
     *
     * @param src
     * @return
     */
    public static Mat gray(Mat src) {
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_RGB2GRAY);
        return grayMat;
    }

    /***
     * 灰度，二值化
     * @param src
     * @return
     */
    public static Mat binarization(Mat src) {
        Mat gray = new Mat();
        Mat binary = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        /***
         * 参数一：src，待二值化的多通道图像，只能是CV_8U和CV_32F两种数据类型
         * 参数二：dst，二值化后的图像，与输入图像具有相同的尺寸、类型和通道数
         * 参数三：thresh，二值化的阈值
         * 参数四：maxval，二值化过程的最大值，此函数只在THRESH_BINARY和THRESH_BINARY_INV两种二值化方法中才使用
         * 参数五：type，二值化类型
         * THRESH_BINARY	    0	灰度值大于阈值为最大值，其他值为0
         * THRESH_BINARY_INV	1	灰度值大于阈值为0，其他值为最大值
         * THRESH_TRUNC	        2	灰度值大于阈值的为阈值，其他值不变
         * THRESH_TOZERO	    3	灰度值大于阈值的不变，其他值为0
         * THRESH_TOZERO_INV	4	灰度值大于阈值的为零，其他值不变
         * THRESH_MASK	        7	NA
         * THRESH_OTSU	        8	大津法自动寻求全局阈值
         * THRESH_TRIANGLE	    16	三角形法自动寻求全局阈值
         */
        //Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        //反转数组的每一位。
        //Core.bitwise_not(binary, binary);
        return binary;
    }

    /***
     * 二值化
     * @param src
     * @return
     */
    public static Mat threshold(Mat src) {
        Mat binary = new Mat();
        /***
         * 参数一：src，待二值化的多通道图像，只能是CV_8U和CV_32F两种数据类型
         * 参数二：dst，二值化后的图像，与输入图像具有相同的尺寸、类型和通道数
         * 参数三：thresh，二值化的阈值
         * 参数四：maxval，二值化过程的最大值，此函数只在THRESH_BINARY和THRESH_BINARY_INV两种二值化方法中才使用
         * 参数五：type，二值化类型
         * THRESH_BINARY	    0	灰度值大于阈值为最大值，其他值为0
         * THRESH_BINARY_INV	1	灰度值大于阈值为0，其他值为最大值
         * THRESH_TRUNC	        2	灰度值大于阈值的为阈值，其他值不变
         * THRESH_TOZERO	    3	灰度值大于阈值的不变，其他值为0
         * THRESH_TOZERO_INV	4	灰度值大于阈值的为零，其他值不变
         * THRESH_MASK	        7	NA
         * THRESH_OTSU	        8	大津法自动寻求全局阈值
         * THRESH_TRIANGLE	    16	三角形法自动寻求全局阈值
         */
        //Imgproc.threshold(src, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
        Imgproc.threshold(src, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        //反转数组的每一位。
        //Core.bitwise_not(binary, binary);
        return binary;
    }


    /**
     * 均值模糊方法: 均值滤波是最简单的滤波，对核进行均值计算，每一个邻域像素都有相同的权值。
     *
     * @return
     */
    public static Mat blur(Mat src) {
        Mat outMat = new Mat();
        // 均值模糊方法
        Imgproc.blur(src, outMat, new Size(3, 3));
        return outMat;
    }


    /**
     * 高斯模糊方法:高斯核是通过高斯函数所获得的，大概是临近像素对特定像素结果的影响比那些较远像素的影响要高。
     *
     * @return
     */
    public static Mat gaussianBlur(Mat src) {
        Mat outMat = new Mat();
        // 高斯模糊方法
        Imgproc.GaussianBlur(src, outMat, new Size(3, 3), 0);
        return outMat;
    }

    /***
     * 中值模糊方法:椒盐噪声是一种图片中常见的噪点，噪点随机发呢不在图片中，中值模糊就是为了除去这些噪点，将核中的像素按照升序或者降序排列，
     * @param src
     * @return
     */
    public static Mat medianBlur(Mat src) {
        Mat outMat = new Mat();
        // 中值模糊方法
        Imgproc.medianBlur(src, outMat, 5);
        return outMat;
    }


    /**
     * 锐化：锐化也可以看作是一种线性滤波操作，并且锚点像素有较高的权重，而周围的像素权重较低。
     *
     * @return
     */
    public static Mat sharpen(Mat src) {
        Mat outMat = new Mat();
        //锐化
        Mat kenrl = new Mat(3, 3, CvType.CV_16SC1);
        kenrl.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
        Imgproc.filter2D(src, outMat, src.depth(), kenrl);
        return outMat;
    }


    /**
     * 边缘检测的一般步骤：
     * 1、滤波——消除噪声
     * 2、增强——使边界轮廓更加明显
     * 3、检测——选出边缘点
     */

    /**
     * 高斯差分法检测边缘
     * 算法原理：先将图片灰度化，然后进行两个半径的高斯模糊，然后降高斯模糊后的两个图相减，再反转二值阈值化，再讲mat转成图片
     *
     * @param bitmap
     * @return
     */
    private static Bitmap GaussianBorderDetection(Bitmap bitmap) {
        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, src);

        //第一步  将图像转为灰度图像
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        //第二步  用两个不同的模糊半径对灰度图像执行高斯模糊（取得两幅高斯模糊图像）
        Mat blur1 = new Mat();
        Imgproc.GaussianBlur(grayMat, blur1, new Size(15, 15), 5);
        Mat blur2 = new Mat();
        Imgproc.GaussianBlur(grayMat, blur2, new Size(21, 21), 5);
        //第三步  将两幅高斯模糊图像做减法，得到一幅包含边缘点的结果图像
        Mat diff = new Mat();
        Core.absdiff(blur1, blur2, diff);

        // Mat转Bitmap
        Bitmap processedImage = Bitmap.createBitmap(diff.cols(), diff.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(diff, processedImage);
        return processedImage;
    }


    /**
     * 　Canny边缘检测器
     *
     * @param bitmap
     * @return
     */
    private static Bitmap cannyBorderDetection(Bitmap bitmap) {
        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, src);
        //首先将图像灰度化
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);

        Mat cannyEdges = new Mat();
        Imgproc.Canny(grayMat, cannyEdges, 10, 100);

        // Mat转Bitmap
        Bitmap processedImage = Bitmap.createBitmap(cannyEdges.cols(), cannyEdges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cannyEdges, processedImage);
        return processedImage;
    }


    /**
     * 　Sobel滤波器
     * <p>
     * Sobel滤波器也叫Sobel算子，与Canny边缘检测一样，需要计算像素的灰度梯度，只不过是换用另一种方式。
     * <p>
     * 大致步骤：1.将图像进行灰度化，2.计算水平方向灰度梯度的绝对值，3.计算数值方向灰度梯度的绝对值，4计算最终的梯度
     *
     * @param bitmap
     * @return
     */
    private static Bitmap sobelBorderDetection(Bitmap bitmap) {
        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, src);
        //首先将图像灰度化
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);

        //第二步：计算水平方向灰度梯度的绝对值
        Mat grad_x = new Mat();
        Imgproc.Sobel(grayMat, grad_x, CvType.CV_16S, 1, 0, 3, 1, 0);
        Mat abs_grad_x = new Mat();
        Core.convertScaleAbs(grad_x, abs_grad_x);

        //第三步：计算垂直方法灰度梯度的绝对值
        Mat grad_y = new Mat();
        Imgproc.Sobel(grayMat, grad_y, CvType.CV_16S, 0, 1, 3, 1, 0);
        Mat abs_grad_y = new Mat();
        Core.convertScaleAbs(grad_y, abs_grad_y);

        //第四步：计算最终梯度
        Mat sobel = new Mat();
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 1, sobel);


        // Mat转Bitmap
        Bitmap processedImage = Bitmap.createBitmap(sobel.cols(), sobel.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(sobel, processedImage);
        return processedImage;
    }


    /**
     * 　Sobel滤波器
     * <p>
     * Sobel滤波器也叫Sobel算子，与Canny边缘检测一样，需要计算像素的灰度梯度，只不过是换用另一种方式。
     * <p>
     * 大致步骤：1.将图像进行灰度化，2.计算水平方向灰度梯度的绝对值，3.计算数值方向灰度梯度的绝对值，4计算最终的梯度
     *
     * @param src
     * @return
     */
    public static Mat sobelBorderDetection(Mat src) {
        //首先将图像灰度化
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);

        //第二步：计算水平方向灰度梯度的绝对值
        Mat grad_x = new Mat();
        Imgproc.Sobel(grayMat, grad_x, CvType.CV_16S, 1, 0, 3, 1, 0);
        Mat abs_grad_x = new Mat();
        Core.convertScaleAbs(grad_x, abs_grad_x);

        //第三步：计算垂直方法灰度梯度的绝对值
        Mat grad_y = new Mat();
        Imgproc.Sobel(grayMat, grad_y, CvType.CV_16S, 0, 1, 3, 1, 0);
        Mat abs_grad_y = new Mat();
        Core.convertScaleAbs(grad_y, abs_grad_y);

        //第四步：计算最终梯度
        Mat sobel = new Mat();
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 1, sobel);
        return sobel;
    }

    /***
     * Sobel滤波器 (x、y梯度)
     * @param src
     * @return
     */
    public static Mat sobelXY(Mat src) {
        //计算水平方向灰度梯度的绝对值
        Mat grad_x = new Mat();
        Imgproc.Sobel(src, grad_x, CvType.CV_16S, 1, 0, 3, 1, 0);
        Mat abs_grad_x = new Mat();
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Mat binary_x = new Mat();
        Imgproc.threshold(abs_grad_x, binary_x, 0, 255, Imgproc.THRESH_OTSU);

        //计算垂直方法灰度梯度的绝对值
        Mat grad_y = new Mat();
        Imgproc.Sobel(src, grad_y, CvType.CV_16S, 0, 1, 3, 1, 0);
        Mat abs_grad_y = new Mat();
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Mat binary_y = new Mat();
        Imgproc.threshold(abs_grad_y, binary_y, 0, 255, Imgproc.THRESH_OTSU);

        //第四步：计算最终梯度
        Mat sobel = new Mat();
        Core.addWeighted(binary_x, 0.5, binary_y, 0.5, 1, sobel);
        return sobel;
    }


    /**
     * Harris角点检测
     * <p>
     * 角点是两条边缘的交点或者在局部邻域中有多个显著边缘方向的点。Harris角点检测是一种在角点检测中最常见的技术。
     * <p>
     * Harris角点检测器在图像上使用滑动窗口计算亮度的变化。
     *
     * @param bitmap
     * @return
     */
//    private static Bitmap harrisBorderDetection(Bitmap bitmap) {
//        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
//        Utils.bitmapToMat(bitmap, src);
//        //首先将图像灰度化
//        Mat grayMat = new Mat();
//        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
//
//        //第二步：找出harri角点
//        Mat tempDst = new Mat();
//        Imgproc.cornerHarris(grayMat, tempDst, 2, 3, 0.04);
//
//        //第三步：在新的图片上绘制角点 
//        Random r = new Random();
//        for (int i = 0; i < tempDstNorm.cols(); i++) {
//            for (int j = 0; j < tempDstNorm.rows(); j++) {
//                double[] value = tempDstNorm.get(j, i);
//                if (value[0] > 250) {
//                    //决定了画出哪些角点，值越大选择画出的点就越少。如果程序跑的比较慢，就是由于值选取的太小，导致画的点过多
//                    Imgproc.circle(corners, new Point(i, j), 5, new Scalar(r.nextInt(255)), 2);
//                }
//            }
//        }
//
//        // Mat转Bitmap
//        Bitmap processedImage = Bitmap.createBitmap(cannyEdges.cols(), cannyEdges.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(cannyEdges, processedImage);
//        return processedImage;
//    }


//    /**
//     * 　Canny边缘检测器
//     *
//     * @param bitmap
//     * @return
//     */
//    private static Bitmap cannyBorderDetection(Bitmap bitmap) {
//        Imgproc.calcHist(matList.subList(0, 1), channels, new Mat(), hist_b, histSize, ranges, false);
//
//
//        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
//        Utils.bitmapToMat(bitmap, src);
//        //首先将图像灰度化
//        Mat grayMat = new Mat();
//        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
//
//        Mat cannyEdges = new Mat();
//        Imgproc.Canny(grayMat, cannyEdges, 10, 100);
//
//        // Mat转Bitmap
//        Bitmap processedImage = Bitmap.createBitmap(cannyEdges.cols(), cannyEdges.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(cannyEdges, processedImage);
//        return processedImage;
//    }

    /**
     * 获取两个矩阵的相似度
     *
     * @param srcMat
     * @param desMat
     */
    public double getComPareHist(Mat srcMat, Mat desMat) {
        srcMat.convertTo(srcMat, CvType.CV_32F);
        desMat.convertTo(desMat, CvType.CV_32F);
        double target = Imgproc.compareHist(srcMat, desMat, Imgproc.CV_COMP_CORREL);
        Log.e(TAG, "相似度 ：   ==" + target);
        return target;
    }


    /**
     * 找出两张图片中的不同
     */
    public static Bitmap getPicturesDifferent(Mat mat1, Mat mat2) {
        Mat mat11 = new Mat();
        Core.subtract(mat1, mat2, mat11);
        Mat mat22 = new Mat();
        Core.subtract(mat2, mat1, mat22);
        Mat result = new Mat();
        Core.add(mat11, mat22, result);
        // 二值化处理
        Bitmap bmp = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bmp);
        return bmp;
    }

///////////////////////////////////////////////////


    public static Mat preprocess(Mat src) {
        //1.Sobel算子，x方向求梯度
        Mat sobel = new Mat();
        Imgproc.Sobel(src, sobel, CvType.CV_8U, 1, 0, 3);

        //2.二值化
        Mat binary = new Mat();
        Imgproc.threshold(sobel, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);

        //3.膨胀和腐蚀操作核设定
        Mat element1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(30, 90));
        //控制高度设置可以控制上下行的膨胀程度，例如3比4的区分能力更强,但也会造成漏检
        Mat element2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(24, 4));

        //4.膨胀一次，让轮廓突出
        Mat dilate1 = new Mat();
        Imgproc.dilate(binary, dilate1, element2);

        //5.腐蚀一次，去掉细节，表格线等。这里去掉的是竖直的线
        Mat erode1 = new Mat();
        Imgproc.dilate(dilate1, erode1, element1);

        //6.再次膨胀，让轮廓明显一些
        Mat dilate2 = new Mat();
        Imgproc.dilate(dilate1, dilate2, element2);
        return dilate1;
    }

    public static List<RotatedRect> findTextRegion(Mat src) {
        List<RotatedRect> rects = new ArrayList<>();
        //1.查找轮廓
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        //2.筛选那些面积小的
        for (int i = 0; i < contours.size(); i++) {
            //计算当前轮廓的面积
            double area = Imgproc.contourArea(contours.get(i));
            //面积小于1000的全部筛选掉
            if (area < 1000) {
                continue;
            }

            MatOfPoint matOfPoint = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(matOfPoint.toArray());

            //轮廓近似，作用较小，approxPolyDP函数有待研究
            double epsilon = 0.001 * Imgproc.arcLength(curve, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(curve, approx, epsilon, true);
            //找到最小矩形，该矩形可能有方向
            RotatedRect rect = Imgproc.minAreaRect(curve);
            //计算高和宽
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;

            //筛选那些太细的矩形，留下扁的
            if (m_height > m_width * 1.2) {
                continue;
            }

            //符合条件的rect添加到rects集合中
            rects.add(rect);

        }
        return rects;

    }


    /***
     * 获取图片中子Mat集合
     * @param src
     * @return
     */
    public static List<Mat> getSubMat(Mat src) {
        //1.转化成灰度图
        src = ProcessingImageUtils.gray(src);
        //均值滤波
        src = ProcessingImageUtils.blur(src);
        //高斯滤波
        src = ProcessingImageUtils.gaussianBlur(src);
        //中值滤波
        src = ProcessingImageUtils.medianBlur(src);
        //二值化
        src = ProcessingImageUtils.threshold(src);
        Mat historyMat = src.clone();

        //1.Sobel算子，x方向求梯度
        Mat sobel = new Mat();
        Imgproc.Sobel(src, sobel, CvType.CV_8U, 1, 0, 3);
        //2.二值化
        Mat binary = new Mat();
        Imgproc.threshold(sobel, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        //3.膨胀和腐蚀操作核设定
        Mat element1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(30, 40));
        //控制高度设置可以控制上下行的膨胀程度，例如3比4的区分能力更强,但也会造成漏检
        Mat element2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(24, 4));
        //4.膨胀一次，让轮廓突出
        Mat dilate1 = new Mat();
        Imgproc.dilate(binary, dilate1, element2);
        //5.腐蚀一次，去掉细节，表格线等。这里去掉的是竖直的线
        Mat erode1 = new Mat();
        Imgproc.dilate(dilate1, erode1, element1);
        //6.再次膨胀，让轮廓明显一些
        Mat dilate2 = new Mat();
        Imgproc.dilate(erode1, dilate2, element2);
        //3.查找和筛选文字区域
        List<RotatedRect> rects = ProcessingImageUtils.findTextRegion(dilate2);

        //4.用绿线画出这些找到的轮廓
        List<Mat> textMats = new LinkedList<>();
        for (RotatedRect rotatedRect : rects) {
            Rect rect = rotatedRect.boundingRect();
            Imgproc.rectangle(historyMat, rect.tl(), rect.br(), new Scalar(255, 0, 0, 255));
            textMats.add(historyMat.submat(rect));

//            Point p[] = new Point[4];
//            rotatedRect.points(p);
//            for (int j = 0; j <= 3; j++) {
//                Imgproc.line(src, p[j], p[(j + 1) % 4], new Scalar(0, 255, 0), 2);
//            }
        }
        return textMats;
    }


    /**
     * Wellner自适应阈值算法的二维扩展
     *
     * @param grayBitmap:灰度图像
     * @param srcBitmap:原始图像
     * @param s:用以求出正方形的边长
     * @param t:比率（1-t/100）
     */
    public static Bitmap analyzeFilterPixelMap(Bitmap grayBitmap, Bitmap srcBitmap, int s, int t) {
        //获取位图的宽
        int width = grayBitmap.getWidth();
        //获取位图的高
        int height = grayBitmap.getHeight();
        //通过位图的大小创建像素点数组
        int[] grayPixels = new int[width * height];
        int[] srcPixels = new int[width * height];
        //当前像素点到正方形边界的像素点个数
        int s_len = (int) (width / s * 0.5);
        double t_precent = 1 - t / 100.0;
        //积分图像
        long[][] integralImageSumArr = new long[height][width];
        grayBitmap.getPixels(grayPixels, 0, width, 0, 0, width, height);
        srcBitmap.getPixels(srcPixels, 0, width, 0, 0, width, height);
        /****** 第一步：计算积分图像 ******/
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                /**
                 * A--B
                 * |  |
                 * C--D
                 *积分图算法： D = B + C - A + Xd; 其中Xd表示D位置的灰度值
                 * */
                int currentPixelGray = grayPixels[w + h * width] & 0x000000FF;
                integralImageSumArr[h][w] =
                        (h < 1 ? 0 : integralImageSumArr[h - 1][w])
                                + (w < 1 ? 0 : integralImageSumArr[h][w - 1])
                                - ((h < 1 || w < 1) ? 0 : integralImageSumArr[h - 1][w - 1])
                                + currentPixelGray;
            }
        }
        /**
         * 第二步：
         * (1).求以该像素为中心，s为边长的正方形里面的像素的和的平均值；
         * (2).比较当前像素与平均值的(1-t/100)倍的大小，大于则记录该像素点是二值化的背景，反之则记录为前景。
         **/
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int currentPixelGray = grayPixels[w + h * width] & 0x000000FF;
                if (currentPixelGray > getSquarePixelsGrayMean(integralImageSumArr, w, h, s_len) * t_precent) {
                    srcPixels[w + h * width] = 0x00FFFFFF;
                }
            }
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        result.setPixels(srcPixels, 0, width, 0, 0, width, height);
        return result;

    }

    private static double getSquarePixelsGrayMean(long[][] integralImageSumArr, int w, int h, int s_len) {
        /**
         * 算法：
         *
         * 积分图:    像素灰度值：
         * A--B--C    a--b--c
         * |  |  |    |  |  |
         * D--E--F    d--e--f
         * |  |  |    |  |  |
         * G--H--I    g--h--i
         *那么，e + f + h + i = I - C - G + A
         * */
        int w_real = integralImageSumArr[0].length;
        int h_real = integralImageSumArr.length;
        //求出正方形四个顶点的索引，正方形操作像素边界的部分直接舍弃
        int w_index_left = (w - s_len - 1 < 0 ? 0 : w - s_len - 1);
        int w_index_right = (w + s_len + 1 > w_real - 1 ? w_real - 1 : w + s_len + 1);
        int h_index_top = (h - s_len - 1 < 0 ? 0 : h - s_len - 1);
        int h_index_bottom = (h + s_len + 1 > h_real - 1 ? h_real - 1 : h + s_len + 1);

        int grayNum = 0;
        long graySum = 0;

        long I = integralImageSumArr[h_index_bottom][w_index_right];
        long C = (h_index_top == 0 ? 0 : integralImageSumArr[h_index_top - 1][w_index_right]);
        long G = (w_index_left == 0 ? 0 : integralImageSumArr[h_index_bottom][w_index_left - 1]);
        long A;
        if (h_index_top == 0 && w_index_left != 0) {
            A = integralImageSumArr[0][w_index_left - 1];
        } else if (h_index_top != 0 && w_index_left == 0) {
            A = integralImageSumArr[h_index_top - 1][0];
        } else if (h_index_top == 0 && w_index_left == 0) {
            A = integralImageSumArr[0][0];
        } else {
            A = integralImageSumArr[h_index_top - 1][w_index_left - 1];
        }
        graySum = I - C - G + A;
        grayNum = (h_index_bottom - h_index_top + 1) * (w_index_right - w_index_left + 1);
        return (double) graySum / grayNum;

    }

}