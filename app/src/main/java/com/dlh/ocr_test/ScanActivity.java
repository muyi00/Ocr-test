package com.dlh.ocr_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
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
    private ImageView image;
    private CheckBox binarization_cb;
    private TessTwoScanner tessTwoScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        scannerView = findViewById(R.id.sv);
        tvResult = findViewById(R.id.tv_result);
        image = findViewById(R.id.image);
        binarization_cb = findViewById(R.id.binarization_cb);

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
                    tvResult.setText("识别结果：\n" + result.toString());
                    Glide.with(ScanActivity.this)
                            .load(result.bitmap)
                            .into(image);
                    startVibrator();
                }
                scannerView.restartPreviewAfterDelay(2000);
            }
        });

        binarization_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tessTwoScanner.setBinarization(isChecked);
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