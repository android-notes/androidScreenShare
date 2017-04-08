# androidScreenShareAndControl

### 免root兼容所有Android版本屏幕共享及远程控制。

### 使用方式

对于mac 笔记本用户：
* Android手机开启开发者选项
* 用数据线连接Android手机和mac
* 运行lib目录下的Client，用于显示和控制
* 运行lib目录下的Install，然后点击连接按钮，若不显示请安如下方式操作


对于所有用户：
* Android手机开启开发者选项
* 用数据线连接Android手机和PC
* 使用adb命令把项目根目录下的Main.dex放到手机中 `adb push Main.dex /sdcard/Main.dex`
* 执行adb shell命令 `adb shell`
* 执行命令 `export CLASSPATH=/sdcard/Main.dex`
* 执行命令 `exec app_process /sdcard com.wanjian.puppet.Main`
* 新建命令窗口，然后执行 `adb forward tcp:8888 localabstract:puppet-ver1`
* 运行lib目录下的Client，用于显示和控制,点击连接按钮即可



完整命令如下
```html

MGJwanjian:sss wanjian$ adb push Main.dex /sdcard/Main.dex
[100%] /sdcard/Main.dex
MGJwanjian:sss wanjian$ adb shell
shell@mx5:/ $ export CLASSPATH=/sdcard/Main.dex
shell@mx5:/ $ exec app_process /sdcard com.wanjian.puppet.Main
MGJwanjian:~ wanjian$ adb forward tcp:8888 localabstract:puppet-ver1


```




### 效果视频

[https://github.com/android-notes/androidScreenShareAndControl/blob/master/%E6%95%88%E6%9E%9C%E8%A7%86%E9%A2%91.mp4](https://github.com/android-notes/androidScreenShareAndControl/blob/master/%E6%95%88%E6%9E%9C%E8%A7%86%E9%A2%91.mp4)



### 简易版
[http://blog.csdn.net/qingchunweiliang/article/details/69210431](http://blog.csdn.net/qingchunweiliang/article/details/69210431)







