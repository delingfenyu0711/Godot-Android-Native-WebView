package com.example.in_appwebview;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

public class NativeWebView extends GodotPlugin {

    private WebView webView;

    public NativeWebView(Godot godot) {
        super(godot);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "NativeWebView";
    }

    @UsedByGodot
    public void open(String url) {
        runOnUiThread(() -> {
            if (webView == null) {
                webView = new WebView(getActivity());

                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true);
                settings.setDomStorageEnabled(true);


                settings.setAllowFileAccessFromFileURLs(true);
                settings.setAllowUniversalAccessFromFileURLs(true);
                
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    settings.setMediaPlaybackRequiresUserGesture(false);
                }

                webView.setBackgroundColor(0x00000000);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (url.startsWith("godot://")) {
                            emitSignal("on_url_changed", url);
                            return true;
                        }
                        return false;
                    }
                });

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                getActivity().addContentView(webView, layoutParams);
            }

            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        });
    }

    @UsedByGodot
    public void evaluate_javascript(String jsCode) {
        runOnUiThread(() -> {
            if (webView != null) {
                webView.evaluateJavascript(jsCode, null);
            }
        });
    }

    @UsedByGodot
    public void hide() {
        runOnUiThread(() -> {
            if (webView != null) {
                webView.setVisibility(View.GONE);
            }
        });
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();
        signals.add(new SignalInfo("on_url_changed", String.class));
        return signals;
    }
}