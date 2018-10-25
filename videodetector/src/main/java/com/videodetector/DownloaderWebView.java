package com.videodetector;

import android.content.Context;
import android.os.Build;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;

public class DownloaderWebView extends WebView {

    private CustomWebViewClient client;

    public DownloaderWebView(Context context) {
        super(context);
        initView(context);
    }

    public DownloaderWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private static void fillMediaContainerListWithTag(ArrayList<CustomWebViewClient.MediaContainer> all, String title, Elements vElements, String imgTag) {
        for (Element last : vElements) {
            CustomWebViewClient.MediaContainer container = new CustomWebViewClient.MediaContainer();

            container.setThumbImage(last.attr(imgTag));
            container.setUrl(last.attr("src"));
            container.setMimeType(last.attr("type"));
            container.setPageTitle(title);
            all.add(container);
        }
    }

    private void initView(Context context) {
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT >= 17) {
            this.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        this.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        if (Build.VERSION.SDK_INT >= 26) {
            this.getSettings().setSafeBrowsingEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= 24) {
            this.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/64.0.3282.137 Mobile Safari/537.36");
        }

        addJavascriptInterface(this, "HtmlViewer");
        setWebViewClient(new CustomWebViewClient());
    }

    @JavascriptInterface
    public void showHTML(String html) {
        Log.i(CustomWebViewClient.TAG, "source: " + html);

        if (html != null && getCustomWebViewClient().getDetectListener() != null) {

            try {
                ArrayList<CustomWebViewClient.MediaContainer> all = new ArrayList<>();

                Document parse = Jsoup.parse(html);

                Elements vElements = parse.select("video[src]");
                fillMediaContainerListWithTag(all, getTitle(), vElements, "poster");

                Elements sElements = parse.select("video source[src]");
                fillMediaContainerListWithTag(all, getTitle(), sElements, "srcset");

                int bestQuality = -1;
                int bestQualityIndex = -1;
                for (int i = 0; i < all.size(); i++) {
                    CustomWebViewClient.MediaContainer container = all.get(i);
                    int q = container.quality();
                    if (q > bestQuality) {
                        bestQuality = q;
                        bestQualityIndex = i;
                    }
                }

                if (bestQualityIndex > -1) {
                    CustomWebViewClient.videoDetected(getCustomWebViewClient().getDetectListener(), all.get(bestQualityIndex));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (!(client instanceof CustomWebViewClient)) {
            throw new IllegalArgumentException("WebViewClient should be CustomWebViewClient!");
        }
        this.client = (CustomWebViewClient) client;
        super.setWebViewClient(client);
    }

    final public CustomWebViewClient getCustomWebViewClient() {
        return this.client;
    }

    public void downloadVideo(CustomWebViewClient.MediaContainer container, File path, DownloadListener listener) {
        Log.e(CustomWebViewClient.TAG, "downloadVideo" + container.toString());
        CustomWebViewClient.reset(getCustomWebViewClient().getDetectListener());

        if (container != null && container.getUrl() != null &&
                (CustomWebViewClient.isYoutube(container.getUrl()) || CustomWebViewClient.isGoogle(container.getUrl()))) {
            CustomWebViewClient.alertYoutube(getCustomWebViewClient().getDetectListener());
            return;
        }

        Downloader.download(container, path, listener);
    }

    public void setDetectListener(CustomWebViewClient.DetectListener detectListener) {
        getCustomWebViewClient().setDetectListener(detectListener);
    }


    public interface DownloadListener {
        @MainThread
        void progress(CustomWebViewClient.MediaContainer mediaContainer, int ratio);

        @MainThread
        void downloadStarted(CustomWebViewClient.MediaContainer mediaContainer);

        @MainThread
        void downloadCompleted(CustomWebViewClient.MediaContainer mediaContainer, String error);
    }

}