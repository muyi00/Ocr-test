package com.dlh.ocr_test.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.dlh.lib.IScanner;
import com.dlh.lib.ImageInfo;
import com.dlh.lib.NV21;
import com.dlh.lib.Result;
import com.dlh.ocr_test.baidu.RecognizeService;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * desc   :
 * author : YJ
 * time   : 2020/10/15 9:26
 */
public class BaiduScanner implements IScanner {

    private static final String TAG = "BaiduScanner";

    //高斯
    public boolean isEnableGaussianBlur = false;
    //中值
    public boolean isEnableMedianBlur = false;
    //均值
    public boolean isEnableBlur = false;
    private String filePath;
    private NV21 nv21;
    private Context context;
    private int previewFrameCount = 0;
    public List<ImageInfo> imageInfos = new ArrayList<>();

    public BaiduScanner(Context context) {
        this.context = context;
        nv21 = new NV21(context);
        String basePath = context.getFilesDir().getAbsolutePath();
        filePath = String.format("%s/baidu_orc.png", basePath);
    }


    @Override
    public Result scan(byte[] data, int width, int height) throws Exception {
        // 节流
        if (previewFrameCount++ % 5 != 0) {
            return null;
        }
        imageInfos.clear();
        Bitmap bitmap = nv21.nv21ToBitmap(data, width, height);
        addShowBitmap("百度识别", bitmap);
        Utils.saveBitmap(bitmap, filePath);

        final Result resultInfo = new Result();

        resultInfo.type = Result.TYPE_IMAGE;
        resultInfo.imageInfos = imageInfos;
        RecognizeService.recAccurateBasic(context, filePath,
                new RecognizeService.ServiceListener() {
                    @Override
                    public void onResult(String result) {
                        resultInfo.data = result;
                        Log.i(TAG, "百度识别成功:" + result);
                    }
                });
        return resultInfo;
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
