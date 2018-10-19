package com.videodetector.downloader;


import android.os.AsyncTask;
import android.util.Log;

import com.videodetector.CustomWebViewClient;
import com.videodetector.DownloaderWebView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Mp4Downloader extends CustomAsyncTask<String, Integer, Void> {
    public static final String TAG = "Mp4Downloader";

    private final CustomWebViewClient.MediaContainer container;
    private String outfile;
    private URL url;
    private DownloaderWebView.DownloadListener downloadListener;

    public Mp4Downloader(CustomWebViewClient.MediaContainer container, URL url, DownloaderWebView.DownloadListener downloadListener) throws MalformedURLException {
        this.url = new URL(container.getUrl());
        this.container = container;
        this.downloadListener = downloadListener;
    }

    @Override
    protected Void doInBackground(String... params) {
        outfile = params[0];
        int count;

        downloadStarted(downloadListener, container);
        publishProgress(0);
        try {
            URLConnection conection = url.openConnection();
            conection.connect();
            int lenghtOfFile = conection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream(), 8192);


            OutputStream output = new FileOutputStream(outfile);

            byte data[] = new byte[1024];
            long total = 0;
            int i = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                i++;
                if (i % 100 == 0) {
                    publishProgress((int) ((total * 100) / lenghtOfFile));
                }
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            publishProgress(100);
            downloadCompleted(downloadListener, container, null); //
        } catch (IOException e) {
            Log.v(TAG, "Exception");
            e.printStackTrace();
            downloadCompleted(downloadListener, container, e.getMessage()); //
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void _void) {

    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (downloadListener != null) {
            downloadListener.progress(container, values[0]);
        }
    }
}


