package com.dlh.ocr_test.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * desc   :
 * author : YJ
 * time   : 2020/10/13 14:52
 */
public class TessTwoUtils {

    private Context context;
    private TessBaseAPI baseApi;
    //识别语言英文
    static final String DEFAULT_LANGUAGE = "eng";//"chi_sim";//

    public TessTwoUtils(Context context) {
        this.context = context;
        //训练数据路径，tessdata
        String TESSBASE_PATH = context.getFilesDir().getAbsolutePath();
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


    public void discern(Bitmap bitmap, OnDiscernCallback onDiscernCallback) {
        if (bitmap == null) {
            return;
        }
        baseApi.setImage(bitmap);
        String str = baseApi.getUTF8Text();
        if (onDiscernCallback != null) {
            onDiscernCallback.onDiscern(str);
        }
    }

    public interface OnDiscernCallback {
        void onDiscern(String str);
    }

}
