# 📱 Godot 4.x Android Native WebView (C# / Mono)

**一个极其轻量、支持背景透明、专为 Godot 4.x C# 版本打造的 Android 原生 WebView 插件。**

![Godot Version](https://img.shields.io/badge/Godot-4.x%20(C%23)-blue?logo=godotengine)
![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)
![License](https://img.shields.io/badge/License-MIT-yellow)

在 Godot 中内嵌网页一直是个痛点，市面上的旧插件大多基于 GDScript 桥接且年久失修。本项目提供了一个纯原生、无依赖的底层单例方案，让你在 Godot 游戏画面之上，完美悬浮渲染 HTML5 交互内容。

## ✨ 核心特性

- **🚀 零 GDScript 桥接 (Pure C#)**：摒弃复杂的中间代理节点，直接通过 `Engine.GetSingleton("NativeWebView")` 在 C# 中与 Android 底层通信，性能极佳，无内存泄漏风险。
- **👻 透明沉浸式渲染**：WebView 背景默认完全透明，HTML UI 可以完美融合、悬浮于 Godot 渲染层之上。
- **🔌 极简双向通信**：
  - **JS 向 Godot 发送消息**：通过自动拦截自定义 `godot://` 协议实现信号发送。
  - **Godot 向 JS 发送指令**：支持通过 `evaluate_javascript` 接口安全执行底层 JS 代码。
- **📂 支持本地离线沙盒加载**：已默认开启 `setAllowFileAccess(true)`，完美支持加载 `user://` 沙盒目录下的离线 HTML/JS/CSS 文件。
- **📦 Godot 4.x 标准扩展结构**：告别旧版 `.gdap` 配置，采用 Godot 4 最新的 `export_plugin.gd` 自动化打包构建，对开发者极其友好。

## 🛠️ 安装与使用

### 1. 导入 Godot 插件
1. 下载本项目，将 `godot_plugin/addons/NativeWebView` 文件夹完整复制到你 Godot 项目的 `res://addons/` 目录下。
2. 在 Godot 编辑器中，点击 **项目 -> 项目设置 -> 插件**，勾选启用 `NativeWebView`。
3. 在导出 Android 预设时，务必勾选 **“使用 Gradle 构建 (Custom Build)”**。

### 2. C# 极简调用示例

只需在场景树中挂载以下 C# 脚本，即可召唤原生的 WebView：

```csharp
using Godot;

public partial class WebViewDemo : Node
{
    private GodotObject _nativeWebView;

    public override void _Ready()
    {
        // 1. 获取底层 Android 单例
        if (Engine.HasSingleton("NativeWebView"))
        {
            _nativeWebView = Engine.GetSingleton("NativeWebView");
            GD.Print("🎉 原生 WebView 插件获取成功！");
            
            // 2. 绑定 JS 发来的通信信号
            _nativeWebView.Connect("on_url_changed", new Callable(this, MethodName.OnWebMessageReceived));
        }
    }

    public void OpenWebPage()
    {
        if (_nativeWebView == null) return;

        // 【选项A】打开在线网页
        // _nativeWebView.Call("open", "[https://godotengine.org](https://godotengine.org)");

        // 【选项B】打开解压到手机本地 user:// 目录下的离线网页
        string userDir = OS.GetUserDataDir();
        string absoluteHtmlPath = userDir + "/my_web_app/index.html";
        _nativeWebView.Call("open", $"file://{absoluteHtmlPath}");
    }

    // 接收来自网页的信号 (当 JS 执行 window.location.href = "godot://xxx" 时触发)
    private void OnWebMessageReceived(string url)
    {
        GD.Print($"收到来自网页的消息: {url}");
        
        if (url.StartsWith("godot://close_web"))
        {
            // 隐藏网页，露出底层游戏画面
            _nativeWebView?.Call("hide");
        }
    }

    // 让 Godot 主动调用网页里的 JS 函数
    public void CallJsFunction()
    {
        _nativeWebView?.Call("evaluate_javascript", "alert('Hello from Godot C#!');");
    }
}
