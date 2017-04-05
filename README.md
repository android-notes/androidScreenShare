# androidScreenShareAndControl

### 新版本
免root兼容所有Android版本屏幕共享及远程控制。

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
* 退出shell窗口，然后执行 `forward tcp:8888 localabstract:puppet-ver1`
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

[http://weibo.com/tv/v/ED0e0pY2t?fid=1034:1e4fba6695c16e041b279b82068ab06e](http://weibo.com/tv/v/ED0e0pY2t?fid=1034:1e4fba6695c16e041b279b82068ab06e)












### 旧版本

免root兼容所有android屏幕实时共享及远程控制 beta1

该版本可以兼容所有android系统，但仅限于集成了该代码的app，只支持显示activity的布局，不支持显示对话框中的内容。


后续会发布可以显示屏幕上所有内容，可以实时录制所有app，所有对话框，等屏幕上一切能看到的内容，但最低兼容android5.0


工程中包括android工程和lib中java 工程，java工程作为客户端显示手机上内容及远程控制，

通过鼠标点击及拖拽（mac 可以三指拖动屏幕实现手机端滚动）控制手机。


### 初始化

 API14(ICE_CREAM_SANDWICH)及以上版本全局初始化一次即可,context任意,可以是activity也可以是其他。
 以下版本需在每个activity的onResume中初始化,context需要传当前activity。
 
```java

RecorderManager.getInstance(ctx)
                        .startRecorder(ctx, 0.5f);

```

停止录屏

```java

 RecorderManager.getInstance(ctx)
                        .stopRecorder();
                        
```



效果视频：
http://weibo.com/tv/v/Ej3QbovzH?fid=1034:2d6352468588100d72d33d0d1b9e45c1


