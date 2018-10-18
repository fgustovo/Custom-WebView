package com.videodetector;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashSet;

public class CustomWebViewClient extends WebViewClient {
    public static final String TAG = "CustomWebViewClient";

    private DetectListener detectListener;

    private static HashSet<String> skip = new HashSet<>();

    static {
        skip.add("ico");
        skip.add("js");
        skip.add("css");
        skip.add("svg");
        skip.add("json");
        skip.add("png");
        skip.add("jpg");
        skip.add("jpeg");
        skip.add("ttf");
    }

    public void setDetectListener(DetectListener detectListener) {
        this.detectListener = detectListener;
    }

    public DetectListener getDetectListener() {
        return detectListener;
    }

    @Override
    @CallSuper
    public void onPageFinished(WebView webView, final String url) {
        Log.d(TAG, "onPageFinished: " + url);
        super.onPageFinished(webView, url);
        webView.loadUrl("javascript:HtmlViewer.showHTML" +
                "(Array.prototype.slice.call(document.getElementsByTagName('video')).map(function(a) {return a.outerHTML}).join(' '));");
    }

    @Override
    @CallSuper
    public void onLoadResource(WebView webView, String url) {
        Log.d(TAG, "onLoadResource: " + url);
        for (String s : skip) {
            if (url.endsWith(s)) {
                super.onLoadResource(webView, url);
                return;
            }
        }
        if (url.toLowerCase().startsWith("http")) {
            new ContentTypeCheckerTask(detectListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        } else
            super.onLoadResource(webView, url);
    }

    @Override
    @CallSuper
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG, "onPageStarted: " + url);
        super.onPageStarted(view, url, favicon);
        CustomWebViewClient.reset(detectListener);
    }


    @TargetApi(21)
    @CallSuper
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        return shouldInterceptRequest(webView, webResourceRequest.getUrl().toString());
    }

    @TargetApi(11)
    @CallSuper
    public WebResourceResponse shouldInterceptRequest(WebView webView, String str) {
        if ((MimeType.isVideo(str))) {
            Log.w(TAG, "shouldInterceptRequest: " + str);
        } else {
            Log.d(TAG, "shouldInterceptRequest: " + str);
        }
        return super.shouldInterceptRequest(webView, str);
    }

    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        Log.d(TAG, "shouldOverrideUrlLoading: " + str);
        return str.equalsIgnoreCase("about:blank") || str.startsWith("market://") || str.startsWith("data");
    }

    @TargetApi(21)
    @CallSuper
    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
        return shouldOverrideUrlLoading(webView, webResourceRequest.getUrl().toString());
    }


    public static class MediaContainer {
        private String url;
        private @Nullable
        String mimeType;
        private @Nullable
        String thumbImage;

        public MediaContainer() {

        }

        public MediaContainer(String url, @Nullable String mimeType, @Nullable String thumbImage) {
            this.url = url;
            this.mimeType = mimeType;
            this.thumbImage = thumbImage;
        }

        public MediaContainer(String url, @Nullable String mimeType) {
            this.url = url;
            this.mimeType = mimeType;
        }

        public int quality() {
            int hasMimeType = TextUtils.isEmpty(getMimeType()) ? 0 : 1;
            int hasThumbImage = TextUtils.isEmpty(getThumbImage()) ? 0 : 1;
            int mp4 = MimeType.mp4.getType().equals(getMimeType()) ? 3 : 0;

            return mp4 + hasThumbImage + hasMimeType;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            if (url != null && url.startsWith("//")) {
                url = "http:" + url;
            }
            this.url = url;
        }

        @Nullable
        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(@Nullable String mimeType) {
            this.mimeType = mimeType;
        }

        @Nullable
        public String getThumbImage() {
            return thumbImage;
        }

        public void setThumbImage(@Nullable String thumbImage) {
            this.thumbImage = thumbImage;
        }

        @Override
        public String toString() {
            return "MediaContainer{" +
                    "mimeType='" + mimeType + '\'' +
                    ", url='" + url + '\'' +
                    ", thumbImage='" + thumbImage + '\'' +
                    '}';
        }
    }


    public static void videoDetected(final DetectListener detectListener, final MediaContainer mediaContainer) {
        if (detectListener == null || mediaContainer == null || TextUtils.isEmpty(mediaContainer.getUrl()) || mediaContainer.getUrl().startsWith("blob:")) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                detectListener.videoDetected(mediaContainer);
            }
        });
    }

    public static void reset(final DetectListener detectListener) {
        if (detectListener == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                detectListener.reset();
            }
        });
    }

    public interface DetectListener {
        @MainThread
        void videoDetected(MediaContainer mediaContainer);

        @MainThread
        void reset();
    }

   /* todo public static void main(String[] args) {
        System.out.println("c12: \t" + a("7777772e6461696c796d6f74696f6e2e636f6d2f706c617965722f6d657461646174612f766964656f2f")); // c12
        System.out.println("c141: \t" + a("68747470733a2f2f7777772e6461696c796d6f74696f6e2e636f6d2f706c617965722f6d657461646174612f766964656f2f")); // c141
        System.out.println("c151: \t" + a("3f656d6265646465723d68747470732533412532462532467777772e6461696c796d6f74696f6e2e636f6d253246766964656f253246")); // c151
        System.out.println("c152: \t" + a("26696e746567726174696f6e3d696e6c696e65")); // c152
        System.out.println("c17: \t" + a("7777772e6461696c796d6f74696f6e2e636f6d2f63646e2f6d616e69666573742f766964656f")); // c17
  *//**c12: 	www.dailymotion.com/player/metadata/video/
     c141: 	https://www.dailymotion.com/player/metadata/video/
     c151: 	?embedder=https%3A%2F%2Fwww.dailymotion.com%2Fvideo%2F
     c152: 	&integration=inline
     c17: 	www.dailymotion.com/cdn/manifest/video*//*
    }
    public static String a(String str) {
        byte[] bArr = new byte[(str.length() / 2)];
        int ı = 0;
        int ı2 = 0;
        while (ı < str.length()) {
            int ı3 = ı2 + 1;
            int ı4 = ı + 2;
            bArr[ı2] = Byte.parseByte(str.substring(ı, ı4), 16);
            ı2 = ı3;
            ı = ı4;
        }
        return new String(bArr);
    }
*/
}