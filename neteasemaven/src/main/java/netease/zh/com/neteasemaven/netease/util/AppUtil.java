package netease.zh.com.neteasemaven.netease.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class AppUtil {

    private static Context context;

    private AppUtil(Context context) {
        this.context = context;
    }

    private static AppUtil appUtil;

    public static synchronized AppUtil getInstance(Context context) {
        if (appUtil == null) appUtil = new AppUtil(context);
        return appUtil;
    }

    /**
     * 获取App包 信息版本号
     *
     * @param context
     * @return
     */
    private PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    /**
     * 获取目前软件版本code
     *
     * @return
     */
    public String getAppVersionCode() {
        String versionCode = "";
        try {
            PackageInfo pi = getPackageInfo(context);
            versionCode = pi.versionCode + "";
        } catch (Exception e) {
        }
        return versionCode;
    }

    /**
     * 获取目前软件版本name
     *
     * @return
     */
    public String getAppVersionName() {
        String versionName = "";
        try {
            PackageInfo pi = getPackageInfo(context);
            versionName = pi.versionName + "";
        } catch (Exception e) {
        }
        return versionName;
    }

    /**
     * 从AndroidManifest中获取channel值
     */
    public String getAppChannel() {
        int channel = 0;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(context.getPackageName(), pm.GET_META_DATA);
            channel = info.metaData.getInt("CHANNEL");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return channel + "";
    }

    /**
     * 从AndroidManifest中获取crashVersion
     */
    public String getCrashVersion() {
        String crashVersion = "get version error";
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(context.getPackageName(), pm.GET_META_DATA);
            crashVersion = info.metaData.getString("CRASH");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return crashVersion;
    }

    /**
     * 获取IMEI
     *
     * @return
     */
    public String getIMEI() {
        String imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();
        return imei;
    }

    /**
     * 显示软键盘
     *
     * @param context
     * @param view
     */
    public static void showSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 隐藏软键盘
     *
     * @param context
     * @param view
     */
    public static void hideSoftKeyBoard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromInputMethod(view.getWindowToken(), 0);
    }
}
