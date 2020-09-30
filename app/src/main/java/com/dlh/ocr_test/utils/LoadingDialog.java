package com.dlh.ocr_test.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.dlh.ocr_test.R;


/**
 * desc   : 无限加载进度框
 * author : YJ
 * time   : 2020/9/7 19:12
 */
public class LoadingDialog extends Dialog {
    private TextView tv;

    public LoadingDialog(Context context) {
        super(context, R.style.cbb_boxDialog);
        this.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.cbb_loading_dialog);
        this.tv = (TextView) findViewById(R.id.dialogContent);
    }

    public LoadingDialog show(String content) {
        super.show();
        tv.setText(content);
        return this;
    }

    public void setContent(String content) {
        tv.setText(content);
    }
}
