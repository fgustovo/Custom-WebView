package com.videodetector.clipboard;


import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.videodetector.Utils;

public class ClipboardHandlerService extends JobIntentService {
    private static final String TAG = "ClipboardHandlerService";

    private ClipboardManager clipboardManager;
    private OnPrimaryClipChangedListener clipChangedListener;

    public static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context);
    }

    public static final int JOB_ID = 2;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ClipboardHandlerService.class, JOB_ID, work);
    }

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
                    FloatingWindowService.enqueueWork(ClipboardHandlerService.this, intent);
                } else {
                    // todo notification
                }

                Log.e(TAG, "clipboard: " + a);

            }
        };
        this.clipboardManager.addPrimaryClipChangedListener(this.clipChangedListener);
    }

    public void onDestroy() {
        super.onDestroy();

        if (!(this.clipboardManager == null || this.clipChangedListener == null)) {
            this.clipboardManager.removePrimaryClipChangedListener(this.clipChangedListener);
        }
        if (Build.VERSION.SDK_INT < 26) {
            Intent service = new Intent(this, ClipboardHandlerService.class);
            ClipboardHandlerService.enqueueWork(this, service);
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }
}