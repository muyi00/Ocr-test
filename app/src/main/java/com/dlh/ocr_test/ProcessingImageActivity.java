package com.dlh.ocr_test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dlh.ocr_test.permissions.PermissionGroupUtil;
import com.dlh.ocr_test.permissions.PermissionUtil;
import com.dlh.ocr_test.utils.AsyncTaskUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc: 图片处理
 * @author: YJ
 * @time: 2020/9/28
 */
public class ProcessingImageActivity extends AppCompatActivity {

    private ImageView imageView1, imageView2;
    private static final int REQUEST_CODE_CHOOSE = 100;
    private String imagePath;
    private PermissionUtil permissionUtil;
    private CheckBox left_rotation_rb,
            correct_rb,
            de_skew_rb,
            medianBlur_rb,
            gray_rb,
            blur_rb,
            gaussianBlur_rb,
            sharpen_rb,
            sobelXY_rb,
            binarization_rb;

    private CheckBox dilate_rb0, dilate_rb1, dilate_rb2;
    private CheckBox erode_rb0, erode_rb1, erode_rb2;
    private CheckBox morphologyEx_rb2, morphologyEx_rb3, morphologyEx_rb4, morphologyEx_rb5, morphologyEx_rb6, morphologyEx_rb7;
    private TextView dilate_value_tv, erode_value_tv, morphologyEx_value_tv;
    private SeekBar dilate_seekbar, erode_seekbar, morphologyEx_seekbar;
    private int dilate_value = 3, erode_value = 3, morphologyEx_value = 3;

    private Bitmap disposBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_image);
        permissionUtil = new PermissionUtil(this);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);

        left_rotation_rb = findViewById(R.id.left_rotation_rb);
        de_skew_rb = findViewById(R.id.de_skew_rb);
        correct_rb = findViewById(R.id.correct_rb);

        medianBlur_rb = findViewById(R.id.medianBlur_rb);
        sobelXY_rb = findViewById(R.id.sobelXY_rb);

        gray_rb = findViewById(R.id.gray_rb);
        blur_rb = findViewById(R.id.blur_rb);
        gaussianBlur_rb = findViewById(R.id.gaussianBlur_rb);
        sharpen_rb = findViewById(R.id.sharpen_rb);

        dilate_rb0 = findViewById(R.id.dilate_rb0);
        dilate_rb1 = findViewById(R.id.dilate_rb1);
        dilate_rb2 = findViewById(R.id.dilate_rb2);

        erode_rb0 = findViewById(R.id.erode_rb0);
        erode_rb1 = findViewById(R.id.erode_rb1);
        erode_rb2 = findViewById(R.id.erode_rb2);

        dilate_value_tv = findViewById(R.id.dilate_value_tv);
        erode_value_tv = findViewById(R.id.erode_value_tv);
        dilate_seekbar = findViewById(R.id.dilate_seekbar);
        erode_seekbar = findViewById(R.id.erode_seekbar);

        binarization_rb = findViewById(R.id.binarization_rb);

        morphologyEx_rb2 = findViewById(R.id.morphologyEx_rb2);
        morphologyEx_rb3 = findViewById(R.id.morphologyEx_rb3);
        morphologyEx_rb4 = findViewById(R.id.morphologyEx_rb4);
        morphologyEx_rb5 = findViewById(R.id.morphologyEx_rb5);
        morphologyEx_rb6 = findViewById(R.id.morphologyEx_rb6);
        morphologyEx_rb7 = findViewById(R.id.morphologyEx_rb7);
        morphologyEx_value_tv = findViewById(R.id.morphologyEx_value_tv);
        morphologyEx_seekbar = findViewById(R.id.morphologyEx_seekbar);

        dilate_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dilate_value = progress;
                dilate_value_tv.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        erode_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                erode_value = progress;
                erode_value_tv.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        morphologyEx_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                morphologyEx_value = progress;
                morphologyEx_value_tv.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /***
     * 选择图片
     * @param view
     */
    public void choosePictureBtn(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.Group.STORAGE, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                Matisse.from(ProcessingImageActivity.this)
                        //选择图片
                        .choose(MimeType.ofImage())
                        //是否只显示选择的类型的缩略图，就不会把所有图片视频都放在一起，而是需要什么展示什么
                        .showSingleMediaType(true)
                        //这两行要连用 是否在选择图片中展示照相 和适配安卓7.0 FileProvider
                        .capture(true)
                        .captureStrategy(new CaptureStrategy(true, getFileProvider(ProcessingImageActivity.this)))
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
                Glide.with(ProcessingImageActivity.this)
                        .load(imagePath)
                        .into(imageView1);
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


    public void disposeBtn(View view) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }
        new AsyncTaskUtil(this, new AsyncTaskUtil.AsyncCallBack() {
            @Override
            public int asyncProcess() throws InterruptedException {
                dispose2();
                return 0;
            }

            @Override
            public void postUI(int rsult) {
                if (disposBitmap != null) {
                    imageView2.setImageBitmap(disposBitmap);
                }
            }
        }).setMaskContent("正在处理图片").executeTask();
    }

    private void dispose() {
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            return;
        }
        src = ProcessingImageUtils.resize(src);

        //左旋转照片
        if (left_rotation_rb.isChecked()) {
            src = ProcessingImageUtils.rotateLeft(src);
        }

        //灰度
        if (gray_rb.isChecked()) {
            src = ProcessingImageUtils.gray(src);
        }
        //均值滤波
        if (blur_rb.isChecked()) {
            src = ProcessingImageUtils.blur(src);
        }
        //高斯滤波
        if (gaussianBlur_rb.isChecked()) {
            src = ProcessingImageUtils.gaussianBlur(src);
        }
        //中值滤波
        if (medianBlur_rb.isChecked()) {
            src = ProcessingImageUtils.medianBlur(src);
        }
        //锐化滤波
        if (sharpen_rb.isChecked()) {
            src = ProcessingImageUtils.sharpen(src);
        }
        //二值化
        if (binarization_rb.isChecked()) {
            src = ProcessingImageUtils.binarization(src);
        }

        //src = ProcessingImageUtils.sobelBorderDetection(src);

        //将矩阵转换为图像
        disposBitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(src, disposBitmap);
        // 释放内存
        src.release();
    }


    private void dispose2() {
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            return;
        }
        //旋转图片
        if (left_rotation_rb.isChecked()) {
            src = ProcessingImageUtils.rotateLeft(src);
        }

        Core.multiply(src, new Scalar(2, 2, 2), src);

        //灰度
        if (gray_rb.isChecked()) {
            src = ProcessingImageUtils.gray(src);
        }

        if (correct_rb.isChecked()) {
            src = ProcessingImageUtils.deSkewText(src);
        }
        //均值滤波
        if (blur_rb.isChecked()) {
            src = ProcessingImageUtils.blur(src);
        }
        //高斯滤波
        if (gaussianBlur_rb.isChecked()) {
            src = ProcessingImageUtils.gaussianBlur(src);
        }
        //中值滤波
        if (medianBlur_rb.isChecked()) {
            src = ProcessingImageUtils.medianBlur(src);
        }
        //锐化滤波
        if (sharpen_rb.isChecked()) {
            src = ProcessingImageUtils.sharpen(src);
        }
        if (sobelXY_rb.isChecked()) {
            src = ProcessingImageUtils.sobelXY(src);
        }
        //二值化
        if (binarization_rb.isChecked()) {
            src = ProcessingImageUtils.threshold(src);
        }
        //膨胀
        if (dilate_rb0.isChecked() || dilate_rb1.isChecked() || dilate_rb2.isChecked()) {
            int shape = Imgproc.CV_SHAPE_RECT;
            if (dilate_rb0.isChecked()) {
                shape = Imgproc.CV_SHAPE_RECT;
            } else if (dilate_rb1.isChecked()) {
                shape = Imgproc.CV_SHAPE_CROSS;
            } else if (dilate_rb2.isChecked()) {
                shape = Imgproc.CV_SHAPE_ELLIPSE;
            }
            Mat kernel = Imgproc.getStructuringElement(shape, new Size(dilate_value, dilate_value));
            Imgproc.dilate(src, src, kernel);
        }

        //腐蚀
        if (erode_rb0.isChecked() || erode_rb1.isChecked() || erode_rb2.isChecked()) {
            int shape = Imgproc.CV_SHAPE_RECT;
            if (erode_rb0.isChecked()) {
                shape = Imgproc.CV_SHAPE_RECT;
            } else if (erode_rb1.isChecked()) {
                shape = Imgproc.CV_SHAPE_CROSS;
            } else if (erode_rb2.isChecked()) {
                shape = Imgproc.CV_SHAPE_ELLIPSE;
            }
            Mat kernel = Imgproc.getStructuringElement(shape, new Size(erode_value, erode_value));
            Imgproc.erode(src, src, kernel);
        }

        //形态处理
        if (morphologyEx_rb2.isChecked()
                || morphologyEx_rb3.isChecked()
                || morphologyEx_rb4.isChecked()
                || morphologyEx_rb5.isChecked()
                || morphologyEx_rb6.isChecked()
                || morphologyEx_rb7.isChecked()) {
            int flag = Imgproc.MORPH_OPEN;
            if (morphologyEx_rb2.isChecked()) {
                flag = Imgproc.MORPH_OPEN;
            } else if (morphologyEx_rb3.isChecked()) {
                flag = Imgproc.MORPH_CLOSE;
            } else if (morphologyEx_rb4.isChecked()) {
                flag = Imgproc.MORPH_GRADIENT;
            } else if (morphologyEx_rb5.isChecked()) {
                flag = Imgproc.MORPH_TOPHAT;
            } else if (morphologyEx_rb6.isChecked()) {
                flag = Imgproc.MORPH_BLACKHAT;
            } else if (morphologyEx_rb7.isChecked()) {
                flag = Imgproc.MORPH_HITMISS;
            }
            Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(morphologyEx_value, morphologyEx_value));
            Imgproc.morphologyEx(src, src, flag, kernel);
        }

        //将矩阵转换为图像
        disposBitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(src, disposBitmap);
        // 释放内存
        src.release();
    }

    private void dispose3() {
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            return;
        }
        src = ProcessingImageUtils.rotateLeft(src);
        //1.转化成灰度图
        src = ProcessingImageUtils.gray(src);
        //均值滤波
        src = ProcessingImageUtils.blur(src);
        //高斯滤波
        src = ProcessingImageUtils.gaussianBlur(src);
        //中值滤波
        src = ProcessingImageUtils.medianBlur(src);
        //二值化
        src = ProcessingImageUtils.threshold(src);


        //2.形态学变换的预处理，得到可以查找矩形的轮廓
        Mat dilation = preprocess(src);

        //3.查找和筛选文字区域
        List<RotatedRect> rects = findTextRegion(dilation);
        //4.用绿线画出这些找到的轮廓
        for (RotatedRect rotatedRect : rects) {
            Point p[] = new Point[4];
            rotatedRect.points(p);
            for (int j = 0; j <= 3; j++) {
                Imgproc.line(src, p[j], p[(j + 1) % 4], new Scalar(0, 255, 0), 2);
            }
        }

        //将矩阵转换为图像
        disposBitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(src, disposBitmap);
        // 释放内存
        src.release();
    }


    private Mat preprocess(Mat src) {
        //1.Sobel算子，x方向求梯度
        Mat sobel = new Mat();
        Imgproc.Sobel(src, sobel, CvType.CV_8U, 1, 0, 3);

        //2.二值化
        Mat binary = new Mat();
        Imgproc.threshold(sobel, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);

        //3.膨胀和腐蚀操作核设定
        Mat element1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(30, 90));
        //控制高度设置可以控制上下行的膨胀程度，例如3比4的区分能力更强,但也会造成漏检
        Mat element2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(24, 4));

        //4.膨胀一次，让轮廓突出
        Mat dilate1 = new Mat();
        Imgproc.dilate(binary, dilate1, element2);

        //5.腐蚀一次，去掉细节，表格线等。这里去掉的是竖直的线
//        Mat erode1 = new Mat();
//        Imgproc.dilate(dilate1, erode1, element1);

        //6.再次膨胀，让轮廓明显一些
        Mat dilate2 = new Mat();
        Imgproc.dilate(dilate1, dilate2, element2);
        return dilate1;
    }

    //https://blog.csdn.net/yangzm/article/details/81105844
    // public static void findContours(Mat image, List<MatOfPoint> contours, Mat hierarchy, int mode, int method, Point offset) {/

    private List<RotatedRect> findTextRegion(Mat src) {
        List<RotatedRect> rects = new ArrayList<>();
        //1.查找轮廓
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        //2.筛选那些面积小的
        for (int i = 0; i < contours.size(); i++) {
            //计算当前轮廓的面积
            double area = Imgproc.contourArea(contours.get(i));
            //面积小于1000的全部筛选掉
            if (area < 1000) {
                continue;
            }

            MatOfPoint matOfPoint = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(matOfPoint.toArray());

            //轮廓近似，作用较小，approxPolyDP函数有待研究
            double epsilon = 0.001 * Imgproc.arcLength(curve, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(curve, approx, epsilon, true);
            //找到最小矩形，该矩形可能有方向
            RotatedRect rect = Imgproc.minAreaRect(curve);
            //计算高和宽
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;

            //筛选那些太细的矩形，留下扁的
            if (m_height > m_width * 1.2) {
                continue;
            }

            //符合条件的rect添加到rects集合中
            rects.add(rect);

        }
        return rects;

    }


    private void dispose4() {
        //阈值化

        //腐蚀

        //降噪

        //文字区域检测

        //文字提取


    }

}