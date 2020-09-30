package com.dlh.ocr_test.permissions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.dlh.ocr_test.R;

import java.util.ArrayList;
import java.util.List;

/***
 * 动态权限工具类
 */
public class PermissionUtil {

    /***
     * 权限申请码
     */
    private final int REQUECT_CODE_PERMISSION = 800;
    /***
     * 跳转程序设置界面的请求码
     */
    public final int REQUECT_CODE_SETTING = 801;

    private Activity activity;
    /***
     * 请求时传入的权限
     */
    private String[] mPermissions;
    /***
     * 未获得的权限
     */
    private List<String> deniedPermissions;
    private RequestPermissionCallback callback;
    public SettingPageUtil settingPageUtil;

    public PermissionUtil(Activity activity) {
        this.activity = activity;
        settingPageUtil = new SettingPageUtil(activity);
    }

    //检测权限
    public void checkPermision(String[] mPermissions, RequestPermissionCallback callback) {
        this.mPermissions = mPermissions;
        this.callback = callback;
        if (mPermissions == null || mPermissions.length == 0) {
            //没有传入权限
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (callback != null) {
                callback.onSuccessful(deniedPermissions);
            }
            return;
        }
        requestPermissions();
    }

    private void requestPermissions() {
        deniedPermissions = findDeniedPermissions(activity, mPermissions);
        if (deniedPermissions != null && deniedPermissions.size() > 0) {
            //申请多个对应的权限
            ActivityCompat.requestPermissions(activity, deniedPermissions.toArray(new String[]{}), REQUECT_CODE_PERMISSION);
        } else {
            //权限允许
            if (callback != null) {
                callback.onSuccessful(deniedPermissions);
            }
        }
    }

    /***
     * 权限请求回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUECT_CODE_PERMISSION) {
            int permission = 0;
            for (int i = 0; i < permissions.length; i++) {
                for (int j = 0; j < mPermissions.length; j++) {
                    if (mPermissions[j].equals(permissions[i])) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permission++;
                        }
                    }
                }
            }
            if (permission == permissions.length) {
                //请求的多个权限成功
                if (callback != null) {
                    callback.onSuccessful(deniedPermissions);
                }
            } else {
                //请求的多个权限失败
                if (callback != null) {
                    callback.onFailure(deniedPermissions);
                }
                List<String> permissionNames = PermissionGroupUtil.transformText(activity, findDeniedPermissions(activity, permissions));
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("权限申请");
                if (isRefused(permissions)) {
                    String message = activity.getString(R.string.message_permission_rationale, TextUtils.join("\n", permissionNames));
                    builder.setMessage(message);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions();
                        }
                    });
                } else {
                    String message = activity.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));
                    builder.setMessage(message);
                    builder.setPositiveButton("设置权限", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toSettingPage();
                        }
                    });
                }
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        }
    }

    /***
     * 跳转到程序设置界面
     */
    public void toSettingPage() {
        settingPageUtil.start(REQUECT_CODE_SETTING);
    }


    /***
     * 未获取的权限中是否存在已经拒绝过的权限
     * @param permissions
     * @return
     */
    private boolean isRefused(String[] permissions) {
        for (String p : permissions) {
            //第一次打开App时	false
            //上次弹出权限点击了禁止（但没有勾选“下次不在询问”）	true
            //上次选择禁止并勾选：下次不在询问	false
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
                return true;
            }
        }
        return false;
    }


    /***
     * 获取没有允许的权限
     * @param mPermissions
     * @return
     */
    private List<String> findDeniedPermissions(Context context, String[] mPermissions) {
        List<String> list = new ArrayList<>();
        for (String str : mPermissions) {
            if (ContextCompat.checkSelfPermission(context, str) != PackageManager.PERMISSION_GRANTED) {
                list.add(str);
            }
        }
        return list;
    }


    public interface RequestPermissionCallback {
        /**
         * 权限请求成功
         *
         * @param permissions
         */
        void onSuccessful(List<String> permissions);

        /**
         * 权限请求失败
         *
         * @param permissions
         */
        void onFailure(List<String> permissions);
    }


}
