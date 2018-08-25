package com.wanjian.puppet;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.IDisplayManager;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.Surface;

import java.lang.reflect.Method;

public class SurfaceControlVirtualDisplayFactory implements VirtualDisplayFactory {
    private static final String LOGTAG = "SCVDF";
    Rect displayRect;
    Point displaySize = getCurrentDisplaySize();

    public static Point getCurrentDisplaySize() {
        return getCurrentDisplaySize(true);
    }

    public static Point getCurrentDisplaySize(boolean rotate) {
        try {
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
            Point displaySize = new Point();
            IWindowManager wm;
            int rotation;
            if (VERSION.SDK_INT >= 18) {
                wm = Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));
                wm.getInitialDisplaySize(0, displaySize);
//                rotation = wm.getRotation();
            } else if (VERSION.SDK_INT == 17) {
                DisplayInfo di = IDisplayManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"display"})).getDisplayInfo(0);
                displaySize.x = ((Integer) DisplayInfo.class.getDeclaredField("logicalWidth").get(di)).intValue();
                displaySize.y = ((Integer) DisplayInfo.class.getDeclaredField("logicalHeight").get(di)).intValue();
                rotation = ((Integer) DisplayInfo.class.getDeclaredField("rotation").get(di)).intValue();
            } else {
                wm = Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));
                wm.getRealDisplaySize(displaySize);
//                rotation = wm.getRotation();
            }
//            if ((rotate && rotation == 1) || rotation == 3) {
//                int swap = displaySize.x;
//                displaySize.x = displaySize.y;
//                displaySize.y = swap;
//            }
            return displaySize;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int dpi, int flags, Surface surface, Handler handler) {
        try {
            Class surfaceControlClass = Class.forName("android.view.SurfaceControl");
            Class cls = surfaceControlClass;
            final IBinder token = (IBinder) cls.getDeclaredMethod("createDisplay", new Class[]{String.class, Boolean.TYPE}).invoke(null, new Object[]{name, Boolean.valueOf(false)});
            cls = surfaceControlClass;
            Method setDisplaySurfaceMethod = cls.getDeclaredMethod("setDisplaySurface", new Class[]{IBinder.class, Surface.class});
            cls = surfaceControlClass;
            final Method setDisplayProjectionMethod = cls.getDeclaredMethod("setDisplayProjection", new Class[]{IBinder.class, Integer.TYPE, Rect.class, Rect.class});
            cls = surfaceControlClass;
            Method setDisplayLayerStackMethod = cls.getDeclaredMethod("setDisplayLayerStack", new Class[]{IBinder.class, Integer.TYPE});
            final Method openTransactionMethod = surfaceControlClass.getDeclaredMethod("openTransaction", new Class[0]);
            final Method closeTransactionMethod = surfaceControlClass.getDeclaredMethod("closeTransaction", new Class[0]);
            final Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
            this.displayRect = new Rect(0, 0, width, height);
            Rect layerStackRect = new Rect(0, 0, this.displaySize.x, this.displaySize.y);
            openTransactionMethod.invoke(null, new Object[0]);
            setDisplaySurfaceMethod.invoke(null, new Object[]{token, surface});
            setDisplayProjectionMethod.invoke(null, new Object[]{token, Integer.valueOf(0), layerStackRect, this.displayRect});
            setDisplayLayerStackMethod.invoke(null, new Object[]{token, Integer.valueOf(0)});
            closeTransactionMethod.invoke(null, new Object[0]);
            cls = surfaceControlClass;
            final Method destroyDisplayMethod = cls.getDeclaredMethod("destroyDisplay", new Class[]{IBinder.class});
            return new VirtualDisplay() {
                IRotationWatcher watcher;
                IWindowManager wm = Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));

                public void release() {
                    Log.i(SurfaceControlVirtualDisplayFactory.LOGTAG, "VirtualDisplay released");
                    this.wm = null;
                    this.watcher = null;
                    try {
                        destroyDisplayMethod.invoke(null, new Object[]{token});
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                }
            };
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public Point getDisplaySize() {
        return new Point(this.displaySize);
    }

    public Rect getDisplayRect() {
        return this.displayRect;
    }

    public void release() {
        Log.i(LOGTAG, "factory released");
    }
}