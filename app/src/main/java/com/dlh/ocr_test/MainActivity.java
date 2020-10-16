package com.dlh.ocr_test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
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
    private boolean hasGotToken = false;
    private AlertDialog.Builder alertDialog;

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
        alertDialog = new AlertDialog.Builder(this);
        // 请选择您的初始化方式
        // initAccessToken();
        initAccessTokenWithAkSk();
    }

    /**
     * 用明文ak，sk初始化
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(),  "44GGLvjYjEpeBqMdE7UTzaXk", "rsm9t0DZohTBmonWdwk44dw9vUt6UWfc");
    }

    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
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

    public void ocrLocalBtn(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.cameraPermissions, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                startActivity(new Intent(MainActivity.this, OcrLocalActivity.class));
            }

            @Override
            public void onFailure(List<String> permissions) {

            }
        });
    }

    public void ocrLocalBtn2(View view) {
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

    public void ocrBaiduBtn(View view) {
        permissionUtil.checkPermision(PermissionGroupUtil.cameraPermissions, new PermissionUtil.RequestPermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                startActivity(new Intent(MainActivity.this, OcrBaiduActivity.class));
            }

            @Override
            public void onFailure(List<String> permissions) {

            }
        });
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