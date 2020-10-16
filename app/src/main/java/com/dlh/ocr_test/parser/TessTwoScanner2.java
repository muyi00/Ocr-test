package com.dlh.ocr_test.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.dlh.lib.IScanner;
import com.dlh.lib.ImageInfo;
import com.dlh.lib.NV21;
import com.dlh.lib.Result;
import com.dlh.ocr_test.ProcessingImageUtils;
import com.dlh.ocr_test.utils.TessTwoUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * desc   :
 * author : YJ
 * time   : 2020/9/24 10:33
 */
public class TessTwoScanner2 implements IScanner {

    private static final String TAG = "TessTwoScanner2";
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


    public TessTwoScanner2(Context context) {
        Log.i(TAG, "TessTwo 初始化");
        nv21 = new NV21(context);
        tessTwoUtils = new TessTwoUtils(context);
    }

    @Override
    public Result scan(byte[] data, int width, int height) throws Exception {
        imageInfos.clear();
        Log.i(TAG, "开始处理图片");
        t1 = System.currentTimeMillis();
        Bitmap bitmap = nv21.nv21ToBitmap(data, width, height);

        Mat src = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, src);
        //原图
        Mat src0 = src.clone();
        Mat src02 = src.clone();
        Core.add(src, new Scalar(-20, -20, -20), src);
        Core.multiply(src, new Scalar(2, 2, 2), src);
        src = ProcessingImageUtils.gray(src);

        if (isEnableGaussianBlur) {
            Imgproc.GaussianBlur(
                    src,
                    src,
                    new Size(3, 3),
                    0);
        }


        if (isEnableMedianBlur) {
            Imgproc.medianBlur(src, src, 15);
        }

        if (isEnableBlur) {
            Imgproc.blur(src, src, new Size(3, 3));
        }

        src = ProcessingImageUtils.threshold(src);
        Mat thresholdMat = src.clone();

        Mat erodeKernel = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT,
                new Size(5, 5));
        Imgproc.erode(src, src, erodeKernel);

        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT,
                new Size(6, 6));
        Imgproc.morphologyEx(src, src, Imgproc.MORPH_OPEN, kernel);
        //处理图
        Mat workedExMat = src.clone();

        //查找轮廓
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

        List<Mat> validMats = new ArrayList<Mat>();
        for (int i = 0; i < contours.size(); i++) {
            //计算点集或灰度图像的非零像素的右上边界矩形。
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if (rect.height >= workedExMat.height()) {
                //矩形区域太大
                continue;
            }
            if (rect.height < workedExMat.height() / 4) {
                //矩形区域太小
                continue;
            }

            Imgproc.rectangle(src0, rect, new Scalar(255.0, 255.0, 0.0), 4, Imgproc.LINE_8);
            Imgproc.rectangle(workedExMat, rect, new Scalar(255.0, 255.0, 0.0), 4, Imgproc.LINE_8);

            if (isDiscern_0_Bitmap) {
                validMats.add(src02.submat(rect));
            } else {
                validMats.add(workedExMat.submat(rect));
            }
        }
        addShowBitmap("边界矩形1", src0);
        addShowBitmaps("子图", validMats);
        t2 = System.currentTimeMillis();
        Log.i(TAG, "处理图片完成：" + (t2 - t1));
        // 释放内存
        src.release();

        Log.i(TAG, "validMats=" + validMats.size());
        t1 = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        Log.i(TAG, "开始识别-------------------------------》");
        for (Mat mat : validMats) {
            Bitmap bitmapTemp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(mat, bitmapTemp);
            String str = tessTwoUtils.orcDiscern(bitmapTemp);
            Log.i(TAG, "子图识别内容：" + str);
            sb.append(tessTwoUtils.orcDiscern(bitmapTemp));
        }


        Result result = new Result();
        result.type = Result.TYPE_IMAGE;
        result.imageInfos = imageInfos;
        result.data = sb.toString();
        t2 = System.currentTimeMillis();
        Log.i(TAG, "识别内容：" + result.data);
        Log.i(TAG, "识别成功耗时：" + +(t2 - t1));
        return result;
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
