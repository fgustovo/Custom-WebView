package com.videodetector.clipboard;


import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.videodetector.Utils;

public class ClipboardHandlerService extends Service {
    private static final String TAG = "ClipboardHandlerService";

    private ClipboardManager clipboardManager;
    private OnPrimaryClipChangedListener clipChangedListener;

    public void onCreate() {
        super.onCreate();

        this.clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        this.clipChangedListener = new OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                String a = Utils.getTextFromClipboard(ClipboardHandlerService.this, ClipboardHandlerService.this.clipboardManager);
                if (a == null || !a.startsWith("http")) {
                    return;
                }

                if (canDrawOverlays(ClipboardHandlerService.this)) {
                    Intent intent = new Intent(ClipboardHandlerService.this, FloatingWindowService.class);
                    intent.putExtra("url", a);
                    ClipboardHandlerService.this.startService(intent);
                } else {
                    // todo notification
                }

                Log.e(TAG, "clipboard: " + a);

            }
        };
        this.clipboardManager.addPrimaryClipChangedListener(this.clipChangedListener);
    }

    public static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context);
    }


    public int onStartCommand(Intent intent, int i, int i2) {
        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        if (!(this.clipboardManager == null || this.clipChangedListener == null)) {
            this.clipboardManager.removePrimaryClipChangedListener(this.clipChangedListener);
        }
        if (Build.VERSION.SDK_INT < 26) {
            startService(new Intent(this, ClipboardHandlerService.class));
        }
    }
}