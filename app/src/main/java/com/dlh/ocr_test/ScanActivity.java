package com.dlh.ocr_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dlh.lib.Callback;
import com.dlh.lib.ImageInfo;
import com.dlh.lib.Result;
import com.dlh.lib.ScannerView;
import com.dlh.ocr_test.adapter.CommonAdapter;
import com.dlh.ocr_test.adapter.ViewHolder;
import com.dlh.ocr_test.parser.TessTwoScanner;
import com.dlh.ocr_test.parser.Utils;
import com.dlh.ocr_test.utils.AsyncTaskUtil;

import java.util.ArrayList;


public class ScanActivity extends AppCompatActivity {

    private ScannerView scannerView;
    private Vibrator vibrator;
    private TextView tvResult;
    private Button start_btn, save_btn;
    private TessTwoScanner tessTwoScanner;
    private CheckBox gaussianBlur_cb, medianBlur_cb, blur_cb;

    private ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
    private CommonAdapter<ImageInfo> adapter;
    private String basePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        scannerView = findViewById(R.id.sv);
        basePath = getFilesDir().getAbsolutePath();
        gaussianBlur_cb = findViewById(R.id.gaussianBlur_cb);
        medianBlur_cb = findViewById(R.id.medianBlur_cb);
        blur_cb = findViewById(R.id.blur_cb);
        gaussianBlur_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tessTwoScanner.isEnableGaussianBlur = isChecked;
            }
        });
        medianBlur_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tessTwoScanner.isEnableMedianBlur = isChecked;
            }
        });

        blur_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tessTwoScanner.isEnableBlur = isChecked;
            }
        });

        tvResult = findViewById(R.id.tv_result);
        start_btn = findViewById(R.id.start_btn);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_btn.getText().toString().equals("停止")) {
                    scannerView.onPause();
                    start_btn.setText("开始");
                } else {
                    scannerView.onResume();
                    start_btn.setText("停止");
                }
            }
        });

        save_btn = findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        tessTwoScanner = new TessTwoScanner(ScanActivity.this);

        scannerView.setShouldAdjustFocusArea(true);
        scannerView.setViewFinder(new ViewFinder(this));
        scannerView.setSaveBmp(false);
        scannerView.setScanner(tessTwoScanner);
        scannerView.setRotateDegree90Recognition(true);
        scannerView.setCallback(new Callback() {
            @Override
            public void result(Result result) {
                if (result != null) {
                    imageInfos.clear();
                    imageInfos.addAll(result.imageInfos);
                    adapter.notifyDataSetChanged();
                    if (!TextUtils.isEmpty(result.data)) {
                        tvResult.setText("识别结果：\n" + result.toString());
                    }
                    startVibrator();
                }
                scannerView.restartPreviewAfterDelay(2000);
            }
        });

        adapter = new CommonAdapter<ImageInfo>(this, imageInfos, R.layout.item_bitmap_info) {
            @Override
            public void convert(ViewHolder helper, int position, ImageInfo item) {
                helper.setText(R.id.title, item.title);
                ImageView image = helper.getView(R.id.image);
                Glide.with(ScanActivity.this)
                        .load(item.bitmap)
                        .into(image);
            }
        };
        ListView bitmap_lv = findViewById(R.id.bitmap_lv);
        bitmap_lv.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        scannerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        super.onDestroy();
    }

    private void startVibrator() {
        if (vibrator == null)
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }

    private void saveImage() {
        new AsyncTaskUtil(this, new AsyncTaskUtil.AsyncCallBack() {
            @Override
            public int asyncProcess() throws InterruptedException {
                ImageInfo info = imageInfos.get(imageInfos.size() - 1);
                String namePath = String.format("%s/%s.png", basePath, System.currentTimeMillis());
                Utils.saveBitmap(info.bitmap, namePath);
                return 0;
            }

            @Override
            public void postUI(int rsult) {
                adapter.notifyDataSetChanged();
            }
        }).setMaskContent("正在保存图片").executeTask();
    }
}