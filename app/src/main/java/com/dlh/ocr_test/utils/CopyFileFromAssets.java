package com.dlh.ocr_test.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * desc   :
 * author : YJ
 * time   : 2020/10/13 13:42
 */
public class CopyFileFromAssets {

    /***
     * 复制 TessTwoData
     * @param mContext
     */
    public static void copyTessTwoData(Context mContext) {
        //存储路径
        String path = mContext.getFilesDir().getAbsolutePath() + "/tessdata";
        //英文eng.traineddata
        File eng = new File(path, "eng.traineddata");
        if (!eng.exists()) {
            CopyFileFromAssets.copy(mContext, "eng.traineddata", path, "eng.traineddata");
        }

        //中文chi_sim.traineddata
        File chi_sim = new File(path, "chi_sim.traineddata");
        if (!chi_sim.exists()) {
            CopyFileFromAssets.copy(mContext, "chi_sim.traineddata", path, "chi_sim.traineddata");
        }
    }


    /**
     * @param mContext
     * @param ASSETS_NAME 要复制的文件名
     * @param savePath    要保存的路径
     * @param saveName    复制后的文件名
     *                    testCopy(Context context)是一个测试例子。
     */

    public static void copy(Context mContext, String ASSETS_NAME, String savePath, String saveName) {
        String filename = savePath + "/" + saveName;

        File dir = new File(savePath);
        // 如果目录不中存在，创建这个目录
        if (!dir.exists())
            dir.mkdir();
        try {
            if (!(new File(filename)).exists()) {
                InputStream is = mContext.getResources().getAssets().open(ASSETS_NAME);
                FileOutputStream fos = new FileOutputStream(filename);
                byte[] buffer = new byte[7168];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
