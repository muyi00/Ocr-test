package com.dlh.ocr_test.permissions;

import android.Manifest;
import android.content.Context;


import com.dlh.ocr_test.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 权限组及提示工具
 *
 * @author: YJ
 * @date: 2019/9/3
 */
public class PermissionGroupUtil {

    public static final class Group {
        /***
         * 日历
         */
        public static final String[] CALENDAR = new String[]{
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR};

        /***
         * 相机
         */
        public static final String[] CAMERA = new String[]{Manifest.permission.CAMERA};

        /***
         * 联系人
         */
        public static final String[] CONTACTS = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.GET_ACCOUNTS};

        /***
         * 位置
         */
        public static final String[] LOCATION = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        /***
         * 麦克风
         */
        public static final String[] MICROPHONE = new String[]{Manifest.permission.RECORD_AUDIO};

        /***
         * 电话
         */
        public static final String[] PHONE = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.ADD_VOICEMAIL,
                Manifest.permission.USE_SIP,
                Manifest.permission.PROCESS_OUTGOING_CALLS};

        /***
         * 传感器
         */
        public static final String[] SENSORS = new String[]{Manifest.permission.BODY_SENSORS};

        /**
         * 短信
         */
        public static final String[] SMS = new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_WAP_PUSH,
                Manifest.permission.RECEIVE_MMS};

        /***
         * 存储
         */
        public static final String[] STORAGE = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    /**
     * 将权限转换为文本。
     */
    public static List<String> transformText(Context context, String... permissions) {
        return transformText(context, Arrays.asList(permissions));
    }

    /**
     * 将权限转换为文本。
     */
    public static List<String> transformText(Context context, String[]... groups) {
        List<String> permissionList = new ArrayList<>();
        for (String[] group : groups) {
            permissionList.addAll(Arrays.asList(group));
        }
        return transformText(context, permissionList);
    }

    /**
     * 将权限转换为文本。
     */
    public static List<String> transformText(Context context, List<String> permissions) {
        List<String> textList = new ArrayList<>();
        for (String permission : permissions) {
            switch (permission) {
                case Manifest.permission.READ_CALENDAR:
                case Manifest.permission.WRITE_CALENDAR: {
                    String message = context.getString(R.string.permission_name_calendar);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }

                case Manifest.permission.CAMERA: {
                    String message = context.getString(R.string.permission_name_camera);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                case Manifest.permission.READ_CONTACTS:
                case Manifest.permission.WRITE_CONTACTS:
                case Manifest.permission.GET_ACCOUNTS: {
                    String message = context.getString(R.string.permission_name_contacts);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                case Manifest.permission.ACCESS_FINE_LOCATION:
                case Manifest.permission.ACCESS_COARSE_LOCATION: {
                    String message = context.getString(R.string.permission_name_location);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                case Manifest.permission.RECORD_AUDIO: {
                    String message = context.getString(R.string.permission_name_microphone);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                case Manifest.permission.READ_PHONE_STATE:
                case Manifest.permission.CALL_PHONE:
                case Manifest.permission.READ_CALL_LOG:
                case Manifest.permission.WRITE_CALL_LOG:
                case Manifest.permission.USE_SIP:
                case Manifest.permission.PROCESS_OUTGOING_CALLS: {
                    String message = context.getString(R.string.permission_name_phone);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                case Manifest.permission.BODY_SENSORS: {
                    String message = context.getString(R.string.permission_name_sensors);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                case Manifest.permission.SEND_SMS:
                case Manifest.permission.RECEIVE_SMS:
                case Manifest.permission.READ_SMS:
                case Manifest.permission.RECEIVE_WAP_PUSH:
                case Manifest.permission.RECEIVE_MMS: {
                    String message = context.getString(R.string.permission_name_sms);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                    String message = context.getString(R.string.permission_name_storage);
                    if (!textList.contains(message)) {
                        textList.add(message);
                    }
                    break;
                }
                default:
            }
        }
        return textList;
    }


    /***
     * 登录权限
     */
    public static String[] loginPermissions = new String[]{
            //定位
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //打电话
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            //拍照
            //Manifest.permission.CAMERA,
            //SD卡读写
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //读取联系人
            Manifest.permission.READ_CONTACTS,
    };


    /***
     * 拍照权限
     * @return
     */
    public static String[] cameraPermissions = new String[]{
            //拍照
            Manifest.permission.CAMERA,
            //SD卡读写
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    /***
     * 读取联系人
     */
    public static String[] readContactsPermissions = new String[]{
            //读取联系人
            Manifest.permission.READ_CONTACTS
    };

    /***
     * 打电话
     */
    public static String[] callPhonePermissions = new String[]{
            //打电话
            Manifest.permission.CALL_PHONE
    };
}
