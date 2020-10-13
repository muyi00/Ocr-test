package com.dlh.ocr_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;


public class ScanActivity extends AppCompatActivity {

    private ScannerView scannerView;
    private Vibrator vibrator;
    private TextView tvResult;
    private Button start_btn;
    private TessTwoScanner tessTwoScanner;

    private ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
    private CommonAdapter<ImageInfo> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        scannerView = findViewById(R.id.sv);
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

}