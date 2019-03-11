package com.musichero.xmusic.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.ResultReceiver;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * some methods for setting
 *
 * @author DoBao
 */
public class ApplicationUtils {

    /**
     * Md5 input string
     *
     * @return md5 string
     */
    public static int getColor(Context mContext, int attId) {
        try {
            TypedValue typedValue = new TypedValue();
            if (mContext.getTheme().resolveAttribute(attId, typedValue, true)) {
                return typedValue.data;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getMd5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;

            }
            return md5;
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getMessage());
            return null;
        }
    }

    /**
     * check connection internet
     *
     * @param mContext
     * @return true if connecting
     */
    public static boolean isOnline(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static boolean hasSDcard() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getDeviceId(Context mContext) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String mDeviceID = mTelephonyMgr.getDeviceId();
        if (mDeviceID == null || mDeviceID.equals("0")) {
            mDeviceID = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
        }
        return mDeviceID;
    }


    public static String getNameApp(Context mContext) {
        final PackageManager pm = mContext.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(mContext.getPackageName(), 0);
        }
        catch (final NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        return applicationName;
    }

    public static int getVersionCode(Context mContext) {
        PackageInfo pinfo;
        try {
            pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            return versionNumber;
        }
        catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getSignature(Context mContext) {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo appInfo = manager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            return appInfo.signatures[0].toString();

        }
        catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void hiddenVirtualKeyboard(Context mContext, View myEditText) {
        try {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showVirtualKeyboad(Context mContext, EditText myEditText) {
        try {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(myEditText, InputMethodManager.SHOW_IMPLICIT);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getHashKey(Context mContext) {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        }
        catch (NameNotFoundException e) {
            e.printStackTrace();

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVersionName(Context mContext) {
        PackageInfo pinfo;
        try {
            pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String versionName = pinfo.versionName;
            return versionName;
        }
        catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getAndroidVersion() {
        try {
            int e = Build.VERSION.SDK_INT;
            if (e == 14) {
                return "android 4.0";
            }

            if (e == 15) {
                return "android 4.0.3";
            }

            if (e == 16) {
                return "android 4.1.2";
            }

            if (e == 17) {
                return "android 4.2.2";
            }

            if (e == 18) {
                return "android 4.3.1";
            }

            if (e == 19) {
                return "android 4.4.2";
            }

            if (e == 21) {
                return "android 5.0";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "Unknown";
    }

    public static String getCarierName(Context mContext) {
        String carrierName = null;

        try {
            TelephonyManager e = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            carrierName = e.getNetworkOperatorName();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return carrierName;
    }

    public static NetworkInfo getConnectionType(Context mContext) {
        try {
            ConnectivityManager e = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            return e.getActiveNetworkInfo();
        }
        catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static String getDeviceName() {
        try {
            String e = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (!StringUtils.isEmpty(e)) {
                if (!StringUtils.isEmpty(model)) {
                    return e + "-" + model;
                }

                return e;
            }

            if (!StringUtils.isEmpty(model)) {
                return model;
            }
        }
        catch (Exception var2) {
            var2.printStackTrace();
        }

        return "Unknown";
    }

    public static String colorToHexString(int color) {
        return String.format("#%08X", 0xFFFFFFFF & color);
    }

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);
        return createChecksum(fis);
    }

    public static byte[] createChecksum(InputStream fis) throws Exception {
        MessageDigest complete = MessageDigest.getInstance("MD5");

        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = fis.read(buf)) > 0) {
            complete.update(buf, 0, len);
        }
        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        return getMD5Checksum(new FileInputStream(filename));
    }

    public static String getMD5Checksum(InputStream fis) throws Exception {
        byte[] b = createChecksum(fis);
        String result = "";
        int size = b.length;
        for (int i = 0; i < size; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static void showSoftInputUnchecked(Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            Method showSoftInputUnchecked = null;
            try {
                showSoftInputUnchecked = imm.getClass().getMethod("showSoftInputUnchecked", int.class, ResultReceiver.class);
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (showSoftInputUnchecked != null) {
                try {
                    showSoftInputUnchecked.invoke(imm, 0, null);
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
