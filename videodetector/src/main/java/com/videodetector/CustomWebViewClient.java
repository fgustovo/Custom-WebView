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
        if (isYoutube(url)) {
            alertYoutube(detectListener);
            webView.stopLoading();
            return;
        }
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
            new ContentTypeCheckerTask(detectListener, webView.getTitle()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        } else
            super.onLoadResource(webView, url);
    }

    @Override
    @CallSuper
    public void onPageStarted(WebView webView, String url, Bitmap favicon) {
        Log.d(TAG, "onPageStarted: " + url);
        super.onPageStarted(webView, url, favicon);
        if (isYoutube(url)) {
            alertYoutube(detectListener);
            webView.stopLoading();
            return;
        }
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

    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        Log.d(TAG, "shouldOverrideUrlLoading: " + url);
        if (isYoutube(url)) {
            alertYoutube(detectListener);
            webView.stopLoading();
            return false;
        }
        return url.equalsIgnoreCase("about:blank") || url.startsWith("market://") || url.startsWith("data");
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

        private @Nullable
        String pageTitle;

        public MediaContainer() {

        }

        public MediaContainer(String url, @Nullable String mimeType) {
            this.url = url;
            this.mimeType = mimeType;
        }

        public int quality() {
            int hasMimeType = TextUtils.isEmpty(getMimeType()) ? 0 : 1;
            int hasThumbImage = TextUtils.isEmpty(getThumbImage()) ? 0 : 1;
            int hasPageTitle = TextUtils.isEmpty(getPageTitle()) ? 0 : 1;
            int mp4 = MimeType.mp4.getType().equals(getMimeType()) ? 3 : 0;

            return mp4 + hasThumbImage + hasMimeType + hasPageTitle;
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

        public String getPageTitle() {
            return pageTitle;
        }

        public void setPageTitle(String pageTitle) {
            this.pageTitle = pageTitle;
        }

        @Override
        public String toString() {
            return "MediaContainer{" +
                    "mimeType='" + mimeType + '\'' +
                    ", url='" + url + '\'' +
                    ", title='" + getPageTitle() + '\'' +
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

    public static void alertYoutube(final DetectListener detectListener) {
        if (detectListener == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                detectListener.alertYoutube();
            }
        });
    }

    public static   boolean isGoogle(String url) {
        return url != null && (url.contains("google.co"));
    }

    public static   boolean isYoutube(String url) {
        return url != null && (url.contains("youtube.co") || url.contains("youtu.be"));
    }

    public interface DetectListener {
        @MainThread
        void videoDetected(MediaContainer mediaContainer);

        @MainThread
        void reset();

        @MainThread
        void alertYoutube();
    }
    // todo vimeo
}