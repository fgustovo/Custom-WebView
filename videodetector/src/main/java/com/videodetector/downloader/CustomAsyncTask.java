package com.videodetector.downloader;


import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.videodetector.CustomWebViewClient;
import com.videodetector.DownloaderWebView;


public abstract class CustomAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    public static void downloadStarted(final DownloaderWebView.DownloadListener downloadListener, final CustomWebViewClient.MediaContainer container) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                downloadListener.downloadStarted(container);
            }
        });
    }

    public static void downloadCompleted(final DownloaderWebView.DownloadListener downloadListener, final CustomWebViewClient.MediaContainer container, final String error) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                downloadListener.downloadCompleted(container, error);
            }
        });
    }
}


