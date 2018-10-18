package com.videodetector.downloader;


import android.os.AsyncTask;
import android.util.Log;

import com.videodetector.DownloaderWebView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class Mp4Downloader extends AsyncTask<String, Integer, Void> {
    public static final String TAG = "Mp4Downloader";

    private String outfile;
    private URL url;
    private DownloaderWebView.DownloadListener downloadListener;

    public Mp4Downloader(URL url, DownloaderWebView.DownloadListener downloadListener) {
        this.url = url;
        this.downloadListener = downloadListener;
    }

    @Override
    protected Void doInBackground(String... params) {
        outfile = params[0];
        int count;
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

        } catch (IOException e) {
            Log.v(TAG, "Exception");
            e.printStackTrace();
        }
        publishProgress(100);
        return null;
    }

    @Override
    protected void onPostExecute(Void _void) {

    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (downloadListener != null) {
            downloadListener.progress(values[0]);
        }
    }
}


