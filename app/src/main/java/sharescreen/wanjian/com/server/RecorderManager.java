package sharescreen.wanjian.com.server;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by wanjian on 2016/11/20.
 */

public class RecorderManager {


    private static final String TAG = RecorderManager.class.getName();
    public static final byte VERSION = 1;
    private static RecorderManager sManager;
    private Context mContext;
    private Handler mCompressHandler;
    private List<ClientHandler> mClientHandlers = new ArrayList<>();
    private Bitmap mDrawingBoard;
    private Canvas mCanvas = new Canvas();
    private View rootView;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private Runnable mDrawTask = new DrawTask();
    private Runnable mCompressTask = new CompressTask();

    private final int MAX_CLIENT_COUNT = 10;
    //    private final float fps = 60f;
//    private final int delay = (int) (1000 / fps);

    public static synchronized RecorderManager getInstance(Context context) {
        if (sManager == null) {
            sManager = new RecorderManager(context);
        }
        return sManager;
    }

    private RecorderManager(Context context) {
        this.mContext = context.getApplicationContext();
        new HandlerThread("Compress-Thread") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mCompressHandler = new Handler();
            }
        }.start();
        startListen();

    }

    private void startListen() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                ServerSocket serverSocket = null;

                for (int i = 8080; i < 65535; i++) {
                    try {
                        serverSocket = new ServerSocket(i);
                        final int port = i;
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "端口: " + port, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    } catch (IOException e) {
                    }
                }
                for (int i = 0; i < MAX_CLIENT_COUNT; ) {
                    try {
                        final Socket socket = serverSocket.accept();
                        new HandlerThread("Client-Thread") {
                            @Override
                            protected void onLooperPrepared() {
                                super.onLooperPrepared();
                                mClientHandlers.add(new ClientHandler(socket));
                            }
                        }.start();
                        listenRemoteTouchEvent(socket);
                        i++;
                    } catch (IOException e) {
                        return;
                    }
                }

            }
        }.start();

    }

    private void listenRemoteTouchEvent(final Socket socket) {
        new Thread() {
            private final String DOWN = "DOWN";
            private final String MOVE = "MOVE";
            private final String UP = "UP";

            @Override
            public void run() {
                super.run();
                try {
                    MotionEvent motionEvent = null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true) {
                        String line;
                        try {
                            line = reader.readLine();
                        } catch (Exception e) {
                            return;
                        }
                        try {
                            if (line.startsWith(DOWN)) {
                                hanlerDown(line.substring(DOWN.length()));
                            } else if (line.startsWith(MOVE)) {
                                float[] xy = getXY(line.substring(MOVE.length()));
                                if (motionEvent == null) {
                                    motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, xy[0], xy[1], 0);
                                } else {
                                    motionEvent.setAction(MotionEvent.ACTION_MOVE);
//                                    motionEvent.setLocation(xy[0], xy[1]);
                                }
                                sendTouchEvent(motionEvent, motionEvent.getAction(), xy[0], xy[1]);
                            } else if (line.startsWith(UP)) {
                                float[] xy = getXY(line.substring(UP.length()));
                                sendTouchEvent(motionEvent, MotionEvent.ACTION_UP, xy[0], xy[1]);
                                motionEvent = null;
                            }
                        } catch (Exception e) {
                        }


                    }
                } catch (Exception e) {

                }
            }

            private void sendTouchEvent(final MotionEvent motionEvent, final int action, final float x, final float y) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            motionEvent.setAction(action);
                            motionEvent.setLocation(x, y);
                            rootView.dispatchTouchEvent(motionEvent);
                            Log.d(TAG, "touch event " + motionEvent.getAction() + " " + motionEvent.getX() + " " + motionEvent.getY());
                        } catch (Exception e) {
                        }
                    }
                });
            }

            private float[] getXY(String nums) {
                try {
                    String[] s = nums.split("#");
                    float scaleX = Float.parseFloat(s[0]);
                    float scaleY = Float.parseFloat(s[1]);
                    return new float[]{rootView.getWidth() * scaleX, rootView.getHeight() * scaleY};
                } catch (Exception e) {

                }
                return new float[2];
            }

            private void hanlerDown(String line) {
                try {
                    float xy[] = getXY(line);
                    MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, xy[0], xy[1], 0);
                    sendTouchEvent(motionEvent, MotionEvent.ACTION_DOWN, xy[0], xy[1]);
                    sendTouchEvent(motionEvent, MotionEvent.ACTION_UP, xy[0], xy[1]);
                } catch (Exception e) {
                }
            }
        }.start();
    }

    public void stopRecorder() {

        rootView = null;
        mUIHandler.removeCallbacks(mDrawTask);
        if (mCompressHandler != null) {
            mCompressHandler.getLooper().quit();
        }
        for (ClientHandler clientHandler : mClientHandlers) {
            clientHandler.close();
        }
//        try {
//            socket.close();
//        } catch (Exception e) {
//
//        }
        sManager = null;
    }

    /**
     * API14(ICE_CREAM_SANDWICH)及以上版本全局初始化一次即可,context任意,可以是activity也可以是其他。
     * 以下版本需在每个activity的onResume中初始化,context需要传当前activity。
     *
     * @param context API14(ICE_CREAM_SANDWICH)以下传当前activty,其他版本建议传当前activty也可以是任意context
     * @param scale   实际传输图像尺寸与手机屏幕比例
     */
    public void startRecorder(final Context context, float scale) {
        Point point = getScreenSize(context);
        int exceptW = (int) (point.x * scale);
        int exceptH = (int) (point.y * scale);
        if (mDrawingBoard == null) {
            mDrawingBoard = Bitmap.createBitmap(exceptW, exceptH, Bitmap.Config.RGB_565);
        }
        if (mDrawingBoard.getWidth() != exceptW || mDrawingBoard.getHeight() != exceptH) {
            mDrawingBoard.recycle();
            mDrawingBoard = Bitmap.createBitmap(exceptW, exceptH, Bitmap.Config.RGB_565);
        }
        mCanvas.setBitmap(mDrawingBoard);
        mCanvas.scale(scale, scale);
        if (context instanceof Activity) {
            startRecorderActivity(((Activity) context));
        } else {
            Toast.makeText(context, "请下拉一下通知栏试试", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksAdapter() {
                @Override
                public void onActivityResumed(Activity activity) {
                    startRecorderActivity(activity);
                }
            });
        }
    }


    private static Point getScreenSize(Context context) {
        int w = context.getResources().getDisplayMetrics().widthPixels;
        int h = context.getResources().getDisplayMetrics().heightPixels;

        return new Point(w, h);
    }

    private void startRecorderActivity(Activity activity) {
        rootView = activity.getWindow().getDecorView();
        mUIHandler.removeCallbacks(mDrawTask);
        mUIHandler.post(mDrawTask);
    }


    private class DrawTask implements Runnable {
        @Override
        public void run() {
            if (rootView == null) {
                return;
            }
            mUIHandler.removeCallbacks(mDrawTask);
            rootView.draw(mCanvas);
            mCompressHandler.removeCallbacks(mCompressTask);
            mCompressHandler.post(mCompressTask);
        }
    }

    private class CompressTask implements Runnable {
        ByteArrayPool mByteArrayPool = new ByteArrayPool(1024 * 30);
        PoolingByteArrayOutputStream mByteArrayOutputStream = new PoolingByteArrayOutputStream(mByteArrayPool);

        @Override
        public void run() {
            try {//动态改变缩放比例时,由于不在该线程,可能导致bitmap被回收
                mByteArrayOutputStream.reset();
                long s = System.currentTimeMillis();
                mDrawingBoard.compress(Bitmap.CompressFormat.JPEG, 60, mByteArrayOutputStream);
                byte[] jpgBytes = mByteArrayOutputStream.toByteArray();
                Log.d(TAG, "compress " + (System.currentTimeMillis() - s));
                for (ClientHandler clientHandler : mClientHandlers) {
                    clientHandler.sendData(jpgBytes);
                }
                mUIHandler.post(mDrawTask);
            } catch (Exception e) {
            }
//            mUIHandler.postDelayed(mDrawTask, delay);
        }
    }
}
