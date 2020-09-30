package com.dlh.ocr_test.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.dlh.lib.IScanner;
import com.dlh.lib.NV21;
import com.dlh.lib.Result;
import com.dlh.ocr_test.ProcessingImageUtils;
import com.dlh.ocr_test.baidu.FileUtil;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * desc   :
 * author : YJ
 * time   : 2020/9/24 10:33
 */
public class TessTwoScanner implements IScanner {

    private static final String TAG = "TessTwo";
    private NV21 nv21;
    private boolean isBinarization = false;
    private TessBaseAPI baseApi;
    //训练数据路径，tessdata
    static final String TESSBASE_PATH = Environment.getExternalStorageDirectory() + "/";
    //识别语言英文
    static final String DEFAULT_LANGUAGE = "eng";//"chi_sim";//
    private String filePath;

    private boolean isParsing = false;

    public TessTwoScanner(Context context) {
        filePath = FileUtil.getSaveFile(context).getAbsolutePath();
        nv21 = new NV21(context);
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);//这里需要注意
        //设置字典白名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789");
        // 识别黑名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"|~`,./<>? ");
        //设置识别模式
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
//        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
    }

    @Override
    public Result scan(byte[] data, int width, int height) throws Exception {
        if (isParsing) {
            return null;
        }
        isParsing = true;
        Bitmap bitmap = nv21.nv21ToBitmap(data, width, height);
        if (isBinarization) {
            Mat src = new Mat();
            org.opencv.android.Utils.bitmapToMat(bitmap, src);
            src = ProcessingImageUtils.gray(src);
            src = ProcessingImageUtils.blur(src);
            src = ProcessingImageUtils.gaussianBlur(src);
            src = ProcessingImageUtils.medianBlur(src);
            src = ProcessingImageUtils.threshold(src);
            org.opencv.android.Utils.matToBitmap(src, bitmap);
            // 释放内存
            src.release();
        }
        baseApi.setImage(bitmap);
        String str = baseApi.getUTF8Text();
        isParsing = false;
        if (!TextUtils.isEmpty(str)) {
            Result result = new Result();
            result.type = Result.TYPE_IMAGE;
            result.bitmap = bitmap;
            result.data = baseApi.getUTF8Text().trim();
            return result;
        }
        return null;
    }


    public void setBinarization(boolean binarization) {
        isBinarization = binarization;
    }
}
