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
import com.dlh.ocr_test.adapter.CommonAdapter;
import com.dlh.ocr_test.adapter.ViewHolder;
import com.dlh.ocr_test.permissions.PermissionGroupUtil;
import com.dlh.ocr_test.permissions.PermissionUtil;
import com.dlh.ocr_test.utils.AsyncTaskUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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

/**
 * 步骤：
 * <p>
 * 阈值化
 * 腐蚀
 * 降噪
 * 文字区域检测
 * 文字提取
 * <p>
 * https://blog.csdn.net/qq_20158897/article/details/100558458?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param
 */
public class Processing3Activity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 100;
    private String imagePath;
    private PermissionUtil permissionUtil;
    private ArrayList<ImageInfo> imageInfos = new ArrayList<>();
    private CommonAdapter<ImageInfo> adapter;
    private ImageView imageView0;
    private CheckBox left_rotation_cb;
    private Mat original;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing3);

        permissionUtil = new PermissionUtil(this);

        imageView0 = findViewById(R.id.imageView0);
        left_rotation_cb = findViewById(R.id.left_rotation_cb);


        adapter = new CommonAdapter<ImageInfo>(this, imageInfos, R.layout.item_bitmap_info) {
            @Override
            public void convert(ViewHolder helper, int position, ImageInfo item) {
                helper.setText(R.id.title, item.title);
                ImageView image = helper.getView(R.id.image);
                Glide.with(Processing3Activity.this)
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
                Glide.with(Processing3Activity.this)
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
    public void choosePictureBtn2(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.Group.STORAGE, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                Matisse.from(Processing3Activity.this)
                        //选择图片
                        .choose(MimeType.ofImage())
                        //是否只显示选择的类型的缩略图，就不会把所有图片视频都放在一起，而是需要什么展示什么
                        .showSingleMediaType(true)
                        //这两行要连用 是否在选择图片中展示照相 和适配安卓7.0 FileProvider
                        .capture(true)
                        .captureStrategy(new CaptureStrategy(true, getFileProvider(Processing3Activity.this)))
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


    public void disposeBtn2(View view) {
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

        original = new Mat();
        src.copyTo(original);

        //灰度图，单通道
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        addShowBitmap("灰度图，单通道", src);
        //阈值化（二值化）
        Imgproc.threshold(src, src, 100, 255, Imgproc.THRESH_BINARY);
        //腐蚀
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11, 11));
        Imgproc.erode(src, src, erodeKernel);
        addShowBitmap("阈值化，并腐蚀", src);
        //中值滤波
        Imgproc.medianBlur(src, src, 7);
        //addShowBitmap("中值滤波，降噪", src);


        Mat lableImg = new Mat();
        Mat showMat = new Mat(src.size(), CvType.CV_8UC3);
        src.convertTo(lableImg, CvType.CV_32SC1);

        int rows = lableImg.rows();
        int cols = lableImg.cols();

        double lable = 0;
        List<List<Point>> texts = new LinkedList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // 获取种子
                double[] data = lableImg.get(r, c);
                if (data == null || data.length < 1) {
                    continue;
                }
                if (data[0] == 255) {
                    // 背景
                    showMat.put(r, c, 255, 255, 255);
                    continue;
                }
                if (data[0] != 0) {
                    // 已经标记过了
                    continue;
                }

                // 新的填充开始
                lable++;
                double[] color = {Math.random() * 255, Math.random() * 255, Math.random() * 255};

                // 开始种子填充
                LinkedList<Point> neighborPixels = new LinkedList<>();
                neighborPixels.push(new Point(r, c));

                List<Point> textPoint = new LinkedList<>();

                while (!neighborPixels.isEmpty()) {
                    Point curPx = neighborPixels.pop();
                    int row = (int) curPx.x;
                    int col = (int) curPx.y;
                    textPoint.add(new Point(col, row));
                    lableImg.put(row, col, lable);
                    showMat.put(row, col, color);

                    // 左边
                    double[] left = lableImg.get(row, col - 1);
                    if (left != null && left.length > 0 && left[0] == 0) {
                        neighborPixels.push(new Point(row, col - 1));
                    }

                    // 右边
                    double[] right = lableImg.get(row, col + 1);
                    if (right != null && right.length > 0 && right[0] == 0) {
                        neighborPixels.push(new Point(row, col + 1));
                    }

                    // 上边
                    double[] top = lableImg.get(row - 1, col);
                    if (top != null && top.length > 0 && top[0] == 0) {
                        neighborPixels.push(new Point(row - 1, col));
                    }

                    // 下边
                    double[] bottom = lableImg.get(row + 1, col);
                    if (bottom != null && bottom.length > 0 && bottom[0] == 0) {
                        neighborPixels.push(new Point(row + 1, col));
                    }
                }
                texts.add(textPoint);
            }
        }

        addShowBitmap("联通区域检测", showMat);


        List<Mat> textMats = new LinkedList<>();
        for (List<Point> data : texts) {
            MatOfPoint mat = new MatOfPoint();
            mat.fromList(data);
            Rect rect = Imgproc.boundingRect(mat);
            Imgproc.rectangle(showMat, rect.tl(), rect.br(), new Scalar(255, 0, 0, 255));
            textMats.add(src.submat(rect));
        }
        addShowBitmaps("内容截取", textMats);

        showMat.release();
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


    /**
     * 阈值化，并腐蚀
     *
     * @param src
     */
    private Mat erode(Mat src) {
        src = ProcessingImageUtils.gray(src);
        // 阈值化
        //Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        Imgproc.threshold(src, src, 100, 255, Imgproc.THRESH_BINARY);

//        Imgproc.adaptiveThreshold(opMat,opMat,255.0,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,3,0.0);

        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(13, 13));
        Imgproc.erode(src, src, erodeKernel);
        return src;
    }

    /**
     * 采用中值滤波进行降噪
     *
     * @param src
     */
    private Mat medianBlur(Mat src) {
        Imgproc.medianBlur(src, src, 7);
        return src;
    }


    /**
     * 种子填充法进行联通区域检测
     */
    private Mat seedFill(Mat binImg, Mat src) {
        Mat lableImg = new Mat();
        Mat showMat = new Mat(binImg.size(), CvType.CV_8UC3);
        binImg.convertTo(lableImg, CvType.CV_32SC1);

        int rows = lableImg.rows();
        int cols = lableImg.cols();

        double lable = 0;
        List<List<Point>> texts = new LinkedList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // 获取种子
                double[] data = lableImg.get(r, c);
                if (data == null || data.length < 1) {
                    continue;
                }
                if (data[0] == 255) {
                    // 背景
                    showMat.put(r, c, 255, 255, 255);
                    continue;
                }
                if (data[0] != 0) {
                    // 已经标记过了
                    continue;
                }

                // 新的填充开始
                lable++;
                double[] color = {Math.random() * 255, Math.random() * 255, Math.random() * 255};

                // 开始种子填充
                LinkedList<Point> neighborPixels = new LinkedList<>();
                neighborPixels.push(new Point(r, c));

                List<Point> textPoint = new LinkedList<>();

                while (!neighborPixels.isEmpty()) {
                    Point curPx = neighborPixels.pop();
                    int row = (int) curPx.x;
                    int col = (int) curPx.y;
                    textPoint.add(new Point(col, row));
                    lableImg.put(row, col, lable);
                    showMat.put(row, col, color);

                    // 左边
                    double[] left = lableImg.get(row, col - 1);
                    if (left != null && left.length > 0 && left[0] == 0) {
                        neighborPixels.push(new Point(row, col - 1));
                    }

                    // 右边
                    double[] right = lableImg.get(row, col + 1);
                    if (right != null && right.length > 0 && right[0] == 0) {
                        neighborPixels.push(new Point(row, col + 1));
                    }

                    // 上边
                    double[] top = lableImg.get(row - 1, col);
                    if (top != null && top.length > 0 && top[0] == 0) {
                        neighborPixels.push(new Point(row - 1, col));
                    }

                    // 下边
                    double[] bottom = lableImg.get(row + 1, col);
                    if (bottom != null && bottom.length > 0 && bottom[0] == 0) {
                        neighborPixels.push(new Point(row + 1, col));
                    }
                }
                texts.add(textPoint);
            }
        }

        List<Mat> textMats = new LinkedList<>();
        for (List<Point> data : texts) {
            MatOfPoint mat = new MatOfPoint();
            mat.fromList(data);
            Rect rect = Imgproc.boundingRect(mat);
            Imgproc.rectangle(showMat, rect.tl(), rect.br(), new Scalar(255, 0, 0, 255));
            textMats.add(src.submat(rect));
        }

        addShowBitmaps("内容截取", textMats);

        return showMat;
    }
}