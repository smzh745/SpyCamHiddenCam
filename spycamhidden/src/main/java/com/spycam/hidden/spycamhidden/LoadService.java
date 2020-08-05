package com.spycam.hidden.spycamhidden;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class LoadService {


    public static void loadServiceData(Context context, String uid, String url, String macAddress, long milliSeconds) {
        Intent intent = new Intent(context, BackgroundVideoRecorder.class);
        intent.putExtra("Front_Request", true);
        intent.putExtra("uid", uid);
        intent.putExtra("url", url);
        intent.putExtra("macAddress", macAddress);
        intent.putExtra("milliSeconds", milliSeconds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
