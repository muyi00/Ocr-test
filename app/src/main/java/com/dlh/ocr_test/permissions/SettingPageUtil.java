package com.dlh.ocr_test.permissions;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * SettingPageUtil
 *
 * @author: YJ
 * @date: 2019/9/3
 */
public class SettingPageUtil {
    private static final String MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String MARK = Build.MANUFACTURER.toLowerCase();

    private Activity baseActivity;

    public SettingPageUtil(Activity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public void start() {
        start(-1);
    }

    /**
     * Start.
     *
     * @param requestCode this code will be returned in onActivityResult() when the activity exits.
     * @return true if successful, otherwise is false.
     */
    public void start(int requestCode) {
        Intent intent;
        if (MARK.contains("huawei")) {
            intent = huaweiApi(baseActivity);
        } else if (MARK.contains("xiaomi")) {
            intent = xiaomiApi(baseActivity);
        } else if (MARK.contains("oppo")) {
            intent = oppoApi(baseActivity);
        } else if (MARK.contains("vivo")) {
            intent = vivoApi(baseActivity);
        } else if (MARK.contains("meizu")) {
            intent = meizuApi(baseActivity);
        } else {
            intent = defaultApi(baseActivity);
        }
        try {
            baseActivity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            intent = defaultApi(baseActivity);
            baseActivity.startActivityForResult(intent, requestCode);
        }
    }

    private static Intent defaultApi(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    private static Intent huaweiApi(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return defaultApi(context);
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
        return intent;
    }

    private static Intent xiaomiApi(Context context) {
        String version = getSystemProperty(MIUI_VERSION_NAME);
        if (TextUtils.isEmpty(version) || version.contains("7") || version.contains("8")) {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.putExtra("extra_pkgname", context.getPackageName());
            return intent;
        }
        return defaultApi(context);
    }

    private static Intent vivoApi(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packagename", context.getPackageName());
        intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"));
        if (hasActivity(context, intent)) {
            return intent;
        }
        intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"));
        return intent;
    }

    private static Intent oppoApi(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packageName", context.getPackageName());
        intent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity"));
        return intent;
    }

    private static Intent meizuApi(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return defaultApi(context);
        }
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.putExtra("packageName", context.getPackageName());
        intent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity"));
        return intent;
    }

    private static boolean hasActivity(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    public static String getSystemProperty(String propName) {
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            return input.readLine();
        } catch (IOException ex) {
            return "";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }
    }


}
