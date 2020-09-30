package com.dlh.ocr_test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.baidu.ocr.ui.camera.CameraActivity;
import com.bumptech.glide.Glide;
import com.dlh.lib.NV21;
import com.dlh.lib.Result;
import com.dlh.ocr_test.baidu.BaiduOcrActivity;
import com.dlh.ocr_test.baidu.FileUtil;
import com.dlh.ocr_test.baidu.RecognizeService;
import com.dlh.ocr_test.permissions.PermissionGroupUtil;
import com.dlh.ocr_test.permissions.PermissionUtil;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class TesseractActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_CHOOSE = 100;
    private static final int REQUEST_CODE_GENERAL_BASIC = 106;
    private String imagePath;
    private PermissionUtil permissionUtil;
    private TextView content;
    private ImageView imageView, imageView2;
    private RadioButton rb1, rb2, rb3;

    private NV21 nv21;
    private TessBaseAPI baseApi;
    //训练数据路径，tessdata
    private static final String TESSBASE_PATH = Environment.getExternalStorageDirectory() + "/";
    //识别语言英文
    private static final String DEFAULT_LANGUAGE = "chi_sim";//"eng";//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tesseract);

        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        content = findViewById(R.id.content);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        permissionUtil = new PermissionUtil(this);

        nv21 = new NV21(this);
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);

    }

    private void initTessBaseAPI() {
        if (rb1.isChecked()) {
            baseApi.init(TESSBASE_PATH, "chi_sim");
        } else if (rb2.isChecked()) {
            baseApi.init(TESSBASE_PATH, "eng");
        } else if (rb3.isChecked()) {
            baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
        }
        //设置字典白名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789QWERTYUPASDFGHJKLZXCVBNM");
        //识别黑名单
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"|~`,./<>? ");
        //设置识别模式
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        //baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /***
     * 获取FileProvider
     * @param context
     * @return
     */
    public String getFileProvider(Context context) {
        return String.format("%s.cbb.provider", context.getApplicationInfo().packageName);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> pathList = Matisse.obtainPathResult(data);
            if (pathList != null) {
                imagePath = pathList.get(0);
//                Glide.with(TesseractActivity.this)
//                        .load(imagePath)
//                        .into(imageView);
                displaySelectedImage(imagePath);
            }
        } else if (requestCode == REQUEST_CODE_GENERAL_BASIC && resultCode == Activity.RESULT_OK) {
//            Glide.with(TesseractActivity.this)
//                    .load(imagePath)
//                    .into(imageView);
            displaySelectedImage(imagePath);
        }
    }

    private void displaySelectedImage(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSample = 1;
        if (w > 1000 || h > 1000) {
            while (Math.max(w / inSample, h / inSample) > 1000) {
                inSample *= 2;
            }
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        imageView.setImageBitmap(bm);
    }


    public void choosePhotosBtn(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.Group.STORAGE, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                Matisse.from(TesseractActivity.this)
                        //选择图片
                        .choose(MimeType.ofImage())
                        //是否只显示选择的类型的缩略图，就不会把所有图片视频都放在一起，而是需要什么展示什么
                        .showSingleMediaType(true)
                        //这两行要连用 是否在选择图片中展示照相 和适配安卓7.0 FileProvider
                        .capture(true)
                        .captureStrategy(new CaptureStrategy(true, getFileProvider(TesseractActivity.this)))
                        //最大选择数量为1
                        .maxSelectable(1)
                        //选择方向
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        //界面中缩略图的质量
                        .thumbnailScale(0.8f)
                        //蓝色主题
                        .theme(R.style.Matisse_Zhihu)
                        //Glide加载方式
                        .imageEngine(new GlideImageEngine())
                        //请求码
                        .forResult(REQUEST_CODE_CHOOSE);
            }

            @Override
            public void onFailure(List<String> permissions) {

            }
        });
    }

    public void takePhotosBtn(View view) {
        if (!TextUtils.isEmpty(imagePath)) {
            File file = new File(imagePath);
            if (file.exists()) {
                file.delete();
            }
        }
        imagePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
        Intent intent = new Intent(TesseractActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, imagePath);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent, REQUEST_CODE_GENERAL_BASIC);
    }


    public void identifyBtn(View view) {
        initTessBaseAPI();
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            return;
        }

        Mat dst =   ProcessingImageUtils.deSkewText(ProcessingImageUtils.rotateLeft(src));

        // 转换为Bitmap，显示
        Bitmap bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(dst, bm);
        // 释放内存
        dst.release();
        src.release();
        imageView2.setImageBitmap(bm);
        baseApi.setImage(bm);
        String str = baseApi.getUTF8Text();
        if (!TextUtils.isEmpty(str)) {
            content.setText(str);
        }
    }


}