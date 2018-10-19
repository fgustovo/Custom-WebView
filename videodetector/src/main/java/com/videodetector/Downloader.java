package com.videodetector;

import com.videodetector.downloader.M3UPlaylistDownloader;
import com.videodetector.downloader.Mp4Downloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Downloader {


    public static void download(CustomWebViewClient.MediaContainer container, File path, DownloaderWebView.DownloadListener listener) {
        if (!validated(container, path, listener)) {
            return;
        }

        if (!path.getParentFile().exists()) {
            path.getParentFile().mkdirs();
        }

        if (!path.exists()) {
            try {
                path.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String outfile = path.getPath();

        try {
            MimeType mimeType = MimeType.getMimeType(container.getMimeType());
            if (mimeType != null && mimeType.isPlaylist()) {
                new M3UPlaylistDownloader(container, new URL(container.getUrl()), null, listener).execute(outfile);
            } else {
                new Mp4Downloader(container, new URL(container.getUrl()), listener).execute(outfile);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (listener != null) {
                listener.downloadCompleted(container, e.getMessage());
            }
        }

    }


    private static boolean validated(CustomWebViewClient.MediaContainer container, File path, DownloaderWebView.DownloadListener listener) {
        if (path == null) {
            if (listener != null) {
                listener.downloadCompleted(container, "File Path null");
            }
            return false;
        }

        if (container == null) {
            if (listener != null) {
                listener.downloadCompleted(container, "MediaContainer null");
            }
            return false;
        }

        if (container.getUrl() == null) {
            if (listener != null) {
                listener.downloadCompleted(container, "URL null");
            }
            return false;
        }
        return true;
    }


}