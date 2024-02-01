package com.example.qrcodecoursework;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
public class PhoneInfoHelper {
    public static String getAndroidId(Context context) {
        // Получаем Android ID
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return androidId;
    }
}
