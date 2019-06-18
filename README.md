
### 免root兼容所有Android版本屏幕共享及远程控制。
### 效果
![demo](https://github.com/android-notes/androidScreenShareAndControl/blob/master/demo.gif?raw=true)
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

注意：高版本的android手机需要去开发者选项中开启 允许模拟点击

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



### 屏幕共享原理

原理和Vysor相同，Android提供了两个截屏方法Surface. screenshot和SurfaceControl. screenshot，
这两个API是隐藏的，客户端没有权限调用，即使通过反射也得不到bitmap，我们可以使用adb命令
启动一个进程，让该进程调用该API就可以得到bitmap了，然后通过socket把数据发送到PC即可。

关键代码如下：
```java

public class Main{
    public static void main(String[]args){
        Point size = new Point();
        size.x = 1080;//最终截屏图片的大小，可以和屏幕不一样大
        size.y = 1920;
       String surfaceClassName;
           if (Build.VERSION.SDK_INT <= 17) {
            surfaceClassName = "android.view.Surface";
           } else {
            surfaceClassName = "android.view.SurfaceControl";
           }
           Bitmap b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(size.x), Integer.valueOf(size.y)});
    }
}


```

* 然后按照如下操作：
    * 把这个类编译成class文件
    * 使用dx --dex --output=Main.dex  Main.class打包成dex文件
    * 把dex文件发送到手机adb push Main.dex /sdcard/Main.dex
    * 执行adb shell进入shell
    * 设置类路径 export CLASSPATH=/sdcard/Main.dex
    * 运行代码 exec app_process /sdcard Main

 这样就可以调用到Main.main方法

### 远程控制原理

* PC端获取点击位置相对于当前显示窗口的比例
* 把该比例发送给手机端
* 手机端根据手机屏幕大小把比例转换成绝对位置并调用如下代码既可以实现远程控制，x和y是点击的绝对位置，action是动作，如按下，滑动，抬起等

```java



InputManager im = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);


MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);

Method injectInputEventMethod = InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});


MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
event.setSource(InputDeviceCompat.SOURCE_TOUCHSCREEN);

injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});



```

##### 注意：
      获取屏幕大小时会用到几个特殊的API，Android sdk没有提供这几个API，但Android运行时却可以调用，
      为了保证编译不报错，我们可以自己手写这几个API，保证包名，方法签名和系统中的相同即可，方法若有
      返回值直接返回null即可。例如


```java

package android.view;
import android.graphics.Point;
import android.os.IBinder;
/**
 * Created by wanjian on 2017/4/4.
 */
public interface IWindowManager {
    void getInitialDisplaySize(int i, Point displaySize);
    int getRotation();
    void getRealDisplaySize(Point displaySize);
    abstract class Stub {
         public static IWindowManager asInterface(IBinder invoke) {
                     return null;
         }
     }
}


```


```java

package android.view;

/**
 * Created by wanjian on 2017/4/4.
 */

public interface DisplayInfo {
}



```



```java
 package android.view;

/**
 * Created by wanjian on 2017/4/4.
 */

public interface IRotationWatcher {
}


```


由于Android的双亲委派类加载机制，Android会从系统路径下加载这几个类，并不会使用我们编写的类，我们编
写的这几个类只是为了编译不报错，所以返回null也不会出现空指针






### 简易版
[http://blog.csdn.net/qingchunweiliang/article/details/69210431](http://blog.csdn.net/qingchunweiliang/article/details/69210431)



### Github
[https://github.com/android-notes/androidScreenShareAndControl](https://github.com/android-notes/androidScreenShareAndControl)


### 附 编译class方式：

最简单的方式：
在`android studio`中右击`com.wanjian.puppet.Main`这个文件，选择 `run Main.main()`，编译后的class文件就会自动保存到  
`androidScreenShareAndControl/shareandcontrollib/build/intermediates/classes/debug` 这个目录中 


方式2：
* 把 `android sdk`目录下的`android.jar`和`supportv4.jar`拷贝到   
`androidScreenShareAndControl/shareandcontrollib/src/main/java`
目录下

* 同时在这个目录下新建`classes`文件夹，用于保存编译后的class文件，并把命令行切换到这个目录

* 执行如下命令，其中`android.jar`和`support-v4-23.4.0-sources.jar` 是`android sdk`中的`jar`包,一个在`platforms/android-xx`文件夹下，一个在`extras/android/m2repository/com/android/support/support-v4`下。（命令中间用:分割，windows的话需要用;分割）

* `javac -cp android.jar:support-v4-23.4.0-sources.jar:./  com/wanjian/puppet/Main.java  -d classes`

* 这样就会在classes文件夹中生成class文件了 (JDK版本不能太高，不然会提示 unsupported class file version 52.0)

![img](https://raw.githubusercontent.com/android-notes/blogimg/master/%E6%89%93%E5%8C%85class%E5%92%8Cdex.png)


### 打包dex方式：
首先命令窗口切换到 `/androidScreenShareAndControl/shareandcontrollib/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes` 目录下，可以看到
所有编译生成的class文件，如果没有先执行上面步骤生成class文件。

然后使用 `dx  --dex --output=Main.dex ./`命令生成dex文件。dx命令文件在 `sdk/build-tools/版本号` 下

![dex](https://raw.githubusercontent.com/android-notes/androidScreenShareAndControl/master/dex-package.png)



