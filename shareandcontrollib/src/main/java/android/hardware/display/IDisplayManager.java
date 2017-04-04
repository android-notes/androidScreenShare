package android.hardware.display;

import android.os.IBinder;
import android.view.DisplayInfo;

/**
 * Created by wanjian on 2017/4/4.
 */

public interface IDisplayManager {
    DisplayInfo getDisplayInfo(int i);

    abstract class Stub {
        public static IDisplayManager asInterface(IBinder invoke) {
            return null;
        }
    }
}
