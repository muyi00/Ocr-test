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
import java.util.LinkedList;
import java.util.List;

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


    public TessTwoScanner(Context context) {
        Log.i(TAG, "TessTwo 初始化");
        nv21 = new NV21(context);
        tessTwoUtils = new TessTwoUtils(context);
    }

    @Override
    public Result scan(byte[] data, int width, int height) throws Exception {
        Bitmap bitmap = nv21.nv21ToBitmap(data, width, height);
        imageInfos.clear();

        Log.i(TAG, "开始处理图片");
        Mat src = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, src);
        Mat mSource = new Mat();
        Imgproc.cvtColor(src, mSource, Imgproc.COLOR_BGR2RGB);
        addShowBitmap("原图", src);


//        Core.add(src, new Scalar(10,10,10), src);
//        addShowBitmap("调整亮度", src);
//
//        Core.multiply(src, new Scalar(1.8, 1.8, 1.8), src);
//        addShowBitmap("调整对比度", src);

        //1.转化成灰度图
        src = ProcessingImageUtils.gray(src);
        //addShowBitmap("灰度图", src);

        if (isEnableGaussianBlur) {
            //高斯滤波
            src = ProcessingImageUtils.gaussianBlur(src);
//            addShowBitmap("高斯滤波", src);
        }
        if (isEnableMedianBlur) {
            //中值滤波
            src = ProcessingImageUtils.medianBlur(src);
//            addShowBitmap("中值滤波", src);
        }

        if (isEnableBlur) {
            //均值滤波
            src = ProcessingImageUtils.blur(src);
//            addShowBitmap("均值滤波", src);
        }

        //二值化
//        Mat src1 = ProcessingImageUtils.threshold(src);
//        addShowBitmap("二值化", src1);
        src = ProcessingImageUtils.adaptiveThreshold(src,7,10);
        addShowBitmap("二值化", src);

//        //膨胀
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
//        Imgproc.dilate(src, src, kernel);
//        addShowBitmap("膨胀", src);
//        //Core.bitwise_not(src, src);
//
//        //腐蚀
//        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(5, 10));
//        Imgproc.erode(src, src, erodeKernel);
//        addShowBitmap("腐蚀", src);

//        //查找轮廓
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//        for (int i = 0; i < contours.size(); i++) {
//            //计算点集或灰度图像的非零像素的右上边界矩形。
//            Rect rect = Imgproc.boundingRect(contours.get(i));
//            addShowBitmap("子区域", mSource.submat(rect));
//        }
        org.opencv.android.Utils.matToBitmap(src, bitmap);
        // 释放内存
        src.release();
        Log.i(TAG, "处理图片完成");

        final Result result = new Result();
        result.type = Result.TYPE_IMAGE;
        result.imageInfos = imageInfos;
        tessTwoUtils.discern(bitmap, new TessTwoUtils.OnDiscernCallback() {
            @Override
            public void onDiscern(String str) {
                Log.i(TAG, "识别成功");
                result.data = str;
            }
        });
        return result;
    }

    private boolean isBlackLine(Mat src, int currentRow) {
        byte[] linePxData = new byte[src.channels() * src.cols()];
        src.get(currentRow, 0, linePxData);
        int blackCount = 0;
        for (int col = 0; col < linePxData.length; col++) {//行中循环列，处理内容：修改一整行的数据
            Log.i(TAG, "像素值" + linePxData[col]);
            if (linePxData[col] == 0x00) {

            }
        }
        return false;
    }


    private void addShowBitmap(String title, Bitmap bitmap) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.title = title;
        imageInfo.bitmap = bitmap;
        imageInfos.add(0, imageInfo);
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
