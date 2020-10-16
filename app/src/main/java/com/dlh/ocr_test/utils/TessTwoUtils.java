package com.dlh.ocr_test.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * desc   :
 * author : YJ
 * time   : 2020/10/13 14:52
 */
public class TessTwoUtils {
    private static final String TAG = "TessTwoUtils";

    private Context context;
    private TessBaseAPI baseApi;
    //识别语言英文
    static final String DEFAULT_LANGUAGE = "dlh";//"chi_sim";//dlh
    private String path;

    public TessTwoUtils(Context context) {
        this.context = context;
        //训练数据路径，tessdata
        path = context.getFilesDir().getAbsolutePath();

    }


    public String orcDiscern(Bitmap bitmap) {
        if (bitmap == null) {
            Log.i(TAG, "bitmap == null");
            return "";
        }
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(path, DEFAULT_LANGUAGE);//这里需要注意
        //设置字典白名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789QWERTYUPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm");
        // 识别黑名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"|~`,./<>? ");
        //设置识别模式
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
//        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);

        baseApi.setImage(bitmap);
        String str = baseApi.getUTF8Text();
        Log.i(TAG, "getUTF8Text =" + str);
        return str;
    }
}
