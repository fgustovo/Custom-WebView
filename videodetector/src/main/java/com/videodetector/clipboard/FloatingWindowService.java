package com.videodetector.clipboard;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.videodetector.R;
import com.videodetector.Utils;


public class FloatingWindowService extends Service {
    private static final String TAG = "FloatingWindowService";
    private boolean displayed = false;

    private WindowManager windowManager;
    private LinearLayout linearLayout;
    private ImageView imageView;

    private String url;

    public final IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                this.url = extras.getString("url");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public final void onCreate() {
        super.onCreate();
        if (ClipboardListener.isValid()) {
            FloatingWindowService.this.display();
        }
    }

    public final void onDestroy() {
        super.onDestroy();
        try {
            if (this.imageView != null) {
                this.imageView.setVisibility(View.GONE);
            }
            if (this.linearLayout != null) {
                this.windowManager.removeView(this.linearLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destroy() {
        if (!displayed || this.linearLayout == null) {
            stopSelf();
            return;
        }
        displayed = false;
        hideImageView(this.imageView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void display() {
        if (!displayed) {
            displayed = false;

            WindowManager.LayoutParams params = getLayoutParams();

            this.windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

            this.linearLayout = (LinearLayout) LayoutInflater.from(getApplication()).inflate(R.layout.floating_layout, null);
            this.windowManager.addView(this.linearLayout, params);
            this.imageView = (ImageView) this.linearLayout.findViewById(R.id.float_id);

            Drawable icon;
            Integer drawable = ClipboardListener.getImage();
            if (drawable != null) {
                icon = getResources().getDrawable(drawable);
            } else {
                icon = getApplicationInfo().loadIcon(getPackageManager());
            }
            imageView.setImageDrawable(icon);

            this.linearLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            this.imageView.setOnClickListener(new OnClickListener() {
                @SuppressLint("WrongConstant")
                public void onClick(View view) {
                    FloatingWindowService.this.destroy();


                    Class<? extends Activity> activityToRun = ClipboardListener.getActivityToRun();
                    if (activityToRun == null) {
                        Log.e(TAG, "ClipboardListener.getActivityToRun() is null");
                        return;
                    }
                    Intent intent = new Intent(FloatingWindowService.this, activityToRun);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_RECEIVER_FOREGROUND |
                            Intent.FLAG_RECEIVER_REPLACE_PENDING);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    FloatingWindowService.this.startActivity(intent);
                }
            });
            this.linearLayout.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 4) {
                        FloatingWindowService.this.destroy();
                    }
                    return false;
                }
            });
            this.imageView.setVisibility(View.GONE);
            this.linearLayout.postDelayed(new Runnable() {
                public void run() {
                    FloatingWindowService.this.imageView.setVisibility(View.VISIBLE);
                    FloatingWindowService.this.showImageView(FloatingWindowService.this.imageView);
                }
            }, 300);
        }
    }


    @NonNull
    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.RGBA_8888;
        if (VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (VERSION.SDK_INT >= 24) {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else if (VERSION.SDK_INT >= 19) {
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            try {
                String obj = Build.MODEL;
                if (!TextUtils.isEmpty(obj) && obj.toLowerCase().contains("vivo") && VERSION.SDK_INT > 19 && VERSION.SDK_INT < 23) {
                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.x = 0;
        params.y = 0;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        return params;
    }

    private void showImageView(View view) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "translationX", Utils.pixelToDp(this, 100.0f), 0.0f);
        ofFloat.setIntValues(3, 2, 1);
        ofFloat.setDuration(300);
        ofFloat.start();
    }

    private void hideImageView(View view) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
        ofFloat.setIntValues(3, 2, 1);
        ofFloat.setDuration(500);
        ofFloat.start();
        ofFloat.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                try {
                    if (FloatingWindowService.this.imageView != null) {
                        FloatingWindowService.this.imageView.setVisibility(View.GONE);
                    }
                    if (!(FloatingWindowService.this.windowManager == null || FloatingWindowService.this.linearLayout == null)) {
                        FloatingWindowService.this.windowManager.removeView(FloatingWindowService.this.linearLayout);
                    }
                    FloatingWindowService.this.stopSelf();
                } catch (Exception e) {
                    e.printStackTrace();
                    FloatingWindowService.this.stopSelf();
                }
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

}