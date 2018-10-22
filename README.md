# Custom-WebView  [![](https://jitpack.io/v/fgustovo/Custom-WebView.svg)](https://jitpack.io/#fgustovo/Custom-WebView)

Add to your project:
----------------------------
Available on Jitpack. Please ensure that you are using the latest versions by [checking here](https://jitpack.io/#gs.oak/adchain)

Add the following Gradle configuration to your Android project:
```groovy
// In your root build.gradle file:
buildscript {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' } // add repository
    }
}

// In your app projects build.gradle file:

dependencies {
    implementation 'com.github.fgustovo:Custom-WebView:0.0.5'
}
```

<br/>

Layout:
```xml
<com.videodetector.DownloaderWebView
        android:id="@+id/webview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

<br/>

Detect Videos in Webview:
```java
webView = (DownloaderWebView) view.findViewById(R.id.webview);
webView.setDetectListener(new CustomWebViewClient.DetectListener() {
            @Override
            public void videoDetected(CustomWebViewClient.MediaContainer mediaContainer) {
                // New video detected. 
                // store `mediaContainer`, you will use it when user pressed download button 
            }

            @Override
            public void reset() {
                // Page changed and video not visible anymore. 
            }

            @Override
            public void alertYoutube() {
                // Give alert. Youtube doesn't allowed 
            }
        });
```

<br/>

Download Detected Videos:
```java
String outfile = "_file path to save_";
webView.downloadVideo(mediaContainer, new File(outfile), new DownloaderWebView.DownloadListener() {
                    @Override
                    public void progress(CustomWebViewClient.MediaContainer mediaContainer, int i) {
                        // progress updated to `i` for mediaContainer
                    }

                    @Override
                    public void downloadStarted(CustomWebViewClient.MediaContainer mediaContainer) {
                        // downloadStarted for mediaContainer
                    }

                    @Override
                    public void downloadCompleted(CustomWebViewClient.MediaContainer mediaContainer, String error) {
                        if (error != null) {
                            Toast.makeText(getContext(), "Couldn't download: " + error, Toast.LENGTH_LONG).show();
                            return;
                        }
                        // downloadCompleted for mediaContainer
                    }
                });
```



Transfer Share/Copy intents to MainActivity:
```java
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Intent nextActivity = Utils.transferIntent(this, MainActivity.class, getIntent());
        startActivity(nextActivity);
        finish();

        ClipboardListener.init(this, R.drawable.logo1, Splash.class); // init if you want clipboard Listener
    }
}
```

Get Shared/Copied URL
```java
    String url = Utils.getURL(getIntent());
```


