# androidScreenShareAndControl
免root兼容所有android屏幕实时共享及远程控制 beta1

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
