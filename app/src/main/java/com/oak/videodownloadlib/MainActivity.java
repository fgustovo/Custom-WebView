package com.oak.videodownloadlib;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.videodetector.CustomWebViewClient;
import com.videodetector.DownloaderWebView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener {
    public static final String TAG = "MainActivity";

    DownloaderWebView webView;

    ArrayList<String> urls = new ArrayList<>();
    public static int i = 0;
    EditText edittext;
    FloatingActionButton fab;
    Button next;
    TextView d_content;
    TextView d_url;
    private CustomWebViewClient.MediaContainer container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                8);

        d_content = (TextView) findViewById(R.id.d_content);
        d_url = (TextView) findViewById(R.id.d_url);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = i++ % urls.size();
                String url = urls.get(index);
                next.setText("Next:" + index);
                webView.loadUrl(url);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String outfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        File.separator + "test_" + (i) + "_" + System.currentTimeMillis() + ".mp4";

                webView.downloadVideo(container, new File(outfile), new DownloaderWebView.DownloadListener() {
                    @Override
                    public void progress(int ratio) {
                        Log.d(TAG, "Progress:" + ratio);
                        d_content.setText("%" + ratio);
                    }

                    @Override
                    public void error(String cause) {
                        Log.e(TAG, "ERROR:" + cause);

                    }
                });
            }
        });

        edittext = findViewById(R.id.url);
        edittext.setOnEditorActionListener(this);

        urls.add("https://vimeo.com/stock/clip-287237195-interior-shot-of-a-guy-driving-down-the-road-in-an-old-car"); // m4s
        urls.add("https://www.dailymotion.com/video/x6vckzg"); // reklam iniyor


        initWebView();
    }

    private void initWebView() {
        webView = (DownloaderWebView) findViewById(R.id.webview);
        webView.setWebViewClient(new CustomWebViewClient() {
            @Override
            public void onPageFinished(WebView webView, final String url) {
                super.onPageFinished(webView, url);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edittext.setText(url);
                    }
                });
            }
        });

        webView.setDetectListener(new CustomWebViewClient.DetectListener() {
            @Override
            public void videoDetected(CustomWebViewClient.MediaContainer container) {
                Log.e(TAG, "videoDetected: " + container.toString());

                setButtonTint(fab, ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorAccent)));
                MainActivity.this.container = container;
                fab.setEnabled(true);

                d_content.setText(container.getMimeType());
                d_url.setText(container.getUrl());

                ObjectAnimator objAnim = ObjectAnimator.ofPropertyValuesHolder(fab, PropertyValuesHolder.ofFloat("scaleX", 1.2f), PropertyValuesHolder.ofFloat("scaleY", 1.2f));
                objAnim.setDuration(300);
                objAnim.setRepeatCount(1);
                objAnim.setRepeatMode(ObjectAnimator.REVERSE);
                objAnim.start();
            }

            @Override
            public void reset() {
                Log.e(TAG, "reset: ");
                setButtonTint(fab, ColorStateList.valueOf(Color.GRAY));
                fab.setEnabled(false);
                MainActivity.this.container = null;
                d_content.setText("content");
                d_url.setText("url");
            }
        });
    }

    public static void setButtonTint(FloatingActionButton button, ColorStateList tint) {
        ViewCompat.setBackgroundTintList(button, tint);
    }

    @Override
    public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_NULL
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            String url = exampleView.getText().toString();
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            webView.loadUrl(url);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

}
