package com.wanjian.puppet;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.ServiceManager;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.SurfaceControl;

public class ScreenUtils {

    public static Point getDisplaySize() {
        return new Point(sDisplaySize.x, sDisplaySize.y);
    }

    private static final Point sDisplaySize = new Point();

    static {
        if (VERSION.SDK_INT >= 18) {
            IWindowManager wm = Stub.asInterface((IBinder) ServiceManager.getService("window"));
            wm.getInitialDisplaySize(0, sDisplaySize);
        } else if (VERSION.SDK_INT == 17) {
            DisplayInfo di =
                    IDisplayManager.Stub.asInterface((IBinder) ServiceManager.getService("display")).getDisplayInfo(0);
            sDisplaySize.x = di.logicalWidth;
            sDisplaySize.y = di.logicalHeight;
        } else {
            IWindowManager wm = Stub.asInterface((IBinder) ServiceManager.getService("window"));
            wm.getRealDisplaySize(sDisplaySize);
        }
    }

    public static Bitmap screenshot() throws Exception {
        Point size = ScreenUtils.getDisplaySize();
        Bitmap b;
        if (Build.VERSION.SDK_INT <= 17) {
            String surfaceClassName = "android.view.Surface";
            b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[] {int.class, int.class})
                    .invoke(null, new Object[] {size.x, size.y});

        } else if (Build.VERSION.SDK_INT < 28) {
            b = SurfaceControl.screenshot(size.x, size.y);
        } else {
            b = SurfaceControl.screenshot(new Rect(0, 0, size.x, size.y), size.x, size.y, 0);
        }
        return b;
    }
}