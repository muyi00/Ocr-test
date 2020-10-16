package com.dlh.ocr_test.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.dlh.lib.IScanner;
import com.dlh.lib.ImageInfo;
import com.dlh.lib.NV21;
import com.dlh.lib.Result;
import com.dlh.ocr_test.ProcessingImageUtils;
import com.dlh.ocr_test.TesseractUtil;
import com.dlh.ocr_test.baidu.FileUtil;
import com.dlh.ocr_test.utils.TessTwoUtils;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.core.Core.BORDER_DEFAULT;

/**
 * desc   :
 * author : YJ
 * time   : 2020/9/24 10:33
 */
public class TessTwoScanner implements IScanner {

    private static final String TAG = "TessTwoScanner";
    private NV21 nv21;
    public List<ImageInfo> imageInfos = new ArrayList<>();
    private TessTwoUtils tessTwoUtils;
    private long t1, t2;
    //高斯
    public boolean isEnableGaussianBlur = false;
    //中值
    public boolean isEnableMedianBlur = false;
    //均值
    public boolean isEnableBlur = false;
    public boolean isDiscern_0_Bitmap = false;


    public TessTwoScanner(Context context) {
        Log.i(TAG, "TessTwo 初始化");
        nv21 = new NV21(context);
        tessTwoUtils = new TessTwoUtils(context);
    }

    @Override
    public Result scan(byte[] data, int width, int height) throws Exception {
        Log.i(TAG, "开始处理图片");
        t1 = System.currentTimeMillis();
        Bitmap bitmap = nv21.nv21ToBitmap(data, width, height);
        imageInfos.clear();
        Mat src = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, src);
        //原图
        Mat src0 = src.clone();
        //addShowBitmap("原图", src);

        Core.add(src, new Scalar(-20, -20, -20), src);
        Core.multiply(src, new Scalar(1.8, 1.8, 1.8), src);

        //1.转化成灰度图
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
//        addShowBitmap("灰度图", src);
        //二值化
//        Mat mat11 = ProcessingImageUtils.threshold(src);
//        addShowBitmap("二值化", mat11);

        if (isEnableBlur) {
            //均值滤波
            Imgproc.blur(src, src, new Size(3, 3));
        }

        if (isEnableGaussianBlur) {
            //高斯滤波
            Imgproc.GaussianBlur(
                    src,
                    src,
                    new Size(5, 5),
                    0);
        }

        if (isEnableMedianBlur) {
            //中值滤波
            Imgproc.medianBlur(src, src, 15);
        }

        Imgproc.adaptiveThreshold(
                src,
                src,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                5,
                9);
        addShowBitmap("二值", src);

//        Mat kernel2 = Imgproc.getStructuringElement(
//                Imgproc.CV_SHAPE_RECT,
//                new Size(3, 3));
//        Imgproc.morphologyEx(src, src, Imgproc.MORPH_HITMISS, kernel2);
//        addShowBitmap("击中击不中", src);
//
//        Mat kernel = Imgproc.getStructuringElement(
//                Imgproc.CV_SHAPE_RECT,
//                new Size(3, 3));
//        Imgproc.morphologyEx(src, src, Imgproc.MORPH_OPEN, kernel);
//        addShowBitmap("开操作", src);

//        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
//        Imgproc.dilate(src, src, dilateKernel);
//        addShowBitmap("膨胀", src);


//        Core.bitwise_not(src, src);
//        Core.add(mat11, mat22, src);
//        addShowBitmap("图片相加", src);

//        //膨胀
//        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
//        Imgproc.dilate(src, src, dilateKernel);
//        addShowBitmap("膨胀", src);
        //Core.bitwise_not(src, src);
//
        //腐蚀
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(18, 38));
        Imgproc.erode(src, src, erodeKernel);

        addShowBitmap("膨胀", src);
        StringBuilder sb = new StringBuilder();

        //轮廓查找
        t1 = System.currentTimeMillis();
        List<Mat> subgraphMats = getSubgraphMats(src0, src, false);
        for (Mat subMat : subgraphMats) {
            addShowBitmap("处理前的子图", subMat);
            Mat disposesubMat = disposeSubMat(subMat);
            addShowBitmap("处理后的子图", disposesubMat);

            Mat selectMats;
            if (isDiscern_0_Bitmap) {
                selectMats = subMat;
            } else {
                selectMats = disposesubMat;
            }
            //查找子图的轮廓
            List<Mat> subMatsubMats = getSubgraphMats(selectMats, disposesubMat, true);
            for (Mat mat : subMatsubMats) {
                Bitmap bitmapTemp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                org.opencv.android.Utils.matToBitmap(mat, bitmapTemp);
                String str = tessTwoUtils.orcDiscern(bitmapTemp);
                Log.i(TAG, "子图识别内容：" + str);
                sb.append(tessTwoUtils.orcDiscern(bitmapTemp));
            }
        }

        // 释放内存
        src.release();

        Result result = new Result();
        result.type = Result.TYPE_IMAGE;
        result.imageInfos = imageInfos;
        result.data = sb.toString();
        t2 = System.currentTimeMillis();
        Log.i(TAG, "识别内容：" + result.data);
        Log.i(TAG, "识别成功：" + (t2 - t1));
        return result;
    }


    /***
     * 获取子图，将原图分隔
     * @param src0 原图
     * @param src 处理后的图片
     * @return
     */
    private List<Mat> getSubgraphMats(Mat src0, Mat src, boolean isshow) {
        //第一次轮廓查找
        Mat mSource = src0.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                Rect rect0 = Imgproc.boundingRect(o1);
                Rect rect1 = Imgproc.boundingRect(o2);
                return rect0.x - rect1.x;
            }
        });
        List<Mat> subgraphMats = new ArrayList<Mat>();
        for (int i = 0; i < contours.size(); i++) {
            //计算点集或灰度图像的非零像素的右上边界矩形。
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if (rect.height >= src0.height()) {
                //矩形区域太大
                continue;
            }
            if (rect.height < src0.height() / 4) {
                //矩形区域太小
                continue;
            }
            if (isshow) {
                //区域
                Imgproc.rectangle(mSource, rect, new Scalar(255.0, 255.0, 0.0), 4, Imgproc.LINE_8);
            }
            Mat temp = src0.submat(rect);
            subgraphMats.add(temp);
        }
        if (isshow) {
            addShowBitmap("边界矩形", mSource);
        }
        return subgraphMats;
    }


    /***
     * 处理子图
     * @param subMat
     * @return
     */
    private Mat disposeSubMat(Mat subMat) {
        Mat subSrc = new Mat();
        Core.multiply(subMat, new Scalar(2, 2, 2), subSrc);
        //灰度图
        Imgproc.cvtColor(subSrc, subSrc, Imgproc.COLOR_RGB2GRAY);
        //滤波
        //Imgproc.GaussianBlur(subSrc, subSrc, new Size(5, 5), 0);
        Imgproc.medianBlur(subSrc, subSrc, 17);

        //Imgproc.blur(subSrc, subSrc, new Size(3, 3));
        //二值化
        Imgproc.threshold(subSrc, subSrc, 100, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);

//        Mat erodeKernel = Imgproc.getStructuringElement(
//                Imgproc.CV_SHAPE_RECT,
//                new Size(5, 7));
//        //腐蚀
//        Imgproc.erode(subSrc, subSrc, erodeKernel);
//
//        Mat kernel = Imgproc.getStructuringElement(
//                Imgproc.CV_SHAPE_RECT,
//                new Size(5, 9));
//        //开操作
//        Imgproc.morphologyEx(subSrc, subSrc, Imgproc.MORPH_OPEN, kernel);

        return subSrc;
    }


    private void addShowBitmap(String title, Bitmap bitmap) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.title = title;
        imageInfo.bitmap = bitmap;
        imageInfos.add(imageInfo);
    }

    private void addShowBitmap(String title, Mat src) {
        Bitmap bitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(src, bitmap);

        addShowBitmap(title, bitmap);
    }

    private void addShowBitmaps(String title, List<Mat> srcList) {
        for (Mat src : srcList) {
            addShowBitmap(title, src);
        }
    }


}
