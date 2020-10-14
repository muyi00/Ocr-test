package com.dlh.ocr_test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.dlh.lib.ImageInfo;
import com.dlh.ocr_test.adapter.CommonAdapter;
import com.dlh.ocr_test.adapter.ViewHolder;
import com.dlh.ocr_test.permissions.PermissionGroupUtil;
import com.dlh.ocr_test.permissions.PermissionUtil;
import com.dlh.ocr_test.utils.AsyncTaskUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ProcessingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 100;
    private String imagePath;
    private PermissionUtil permissionUtil;
    private ArrayList<ImageInfo> imageInfos = new ArrayList<>();
    private CommonAdapter<ImageInfo> adapter;
    private ImageView imageView0;
    private CheckBox left_rotation_cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        permissionUtil = new PermissionUtil(this);

        imageView0 = findViewById(R.id.imageView0);
        left_rotation_cb = findViewById(R.id.left_rotation_cb);


        adapter = new CommonAdapter<ImageInfo>(this, imageInfos, R.layout.item_bitmap_info) {
            @Override
            public void convert(ViewHolder helper, int position, ImageInfo item) {
                helper.setText(R.id.title, item.title);
                ImageView image = helper.getView(R.id.image);
                Glide.with(ProcessingActivity.this)
                        .load(item.bitmap)
                        .into(image);
            }
        };
        ListView bitmap_lv = findViewById(R.id.bitmap_lv);
        bitmap_lv.setAdapter(adapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> pathList = Matisse.obtainPathResult(data);
            if (pathList != null) {
                imagePath = pathList.get(0);
                Glide.with(ProcessingActivity.this)
                        .load(imagePath)
                        .into(imageView0);

            }
        }
    }

    /***
     * 获取FileProvider
     * @param context
     * @return
     */
    public String getFileProvider(Context context) {
        return String.format("%s.cbb.provider", context.getApplicationInfo().packageName);
    }

    /***
     * 选择图片
     * @param view
     */
    public void choosePictureBtn(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.Group.STORAGE, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                Matisse.from(ProcessingActivity.this)
                        //选择图片
                        .choose(MimeType.ofImage())
                        //是否只显示选择的类型的缩略图，就不会把所有图片视频都放在一起，而是需要什么展示什么
                        .showSingleMediaType(true)
                        //这两行要连用 是否在选择图片中展示照相 和适配安卓7.0 FileProvider
                        .capture(true)
                        .captureStrategy(new CaptureStrategy(true, getFileProvider(ProcessingActivity.this)))
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


    public void disposeBtn(View view) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }
        imageInfos.clear();
        new AsyncTaskUtil(this, new AsyncTaskUtil.AsyncCallBack() {
            @Override
            public int asyncProcess() throws InterruptedException {
                dispose();
                return 0;
            }

            @Override
            public void postUI(int rsult) {
                adapter.notifyDataSetChanged();
            }
        }).setMaskContent("正在处理图片").executeTask();
    }


    private void dispose() {
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            return;
        }
        if (left_rotation_cb.isChecked()) {
            src = ProcessingImageUtils.rotateLeft(src);
        }

        Core.multiply(src, new Scalar(2, 2, 2), src);
        //1.转化成灰度图
        src = ProcessingImageUtils.gray(src);
        addShowBitmap("灰度", src);
        src = ProcessingImageUtils.gaussianBlur(src);
        addShowBitmap("高斯", src);
        src = ProcessingImageUtils.medianBlur(src);
        src = ProcessingImageUtils.blur(src);
//        src = ProcessingImageUtils.threshold(src);
        src = ProcessingImageUtils.adaptiveThreshold(src, 7, 10);
        addShowBitmap("二值化", src);
        Mat historyMat = src.clone();

        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
        Imgproc.erode(src, src, erodeKernel);
        addShowBitmap("腐蚀", src);


//
//        Mat element2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(60, 60));
//        Mat dilate1 = new Mat();
//        Imgproc.dilate(src, dilate1, element2);
//        addShowBitmap("sobel处理-第一次膨胀", dilate1);
//
        //3.查找和筛选文字区域
        List<RotatedRect> rects = ProcessingImageUtils.findTextRegion(src);

        //4.用绿线画出这些找到的轮廓
        List<Mat> textMats = new LinkedList<>();
        try {
            for (RotatedRect rotatedRect : rects) {
                Rect rect = rotatedRect.boundingRect();
                if (rect.height < historyMat.height() / 6) {
                    continue;
                }
                textMats.add(historyMat.submat(rect));
            }
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }
//
//        //将矩阵转换为图像
//        addShowBitmap("最终结果", src);
        addShowBitmaps("内容截取", textMats);
        // 释放内存
        src.release();
    }


    private void addShowBitmaps(String title, List<Mat> srcList) {
        for (Mat src : srcList) {
            addShowBitmap(title, src);
        }
    }

    private void addShowBitmap(String title, Mat src) {
        Bitmap bitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(src, bitmap);

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.title = title;
        imageInfo.bitmap = bitmap;
        imageInfos.add(imageInfo);

    }

}