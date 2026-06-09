@tool
extends EditorPlugin

var export_plugin : AndroidExportPlugin

func _enter_tree():
    export_plugin = AndroidExportPlugin.new()
    add_export_plugin(export_plugin)

func _exit_tree():
    remove_export_plugin(export_plugin)
    export_plugin = null

class AndroidExportPlugin extends EditorExportPlugin:
    var _plugin_name = "NativeWebView"

    func _supports_platform(platform):
        if platform is EditorExportPlatformAndroid:
            return true
        return false

    # 核心：告诉 Godot 打包时把我们的 aar 文件一起编译进去
    func _get_android_libraries(platform, debug):
        if debug:
            return PackedStringArray(["res://addons/NativeWebView/NativeWebView.aar"])
        else:
            return PackedStringArray(["res://addons/NativeWebView/NativeWebView.aar"])

    func _get_name():
        return _plugin_name