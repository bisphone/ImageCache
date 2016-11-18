package imageCache;

import android.util.Log;

/**
 * Created by shayan on 11/16/16.
 */

final class ImageCacheLog {

    private final static String TAG = "ImageCache";

    private static ImageCacheLog imageCacheLog;

    private boolean logEnable = true;

    private ImageCacheLog() {
    }

    static ImageCacheLog getInstance() {
        if (imageCacheLog == null) {
            synchronized (ImageCacheLog.class) {
                if (imageCacheLog == null) {
                    imageCacheLog = new ImageCacheLog();
                }
            }
        }

        return imageCacheLog;
    }

    void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }

    void w(String message) {
        if (!logEnable) {
            return;
        }

        Log.w(TAG, message);
    }

}
