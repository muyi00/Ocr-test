package com.dlh.ocr_test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.dlh.ocr_test.utils.TessTwoUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC3;

public class ProcessingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 100;
    private String imagePath;
    private PermissionUtil permissionUtil;
    private ArrayList<ImageInfo> imageInfos = new ArrayList<>();
    private CommonAdapter<ImageInfo> adapter;
    private ImageView imageView0;
    private CheckBox left_rotation_cb;
    private TessTwoUtils tessTwoUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        tessTwoUtils = new TessTwoUtils(this);
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
                dispose3();
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

        Mat mSource = src.clone();
        Mat mSource2 = src.clone();
        Mat mSource3 = src.clone();
        Mat source = new Mat();

//        Imgproc.cvtColor(src, source, Imgproc.COLOR_BGR2RGB);
//        Imgproc.bilateralFilter(source, source2, 5, 200, 200.0);
//        addShowBitmap("双边滤波", source2);

//        Imgproc.boxFilter(src, source, CvType.CV_8U, new Size(9.0, 9.0), new Point(-1.0, -1.0), true);
        //Imgproc.boxFilter(src, resultNormalize, -1, new Size(3.0, 3.0), new Point(-1.0, -1.0), false);
//        addShowBitmap("方框滤波", source2);
//
        // Imgproc.sqrBoxFilter(src, source, CvType.CV_8U, new Size(3.0, 3.0), new Point(-1.0, -1.0), true);
        Imgproc.sqrBoxFilter(src, source, src.depth(), new Size(1, 1));
        addShowBitmap("方框滤波", source);


//        if (left_rotation_cb.isChecked()) {
//            src = ProcessingImageUtils.rotateLeft(src);
//        }

//
//        Core.multiply(src, new Scalar(2, 2, 2), src);
        //1.转化成灰度图
        Imgproc.cvtColor(source, src, Imgproc.COLOR_RGB2GRAY);
//        addShowBitmap("灰度", src);
//        Imgproc.GaussianBlur(src, src, new Size(5, 5), 5);
//        //addShowBitmap("高斯", src);
//        //Imgproc.medianBlur(src, src, 15);
//        Imgproc.blur(src, src, new Size(9, 9));
//        //src = ProcessingImageUtils.threshold(src);
//        Mat dst = new Mat();
//        Imgproc.cvtColor(src, dst, Imgproc.COLOR_GRAY2BGR);
//        Imgproc.bilateralFilter(dst, src, 25, 25 * 2, 25 / 2);
//
        addShowBitmap("二值化前", src);
        Imgproc.threshold(src, src, 100, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        addShowBitmap("二值化", src);
//        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
//        Imgproc.dilate(src, src, dilateKernel);
//        addShowBitmap("膨胀", src);
//
//
        Mat erodeKernel = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT,
                new Size(15, 15));
        Imgproc.erode(src, src, erodeKernel);
        addShowBitmap("腐蚀", src);

        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT,
                new Size(9, 7));
        Imgproc.morphologyEx(src, src, Imgproc.MORPH_OPEN, kernel);
        addShowBitmap("开操作", src);

        Mat kernel2 = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT,
                new Size(3, 3));
        Imgproc.morphologyEx(src, src, Imgproc.MORPH_HITMISS, kernel2);
        addShowBitmap("击中击不中", src);


//
//
////
////        Mat element2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(60, 60));
////        Mat dilate1 = new Mat();
////        Imgproc.dilate(src, dilate1, element2);
////        addShowBitmap("sobel处理-第一次膨胀", dilate1);


        //3.查找和筛选文字区域
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
            if (rect.height >= mSource.height()) {
                //矩形区域太大
                continue;
            }
            if (rect.height < mSource.height() / 4) {
                //矩形区域太小
                continue;
            }

            //矩形
            Imgproc.rectangle(mSource2, rect, new Scalar(255.0, 255.0, 0.0), 4, Imgproc.LINE_8);
            Mat temp = mSource.submat(rect);
            validMats.add(temp);
            addShowBitmap("子区域", temp);
        }

        // 释放内存
        src.release();

        Mat erodeKernel2 = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT,
                new Size(9, 9));

        //子图处理
        for (int i = 0; i < validMats.size(); i++) {
            //for (Mat mat : validMats) {
            Mat subSrc = validMats.get(i).clone();
            Imgproc.cvtColor(subSrc, subSrc, Imgproc.COLOR_RGB2GRAY);
            Imgproc.GaussianBlur(subSrc, subSrc, new Size(3, 3), 5);
            Imgproc.medianBlur(subSrc, subSrc, 15);
            //二值化
            Imgproc.threshold(subSrc, subSrc, 100, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);


            //  Imgproc.erode(subSrc, subSrc, erodeKernel2);
//            Imgproc.morphologyEx(subSrc, subSrc, Imgproc.MORPH_OPEN, kernel);
//            Imgproc.morphologyEx(subSrc, subSrc, Imgproc.MORPH_HITMISS, kernel2);

            Bitmap bitmapTemp = Bitmap.createBitmap(subSrc.cols(), subSrc.rows(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(subSrc, bitmapTemp);
            String str = tessTwoUtils.orcDiscern(bitmapTemp);
            //addShowBitmap(str, bitmapTemp);
            if (i == 3) {
                break;
            }
        }
        addShowBitmap("边界矩形", mSource2);
    }

    private void dispose2() {

        //多边形大小
        Mat mat = Mat.zeros(600, 600, CvType.CV_8UC3);
        //填充颜色
        mat.setTo(new Scalar(255, 255, 255));

        //定点
        MatOfPoint matOfPoint = new MatOfPoint();
        List<Point> pointList = new ArrayList<>();
        pointList.add(new Point(50, 10));
        pointList.add(new Point(300, 12));
        pointList.add(new Point(350, 250));
        pointList.add(new Point(9, 250));
        matOfPoint.fromList(pointList);

        Imgproc.fillConvexPoly(mat, matOfPoint, new Scalar(0, 0, 0));
        addShowBitmap("多边形区域", mat);
    }

    private void dispose3() {
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            return;
        }

        Mat source = src.clone();

        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2RGB);
        Mat source2 = src.clone();
        Imgproc.bilateralFilter(src, source2, 5, 200, 200.0);
        addShowBitmap("双边滤波", source2);


    }

    private void addShowBitmaps(String title, List<Mat> srcList) {
        for (Mat src : srcList) {
            addShowBitmap(title, src);
        }
    }

    private void addShowBitmap(String title, Mat src) {
        Bitmap bitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.RGB_565);
        org.opencv.android.Utils.matToBitmap(src, bitmap);
        addShowBitmap(title, bitmap);
    }

    private void addShowBitmap(String title, Bitmap bitmap) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.title = title;
        imageInfo.bitmap = bitmap;
        imageInfos.add(0, imageInfo);
    }

}