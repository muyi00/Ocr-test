package com.dlh.ocr_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dlh.ocr_test.baidu.BaiduOcrActivity;
import com.dlh.ocr_test.permissions.PermissionGroupUtil;
import com.dlh.ocr_test.permissions.PermissionUtil;
import com.dlh.ocr_test.utils.AsyncTaskUtil;
import com.dlh.ocr_test.utils.CopyFileFromAssets;

import org.opencv.android.OpenCVLoader;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private PermissionUtil permissionUtil;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        permissionUtil = new PermissionUtil(this);
        iniLoadOpenCV();
        permissionUtil.checkPermision(PermissionGroupUtil.cameraPermissions, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                loadTessTwoFileData();
            }

            @Override
            public void onFailure(List<String> permissions) {

            }
        });
    }

    private void iniLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.i(TAG, "Opencv 已加载");
        } else {
            Toast.makeText(this, "Opencv 未加载", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTessTwoFileData() {
        //tessdata
        new AsyncTaskUtil(this, new AsyncTaskUtil.AsyncCallBack() {
            @Override
            public int asyncProcess() throws InterruptedException {
                CopyFileFromAssets.copyTessTwoData(mContext);
                return 0;
            }

            @Override
            public void postUI(int rsult) {
            }
        }).setMaskContent("正在初始化").executeTask();
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


    public void tesseractOcrBtn(View view) {
        startActivity(new Intent(this, TesseractActivity.class));
    }


    public void processingImageBtn(View view) {
        //图片处理
        startActivity(new Intent(this, ProcessingImageActivity.class));
    }

    public void processingImageBtn2(View view) {
        //图片处理
        startActivity(new Intent(this, ProcessingActivity.class));
    }

    public void processingImageBtn3(View view) {
        //图片处理
        startActivity(new Intent(this, Processing3Activity.class));
    }

}