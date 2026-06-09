package com.example.in_appwebview;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

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
        // 这是在 Godot C# 里获取单例的名字
        return "NativeWebView";
    }

    // 1. 暴露给 Godot 的打开网页方法
    @UsedByGodot
    public void open(String url) {
        // WebView 必须在安卓的主 UI 线程中运行
        runOnUiThread(() -> {
            if (webView == null) {
                // 初始化纯内嵌式 WebView
                webView = new WebView(getActivity());

                // 核心权限配置：允许读取 file:// 本地文件，允许执行 JS
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true);
                settings.setDomStorageEnabled(true);

                // 背景透明，完美融合 Godot 游戏画面
                webView.setBackgroundColor(0x00000000);

                // 2. 拦截协议，实现 JS 向 Godot 发送信号
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (url.startsWith("godot://")) {
                            // 捕捉到通关信号，发射给 Godot
                            emitSignal("on_url_changed", url);
                            return true; // 拦截系统跳转
                        }
                        return false;
                    }
                });

                // 3. 将 WebView 盖在 Godot 渲染层之上
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

    // 执行 JavaScript 代码
    @UsedByGodot
    public void evaluate_javascript(String jsCode) {
        runOnUiThread(() -> {
            if (webView != null) {
                webView.evaluateJavascript(jsCode, null);
            }
        });
    }

    // 隐藏 WebView 露出游戏画面
    @UsedByGodot
    public void hide() {
        runOnUiThread(() -> {
            if (webView != null) {
                webView.setVisibility(View.GONE);
            }
        });
    }

    // 注册要发送给 Godot 的信号
    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();
        signals.add(new SignalInfo("on_url_changed", String.class));
        return signals;
    }
}