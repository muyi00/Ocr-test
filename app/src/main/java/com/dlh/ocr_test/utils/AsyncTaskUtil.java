package com.dlh.ocr_test.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * desc   : 异步任务工具
 * author : YJ
 * time   : 2020/9/7 19:27
 */
public class AsyncTaskUtil extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "PrinterAsyncTask";

    private final AsyncCallBack asyncCallBack;
    private LoadingDialog loadingDialog;
    private String maskContent = null;
    private Context context;
    private boolean mCancelable;
    private DialogInterface.OnDismissListener listener;


    public AsyncTaskUtil(Context context, boolean mCancelable, AsyncCallBack back) {
        super();
        this.context = context;
        this.mCancelable = mCancelable;
        this.asyncCallBack = back;
    }

    public AsyncTaskUtil(Context context, AsyncCallBack back) {
        this(context, true, back);
    }

    public AsyncTaskUtil(AsyncCallBack back) {
        this(null, back);
    }

    @Override
    protected Integer doInBackground(String... params) {
        if (asyncCallBack != null) {
            int tt = 0;
            try {
                tt = asyncCallBack.asyncProcess();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return tt;
        }
        return 0;
    }

    @Override
    public void onPreExecute() {
        if (maskContent != null) {
            loadingDialog = new LoadingDialog(context).show(maskContent);
            loadingDialog.setCancelable(mCancelable);
            loadingDialog.setOnDismissListener(listener);
        }
    }

    @Override
    protected void onPostExecute(Integer Result) {
        if (asyncCallBack != null) {
            asyncCallBack.postUI(Result);
        }

        try {
            if ((loadingDialog != null) && this.loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
        } catch (final Exception e) {
            // Handle or log or ignore
            Log.e(TAG, "关闭弹出框异常");
        } finally {
            loadingDialog = null;
        }
    }

    public AsyncTaskUtil setMaskContent(String maskContent) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            if (!TextUtils.isEmpty(maskContent)) {
                loadingDialog.setContent(maskContent);
            }
        } else {
            this.maskContent = maskContent;
        }
        return this;
    }

    public AsyncTaskUtil executeTask() {
        this.execute();
        return this;
    }

    public interface AsyncCallBack {
        int asyncProcess() throws InterruptedException;

        void postUI(int rsult);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        if (listener != null && loadingDialog != null) {
            loadingDialog.setOnDismissListener(listener);
        }
    }
}