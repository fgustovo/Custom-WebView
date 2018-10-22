package com.videodetector.clipboard;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.Keep;

public class ClipboardListener {
    private static final String TAG = "ClipboardListener";
    private static Integer image;
    private static Class<? extends Activity> activityToRun;
    
    @Keep
    public static void init(Context context, @DrawableRes Integer image, Class<? extends Activity> activityToRun) {
        ClipboardListener.image = image;
        ClipboardListener.activityToRun = activityToRun;
        context.startService(new Intent(context, ClipboardHandlerService.class));
    }

    static Integer getImage() {
        return image;
    }

    static Class<? extends Activity> getActivityToRun() {
        return activityToRun;
    }

    public static boolean isValid() {
        return getActivityToRun() != null;
    }
}