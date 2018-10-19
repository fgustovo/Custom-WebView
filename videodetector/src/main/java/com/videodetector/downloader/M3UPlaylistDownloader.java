package com.videodetector.downloader;


import android.os.AsyncTask;
import android.util.Log;

import com.videodetector.CustomWebViewClient;
import com.videodetector.DownloaderWebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class M3UPlaylistDownloader extends CustomAsyncTask<String, Void, Boolean> {
    public static final String TAG = "M3UPlaylistDownloader";

    private static final String EXT_X_KEY = "#EXT-X-KEY";
    private static final String BANDWIDTH = "BANDWIDTH";

    private boolean isMaster = false;
    private long maxRate = 0L;
    private int maxRateIndex = 0;
    private String outfile;
    private String key;
    private URL url;
    private List<String> playlist;
    private DownloaderWebView.DownloadListener downloadListener;
    private CustomWebViewClient.MediaContainer container;

    public M3UPlaylistDownloader(CustomWebViewClient.MediaContainer container, URL url, List<String> playlist, DownloaderWebView.DownloadListener downloadListener) throws MalformedURLException {
        this.url = new URL(container.getUrl());
        this.container = container;
        this.playlist = (playlist == null ? new ArrayList<String>() : playlist);
        this.downloadListener = downloadListener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        BufferedReader reader;
        outfile = params[0];
        key = params.length > 1 ? params[1] : null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));


            String line;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                playlist.add(line);

                if (line.contains(BANDWIDTH))
                    isMaster = true;

                if (isMaster && line.contains(BANDWIDTH)) {
                    try {
                        int pos = line.lastIndexOf(BANDWIDTH + "=") + 10;
                        int end = line.indexOf(",", pos);
                        if (end < 0 || end < pos) end = line.length() - 1;
                        long bandwidth = Long.parseLong(line.substring(pos, end));

                        maxRate = Math.max(bandwidth, maxRate);

                        if (bandwidth == maxRate)
                            maxRateIndex = index + 1;
                    } catch (NumberFormatException ignore) {
                        Log.v(TAG, "NumberFormatException" + ignore.getMessage());
                    }
                }

                index++;
            }

            reader.close();

        } catch (IOException e) {
            Log.v(TAG, "Exception");
            e.printStackTrace();
        }
        return isMaster;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (isMaster) {
            Log.v(TAG, "Found master playlist, fetching highest stream at Kb/s: " + maxRate / 1024);
            URL tempUrl = updateUrlForSubPlaylist(url, playlist.get(maxRateIndex));
            if (null != tempUrl) {
                url = tempUrl;
                playlist.clear();
                try {
                    new M3UPlaylistDownloader(container, url, playlist, downloadListener).execute(outfile, key);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        try {
            downloadAfterCrypto(container, downloadListener, url, playlist, outfile, key);
        } catch (Throwable e) {
            Log.v(TAG, "Exception");
            e.printStackTrace();
        }

    }

    private static String getBaseUrl(URL url, String line) {
        if (line == null) {
            line = "";
        }
        if (!line.startsWith("http")) {
            try {
                return url.toURI().resolve(line).toString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            return line;
        }

        String urlString = url.toString();
        int index = urlString.lastIndexOf('/');
        return urlString.substring(0, ++index) + line;
    }

    private URL updateUrlForSubPlaylist(URL url, String sub) {
        URL aUrl = null;

        String newUrl = getBaseUrl(url, sub);

        try {
            aUrl = new URL(newUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return aUrl;
    }

    private static void downloadAfterCrypto(CustomWebViewClient.MediaContainer container, DownloaderWebView.DownloadListener downloadListener, URL url, List<String> playlist, String outfile, String key) throws IOException, GeneralSecurityException {
        Log.v(TAG, "downloadAfterCrypto url:" + url);
        if (downloadListener != null) {
            downloadStarted(downloadListener, container);
            downloadListener.progress(container, 0);
        }
        Crypto crypto = new Crypto(getBaseUrl(url, null), key);

        int lastIndex = -1;
        for (int i = 0; i < playlist.size(); i++) {
            String line = playlist.get(i).trim();
            line = line.trim();

            if (line.length() > 0 && !line.startsWith("#")) {
                lastIndex = i;
            }
        }

        for (int i = 0; i < playlist.size(); i++) {

            String line = playlist.get(i);
            line = line.trim();

            if (line.startsWith(EXT_X_KEY)) {
                crypto.updateKeyString(line);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                    }
                }, 0, 10);
                Log.v(TAG, "\rCurrent Key: " + crypto.getCurrentKey());
                Log.v(TAG, "Current IV:  " + crypto.getCurrentIV());
            } else if (line.length() > 0 && !line.startsWith("#")) {
                URL segmentUrl = new URL(getBaseUrl(url, line));

                downloadInternal(container, crypto, downloadListener, segmentUrl, outfile, i, playlist.size(), lastIndex == i);
            }
        }
    }


    private static void downloadInternal(final CustomWebViewClient.MediaContainer container, final Crypto crypto, final DownloaderWebView.DownloadListener downloadListener, final URL segmentUrl, final String outFile, final int currProgress, final int size, final boolean last) {
        final byte[] buffer = new byte[512];

        new CustomAsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                InputStream is = null;
                try {
                    is = crypto.hasKey()
                            ? crypto.wrapInputStream(segmentUrl.openStream())
                            : segmentUrl.openStream();


                    FileOutputStream out;

                    if (outFile != null) {
                        File file = new File(outFile);
                        out = new FileOutputStream(outFile, file.exists());
                    } else {
                        String path = segmentUrl.getPath();
                        int pos = path.lastIndexOf('/');
                        out = new FileOutputStream(path.substring(++pos), false);
                    }

                    Log.v(TAG, "Downloading segment: " + segmentUrl);

                    int read;

                    while ((read = is.read(buffer)) >= 0) {
                        out.write(buffer, 0, read);
                    }
                    publishProgress(currProgress * 100 / size);
                    Log.e("Progress:*", currProgress + " * " + size + " Last:" + last);
                    if (last) {
                        publishProgress(100);

                        downloadCompleted(downloadListener, container, null);
                    }
                    is.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    downloadCompleted(downloadListener, container, e.getMessage());
                }
                Log.v(TAG, "Downloaded: " + segmentUrl);
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (downloadListener != null) {
                    downloadListener.progress(container, values[0]);
                }
            }
        }.execute();
    }

}


