package com.walton.startupbroadcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.core.app.NotificationCompat;

public class OverlayService extends Service implements View.OnTouchListener, View.OnClickListener {
    private static final String TAG = "OverlayService";
    public static final String ACTION_REMOVE_OVERLAY = "ACTION_REMOVE_OVERLAY";

    private WindowManager wm;
    private View overlayView;
    private boolean isOverlayShown = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null && ACTION_REMOVE_OVERLAY.equals(intent.getAction())) {
            removeOverlay();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "overlay_service_channel";
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Overlay Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Overlay Service")
                    .setContentText("Overlay running")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();

            startForeground(1, notification);
        }

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // Inflate the styled layout instead of building a TextView manually
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_display_button, null);
        Button button = overlayView.findViewById(R.id.btn_overlay_display);
        button.setOnClickListener(this);
        button.setOnTouchListener(this);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER | Gravity.TOP;
        params.x = 10;
        params.y = 120;

        if (!isOverlayShown) {
            try {
                wm.addView(overlayView, params);
                isOverlayShown = true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to add overlay", e);
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "Overlay touched");
        return false;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Overlay clicked, removing overlay");
        removeOverlay();
    }

    private void removeOverlay() {
        try {
            if (wm != null && overlayView != null && isOverlayShown) {
                wm.removeView(overlayView);
                overlayView = null;
                isOverlayShown = false;
                Log.d(TAG, "Overlay removed successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing overlay", e);
        }
    }

    @Override
    public void onDestroy() {
        removeOverlay();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}