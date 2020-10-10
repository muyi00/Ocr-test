package com.dlh.ocr_test.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import com.dlh.lib.IScanner;
import com.dlh.lib.NV21;
import com.dlh.lib.Result;
import com.dlh.ocr_test.ProcessingImageUtils;
import com.dlh.ocr_test.baidu.FileUtil;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * desc   :
 * author : YJ
 * time   : 2020/9/24 10:33
 */
public class TessTwoScanner implements IScanner {

    private static final String TAG = "TessTwo";
    private NV21 nv21;
    private boolean isProcessing = false;
    private TessBaseAPI baseApi;
    //训练数据路径，tessdata
    static final String TESSBASE_PATH = Environment.getExternalStorageDirectory() + "/";
    //识别语言英文
    static final String DEFAULT_LANGUAGE = "eng";//"chi_sim";//
    private String filePath;

    public TessTwoScanner(Context context) {
        filePath = FileUtil.getSaveFile(context).getAbsolutePath();
        nv21 = new NV21(context);
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);//这里需要注意
        //设置字典白名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789QWERTYUPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm");
        // 识别黑名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"|~`,./<>? ");
        //设置识别模式
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
//        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
    }

    @Override
    public Result scan(byte[] data, int width, int height) throws Exception {
        Bitmap bitmap = nv21.nv21ToBitmap(data, width, height);
        if (isProcessing) {
            Mat src = new Mat();
            org.opencv.android.Utils.bitmapToMat(bitmap, src);
            //1.转化成灰度图
            src = ProcessingImageUtils.gray(src);
            //均值滤波
            src = ProcessingImageUtils.blur(src);
            //高斯滤波
            src = ProcessingImageUtils.gaussianBlur(src);
            //中值滤波
            src = ProcessingImageUtils.medianBlur(src);
            //二值化
            //src = ProcessingImageUtils.threshold(src);
            Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
            org.opencv.android.Utils.matToBitmap(src, bitmap);
            // 释放内存
            src.release();
        }
        baseApi.setImage(bitmap);
        String str = baseApi.getUTF8Text();
        if (!TextUtils.isEmpty(str)) {
            Result result = new Result();
            result.type = Result.TYPE_IMAGE;
            result.bitmap = bitmap;
            result.data = baseApi.getUTF8Text().trim();
            return result;
        }
        return null;
    }


    public void setProcessing(boolean processing) {
        isProcessing = processing;
    }
}
