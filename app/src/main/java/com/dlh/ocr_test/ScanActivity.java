package com.dlh.ocr_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dlh.lib.Callback;
import com.dlh.lib.Result;
import com.dlh.lib.ScannerView;
import com.dlh.ocr_test.parser.TessTwoScanner;

public class ScanActivity extends AppCompatActivity {

    private ScannerView scannerView;
    private Vibrator vibrator;
    private TextView tvResult;
    private ImageView image, image1;
    private CheckBox processing_cb;
    private TessTwoScanner tessTwoScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        scannerView = findViewById(R.id.sv);
        tvResult = findViewById(R.id.tv_result);
        image1 = findViewById(R.id.image1);
        image = findViewById(R.id.image);
        processing_cb = findViewById(R.id.processing_cb);

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
                    Glide.with(ScanActivity.this)
                            .load(result.bitmap)
                            .into(image);
                    if (!TextUtils.isEmpty(result.data)) {
                        tvResult.setText("识别结果：\n" + result.toString());
                    }
                    startVibrator();
                }
                scannerView.restartPreviewAfterDelay(2000);
            }
        });

        processing_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tessTwoScanner.setProcessing(isChecked);
            }
        });
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