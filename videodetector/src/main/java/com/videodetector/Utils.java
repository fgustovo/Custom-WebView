package com.videodetector;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {


    public static Intent transferIntent(Context context, Class toClass, Intent intent) {
        Intent i = new Intent(context, toClass);
        String url = getURL(intent);
        if (url != null) {
            i.putExtra("url", url);
        }
        return i;
    }
    public static String getURL(Intent intent) {
        if (intent == null) {
            return null;
        }

        if (intent.getAction() == null && !TextUtils.isEmpty(intent.getStringExtra("url"))) {
            return intent.getStringExtra("url");
        }

        if (Intent.ACTION_SEND.equals(intent.getAction()) &&
                "text/plain".equals(intent.getType()) && intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
            String url = intent.getStringExtra(Intent.EXTRA_TEXT);

            String regex = "(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return (matcher.group(0));
            }
        }
        return null;
    }

    public static synchronized String getTextFromClipboard(Context context, ClipboardManager clipboardManager) {
        synchronized (Utils.class) {
            if (clipboardManager == null) {
                clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            }
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).coerceToText(context);
                return text == null ? "" : text.toString();
            }
        }
        return null;
    }


    public static float pixelToDp(Context context, float f) {
        return ((context.getResources().getDisplayMetrics().density * f) + 0.5f);
    }

}