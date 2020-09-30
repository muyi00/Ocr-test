package com.dlh.ocr_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dlh.ocr_test.baidu.BaiduOcrActivity;
import com.dlh.ocr_test.permissions.PermissionGroupUtil;
import com.dlh.ocr_test.permissions.PermissionUtil;

import org.opencv.android.OpenCVLoader;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private PermissionUtil permissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionUtil = new PermissionUtil(this);
        iniLoadOpenCV();
    }

    private void iniLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.i(TAG, "Opencv 已加载");
        } else {
            Toast.makeText(this, "Opencv 未加载", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void baiduOcrBtn(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.cameraPermissions, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                startActivity(new Intent(MainActivity.this, BaiduOcrActivity.class));
            }

            @Override
            public void onFailure(List<String> permissions) {

            }
        });
    }


    public void ocrBtn(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.cameraPermissions, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
            }

            @Override
            public void onFailure(List<String> permissions) {

            }
        });
    }


    public void processingImageBtn(View view) {
        //图片处理
        startActivity(new Intent(this, ProcessingImageActivity.class));
    }


    public void tesseractOcrBtn(View view) {
        startActivity(new Intent(this, TesseractActivity.class));
    }
}