package com.videodetector;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class ContentTypeCheckerTask extends AsyncTask<String, Void, CustomWebViewClient.MediaContainer> {

    private final CustomWebViewClient.DetectListener detectListener;

    public ContentTypeCheckerTask(CustomWebViewClient.DetectListener detectListener) {
        this.detectListener = detectListener;
    }

    @Override
    protected CustomWebViewClient.MediaContainer doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("HEAD");
            connection.connect();

            String contentType = connection.getContentType();
            contentType = contentTypeString(contentType);

            Log.d(CustomWebViewClient.TAG, "contentType: " + contentType + " URL:" + params[0]);

            if (contentType != null && (MimeType.isVideo(contentType)))
                return new CustomWebViewClient.MediaContainer(params[0], contentType);
        } catch (Exception e) {
//            Log.e(getClass().getSimpleName(), "exception during content type detection for url " + params[0], e);
        }
        return null;
    }

    private String contentTypeString(String contentType) {
        if (contentType != null && contentType.contains(";")) {
            String[] split = contentType.split(";");
            for (String s : split) {
                if (s.contains("/")) {
                    contentType = s.trim();
                    break;
                }
            }
        }
        return contentType;
    }

    @Override
    protected void onPostExecute(CustomWebViewClient.MediaContainer container) {
        if (container == null || container.getUrl() == null) {
            return;
        }

        Log.e(CustomWebViewClient.TAG, "VIDEO DETECTED: " + container.toString());

        CustomWebViewClient.videoDetected(detectListener, container);

    }

}