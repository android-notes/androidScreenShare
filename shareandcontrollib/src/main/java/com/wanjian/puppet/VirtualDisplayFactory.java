package com.wanjian.puppet;

import android.os.Handler;
import android.view.Surface;

public interface VirtualDisplayFactory {
    VirtualDisplay createVirtualDisplay(String str, int i, int i2, int i3, int i4, Surface surface, Handler handler);

    void release();
}